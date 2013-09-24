package com.example

import scala.slick.driver.H2Driver.simple._

import unfiltered.request._
import unfiltered.response._

import setup._

object DogRun extends unfiltered.filter.Plan {
  def intent = {
    case _ => 
      db.withTransaction { implicit session =>
        Html5(
          <select> {
            Breeds.list.map { breed =>
              <option value={breed.id.toString}>{breed.name}</option>
            }
          } </select>
        )
      }
  }
}
