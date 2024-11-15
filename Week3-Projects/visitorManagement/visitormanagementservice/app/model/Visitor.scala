package models

import java.sql.Timestamp
import play.api.libs.json._

case class Visitor(
                    visitorId: Option[Long] = None,
                    name: String,
                    contactNo: String,
                    address: String,
                    hostName: String,
                    hostEmail: String,
                    visitorEmail: String, // New field for visitor's email
                    purpose: String,
                    idProof: Array[Byte],
                    block: String,
                    status: String = "CHECKED_IN",
                    checkinTime: Timestamp,
                    checkoutTime: Option[Timestamp] = None
                  )

object Visitor {
  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    def writes(ts: Timestamp): JsValue = JsNumber(ts.getTime)
    def reads(json: JsValue): JsResult[Timestamp] = json.validate[Long].map(new Timestamp(_))
  }

  implicit val visitorFormat: OFormat[Visitor] = Json.format[Visitor]
}


