import kore.r.R
import kotlinx.browser.document
import kotlinx.dom.appendText

fun main() {
    val a = R.ok(3)
    document.body?.appendText("Hello, ${greet()}!, ${a.invoke()}")

}

fun greet() = "world"