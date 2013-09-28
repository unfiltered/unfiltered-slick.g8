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


  def intent = SlickIntent { implicit session =>
    {
      case Path("/") =>
        page("Breeds")(breedList)
      case Path(Seg("breed" :: IdBreed(breed) :: Nil)) =>
        page(breed.name)(dogList(breed))
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
      Breeds.list.map { breed =>
        <li><a href={"/breed/" + breed.id.toString}>{breed.name}</a></li>
      }
    } </ul>

  def dogList(breed: Breed)(implicit sess: Session) =
    <ul> {
      for (dog <- dogsOfBreed(breed).list)
      yield <li>{dog.name}</li>
    } </ul>
}
