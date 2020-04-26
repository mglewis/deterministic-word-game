package uk.co.mglewis.server

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike


class MessageSerializerTest extends AnyWordSpecLike with Matchers {


  "MessageSerializerTest" should {
    "deserialize sensible valid json" in {
      val testString =
        s"""
           |{
           |  "update_id": 541846274,
           |  "message": {
           |    "message_id": 8,
           |    "from": {
           |      "id": 26746447,
           |      "is_bot": false,
           |      "first_name": "Matt",
           |      "last_name": "Lewis",
           |      "language_code": "en"
           |    },
           |    "chat": {
           |      "id": 26746547,
           |      "first_name": "Matt",
           |      "last_name": "Lewis",
           |      "type": "private"
           |    },
           |    "date": 1586812765,
           |    "text": "Word games are fun!"
           |  }
           |}
         """.stripMargin

      val message = MessageSerializer.deserialize(testString)

      message.messageId should be (8)
      message.from.firstName should be (Some("Matt"))
      message.from.lastName should be (Some("Lewis"))
      message.text should be ("Word games are fun!")
    }

  }
}
