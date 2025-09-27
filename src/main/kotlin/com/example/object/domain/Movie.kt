package com.example.`object`.domain

import java.time.Duration

class Movie(
    private val title: String,
    private val runningTime: Duration,
    val fee: Money,
    private var discountPolicy: DiscountPolicy
) {

    fun calculateMovieFee(screening: Screening): Money {
        return fee.minus(discountPolicy.calculateDiscountAmount(screening))
    }

    fun changeDiscountPolicy(discountPolicy: DiscountPolicy) {
        this.discountPolicy = discountPolicy
    }
}
