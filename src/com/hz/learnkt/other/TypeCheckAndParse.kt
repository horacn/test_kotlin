package com.hz.learnkt.other

import java.io.File

/** 类型的检查与转换“is”与“as”
 * @Author hezhao
 * @Time   2018-06-18 17:39
 * @Description 无
 * @Version V 1.0
 */

fun main(args: Array<String>) {
    // is 与 !is 操作符
    val obj = "1"
    if (obj is String) {
        print(obj.length)
    }

    if (obj !is String) { // 与 !(obj is String) 相同
        print("Not a String")
    }
    else {
        print(obj.length)
    }


    // “不安全的”转换操作符
    // 通常，如果转换是不可能的，转换操作符会抛出一个异常。因此，我们称之为不安全的。 Kotlin 中的不安全转换由中缀操作符 as（参见operator precedence）完成：
    val y = 1
    val x: String = y as String
    val x2: String? = y as String? // 可空类型

    // “安全的”（可空）转换操作符
    // 为了避免抛出异常，可以使用安全转换操作符 as?，它可以在失败时返回 null：
    val y2 = null
    val x3: String? = y as? String // 请注意，尽管事实上 as? 的右边是一个非空类型的 String，但是其转换的结果是可空的。

}

// 智能转换
// 在许多情况下，不需要在 Kotlin 中使用显式转换操作符，因为编译器跟踪不可变值的 is-检查以及显式转换，并在需要时自动插入（安全的）转换：
fun demo(x: Any) {
    if (x is String) {
        print(x.length) // x 自动转换为字符串
    }
}

// 编译器足够聪明，能够知道如果反向检查导致返回那么该转换是安全的：
fun demo2(x: Any) {
    if (x !is String) return
    print(x.length) // x 自动转换为字符串
}

// 或者在 && 和 || 的右侧：
fun demo3(x: Any) {
    // `||` 右侧的 x 自动转换为字符串
    if (x !is String || x.length == 0) return

    // `&&` 右侧的 x 自动转换为字符串
    if (x is String && x.length > 0) {
        print(x.length) // x 自动转换为字符串
    }
}

// 这些 智能转换 用于 when-表达式 和 while-循环 也一样：
fun demo4(x: Any) {
    when (x) {
        is Int -> print(x + 1)
        is String -> print(x.length + 1)
        is IntArray -> print(x.sum())
    }
}

// 请注意，当编译器不能保证变量在检查和使用之间不可改变时，智能转换不能用。 更具体地，智能转换能否适用根据以下规则：

// val 局部变量——总是可以，局部委托属性除外；
// val 属性——如果属性是 private 或 internal，或者该检查在声明属性的同一模块中执行。智能转换不适用于 open 的属性或者具有自定义 getter 的属性；
// var 局部变量——如果变量在检查和使用之间没有修改、没有在会修改它的 lambda 中捕获、并且不是局部委托属性；
// var 属性——决不可能（因为该变量可以随时被其他代码修改）。


// 类型擦除与泛型检测
// Kotlin 在编译时确保涉及泛型操作的类型安全性， 而在运行时，泛型类型的实例并无未带有关于它们实际类型参数的信息。例如， List<Foo> 会被擦除为 List<*>。通常，在运行时无法检测一个实例是否属于带有某个类型参数的泛型类型。
//
// 为此，编译器会禁止由于类型擦除而无法执行的 is 检测，例如 ints is List<Int> 或者 list is T（类型参数）。当然，你可以对一个实例检测星投影的类型：
//
// if (something is List<*>) {
//    something.forEach { println(it) } // 这些项的类型都是 `Any?`
// }
// 类似地，当已经让一个实例的类型参数（在编译期）静态检测， 就可以对涉及非泛型部分做 is 检测或者类型转换。请注意， 在这种情况下，会省略尖括号：
//
// fun handleStrings(list: List<String>) {
//     if (list is ArrayList) {
//         // `list` 会智能转换为 `ArrayList<String>`
//     }
// }
// 省略类型参数的这种语法可用于不考虑类型参数的类型转换：list as ArrayList。
//
// 带有具体化的类型参数的内联函数使其类型实参在每个调用处内联，这就能够对类型参数进行 arg is T 检测，但是如果 arg 自身是一个泛型实例，其类型参数还是会被擦除。例如：
 inline fun <reified A, reified B> Pair<*, *>.asPairOf(): Pair<A, B>? {
     if (first !is A || second !is B) return null
     return first as A to second as B
 }

val somePair: Pair<Any?, Any?> = "items" to listOf(1, 2, 3)

val stringToSomething = somePair.asPairOf<String, Any>()
val stringToInt = somePair.asPairOf<String, Int>()
val stringToList = somePair.asPairOf<String, List<*>>()
val stringToStringList = somePair.asPairOf<String, List<String>>() // 破坏类型安全！



// 非受检类型转换
// 如上所述，类型擦除使运行时不可能对泛型类型实例的类型实参进行检测，并且代码中的泛型可能相互连接不够紧密，以致于编译器无法确保类型安全。
//
// 即便如此，有时候我们有高级的程序逻辑来暗示类型安全。例如：

fun readDictionary(file: File): Map<String, *> = file.inputStream().use {
    TODO("Read a mapping of strings to arbitrary elements.")
}

// 我们已将存有一些 `Int` 的映射保存到该文件
val intsFile = File("ints.dictionary")

// Warning: Unchecked cast: `Map<String, *>` to `Map<String, Int>`
val intsDictionary: Map<String, Int> = readDictionary(intsFile) as Map<String, Int>

// 编译器会对最后一行的类型转换产生一个警告。该类型转换不能在运行时完全检测，并且不能保证映射中的值是“Int”。
//
// 为避免未受检类型转换，可以重新设计程序结构：在上例中，可以使用具有类型安全实现的不同接口 DictionaryReader<T> 与 DictionaryWriter<T>。 可以引入合理的抽象，将未受检的类型转换从调用代码移动到实现细节中。 正确使用泛型型变也有帮助。
//
// 对于泛型函数，使用具体化的类型参数可以使诸如 arg as T 这样的类型转换受检，除非 arg 对应类型的自身类型参数已被擦除。
//
// 可以通过在产生警告的语句或声明上用注解 @Suppress("UNCHECKED_CAST") 标注来禁止未受检类型转换警告：

inline fun <reified T> List<*>.asListOfType(): List<T>? =
        if (all { it is T })
            @Suppress("UNCHECKED_CAST")
            this as List<T> else
            null
// 在 JVM 平台中，数组类型（Array<Foo>）会保留关于其元素被擦除类型的信息，并且类型转换为一个数组类型可以部分受检： 元素类型的可空性与类型实参仍然会被擦除。例如， 如果 foo 是一个保存了任何 List<*>（无论可不可空）的数组的话，类型转换 foo as Array<List<String>?> 都会成功。