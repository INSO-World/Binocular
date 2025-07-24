package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Commit
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NoCommitCycleValidator : ConstraintValidator<NoCommitCycle, Commit> {
    override fun isValid(
        commit: Commit?,
        context: ConstraintValidatorContext,
    ): Boolean {
//        val graph = DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge::class.java)
//        if (commit == null) return true
//        hasCycleBySha(commit, mutableListOf(), context, graph)
//        println(graph.ac)
        return true
    }

    private fun hasCycleBySha(
        commit: Commit,
        path: MutableList<String>,
        context: ConstraintValidatorContext,
    ): Boolean {
//        val sha = commit.sha
//        val index = path.indexOf(sha)
//        if (index != -1) {
//            // Cycle detected: extract the cycle path
//            val cyclePath = path.subList(index, path.size) + sha
//            context.disableDefaultConstraintViolation()
//            context
//                .buildConstraintViolationWithTemplate(
//                    "Commit cycle detected: " + cyclePath.joinToString(" -> "),
//                ).addConstraintViolation()
//            return true
//        }
//        path.add(sha)
//        path.removeAt(path.size - 1)
        return false
    }
}
