package com.knoldus

import akka.actor.{Actor, ActorRef, Props, UnhandledMessage}
import akka.event.Logging
import com.knoldus.InventoryAssociate.{GiveOrder, Item, Items}
import com.knoldus.Supplier._

object Supplier {

  def props(inventoryAssociate: ActorRef): Props =
    Props(new Supplier(inventoryAssociate))

  case class Delivery(item: Item)

  case class Payment(cost: Double)

  case class SendOrder(name: String, quantity: Int)

  case class OrderReceived(name: String, quantity: Int)

  case object NoEntry extends Exception

  case object TakeOrder

  case object EnterBuilding

}

class Supplier(inventoryAssociate: ActorRef) extends Actor {
  val log = Logging(context.system, this)
  override def preStart(): Unit = {
    val actorName = self.path
    log.info(s"Supplier preStart with path: $actorName")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info(s"Supplier preRestart - $message - $reason")
  }

  override def postStop(): Unit = {
    log.info("Supplier Stop")
  }

  override def receive: Receive = {
    case Delivery(item) => inventoryAssociate ! Items(item)
    case Payment(cost) => log.info(s"Received $cost")
    case TakeOrder => inventoryAssociate ! GiveOrder
    case SendOrder(name, quantity) => self ! OrderReceived(name, quantity)
    case UnhandledMessage => log.info("unhandled")
    case OrderReceived(name, quantity) =>log.info(s"Received an order of $quantity $name")
    case EnterBuilding => throw NoEntry
  }
}
