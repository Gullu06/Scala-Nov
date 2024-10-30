// Companion class
class Person private(val name: String, val age: Int) {
  override def toString: String = s"Person(Name: $name, Age: $age)"
}

// Companion object
object Person {
  private var personCount: Int = 0

  def apply(name: String, age: Int): Person = {
    personCount += 1
    new Person(name, age)
  }

  def getCount: Int = personCount
}

// Usage example
object Main extends App {
  val person1 = Person("Raj", 30)
  val person2 = Person("Simaran", 25)

  println(person1)
  println(person2)
  
  println(s"Total persons created: ${Person.getCount}") // Output: Total persons created: 2
}
