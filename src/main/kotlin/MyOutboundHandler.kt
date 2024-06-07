package org.example

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.util.AttributeKey
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant

@ChannelHandler.Sharable
@Component
class MyOutboundHandler : ChannelDuplexHandler() {
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        if (msg is FullHttpRequest) {
            val requestStartTime = Instant.now().toEpochMilli()
            ctx.channel().attr(AttributeKey.valueOf<Long>("requestStartTime")).set(requestStartTime)
        }

        Mono.deferContextual { contextView ->
            val requestId = contextView.getOrDefault("requestId", "default-request-id")
            println("${Thread.currentThread().name}, Request ID in MyOutboundHandler deferred: $requestId")
//            ctx.channel().attr(AttributeKey.valueOf<String>("requestId")).set(it)
            Mono.just(requestId!!)
        }
            .contextWrite { context ->
            context.putAll(MDCContextLifter.addMDCToContext())
        }
        .subscribe {
            println("${Thread.currentThread().name}, Request ID in MyOutboundHandler subscribe: $it")
            ctx.channel().attr(AttributeKey.valueOf<String>("requestId")).set(it)
            ctx.write(msg, promise)
            // telemetyrClient call would come here
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val requestId: String = ctx.channel().attr(AttributeKey.valueOf<String>("requestId")).get()
        println("${Thread.currentThread().name}, !!! ReqId from channel attribute: $requestId")
        super.channelRead(ctx, msg)
    }
}
