package com.example.`object`

import com.example.`object`.app.ReservationService
import com.example.`object`.domain.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.time.LocalDateTime

@SpringBootTest
class ReservationServiceIntegrationTest {

    @Autowired
    private lateinit var reservationService: ReservationService

    @Test
    fun `예매 통합 테스트`() {
        // given
        val movie = Movie(
            "Avatar",
            Duration.ofMinutes(120),
            Money.wons(10000),
            NoneDiscountPolicy()
        )
        val screening = Screening(
            movie,
            1,
            LocalDateTime.of(2025, 9, 29, 10, 0)
        )
        val customer = Customer("swlee", "1")
        val audienceCount = 1

        // when
        val reservation = reservationService.reserve(customer, screening, audienceCount)

        // then
        assertNotNull(reservation)
        assertEquals(Money.wons(10000), reservation.fee)
        assertEquals(audienceCount, reservation.audienceCount)
    }
}