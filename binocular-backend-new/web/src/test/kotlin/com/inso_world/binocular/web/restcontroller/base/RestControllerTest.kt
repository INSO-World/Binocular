package com.inso_world.binocular.web.restcontroller.base

import com.inso_world.binocular.web.BaseDbTest
import com.inso_world.binocular.web.BinocularWebApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [BinocularWebApplication::class],
)
@AutoConfigureMockMvc
internal class RestControllerTest : BaseDbTest()
