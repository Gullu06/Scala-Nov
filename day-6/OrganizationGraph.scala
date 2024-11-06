import scala.io.StdIn.readLine
import scala.util.matching.Regex

// Case class representing a team member
case class TeamMember(id: Int, name: String, city: String) {
  override def toString: String = s"Member(id: $id, name: $name, city: $city)"
}

// Case class representing a department in the organization
case class Department(name: String, var superior: Department, var subDepartments: List[Department], var staff: List[TeamMember]) {
  override def toString: String = s"Dept(name: $name, superior: ${superior.name}, subDepts: ${subDepartments}, staff: $staff)"
}

// Define the headquarters of the organization
object OrgSystem {
  var globalHQ: Department = Department("Headquarters", null, List.empty[Department], List.empty[TeamMember])

  // Function to display the structure of the organization with proper indentation
  private def showOrgStructure(dept: Department, indent: String = "", isLastDept: Boolean = true, isRootDept: Boolean = true): Unit = {
    if (isRootDept) {
      println(dept.name)
    } else {
      val lineConnector = if (isLastDept) "└── " else "├── "
      println(s"$indent$lineConnector${dept.name}")
    }

    val childIndent = if (isLastDept) indent + "    " else indent + "│   "

    // Display team members in the current department
    if (dept.staff != null && dept.staff.nonEmpty) {
      dept.staff.zipWithIndex.foreach { case (worker, idx) =>
        val lineStart = if (idx == dept.staff.length - 1 && dept.subDepartments.isEmpty) "└── " else "├── "
        println(s"$childIndent$lineStart(${worker.id}, ${worker.name}, ${worker.city})")
      }
    }

    // Recursively display sub-departments
    if (dept.subDepartments != null && dept.subDepartments.nonEmpty) {
      dept.subDepartments.zipWithIndex.foreach { case (subDept, idx) =>
        val lastSubDept = idx == dept.subDepartments.length - 1
        showOrgStructure(subDept, childIndent, lastSubDept, isRootDept = false)
      }
    }
  }

  // Function to search for a department by name, returns the department wrapped in a List
  private def findDepartment(dept: Department, deptName: String): List[Department] = {
    if (dept.name == deptName) {
      List(dept)  // Return department in a List to work with flatMap
    } else {
      dept.subDepartments.flatMap(findDepartment(_, deptName))  // Search in sub-departments
    }
  }

  // Function to show user input prompts
  private def showPrompts(): Unit = {
    println("Press <d> to display the organization's structure")
    println("Press <q> to quit the application")
    println("To add a team member, use the format:")
    println("<parent_dept>,<current_dept>,(<id>,<name>,<city>)")
  }

  def main(args: Array[String]): Unit = {
    println("Welcome to the Organization System")
    showPrompts()
    val quitCommand = "q"
    var userInput = readLine()

    // Main loop for processing user input
    while (userInput != quitCommand) {
      if (userInput == "d") {
        showOrgStructure(globalHQ)  // Display the organization structure
      } else {
        val inputPattern: Regex = """([^,]+),([^,]+),\((\d+),([^,]+),([^,]+)\)""".r

        userInput match {
          case inputPattern(parentDept, currentDept, id, name, city) =>
            // Create a new team member
            val newMember = TeamMember(id = id.toInt, name = name, city = city)

            // Try to find the parent department
            var parentDeptNode = findDepartment(dept = globalHQ, deptName = parentDept).headOption.getOrElse(null)
            if (parentDeptNode != null) {
              // If the current department exists under the parent, add the member
              var currentDeptNode = findDepartment(dept = parentDeptNode, deptName = currentDept).headOption.getOrElse(null)
              if (currentDeptNode != null) {
                currentDeptNode.staff = currentDeptNode.staff :+ newMember // Add to existing department
              } else {
                // If the department doesn't exist, create a new one
                val newSubDept = Department(currentDept, parentDeptNode, List.empty[Department], List(newMember))
                parentDeptNode.subDepartments = parentDeptNode.subDepartments :+ newSubDept
              }
            } else {
              // If the parent department doesn't exist, create it
              parentDeptNode = Department(name = parentDept, superior = globalHQ, subDepartments = List.empty[Department], staff = List.empty[TeamMember])
              val newSubDept = Department(currentDept, parentDeptNode, List.empty[Department], List(newMember))
              parentDeptNode.subDepartments = parentDeptNode.subDepartments :+ newSubDept
              globalHQ.subDepartments = globalHQ.subDepartments :+ parentDeptNode
            }

          case _ =>
            println("The input format is incorrect. Please follow the prescribed format.")
        }
      }

      showPrompts()
      userInput = readLine()
    }

    println("Thank you for using the Organization System. Goodbye!")
  }
}

// Hr,Salary,(1,Raj,Bhopal)
