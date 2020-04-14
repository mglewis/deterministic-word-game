package uk.co.mglewis.server

import org.json4s.native.JsonMethods

case class User(
  id: Int,
  firstName: String,
  lastName: String,
  isBot: Boolean
) {
  val fullName: String = s"$firstName $lastName".trim
}

case class Message(
  messageId: Int,
  from: User,
  chat: Chat,
  text: String
)

private case class Update(
  updateId: Int,
  message: Message
)

case class Chat(
  id: Int
)

object MessageSerializer {

  implicit val formats = org.json4s.DefaultFormats

  def deserialize(input: String): Message = {
    val json = JsonMethods.parse(input).camelizeKeys

    json.extract[Update].message
  }

}
