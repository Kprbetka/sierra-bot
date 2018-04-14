package com.inno.sierra.bot

object MessagesText {

  val INFO: String =
    """Telegram bot created with Scala.
      |This bot is a simple Assistant that provides the following functionality:\n
      |/start: Starts this bot.\n
      |/keepinmind: Creates an Event to Keep in Mind.\n
      |/info:  Displays description (this text).\n
      |/exit:  TODO.\n
      """.stripMargin

  val START_FIRST_TIME: String =
    """Nice to meet you, %s! I can help
      |you to plan your activities. I'll try to be useful for you :)
    """.stripMargin

  val START_AGAIN: String =
    """Welcome back, %s! I'm glad to see you again :)
      |How can I help you?
    """.stripMargin

  val SUBSCRIBE_DONE: String =
    "Now you will be informed about the events in this group :)"

  val SUBSCRIBE_ALREADY: String =
    "You are already subscribed to the events in this chat ;)"

  val ERROR_UNEXPTECTED: String =
    "I'm so sorry! It seems something went wrong, try again, please."
}