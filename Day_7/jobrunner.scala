object jobruuner extends App{
    case class JobRunner(
        name : String,
        job: String
    )

    object JobRunner{
        def apply(name: String, delay: Int)(jobDescription : String => String) = {
            // Sleep for  milliseconds
            var i : Int = delay
            while (i>0) {
                println(i)
                i-=1
            }
            Thread.sleep(delay * 1000) 
            val job = jobDescription()
            new JobRunner(name, job)
        }
    }

    // Create a JobRunner instance
    val dayShift: JobRunner = JobRunner("Day Shift", 4) { name =>
        println(s"Job role for $name is Manager")
        s"Job role for $name is Manager"
    }
}