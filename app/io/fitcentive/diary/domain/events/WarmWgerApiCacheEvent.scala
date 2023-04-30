package io.fitcentive.diary.domain.events

import com.google.pubsub.v1.PubsubMessage
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.fitcentive.sdk.gcp.pubsub.PubSubMessageConverter
import io.fitcentive.sdk.utils.PubSubOps

case class WarmWgerApiCacheEvent(message: String) extends EventMessage

object WarmWgerApiCacheEvent extends PubSubOps {

  implicit val codec: Codec[WarmWgerApiCacheEvent] =
    deriveCodec[WarmWgerApiCacheEvent]

  implicit val converter: PubSubMessageConverter[WarmWgerApiCacheEvent] =
    (message: PubsubMessage) => message.decodeUnsafe[WarmWgerApiCacheEvent]
}
