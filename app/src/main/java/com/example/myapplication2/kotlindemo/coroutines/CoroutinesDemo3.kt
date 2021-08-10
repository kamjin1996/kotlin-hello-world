package com.example.myapplication2.kotlindemo.coroutines

import kotlinx.coroutines.*

/**
 * 协程之上下文调度器demo
 */
class CoroutinesDemo3 {
}

//定义一个线程log格式化函数
val log = { other: String ->
    println("【ThreadName:${Thread.currentThread().name}】  ".plus(other))
}


fun main() {
    //调度器与线程
    //jobDispatcher()

    //受限调度和非受限调度
//    jobUnconfinedVsConfined();

    //协程的线程切换
    //threadSwitch();

    //子协程的取消随着父协程取消而递归取消 但是通过GlobalScope启动的协程不受其影响
    //subCoroutineRun();

    //父协程对于子协程的职责
//    parentCoroutineRun();

    //给协程命名
    //namedCoroutine();

    /**
     * 只有在有UI线程的情况下才能使用下面代码
     *
     */
//    runBlocking {
//        val myActivity = MyActivity();
//        myActivity.doSomething(); //运行测试函数
//        println("Launched activity!")
//        delay(500L) //延迟半秒钟
//        println("Destroying activity!")
//        myActivity.destory() //取消所有协程
//        delay(1000) //为了在视觉上确认他们没有工作
//    }

    //局部线程变量传递
    threadLocalWithCoroutine();
}

/**
 * 调度器与线程
 */
fun jobDispatcher() = runBlocking {
    launch { // 运行在父协程的上下文中，即 runBlocking 主协程
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Unconfined) { // 不受限的——将工作在主线程中
        println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Default) { // 将会获取默认调度器
        println("Default               : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(newSingleThreadContext("MyOwnThread")) { // 将使它获得一个新的线程
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
    }
}

/**
 * 任务未受限和受限调度
 */
fun jobUnconfinedVsConfined() = runBlocking {
    launch(Dispatchers.Unconfined) { // 非受限的——将和主线程一起工作
        println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
        delay(500)
        println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
    }
    launch { // 父协程的上下文，主 runBlocking 协程
        println("main runBlocking: I'm working in thread ${Thread.currentThread().name}")
        delay(1000)
        println("main runBlocking: After delay in thread ${Thread.currentThread().name}")
    }
}

/*
协程的线程切换
 */
fun threadSwitch() = runBlocking() {

    newSingleThreadContext("Ctx1").use { ctx1 ->
        newSingleThreadContext("Ctx2").use { ctx2 ->
            runBlocking(ctx1) {
                log("Started in ctx1")
                withContext(ctx2) {
                    log("Working in ctx2")
                    //获取协程的上下文
                    println("coroutineContext is:${coroutineContext[Job]}")
                }
                log("Back to ctx1")
            }
        }
    }
}

//子协程
/**
 * 当一个协程被其它协程在 CoroutineScope 中启动的时候， 它将通过 CoroutineScope.coroutineContext 来承袭上下文，
 * 并且这个新协程的 Job 将会成为父协程作业的 子 作业。当一个父协程被取消的时候，所有它的子协程也会被递归的取消。
然而，当使用 GlobalScope 来启动一个协程时，则新协程的作业没有父作业。 因此它与这个启动的作用域无关且独立运作。
 */
fun subCoroutineRun() = runBlocking {
    val request = launch {
        //孵化两个子协程 ，其中一个通过GlobalScope启动
        GlobalScope.launch {
            println("job1: I run in GlobalScope and execute independently!")
            delay(1000)
            println("job1: I am not affected by cancellation of the request")
        }

        //另一个则继承了当前上下文成为子协程
        launch {
            delay(100)
            println("job2: I am a child of the request coroutine")
            delay(1000)
            println("job2: I will not execute this line if my parent request is cancelled")
        }
    }
    delay(500)
    request.cancel() //取消请求的执行
    delay(1000L)
    println("main: Who has survived request cancellation?")
}

//父协程的职责
/**
 * 一个父协程总是等待所有的子协程执行结束。父协程并不显式的跟踪所有子协程的启动，并且不必使用 Job.join 在最后的时候等待它们：
 * 这里使用join是为了展示执行的情况
 */
fun parentCoroutineRun() = runBlocking {
    val request = launch {
        repeat(3) { i ->
            launch {
                delay((i + 1) * 2000L) //延迟200ms 400ms 600ms
                println("Coroutine $i is done")
            }
        }
        println("request: Im done and I dont explicitly join my children that are still active")
    }
    request.join() //等待请求的完成，包括其子协程
    println("Now processing of the request is complete")
}

//给协程命名 CoroutineName("xxx")
fun namedCoroutine() = runBlocking() {
    log("Started main coroutine")
// 运行两个后台值计算
    val v1 = async(CoroutineName("v1coroutine")) {
        delay(500)
        log("Computing v1")
        252
    }
    val v2 = async(CoroutineName("v2coroutine")) {
        delay(1000)
        log("Computing v2")
        6
    }
    log("The answer for v1 / v2 = ${v1.await() / v2.await()}")
}

//组合上下文中的元素
/**
 * 有时我们需要在协程上下文中定义多个元素。我们可以使用 + 操作符来实现。 比如说，
 * 我们可以显式指定一个调度器来启动协程并且同时显式指定一个命名
 */
fun combinationCoroutineRun() = runBlocking {
    launch(Dispatchers.Default + CoroutineName("test")) { }
}

//协程作用域
//这里利用mainScope函数 来创建一个和Activity类生命周期绑定的协程作用域
/**
 * 这里缺少UI（Main线程）线程，所以此处无法启动
 * 目的就是说明协程其实可以和一个类型生命周期绑定，利用一些方法来控制
 */
class MyActivity {

    private val mainScope = CoroutineScope(Dispatchers.Main)

    fun destory() {
        mainScope.coroutineContext.cancel();
    }

    fun doSomething() {
        // 在示例中启动了 10 个协程，且每个都工作了不同的时长
        repeat(10) { i ->
            mainScope.launch {
                delay((i + 1) * 200L) // 延迟 200 毫秒、400 毫秒、600 毫秒等等不同的时间
                println("Coroutine $i is done")
            }
        }
    }

}

//线程局部数据
/**
 * 有时，能够将一些线程局部数据传递到协程与协程之间是很方便的。 然而，由于它们不受任何特定线程的约束，如果手动完成，可能会导致出现样板代码。

ThreadLocal， asContextElement 扩展函数在这里会充当救兵。它创建了额外的上下文元素， 且保留给定 ThreadLocal 的值，并在每次协程切换其上下文时恢复它。
 */
val threadLocal = ThreadLocal<String?>(); //声明线程局部变量
fun threadLocalWithCoroutine() = runBlocking {
    val threadInfo = { str: String ->
        println(str.plus(" current thread: ${Thread.currentThread()},thread local value : '${threadLocal.get()}'"));
    }

    threadLocal.set("main");
    threadInfo("Pre-main")
    val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
        threadInfo("Launch start")
        yield()
        threadInfo("After yield")
    }
    job.join()
    threadInfo("Post-main")
}