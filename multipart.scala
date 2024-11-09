object multipart extends App {
    def multipart(name: String)(logic : => Unit) : Unit = {
        println(s"this the name : $name")
        logic
    }

    multipart("Abhi") {
        println("Hey hi")
    }

    case class Transaction(
        id: String,
        amount: Double,
        totalAmount: Double
    )

    object Transaction {
        def apply(id: String, amount: Double)(feeCalculation: Double => Double) = {
            val fee = feeCalculation(amount)
            val totalAmount = amount + fee
            println(s"Creating transaction with ID: '$id', amount: $$${amount}, fee: $$${fee}, total amount: $$${totalAmount}")
            new Transaction(id, amount, totalAmount)
        }
    }

    val transaction = Transaction("TX12345", 100.0) { amount =>
        if (amount > 50) amount * 0.05 
        else 2.0
    }

    println(s"Transaction Details: ID = ${transaction.id}, Amount = ${transaction.amount}, Total Amount = ${transaction.totalAmount}")
}
