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
package org.evactor.publish

import akka.actor.ActorRef
import org.evactor.expression.Expression
import org.evactor.model.events.Event

/**
 * Specifies how the event should be published
 */
abstract class Publication {

  def channel(event: Event): String
  def categories(event: Event): Set[String] 
  
}

/**
 * Publishing events to a specified channel and category
 */
case class StaticPublication (
    val channel: String,
    val categories: Set[String]) extends Publication {
    
  def channel(event: Event) = channel
  def categories(event: Event) = categories
    
}

/**
 * Extracting values from events with the HasMessage trait
 * to decide where to publish the event.
 */
case class DynamicPublication (
    val channelExpr: Expression,
    val categoryExpr: Option[Expression]) extends Publication {
  
  def channel(event: Event) = channelExpr.evaluate(event) match {
      case Some(v: Any) => v.toString
      case _ => throw new PublishException("couldn't extract a channel from event %s with expression %s".format(event, channelExpr))
  }
  
  def categories(event: Event): Set[String] = categoryExpr match {
    case Some(expr) => expr.evaluate(event) match {
      case Some(t: Traversable[String]) => t.toSet
      case Some(l: Traversable[Any]) => l.map(_.toString).toSet
      case Some(v: Any) => Set(v.toString)
      case _ => throw new PublishException("couldn't extract a category from event %s with expression %s".format(event, expr))
    }
    case None => Set()
  }
}

/**
 * Publishing events to a specified actor (used for testing)
 */
case class TestPublication (val testActor: ActorRef) extends Publication {
    
  def channel(event: Event) = "none"
  def categories(event: Event) = Set()
    
}

