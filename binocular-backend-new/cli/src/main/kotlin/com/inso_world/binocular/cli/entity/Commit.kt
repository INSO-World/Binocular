// package com.inso_world.binocular.cli.entity
//
// import jakarta.persistence.CascadeType
// import jakarta.persistence.Column
// import jakarta.persistence.Entity
// import jakarta.persistence.FetchType
// import jakarta.persistence.GeneratedValue
// import jakarta.persistence.GenerationType
// import jakarta.persistence.Id
// import jakarta.persistence.Index
// import jakarta.persistence.JoinColumn
// import jakarta.persistence.JoinTable
// import jakarta.persistence.Lob
// import jakarta.persistence.ManyToMany
// import jakarta.persistence.ManyToOne
// import jakarta.persistence.Table
// import jakarta.validation.constraints.NotBlank
// import jakarta.validation.constraints.NotNull
// import jakarta.validation.constraints.PastOrPresent
// import jakarta.validation.constraints.Size
// import java.time.LocalDateTime
// import java.util.Objects
//
// @Entity
// @Table(name = "commits", indexes = [Index(name = "idx_sha", columnList = "sha")])
// data class Commit(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    val id: Long? = null,
//    @Column(unique = true, nullable = false, length = 40)
//    @field:NotBlank
//    @field:Size(min = 40, max = 40)
//    val sha: String,
//    @Lob
//    @Column(nullable = false, columnDefinition = "TEXT")
//    @field:NotBlank
//    val message: String? = null,
//    @Column(name = "commit_time", nullable = false)
//    @field:PastOrPresent
//    @field:NotNull
//    val commitTime: LocalDateTime? = null,
//    @Column(name = "author_time", nullable = true)
//    @field:PastOrPresent
//    val authorTime: LocalDateTime? = null,
//    @ManyToMany(targetEntity = Commit::class, cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
//    @JoinTable(
//        name = "commit_parents",
//        joinColumns = [JoinColumn(name = "commit_id", nullable = false)],
//        inverseJoinColumns = [JoinColumn(name = "parent_id", nullable = true)],
//    )
//    var parents: List<Commit> = emptyList(),
//    @ManyToMany(targetEntity = Branch::class, fetch = FetchType.EAGER)
//    @JoinTable(
//        name = "commit_branches",
//        joinColumns = [JoinColumn(name = "commit_id", nullable = false)],
//        inverseJoinColumns = [JoinColumn(name = "branch_id", nullable = true)],
//    )
//    val branches: MutableSet<Branch> = mutableSetOf(),
//    @ManyToOne(fetch = FetchType.LAZY)
//    var committer: User? = null,
//    @ManyToOne(fetch = FetchType.LAZY)
//    var author: User? = null,
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    var repository: Repository? = null,
// ) {
//    override fun toString(): String =
//        "Commit(id=$id, sha='$sha', message='$message', branches=$branches, commitTime=$commitTime, authorTime=$authorTime)"
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as Commit
//
//        return sha == other.sha
//    }
//
//    override fun hashCode(): Int = Objects.hash(sha)
// }
