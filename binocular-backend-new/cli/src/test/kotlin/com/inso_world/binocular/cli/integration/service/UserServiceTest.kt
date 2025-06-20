package com.inso_world.binocular.cli.integration.service

import com.inso_world.binocular.cli.service.UserService
import com.inso_world.binocular.cli.integration.service.base.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.Stream

internal class UserServiceTest(
  @Autowired private val userService: UserService,
) : BaseServiceTest() {

  private val simpleRepoEmails = listOf(
    "alice@example.com",
    "bob@example.com",
    "carol@example.com",
  )

  @Test
  fun find_all_emails_empty_database() {
    super.cleanup()

    assertThat(
      this.userService.findAllUsersByEmails(simpleRepoEmails)
    ).isEmpty()
  }

  @Test
  fun find_all_emails_empty_list() {
    assertThat(
      this.userService.findAllUsersByEmails(emptyList())
    ).isEmpty()
  }

  @Test
  fun find_all_emails_find_all() {
    val users = this.userService.findAllUsersByEmails(simpleRepoEmails)
    assertAll(
      { assertThat(users).isNotEmpty() },
      { assertThat(users).hasSize(3) },
      { assertThat(users.map { it.id }).doesNotContainNull() },
      { assertThat(users.map { it.email }).containsAll(simpleRepoEmails) },
    )
  }

  @ParameterizedTest
  @MethodSource("generatePartialMailSearchData")
  fun find_all_emails_find_partial(toFind: List<String>, count: Int) {
    val users = this.userService.findAllUsersByEmails(toFind)
    assertAll(
      { assertThat(users).isNotEmpty() },
      { assertThat(users).hasSize(count) },
      { assertThat(users.map { it.id }).doesNotContainNull() },
      { assertThat(users.map { it.email }).containsAll(toFind) },
    )
  }

  @ParameterizedTest
  @MethodSource("find_all_emails_find_nonExistingEmailsData")
  fun find_all_emails_find_nonExistingEmails(
    allToFind: List<String>,
    existing: List<String>,
    nonExisting: List<String>,
    count: Int
  ) {
    val users = this.userService.findAllUsersByEmails(allToFind)
    assertAll(
      { assertThat(users).hasSize(count) },
      { assertThat(users.map { it.id }).doesNotContainNull() },
      { assertThat(users.map { it.email }).containsAll(existing) },
      { assertThat(users.map { it.email }).doesNotContainAnyElementsOf(nonExisting) },
    )
  }

  @Test
  fun find_all_emails_find_duplicates_expect_one() {
    val users = this.userService.findAllUsersByEmails(
      listOf(
        "alice@example.com", "alice@example.com",
      )
    )
    assertAll(
      { assertThat(users).hasSize(1) },
      { assertThat(users.map { it.id }).doesNotContainNull() },
      { assertThat(users.map { it.email }).containsAll(listOf("alice@example.com")) },
    )
  }

  companion object {
    @JvmStatic
    protected fun generatePartialMailSearchData(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(listOf("alice@example.com"), 1),
        Arguments.of(listOf("bob@example.com"), 1),
        Arguments.of(listOf("carol@example.com"), 1),
        Arguments.of(listOf("alice@example.com", "carol@example.com"), 2),
        Arguments.of(listOf("alice@example.com", "bob@example.com"), 2),
        Arguments.of(listOf("bob@example.com", "carol@example.com"), 2),
      )
    }

    @JvmStatic
    protected fun find_all_emails_find_nonExistingEmailsData(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(
          listOf("nonExistent@example.com", "carol@example.com"),
          listOf("carol@example.com"),
          listOf("nonExistent@example.com"),
          1
        ),
        Arguments.of(
          listOf("nonExistent@example.com", "carol@example.com", "bob@example.com"),
          listOf("carol@example.com", "bob@example.com"),
          listOf("nonExistent@example.com"),
          2
        ),
        Arguments.of(
          listOf("nonExistent@example.com", "nonExistent2@example.com"),
          emptyList<String>(),
          listOf("nonExistent@example.com", "nonExistent2@example.com"),
          0
        ),
        Arguments.of(
          listOf("nonExistent@example.com", "alice@example.com", "bob@example.com", "carol@example.com"),
          listOf("alice@example.com", "bob@example.com", "carol@example.com"),
          listOf("nonExistent@example.com"),
          3
        ),
      )
    }
  }


}
