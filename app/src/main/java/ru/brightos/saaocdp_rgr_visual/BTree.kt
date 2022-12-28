package ru.brightos.saaocdp_rgr_visual

import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node

class BTree(order: Int) {

    private var minKeySize = order
    private var minChildrenSize = minKeySize + 1
    private var maxKeySize = 2 * minKeySize
    private var maxChildrenSize = maxKeySize + 1

    private var root: BNode? = null
    private var size = 0

    fun add(value: Int): Boolean {
        if (root == null) {
            root = BNode(null)
            root!!.addKey(value)
        } else {
            var node = root
            while (node != null) {
                if (node.childrenSize == 0) {
                    node.addKey(value)
                    if (node.keysSize <= maxKeySize)
                        break
                    split(node)
                    break
                }
                // Navigate

                // Lesser or equal
                val lesser: Int = node.keys[0]
                if (value <= lesser) {
                    node = node.children[0]
                    continue
                }

                // Greater
                val numberOfKeys = node.keysSize
                val last = numberOfKeys - 1
                val greater: Int = node.keys[last]
                if (value > greater) {
                    node = node.children[numberOfKeys]
                    continue
                }

                // Search internal nodes
                for (i in 1 until node!!.keysSize) {
                    val prev: Int = node.keys[i - 1]
                    val next: Int = node.keys[i]
                    if (value in (prev + 1)..next) {
                        node = node.children[i]
                        break
                    }
                }
            }
        }
        size++
        return true
    }

    private fun split(nodeToSplit: BNode) {
        var node = nodeToSplit
        val numberOfKeys = node.keysSize
        val medianIndex = numberOfKeys / 2
        val medianValue: Int = node.keys[medianIndex]
        val left = BNode(null)

        repeat(medianIndex) {
            left.addKey(node.keys[it])
        }

        if (node.childrenSize > 0)
            for (j in 0..medianIndex)
                left.addChild(node.children[j])

        val right = BNode(null)
        for (i in medianIndex + 1 until numberOfKeys)
            right.addKey(node.keys[i])

        if (node.childrenSize > 0)
            for (j in medianIndex + 1 until node.childrenSize)
                right.addChild(node.children[j])

        if (node.parent == null) {
            // new root, height of tree is increased
            val newRoot = BNode(null)
            newRoot.addKey(medianValue)
            node.parent = newRoot
            root = newRoot
            node = root!!
            node.addChild(left)
            node.addChild(right)
        } else {
            // Move the median value up to the parent
            val parent = node.parent
            parent!!.addKey(medianValue)
            parent.removeChild(node)
            parent.addChild(left)
            parent.addChild(right)
            if (parent.keysSize > maxKeySize) split(parent)
        }
    }

    fun remove(value: Int) = remove(value, getNode(value))

    private fun remove(value: Int, node: BNode?): Int? {
        if (node == null)
            return null

        val index = node.indexOf(value)
        val removed = node.removeKey(value)

        if (node.children.isEmpty()) {
            // Листовая страница
            if (node.parent != null && node.keysSize < minKeySize)
                combined(node)
            else if (node.parent == null && node.keysSize == 0)
                root = null
        } else {
            // Не листовая страница
            val lesser = node.children[index]
            val greatest = getGreatestNode(lesser)
            val replaceValue: Int = removeGreatestValue(greatest)!!

            node.addKey(replaceValue)

            if (greatest.parent != null && greatest.keysSize < minKeySize)
                combined(greatest)

            if (greatest.childrenSize > maxChildrenSize)
                this.split(greatest)
        }
        size--
        return removed
    }

    private fun removeGreatestValue(node: BNode?) =
        if (node!!.keysSize > 0) node.removeKeyAt(node.keysSize - 1) else null

    fun clear() {
        root = null
        size = 0
    }

    fun contains(value: Int) = getNode(value) != null

    private fun getNode(value: Int): BNode? {
        var node = root
        while (node != null) {
            val lesser: Int = node.keys[0]
            if (value < lesser) {
                node = if (node.childrenSize > 0) node.children[0] else null
                continue
            }
            val numberOfKeys = node.keysSize
            val last = numberOfKeys - 1
            val greater: Int = node.keys[last]
            if (value > greater) {
                node = if (node.childrenSize > numberOfKeys)
                    node.children[numberOfKeys] else null
                continue
            }
            for (i in 0 until numberOfKeys) {
                val currentValue: Int = node!!.keys[i]
                if (currentValue == value) {
                    return node
                }
                val next = i + 1
                if (next <= last) {
                    val nextValue: Int = node.keys[next]
                    if (value in (currentValue + 1) until nextValue) {
                        if (next < node.childrenSize) {
                            node = node.children[next]
                            break
                        }
                        return null
                    }
                }
            }
        }
        return null
    }

    private fun getGreatestNode(nodeToGet: BNode?): BNode {
        var node = nodeToGet
        while (node!!.childrenSize > 0) {
            node = node.children[node.childrenSize - 1]
        }
        return node
    }

    // Комбинируем дочерние ключи с родительскими, когда размер меньше минимального
    private fun combined(node: BNode?): Boolean {
        val parent = node!!.parent
        val index = parent!!.indexOf(node)

        val indexOfLeftNeighbor = index - 1
        val indexOfRightNeighbor = index + 1

        var rightNeighbor: BNode? = null
        var rightNeighborSize = -minChildrenSize

        if (indexOfRightNeighbor < parent.childrenSize) {
            rightNeighbor = parent.children[indexOfRightNeighbor]
            rightNeighborSize = rightNeighbor.keysSize
        }

        if (rightNeighbor != null && rightNeighborSize > minKeySize) {
            // Пытаемся выпросить ключ у правого соседа
            val removeValue: Int = rightNeighbor.keys[0]
            val prev = getIndexOfPreviousValue(parent, removeValue)
            val parentValue: Int = parent.removeKeyAt(prev)!!
            val neighborValue: Int = rightNeighbor.removeKeyAt(0)!!
            node.addKey(parentValue)
            parent.addKey(neighborValue)
            if (rightNeighbor.childrenSize > 0) {
                node.addChild(rightNeighbor.removeChild(0)!!)
            }
        } else {
            var leftNeighbor: BNode? = null
            var leftNeighborSize = -minChildrenSize

            if (indexOfLeftNeighbor >= 0) {
                leftNeighbor = parent.children[indexOfLeftNeighbor]
                leftNeighborSize = leftNeighbor.keysSize
            }

            if (leftNeighbor != null && leftNeighborSize > minKeySize) {
                // Теперь пытаемся выпросить ключ у левого соседа
                val removeValue: Int = leftNeighbor.keys[leftNeighbor.keysSize - 1]
                val prev = getIndexOfNextValue(parent, removeValue)
                val parentValue: Int = parent.removeKeyAt(prev)!!
                val neighborValue: Int = leftNeighbor.removeKeyAt(leftNeighbor.keysSize - 1)!!
                node.addKey(parentValue)
                parent.addKey(neighborValue)
                if (leftNeighbor.childrenSize > 0) {
                    node.addChild(leftNeighbor.removeChild(leftNeighbor.childrenSize - 1)!!)
                }
            } else if (parent.keysSize > 0 && (rightNeighbor != null || leftNeighbor != null)) {
                // Не можем взять элемент у соседей, так что сливаем с правым или левым соседом

                val removeValue: Int =
                    if (rightNeighbor != null)
                        rightNeighbor.keys[0]
                    else
                        leftNeighbor!!.keys[leftNeighbor.keysSize - 1]

                val operableNeighbor = rightNeighbor ?: leftNeighbor!!

                val prev =
                    getIndexOfPreviousValue(parent, removeValue)
                val parentValue: Int = parent.removeKeyAt(prev)!!

                parent.removeChild(operableNeighbor)
                node.addKey(parentValue)

                for (i in 0 until operableNeighbor.keysSize)
                    node.addKey(operableNeighbor.keys[i])

                for (i in 0 until operableNeighbor.childrenSize)
                    node.addChild(operableNeighbor.children[i])

                if (parent.parent != null && parent.keysSize < minKeySize)
                    combined(parent) // Удаление ключа сделало родителя слишком маленьким, объединяем поддеревья
                else if (parent.keysSize == 0) {
                    // Так как у родителя нет ключей, делаем эту ноду корневой
                    node.parent = null
                    root = node
                }
            }
        }
        return true
    }

    // Индекс предыдущего ключа в ноде
    private fun getIndexOfPreviousValue(node: BNode?, value: Int): Int {
        for (i in 1 until node!!.keysSize) {
            val t: Int = node.keys[i]
            if (t >= value) return i - 1
        }
        return node.keysSize - 1
    }

    // Индекс следующего ключа в ноде
    private fun getIndexOfNextValue(node: BNode?, value: Int): Int {
        for (i in 0 until node!!.keysSize) {
            val t: Int = node.keys[i]
            if (t >= value) return i
        }
        return node.keysSize - 1
    }

    fun size(): Int {
        return size
    }

    fun validate(): Boolean {
        return if (root == null) true else validateNode(root)
    }

    // Проверяем, соответствует ли наше дерево правилам Б-дерева
    private fun validateNode(node: BNode?): Boolean {
        val keySize = node!!.keysSize
        if (keySize > 1) {
            for (i in 1 until keySize) {
                val p: Int = node.keys[i - 1]
                val n: Int = node.keys[i]
                if (p > n) return false
            }
        }
        val childrenSize = node.childrenSize
        if (node.parent == null) {
            if (keySize > maxKeySize) {
                return false
            } else if (childrenSize == 0) {
                // Лист
                return true
            } else if (childrenSize < 2) {
                return false
            } else if (childrenSize > maxChildrenSize) {
                return false
            }
        } else {
            if (keySize < minKeySize) {
                return false
            } else if (keySize > maxKeySize) {
                return false
            } else if (childrenSize == 0) {
                return true
            } else if (keySize != childrenSize - 1) {
                return false
            } else if (childrenSize < minChildrenSize) {
                return false
            } else if (childrenSize > maxChildrenSize) {
                return false
            }
        }

        val first = node.children[0]
        if (first.keys[first.keysSize - 1] > node.keys[0])
            return false

        val last = node.children[node.childrenSize - 1]
        if (last.keys[0] < node.keys[node.keysSize - 1])
            return false

        for (i in 1 until node.keysSize) {
            val previous = node.keys[i - 1]
            val next = node.keys[i]
            val child = node.children[i]

            if (previous > child.keys[0] || next < child.keys[child.keysSize - 1])
                return false
        }

        for (i in 0 until node.childrenSize)
            if (!validateNode(node.children[i]))
                return false

        return true
    }

    fun buildGraph(): Graph {
        val graph = Graph()
        root?.buildGraph(graph)
        println(graph.nodeCount)
        return graph
    }

    private class BNode(var parent: BNode?) {
        val keys = arrayListOf<Int>()
        val keysSize
            get() = keys.size

        val children = arrayListOf<BNode>()
        val childrenSize
            get() = children.size

        fun getKey(index: Int) = keys[index]

        fun indexOf(value: Int): Int {
            for (i in 0 until keysSize) {
                if (keys[i] == value) return i
            }
            return -1
        }

        fun addKey(value: Int) {
            keys.add(value)
            keys.sort()
        }

        fun removeKey(value: Int): Int? {
            if (keys.isEmpty())
                return null

            return if (keys.remove(value))
                value
            else
                null
        }

        fun removeKeyAt(index: Int): Int? {
            if (index >= keys.size)
                return null

            return keys.removeAt(index)
        }

        fun indexOf(child: BNode): Int {
            for (i in 0 until childrenSize) {
                if (children[i] == child) return i
            }
            return -1
        }

        fun addChild(child: BNode): Boolean {
            child.parent = this
            children.add(child)
            return true
        }

        fun removeChild(child: BNode): Boolean {
            if (children.isEmpty())
                return false

            return children.remove(child)
        }

        fun removeChild(index: Int): BNode? {
            if (index >= children.size)
                return null

            return children.removeAt(index)
        }

        fun buildGraph(graph: Graph, parent: Node? = null) {
            val newNode = Node(keys.toString())
            println("$keys ${parent?.data}")
            if (parent != null)
                graph.addEdge(parent, newNode)
            else
                graph.addNode(newNode)

            children.forEach {
                it.buildGraph(graph, newNode)
            }
        }
    }
}