import akka.actor.{Actor, ActorSystem, Props, ActorRef}
import akka.kafka.ConsumerSettings
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import spray.json._
import JsonFormat._

// Main object for creating and running the microservice
object MicroserviceApp extends App {
  implicit val system: ActorSystem = ActorSystem("MessageConsumingSystem")

  // Create the MessageGatherer actor
  private val messageGatherer = system.actorOf(Props[MessageGatherer], "messageGatherer")

  // Create the listener actors
  private val cloudListener = system.actorOf(Props(new CloudListener(messageGatherer)), "cloudListener")
  private val networkListener = system.actorOf(Props(new NetworkListener(messageGatherer)), "networkListener")
  private val appListener = system.actorOf(Props(new AppListener(messageGatherer)), "appListener")

  // Kafka consumer settings
  private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("message-listeners-group")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  // Subscribe to cloud messages
  Consumer.plainSource(consumerSettings, akka.kafka.Subscriptions.topics("cloud-message"))
    .map(record => record.value().parseJson.convertTo[Message])
    .runWith(Sink.foreach(message => cloudListener ! message))

  // Subscribe to network messages
  Consumer.plainSource(consumerSettings, akka.kafka.Subscriptions.topics("network-message"))
    .map(record => record.value().parseJson.convertTo[Message])
    .runWith(Sink.foreach(message => networkListener ! message))

  // Subscribe to app messages
  Consumer.plainSource(consumerSettings, akka.kafka.Subscriptions.topics("app-message"))
    .map(record => record.value().parseJson.convertTo[Message])
    .runWith(Sink.foreach(message => appListener ! message))

  println("Microservice is running and listening for Kafka messages...")
}
