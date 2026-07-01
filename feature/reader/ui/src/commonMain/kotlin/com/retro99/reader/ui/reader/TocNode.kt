package com.retro99.reader.ui.reader

import com.retro99.reader.ui.model.TocItemUiModel

internal class TocNode(
    val item: TocItemUiModel,
    val flatIndex: Int,
    val children: MutableList<TocNode> = mutableListOf(),
) {
    val hasChildren: Boolean get() = children.isNotEmpty()
}

internal fun List<TocItemUiModel>.toTocNodes(): List<TocNode> {
    if (isEmpty()) return emptyList()
    val nodes = mapIndexed { index, item -> TocNode(item, index) }
    val roots = mutableListOf<TocNode>()
    val stack = ArrayDeque<TocNode>()

    for (node in nodes) {
        while (stack.isNotEmpty() && stack.last().item.level >= node.item.level) {
            stack.removeLast()
        }
        if (stack.isEmpty()) {
            roots.add(node)
        } else {
            stack.last().children.add(node)
        }
        stack.addLast(node)
    }
    return roots
}

internal data class FlatTocEntry(
    val node: TocNode,
    val depth: Int,
    val isExpanded: Boolean,
)

internal fun flattenTocNodes(
    nodes: List<TocNode>,
    expandedStates: Map<Int, Boolean>,
    depth: Int = 0,
): List<FlatTocEntry> {
    val result = mutableListOf<FlatTocEntry>()
    for (node in nodes) {
        val isExpanded = expandedStates[node.flatIndex] ?: false
        result.add(FlatTocEntry(node, depth, isExpanded))
        if (node.hasChildren && isExpanded) {
            result.addAll(flattenTocNodes(node.children, expandedStates, depth + 1))
        }
    }
    return result
}

internal fun findAncestorFlatIndices(
    nodes: List<TocNode>,
    targetFlatIndex: Int,
    path: List<Int> = emptyList(),
): Set<Int> {
    for (node in nodes) {
        if (node.flatIndex == targetFlatIndex) {
            return path.toSet()
        }
        if (node.hasChildren) {
            val result = findAncestorFlatIndices(node.children, targetFlatIndex, path + node.flatIndex)
            if (result.isNotEmpty() || node.children.any { it.flatIndex == targetFlatIndex }) {
                return result
            }
        }
    }
    return emptySet()
}
