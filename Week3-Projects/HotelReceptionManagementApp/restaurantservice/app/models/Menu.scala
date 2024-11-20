package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class Menu(id: Int, foodItem: String, foodType: String, price: Double)

class MenuDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class MenuTable(tag: Tag) extends Table[Menu](tag, "menu") {
    def id = column[Int]("id", O.PrimaryKey)

    def food_item = column[String]("food_item")

    def food_type = column[String]("food_type")

    def price = column[Double]("price")

    def * = (id, food_item, food_type, price) <> ((Menu.apply _).tupled, Menu.unapply)
  }

  val menu = TableQuery[MenuTable]

  def list(): Future[Seq[Menu]] = db.run {
    menu.result
  }

  def insertMenuItem(insertList: Seq[Menu]): Future[Option[Int]] = db.run {
    menu.delete.andThen(menu ++= insertList)

  }
}