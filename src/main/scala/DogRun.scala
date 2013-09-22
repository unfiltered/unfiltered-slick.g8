package com.example

import scala.slick.driver.H2Driver.simple._

import unfiltered.request._
import unfiltered.response._

import setup._

object DogRun extends unfiltered.filter.Plan {
  def intent = {
    case _ => 
      val output = db.withTransaction { implicit session =>
        (for {
          breed <- Breeds
          dog <- Dogs if dog.breedId === breed.id
        } yield (dog, breed)).list
      }
      ResponseString(output.mkString("\n"))
  }
}
