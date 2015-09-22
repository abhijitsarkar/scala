package name.abhijitsarkar.scala.orderfsm;

import akka.actor.FSM.{ CurrentState, SubscribeTransitionCallBack }
import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.testkit.{ ImplicitSender, TestKit }
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.FlatSpecLike
import OrderSystem._
import akka.actor.FSM.Transition

class OrderSystemSpec extends TestKit(ActorSystem("order-system")) with ImplicitSender with FlatSpecLike
    with Matchers with BeforeAndAfterAll {

  override protected def afterAll() {
    super.afterAll()
    system.shutdown()
  }
  
  // note: the SubscribeTransitionCallBack could be called multiple times. it returns the current state first
  // and then transitions, if any have happened.
  
  it should "stay in OrderPending state as long as barista is busy" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    orderSystem ! SubscribeTransitionCallBack(testActor)
    
    orderSystem ! BaristaIsBusy
    
    expectMsg(CurrentState(orderSystem, OrderPending))
  }
  
  it should "transition from OrderPending to OrderPlaced state when barista is available and customer has yet to pay" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    orderSystem ! SubscribeTransitionCallBack(testActor)
    
    expectMsg(CurrentState(orderSystem, OrderPending))
    
    orderSystem ! BaristaIsAvailable(OrderPending, YetToMakePayment)
    
    expectMsg(Transition(orderSystem, OrderPending, OrderPlaced))
  }
  
  it should "transition from OrderPending to OrderReady state when barista is available and payment is accepted" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    orderSystem ! SubscribeTransitionCallBack(testActor)
    
    expectMsg(CurrentState(orderSystem, OrderPending))
    
    orderSystem ! BaristaIsAvailable(OrderPending, PaymentAccepted)
    
    expectMsg(Transition(orderSystem, OrderPending, OrderReady))
  }
}
