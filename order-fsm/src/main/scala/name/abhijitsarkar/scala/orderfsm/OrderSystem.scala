package name.abhijitsarkar.scala.orderfsm

import akka.actor.LoggingFSM
import akka.actor.ActorLogging
import akka.actor.Actor

object OrderSystem {
  sealed trait State
  case object OrderPending extends State
  case object OrderPlaced extends State
  case object OrderReady extends State
  case object OrderClosed extends State

  sealed trait Activity

  sealed trait PaymentActivity extends Activity
  case object YetToMakePayment extends PaymentActivity
  case object PaymentAccepted extends PaymentActivity
  case object PaymentDeclined extends PaymentActivity

  sealed trait BaristaActivity extends Activity { val fromState: State; val paymentActivity: PaymentActivity }
  case class BaristaIsAvailable(fromState: State, paymentActivity: PaymentActivity) extends BaristaActivity
  case class BaristaIsBusy(fromState: Nothing, paymentActivity: Nothing) extends BaristaActivity

  sealed trait CustomerActivity extends Activity
  case object HappyWithOrder extends CustomerActivity
  case object NotHappyWithOrder extends CustomerActivity

  // data is passed internally among FSM states, not usually sent from outside. Data necessary for matching is part of 
  // the case objects/classes.

  // in case some data is sent from outside
  // send as, fsm ! (event, newData)
  // and match as, case Event((event, newData), data) =>
  case class Data(fromState: State, paymentActivity: PaymentActivity)
}

// good example: https://github.com/tombray/akka-fsm-examples
import OrderSystem._
class OrderSystem extends Actor with ActorLogging with LoggingFSM[State, Data] {
  startWith(OrderPending, Data(OrderPending, YetToMakePayment))

  when(OrderPending) {
    case Event(BaristaIsBusy, _) => stay
    case Event(BaristaIsAvailable(_, YetToMakePayment), _) => goto(OrderPlaced) using Data(stateName, YetToMakePayment)
    case Event(BaristaIsAvailable(_, PaymentAccepted), _) => goto(OrderReady) using Data(stateName, PaymentAccepted)
  }

  val waitingToBeServed = Data(OrderPlaced, PaymentAccepted)

  when(OrderPlaced) {
    case Event(b: BaristaIsAvailable, `waitingToBeServed`) => goto(OrderReady) using `waitingToBeServed`
    case Event(b: BaristaIsBusy, `waitingToBeServed`) => goto(OrderPending) using `waitingToBeServed`
    case Event(_, Data(_, PaymentDeclined)) => goto(OrderClosed) using Data(stateName, PaymentDeclined)
    case Event(_, Data(_, YetToMakePayment)) => stay using Data(stateName, YetToMakePayment)
  }

  when(OrderReady) {
    case Event(HappyWithOrder, _) => goto(OrderClosed)
    case Event(NotHappyWithOrder, _) => goto(OrderPending) using Data(stateName, PaymentAccepted)
  }

  when(OrderClosed) {
    case Event(_, _) => stay
  }

  whenUnhandled {
    case Event(e, s) => {
      // state name is available as 'stateName'
      log.warning("Received unhandled request {} in state {}/{}", e, stateName, s)
      stay
    }
  }

  // previous state data is available as 'stateData' and next state data as 'nextStateData'
  // not necessary as LoggingFSM (if configured) will take care of logging
  onTransition {
    case _ -> nextState => log.info("Entering state: {} with payment activity: {} from state: {} with payment activity: {}.",
      nextState, stateData.paymentActivity, nextStateData.fromState, nextStateData.paymentActivity)
  }

  initialize()
}