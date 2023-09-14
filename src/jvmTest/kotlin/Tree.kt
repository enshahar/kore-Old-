sealed class Tree<ITEM:Any> {
    companion object{
        fun <ITEM:Any> of(left:Tree<ITEM>, right:Tree<ITEM>):Tree<ITEM> = Branch(left, right)
        fun <ITEM:Any> of(left:ITEM, right:ITEM):Tree<ITEM> = Branch(Leaf(left), Leaf(right))
        fun <ITEM:Any> of(value:ITEM):Tree<ITEM> = Leaf(value)
    }
    data class Leaf<ITEM:Any>(val item:ITEM):Tree<ITEM>()
    data class Branch<ITEM:Any>(val left:Tree<ITEM>, val right:Tree<ITEM>):Tree<ITEM>()
}
fun <ITEM:Any> Tree<ITEM>.setLeft(tree: Tree<ITEM>):Tree<ITEM> = when(this){
    is Tree.Leaf -> Tree.Branch(tree, this)
    is Tree.Branch -> Tree.Branch(tree, right)
}
fun <ITEM:Any> Tree<ITEM>.setRight(tree: Tree<ITEM>):Tree<ITEM> = when(this){
    is Tree.Leaf -> Tree.Branch(this, tree)
    is Tree.Branch -> Tree.Branch(left, tree)
}
fun <ITEM:Any> Tree<ITEM>.size2():Int = when(this){
    is Tree.Leaf -> 1
    is Tree.Branch -> left.size2() + right.size2()
}
inline fun Tree<Int>.size():Int = fold(0){acc, it-> acc + 1}
fun <ITEM:Any, ACC:Any> Tree<ITEM>.fold(base:ACC, block:(ACC, ITEM)->ACC):ACC = when(this){
    is Tree.Leaf -> block(base, item)
    is Tree.Branch -> right.fold(left.fold(base, block), block)
}
inline fun Tree<Int>.max():Int = fold(Int.MIN_VALUE){acc, it-> if(acc < it) it else acc}

fun <ITEM:Any> Tree<ITEM>.depth(acc:Int = 0):Int = when(this){
    is Tree.Leaf -> acc + 1
    is Tree.Branch ->{
        val lDepth = left.depth(acc + 1)
        val rDepth = right.depth(acc + 1)
        if(lDepth > rDepth) lDepth else rDepth
    }
}