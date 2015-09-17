package name.abhijitsarkar.user.controller

import akka.actor.Props
import name.abhijitsarkar.user.service.UserBusinessDelegate
import akka.event.NoLogging
import name.abhijitsarkar.user.repository.MockUserRepository
import name.abhijitsarkar.user.ActorPlumbing
import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.Matchers
import org.scalatest.FlatSpec

trait UserResourceSpec extends FlatSpec with Matchers with ScalatestRouteTest with ActorPlumbing {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  val userRepository = new MockUserRepository
  val businessDelegateProps: Props = UserBusinessDelegate.props(userRepository, executor)
}