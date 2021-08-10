package com.example.myapplication2

import android.app.Activity
import android.view.View
import android.widget.Button
import java.util.*

/**
 * 普通函数
 *
 * 以下是根据预期目的选择作用域函数的简短指南：

对一个非空（non-null）对象执行 lambda 表达式：let
将表达式作为变量引入为局部作用域中：let
对象配置：apply
对象配置并且计算结果：run
在需要表达式的地方运行语句：非扩展的 run
附加效果：also
一个对象的一组函数调用：with
不同函数的使用场景存在重叠，你可以根据项目或团队中使用的特定约定选择函数。

尽管作用域函数是使代码更简洁的一种方法，但请避免过度使用它们：这会降低代码的可读性并可能导致错误。
避免嵌套作用域函数，同时链式调用它们时要小心：此时很容易对当前上下文对象及 this 或 it 的值感到困惑。
 */
fun cal(p1: Int, p2: Int, event1: (Int, Int) -> Int, event2: (Int, Int) -> Int) {
    println();
    println("执行event1 ${event1(p1, p2)}");
    println("执行event2 ${event2(p1, p2)}");
}

/**
 * lambda函数
 */
val sum = { x: Int, y: Int ->
    print("求和 ")
    x + y;
}
val diff = { x: Int, y: Int ->
    print("求差 ")
    x - y;
}

fun main() {
    cal(p1 = 10, 20, event1 = sum, event2 = diff);

    //val btnLambda = DemoActivity().btnLambda()
    //btnLambda.setOnClickListener({ println("outout") })

    // lambda形参数如果在最后一个参数时，则可以在调用时 直接使用大括号来编写lambda的方法体
    testLambda("param1") { println("in");"out" }

    testLambda2 { println("in");"out" }

    testRun()
}

fun testLambda(s: String, block: () -> String) {
    println();
    block()
}

fun testLambda2(block: () -> String) {
    block();
}

class DemoActivity : Activity() {

    fun btnLambda(): Button {
        val button = Button(this)
        val vic = View.OnClickListener { println("asda") };
        with(button) {
            this.setOnClickListener({ println("asda") })
            this.setOnClickListener(vic)
        }
        return button
    }
}

class A {
    val a = 1;
    val b = "b";

    public fun showA() {
        println("a=${this.a}")
    }

    public fun showB() {
        println("b=${this.b}")
    }
}

fun testRun() {
    val runTest = run<String> {
        println("oneoneone")
        "outoutout"
    }

    println("run执行结果：${runTest}")

    val a = A()

    println("===============run================")
    a.run {
        run {
            println("a:${this.a} b:${this.b}")
            println(this)
        }
        "outoutoutAAA"
    }.run { println(this.length) }.run {
        val s = "是否是10:${Objects.equals(10, this)}";
        println(s);
        s
    }.run { println(this.length) }

    println("===============let================")
    a.let {
        println("a:${it.a} b:${it.b}")
        println(it)
        "outoutoutAAA"
    }.let { println(it.length) }.let {
        val s = "是否是10:${Objects.equals(10, it)}";
        println(s);
        s
    }.let { println(it.length) }

    println("===============with================")
    with(a) {
        println(this)
        println(this.a)
        println(this.b)
        this.showB()
        this
    }.showA();


    println("===============apply================")
    a.apply { println("apply1--${this.a}") }.apply { println("apply2--${this.b}") }
        .apply { println("apply3--") }

    println("===============also================")
    a.also { println("also1--${it.a}") }.also { println("also2--${it.b}");it.a }
        .also { println("also3--${it.showB()}") }


    println("===============takeif takeunless================")
    println("test takeIf")
    A().takeIf { it.a > 0 }?.showA();
    println("================")
    println("test takeUnless")
    A().takeUnless { it.b != "b" }?.showB();

    println("===============repeat================")
    repeat(10) {
        println("${it}")
    }


    class B {
        val i: Int by lazy {
            println("lazy i init")
            20
        }

        init {
            println("B构造函数执行")
        }
    }

    println("===============lazy================")
    val b = B();
    println("=================初始化b完成=================")
    //让lazy变量初始化 只需要使用其即可
    println("b 初始化...:${b.i}")
}