import java.sql.{Connection, DriverManager, ResultSet, Statement}
object candidates extends App{
    Class.forName("com.mysql.cj.jdbc.Driver")

    //Establishing connection
    val url = "jdbc:mysql://scaladb.mysql.database.azure.com:3306/teja_db"
    val username = "mysqladmin"
    val password = "Password@12345"

    val connection : Connection = DriverManager.getConnection(url, username, password)

    def deleteTable(): Unit = {
        try {
            // Create a statement
            val statement: Statement = connection.createStatement()

            // Create a table
            val deleteTableSQL =
                """
                DROP TABLE candidates 
                """

            statement.execute(deleteTableSQL)
            println("Table deleted successfully.")
            statement.close()
        } catch {
            case e: Exception => e.printStackTrace()
        }
    }
    deleteTable()
    connection.close()

}