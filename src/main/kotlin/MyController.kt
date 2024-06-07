package org.example

import org.slf4j.MDC
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration

@RestController
class MyController(
    private val webClient: WebClient
) {

    @GetMapping("/hello")
    fun hello(@RequestHeader("requestId", required = false) requestId: String?): Mono<String> {
        if (requestId != null) {
            MDC.put("requestId", requestId)
        }
        val mono =
            webClient.get()
                .uri("https://jsonplaceholder.typicode.com/todos/1")
                .retrieve()
                .bodyToMono(String::class.java)
                .contextWrite { ctx -> ctx.putAll(MDCContextLifter.addMDCToContext()) }
                .doFinally {
                    println("${Thread.currentThread().name}: deleting MDC")
//                    MDC.clear()
                }
        return mono
    }

}
