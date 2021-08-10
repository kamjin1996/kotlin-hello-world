package com.example.myapplication2

/**
 * 其他
 * 1.运算符
 */
class OtherDemo {
}

fun main() {
    //一元运算符
    var calculate = Calculate(1, 2);

    println(+calculate)
    println(-calculate)
    println(!calculate)

    println("============")

    //二元运算符
    val calculate1 = calculate++

    println(calculate1.x)
    println(calculate1.y)

    println("============")
    val calculate2 = calculate--
    println(calculate2.x)
    println(calculate2.y)
    println("============")

    //算数运算符
    val calculate31 = calculate + Calculate2(5, 6)
    println(calculate31.x)
    println(calculate31.y)

    val calculate32 = calculate - Calculate2(5, 6)
    println(calculate32.x)
    println(calculate32.y)

    val calculate33 = calculate * Calculate2(5, 6)
    println(calculate33.x)
    println(calculate33.y)

    val calculate34 = calculate / Calculate2(5, 6)
    println(calculate34.x)
    println(calculate34.y)

    val calculate35 = calculate % Calculate2(5, 6)
    println(calculate35.x)
    println(calculate35.y)

    val calculate36 = calculate..Calculate2(5, 6)
    calculate36.forEach { println("rangeTo: ${it.x} ${it.y}") }

    val calculate37 = calculate in Calculate2(5, 6)
    println(calculate37)

    calculate[3].let(::println)

}

operator fun Calculate.inc(): Calculate {
    x++
    y++
    return this;
}

operator fun Calculate.dec(): Calculate {
    x--
    y--
    return this;
}

operator fun Calculate.not(): String {
    return "not: ${this.x != this.y}"
}

//一元操作符 + - !
open class Calculate(var x: Int = 0, var y: Int = 0) {

    operator fun unaryPlus(): Int = x + y
    operator fun unaryMinus(): Int = x - y
    //operator fun not(): Boolean = x != y

    operator fun plus(calculate2: Calculate2) =
        Calculate3(this.x + calculate2.x, this.y + calculate2.y)

    operator fun minus(calculate2: Calculate2) =
        Calculate3(this.x - calculate2.x, this.y - calculate2.y)


    operator fun times(calculate2: Calculate2) =
        Calculate3(this.x * calculate2.x, this.y * calculate2.y)

    operator fun div(calculate2: Calculate2) =
        Calculate3(this.x / calculate2.x, this.y / calculate2.y)

    operator fun rem(calculate2: Calculate2) =
        Calculate3(this.x % calculate2.x, this.y % calculate2.y)

    operator fun rangeTo(calculate2: Calculate2): MutableList<Calculate3> {
        val result: MutableList<Calculate3> = mutableListOf(Calculate3(0, 0));
        for (i in x..calculate2.x) {
            result.add(Calculate3(i, i));
        }
        return result
    }

    operator fun get(x: Int): String {
        val s = when (x) {
            1 -> "1"
            2 -> "2"
            3 -> "3"
            4 -> "4"
            5 -> "5"
            else -> "null"
        }
        return s
    }

}

class Calculate2(x: Int, y: Int) : Calculate(x, y) {
    operator fun contains(calculate: Calculate): Boolean {
        return false
    }
}

class Calculate3(x: Int, y: Int) : Calculate(x, y) {
    val key: String = x.toString()

    fun codeStyleTest(
        baaaaaaaasdasdasdasdcasdasdasdasdasdasdasaaaab: String,
        ccasdaasdasdasdasdasdascccasdaasdasdasdasdasdascccasdaasdasdasdasdasdascccasdaasdasdasdasdasdasc: String,
        ascasdasdasdasdssssssssssssssssssssssssssssssssssssssssssssssas: Int
    ) {
        println(baaaaaaaasdasdasdasdcasdasdasdasdasdasdasaaaab)
        println(
            ccasdaasdasdasdasdasdascccasdaasdasdasdasdasdascccasdaasdasdasdasdasdascccasdaasdasdasdasdasdasc
        )
        println(ascasdasdasdasdssssssssssssssssssssssssssssssssssssssssssssssas)
        println()
    }
}

