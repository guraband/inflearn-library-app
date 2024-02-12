package com.group.libraryapp.calculator

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CalculatorTest {
    @Test
    fun addTest() {
        // given
        var calculator = Calculator(5)

        // when
        calculator.add(3)

        // then
        assertThat(calculator.number).isEqualTo(8)
    }

    @Test
    fun minusTest() {
        // given
        var calculator = Calculator(5)

        // when
        calculator.minus(2)

        // then
        assertThat(calculator.number).isEqualTo(3)
    }

    @Test
    fun multiplyTest() {
        // given
        var calculator = Calculator(5)

        // when
        calculator.multiply(3)

        // then
        assertThat(calculator.number).isEqualTo(15)
    }

    @Test
    fun divideTest() {
        // given
        var calculator = Calculator(5)

        // when
        calculator.divide(2)

        // then
        assertThat(calculator.number).isEqualTo(2)
    }

    @Test
    fun divideExceptionTest() {
        // given
        var calculator = Calculator(5)

        // when
        val message = assertThrows<IllegalArgumentException> {
            calculator.divide(0)
        }.message

        // then
        assertThat(message).isEqualTo("0으로 나눌 수 없습니다.")
    }
}