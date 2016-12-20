package com.example

import SlickSetup._

object Server extends App {
  unfiltered.jetty.Server.anylocal
    .plan(DogRun)
    .resources(new java.net.URL(getClass().getResource("/www/css"), "."))
    .run({ _ =>
      initDb()
    }, { svr =>
      datasource.close()
    })
}
