package org.eslater.jsonparser

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonParserTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "{\"key\":\"value\"}",
        "{   \"key\":\"value\"    }",
        "   {\"key\":\"value\"}  "
    ])
    fun `should tokenize the input`(json: String) {
        var tokens: List<Token> = JsonParser().tokenize(json)
        var expected: List<Token> = listOf(
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.STRING, "value"),
            Token(TokenType.END_OBJECT, "}"))
            assertEquals(expected, tokens)
    }

    @Test
    fun `should handle escaped quotes in a string`() {
        val json = "{\"key\":\"foo\\\"bar\"}"
        var tokens: List<Token> = JsonParser().tokenize(json)
        var expected: List<Token> = listOf(
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.STRING, """foo"bar"""),
            Token(TokenType.END_OBJECT, "}"))
        assertEquals(expected, tokens)
    }

    @Test
    fun `should handle escaped quotes in a string and escaped backslashes at end`() {
        val json = """{"key":"foo\"bar\\"}"""
        var tokens: List<Token> = JsonParser().tokenize(json)
        var expected: List<Token> = listOf(
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.STRING, """foo"bar\"""),
            Token(TokenType.END_OBJECT, "}"))
        assertEquals(expected, tokens)
    }

    @ParameterizedTest
    @MethodSource("jsonDocs")
    fun `should parse and serialize the json document`(json: String, expected: JsonObject) {
        val parser = JsonParser()
        val result: JsonObject = parser.parse(json)
        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        fun jsonDocs() = listOf(
            Arguments.of("{\"key\":\"value\"}",
                JsonObject(mutableMapOf("key" to JsonString("value")))),
            Arguments.of( "{\"key\":\"\"}",
                JsonObject(mutableMapOf("key" to JsonString("")))),
            Arguments.of( "{\"key\":\"value\",\"foo\":\"bar\"}",
                JsonObject(mutableMapOf("key" to JsonString("value"), "foo" to JsonString("bar")))),
            Arguments.of("{\"key1\": true,\"key2\": false,\"key3\": null,\"key4\": \"value\",\"key5\": 101}",
                JsonObject(mutableMapOf(
                    "key1" to JsonBoolean(true),
                    "key2" to JsonBoolean(false),
                    "key3" to JsonNull(),
                    "key4" to JsonString("value"),
                    "key5" to JsonNumber(101))))
        )
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "xxx{\"key\":\"value\"}",
        "{\"key\":\"value\"}xxx",
        "{\"key\":\"value\"asdasd}",
        "{asdasd\"key\":\"value\"asdasd}",
        "{asdasd\"key\"::\"value\"asdasd}",
        "{asdasd\"key\"\"\"::\"value\"asdasd}",
        "{{\"key\"::\"value\"}",
        "{\"key\"::\"value\"}}",
        "{{\"key\"::\"value\"}}",
    ])
    fun `should throw JsonParse exception when the input is not valid json`(json: String) {
        val parser = JsonParser()
        assertThrows<JsonParseException> {
            parser.parse(json)
        }
    }

}