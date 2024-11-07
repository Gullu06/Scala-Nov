import java.sql.{Connection, DriverManager, ResultSet, Statement, PreparedStatement}

import scala.language.implicitConversions
import scala.io.Source
import scala.collection.mutable.ListBuffer

case class Candidate(sno: Int, name: String, city: String)

object DatabaseExample {
    def main(args: Array[String]): Unit = {
        // Load the JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver")

        // Establish the connection
        val url = "jdbc:mysql://scaladb.mysql.database.azure.com:3306/Priyanshi"
        val username = "mysqladmin"
        val password = "XXXXXXXXX"

        // Implicit conversion from Tuple to Candidate
        implicit def tupleToCandidate(candidate: (Int, String, String)): Candidate = {
            new Candidate(candidate._1, candidate._2, candidate._3)
        }

        // Sample candidate data
        val candidateData: Array[Candidate] = Array((1, "Alice", "New York"),(2, "Bob", "Los Angeles"),
(3, "Charlie", "Chicago"),(4, "Diana", "Houston"),(5, "Eve", "Phoenix"),(6, "Frank", "Philadelphia"),
(7, "Grace", "San Antonio"),(8, "Hank", "San Diego"),(9, "Ivy", "Dallas"),(10, "Jack", "San Jose"),
(11, "Kathy", "Austin"),(12, "Leo", "Jacksonville"),(13, "Mona", "Fort Worth"),(14, "Nina", "Columbus"),
(15, "Oscar", "Charlotte"),(16, "Paul", "San Francisco"),(17, "Quinn", "Indianapolis"),(18, "Rita", "Seattle"),
(19, "Steve", "Denver"),(20, "Tina", "Washington"),(21, "Uma", "Boston"),(22, "Vince", "El Paso"),
(23, "Wendy", "Detroit"),(24, "Xander", "Nashville"),(25, "Yara", "Portland"),(26, "Zane", "Oklahoma City"),
(27, "Aiden", "Las Vegas"),(28, "Bella", "Louisville"),(29, "Caleb", "Baltimore"),(30, "Daisy", "Milwaukee"),
(31, "Ethan", "Albuquerque"),(32, "Fiona", "Tucson"),(33, "George", "Fresno"),(34, "Hazel", "Mesa"),
(35, "Ian", "Sacramento"),(36, "Jill", "Atlanta"),(37, "Kyle", "Kansas City"),(38, "Luna", "Colorado Springs"),
(39, "Mason", "Miami"),(40, "Nora", "Raleigh"),(41, "Owen", "Omaha"),(42, "Piper", "Long Beach"),
(43, "Quincy", "Virginia Beach"),(44, "Ruby", "Oakland"),(45, "Sam", "Minneapolis"),(46, "Tara", "Tulsa"),
(47, "Ursula", "Arlington"),(48, "Victor", "New Orleans"),(49, "Wade", "Wichita"),(50, "Xena", "Cleveland")
)

        // Creating the connection
        val connection: Connection = DriverManager.getConnection(url, username, password)

        try {
            val statement: Statement = connection.createStatement()

            // Create table SQL
            val createTableSQL =
                """
                    CREATE TABLE IF NOT EXISTS candidates (
                    sno INT PRIMARY KEY,
                    name VARCHAR(100),
                    city VARCHAR(100)
                    )
                """

            statement.execute(createTableSQL)
            println("Table created successfully.")

            // Preparing the insert statement
            val insertSQL = "INSERT INTO candidates (sno, name, city) VALUES (?, ?, ?)"
            val preparedStatement: PreparedStatement = connection.prepareStatement(insertSQL)

            // Insert candidate data into the table
            candidateData.foreach { candidate =>
                preparedStatement.setInt(1, candidate.sno)
                preparedStatement.setString(2, candidate.name)
                preparedStatement.setString(3, candidate.city)
                preparedStatement.executeUpdate()
            }
            println("Data inserted successfully.")

            // Query the data
            val query = "SELECT * FROM candidates"
            val resultSet: ResultSet = statement.executeQuery(query)

            // Process the ResultSet
            println("Candidates:")
            while (resultSet.next()) {
                val sno = resultSet.getInt("sno")
                val name = resultSet.getString("name")
                val city = resultSet.getString("city")
                println(s"Sno: $sno, Name: $name, City: $city")
            }

        } catch {
            case e: Exception => e.printStackTrace()
        } finally {
            // Close Statement and Connection
            connection.close()
        }
    }
}
