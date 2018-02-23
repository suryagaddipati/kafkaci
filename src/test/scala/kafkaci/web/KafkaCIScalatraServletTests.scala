package kafkaci.web

import org.scalatra.test.scalatest._

class KafkaCIScalatraServletTests extends ScalatraFunSuite {

  addServlet(classOf[KafkaCIScalatraServlet], "/*")

  test("GET / on KafkaCIScalatraServlet should return status 200"){
    get("/"){
      status should equal (200)
    }
  }

}
