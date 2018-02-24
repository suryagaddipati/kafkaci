package kafkaci.web

import kafkaci.Topics.{GITHUB_WEBHOOKS_COUNT_STORE, PROJECTS_STORE}
import kafkaci.models.Project
import kafkaci.producers.Producers
import kafkaci.util.StoreHelpers.waitUntilStoreIsQueryable
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.scalatra._
import org.scalatra.forms.{FormSupport, label, mapping, maxlength, required, text}
import org.scalatra.i18n.I18nSupport

class WebApp(streams : KafkaStreams) extends ScalatraServlet with FormSupport with I18nSupport {

  get("/") {
    views.html.hello()
  }

  get("/webhook-count"){
    val repoName = params("repo")
    val webhookCountStore = waitUntilStoreIsQueryable(GITHUB_WEBHOOKS_COUNT_STORE, QueryableStoreTypes.keyValueStore[String, Long], streams)
    val count = webhookCountStore.get(repoName)
    Ok(count)
  }

  case class NewProjectForm(repo: String)
  val form = mapping(
    "repo"     -> label("Repository", text(required, maxlength(100))),
  )(NewProjectForm.apply)

  get("/new-project"){
    views.html.newProject()
  }

  post("/new-project"){
    validate(form)(
      errors => BadRequest(errors),
      form   => {
        Created(Producers.projectCreateRequest(form.repo))
      }
    )
  }

  get("/project"){
    val name = params("name")
    val projectStore = waitUntilStoreIsQueryable(PROJECTS_STORE, QueryableStoreTypes.keyValueStore[String,Project],streams)
    Ok(projectStore.get(name))
  }

  post("/new-build"){

  }


}
