package com.example

import scala.slick.driver.H2Driver.simple._

/*
  Example Data Model
  
  +------------------+
  | DOG              |        
  +------------------+        +---------+
  | ID               |        | BREED   |
  | NAME             |        +---------+
  | BREED_ID---------+--------| ID      |
  +------------------+        | NAME    |
                              +---------+
*/
// result types
case class Breed(id: Int, name: String)
case class Dog(id: Int, name: String, breedId: Int)

// schema description
class Dogs(tag: Tag) extends Table[Dog](tag, "DOG") {
  def id = column[Int]("ID")
  def name = column[String]("NAME")
  def breedId = column[Int]("BREED_ID")
  def * = (id, name, breedId) <> (Dog.tupled, Dog.unapply)
}
class Breeds(tag: Tag) extends Table[Breed](tag, "BREED") {
  def id = column[Int]("ID")
  def name = column[String]("NAME")
  def * = (id, name) <> (Breed.tupled, Breed.unapply)
}

object setup {
  val db = Database.forURL("jdbc:h2:mem:test", driver = "org.h2.Driver")

  def Dogs = TableQuery[Dogs]
  def Breeds = TableQuery[Breeds]

  def initDb(implicit session: Session): Unit = {
    (Breeds.ddl ++ Dogs.ddl).create

    Breeds ++= Seq(
      Breed(1, "Collie"),
      Breed(2, "Terrier")
    )

    Dogs ++= Seq(
      Dog(1, "Lassie", 1),
      Dog(2, "Toto", 2),
      Dog(3, "Wishbone", 2)
    )
  }
}
