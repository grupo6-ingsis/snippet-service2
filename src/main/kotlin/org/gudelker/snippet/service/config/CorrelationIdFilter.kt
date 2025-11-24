package org.gudelker.snippet.service.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

// Se ejecuta antes que nada
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : OncePerRequestFilter() {
    companion object {
        const val CORRELATION_ID_KEY = "correlation-id"
        const val CORRELATION_ID_HEADER = "X-Correlation-Id"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // Obtener el ID del header o generar uno nuevo
        val requestHeader = request.getHeader(CORRELATION_ID_HEADER)
        val correlationId =
            if (requestHeader.isNullOrBlank()) {
                UUID.randomUUID().toString()
            } else {
                requestHeader
            }

        // Ponerlo en el "Contexto de Diagnóstico Mapeado" (MDC)
        MDC.put(CORRELATION_ID_KEY, correlationId)

        // Devolverlo en la respuesta para facilitar el debugging al frontend
        response.addHeader(CORRELATION_ID_HEADER, correlationId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            // Limpiar al terminar para no mezclar logs de otros hilos
            MDC.remove(CORRELATION_ID_KEY)
        }
    }
}
