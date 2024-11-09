import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.util.Random
import scala.concurrent.duration._

val taskCompletePromise: Promise[String] = Promise()
val taskCompleteFuture = taskCompletePromise.future

// Simulate each thread as a separate Future task
def performRandomIntegerGeneration(name: String): Future[Unit] = {
    Future {
        var isTrue : Boolean = true
        while (isTrue) {
            val randomInt = Random.nextInt(200) // Random integer between 0 and 1999

            // Check if the task is already completed by another thread
            if (!taskCompletePromise.isCompleted) {
                println(s"$name generated: $randomInt")

                // If the generated random integer is 15, complete the task
                if (randomInt == 150) {
                    println(s"$name stopped all tasks by generating 15!")
                    taskCompletePromise.success(s"Task completed by $name with random number 15")
                    isTrue = false
                }
            } else {
                println(s"$name saw that the task was already completed. Stopping.")
                isTrue = false
            }
        }
    }
}

@main def promiseAndFutures() = { 
    // Create multiple threads performing the task
    performRandomIntegerGeneration("Thread 1")
    performRandomIntegerGeneration("Thread 2")
    // performRandomIntegerGeneration("Thread 3")

    // Monitor the task completion future
    taskCompleteFuture.onComplete {
        case Success(msg) => println(s"Notification: $msg")
        case Failure(ex) => println(s"Task failed: ${ex.getMessage}")
    }
    
    println("All worker tasks have been completed. Exiting.")
}
