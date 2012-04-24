/*
 * Copyright 2012 Albert Örwall
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.evactor.process.extract

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.evactor.model.attributes.HasMessage
import org.evactor.model.events.Event
import org.evactor.process._
import org.evactor.expression.ExpressionEvaluator

/**
 * Extract information from messages. 
 * 
 * It will first evaluate the message in the event with an ExpressionEvaluator
 * and then create a new Event, based on the evaluated message, with an EventCreator.
 */
abstract class Extractor(
    override val subscriptions: List[Subscription], 
    val channel: String,
    val expression: String) 
  extends Processor(subscriptions) 
  with EventCreator
  with ExpressionEvaluator
  with Monitored
  with Publisher
  with ActorLogging {
         
  type T = Event with HasMessage
  
  override def receive  = {
    case event: Event with HasMessage => process(event)
    case actor: ActorRef => testActor = Some(actor) 
    case msg => log.debug("can't handle " + msg )
  }
  
  override protected def process(event: Event with HasMessage) {
    
    log.debug("will extract values from {}", event )
	  
    createBean(evaluate(event), event, channel) match {
      case Some(event) => {
        testActor match {
          case Some(actor: ActorRef) => actor ! event
          case None => publish(event)

        }        
      }
      case None => log.info("couldn't extract anything from event: {}", event)
    }
    
  }
}
