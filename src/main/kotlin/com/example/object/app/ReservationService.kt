package com.example.`object`.app

import com.example.`object`.domain.Customer
import com.example.`object`.domain.Reservation
import com.example.`object`.domain.Screening

class ReservationService {

    fun reserve(customer: Customer, screening: Screening, audienceCount: Int): Reservation {
        return screening.reserve(customer, audienceCount)
    }
}
