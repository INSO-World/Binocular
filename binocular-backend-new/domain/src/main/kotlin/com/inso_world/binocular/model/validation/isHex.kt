package com.inso_world.binocular.model.validation

fun Char.isHex(): Boolean =
    this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
