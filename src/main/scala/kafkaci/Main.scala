package kafkaci

import java.util.Properties

import com.lightbend.kafka.scala.streams.{KGroupedStreamS, KStreamS, KTableS, StreamsBuilderS}
import kafkaci.Topics._
import kafkaci.models.Build
import kafkaci.models.Serdes._
import kafkaci.models.github.GithubWebhook
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.{Consumed, KafkaStreams, StreamsConfig}

object Main {
  def main(args: Array[String]) {
    val streams = new KafkaStreams(Topology.get, streamsConfiguration)
    streams.cleanUp()
    streams.start()
    ApiServer.start(streams)

  }


  def streamsConfiguration(): Properties ={
    val appServerPort = 9000
    val streamsConfiguration = new Properties()
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "KafkaCI")

    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, "localhost:" + appServerPort)
    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafkaCIState")
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "100")

    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    streamsConfiguration
  }


}