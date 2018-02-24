package kafkaci.web

import org.scalatra.test.scalatest._

class WebAppTests extends ScalatraFunSuite {

  addServlet(classOf[WebApp], "/*")

  test("GET / on KafkaCIScalatraServlet should return status 200"){
    get("/"){
      status should equal (200)
    }
  }

}
