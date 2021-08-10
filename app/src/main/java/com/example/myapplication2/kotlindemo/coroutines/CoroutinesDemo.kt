package com.example.myapplication2.kotlindemo.coroutines

import kotlinx.coroutines.*

/**
 * 协程demo 基础和取消与超时
 */
class CoroutinesDemo {

}

fun main() {

    //runBlocking使用
    //runBlockingTest();

    //全局作用域启动协程
    //globalLaunch()

    //局部作用域启动协程
    //localLaunch()

    //协程作用域生效验证
    //coroutineScope()

    //协程代码抽取
    //codeMethodExtract()

    //协程很轻量级
    //coroutineIsLight()

    //全局启动的协程类似于守护线程
    //globalLaunchLikeDeamon()

    //取消协程
    //cancelJob()

    //未能取消的计算中协程
    //notCancelCalingJob()

    //使计算代码可取消
    //doCancelCalingJob();

    //在finally中释放资源
    //withFinallyRelease();

    //运行不能取消的代码块
//    runCantCancelCodeBlock()

    //超时控制
//    timeOutWithException();

    //如果超时时返回null
    // timeOutWithOrNull()

    //异步和资源释放 错误做法
    asyncAndTimeOutX();

    //异步和释放资源 正确做法
    asyncAndTimeOutEnter();
}

/**
 * runBlocking，只要有协程的代码在运行，runBlocking就会进行阻塞
 */
fun runBlockingTest() = runBlocking {
    GlobalScope.launch { // 在后台启动一个新的协程并继续
        delay(1000L) // 非阻塞的等待 1 秒钟（默认时间单位是毫秒）
        println("World!") // 在延迟后打印输出
    }
    println("Hello,") // 协程已在等待时主线程还在继续
    delay(2000L) // 阻塞主线程 2 秒钟来保证 JVM 存活
}

/**
 * 在全局作用域启动的协程 需要使用join来让runBlocking等待
 */
fun globalLaunch() = runBlocking<Unit> {

    val job = GlobalScope.launch { //启动一个协程
        delay(1000L)
        println("World!")
    }

    println("Hello,")
    job.join();
}

/**
 * 不用全局协程作用域启动协程，这样就不需要使用join来加入主协程
 * */
fun localLaunch() = runBlocking {
    launch { //在runBlocking作用域中启动一个新的协程
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}

/*
协程的作用域验证
 */
fun coroutineScope() = runBlocking { // this: CoroutineScope
    launch {
        delay(200L)
        println("Task from runBlocking")
    }

    coroutineScope { // 创建一个协程作用域
        launch {
            delay(500L)
            println("Task from nested launch")
        }

        delay(100L)
        println("Task from coroutine scope") // 这一行会在内嵌 launch 之前输出
    }

    println("Coroutine scope is over") // 这一行在内嵌 launch 执行完毕后才输出
}

/**
 * 协程代码提取方法 需要用suspend修饰
 */
fun codeMethodExtract() = runBlocking {
    launch { doWorld() }
    println("Hello,")
}

/**
 * 包含挂起函数的方法等 需要用suspend修饰 代表挂起函数
 */
suspend fun doWorld() {
    delay(1000L)
    println("World!")
}

/**
 * 协程很轻量级
 * 启动十万个协程，并且在5秒钟后，每个协程都输出一个点
 * 尝试使用线程，很可能会发生内存不足的提示
 */
fun coroutineIsLight() = runBlocking {
    repeat(100_000) { // 启动大量的协程
        launch {
            delay(5000L)
            print(".")
        }
    }
}

/**
 * 全局协程像守护线程
 * 以下代码在 GlobalScope 中启动了一个长期运行的协程，该协程每秒输出“I'm sleeping”，之后在主函数中延迟一段时间后返回。
 *
 */
fun globalLaunchLikeDeamon() = runBlocking {
    GlobalScope.launch {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(9000L) // 在延迟后退出
}

//取消协程
fun cancelJob() = runBlocking {
    val job = launch {
        repeat(1000) { i ->
            println("job: Im sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L); //延迟一段时间
    println("main: Im tired of waiting!")

    job.cancel()
    job.join()
    // 上面两步同 job.cancelAndJoin()

    println("main: Now I can quit.")
}

/**
 * 未能取消的计算中协程
 *
 * 协程的取消是 协作 的。一段协程代码必须协作才能被取消。
 * 所有 kotlinx.coroutines 中的挂起函数都是 可被取消的 。它们检查协程的取消， 并在取消时抛出 CancellationException。
 * 然而，如果协程正在执行计算任务，并且没有检查取消的话，那么它是不能被取消的
 *
 * 运行示例代码，并且我们可以看到它连续打印出了“I'm sleeping”，甚至在调用取消后， 作业仍然执行了五次循环迭代并运行到了它结束为止。
 */
fun notCancelCalingJob() = runBlocking {
    val startTime = System.currentTimeMillis();
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { //一个执行计算的循环 只是为了占用cpu
            // 每秒打印两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: Im sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) //等待一段时间
    println("main: Im tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit.")
}

//使计算代码可取消
/**
 * isActive来获取当前【协程域】是否还在活动状态 <br>
 *     isActive 等价于 coroutineContext[Job]?.isActive == true
 */
fun doCancelCalingJob() = runBlocking {
    val startTime = System.currentTimeMillis();
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0;
        while (isActive) { //可以被取消的计算循环
            //每秒打印两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: Im sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }

    delay(1300L) //等待一段时间
    println("main: Im tired of waiting!")
    job.cancelAndJoin() //取消作业并等待它结束
    println("main: Now I can quit.")

}

//在finally中释放资源
fun withFinallyRelease() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("job: Im sleeping $i...")
                delay(500L)
            }
        } finally {
            println("job: Im running finally...clean")
        }
    }

    delay(1300L)
    println("main: Im tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit.")
}

//运行不能取消的代码块
/**
 * 在前一个例子中任何尝试在 finally 块中调用挂起函数的行为都会抛出 CancellationException，
 * 因为这里持续运行的代码是可以被取消的。通常，这并不是一个问题，所有良好的关闭操作（关闭一个文件、取消一个作业、或是关闭任何一种通信通道）通常都是非阻塞的，
 * 并且不会调用任何挂起函数。
 * 然而，在真实的案例中，当你需要挂起一个被取消的协程，你可以将相应的代码包装在 withContext(NonCancellable) {……} 中，
 * 并使用 withContext 函数以及 NonCancellable 上下文
 */
fun runCantCancelCodeBlock() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("job: Im sleeping $i...")
                delay(500L)
            }
        } finally {
            withContext(NonCancellable) { //标记当前域为不可取消域（代码块）
                println("job: Im running finally")
                delay(1000L)
                println("job: And I've just delayed for 1 sec because Im non-cancellable")
            }
        }
    }

    delay(1300L) //延迟
    println("main: Im tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit.")
}

//超时平抛出异常
/**
 * 在实践中绝大多数取消一个协程的理由是它有可能超时。 当你手动追踪一个相关 Job 的引用并启动了一个单独的协程在延迟后取消追踪，
 * 这里已经准备好使用 withTimeout 函数来做这件事
 *
 * withTimeout 抛出了 TimeoutCancellationException，它是 CancellationException 的子类。
 * 我们之前没有在控制台上看到堆栈跟踪信息的打印。这是因为在被取消的协程中 CancellationException 被认为是协程执行结束的正常原因。
 */
fun timeOutWithException() = runBlocking {
    withTimeout(1300L) {
        repeat(1000) { i ->
            println("Im sleeping $i...")
            delay(500L)
        }
    }
}

//超时结果返回null
/**
 * 由于取消只是一个例外，所有的资源都使用常用的方法来关闭。 如果你需要做一些各类使用超时的特别的额外操作，
 * 可以使用类似 withTimeout 的 withTimeoutOrNull 函数，
 * 并把这些会超时的代码包装在 try {...} catch (e: TimeoutCancellationException) {...} 代码块中，
 * 而 withTimeoutOrNull 通过返回 null 来进行超时操作，从而替代抛出一个异常：
 */
fun timeOutWithOrNull() = runBlocking {
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("Im sleeping $i...")
            delay(500L)
        }
        "Done"
    }
    println("Result is $result")
}


//异步超时和资源
/**
 * 与Timeout的超时事件是异步的代码在其块运行，并可能发生在任何时间，甚至在从超时块内部返回之前。
 * 如果您在块内打开或获取需要关闭或在块外释放的某些资源，请记住这一点。

例如，在这里，我们模仿一个与类的密切资源，只是跟踪多少次，它是通过增加计数器和从其功能减损该计数器创建。
让我们运行大量的coroutine与小超时尝试从块内获取此资源后，有点延迟，并从外部释放它。

<br>
运行当前代码会看到它并不总是打印为零，尽管它可能取决于您的计算机的计时，您可能需要调整此示例中的超时以实际看到非零值
要解决这个问题 看下一个方案
 */
var acquired = 0;

class Resource {
    init {
        acquired++; //获取资源
    }

    fun close() {//释放资源
        acquired--
    }
}

fun asyncAndTimeOutX() = run {
    runBlocking {
        repeat(100_000) { //发射100k个协程
            launch {
                val resource = withTimeout(60) { //60ms超时
                    delay(50) //50ms延迟
                    Resource()
                }
                resource.close() //释放资源
            }
        }
    }
    //在 runBlocking 之外，所有协程都已完成
    println(acquired) //打印仍获取的资源数量
}

/**
 * 正确释放资源的做法
 * 需要在变量中存储对资源的引用，而不是从块中返回资源
 */
fun asyncAndTimeOutEnter() = run {
    runBlocking {
        repeat(100_000) { //开启100k个协程
            launch {
                var resource: Resource? = null //尚未获得资源

                try {
                    withTimeout(60) {
                        delay(50)
                        resource = Resource() //如果获取了资源，则将资源存储到变量中
                    }
                    //我们可以用这里的资源做些其他事情
                } finally {
                    resource?.close(); //释放资源 如果已经获取了的话
                }

            }
        }
    }

    //在 runBlocking 之外，所有协程都已完成
    println(acquired)
}
