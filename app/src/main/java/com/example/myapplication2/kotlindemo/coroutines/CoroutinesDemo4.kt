package com.example.myapplication2.kotlindemo.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

/**
 * 异步流
 */
class CoroutinesDemo4 {

}


fun main() {

    //流使用演示
    //flowTest();

    //流是冷的 在调用收集时才会启动
    //flowIsCold();

    //流取消 借助超时方法取消输出
    //flowCancelBase();

    //简单的流构建方式 asFlow
//    flowBuildEsay();

    //流操作符使用
    //flowOperate();

    //流转换 可以模拟map与filter等，最主要的是可以emit发射任意数量的流内容，做跟踪等
    //transformOperate();

    //限长操作符 take 可以限制下游获取流值的数量
    //takeOperateRun();

    //末端操作符 collect等
    //endOperateRun();

    //withContext改变了流的上下文，将会报错 ，应该使用flowOn来正确的更改上下文
    //flowChangeError()

    //正确的更改上下文
//    flowChangeSuccess();

    //没有使用缓冲（buffer()）情况下 缓慢
    //flowNoBuffer();

    //有缓冲情况下 快了
    //flowHaveBuffer();

    //合并流运行
//    conflateFlowRun();

    //如果下游处理的太慢，则只处理最新值
    //collectLatestFlowRun()

    //多个流的组合
    //runZipOrCombine();

    //流展平
    //flatMapFlowRun();

    //trycatch了流的异常
    //tryCatchFlowRun();

    /**
     * catch函数透明捕获flow的异常
     */
    //catchMethodFlowRun();

    //声明式的catch流异常
    //statementCatchFlowRun()

    //声明式的流运行方式
    //statementFlowRun()

    //流启动方式
    collectAndLaunchIn()


}

fun simple(): Flow<Int> = flow { //流构建器
    for (i in 1..4) {
        delay(400) //做其他事情
        //Thread.sleep(400) 如果用这个代替delay，则主线程将会被阻塞
        emit(i) //发送下一个值
    }
}

fun simpleStrAndCheck(): Flow<String> = flow { //流构建器
    for (i in 1..4) {
        delay(400) //做其他事情
        //Thread.sleep(400) 如果用这个代替delay，则主线程将会被阻塞
        emit(i) //发送下一个值
    }
}.map { value ->
    check(value <= 1) {
        "Crashed on $value"
    }
    "string $value"
}


/**
 * 一般在执行一个获取结果型的协程方法时，使用 List 结果类型，意味着我们只能一次返回所有值。 为了表示异步计算的值流（stream），
我们可以使用 Flow 类型（正如同步计算值会使用 Sequence 类型）：

 * 这段代码在不阻塞主线程的情况下每等待 100 毫秒打印一个数字。在主线程中运行一个单独的协程每 100 毫秒打印一次 “I'm not blocked” 已经经过了验证。
 *
 * 注意使用 Flow 的代码与先前示例的下述区别：

名为 flow 的 Flow 类型构建器函数。
flow { ... } 构建块中的代码可以挂起。
函数 simple 不再标有 suspend 修饰符。
流使用 emit 函数 发射 值。
流使用 collect 函数 收集 值。

概括（ Flow 可以流的方式输出协程的返回值）

流是连续的，方式类似于stream
流的协程上下文为调用该流的协程
例如在main线程中某个协程调用了流，那么该流上下文就是main线程的协程中

需要注意的是withContext可以改变协程上下文，如果在流中调用了withContext改变了协程上下文，那么运行时将会报错
 *
 */
fun flowTest() = runBlocking {
    //启动并发的协程以验证主线程并未阻塞
    launch {
        for (k in 1..15) {
            println("Im not blocked $k")
            delay(100)
        }
    }
    //收集这个流
    simple().collect { x -> println(x) }
}

/**
 * 流是冷的，只有在调用收集时 才会启动流中代码的运行
 *
 * 这是返回一个流的 simple 函数没有标记 suspend 修饰符的主要原因。
 * 通过它自己，simple() 调用会尽快返回且不会进行任何等待。该流在每次收集的时候启动， 这就是为什么当我们再次调用 collect 时我们会看到“Flow started”。
 */
fun flowIsCold() = runBlocking {
    println("Calling simple function...")
    val flow = simple()
    println("Calling collect...")
    flow.collect { value -> println(value) }
    println("Calling collect again...")
    flow.collect { value -> println(value) }
}

//流取消基础方式
/**
 * 流取消使用withTimeOutOrNull(也可以用withTimeOut)
 */
fun flowCancelBase() = runBlocking() {
    withTimeoutOrNull(850) { //在850ms后超时 (2个多周期)
        simple().collect { value -> println(value) }
    }
    println("Done")
}

//流构建器
/*
* 更简单的流声明方式:
* ---flowOf 构建器定义了一个发射固定值集的流。
* ---使用 .asFlow() 扩展函数，可以将各种集合与序列转换为流。
* */
fun flowBuildEsay() = runBlocking() {
    (1..3).asFlow().collect { x -> println(x) }
}


suspend fun performRequest(request: Int): String {
    delay(1000) //模仿长时间运行的异步工作
    return "response $request"
}

//过渡流操作符
/**
 * 操作符可以对上游流赋予对应的操作 同样也是冷操作，立马返回一个流，供下游使用，类似于集合的map与filter
 */
fun flowOperate() = runBlocking() {
    (1..3).asFlow()
        .map { request -> performRequest(request) }
        .collect { response -> println(response) }
}

//转换操作符
fun transformOperate() = runBlocking() {
    (1..3).asFlow()
        .transform { request ->
            emit("Making request ${request}");
            emit(performRequest(request))
        }
        .collect { response -> println(response) }
}

fun numbers(): Flow<Int> = flow {
    try {
        emit(1)
        emit(2)
        println("This line will not execute")
        emit(3)
    } finally {
        println("Finally in numbers")
    }
}

//限长操作符
/**
 * take 限值下游获取的数量
 * */

fun takeOperateRun() = runBlocking<Unit> {
    numbers()
        .take(2) // 只获取前两个
        .collect { value -> println(value) }
}

//末端操作符 collect就是末端操作符 此外还有很多其他使用的操作符
/*
* ---转化为各种集合，例如 toList 与 toSet。
* ---获取第一个（first）值与确保流发射单个（single）值的操作符。
* ---使用 reduce 与 fold 将流规约到单个值。
**/
fun endOperateRun() = runBlocking() {
    val sum = (1..5).asFlow()
        .map { it * it } //数字1至5的平方
        .reduce { a, b -> a + b } //求和（末端操作符）
    println(sum)
}

fun simpleChangeContextError(): Flow<Int> = flow {
    // 在流构建器中更改消耗 CPU 代码的上下文的错误方式
    kotlinx.coroutines.withContext(Dispatchers.Default) {
        for (i in 1..3) {
            Thread.sleep(100) // 假装我们以消耗 CPU 的方式进行计算
            emit(i) // 发射下一个值
        }
    }
}

fun simpleChangeContextSuccess(): Flow<Int> = flow {
    for (i in 1..3) {
        Thread.sleep(100) // 假装我们以消耗 CPU 的方式进行计算
        log("Emitting $i")
        emit(i) // 发射下一个值
    }
}.flowOn(Dispatchers.Default) // 在流构建器中改变消耗 CPU 代码上下文的正确方式

//流的产生中改变了上下文，此方法会因为withContext 而无法运行
fun flowChangeError() = runBlocking<Unit> {
    simpleChangeContextError().collect { value -> println(value) }
}

//流正确的改变上下文的方式 flowOn
/**
 * 这里要观察的另一件事是 flowOn 操作符已改变流的默认顺序性。
 * 现在收集发生在一个协程中（“coroutine#1”）而发射发生在运行于另一个线程中与收集协程并发运行的另一个协程（“coroutine#2”）中。
 * 当上游流必须改变其上下文中的 CoroutineDispatcher 的时候，flowOn 操作符创建了另一个协程
 */
fun flowChangeSuccess() = runBlocking {
    simpleChangeContextSuccess().collect { value -> println(value) }
}

//流的协程处理以及缓冲
/**
 * 从收集流所花费的时间来看，将流的不同部分运行在不同的协程中将会很有帮助，特别是当涉及到长时间运行的异步操作时。
 * 例如，考虑一种情况， 一个 simple 流的发射很慢，它每花费 100 毫秒才产生一个元素；而收集器也非常慢， 需要花费 300 毫秒来处理元素
 * 会产生这样的结果，整个收集过程大约需要 1200 毫秒（3 个数字，每个花费 400 毫秒）
 * */
fun flowNoBuffer() = runBlocking {
    val time = measureTimeMillis {
        simple().collect { value ->
            delay(300) //假装花费了300ms来处理它
            println(value)
        }
    }
    println("Collected in $time ms")
}

/**
 * 我们可以在流上使用 buffer 操作符来并发运行这个 simple 流中发射元素的代码以及收集的代码， 而不是顺序运行它们
 * 由于我们高效地创建了处理流水线， 仅仅需要等待第一个数字产生的 100 毫秒以及处理每个数字各需花费的 300 毫秒。这种方式大约花费了 1000 毫秒来运行
 *
 * 当必须更改 CoroutineDispatcher 时，flowOn 操作符使用了相同的缓冲机制， 但是我们在这里显式地请求缓冲而不改变执行上下文。
 * 也就是说flowOn默认是开启buffer()的
 */
fun flowHaveBuffer() = runBlocking {
    val time = measureTimeMillis {
        simple()
            .buffer() // 缓冲发射项，无需等待
            .collect { value ->
                delay(300) // 假装我们花费 300 毫秒来处理它
                println(value)
            }
    }
    println("Collected in $time ms")
}

//流支持合并
/**
 * 当流代表部分操作结果或操作状态更新时，可能没有必要处理每个值，而是只处理最新的那个。在本示例中，当收集器处理它们太慢的时候， conflate 操作符可以用于跳过中间值。
 */
fun conflateFlowRun() = runBlocking {
    val time = measureTimeMillis {
        simple()
            .conflate() //合并发射项，不对每个值进行处理
            .collect { value ->
                delay(1500) //假装花费1500ms处理它 尝试改变这个值，会得到不同的合并结果
                println(value)
            }
    }
    println("Collected in $time ms")
}

//处理最新值
/**
 * 当发射器和收集器都很慢的时候，合并是加快处理速度的一种方式。它通过删除发射值来实现。
 * 另一种方式是取消缓慢的收集器，并在每次发射新值的时候重新启动它。有一组与 xxx 操作符执行相同基本逻辑的 xxxLatest 操作符，
 * 但是在新值产生的时候取消执行其块中的代码。让我们在先前的示例中尝试更换 conflate 为 collectLatest：
 *
 * 如果下游处理的太慢，则只处理最新值
 * */
fun collectLatestFlowRun() = runBlocking {
    val time = measureTimeMillis {
        simple()
            .collectLatest { value -> //取消并重新发射最后一个值
                println("Collecting $value")
                delay(1500) //假装我们花费了1500 ms来处理它 一个流发射来是400ms 那么也就是有四个值虽然收集到了 ，但是只处理最新的也就是到第四个 假如处理方不耗时，那么每个值将都能被处理到
                println("Done $value")
            }
    }
    println("Collected in $time ms")
}

//流的组合
//zip和combine
/**
 * flow1.zip(flow2)
 * flow1.combine(flow2)
 *
 * 区别就是 combine时，第一次是对其的，后面的都是错开的去组合输出 但组合是与上次获取到的内容进行组合输出
 */
fun runZipOrCombine() = runBlocking {
    //zip
    val nums = (1..3).asFlow() //发射1到3
    val strs = flowOf("one", "two", "three");
    nums.zip(strs) { a, b ->
        "$a -> $b" //组合字符串
    }.collect { println(it) }

    //combine
    val nums2 = (1..3).asFlow().onEach { delay(300L) } //发射1到3 间隔300ms
    val strs2 = flowOf("one", "two", "three").onEach { delay(400L) } //发射字符串，每个间隔400ms
    val startTime = System.currentTimeMillis() // 记录开始的时间
    nums2.combine(strs2) { a, b ->
        "$a -> $b" //组合字符串和数字
    }.collect { value ->
        println("$value at ${System.currentTimeMillis() - startTime} ms from start")
    }
}

//================展平流==========================
fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: First")
    delay(500) //等待500ms
    emit("$i: Second")
}

/**
 * 展平流
 */
fun flatMapFlowRun() = runBlocking() {
//flatMapConcat 连接模式 它们是相应序列操作符最相近的类似物。它们在等待内部流完成之前开始收集下一个值
    val startTime = System.currentTimeMillis(); //记录开始时间
    (1..3).asFlow().onEach { delay(100) } //每100ms发射一个数字
        .flatMapConcat { requestFlow(it) }
        .collect { value ->
            println("$value at ${System.currentTimeMillis() - startTime} ms from start")
        }

    println("===================================================")
    //flatMapMerge 并发合并模式 并发收集所有传入的流，并将它们的值合并到一个单独的流，以便尽快的发射值
    val startTime2 = System.currentTimeMillis(); //记录开始时间
    (1..3).asFlow().onEach { delay(100) }
        .flatMapMerge { requestFlow(it) }
        .collect { value ->
            println("$value at ${System.currentTimeMillis() - startTime2} ms from start")
        }

    println("===================================================")
    //flatMapLatest 合并最新流模式 ,与 collectLatest 操作符类似（在"处理最新值" 小节中已经讨论过），也有相对应的“最新”展平模式，在发出新流后立即取消先前流的收集。
    val startTime3 = System.currentTimeMillis(); //记录开始时间
    (1..3).asFlow().onEach { delay(100) }
        .flatMapLatest { requestFlow(it) }
        .collect { value ->
            println("$value at ${System.currentTimeMillis() - startTime3} ms from start")
        }

    fun simple(): Flow<Int> = flow {
        for (i in 1..3) {
            println("Emitting $i")
            emit(i) // 发射下一个值
        }
    }
}

/**
 * 流tryCatch异常处理
 * 不论是在流上游还是下游 都会进行捕获
 */
fun tryCatchFlowRun() = runBlocking<Unit> {
    try {
        simple().collect { value ->
            println(value)
            check(value <= 1) { "Collected $value" }
        }
    } catch (e: Throwable) {
        println("Caught $e")
    }
}

//异常透明性
/**
 * 异常处理且仅处理流发射与处理时的状况 使用catch
 */
fun catchMethodFlowRun() = runBlocking<Unit> {
    simpleStrAndCheck()
        .catch { e -> emit("Caught $e") }//发射一个异常 catch只会捕获上游的异常 即catch方法以上的异常
        // 如果在代码的后面发生了异常 异常将不会被捕获
        .collect { value ->
            check(value != "") { "Collected $value" } //当前异常不会被catch方法捕获
            println(value)
        }
}

//声明式捕获
/**
 * 我们可以将 catch 操作符的声明性与处理所有异常的期望相结合，将 collect 操作符的代码块移动到 onEach 中，
 * 并将其放到 catch 操作符之前。收集该流必须由调用无参的 collect() 来触发
 * 来解决上面的捕获问题
 */
fun statementCatchFlowRun() = runBlocking() {
    simpleStrAndCheck()
        .onEach { value ->
            check(value != "") { "Collected $value" }
            println(value)
        }
        .catch { e -> emit("Caught $e") }//发射一个异常 catch只会捕获上游的异常 即catch方法以上的异常
        .collect()
}

//声明式处理代替finally
fun statementFlowRun() = runBlocking() {
    //finally方式处理
    try {
        simpleStrAndCheck()
            .onEach { value ->
                check(value != "") { "Collected $value" }
                println(value)
            }
            .catch { e -> emit("Caught $e") }//发射一个异常 catch只会捕获上游的异常 即catch方法以上的异常
            .collect()

        //使用finally块，在所有元素收集完成 后做处理，如打印Done
    } finally {
        println("Done")
    }

    //声明式方式处理
    simpleStrAndCheck()
        .onEach { value ->
            check(value != "") { "Collected $value" }
            println(value)
        }

        .onCompletion { cause ->
            if (cause != null)
                println("Flow completed exceptionally")
        } //来代替finally的操作 主要优点是 其 lambda 表达式的可空参数 Throwable 可以用于确定流收集是正常完成还是有异常发生，
        // 如果流收集完成后即使抛异常 当前方法也会执行 否则不执行
        //如果下游处理报错，其也会收集到报错信息 可以打印也可以处理等

        .catch { e -> emit("Caught $e") }//发射一个异常 catch只会捕获上游的异常 即catch方法以上的异常
        .collect()
}

//启动流
/**
 * 流的启动方式可以直接使用 末端操作符进行启动，也可以使用【launchIn(this)】 这样就可以继续执行进一步代码而无需等待
 */
fun events(): Flow<Int> = (1..3).asFlow().onEach { delay(100) } //模拟事件 100ms发送一次
fun collectAndLaunchIn() = runBlocking {
    //普通的流启动 使用的协程为flow的协程 下面的方法将会阻塞
    events().onEach { event -> println("Event: $event") }
        .collect()
    println("Done")

    println("============================================")
    //使用launchIn()启动，这样处理流的协程将与不是同一个 可以立即执行下面的步骤
    /**
     * launchIn 必要的参数 CoroutineScope 指定了用哪一个协程来启动流的收集。在先前的示例中这个作用域来自 runBlocking 协程构建器，
     * 在这个流运行的时候，runBlocking 作用域等待它的子协程执行完毕并防止 main 函数返回并终止此示例。
     *
    在实际的应用中，作用域来自于一个寿命有限的实体。在该实体的寿命终止后，相应的作用域就会被取消，即取消相应流的收集。

    这种成对的 onEach { ... }.launchIn(scope) 工作方式就像 addEventListener 一样。
    而且，这不需要相应的 removeEventListener 函数， 因为取消与结构化并发可以达成这个目的。
    注意，launchIn 也会返回一个 Job，可以在不取消整个作用域的情况下仅取消相应的流收集或对其进行 join。
     */
    events().onEach { event -> println("Event: $event") }
        .launchIn(this)
    println("Done")
}

//流取消检测
/**
 * 流取消可以在下游判断 然后调用cancel()方法
 */
fun foo(): Flow<Int> = flow {
    for (i in 1..5) {
        println("Emitting $i")
        emit(i)
    }
}

fun cancelCheckFlowRun() = runBlocking() {
    //非繁忙流 取消检测
    foo()
        .collect { value -> if (value == 3) cancel();println(value) }

    //繁忙流(计算繁忙型流 例如intRange.asFlow产生的流 ，属于性能较高的计算，没有任何的暂停延迟等 就无法及时的取消) 取消
    //使用cancellabel()
    (1..5).asFlow()
        .cancellable()
        .collect { value -> if (value == 3) cancel();println(value) }
}
