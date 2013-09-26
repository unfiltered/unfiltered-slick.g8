package com.example

import scala.slick.driver.H2Driver.simple._

import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._, Directives._

import setup._

object DogRun extends unfiltered.filter.Plan {

  def asBreed = data.Fallible[Int,Breed] { id =>
    db.withSession { implicit sess =>
      Breeds.filter { _.id === id }.list.headOption
    }
  }

  implicit def implyBreed =
    data.as.String ~> data.as.Int ~> asBreed

  def intent = Directive.Intent {
    case _ =>
      for (breed <- data.as.Option[Breed] named "breed_id")
      yield page(breed)
  }

  def page(breed: Option[Breed]) =
    Html5(db.withSession { implicit session =>
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
    })

  def breedSelect(implicit sess: Session) =
    <select name="breed_id"> {
      Breeds.list.map { breed =>
        <option value={breed.id.toString}>{breed.name}</option>
      }
    } </select>
}
