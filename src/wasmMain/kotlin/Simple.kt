import kore.wrap.Wrap
import kotlinx.browser.document
import kotlinx.dom.appendText

fun main() {
    val a = Wrap.ok(3)
    document.body?.appendText("Hello, ${greet()}!, ${a.invoke()}")

}

fun greet() = "world"