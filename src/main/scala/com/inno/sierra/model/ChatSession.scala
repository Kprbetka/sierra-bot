package com.inno.sierra.model

import java.sql.Timestamp
import java.util.Date
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column

import scala.collection.mutable

object ChatState extends Enumeration {
    type ChatState = Value
    val Started = Value(1, "Started")
    val CreatingEventInputtingName = Value(2, "CreatingEventInputtingName")
    val CreatingEventInputtingDate = Value(3, "CreatingEventInputtingDate")
    val CreatingEventInputtingTime = Value(4, "CreatingEventInputtingTime")
    val CreatingEventInputtingDuration = Value(5, "CreatingEventInputtingDuration")
    val EditingEvent = Value(6, "EditingEvent")
  }

/**
  * There are many inconviniencies with using Squeryl
  * with enum fields, therefore chatState is stored as Int
  * @param id - id will be assigned automatically by ORM
  * @param csid - chatsession id
  * @param alias - user's alias or "" if it's a group
  * @param isGroup - is this is a group (supergroup/channel) or not
  * @param _chatState - state of the chat
  */
case class ChatSession (
                         var id: Long,
                         var csid: Long,
                         var alias: String,
                         var isGroup: Boolean,
                         @Column("CHATSTATE") var _chatState: Int,
                         var inputEventName: Option[String] = None,
                         var inputEventYear: Option[Int] = None,
                         var inputEventMonth: Option[Int] = None,
                         var inputEventDay: Option[Int] = None,
                         var inputEventHour: Option[Int] = None,
                         var inputEventMinutes: Option[Int] = None,
                         var inputEventDurationInMinutes: Option[Int] = None,
                         var inputCalendarMessageId: Option[Int] = None,
                         var inputTimepickerMessageId: Option[Int] = None,
                         var inputDurationpickerMessageId: Option[Int] = None
                       ) extends KeyedEntity[Long] {

  def chatState: ChatState.ChatState = {
    ChatState.values.find(_.id == _chatState).get
  }
  // scalastyle:off method.name
  def chatState_=(chatStateNew: ChatState.ChatState): Unit = {
    _chatState = chatStateNew.id
  }
  // scalastyle:on method.name

  def resetInputs(): Unit = {
    inputEventName = None
    inputEventYear = None
    inputEventMonth = None
    inputEventDay = None
    inputEventHour = None
    inputEventMinutes = None
    inputEventDurationInMinutes = None
    inputCalendarMessageId = None
    inputTimepickerMessageId = None
    inputDurationpickerMessageId = None
  }

  def getEventDate: String = {
    val eventDateStr = for {
      year <- inputEventYear
      month <- inputEventMonth
      dayOfMonth <- inputEventDay
      result = "%02d.%02d.%04d".format(dayOfMonth, month, year)
    } yield result

    eventDateStr.getOrElse("None")
  }

  def getEventTime: String = {
    val eventTimeStr = for {
      hour <- inputEventHour
      minutes <- inputEventMinutes
      result = "%02d:%02d".format(hour, minutes)
    } yield result

    eventTimeStr.getOrElse("None")
  }

  def save() = DbSchema.update(this)

  def getMembers(): List[ChatSession] = {
    DbSchema.getMembers(csid)
  }
}


object ChatSession {
  val DEFAULT_STATE = ChatState.Started

  def create(csid: Long, alias: String, isGroup: Boolean,
             chatState: ChatState.ChatState): ChatSession = {
    DbSchema.insert(new ChatSession(0, csid, alias, isGroup, chatState.id))
  }

  /**
    * Returns the set of chat session:
    * @param ids - for the specified id, if Some provided,
    *            for all existing if None.
    * @return - mutable set of chat sessions.
    */
  def getAll(ids: Option[List[Long]]) = {
    DbSchema.getAll[ChatSession](ids)
  }

  def getByChatId(csid: Long) = {
    DbSchema.getChatSessionByChatId(csid)
  }

  def getMembersOfGroup(csid: Long): List[ChatSession] = {
    DbSchema.getMembers(csid)
  }

  def update(cs: ChatSession) = {
    DbSchema.update[ChatSession](cs)
  }

  def exists(csid: Long): Boolean = {
    DbSchema.getChatSessionByChatId(csid).isDefined
  }

  def hasIntersections(csid: Long, beginDate: Date, endDate: Date) = {
    val begin = new Timestamp(beginDate.getTime)
    val end = new Timestamp(endDate.getTime)
    DbSchema.getEventsWithLimits(csid, begin, end)
  }

  def getEventsForDay(csid: Long, day: Date) = {
    DbSchema.getAllEventsForDay(csid, day)
  }

  def addUserToGroup(groupChatId: Long,
                     memberChatId: Long, memberAlias: String) = {
    val group = DbSchema.getChatSessionByChatId(groupChatId)
      .getOrElse(
        ChatSession.create(groupChatId, "", isGroup = true, DEFAULT_STATE)
      )
    val member = DbSchema.getChatSessionByChatId(memberChatId)
      .getOrElse(
        ChatSession.create(
          memberChatId, memberAlias, isGroup = false, DEFAULT_STATE)
      )

    val gm = GroupMembers(group.id, member.id)
    DbSchema.insert[GroupMembers](gm)
  }

  def removeUserFromGroup(groupChatId: Long,
                          memberChatId: Long): Unit = {
    val group = DbSchema.getChatSessionByChatId(groupChatId)
      .getOrElse(
        ChatSession.create(groupChatId, "", isGroup = true, DEFAULT_STATE)
      )
    val member = DbSchema.getChatSessionByChatId(memberChatId)
      .getOrElse(
        return
      )

    val gm = GroupMembers(group.id, member.id)
    DbSchema.delete(gm)
  }
}