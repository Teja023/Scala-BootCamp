import scala.io.Source
import scala.collection.mutable.ListBuffer

object csv_task extends App{
    val filePath = "sample_data.csv"
    val employees_data = Source.fromFile(filePath).getLines() // 'employees_data' Iterator

    val header_string = employees_data.next()
    println("Headers : " + header_string) // sno,name,city,salary,department
    println()
    val headers = header_string.split(",")
    
    case class Employee(
        val sno : Int,
        val name : String,
        val city : String,
        val salary : Int,
        val department : String
    )

    val employeesBuffer: ListBuffer[Employee] = ListBuffer()

    while (employees_data.hasNext) {
        val Array(sno, name, city, salary, department) = employees_data.next().split(",")
        employeesBuffer += Employee(sno.toInt, name, city, salary.toInt, department)
    }

    // Convert ListBuffer to List
    val employees_list: List[Employee] = employeesBuffer.toList

    // Filter functions
    def filterDepartment(department: String) : List[Employee] = {
        val filteredList : List[Employee] = employees_list.filter(employee => employee.department == department)
        filteredList
    }
    def filterSalary(salary: Int) : List[Employee] = {
        val filteredList : List[Employee] = employees_list.filter(employee => employee.salary >= salary)
        filteredList
    }

    println(filterDepartment("HR"))
    println()

    println(filterSalary(60000))
    println()

    //Formatted output
    val formatted_list = employees_list.map(employee => employee.name + " from " + employee.city + " works in " + employee.department + " with salary of " + employee.salary)

    formatted_list.foreach(println)

    // Grouping employees by department => returns a 'map'
    val employeesByDepartment = employees_list.groupBy(employee => employee.department)

    // Calculating total salary, average salary, and number of employees per department
    val departmentStats = employeesByDepartment.map{ 
        case (department, employees) =>
        val totalSalary = employees.map(employee => employee.salary).sum
        val averageSalary = totalSalary / employees.size
        val numberOfEmployees = employees.size

        (department, totalSalary, averageSalary, numberOfEmployees)
    }

    println()

    // Print the results
    departmentStats.foreach { case (department, totalSalary, averageSalary, numberOfEmployees) =>
        println(s"Department: $department, Total Salary: $totalSalary, Average Salary: $averageSalary, Number of Employees: $numberOfEmployees")
        println()
    }
}