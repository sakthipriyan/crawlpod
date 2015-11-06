package test

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.actor.ActorSystem

/**
 * @author sakthipriyan
 */
class MyActor extends Actor  {
  val log = Logging(context.system, this)
  def receive = {
    case "hell" => log.info("hell")
    case "test" => log.info("received test")
    case "shutdown" => 
    case _      => log.info("received unknown message")
    
  }
}


object MyActor extends App {
  val system = ActorSystem("root")
  val myActor = system.actorOf(Props[MyActor], "myactor")  
  myActor ! "hell"
  myActor ! "test"
  myActor ! 1
  system.shutdown()
}

