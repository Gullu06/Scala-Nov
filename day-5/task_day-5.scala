import scala.io.Source
import scala.util.{Try, Success, Failure}

case class Employee(sno: Int, name: String, city: String, salary: Int, department: String)

object ReadCSV {
  def main(args: Array[String]): Unit = {
    val csvToList = Try {
        val filename = "Day5Task.csv"
        val delimiter = ","
        val file = Source.fromFile(filename)

        val data = for {
            line <- file.getLines().drop(1)
            fields = line.split(delimiter).map(_.trim)
            if fields.length == 5
                sno = fields(0).toInt
                name = fields(1)
                city = fields(2)
                salary = fields(3).toInt
                department = fields(4)
            } yield Employee(sno, name, city, salary, department)

            val result = data.toList
            file.close()
            result
    }
    csvToList match {
        // Perform filter operations based on salary and departments
        case Success(result) =>
            val empList = result.filter(employee => employee.salary > 60000 && employee.department == "Engineering")
            empList.foreach(println)

            // perform map operations to produce formatted report
            val formattedEmployees = result.map { emp =>
            s"Employee ${emp.sno}: ${emp.name}, from ${emp.city}, earns ${emp.salary}, works in ${emp.department}."}
            formattedEmployees.foreach(println)

            // perform reduce operation to find total Salary, average Salary, number of employees department wise
            val totalSalary = result.map(_.salary).sum
            println(s"Total salary: $totalSalary")

            val averageSalary = if(result.nonEmpty) totalSalary/result.size else 0
            println(s"Average salary: $averageSalary")

            val employeesByDepartment = result.groupBy(_.department)
            val employeeCountByDepartment = employeesByDepartment.mapValues(_.size)
            println("Number of employees department wise")
            employeeCountByDepartment.foreach{
                case(department, number_of_employees) => println(s"$department: $number_of_employees")
            }

        case Failure(exception) =>
            exception match {
          case _: java.io.FileNotFoundException =>
            println(s"Error: The file was not found. Please check the file path.")
          case _: NumberFormatException =>
            println(s"Error: There was an issue with number formatting in the file. ${exception.getMessage}")
          case e: Exception =>
            println(s"An unexpected error occurred: ${e.getMessage}")
        }
    }
  }
}

