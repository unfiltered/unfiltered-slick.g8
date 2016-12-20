package com.example

import slick.driver.H2Driver.api._
import com.mchange.v2.c3p0.ComboPooledDataSource

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

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
object SlickSetup {
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

 
  val datasource = new ComboPooledDataSource
  datasource.setDriverClass("org.h2.Driver")
  datasource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")

  val db = Database.forDataSource(datasource)

  def Dogs = TableQuery[Dogs]
  def Breeds = TableQuery[Breeds]

  implicit class BreedsExtensions(val breeds: Query[Breeds, Breed, Seq]) extends AnyVal{
    def ofId(id: Int) =
      for (b <- Breeds if b.id === id)
      yield b
    def forInsert = Breeds.map { b => b.name }
  }

  implicit class DogsExtensions(val breeds: Query[Dogs, Dog, Seq]) extends AnyVal{
    def ofBreed(breed: Breed) =
      for (d <- Dogs if d.breedId === breed.id)
      yield d

    def forInsert = Dogs.map { d => (d.name, d.breedId) }
  }

  def initDb(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val result = Seq(
      (Breeds.schema ++ Dogs.schema).create,
      Breeds.forInsert ++= Seq(
        "Collie",
        "Terrier"
      ),
      Dogs.forInsert ++= Seq(
        ("Lassie", 1),
        ("Toto", 2),
        ("Wishbone", 2)
      )
    ).map(db.run(_))

    Await.result(Future.sequence(result), 5.seconds)
  }
}
