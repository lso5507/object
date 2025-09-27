package com.example.`object`.domain

import java.time.DayOfWeek
import java.time.LocalTime

interface DiscountCondition {
    fun isSatisfiedBy(screening: Screening): Boolean
}

class PeriodCondition(
    private val dayOfWeek: DayOfWeek,
    private val startTime: LocalTime,
    private val endTime: LocalTime
) : DiscountCondition {
    override fun isSatisfiedBy(screening: Screening): Boolean {
        return screening.startTime.dayOfWeek == dayOfWeek &&
                !screening.startTime.toLocalTime().isBefore(startTime) &&
                !screening.startTime.toLocalTime().isAfter(endTime)
    }
}

class SequenceCondition(private val sequence: Int) : DiscountCondition {
    override fun isSatisfiedBy(screening: Screening): Boolean {
        return screening.sequence == this.sequence
    }
}