package kafkaci

import com.lightbend.kafka.scala.streams.{KGroupedStreamS, KStreamS, KTableS, StreamsBuilderS}
import kafkaci.Topics._
import kafkaci.models.{Build, Project}
import kafkaci.models.Serdes.{buildSerde, githubWebhookSerde,projectSerde}
import kafkaci.models.github.GithubWebhook
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.{Consumed, Topology}

object Topology {
  def get: Topology = {
    val builder = new StreamsBuilderS()

    //repo-name/hook
    val githubWebhooks: KStreamS[String,GithubWebhook] = builder.stream[String, GithubWebhook](GITHUB_WEBHOOKS,Consumed.`with`(Serdes.String,githubWebhookSerde))
    val githubWebhookCount: KTableS[String,Long] = githubWebhooks.map((k,v) => (k,v.repo)).groupBy((k,v) =>  k ).count(GITHUB_WEBHOOKS_COUNT)

    val projectCreateRequests: KStreamS[String,String] = builder.stream[String, String](PROJECT_CREATE_REQUESTS)
    val projects :KStreamS[String,Project] = projectCreateRequests.map((k,v) => (k,Project(k)))
    projects.to(PROJECTS,Produced.`with`(Serdes.String,projectSerde))

    val projectTable:KTableS[String,Project] = builder.table(PROJECTS)


    //repo-name/build
    val builds: KStreamS[String,Build] = githubWebhooks.leftJoin(githubWebhookCount,(hook:GithubWebhook,count: Long)  => Build(count+1,hook))

    builds.to(BUILDS, Produced.`with`(Serdes.String,buildSerde))
    //repo-name/builds
    val buildTable: KGroupedStreamS[String,Build] = builds.groupBy((k,v)=>k)
    //    buildTable.aggregate()
    builder.build
  }
}
