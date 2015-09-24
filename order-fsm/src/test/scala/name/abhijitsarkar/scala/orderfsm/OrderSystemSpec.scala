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

  // If the state data changes, using TestFSMRef, you could check state data blocking for some time
  //  awaitCond(
  //    p = orderSystem.stateData == ???, 
  //    max = 2.seconds, 
  //    interval = 200.millis,
  //    message = "waiting for expected state data..."
  //  )

  // awaitCond will throw an exception if the condition is not met within max timeout

  // note: the SubscribeTransitionCallBack could be called multiple times. it returns the current state first
  // and then transitions, if any have happened.
  // SubscribeTransitionCallBack only delivers the CurrentState once, and then on only Transition callbacks.

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

    orderSystem ! BaristaIsAvailable(OrderPending, PaymentPending)

    expectMsg(Transition(orderSystem, OrderPending, OrderPlaced))
  }

  it should "transition from OrderPending to OrderReady state when barista is available and payment is accepted" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    orderSystem ! SubscribeTransitionCallBack(testActor)

    expectMsg(CurrentState(orderSystem, OrderPending))

    orderSystem ! BaristaIsAvailable(OrderPending, PaymentAccepted)

    expectMsg(Transition(orderSystem, OrderPending, OrderReady))
  }

  it should "stay in OrderPlaced state as long as customer has not paid" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    orderSystem ! BaristaIsAvailable(OrderPending, PaymentPending)

    orderSystem ! SubscribeTransitionCallBack(testActor)

    expectMsg(CurrentState(orderSystem, OrderPlaced))

    orderSystem ! BaristaIsAvailable(OrderPlaced, PaymentPending)

    expectNoMsg
  }
  
  it should "transition from OrderPlaced to OrderClosed state when payment is declined" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    
    orderSystem ! BaristaIsAvailable(OrderPending, PaymentPending)
    
    orderSystem ! SubscribeTransitionCallBack(testActor)
    orderSystem ! BaristaIsAvailable(OrderPlaced, PaymentDeclined)
    
    expectMsg(CurrentState(orderSystem, OrderPlaced))
    expectMsg(Transition(orderSystem, OrderPlaced, OrderClosed))
  }
  
  it should "transition from OrderPlaced to OrderReady state when barista is available and payment is accepted" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    
    orderSystem ! BaristaIsAvailable(OrderPending, PaymentPending)
    
    orderSystem ! SubscribeTransitionCallBack(testActor)
    orderSystem ! BaristaIsAvailable(OrderPlaced, PaymentAccepted)
    
    expectMsg(CurrentState(orderSystem, OrderPlaced))
    expectMsg(Transition(orderSystem, OrderPlaced, OrderReady))
  }
  
  it should "transition from OrderPlaced to OrderPending state when barista is busy and payment is accepted" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    
    orderSystem ! BaristaIsAvailable(OrderPending, PaymentPending)
    
    orderSystem ! SubscribeTransitionCallBack(testActor)
    orderSystem ! BaristaIsBusy(OrderPlaced, PaymentAccepted)
    
    expectMsg(CurrentState(orderSystem, OrderPlaced))
    expectMsg(Transition(orderSystem, OrderPlaced, OrderPending))
  }
  
  it should "transition from OrderReady to OrderClosed state when customer is happy" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    
    orderSystem ! BaristaIsAvailable(OrderPending, PaymentPending)
    orderSystem ! BaristaIsAvailable(OrderPlaced, PaymentAccepted)
    
    orderSystem ! SubscribeTransitionCallBack(testActor)
    
    orderSystem ! HappyWithOrder
    
    expectMsg(CurrentState(orderSystem, OrderReady))
    expectMsg(Transition(orderSystem, OrderReady, OrderClosed))
  }
  
  it should "transition from OrderReady to OrderClosed state when customer is not happy" in {
    val orderSystem = system.actorOf(Props[OrderSystem])
    
    orderSystem ! BaristaIsAvailable(OrderPending, PaymentPending)
    orderSystem ! BaristaIsAvailable(OrderPlaced, PaymentAccepted)
    
    orderSystem ! SubscribeTransitionCallBack(testActor)
    
    orderSystem ! NotHappyWithOrder
    
    expectMsg(CurrentState(orderSystem, OrderReady))
    expectMsg(Transition(orderSystem, OrderReady, OrderPending))
  }
}
