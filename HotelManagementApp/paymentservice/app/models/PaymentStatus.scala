package models

object PaymentStatus extends Enumeration {
  type PaymentStatus = Value
  val PENDING, SUCCESSFUL, REFUNDED, FAILED, INPROGRESS = Value
}




package models

import models.PaymentStatus._

class UPIPayment extends Payment {
  override def doPayment(): PaymentStatus = {
    // UPI payment logic here
    SUCCESSFUL
  }
}

class CreditCardPayment extends Payment {
  override def doPayment(): PaymentStatus = {
    // Credit Card payment logic here
    SUCCESSFUL
  }
}



