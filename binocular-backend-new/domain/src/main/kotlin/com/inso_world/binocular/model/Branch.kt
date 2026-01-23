package com.inso_world.binocular.model

import com.inso_world.binocular.model.vcs.ReferenceCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Branch — a named pointer within a Git [Repository].
 *
 * ### Identity & equality
 * - Technical identity: immutable [iid] of type [Id] (assigned at construction).
 * - Business key: [uniqueKey] == [Key]([repository].iid, [name]).
 * - Equality delegates to [AbstractDomainObject] (identity-based); `hashCode()` derives from [iid].
 *
 * ### Construction & validation
 * - Requires a non-blank [name] (`@field:NotBlank` + runtime `require`).
 * - On initialization the instance registers itself in `repository.branches`
 *   (idempotent, add-only set).
 *
 * ### Relationships & collections
 * - [commits]: complete history of all commits reachable from the [head] element on this [branch].
 * - [files]: add-only collection keyed by business keys of [File]; exposed as `Set` for read-only use.
 *
 * ### Thread-safety
 * - The entity is mutable and not thread-safe. Collection fields use concurrent maps internally,
 *   but multi-step workflows are **not** atomic; coordinate externally.
 *
 * @property name Branch name used for domain identity (shortened ref).
 * @property fullName Fully-qualified Git reference name (e.g., `refs/heads/main`).
 * @property category Category/type of the reference as reported by gix.
 * @property active Whether this branch is currently the active/checked-out branch.
 * @property tracksFileRenames Whether file rename tracking is enabled when analyzing history.
 * @property latestCommit Optional last known commit SHA associated with this branch.
 * @property head Last known commit SHA associated with this branch.
 * @property repository Owning repository; this branch registers itself to `repository.branches` in `init`.
 */
@OptIn(ExperimentalUuidApi::class)
class Branch(
    @field:NotBlank val name: String,
    @field:NotBlank val fullName: String,
    override val category: ReferenceCategory,
    @field:NotNull
    override val repository: Repository,
    head: Commit,
) : Reference<Branch.Key>(category, repository), Cloneable {
    @JvmInline
    value class Id(val value: Uuid)

    data class Key(val repositoryId: Repository.Id, val name: String)

    @Deprecated("Avoid using database specific id, use business key", ReplaceWith("iid"))
    var id: String? = null

    @Deprecated("old")
    var active: Boolean = false

    @Deprecated("old")
    var tracksFileRenames: Boolean = false

    @Deprecated("", ReplaceWith("head.sha"))
    val latestCommit: String
        get() = head.sha

    @Deprecated("legacy, use name property instead", replaceWith = ReplaceWith("name"))
    val branch: String = name

    var head: Commit = head
        set(@NotNull value) {
            require(value.repository == this@Branch.repository) {
                "Head is from different repository (${value.repository}) than branch (${this@Branch.repository})"
            }

            field = value
        }

    val files: Set<File> = object : NonRemovingMutableSet<File>() {}

    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(fullName.isNotBlank()) { "fullName must not be blank" }
        // workaround since `this.head = head` does not call the setter, avoid duplicate logic
        head.also { this.head = it }
        repository.branches.add(this)
    }


    /**
     * All commits reachable from this branch’s `head`, in **Git `--topo-order`**.
     *
     * ### Semantics
     * - Produces a **children-before-parents** topological order: **no commit is shown before any of its descendants**
     *   reachable from `head` (i.e., merges appear *before* their parents; `head` appears *first*).
     * - When multiple parent branches are available, the traversal **prefers more recent parents first**
     *   (by `commitDateTime`, tie-broken by `sha`) to emulate Git’s visual “non-crossing” branch flow.
     * - The returned set preserves iteration order (via `LinkedHashSet`) and is recomputed on each access.
     *
     * ### Validation
     * - Annotated with `@get:NotEmpty`: validation frameworks will reject an instance where this getter yields
     *   an empty set. Inclusion of `head` normally guarantees non-emptiness.
     *
     * ### Invariants enforced on get
     * - Result contains only commits reachable via `parents` starting at `head`.
     *
     * ### Bulk adds
     * - N/A — this is a derived snapshot; the underlying model is not mutated.
     *
     * ### Idempotency & recursion safety
     * - Uses **iterative** DFS with visited-tracking, then a **postorder-reverse** to achieve `--topo-order`
     *   without recursion or stack overflows.
     *
     * ### Exceptions
     * - None intentionally; cycles (which should not exist for commits) are tolerated by visited-tracking.
     *
     * ### Thread-safety
     * - Builds an ephemeral snapshot while iterating over mutable, add-only sets (`parents`).
     *   Concurrent mutations may yield a **weakly consistent** snapshot; coordinate externally if needed.
     *
     * ### Complexity
     * - Reachability + ordering is **O(V+E)** time and **O(V)** space over the reachable subgraph.
     */
    @get:NotEmpty
    val commits: Set<Commit>
        get() {
            // 1) Collect reachable nodes from head (iterative DFS on parents)
            val reachable = LinkedHashSet<Commit>()
            val stack = ArrayDeque<Commit>()
            stack.addLast(head)

            // prefer newer parents first to mimic git’s visual flow when branches diverge
            fun parentOrder(c: Commit): List<Commit> =
                c.parents.toList()
                    .sortedWith(compareByDescending<Commit> { it.commitDateTime }.thenBy { it.sha })

            while (stack.isNotEmpty()) {
                val c = stack.removeLast()
                if (reachable.add(c)) {
                    val parents = parentOrder(c)
                    for (i in parents.lastIndex downTo 0) {
                        val p = parents[i]
                        if (p !in reachable) stack.addLast(p)
                    }
                }
            }

            // 2) Postorder over parents, then reverse → children-before-parents (git --topo-order)
            val seen = HashSet<Commit>(reachable.size)
            val out = ArrayList<Commit>(reachable.size)
            val work = ArrayDeque<Pair<Commit, Boolean>>() // (node, expanded?)
            work.addLast(head to false)

            while (work.isNotEmpty()) {
                val (node, expanded) = work.removeLast()
                if (!expanded) {
                    if (!seen.add(node)) continue
                    work.addLast(node to true) // post-visit marker
                    val parents = parentOrder(node).filter { it in reachable }
                    for (i in parents.lastIndex downTo 0) {
                        val p = parents[i]
                        if (p !in seen) work.addLast(p to false)
                    }
                } else {
                    out.add(node) // postorder append
                }
            }

            out.reverse() // reverse postorder ⇒ children before parents (git topo-order)
            return LinkedHashSet(out)
        }

    override val uniqueKey: Key
        get() = Key(repository.iid, this.name)

    // Entities compare by immutable identity only
    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String =
        "Branch(id=$id, iid=$iid, name='$name', fullName='$fullName', category=$category, active=$active, tracksFileRenames=$tracksFileRenames, latestCommit=$latestCommit, head=${head.sha}, repositoryId=${repository.id})"
}
