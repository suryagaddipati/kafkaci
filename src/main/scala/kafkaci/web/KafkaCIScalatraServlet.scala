package kafkaci.web

import org.scalatra._

class KafkaCIScalatraServlet extends ScalatraServlet {

  get("/") {
    views.html.hello()
  }

}
