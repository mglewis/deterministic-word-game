package uk.co.mglewis.server

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import uk.co.mglewis.core.Dictionary

class GameController(secretPath: String) extends Controller {

  private val dictionary = new Dictionary("resources/word_list.txt")

  get("/ping") { request: Request =>
    info("ping")
    "Pong " + request.params.getOrElse("name", "unknown")
  }

  get(s"/$secretPath/test") { request: Request =>
    "Secret found."
  }

  post(s"/$secretPath/update") { request: Request =>
    info(s"Message received $request")
  }

}
