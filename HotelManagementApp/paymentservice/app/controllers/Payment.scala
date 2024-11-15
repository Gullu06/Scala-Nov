package models

import models.PaymentStatus.PaymentStatus

//Creating interface for payment
trait Payment {
  def doPayment(): PaymentStatus
}
