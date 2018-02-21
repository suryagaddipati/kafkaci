package kafkaci.util

object StoreHelpers {
  import org.apache.kafka.streams.KafkaStreams
  import org.apache.kafka.streams.errors.InvalidStateStoreException
  import org.apache.kafka.streams.state.QueryableStoreType

  def waitUntilStoreIsQueryable[T](storeName: String, queryableStoreType: QueryableStoreType[T], streams: KafkaStreams): T =
    try
      streams.store(storeName, queryableStoreType)
    catch {
      case ignored: InvalidStateStoreException =>
        // store not yet ready for querying
        Thread.sleep(100)
        waitUntilStoreIsQueryable(storeName,queryableStoreType,streams)
    }

}
