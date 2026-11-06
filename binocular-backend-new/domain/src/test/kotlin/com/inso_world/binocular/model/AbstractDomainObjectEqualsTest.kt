import com.inso_world.binocular.model.AbstractDomainObject
import com.inso_world.binocular.model.NullKeyAdo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll


// Simple concrete Kotlin subclass for normal cases
private class KObj(
    iid: Int,
    override val uniqueKey: String
) : AbstractDomainObject<Int, String>(iid) {
    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}

internal class AbstractDomainObjectEqualsTest {

    @Test
    fun `equals false when same class & same iid but different uniqueKey (covers uniqueKey branch)`() {
        val a = KObj(iid = 1, uniqueKey = "A")
        val b = KObj(iid = 1, uniqueKey = "B")

        // Hits: javaClass check passes -> cast executes -> iid equal -> uniqueKey differs -> return false
        assertAll(
            { assertFalse(a == b) },
            { assertFalse(b == a) }
        )
    }

    @Test
    fun `equals true when same class & same iid & same uniqueKey`() {
        val a = KObj(iid = 42, uniqueKey = "X")
        val b = KObj(iid = 42, uniqueKey = "X")

        // Hits: javaClass check passes -> cast executes -> iid equal -> uniqueKey equal -> return true
        assertAll(
            { assertTrue(a == b) },
            { assertTrue(b == a) }
        )
    }

    @Test
    fun `equals false when same class but different iid`() {
        val a = KObj(iid = 1, uniqueKey = "A")
        val b = KObj(iid = 2, uniqueKey = "A")

        // Hits: javaClass check passes -> cast executes -> iid differs -> return false (doesn't reach uniqueKey)
        assertAll(
            { assertFalse(a == b) },
            { assertFalse(b == a) }
        )
    }

    @Test
    fun `equals short-circuits to false on different runtime class`() {
        val a = KObj(iid = 1, uniqueKey = "A")
        val other = "not an AbstractDomainObject"

        // Hits: javaClass != other?.javaClass -> return false (cast not executed)
        assertFalse(a.equals(other))
    }

    @Test
    fun `equals triggers Kotlin null-assertion on other_uniqueKey (kills removed Intrinsics_checkNotNull)`() {
        val a = NullKeyAdo(1, "A")     // non-null uniqueKey
        val b = NullKeyAdo(1, null)    // null uniqueKey to trip Kotlin callsite null-check

        // Path: same concrete class -> cast executes -> iid equal -> accessing other.uniqueKey
        // Kotlin inserts Intrinsics.checkNotNull on the property read; expect NPE.
        assertAll(
            { assertFalse(a == b) },
            { assertFalse(b == a) }
        )
    }
}

