// package com.inso_world.binocular.cli.persistence.repository.sql
//
// import com.inso_world.binocular.cli.entity.User
// import org.springframework.data.jpa.repository.JpaRepository
// import org.springframework.stereotype.Repository
// import java.util.stream.Stream
//
// @Repository
// interface UserRepository : JpaRepository<User, String> {
//    fun findAllByEmailIn(emails: Collection<String>): Stream<User>
// }
