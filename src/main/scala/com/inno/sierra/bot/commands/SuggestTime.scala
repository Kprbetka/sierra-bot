package com.inno.sierra.bot.commands

import com.inno.sierra.bot.{MessagesText, Utils}
import com.inno.sierra.model.{ChatSession, ChatState, Event}
import com.typesafe.scalalogging.LazyLogging
import info.mukel.telegrambot4s.api.Extractors
import info.mukel.telegrambot4s.models.{ChatType, Message}

import scala.collection.mutable.ListBuffer

object SuggestTime extends LazyLogging {
  def execute(msg: Message): String = {
    val args = Extractors.commandArguments(msg).get
    if (args.isEmpty) return MessagesText.SUGGESTTIME_NOT_ENOUGH_PARAMS

    val user = msg.from.get
    val chat = msg.chat
    val day = Utils.simpleDateFormat.parse(args.head)

    Start.execute(msg)

    val chatSessions =
      if (chat.`type`.equals(ChatType.Private)) {
        List[ChatSession](ChatSession.getByChatId(chat.id).get)
      } else {
        ChatSession.getMembersOfGroup(chat.id) // TODO: at the chat itself
      }

    logger.debug(chatSessions.toString)

    val events = ListBuffer[Event]()
    chatSessions.foreach(cs =>
      {
        logger.debug("chatSession: " + cs)
        events.appendAll(ChatSession.getEventsForDay(cs.csid, day))
      })
    if (events.isEmpty) {
      MessagesText.SUGGESTTIME_DAY_FREE
    } else {
      val sb = new StringBuffer(MessagesText.SUGGESTTIME_DONE)
      events.foreach(e => sb.append(e.beginDate + " - " + e.endDate + ", ")) // TODO: remove comma
      sb.toString
    }
  }
}
