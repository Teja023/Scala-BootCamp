object multipleInheritences extends App {
    trait Task {
        def doTask(): Unit = {
            println("Getting started with Task.")
        }
    }
    trait Cook extends Task {
        override def doTask() : Unit = {
            // super.doTask()
            println("Food preparation task is started")
        }
    }
    trait Garnish extends Cook {
        override def doTask() : Unit = {
            // super.doTask()
            println("Garnishing task is started")
        }
    }
    trait Pack extends Garnish {
        override def doTask() : Unit = {
            // super.doTask()
            println("Packing task is started")
        }
    }
    class Activity extends Task() {
        def doActivity() : Unit = {
           doTask()
        }
    }

    val x : Activity = new Activity()
    x.doActivity()
    println("----")
    val y : Task = new Activity with Pack with Garnish  with Cook 
    y.doTask()
}
