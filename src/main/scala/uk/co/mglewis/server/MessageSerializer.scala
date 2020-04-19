package uk.co.mglewis.server

import org.json4s.native.JsonMethods

case class User(
  id: Int,
  firstName: Option[String],
  lastName: Option[String],
  isBot: Boolean
) {
  val name: String = {
    (firstName, lastName) match {
      case (Some(first), _) => first
      case (None, Some(last)) => last
      case (None, None) => s"User $id"
    }
  }
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
