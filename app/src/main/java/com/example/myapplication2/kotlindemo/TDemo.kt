package com.example.myapplication2.kotlindemo

/**
 * 泛型的demo
 */
class TDemo {

    /**
     * 水果类
     *
     */
    open class Fruit

    class Banana : Fruit()

    class Apple : Fruit()

    class Desk<T>(f: T) {
        var t: T = f

        fun get(): T {
            return t;
        }

        fun set(f: T) {
            this.t = f
        }
    }

    fun test1() {
        var fruitDesk: Desk<Fruit> = Desk(Fruit());
        var appleDesk: Desk<Apple> = Desk(Apple());

        // fruitDesk = appleDesk;
        // appleDesk = fruitDesk;

        var fruitDesk2: Desk<out Fruit> = Desk(Fruit());
        var fruitDesk2In: Desk<in Fruit> = Desk(Fruit());
        var appleDesk2: Desk<Apple> = Desk(Apple());

        //fruitDesk2 = appleDesk2//OK
        //appleDesk2 = fruitDesk2 //wrong

        println(fruitDesk2.get())
        //fruitDesk2.set(Apple()) //wrong

        fruitDesk2In.set(Apple())
        println(fruitDesk2In.get())//返回 Any? 无用
    }


}

fun main() {
    TDemo().test1();

}