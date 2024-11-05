case class JobRunner(name: String, time: Int)

object JobRunner {
    // def apply(name: String, age: Int): Candidate = new Candidate(name, age)
    def apply(name: String, time: Int)(codeBlock : => Unit): JobRunner = {
        while(true){
        println(s"Waiting for $time")
        Thread.sleep(time*1000)
        codeBlock
        }
        new JobRunner(name, time)
    }
}

@main def main(): Unit = {
// Using apply to create a Person instance
var time = 10
val runJob = JobRunner("Engineer", time) {
        println(s"Run code after every $time secs.")
    }
}
