package uk.co.mglewis.server

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter

object ServerMain extends Server

class Server extends HttpServer {

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add(new GameController)
  }

}
