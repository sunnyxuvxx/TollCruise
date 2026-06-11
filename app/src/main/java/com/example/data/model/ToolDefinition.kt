package com.example.data.model

data class Tool(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val iconName: String
)

object ToolCatalog {
    val categories = listOf("All", "Formatters", "Encoders", "Generators", "Text & Case", "Converters")

    val tools = listOf(
        Tool(
            id = "json_formatter",
            name = "JSON Formatter",
            description = "Beautify, validate, and minify JSON strings with custom indentation.",
            category = "Formatters",
            iconName = "Code"
        ),
        Tool(
            id = "html_beautifier",
            name = "HTML & XML Beautifier",
            description = "Beautify or clean up nested HTML/XML markup markup.",
            category = "Formatters",
            iconName = "Html"
        ),
        Tool(
            id = "base64",
            name = "Base64 Codec",
            description = "Encode plain text to Base64 or decode Base64 back to plain text.",
            category = "Encoders",
            iconName = "Lock"
        ),
        Tool(
            id = "url_codec",
            name = "URL Encoder/Decoder",
            description = "Safely encode special URL parameters or parse back decoded URLs.",
            category = "Encoders",
            iconName = "Link"
        ),
        Tool(
            id = "html_entities",
            name = "HTML Entities",
            description = "Convert reserved text characters to HTML entities and back.",
            category = "Encoders",
            iconName = "Tag"
        ),
        Tool(
            id = "uuid_gen",
            name = "UUID/GUID Generator",
            description = "Generate secure single or bulk v4 random UUID/GUID standards.",
            category = "Generators",
            iconName = "QrCode"
        ),
        Tool(
            id = "password_gen",
            name = "Secure Password Generator",
            description = "Generate cryptographic passwords with adjustable length and rules.",
            category = "Generators",
            iconName = "Password"
        ),
        Tool(
            id = "hash_gen",
            name = "Crypto Hash Generator",
            description = "Generate real-time MD5, SHA-1, SHA-256, and SHA-512 hashes as you type.",
            category = "Text & Case",
            iconName = "Fingerprint"
        ),
        Tool(
            id = "case_converter",
            name = "Case Converter & Stats",
            description = "Interchange word formats (UPPER, camelCase, snake_case) and read text statistics.",
            category = "Text & Case",
            iconName = "TextFormat"
        ),
        Tool(
            id = "epoch_converter",
            name = "Epoch Timestamp Converter",
            description = "Track a ticking clock and convert Unix epochs to human-readable times.",
            category = "Converters",
            iconName = "Schedule"
        ),
        Tool(
            id = "base_converter",
            name = "Number Base Converter",
            description = "Translate arbitrary numerical values between Binary, Octal, Decimal, and Hex.",
            category = "Converters",
            iconName = "Calculate"
        )
    )
}
