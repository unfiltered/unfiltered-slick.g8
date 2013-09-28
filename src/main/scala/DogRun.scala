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
        Pass.fold(
          intent(session),
          (_: HttpRequest[A]) => Pass,
          (req: HttpRequest[A], rf: ResponseFunction[B]) =>
            rf
        )(req)
      }
  }


  def nameParam(errorPage: Option[String] => ResponseFunction[Any]) = 
    data.as.String.trimmed ~> data.as.String.nonEmpty.fail (
      (_,_) => errorPage(Some("Name was empty."))
    ) named "name"

  def intent = SlickIntent { implicit session =>
    Directive.Intent.Path {
      case "/" =>
        def breedPage(error: Option[String] = None) =
          page("Breeds")(breedList ++ errorMsg(error) ++ entry("Breed"))
        (for (_ <- GET) yield breedPage()) orElse
        (for {
          _ <- POST
          name <- nameParam(breedPage)
        } yield {
          BreedsForInsert += name.get // todo: make it not option
          breedPage()
        })
      case Seg("breed" :: IdBreed(breed) :: Nil) =>
        def dogPage(error: Option[String] = None) =
          page(breed.name)(dogList(breed) ++ errorMsg(error) ++ entry("Dog"))
        (for (_ <- GET)
        yield dogPage()) orElse
        (for {
          _ <- POST
          name <- nameParam(dogPage)
        } yield {
          DogsForInsert += (name.get, breed.id)
          dogPage()
        })
    }
  }

  def page(title: String)(content: scala.xml.NodeSeq)(implicit sess: Session) =
    Html5(
      <html>
      <body>
        <h1>{ title}</h1>
        { content }
      </body>
      </html>
    )

  def breedList(implicit sess: Session) =
    <ul> {
      for (breed <- Breeds.list)
      yield <li><a href={"/breed/" + breed.id.toString}>{breed.name}</a></li>
    } </ul>

  def dogList(breed: Breed)(implicit sess: Session) =
    <ul> {
      for (dog <- dogsOfBreed(breed).list)
      yield <li>{dog.name}</li>
    } </ul>

  def entry(kind: String)  =
    <form method="POST">
      <div><input type="text" name="name" /></div>
      <div><input type="submit" value={ "Add " + kind } /></div>
    </form>

  def errorMsg(error: Option[String]) =
    for (e <- error.toSeq)
    yield <div>{e}</div>
}
