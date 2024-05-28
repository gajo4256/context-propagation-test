package org.example

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
open class WebClientConfig(
    private val myOutboundHandler: MyOutboundHandler
) {

    @Bean
    open fun webClient(): WebClient {
        val httpClient: HttpClient = HttpClient.create()
            .doOnRequest { _, connection -> connection.addHandlerFirst(myOutboundHandler) }
//            .responseTimeout(Duration.ofMillis(responseTimeout))

        val wcBuilder = WebClient.builder().clientConnector(ReactorClientHttpConnector(httpClient))
        return wcBuilder
            .filter { request, next ->
                Mono.deferContextual { contextView ->
                    val requestId = contextView.getOrDefault("requestId", "default-request-id")
                    val updatedRequest = ClientRequest.from(request)
                        .header("requestId", requestId)
                        .build()
                    next.exchange(updatedRequest)
                }
            }
            .build()
    }
}
