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
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def breedId = column[Int]("BREED_ID")
  def * = (id, name, breedId) <> (Dog.tupled, Dog.unapply)
  def forInsert = (name, breedId) <> (
    { t: (String,Int) => Dog(0, t._1, t._2)},
    { (u: Dog) => Some(u.name, u.breedId)}
  )
}
class Breeds(tag: Tag) extends Table[Breed](tag, "BREED") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def * = (id, name) <> (Breed.tupled, Breed.unapply)
}

object setup {
  val db = Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  def Dogs = TableQuery[Dogs]
  def Breeds = TableQuery[Breeds]

  def BreedsForInsert = Breeds.map { b => b.name }
  def DogsForInsert = Dogs.map { d => (d.name, d.breedId) }

  def initDb(implicit session: Session): Unit = {
    (Breeds.ddl ++ Dogs.ddl).create

    BreedsForInsert ++= Seq(
      "Collie",
      "Terrier"
    )

    DogsForInsert ++= Seq(
      ("Lassie", 1),
      ("Toto", 2),
      ("Wishbone", 2)
    )
  }
}
