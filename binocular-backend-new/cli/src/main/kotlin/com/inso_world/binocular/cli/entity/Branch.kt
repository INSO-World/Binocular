package com.inso_world.binocular.cli.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(
  name = "branches",
  indexes = [Index(name = "idx_name", columnList = "name")],
  uniqueConstraints = [
    UniqueConstraint(columnNames = ["repository_id", "name"]),
  ]
)
data class Branch(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @Column(nullable = false)
  val name: String,

  @ManyToMany(mappedBy = "branches", fetch = FetchType.LAZY, cascade = [CascadeType.REFRESH])
  var commits: MutableSet<Commit> = mutableSetOf(),

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  var repository: Repository? = null,
) {
  fun addCommit(commit: Commit) {
    this.commits.add(commit)
    commit.branches.add(this)
  }

  fun removeCommit(commit: Commit) {
    this.commits.remove(commit)
    commit.branches.remove(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Branch

    return name == other.name && repository?.name == other.repository?.name
  }

  override fun toString(): String {
    return super.toString()
  }

  override fun hashCode(): Int {
    return Objects.hash(name, repository?.name)
  }
}
