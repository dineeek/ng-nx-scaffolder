package com.ngscaffolder.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NamingUtilsTest {

    @Test
    fun `toKebabCase from PascalCase`() {
        assertEquals("user-profile", NamingUtils.toKebabCase("UserProfile"))
    }

    @Test
    fun `toKebabCase from spaces`() {
        assertEquals("user-profile", NamingUtils.toKebabCase("user profile"))
    }

    @Test
    fun `toKebabCase from already kebab`() {
        assertEquals("user-profile", NamingUtils.toKebabCase("user-profile"))
    }

    @Test
    fun `toKebabCase trims whitespace`() {
        assertEquals("user-profile", NamingUtils.toKebabCase("  UserProfile  "))
    }

    @Test
    fun `toPascalCase from kebab`() {
        assertEquals("UserProfile", NamingUtils.toPascalCase("user-profile"))
    }

    @Test
    fun `toPascalCase from spaces`() {
        assertEquals("UserProfile", NamingUtils.toPascalCase("user profile"))
    }

    @Test
    fun `toCamelCase from kebab`() {
        assertEquals("userProfile", NamingUtils.toCamelCase("user-profile"))
    }

    @Test
    fun `toCamelCase from PascalCase`() {
        assertEquals("userProfile", NamingUtils.toCamelCase("UserProfile"))
    }

    @Test
    fun `toDotCase from kebab`() {
        assertEquals("user.profile", NamingUtils.toDotCase("user-profile"))
    }

    @Test
    fun `handles single word`() {
        assertEquals("users", NamingUtils.toKebabCase("Users"))
        assertEquals("Users", NamingUtils.toPascalCase("users"))
        assertEquals("users", NamingUtils.toCamelCase("users"))
    }

    @Test
    fun `handles multi-word PascalCase`() {
        assertEquals("my-awesome-component", NamingUtils.toKebabCase("MyAwesomeComponent"))
        assertEquals("MyAwesomeComponent", NamingUtils.toPascalCase("my-awesome-component"))
    }
}
