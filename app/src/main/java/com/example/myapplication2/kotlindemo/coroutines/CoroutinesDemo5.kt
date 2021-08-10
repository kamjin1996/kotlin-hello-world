package com.example.myapplication2.kotlindemo.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

/**
 * 协程的通道
 * 通道 便携的在协程之间传值的方法
 *
 * 通道 channel 和BlockingQueue 非常类似的概念，其中一个不同是它替代了【阻塞的put】操作，提供【挂起的了send】，
 * 还替代了【阻塞的take】操作，并提供了【挂起的receive】
 */
class CoroutinesDemo5 {

}

fun main() {

    //通道基础演示
    //channelBaseRun();

    //通道的关闭与迭代
    //channelIterateRun();

    //通道的便携发送与关闭
    //channelProduceAndConsumeMethod();

    //管道使用演示
//    channelPipelineRun();

    //利用管道挑选出素数
//    numbersFilterChannelRun();

    //扇出
//    launchMultiForOnePipeline();

    //扇入
//    multiPipelineForLaunch();

    //通道指定缓存区大小
//    channelMakeCapacity()

    //通道的公平特性
    pingpongChannelRun();
}

/**
 * 通道基础演示
 */
fun channelBaseRun() = runBlocking {
    val channel = Channel<Int>();
    launch {
        //这里可能是消耗大量CPU运算的异步逻辑，我们将仅仅做5次整数平方并发送
        for (x in 1..5) {
            channel.send(x * x)
        }
    }

    //这里我们打印了5次被接收的整数:
    repeat(5) { println(channel.receive()) }
    println("Done")
}

/**
 * 关闭与迭代通道
 * 和队列不同，一个通道可以通过被关闭来表明没有更多的元素将会进入通道。 在接收者中可以定期的使用 for 循环来从通道中接收元素。

从概念上来说，一个 close 操作就像向通道发送了一个特殊的关闭指令。 这个迭代停止就说明关闭指令已经被接收了。
所以这里保证所有先前发送出去的元素都在通道关闭前被接收到。
 */
fun channelIterateRun() = runBlocking() {
    val channel = Channel<Int>()
    launch {
        for (x in 1..5) channel.send(x * x)
        channel.close() //关闭通道
    }
    // 这里使用for循环来迭代所有的元素 直到通道被关闭
    for (y in channel) println(y)
    println("Done")
}

//简便方式构建通道生产者与消费者
/**
 * 协程生成一系列元素的模式很常见。 这是 生产者——消费者 模式的一部分，并且经常能在并发的代码中看到它。
 * 你可以将生产者抽象成一个函数，并且使通道作为它的参数，但这与必须从函数中返回结果的常识相违悖。

这里有一个名为 produce 的便捷的协程构建器，可以很容易的在生产者端正确工作，(创建一个与CoroutineScope作用域绑定的函数)
并且我们使用扩展函数 consumeEach 在消费者端替代 for 循环：
 * */
fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
    for (x in 1..5) send(x * x)
}

fun channelProduceAndConsumeMethod() = runBlocking() {
    val produceSquares = produceSquares()
    produceSquares.consumeEach { println(it) }
    println("Done!")
}

//管道
/**
 *  管道是一种一个协程在流中开始生产可能无穷多个元素的模式
 *
 */
//创建无穷元素的管道
fun CoroutineScope.produceNumbers() = produce<Int>() {
    var x = 1;
    while (true) send(x++)
}

//另一个或多个协程开始消费这些流，做一些操作，并生产额外的结果，在下面例子中对这些数字做了平方
fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce() {
    for (x in numbers) send(x * x)
}

//管道演示
/**
 * 所有创建了协程的函数被定义在了 CoroutineScope 的扩展上， 所以我们可以依靠结构化并发来确保没有常驻在我们的应用程序中的全局协程。
 */
fun channelPipelineRun() = runBlocking() {
    val numbers = produceNumbers() //从1开始生成整数
    val squares = square(numbers)
    repeat(5) { //输出前5个
        println(squares.receive())
    }
    println("Dnoe")
    coroutineContext.cancelChildren() //取消子协程
}

//使用管道的素数
fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
    var x = start
    while (true) send(x++) // 开启了一个无限的整数流
}

//过滤
fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
    for (x in numbers) if (x % prime != 0) send(x)
}

/**
 * 使用管道过滤出素数
 * 注意，你可以在标准库中使用 iterator 协程构建器来构建一个相似的管道。
 * 使用 iterator 替换 produce、yield 替换 send、next 替换 receive、 Iterator 替换 ReceiveChannel 来摆脱协程作用域，你将不再需要 runBlocking。
 * 然而，如上所示，如果你在 Dispatchers.Default 上下文中运行它，使用通道的管道的好处在于它可以充分利用多核心 CPU。

不过，这是一种非常不切实际的寻找素数的方法。在实践中，管道调用了另外的一些挂起中的调用（就像异步调用远程服务）并且这些管道不能内置使用 sequence/iterator，
因为它们不被允许随意的挂起，不像 produce 是完全异步的。
 */
fun numbersFilterChannelRun() = runBlocking() {
    var cur = numbersFrom(2);
    repeat(10) {
        val receive = cur.receive()
        println(receive)
        cur = filter(cur, receive)
    }
    coroutineContext.cancelChildren() //取消所有的子协程来让主协程结束
}

//扇出
/***
 * 多个协程也许会接收相同的管道，在它们之间进行分布式工作。 让我们启动一个定期产生整数的生产者协程 （每秒十个数字）
 *
 */
fun CoroutineScope.produceNumbersDelay() = produce<Int> {
    var x = 1;
    while (true) {
        send(x++);
        delay(100) //等待0.1秒
    }
}

//接下来我们可以得到几个处理器协程。在这个示例中，它们只是打印它们的 id 和接收到的数字
fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        println("Processor #$id received $msg")
    }
}

/**
 * 一个管道产生的值被多个协程接受 称为扇出
 */
fun launchMultiForOnePipeline() = runBlocking() {
    val producer = produceNumbersDelay()
    repeat(5) { launchProcessor(it, producer) }
    delay(950)
    producer.cancel() //取消协程生产者从而kill掉所有的协程
}

//扇入
/**
 * 多个协程可以发送到同一个通道。 比如说，让我们创建一个字符串的通道，和一个在这个通道中以指定的延迟反复发送一个指定字符串的挂起函数
 * */
suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}

/**
 * 扇入
 * 管道被不同的途径以不同的的频率与内容利用多个协程产生元素，管道进行迭代接受
 */
fun multiPipelineForLaunch() = runBlocking() {
    val channel = Channel<String>()
    launch { sendString(channel, "foo", 200L) }
    launch { sendString(channel, "BAR!", 500L) }
    repeat(6) { // 接收前六个
        println(channel.receive())
    }
    coroutineContext.cancelChildren() // 取消所有子协程来让主协程结束
}

// channel通道是可以指定缓存区大小的 可选参数capacity ,这样生产元素时如果超过了缓冲区大小时，通道将会被挂起并阻塞 类似 BlockingQueue指定大小
fun channelMakeCapacity() = runBlocking() {
    val channel = Channel<Int>(4) // 启动带缓冲的通道
    val sender = launch { // 启动发送者协程
        repeat(10) {
            println("Sending $it") // 在每一个元素发送前打印它们
            channel.send(it) // 将在缓冲区被占满时挂起
        }
    }
// 没有接收到东西……只是等待……
    delay(1000)
    sender.cancel() // 取消发送者协程
}

//通道是公平的
/**
 * 发送和接收操作是 公平的 并且尊重调用它们的多个协程。它们遵守先进先出原则，可以看到第一个协程调用 receive 并得到了元素。
 * 在下面的例子中两个协程“乒”和“乓”都从共享的“桌子”通道接收到这个“球”元素。
 */
data class Ball(var hits: Int)

fun pingpongChannelRun() = runBlocking() {
    val table = Channel<Ball>() //一个共享的table 桌子
    launch { player("ping", table) }
    launch { player("pong", table) }
    table.send(Ball(0)) //乒乓球
    delay(1000)//延迟一秒
    coroutineContext.cancelChildren() //游戏结束
}

suspend fun player(name: String, table: Channel<Ball>) {
    for (ball in table) { //在循环中接受球
        ball.hits++
        println("$name $ball")
        delay(300) //等待一段时间
        table.send(ball) //将球发过去
    }
}