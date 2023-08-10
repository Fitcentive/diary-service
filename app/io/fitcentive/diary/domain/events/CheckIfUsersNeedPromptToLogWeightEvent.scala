package io.fitcentive.diary.domain.events

import com.google.pubsub.v1.PubsubMessage
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.fitcentive.sdk.gcp.pubsub.PubSubMessageConverter
import io.fitcentive.sdk.utils.PubSubOps

import java.util.UUID

case class CheckIfUsersNeedPromptToLogWeightEvent(userIds: Seq[UUID]) extends EventMessage

object CheckIfUsersNeedPromptToLogWeightEvent extends PubSubOps {

  implicit val codec: Codec[CheckIfUsersNeedPromptToLogWeightEvent] =
    deriveCodec[CheckIfUsersNeedPromptToLogWeightEvent]

  implicit val converter: PubSubMessageConverter[CheckIfUsersNeedPromptToLogWeightEvent] =
    (message: PubsubMessage) => message.decodeUnsafe[CheckIfUsersNeedPromptToLogWeightEvent]
}
