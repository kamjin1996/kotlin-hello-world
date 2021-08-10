package com.example.myapplication2

/**
 * 集合demo
 */
class CollectionDemo {


}

fun main() {
    createTestFun();

    testColRun()
}

fun testColRun() {
    val listOf = listOf<Int>(1, 23, 3, 456, 45, 4, 34, 3, 4, 3, 3);
    //是否所有元素都大于0 all为为所有元素同样的操作
    listOf.apply { println("all:") }.all { it > 0 }.apply { println(this) }

    //是否存在任意一个大于500的数字
    listOf.apply { println("any:") }.any { it > 500 }.apply { println(this) }

    //符合条件的元素个数
    listOf.apply { println("count:") }.count { it == 3 }.apply { println(this) }

    //找到第一个符合条件的元素
    listOf.apply { println("find:") }.find { it > 100 }.apply { println(this) }

    //按照条件进行分组 大于10的为一组，小于10的为另一组
    val gt10 = listOf.apply { println("groupBy:") }.groupBy { it > 10 } //返回的为map

    gt10[true].apply { println(this) }
    println(gt10[false])

    //filter 过滤
    listOf.filter { it / 2 == 0 }.apply { println(this) }

    //map 将值进行nmap操作
    listOf.map { "str$it" }.apply { println(this) }

    //flatMap 拍平map 当面对复杂数据结构时，我们想要提炼出其中的某一层数据，并不关心其他无关字段。就适合使用 flatMap 进行扁平化提炼。
    val stuList = listOf(Stu("haha", 1), Stu("vivi", 109), Stu("nono", 29))
    stuList.flatMap { listOf(it.score) }.apply {
        var totalSorce = 0
        this.forEach {
            totalSorce += it
        }
        println("成绩总分：${totalSorce} 平均分是：${totalSorce / this.size}")
    }

    //flatten 平铺多个集合元素
    val molist = listOf(listOf(1,2,3,4,5,6), List(3) {})
    println("原来：${molist} 平铺后：${molist.flatten()}");
}

class Stu(var name: String, var score: Int) {
}

fun createTestFun() {
    val listOf = listOf<String>("a", "b", "c")
    listOf.forEach { println(it) }

    val listD = List(3) {
        "str$it"
    }

    listD.forEach {
        println(it)
    }
}
