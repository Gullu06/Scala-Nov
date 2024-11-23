package security

import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import javax.inject.Inject

class Filters @Inject()(jwtAuthFilter: JwtAuthFilter) extends HttpFilters {
  println("Filters class loaded!") // Debug log
  override def filters: Seq[EssentialFilter] = Seq(jwtAuthFilter)
}
