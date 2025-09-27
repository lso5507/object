package com.example.`object`.domain

abstract class DiscountPolicy(private vararg val conditions: DiscountCondition) {

    fun calculateDiscountAmount(screening: Screening): Money {
        for (condition in conditions) {
            if (condition.isSatisfiedBy(screening)) {
                return getDiscountAmount(screening)
            }
        }
        return Money.ZERO
    }

    protected abstract fun getDiscountAmount(screening: Screening): Money
}

class AmountDiscountPolicy(
    private val discountAmount: Money,
    vararg conditions: DiscountCondition
) : DiscountPolicy(*conditions) {

    override fun getDiscountAmount(screening: Screening): Money {
        return discountAmount
    }
}

class PercentDiscountPolicy(
    private val percent: Double,
    vararg conditions: DiscountCondition
) : DiscountPolicy(*conditions) {

    override fun getDiscountAmount(screening: Screening): Money {
        return screening.getMovieFee().times(percent)
    }
}

class NoneDiscountPolicy : DiscountPolicy() {
    override fun getDiscountAmount(screening: Screening): Money {
        return Money.ZERO
    }
}