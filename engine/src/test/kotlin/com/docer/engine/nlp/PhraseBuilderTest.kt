package com.docer.engine.nlp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WordSplitterTest {
    @Test fun splitsCamelCase() = assertEquals(listOf("get", "user", "id"), WordSplitter.split("getUserId"))
    @Test fun splitsAcronyms() = assertEquals(listOf("http", "server"), WordSplitter.split("HTTPServer"))
    @Test fun splitsSnakeCase() = assertEquals(listOf("max", "heap", "size"), WordSplitter.split("MAX_HEAP_SIZE"))
    @Test fun humanizes() = assertEquals("user id", WordSplitter.humanize("userId"))
}

class PluralizerTest {
    @Test fun regular() = assertEquals("users", Pluralizer.pluralize("user"))
    @Test fun sibilant() = assertEquals("boxes", Pluralizer.pluralize("box"))
    @Test fun consonantY() = assertEquals("entities", Pluralizer.pluralize("entity"))
    @Test fun irregular() = assertEquals("children", Pluralizer.pluralize("child"))
}

class PhraseBuilderTest {
    @Test fun getterBecomesReturns() =
        assertEquals("Returns the user name", PhraseBuilder.describe("getUserName"))

    @Test fun booleanBecomesPredicate() =
        assertEquals("Returns `true` if valid", PhraseBuilder.describe("isValid"))

    @Test fun collectionReturnPluralisesNoun() =
        assertEquals("Returns the users", PhraseBuilder.describe("getUser", returnTypeText = "List<User>"))

    @Test fun noVerbFallsBackToName() =
        assertEquals("User account manager", PhraseBuilder.describe("UserAccountManager"))

    @Test fun customVerbMappingOverrides() =
        assertEquals(
            "Fetches the profile",
            PhraseBuilder.describe("fetchProfile", verbMapping = mapOf("fetch" to "Fetches the {noun}")),
        )

    @Test fun extensionReceiverEmbedded() =
        assertEquals("Returns the size on the receiver [String]", PhraseBuilder.describe("getSize", receiverTypeText = "String"))

    @Test fun verbWithNoNounDropsArticle() =
        assertEquals("Resets", PhraseBuilder.describe("reset"))

    @Test fun dartCollectionTypesWork() =
        assertEquals("Returns the users", PhraseBuilder.describe("getUser", returnTypeText = "List<User>", collectionTypes = PhraseBuilder.DART_COLLECTION_TYPES))

    @Test fun rustCollectionTypesWork() =
        assertEquals("Returns the users", PhraseBuilder.describe("getUser", returnTypeText = "Vec<User>", collectionTypes = PhraseBuilder.RUST_COLLECTION_TYPES))
}
