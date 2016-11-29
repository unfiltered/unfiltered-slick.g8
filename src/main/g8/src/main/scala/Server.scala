package com.example

import scala.slick.driver.H2Driver.simple._
import SlickSetup._

object Server extends App {
  unfiltered.jetty.Server.anylocal
    .plan(DogRun)
    .resources(new java.net.URL(getClass().getResource("/www/css"), "."))
    .run({ svr =>
      db.withSession(initDb(_))
    }, { svr =>
      datasource.close()
    })
}
