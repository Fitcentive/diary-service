package io.fitcentive.diary.domain.events

import com.google.pubsub.v1.PubsubMessage
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.fitcentive.sdk.gcp.pubsub.PubSubMessageConverter
import io.fitcentive.sdk.utils.PubSubOps

import java.util.UUID

case class CheckIfUsersNeedPromptToLogDiaryEntriesEvent(userIds: Seq[UUID]) extends EventMessage

object CheckIfUsersNeedPromptToLogDiaryEntriesEvent extends PubSubOps {

  implicit val codec: Codec[CheckIfUsersNeedPromptToLogDiaryEntriesEvent] =
    deriveCodec[CheckIfUsersNeedPromptToLogDiaryEntriesEvent]

  implicit val converter: PubSubMessageConverter[CheckIfUsersNeedPromptToLogDiaryEntriesEvent] =
    (message: PubsubMessage) => message.decodeUnsafe[CheckIfUsersNeedPromptToLogDiaryEntriesEvent]
}
