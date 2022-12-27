package ru.brightos.saaocdp_rgr_visual

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import dev.bandb.graphview.layouts.tree.TreeEdgeDecoration
import ru.brightos.saaocdp_rgr_visual.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val bTree = BTree(2)
    lateinit var binding: ActivityMainBinding
    lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.delete.setOnClickListener {
            if (bTree.remove(
                Integer.parseInt(binding.input.text.toString())
            )) {
                binding.result.text = "Элемент ${binding.input.text} был удалён."
                binding.output.text = bTree.print()
//                setupGraphView()
            }
        }

        binding.insert.setOnClickListener {
            bTree.insert(
                Integer.parseInt(binding.input.text.toString())
            )
            binding.result.text = "Элемент ${binding.input.text} был помещён."
            binding.output.text = bTree.print()
//            setupGraphView()
        }

        binding.search.setOnClickListener {
            if (bTree.search(
                Integer.parseInt(binding.input.text.toString())
            ) != null)
                binding.result.text = "Элемент ${binding.input.text} был найден."
            else
                binding.result.text = "Элемент ${binding.input.text} не был найден."
        }

        binding.result.text = "Программа готова к работе."
    }

//    private fun setupGraphView() {
//        val recycler = binding.recycler
//
//        val configuration = BuchheimWalkerConfiguration.Builder()
//            .setSiblingSeparation(100)
//            .setLevelSeparation(100)
//            .setSubtreeSeparation(100)
//            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
//            .build()
//        recycler.layoutManager = BuchheimWalkerLayoutManager(this, configuration)
//
//        recycler.addItemDecoration(TreeEdgeDecoration())
//        adapter = object : AbstractGraphAdapter<NodeViewHolder>() {
//
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.layout_node, parent, false)
//                return NodeViewHolder(view)
//            }
//
//            override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
//                holder.textView.text = getNodeData(position).toString()
//            }
//        }.apply {
//            submitGraph(bTree.buildGraph())
//            recycler.adapter = this
//        }
//    }
}

class NodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView = itemView.findViewById<TextView>(R.id.node_text)
}