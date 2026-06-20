package com.kdocer.aspect

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AspectRegistryTest {

    @Test fun idsAreUnique() {
        val ids = AspectEngine.ASPECTS.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Aspect ids must be unique: $ids")
    }

    @Test fun everyAspectHasNonBlankDefaults() {
        AspectEngine.ASPECTS.forEach {
            assertTrue(it.id.isNotBlank(), "aspect id must not be blank")
            assertTrue(it.defaultNote.isNotBlank(), "aspect ${it.id} must have a default note")
        }
    }
}
