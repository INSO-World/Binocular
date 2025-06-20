package com.inso_world.binocular.cli.entity

import com.inso_world.binocular.cli.uniffi.ThreadSafeRepository
import jakarta.persistence.*

@Entity
@Table(name = "repositories")
data class Repository(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(unique = true, nullable = false)
  val name: String,

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  var commits: MutableSet<Commit> = mutableSetOf(),

  @OneToMany(fetch = FetchType.LAZY, targetEntity = User::class, cascade = [CascadeType.ALL], orphanRemoval = true)
  var user: MutableSet<User> = mutableSetOf(),

  @OneToMany(fetch = FetchType.LAZY, targetEntity = Branch::class, cascade = [CascadeType.ALL], orphanRemoval = true)
  var branches: MutableSet<Branch> = mutableSetOf(),
) {

  fun addCommit(commit: Commit) {
    this.commits.add(commit)
    commit.repository = this
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Repository

    if (id != other.id) return false
    if (name != other.name) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id?.hashCode() ?: 0
    result = 31 * result + name.hashCode()
    return result
  }

  fun addUser(e: User) {
    this.user.add(e)
    e.repository = this
  }

  fun addBranch(b: Branch) {
    this.branches.add(b)
    b.repository = this
  }
}

