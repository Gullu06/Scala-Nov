package scheduler

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import javax.inject.{Inject, Singleton}

@Singleton
class RestaurantMailServiceScheduler @Inject()(guestService: GuestService) {

  private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  // Schedule the cron job to run daily at midnight
  scheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = {
      try {
        guestService.fetchGuestListAndSendMenu()
      } catch {
        case ex: Exception =>
          // Log the error or handle it as needed
          println(s"Error occurred while executing scheduled task: ${ex.getMessage}")
      }
    }
  }, 0, 1, TimeUnit.DAYS)

  // Gracefully shut down the scheduler when no longer needed
  sys.addShutdownHook {
    scheduler.shutdown()
    if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
      scheduler.shutdownNow()
    }
  }
}