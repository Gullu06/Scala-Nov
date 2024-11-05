import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.util.Random

def RandomNumberThreadExecutor(): Future[String] = {
      val promise = Promise[String]();
      val random = new Random();
      
      val thread1 = new Thread(new Runnable {
        def run(): Unit = {
        while(foundTheNumber==0){    
            val randomNumber = random.nextInt(2000);
            val result = (s"thread 1 has generated 1567");
            if(randomNumber==1567){
                foundTheNumber=1;
                promise.success(result)
            }          
        }
        }
      });

      val thread2 = new Thread(new Runnable {
        def run(): Unit = {
            while(foundTheNumber == 0){ 
                val randomNumber = random.nextInt(2000);
                val result = (s"thread 2  has generated 1567");
                if(randomNumber==1567){
                    foundTheNumber=1;
                    promise.success(result)
                }
            }
        }
      });

      val thread3 = new Thread(new Runnable {
        def run(): Unit = {
            while(foundTheNumber == 0){ 
                val randomNumber = random.nextInt(2000)
                val result = (s"thread 3 has generated 1567")

                if(randomNumber==1567){
                    foundTheNumber = 1
                    promise.success(result)
                }
            }
        }
      });
    thread1.start();
    thread2.start();
    thread3.start();
    promise.future;
}

var foundTheNumber = 0
@main def abc(): Unit = {
    val futureResult: Future[String] = RandomNumberThreadExecutor();
    futureResult.onComplete {
      case Success(result) => println(result)
      case Failure(exception) => println(s"Async operation failed with exception: $exception")
    }
    println("inside the main....");
    
}
