package com.example

import scala.slick.driver.H2Driver.simple._
import setup._

object Server extends App {
  unfiltered.jetty.Http.anylocal
    .filter(DogRun)
    .resources(new java.net.URL(getClass().getResource("/www/css"), "."))
    .run({ svr =>
      db.withSession(initDb(_))
      unfiltered.util.Browser.open(svr.url)
    })
}
