package com.inso_world.binocular.cli.integration.utils

import com.inso_world.binocular.model.Commit

internal fun Commit.traverseGraph(recorder: MutableMap<String, Any?>) {
    recorder.computeIfAbsent(this.sha) {
        this.sha
    }
    this.parents.forEach { p ->
        if (!recorder.contains(p.sha)) {
            p.traverseGraph(recorder)
            recorder.computeIfAbsent(p.sha) { p.sha }
        }
    }
}
