package kafkaci

import com.lightbend.kafka.scala.streams.{KStreamS, KTableS, StreamsBuilderS}
import kafkaci.Topics._
import kafkaci.models.Project
import kafkaci.models.Serdes.{githubWebhookSerde, projectSerde}
import kafkaci.models.github.GithubWebhook
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.{Consumed, Topology}

object KafkaCITopology {




  def get: Topology = {
    val builder = new StreamsBuilderS()

    //repo-name/hook
    val githubWebhooks: KStreamS[String,GithubWebhook] = builder.stream[String, GithubWebhook](GITHUB_WEBHOOKS,Consumed.`with`(Serdes.String,githubWebhookSerde))
    val githubWebhookCount: KTableS[String,Long] = githubWebhooks.map((k,v) => (k,v.repo)).groupBy((k,v) =>  k ).count(GITHUB_WEBHOOKS_COUNT_STORE)


//    val projectStoreSupplier = Stores.inMemoryKeyValueStore(PROJECTS_STORE)
//    val projectsStoreBuilder = Stores.keyValueStoreBuilder(projectStoreSupplier, Serdes.String, projectSerde)
//    builder.addStateStore(projectsStoreBuilder)

    val projectCreateRequests: KStreamS[String,String] = builder.stream[String, String](PROJECT_CREATE_REQUESTS)
    val projectStream = projectCreateRequests.map((k,v) => (k,Project(v)))
      projectStream.to(PROJECTS,Produced.`with`(Serdes.String,projectSerde))
    val m :Materialized[String, Project, KeyValueStore[Bytes, Array[Byte]]] = Materialized.as[String,Project,  KeyValueStore[Bytes, Array[Byte]]](PROJECTS_STORE).withKeySerde(Serdes.String).withValueSerde(projectSerde)
        val projectTable = builder.table(PROJECTS,Consumed.`with`(Serdes.String,projectSerde) )
    projectTable.toStream.print(Printed.toSysOut[String,Project ].withLabel("Project-KTable"))
    //    projectTable.toStream.print(Printed.toSysOut[String,Project])
    ////    projectCreateRequests.transformValues(()=>  new ProjectRequestToProjectTransformer(),PROJECTS_STORE)
    //    projectStream.map((k,v)=>{
    //       print(k)
    //      (k,v)
    //    })





//    //repo-name/build
//    val builds: KStreamS[String,Build] = githubWebhooks.leftJoin(githubWebhookCount,(hook:GithubWebhook,count: Long)  => Build(count+1,hook))
//
//    builds.to(BUILDS, Produced.`with`(Serdes.String,buildSerde))
//    //repo-name/builds
//    val buildTable: KGroupedStreamS[String,Build] = builds.groupBy((k,v)=>k)
    //    buildTable.aggregate()
    builder.build
  }
}
