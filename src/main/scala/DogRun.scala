package com.example

import scala.slick.driver.H2Driver.simple._

import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._, Directives._

import setup._

object DogRun extends unfiltered.filter.Plan {

  def dogsOfBreed(breed: Breed) =
    for (dog <- Dogs if dog.breedId === breed.id)
    yield dog

  def breedOfId(id: Int) =
    Breeds.filter { _.id === id }

  object IdBreed {
    def unapply(idStr: String)(implicit sess: Session) = {
      for {
        id <- scala.util.Try { idStr.toInt }.toOption
        breed <- breedOfId(id).list.headOption
      } yield breed
    }
  }

  def SlickIntent[A,B](intent: Session => unfiltered.Cycle.Intent[A,B]):
      unfiltered.Cycle.Intent[A,B] = {
    case req =>
      db.withSession { implicit session =>
        intent(session).lift(req).getOrElse(Pass)
      }
  }


  def nameParam(errorPage: Option[String] => ResponseFunction[Any]) = {
    def ep = errorPage(Some("Please enter a name"))
    data.as.String.trimmed ~>
      data.as.String.nonEmpty.fail( (_,_) => ep ) ~>
      data.Requiring[String].fail( _ => ep ) named "name"
  }

  def intent = SlickIntent { implicit session =>
    Directive.Intent.Path {
      case "/" =>
        def breedPage(error: Option[String] = None) =
          page("Breeds")(breedList ++ entry(error))
        (for (_ <- GET) yield breedPage()) orElse
        (for {
          _ <- POST
          name <- nameParam(breedPage)
        } yield {
          BreedsForInsert += name
          breedPage()
        })
      case Seg("breed" :: IdBreed(breed) :: Nil) =>
        def dogPage(error: Option[String] = None) =
          page(breed.name)(dogList(breed) ++ entry(error))
        (for (_ <- GET)
        yield dogPage()) orElse
        (for {
          _ <- POST
          name <- nameParam(dogPage)
        } yield {
          DogsForInsert += (name, breed.id)
          dogPage()
        })
    }
  }

  def page(title: String)(content: scala.xml.NodeSeq)(implicit sess: Session) =
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

  def breedList(implicit sess: Session) =
    <div>
      <h1>Breeds</h1>
      <ul> {
        for (breed <- Breeds.list)
        yield <li><a href={"/breed/" + breed.id.toString}>{breed.name}</a></li>
      } </ul>
    </div>

  def dogList(breed: Breed)(implicit sess: Session) =
    <div>
      <a href="/">Breeds</a>
      <h1>{ breed.name }</h1>
      <ul> {
        for (dog <- dogsOfBreed(breed).list)
        yield <li><span>{dog.name}</span></li>
      } </ul>
    </div>

  def entry(error: Option[String]) = {
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
