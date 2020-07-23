package com.knoldus

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, Terminated}
import akka.event.Logging
import com.knoldus.Company.CreateSupplier
import com.knoldus.InventoryAssociate.{Item, ManagerNotFound, RejectOrderException}
import com.knoldus.Supplier.{Delivery, NoEntry}

import scala.concurrent.duration._

class Company extends Actor {
  val log = Logging(context.system, this)
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 second) {
      case ManagerNotFound => Resume
      case RejectOrderException(item) => val newItem = Item(item.name, item.quantity, item.cost, item.quantity)
        sender() ! Delivery(newItem)
        Restart
      case NoEntry => Stop
      case _: Exception => Escalate
    }
  val manager: ActorRef = createManager
  val associate: ActorRef = createAssociate(manager)

  override def preStart(): Unit = {
    log.info(s"Company preStart with path: ${self.path}")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info(s"Company preRestart - $message - $reason")
  }

  override def postStop(): Unit = {
    log.info("Company Stop")
  }

  def createAssociate(inventoryManager: ActorRef): ActorRef = {
    context.actorOf(InventoryAssociate.props(inventoryManager), "associate")
  }

  def createManager: ActorRef = {
    context.actorOf(InventoryManager.props, "manager")
  }

  override def receive: Receive = {
    case CreateSupplier(item: Item) => val supplier = createSupplier(associate)
      supplier ! Delivery(item)

    case Terminated(supplier) => println(s"Bye $supplier")
  }

  def createSupplier(inventoryAssociate: ActorRef): ActorRef = {
    context.actorOf(Supplier.props(inventoryAssociate), "supplier")
  }

}

object Company extends App {

  val system = ActorSystem("company")
  val company = system.actorOf(Company.props, "main")
  val item = Item("Notebook", 10, 40, 12)

  def props: Props = {
    Props(new Company)
  }

  case class CreateSupplier(item: Item)

  company ! CreateSupplier(item)
  Thread.sleep(1000)
  system.terminate()

}