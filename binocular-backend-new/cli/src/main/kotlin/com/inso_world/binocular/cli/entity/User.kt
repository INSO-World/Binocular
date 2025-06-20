package com.inso_world.binocular.cli.entity

import jakarta.persistence.*

@Entity
@Table(
  name = "users", uniqueConstraints = [
    UniqueConstraint(columnNames = ["repository_id", "email"]),
  ]
)
class User(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @Column(nullable = false)
  var name: String? = null,

  @Column(nullable = false)
  var email: String? = null,

  @OneToMany(fetch = FetchType.LAZY, targetEntity = Commit::class, cascade = [CascadeType.ALL])
  var committedCommits: MutableSet<Commit> = mutableSetOf(),

  @OneToMany(fetch = FetchType.LAZY, targetEntity = Commit::class, cascade = [CascadeType.ALL])
  var authoredCommits: MutableSet<Commit> = mutableSetOf(),

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  var repository: Repository? = null,
) {
  fun addAuthoredCommit(commit: Commit) {
    authoredCommits.add(commit)
    commit.author = this
  }

  fun addCommittedCommit(commit: Commit) {
    committedCommits.add(commit)
    commit.committer = this
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as User

    if (id != other.id) return false
    if (name != other.name) return false
    if (email != other.email) return false
    if (repository != other.repository) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id?.hashCode() ?: 0
    result = 31 * result + (name?.hashCode() ?: 0)
    result = 31 * result + (email?.hashCode() ?: 0)
    result = 31 * result + (repository?.hashCode() ?: 0)
    return result
  }
}
