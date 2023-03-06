package com.example

import slick.driver.H2Driver.api._
import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._
import Directives._
import SlickSetup._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.Elem

object DogRun extends unfiltered.filter.Plan {

  def breedOfId(id: Int) =
    getOrElse(
      Await.result(SlickSetup.db.run(Breeds.ofId(id).result.headOption), 1.seconds),
      NotFound ~> ResponseString("Breed not found")
    )

  object BreedId {
    def unapply(idStr: String): Option[Int] = unfiltered.util.Of.Int.unapply(idStr)
  }

  def nameParam(errorPage: Option[String] => ResponseFunction[Any]) = {
    def enterName = errorPage(Some("Please enter a name"))
    (data.as.String.trimmed ~>
      data.as.String.nonEmpty.fail( (_,_) => enterName ) ~>
      data.Requiring[String].fail( _ => enterName )
    ) named "name"
  }

  /** Provides an implicit session for the request-response cycle. */
  def SlickCycle[A,B](intent: unfiltered.Cycle.Intent[A,B]):
      unfiltered.Cycle.Intent[A,B] = {
    case req =>
      intent.lift(req).getOrElse(Pass)
  }

  def intent = SlickCycle {
    Directive.Intent.Path {
      case "/" =>
        def breedPage(error: Option[String] = None) =
          page("Breeds")(breedList() ++ entry(error))
        (for (_ <- GET) yield breedPage()) orElse
        (for {
          _ <- POST
          name <- nameParam(breedPage)
        } yield {
          Await.result(db.run(Breeds.forInsert += name), 1.seconds)
          breedPage()
        })
      case Seg("breed" :: BreedId(breedId) :: Nil) =>
        def dogPage(breed: Breed, error: Option[String] = None) =
          page(breed.name)(dogList(breed) ++ entry(error))
        (for {
          _ <- GET
          _ <- commit
          breed <- breedOfId(breedId)
        } yield dogPage(breed)) orElse
        (for {
          _ <- POST
          breed <- breedOfId(breedId)
          name <- nameParam(dogPage(breed, _))
        } yield {
          Await.result(db.run(Dogs.forInsert += (name -> breed.id)), 1.seconds)
          dogPage(breed)
        })
    }
  }

  def page(title: String)(content: scala.xml.NodeSeq): Html5 =
    Html5(
      <html>
      <head>
        <meta name="viewport" content="width=320"/>
        <title>{title}</title>
        <link rel="stylesheet" type="text/css" href="/css/app.css"/>
      </head>
      <body>
        <div id="container">
          { content }
        </div>
      </body>
      </html>
    )

  def breedList(): Elem =
    <div>
      <h1>Breeds</h1>
      <ul> {
        for (breed <- Await.result(db.run(Breeds.sortBy(_.name).result), 1.seconds))
        yield <li><a href={"/breed/" + breed.id.toString}>{breed.name}</a></li>
      } </ul>
    </div>

  def dogList(breed: Breed): Elem =
    <div>
      <a href="/">Breeds</a>
      <h1>{ breed.name }</h1>
      <ul> {
        for (dog <- Await.result(db.run(Dogs.ofBreed(breed).sortBy(_.name).result), 1.seconds))
        yield <li><span>{dog.name}</span></li>
      } </ul>
    </div>

  def entry(error: Option[String]): scala.xml.NodeBuffer = {
    val autofocus = error.map { _ => xml.Text("true") }
    <div>
      {
        for (e <- error.toSeq)
        yield <ul><li><span class="error">{e}</span></li></ul>
      }
    </div>
    <form method="POST">
      <div><input autofocus={ autofocus } type="text" name="name" /></div>
      <div><a class="button" href="#" onclick="document.forms[0].submit(); return false;">
        +
      </a></div>
    </form>
  }
}
