package ru.brightos.saaocdp_rgr_visual

import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node

class BTree(val power: Int) {
    val minimumKeys = power
    val minimumChildren = power + 1
    val maximumKeys = power * 2
    val maximumChildren = maximumKeys + 1

    var root: BTreeNode? = null

    fun print(): String {
        val sb = arrayListOf<String>()
        root?.print(sb, 0)
        var resultString = ""
        sb.forEach { resultString += "$it\n" }
        return resultString
    }

    fun buildGraph() = Graph().apply {
        root!!.buildGraph(this)
    }

    fun search(key: Int) = if (root == null)
        null
    else
        root?.search(key)

    fun insert(key: Int) {
        if (root == null) {
            root = BTreeNode(power, this, true).apply {
                keys.add(key)
            }
        } else {
            root!!.insertNotFull(key)

            if (root!!.keys.size > maximumKeys)
                root = BTreeNode(power, this, false).apply {
                    children.add(root!!.apply { parentNode = this })
                    splitChild(0, root!!)
                }
        }
    }

    fun remove(key: Int): Boolean {
        val searchResult = search(key)

        if (searchResult == null)
            return false

        root?.remove(key)

        if (root!!.keys.size == 0)
            if (root!!.isLeaf)
                root = null
            else {
                root!!.children[0].parentNode = null
                root = root!!.children.first()
            }

        return true
    }
}