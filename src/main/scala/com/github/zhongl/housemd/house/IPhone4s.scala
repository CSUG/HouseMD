/*
 * Copyright 2013 zhongl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.zhongl.housemd.house

import akka.actor.{ActorRef, IOManager, ActorLogging, Actor}
import akka.actor.IO._
import akka.util.ByteString


/**
 * [[com.github.zhongl.housemd.house.IPhone4s]] is used by [[com.github.zhongl.housemd.house.House]] to communicate with Agent.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class IPhone4s(port: Int, house: ActorRef) extends Actor with ActorLogging {

  val serverHandler = IOManager(context.system).listen("0.0.0.0", port)

  def receive = {
    case Listening(server, address) => log.debug("Mobile is opened.")
    case NewClient(server)          => server.accept().write(ByteString("What's up?"))
    case Read(socket, bytes)        => house ! bytes.decodeString("UTF-8").trim
    case Closed(socket, cause)      =>
      cause match {
        case Error(throwable) => log.error(throwable, "Line is broken!")
        case EOF              =>
        case Chunk(_)         =>
      }
      serverHandler.close()
      context.system.shutdown()
  }
}


