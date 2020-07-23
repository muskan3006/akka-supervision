package com.knoldus

import akka.actor.{Actor, Props}
import akka.event.Logging
import com.knoldus.InventoryAssociate.Item
import com.knoldus.InventoryManager.{AnyOrder, No, ReceivedItem, Yes}

import scala.util.Random


object InventoryManager {

  def props: Props =
    Props(new InventoryManager)
  case class ReceivedItem(item: Item)
  trait Order

  case class Yes(name: String, quantity: Int) extends Order

  case object AnyOrder

  case object No extends Order
}

class InventoryManager extends Actor {
  val log = Logging(context.system, this)
  val random: Int = 20 + (new Random).nextInt((30 - 20) + 1)
  override def preStart(): Unit = {
    log.info(s"InventoryManager preStart with path: ${self.path}")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info(s"InventoryManager preRestart - $message - $reason")
  }

  override def postStop(): Unit = {
    log.info("InventoryManager Stop")
  }
  override def receive: Receive = {

    case AnyOrder => if (random % 2 == 0) {
      sender() ! Yes("Notebooks", 10)
    } else No
    case ReceivedItem(item) =>log.info(s"${item.quantity} ${item.name} perfectly placed.")
  }
}
