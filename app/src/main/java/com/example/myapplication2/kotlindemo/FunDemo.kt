package com.example.myapplication2

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * 函数demo
 */
class FunDemo {
}

fun main() {
    println(intResult(2))
    println(intResult2(2))
    println(0 intResult3 2)
    increaseToTen(2)
    println(1.sum1(10))

    println(2.sum2(5))
    println(sum3(1, 2))

    html {
        body()
    }


    val l = ReentrantLock();

    lock(l) { foo() }
}


inline fun <T> lock(lock: Lock, body: () -> T): T {
    lock.lock()
    try {
        return body()
    } finally {
        lock.unlock()
    }
}

//普通=函数
fun intResult(x: Int) = x * 2

/**
 * 可变参数
 */
fun intResult2(vararg x: Int) = x.toList().map { i -> i * 2 }.sum()

/**
 * 中缀函数
 */
infix fun Int.intResult3(x: Int): Int = x * 2

/**
 * 递归函数
 */
tailrec fun increaseToTen(x: Int): Int = if (x >= 10) 10 else increaseToTen(x + 1)

/**
 * 类型拓展函数
 */
val sum1: Int.(Int) -> Int = { other -> plus(other) }

/**
 * 类型拓展函数2（实用）
 */
val sum2 = fun Int.(other: Int): Int = this + other

/**
 * 简单函数式
 */
val sum3 = { x: Int, y: Int -> x + y }

class HTML {
    fun body() {}
}

fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()
    html.init()
    return html
}

fun foo() {}

