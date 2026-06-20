package com.kdocer.template

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TemplateEngineTest {
    @Test fun substitutesTokens() =
        assertEquals("* @param id the user id", TemplateEngine.render("* @param {name} the {noun}", mapOf("name" to "id", "noun" to "user id")))

    @Test fun dropsTrailingArticleWhenNounEmpty() =
        assertEquals("* @param id", TemplateEngine.render("* @param {name} the {noun}", mapOf("name" to "id", "noun" to "")))

    @Test fun unknownTokenRendersEmpty() =
        assertEquals("* @return", TemplateEngine.render("* @return {description}", mapOf()))
}
