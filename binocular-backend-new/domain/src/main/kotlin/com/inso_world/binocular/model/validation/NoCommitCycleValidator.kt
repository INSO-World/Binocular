package com.inso_world.binocular.model.validation

import com.inso_world.binocular.model.Commit
import com.inso_world.binocular.model.Repository
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NoCommitCycleValidator : ConstraintValidator<NoCommitCycle, Repository> {
    private enum class VisitState {
        VISITING,
        VISITED,
    }

    override fun isValid(
        repo: Repository,
        context: ConstraintValidatorContext,
    ): Boolean {
        return true
        // Track visit state of each commit
        val visitStates = mutableMapOf<String, VisitState>()
        val cyclePath = mutableListOf<String>()

        fun hasCycle(commit: Commit): Boolean {
            val sha = commit.sha

            // If we find a commit we're currently visiting, we've found a cycle
            if (visitStates[sha] == VisitState.VISITING) {
                cyclePath.add(sha)
                return true
            }

            // If we've already fully explored this commit, no cycle here
            if (visitStates[sha] == VisitState.VISITED) {
                return false
            }

            // Mark this commit as being visited
            visitStates[sha] = VisitState.VISITING
            cyclePath.add(sha)

            // Check all parent commits
            for (parent in commit.parents) {
                if (hasCycle(parent)) {
                    return true
                }
            }

            // Remove from cycle path as we backtrack
            cyclePath.removeAt(cyclePath.size - 1)
            // Mark as fully visited
            visitStates[sha] = VisitState.VISITED
            return false
        }

        // Start DFS from each unvisited commit (in case of disconnected subgraphs)
        for (commit in repo.commits) {
            if (!visitStates.containsKey(commit.sha)) {
                if (hasCycle(commit)) {
                    // Found a cycle - build the error message
                    val cycleStart = cyclePath.indexOf(cyclePath.last())
                    val actualCycle = cyclePath.subList(cycleStart, cyclePath.size)

                    context.disableDefaultConstraintViolation()
                    context
                        .buildConstraintViolationWithTemplate(
                            "Commit cycle detected: ${actualCycle.joinToString(" -> ")}",
                        ).addConstraintViolation()

                    return false
                }
            }
        }

        return true
    }
}
