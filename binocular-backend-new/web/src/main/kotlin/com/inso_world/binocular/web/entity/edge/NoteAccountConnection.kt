package com.inso_world.binocular.web.entity.edge

import com.arangodb.springframework.annotation.Edge
import com.arangodb.springframework.annotation.From
import com.arangodb.springframework.annotation.To
import com.inso_world.binocular.web.entity.Account
import com.inso_world.binocular.web.entity.Note
import org.springframework.data.annotation.Id

@Edge(value = "notes-accounts")
data class NoteAccountConnection(
  @Id var id: String? = null,
  @From var from: Note,
  @To var to: Account
)
