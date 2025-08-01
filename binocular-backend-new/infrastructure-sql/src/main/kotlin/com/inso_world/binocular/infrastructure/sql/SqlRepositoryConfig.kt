// package com.inso_world.binocular.infrastructure.sql

// /**
// * Configuration class to enable JPA repositories for SQL entities.
// * This class is only active when the "sql" profile is active.
// */
// @EnableJpaRepositories(
//    basePackages = [
//        "com.inso_world.binocular.core.persistence",
//        "com.inso_world.binocular.cli.persistence", // TODO should be removed anytime soon™
//    ],
// )
// @EntityScan(
//    "com.inso_world.binocular.infrastructure.sql.persistence.entity",
//    "com.inso_world.binocular.cli.entity",
// )
// class SqlRepositoryConfig
