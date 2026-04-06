package com.ngscaffolder.dialogs

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

data class PreviewEntry(val operation: String, val path: String)

class DryRunPreviewDialog(
    project: Project,
    private val entries: List<PreviewEntry>,
) : DialogWrapper(project) {

    init {
        title = "Preview: nx generate"
        setOKButtonText("Generate")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val root = DefaultMutableTreeNode("Files")
        for (entry in entries) {
            insertPath(root, entry)
        }
        val tree = Tree(DefaultTreeModel(root))
        tree.cellRenderer = PreviewTreeCellRenderer()
        tree.isRootVisible = false
        tree.showsRootHandles = true
        TreeUtil.expandAll(tree)

        return JScrollPane(tree).apply {
            preferredSize = Dimension(500, 400)
        }
    }

    private fun insertPath(root: DefaultMutableTreeNode, entry: PreviewEntry) {
        val parts = entry.path.split("/")
        var current = root
        for ((index, part) in parts.withIndex()) {
            val isLeaf = index == parts.lastIndex
            if (isLeaf) {
                current.add(DefaultMutableTreeNode(LeafNode(part, entry.operation)))
            } else {
                val existing = (0 until current.childCount)
                    .map { current.getChildAt(it) as DefaultMutableTreeNode }
                    .firstOrNull { it.userObject is String && it.userObject == part }
                if (existing != null) {
                    current = existing
                } else {
                    val dir = DefaultMutableTreeNode(part)
                    current.add(dir)
                    current = dir
                }
            }
        }
    }

    private data class LeafNode(val name: String, val operation: String)

    private class PreviewTreeCellRenderer : ColoredTreeCellRenderer() {
        override fun customizeCellRenderer(
            tree: JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean,
        ) {
            val node = (value as? DefaultMutableTreeNode)?.userObject ?: return
            when (node) {
                is LeafNode -> {
                    icon = AllIcons.FileTypes.Any_type
                    val attrs = when (node.operation) {
                        "CREATE" -> SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, java.awt.Color(98, 150, 85))
                        "UPDATE" -> SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, java.awt.Color(104, 151, 187))
                        else -> SimpleTextAttributes.REGULAR_ATTRIBUTES
                    }
                    append(node.name, attrs)
                    append("  ${node.operation}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                }
                is String -> {
                    icon = AllIcons.Nodes.Folder
                    append(node, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }
            }
        }
    }
}
