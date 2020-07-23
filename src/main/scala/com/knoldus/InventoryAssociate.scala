package com.knoldus

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import com.knoldus.InventoryAssociate._
import com.knoldus.InventoryManager._
import com.knoldus.Supplier.{Payment, SendOrder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object InventoryAssociate {

  def props(inventoryManager: ActorRef): Props =
    Props(new InventoryAssociate(inventoryManager))

  case class Items(item: Item)

  case class Item(name: String, quantity: Int, cost: Double, orderedItems: Int)

  case class KeepItemAtPlace(item: Item)

  case class RejectOrderException(item: Item) extends IllegalStateException

  case object ManagerNotFound extends IllegalAccessException

  case object GiveOrder

}

class InventoryAssociate(inventoryManager: ActorRef) extends Actor {
  val log = Logging(context.system, this)
  override def preStart(): Unit = {
    log.info(s"InventoryAssociate preStart with path: ${self.path}")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info(s"InventoryAssociate preRestart - $message - $reason")
  }

  override def postStop(): Unit = {
    log.info("InventoryAssociate Stop")
  }

  def createAssociate(inventoryManager: ActorRef): ActorRef = {
    context.actorOf(InventoryAssociate.props(inventoryManager), "associate")
  }
  override def receive: Receive = {

    case Items(item) =>
      log.info(s"Got ${item.quantity} ${item.name}")
      if (item.quantity == item.orderedItems) {
        log.info("correct item received")
        sender() ! Payment(item.cost)
        self ! KeepItemAtPlace(item)
      }
      else throw RejectOrderException(item)
    case KeepItemAtPlace(item) => inventoryManager ! ReceivedItem(item)
    case GiveOrder =>
      implicit val timeout: Timeout = Timeout(20 seconds)
      val response = inventoryManager ? AnyOrder
      response.mapTo[Order].map {
        case Yes(name, quantity) => sender() ! SendOrder(name, quantity)
        case No => throw ManagerNotFound
      }

  }
}
