package scheduler

import com.google.inject.AbstractModule
import play.api.Logging

class SchedulerModule extends AbstractModule with Logging {

  override def configure(): Unit = {
    // Log the initialization of the mail scheduler
    logger.info("Initializing Restaurant Mail Service Scheduler...")

    // Bind the scheduler class to be eagerly instantiated
    bind(classOf[RestaurantMailServiceScheduler]).asEagerSingleton()
  }
}
