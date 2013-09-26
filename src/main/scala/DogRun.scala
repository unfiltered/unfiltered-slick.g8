package com.example

import scala.slick.driver.H2Driver.simple._

import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._, Directives._

import setup._

object DogRun extends unfiltered.filter.Plan {

  def asBreed(implicit sess: Session) = data.Fallible[Int,Breed] { id =>
    Breeds.filter { _.id === id }.list.headOption
  }

  implicit def implyBreed(implicit sess: Session) =
    data.as.String ~> data.as.Int ~> asBreed

  def intent = { case req =>
    db.withSession { implicit session =>
    unfiltered.filter.Intent { Directive.Intent {
      case _ =>
        for (breed <- data.as.Option[Breed] named "breed_id")
        yield page(breed)
    } } (req)
    }
  }

  def page(breed: Option[Breed])(implicit sess: Session) =
    Html5(
      <html>
      <body>
        <form method="get">
        <div>{ breedSelect }</div>
        { breed.toSeq.map { b =>
          <div>{ b.name }</div>
        } }
        <input type="submit" />
      </form>
      </body>
      </html>
    )

  def breedSelect(implicit sess: Session) =
    <select name="breed_id"> {
      Breeds.list.map { breed =>
        <option value={breed.id.toString}>{breed.name}</option>
      }
    } </select>
}
