package com.example.myapplication2.kotlindemo.coroutines

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

/**
 * 组合与挂起函数
 */
class CoroutinesDemo2 {
}

fun main() {
    //默认按照顺序运行
    //defaultRun();

    //使用async并发执行 实际上其中的协程是并发执行了，await函数等待结果即可
    //asyncRun();

    //惰性的async并发执行，其关键是 ，在start时 才开始执行对应协程
    //lazyAsyncRun();

    //在协程以外的位置运行async函数 kotlin强烈不推荐这样使用
    //noRunBlockingUserAsync();

    //使用async结构化并发
    //concurrentSumAsyncCall()

    //失败后所有的都被取消
    ifFailedAsyncBeCancelRun()
}

suspend fun doSomeThing1(): Int {
    delay(3000L); //做一些事情
    return 13
}

suspend fun doSomeThing2(): Int {
    delay(1000L);//也做一些事情
    return 29
}

/**
 * 默认执行按顺序执行
 *
 * 如果需要按 顺序 调用它们，我们接下来会做什么——首先调用 doSomeThing1 接下来 调用 doSomeThing2，并且计算它们结果的和吗？
 * 实际上，如果我们要根据第一个函数的结果来决定是否我们需要调用第二个函数或者决定如何调用它时，我们就会这样做。

我们使用普通的顺序来进行调用，因为这些代码是运行在协程中的，只要像常规的代码一样 顺序 都是默认的。
下面的示例展示了测量执行两个挂起函数所需要的总时间：
 */
fun defaultRun() = runBlocking {
    val time = measureTimeMillis { //measureTimeMillis测量执行时间
        val one = doSomeThing1();
        val two = doSomeThing2();
        println("The answer is ${one + two}")
    }

    println("Completed in $time ms")
}

/**
 * 异步并发执行
 * 如果doSomeThing1和doSomeThing2没有任何关联，没有依赖，并且我们想更快的得到结果，让它们进行 并发 吗？这就是 async 可以帮助我们的地方
 *
 * 在概念上，async 就类似于 launch。它启动了一个单独的协程，这是一个轻量级的线程并与其它所有的协程一起并发的工作。
 * 不同之处在于 launch 返回一个 Job 并且不附带任何结果值，而 async 返回一个 Deferred —— 一个轻量级的非阻塞 future， 这代表了一个将会在稍后提供结果的 promise。
 * 你可以使用 .await() 在一个延期的值上得到它的最终结果， 但是 Deferred 也是一个 Job，所以如果需要的话，你可以取消它。
 */
fun asyncRun() = runBlocking {
    val time = measureTimeMillis { //measureTimeMillis测量执行时间
        val one = async { doSomeThing1() }
        val two = async { doSomeThing2() }
        println("The answer is ${one.await() + two.await()}") //调用await等待结果即可
    }

    println("Completed in $time ms")
}

//惰性的async执行
/**
 * 可选的，async 可以通过将 start 参数设置为 CoroutineStart.LAZY 而变为惰性的。
 * 在这个模式下，只有结果通过 await 获取的时候协程才会启动，或者在 Job 的 start 函数调用的时候
 * 执行以下代码<br>
 *
 *     在先前的例子中这里定义的两个协程没有执行，但是控制权在于程序员准确的在开始执行时调用 start。
 *     我们首先 调用 one，然后调用 two，接下来等待这个协程执行完毕。
注意，如果我们只是在 println 中调用 await，而没有在单独的协程中调用 start，这将会导致顺序行为，直到 await 启动该协程 执行并等待至它结束，这并不是惰性的预期用例。
在计算一个值涉及挂起函数时，这个 async(start = CoroutineStart.LAZY) 的用例用于替代标准库中的 lazy 函数。
 */
fun lazyAsyncRun() = runBlocking() {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomeThing1() }
        val two = async(start = CoroutineStart.LAZY) { doSomeThing2() }

        //执行一些计算

        one.start() //启动第一个
        two.start() //启动第二个
        println("The answer is ${one.await() + two.await()}") //等待结果
    }
    println("Completed in $time ms")
}

//async风格的函数 注意：kotlin强烈不推荐使用这样的风格
//下面两个函数 不是 挂起函数，他们可以在任何地方使用，在任何地方调用他们都是异步执行的（并发执行的）
fun doSomeThing1Async() = GlobalScope.async { doSomeThing1(); }
fun doSomeThing2Async() = GlobalScope.async { doSomeThing2(); }

//在协程外部使用async函数 注意 这里外部整个方法 没用使用runBlocking
/**
 * 考虑一下如果 val one = somethingUsefulOneAsync() 这一行和 one.await() 表达式这里在代码中有逻辑错误， 并
 * 且程序抛出了异常以及程序在操作的过程中中止，将会发生什么。 通常情况下，一个全局的异常处理者会捕获这个异常，
 * 将异常打印成日记并报告给开发者，但是反之该程序将会继续执行其它操作。
 * 但是这里我们的 somethingUsefulOneAsync 仍然在后台执行， 尽管如此，启动它的那次操作也会被终止。这个程序将不会进行结构化并发，如下一小节所示。
 */
fun noRunBlockingUserAsync() {
    val time = measureTimeMillis {
        //我们可以在协程外面启动异步执行
        val one = doSomeThing1Async() //这里同样返回的是Deferred<Int>类型
        val two = doSomeThing2Async()
        //但是等待结果必须调用其他的挂起或阻塞！
        //当我们等待结果的时候，这里我们使用'runBlocking{...}'来阻塞主线程
        runBlocking {
            println("The answer us ${one.await() + two.await()}")
        }
    }
    println("Completed in $time ms")
}

//使用async结构化并发
/**
 * 将async方法放在协程作用域内
 *
 * 这种情况下，如果在 concurrentSum 函数内部发生了错误，并且它抛出了一个异常， 所有在作用域中启动的协程都会被取消。
 */
suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomeThing1(); }
    val two = async { doSomeThing2() }
    one.await() + two.await()
}

//使用异步，结构化并发
/**
 * 执行方法正确的虽然能执行，但是如果其中一个子协程失败了，那么其他等待的协程都会被取消
 * 更下面的例子验证了这一点
 */
fun concurrentSumAsyncCall() = runBlocking {
    val time = measureTimeMillis {
        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
}

//取消始终通过协程的层次结构来进行传递
/**
 * 如果其中一个子协程（即 two）失败，第一个 async 以及等待中的父协程都会被取消
 */
fun ifFailedAsyncBeCancelRun() = runBlocking {
    try {
        failedConcurrentSum();
    } catch (e: ArithmeticException) {
        println("Computation failed with ArithmeticException")
    }
}

suspend fun failedConcurrentSum() = coroutineScope {
    val one = async<Int> {
        try {
            delay(Long.MAX_VALUE) //模拟一个长时间的运算
            42
        } finally {
            println("First child was cancelled")
        }
    }

    val two = async<Int> {
        println("Second child throws an exception")
        throw ArithmeticException()
    }
    one.await() + two.await()
}
