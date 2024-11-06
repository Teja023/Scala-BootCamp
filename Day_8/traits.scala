object multipleInheritence extends App {
    trait GetStarted {
        def prepare(): Unit
    }
    trait KeepIngredients extends GetStarted {
        def prepare() : Unit = {
            println("Getting started with preparation.")
        }
    }
    
    trait Cook extends GetStarted {
        // abstract override is used beacuse the method in super class is abstract
        abstract override def prepare() : Unit = {
            super.prepare()
            println("Food is preparing by the cook")
        }
    }
    trait Seasoning {
        def appplySeasoning() : Unit = {
            println("Adding seasoning to the food")
        }
    }

    class Food extends KeepIngredients with Cook with Seasoning() {
        def prepareFood() : Unit = {    
            prepare()
            appplySeasoning()
        }
    }

    val x : Food = new Food()
    x.prepareFood()
}
