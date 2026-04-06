package com.ngscaffolder.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ValidationTest {

    private val kebabCaseRegex = Regex("^[a-z][a-z0-9-]*$")

    @Test
    fun `valid kebab-case names`() {
        listOf("my-feature", "users", "data-access", "a", "my-lib-123", "a1b2c3").forEach {
            assertTrue(it.matches(kebabCaseRegex), "Expected '$it' to be valid")
        }
    }

    @Test
    fun `rejects empty string`() {
        assertFalse("".matches(kebabCaseRegex))
    }

    @Test
    fun `rejects uppercase`() {
        assertFalse("MyFeature".matches(kebabCaseRegex))
        assertFalse("myFeature".matches(kebabCaseRegex))
        assertFalse("MY-FEATURE".matches(kebabCaseRegex))
    }

    @Test
    fun `rejects spaces`() {
        assertFalse("my feature".matches(kebabCaseRegex))
    }

    @Test
    fun `rejects starting with number`() {
        assertFalse("1my-feature".matches(kebabCaseRegex))
    }

    @Test
    fun `rejects starting with hyphen`() {
        assertFalse("-my-feature".matches(kebabCaseRegex))
    }

    @Test
    fun `rejects underscores`() {
        assertFalse("my_feature".matches(kebabCaseRegex))
    }

    @Test
    fun `rejects special characters`() {
        assertFalse("my.feature".matches(kebabCaseRegex))
        assertFalse("my@feature".matches(kebabCaseRegex))
    }
}
