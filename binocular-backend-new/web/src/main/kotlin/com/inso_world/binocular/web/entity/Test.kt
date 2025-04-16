package com.inso_world.binocular.web.entity

import com.arangodb.springframework.annotation.Document
import org.springframework.data.annotation.Id

@Document(collection = "test")
data class Test(
  @Id var id: String? = null,
)
