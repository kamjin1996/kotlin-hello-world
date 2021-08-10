package com.example.myapplication2

import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * 属性委托
 * 简言之：属性被委托后 属性虽然不受影响，但是将会执行对应的委托方法 类似于java的装饰者模式
 */
class DelegateDemo {
    var newName = "zhangsan";

    @Deprecated("User 'newName' instead", replaceWith = ReplaceWith("newName"))
    val oldName by this::newName

    fun outName(): String {
        return this.oldName;
    }
}

fun main() {

    println(DelegateDemo().outName())

    println("==================================")

    val str: String by lazy {
        println("计算！")
        "hello"
    }

    println(str)
    println(str)
    println(str)

    println("=============================")

    var str2: String by Delegates.observable("no name") { property, oldValue, newValue ->
        println("$oldValue -> $newValue")
    }

    str2 = "name"
    str2 = "age"

    println("=============================")

    var str3: String by Delegates.vetoable("no name") { property: KProperty<*>, oldValue: String, newValue: String ->
        println(property.toString())
        val canAssign: Boolean = newValue.length < 8
        if (canAssign) {
            println("新值长度大于8！无法赋值")
        } else {
            println("赋值成功")
        }
        return@vetoable canAssign
    }

    str3 = "lisi"
    str3 = "zhangsan111"

    println(str3)

    println("===================================")

    class User(map: Map<String, Any?>) {
        val name: String by map
        val age: Int by map
    }

    val user = User(
        mapOf(
            "name" to "zhangsan",
            "age" to 10,
        )
    )

    println(user.name)
    println(user.age)
}

/**
 * 资源
 */
class Resource

class Owner {
    /**
     * 资源代理
     */
    var valResource: Resource by ResourceDelegate()
}

class ResourceDelegate(private var resource: Resource = Resource()) {
    operator fun getValue(thisRef: Owner, property: KProperty<*>): Resource {
        return resource
    }

    operator fun setValue(owner: Owner, property: KProperty<*>, resource1: Resource) {
        resource = resource1
    }
}