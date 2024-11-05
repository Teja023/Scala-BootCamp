import scala.language.implicitConversions
import scala.collection.mutable.{ListBuffer, Set}
import scala.util.boundary, boundary.break
import scala.io.StdIn

object tree extends App {
    case class Employee(
        sno : Int,
        name : String,
        location : String
    )
    case class Node(
        currentDept : String,
        employeeList : ListBuffer[Employee] = ListBuffer(),
        childNodes : ListBuffer[Node] = ListBuffer()
    )

    implicit def tupleToEmployee(tuple: (Int, String, String)) : Employee = Employee(tuple._1, tuple._2, tuple._3)

    // Intializing root Node ("Organization")
    val rootNode = Node("Organization")

    // DFS to check for node in root
    def dfs(node: Node, target: String): Option[Node] = boundary {
        if (node.currentDept == target) {
            break(Some(node))
        }
        for (child <- node.childNodes) {
            dfs(child, target) match {
                case Some(foundNode) => break(Some(foundNode))
                case None =>
            }
        }
        None
    }

    def addNode(parent_dept: String, current_dept: String, employees: ListBuffer[Employee], root: Node): Unit = {
        dfs(root, parent_dept) match {
            case Some(parentNode) =>
                val existingDept = parentNode.childNodes.find(_.currentDept == current_dept)
                existingDept match {
                    case Some(node) =>
                        node.employeeList ++= employees
                        println()
                    case None =>
                        // Check if the child exists anywhere in the tree
                        if (dfs(root, current_dept) != None) {
                            // Child exists but is not an immediate child of parent
                            println()
                            println(s"Department '$current_dept' exists but is not a child of '$parent_dept'. Aborting.")
                        } else {
                            val newDept = Node(current_dept, employees)
                            parentNode.childNodes += newDept
                            println()
                            println(s"Created new child department '$current_dept' under '$parent_dept'.")
                        }
                }
            case None =>
                // Parent department not found
                println(s"Parent department '$parent_dept' not found!")
        }
    }

    // print the tree 
    // def printOrganization(node: Node, depth: Int = 0): Unit = {
    //     println("  " * depth + s"${node.currentDept}: ${node.employeeList.map(_.name).mkString(", ")}")
    //     node.childNodes.foreach(child => printOrganization(child, depth + 1))
    // }
    // Recursive function to print the organization tree
    def printOrganization(node: Node, depth: Int = 0): Unit = {
        if (depth == 0) {
            println(node.currentDept)  // Root node (e.g., "Organization")
        } else {
            println("    " * (depth - 1) + "└── " + node.currentDept)
        }
        
        // Print employees in the current department with further indentation
        node.employeeList.foreach { emp =>
            println("    " * depth + "├── (" + emp.sno + "," + emp.name + "," + emp.location + ")")
        }
        
        // Recursively print child nodes
        node.childNodes.foreach { child =>
            printOrganization(child, depth + 1)
        }
    }


    // Function to read input and add node
    def readInput(): Unit = {
        println("Enter the parent department:")
        val parentDept = StdIn.readLine().trim

        println("Enter the current department:")
        val currentDept = StdIn.readLine().trim

        println("Enter the list of employees in the format (sno, name, location) separated by commas (e.g., (1, \"Abhi\", \"Chennai\"), (2, \"Ajay\", \"California\")):")
        val employeesInput = StdIn.readLine().trim

        val employeePattern = """\(\s*(\d+)\s*,\s*"([^"]+)"\s*,\s*"([^"]+)"\s*\)""".r
        val employees : ListBuffer[Employee] = ListBuffer()

        // Split input by '), (' to separate individual employee entries
        val employeeEntries = employeesInput.split("\\),\\s*\\(").map(_.trim)

        // Ensure we clean up the first and last entries for parentheses
        employeeEntries.foreach { emp =>
            val cleanEntry = emp.stripPrefix("(").stripSuffix(")")
            var employeeString = s"($cleanEntry)"
            employeeString match {
                case employeePattern(snoStr, name, location) =>
                    employees += ((snoStr.toInt, name, location): (Int, String, String))
                case _ =>
                    println(s"Invalid employee format: $emp. Skipping this entry.")
            }
        }
        // Add the node to the tree
        addNode(parentDept, currentDept, employees, rootNode)
    }

    // Continuously read input until  exit
    while (true) {
        println("Current Organization Structure:")
        printOrganization(rootNode)
        println()
        println("Press Enter to add another department or type 'exit' to quit:")
        if (StdIn.readLine() == "exit") {
            println("Exiting...")
            System.exit(0)
        }
        readInput()
    }
}
