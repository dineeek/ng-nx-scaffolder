package com.ngscaffolder.util

object NamingUtils {

    /**
     * "my component" / "MyComponent" / "my-component" → "my-component"
     */
    fun toKebabCase(input: String): String =
        input.trim()
            .replace(Regex("([a-z])([A-Z])"), "$1-$2")
            .replace(Regex("[\\s_]+"), "-")
            .lowercase()

    /**
     * "my-component" / "my component" → "MyComponent"
     */
    fun toPascalCase(input: String): String =
        toKebabCase(input)
            .split("-")
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }

    /**
     * "my-component" / "My Component" → "myComponent"
     */
    fun toCamelCase(input: String): String =
        toPascalCase(input).replaceFirstChar { it.lowercase() }

    /**
     * "my-component" → "my.component" (for selector-style dots)
     */
    fun toDotCase(input: String): String =
        toKebabCase(input).replace("-", ".")
}
