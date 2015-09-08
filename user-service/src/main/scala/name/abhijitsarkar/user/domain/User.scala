package name.abhijitsarkar.user.domain

case class User(val userId: Option[String], val firstName: String, val lastName: String, val phoneNum: String, val email: Option[String]) {
}
