package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.model.Commit

internal fun Commit.traverseGraph(recorder: MutableMap<String, Any?>) {
    val visited = mutableSetOf<String>()
    val queue = ArrayDeque<Commit>()
    queue.add(this)
    
    while (queue.isNotEmpty()) {
        val current = queue.removeLast()
        
        if (visited.contains(current.sha)) {
            continue
        }
        
        visited.add(current.sha)
        recorder[current.sha] = current.sha
        
        // Add all parents to the queue
        current.parents.forEach { parent ->
            if (!visited.contains(parent.sha)) {
                queue.addLast(parent)
            }
        }
        
        // Also add all children to ensure we don't miss any commits
//        current.children.forEach { child ->
//            if (!visited.contains(child.sha)) {
//                queue.add(child)
//            }
//        }
    }
}
