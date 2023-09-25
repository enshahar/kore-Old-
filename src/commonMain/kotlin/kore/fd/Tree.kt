package kore.fd

sealed class Tree<ITEM:Any> {
    companion object{
        operator fun <ITEM:Any> invoke(left: Tree<ITEM>, right: Tree<ITEM>): Tree<ITEM> = Branch(left, right)
        operator fun <ITEM:Any> invoke(value:ITEM): Tree<ITEM> = Leaf(value)
    }
    data class Leaf<ITEM:Any>(val item:ITEM): Tree<ITEM>()
    data class Branch<ITEM:Any>(val left: Tree<ITEM>, val right: Tree<ITEM>): Tree<ITEM>()
}
fun <ITEM:Any> Tree<ITEM>.setLeft(tree: Tree<ITEM>): Tree<ITEM> = when(this){
    is Tree.Leaf -> Tree.Branch(tree, this)
    is Tree.Branch -> Tree.Branch(tree, right)
}
fun <ITEM:Any> Tree<ITEM>.setRight(tree: Tree<ITEM>): Tree<ITEM> = when(this){
    is Tree.Leaf -> Tree.Branch(this, tree)
    is Tree.Branch -> Tree.Branch(left, tree)
}
val <ITEM:Any> Tree<ITEM>.size:Int get() = when(this){
    is Tree.Leaf -> 1
    is Tree.Branch -> 1 + left.size + right.size
}
val <ITEM> Tree<ITEM>.max:ITEM where ITEM:Comparable<ITEM>, ITEM:Number get() = when(this){
    is Tree.Leaf -> item
    is Tree.Branch ->{
        val lMax = left.max
        val rMax = right.max
        if(lMax > rMax) lMax else rMax
    }
}
val <ITEM:Any> Tree<ITEM>.depth:Int get() = when(this){
    is Tree.Leaf -> 1
    is Tree.Branch ->{
        val lDepth = left.depth
        val rDepth = right.depth
        1 + if(lDepth > rDepth) lDepth else rDepth
    }
}
fun <ITEM:Any, OTHER:Any> Tree<ITEM>.map(block:(ITEM)->OTHER): Tree<OTHER> = when(this){
    is Tree.Leaf -> Tree.Leaf(block(item))
    is Tree.Branch -> Tree.Branch(left.map(block), right.map(block))
}
fun <ITEM:Any, OTHER:Any> Tree<ITEM>.fold(leafBlock:(ITEM)->OTHER, branchBlock:(l:OTHER, r:OTHER)->OTHER):OTHER = when(this){
    is Tree.Leaf -> leafBlock(item)
    is Tree.Branch -> branchBlock(left.fold(leafBlock, branchBlock), right.fold(leafBlock, branchBlock))
}
inline val <ITEM:Any> Tree<ITEM>.sizeF:Int get() = fold({1}){ l, r->1 + l + r}
inline val <ITEM> Tree<ITEM>.maxF:ITEM where ITEM:Comparable<ITEM>, ITEM:Number get() = fold({it}){ l, r->if(l > r) l else r}
inline val <ITEM:Any> Tree<ITEM>.depthF:Int get() = fold({1}){ l, r->1 + if(l > r) l else r}
fun <ITEM:Any, OTHER:Any> Tree<ITEM>.mapF(block:(ITEM)->OTHER): Tree<OTHER> = fold({ Tree(block(it)) }){ l, r-> Tree(l, r) }
operator fun <ITEM:Any> Tree<ITEM>.contains(item:ITEM):Boolean = fold({it == item}){ l, r-> l || r}
