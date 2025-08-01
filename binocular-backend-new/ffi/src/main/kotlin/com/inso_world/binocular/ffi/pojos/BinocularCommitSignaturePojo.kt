package com.inso_world.binocular.ffi.pojos

import com.inso_world.binocular.internal.BinocularSig
import com.inso_world.binocular.internal.BinocularTime
import java.util.Objects

data class BinocularCommitSignaturePojo(
    var name: String,
    var email: String,
    var time: BinocularTimePojo,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinocularCommitSignaturePojo

        if (name != other.name) return false
        if (email != other.email) return false
        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Objects.hashCode(name)
        result = 31 * result + Objects.hashCode(email)
        result = 31 * result + Objects.hashCode(time)
        return result
    }

    override fun toString(): String = "BinocularCommitSignaturePojo(name='$name', email='$email', time=$time)"
}

internal fun BinocularSig.toPojo(): BinocularCommitSignaturePojo =
    BinocularCommitSignaturePojo(
        name = this.name,
        email = this.email,
        time = this.time.toPojo(),
    )

data class BinocularTimePojo(
    var seconds: Long,
    var offset: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinocularTimePojo

        if (seconds != other.seconds) return false
        if (offset != other.offset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Objects.hashCode(seconds)
        result = 31 * result + Objects.hashCode(offset)
        return result
    }

    override fun toString(): String = "BinocularTimePojo(seconds=$seconds, offset=$offset)"
}

internal fun BinocularTime.toPojo(): BinocularTimePojo = BinocularTimePojo(seconds, offset)
