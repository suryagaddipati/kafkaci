package kafkaci

import com.lightbend.kafka.scala.streams.{KGroupedStreamS, KStreamS, KTableS, StreamsBuilderS}
import kafkaci.Topics._
import kafkaci.models.{Build, Project}
import kafkaci.models.Serdes.{buildSerde, githubWebhookSerde, projectSerde}
import kafkaci.models.github.GithubWebhook
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{Printed, Produced, Transformer, ValueTransformer}
import org.apache.kafka.streams.processor.{ProcessorContext, StateStore}
import org.apache.kafka.streams.state.{KeyValueBytesStoreSupplier, KeyValueStore, StoreBuilder, Stores}
import org.apache.kafka.streams.{Consumed, Topology}

object Topology {




  def get: Topology = {
    val builder = new StreamsBuilderS()

    //repo-name/hook
    val githubWebhooks: KStreamS[String,GithubWebhook] = builder.stream[String, GithubWebhook](GITHUB_WEBHOOKS,Consumed.`with`(Serdes.String,githubWebhookSerde))//.selectKey((k,v) => v.repo)
    val githubWebhookCount: KTableS[String,Long] = githubWebhooks.map((k,v) => (k,v.repo)).groupBy((k,v) =>  k ).count(GITHUB_WEBHOOKS_COUNT_STORE)


    val projectStoreSupplier = Stores.inMemoryKeyValueStore(PROJECTS_STORE)
    val projectsStoreBuilder = Stores.keyValueStoreBuilder(projectStoreSupplier, Serdes.String, projectSerde)
    builder.addStateStore(projectsStoreBuilder)

    val projectCreateRequests: KStreamS[String,String] = builder.stream[String, String](PROJECT_CREATE_REQUESTS)
    projectCreateRequests.transformValues(()=>  new ProjectRequestToProjectTransformer(),PROJECTS_STORE)
//    projects.print(Printed.toSysOut[String, Project].withLabel("Projects").withKeyValueMapper((k,v) => v.name))





    //repo-name/build
    val builds: KStreamS[String,Build] = githubWebhooks.leftJoin(githubWebhookCount,(hook:GithubWebhook,count: Long)  => Build(count+1,hook))

    builds.to(BUILDS, Produced.`with`(Serdes.String,buildSerde))
    //repo-name/builds
    val buildTable: KGroupedStreamS[String,Build] = builds.groupBy((k,v)=>k)
    //    buildTable.aggregate()
    builder.build
  }

  class ProjectRequestToProjectTransformer extends  ValueTransformer[String,Project]{
    var stateStore: KeyValueStore[String,Project] = null
    override def init(context: ProcessorContext): Unit = {
      stateStore = context.getStateStore(PROJECTS_STORE).asInstanceOf[KeyValueStore[String,Project]]
    }

    override def punctuate(timestamp: Long): Project = null

    override def transform(value: String): Project = {
      val project = Project(value)
      stateStore.put(value,project)
      project
    }

    override def close(): Unit = { }
  }
}
