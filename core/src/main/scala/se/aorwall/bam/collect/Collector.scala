package se.aorwall.bam.collect

import akka.actor.{Actor}
import grizzled.slf4j.Logging
import se.aorwall.bam.process.Processor
import se.aorwall.bam.model.events.Event
import se.aorwall.bam.storage.Storage

/**
 * Collecting events
 */
class Collector extends Actor with Storage with Logging {

  def receive = {
    case event: Event => collect(event)
  }

  def collect(event: Event) = {
   
    debug(context.self + " collecting: " + event)

    // save event and check for duplicates
    val storedEvent = storeEvent(event)
    
    storedEvent match {
      case Some(event) => sendEvent(event)
      case None => warn(context.self + " nothing to send")      
    }    
  }

  private[this] def sendEvent(event: Event){    
    // send event to processor
    context.actorFor("../process") ! event
    
    // send event to analyser
    context.actorFor("../analyse") ! event    
  }
  
  override def preStart = {
    trace(context.self + " starting...")
  }

  override def postStop = {
    trace(context.self + " stopping...")
  }
}
