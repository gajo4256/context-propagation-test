package org.example

import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@RestController
class MyController(
    private val webClient: WebClient,
    @Qualifier("webClient2")
    private val vwacWebClient: WebClient

) {

    @GetMapping("/hello")
    fun hello(@RequestHeader("requestId", required = false) requestId: String?): Mono<String> {
        if (requestId != null) {
            MDC.put("requestId", requestId)
        }
        val mono =
//            Mono.just("Hello, World!")
//            .flatMap {
                webClient.get()
                    .uri("https://jsonplaceholder.typicode.com/todos/1")
                    .retrieve()
                    .bodyToMono(String::class.java)
//            }
        // @DUSKO it also works if this is commented out, since we set it also in MyOutboundHandler
//            .contextWrite { ctx -> ctx.putAll(MDCContextLifter.addMDCToContext()) }
        return mono
    }

    @GetMapping("/hello2")
    fun hello2(@RequestHeader("requestId", required = false) requestId: String?): Mono<String> {
        if (requestId != null) {
            MDC.put("requestId", requestId)
        }
        val mono =
//            Mono.just("Hello, World!")
//            .flatMap {
            vwacWebClient.get()
                .uri("https://jsonplaceholder.typicode.com/todos/1")
                .retrieve()
                .bodyToMono(String::class.java)
//            }
//                .contextWrite { ctx -> ctx.putAll(MDCContextLifter.addMDCToContext()) }
        return mono
    }


}
