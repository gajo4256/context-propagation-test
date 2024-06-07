package org.example

import org.slf4j.MDC
import reactor.util.context.Context
import reactor.util.context.ContextView


object MDCContextLifter {
    fun addMDCToContext(): ContextView {
        var context = Context.empty()
        val requestId = MDC.get("requestId")
        if (requestId != null) {
            context = context.put("requestId", requestId)
        }
        return context
    }
}
