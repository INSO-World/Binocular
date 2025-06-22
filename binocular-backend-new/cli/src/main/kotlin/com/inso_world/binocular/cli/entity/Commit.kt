package com.inso_world.binocular.cli.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(name = "commits", indexes = [Index(name = "idx_sha", columnList = "sha")])
data class Commit(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
  @Column(unique = true, nullable = false, length = 40)
  val sha: String,
  @Lob
  @Column(nullable = false)
  val message: String? = null,
  @Column(name = "commit_time", nullable = false)
  val commitTime: LocalDateTime? = null,
  @Column(name = "author_time", nullable = true)
  val authorTime: LocalDateTime? = null,
  @ManyToMany(targetEntity = Commit::class, cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
  @JoinTable(
    name = "commit_parents",
    joinColumns = [JoinColumn(name = "commit_id", nullable = false)],
    inverseJoinColumns = [JoinColumn(name = "parent_id", nullable = true)],
  )
  var parents: List<Commit> = emptyList(),
  @ManyToMany(targetEntity = Branch::class, fetch = FetchType.EAGER)
  @JoinTable(
    name = "commit_branches",
    joinColumns = [JoinColumn(name = "commit_id", nullable = false)],
    inverseJoinColumns = [JoinColumn(name = "branch_id", nullable = true)],
  )
  val branches: MutableSet<Branch> = mutableSetOf(),
  @ManyToOne(fetch = FetchType.LAZY)
  var committer: User? = null,
  @ManyToOne(fetch = FetchType.LAZY)
  var author: User? = null,
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  var repository: Repository? = null,
) {
  fun addBranch(branch: Branch) {
    this.branches.add(branch)
    branch.commits.add(this)
  }

  fun removeBranch(branch: Branch) {
    this.branches.remove(branch)
    branch.commits.remove(this)
  }

  override fun toString(): String =
    "Commit(id=$id, sha='$sha', message='$message', branches=$branches, commitTime=$commitTime, authorTime=$authorTime)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Commit

    return sha == other.sha
  }

  override fun hashCode(): Int = Objects.hash(sha)

//  fun setCommitter(user: User) {
//    this.committer = user
//    user.committed_commits.add(this)
//  }
//
//  fun setAuthor(user: User) {
//    this.author = user
//    user.authored_commits.add(this)
//  }
}
