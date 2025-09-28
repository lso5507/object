package com.example.`object`

import com.example.`object`.app.ReservationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun reservationService(): ReservationService {
        return ReservationService()
    }
}
