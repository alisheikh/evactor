package se.aorwall.bam.process

import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.BroadcastRouter
import se.aorwall.bam.model.events.Event
import akka.util.Index
import akka.actor.ActorLogging

class ProcessorHandler extends Actor with ActorLogging  {
  
  /**
   * Will try to do a more refined solution for selecting the 
   * right processors for each event later...
   */
  val processorsWithEventName = new Index[String, ActorRef](100, _ compareTo _)
  
  override def preStart = {
    log.debug("starting..")
  }
  
  def receive = {
    case event: Event => process(event)
    case configuration: ProcessorConfiguration => setProcessor(configuration)
    case processorId: String => removeProcessor(processorId)
    case msg => log.info("can't handle: " + msg)
  }
  
  def process(event: Event) {
    log.debug("received event: " + event)
    
  }
    
  def setProcessor(configuration: ProcessorConfiguration) {
    log.debug("setting processor for configuration: " + configuration)
    
    // stopping previous actor if one exists
    val runningActor = context.actorFor(configuration.name)
    context.stop(runningActor)          
    context.actorOf(Props(configuration.getProcessor), name = configuration.name)
  }

  def removeProcessor(processorId: String) {
    log.debug("stopping processor for process: " + processorId)
    val runningActor = context.actorFor(processorId)
    context.stop(runningActor)    
  }

  override def postStop(): Unit = {
    log.debug("stopping..")
  }
}
