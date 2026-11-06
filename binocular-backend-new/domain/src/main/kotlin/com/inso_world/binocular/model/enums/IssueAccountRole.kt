package com.inso_world.binocular.model.enums

/**
 * Role of an Account on an Issue connection.
 */
enum class IssueAccountRole(val value: String) {
    AUTHOR("author"),
    ASSIGNEE("assignee"),
    ASSIGNEES("assignees");

    companion object {
        fun fromValue(value: String?): IssueAccountRole? = when (value?.lowercase()) {
            AUTHOR.value -> AUTHOR
            ASSIGNEE.value -> ASSIGNEE
            ASSIGNEES.value -> ASSIGNEES
            else -> null
        }
    }
}
