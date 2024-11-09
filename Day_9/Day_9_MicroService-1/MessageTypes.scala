import akka.actor.{Actor, ActorRef}
import org.apache.kafka.clients.producer.ProducerRecord
import spray.json._
import JsonFormat._

// Actor for listening to cloud messages
class CloudListener(messageGatherer: ActorRef) extends Actor {
  def receive: Receive = {
    case msg: Message =>
      println(s"[CloudListener] Received message: ${msg.message}")
      messageGatherer ! msg
  }
}

// Actor for listening to network messages
class NetworkListener(messageGatherer: ActorRef) extends Actor {
  def receive: Receive = {
    case msg: Message =>
      println(s"[NetworkListener] Received message: ${msg.message}")
      messageGatherer ! msg
  }
}

// Actor for listening to app messages
class AppListener(messageGatherer: ActorRef) extends Actor {
  def receive: Receive = {
    case msg: Message =>
      println(s"[AppListener] Received message: ${msg.message}")
      messageGatherer ! msg
  }
}

// Actor for gathering messages and sending to a Kafka topic
class MessageGatherer extends Actor {
  def receive: Receive = {
    case msg: Message =>
      val producer = KafkaProducerFactory.createProducer()
      val jsonString = msg.toJson.toString()
      val record = new ProducerRecord[String, String]("consolidated-messages", jsonString)
      producer.send(record)
  }
}