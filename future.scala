import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def dataRetrieval() : Future[String] = {
    Future{
        Thread.sleep(200)
        println("Data Retrieved")
        "This the DATA"
    }
}

@main def futureProgram() = {
    dataRetrieval().onComplete {
        case Success(result) => println(s"data retrieved result: $result")
        case Failure(exception) => println(s"data retrieval failed: ${exception.getMessage}")
    }
    println("This line is executed")
    // Thread.sleep(3000)
    println("with This line the program is terminated")
}