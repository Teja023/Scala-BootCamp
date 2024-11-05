import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.util.Random
import scala.concurrent.duration._
import java.util.concurrent.TimeoutException


object promiseAndFutures {
    def main(args: Array[String]) : Unit = {
        // Function simulating an asynchronous operation
        def performAsyncOperation(): Future[String] = {
            val promise : Promise[String]= Promise()

            // Simulating an asynchronous computation
            def runThread(name: String) = new Thread(new Runnable {
                def run(): Unit = {
                    var isTrue : Boolean = true
                    while(isTrue) {
                        val randomInt = Random.nextInt(2000) // Random integer between 0 and 1999
                        println(s"$name created this $randomInt")
                        // Check if the task is already completed by another thread
                        if (!promise.isCompleted) {
                            // If the generated random integer is 15, complete the task
                            if (randomInt == 1510) {
                                println(s"$name stopped all tasks by generating 15!")
                                promise.success(s"Task completed by $name with random number 15")
                                isTrue = false
                            }
                        } else {
                            println(s"$name saw that the task was already completed. Stopping.")
                            isTrue = false
                        }
                    }
                }
            })

            val thread_1 = runThread("thread_1")
            thread_1.start()
            val thread_2 = runThread("thread_2")
            thread_2.start()
            val thread_3 = runThread("thread_3")
            thread_3.start()
            promise.future 
        }

        // // Simulate each thread as a separate Future task
      

        // Monitor the task completion future
        performAsyncOperation().onComplete {
            case Success(msg) => println(s"Notification: $msg")
            case Failure(ex) => println(s"Task failed: ${ex.getMessage}")
        }

        Thread.sleep(10000)        
        println("Task execution completed")
    }
}