package org.eslater.jsonparser

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

class JsonParserTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "{\"key\":\"value\"}",
        "{   \"key\":\"value\"    }",
        "   {\"key\":\"value\"}  ",
    ])
    fun `should tokenize the input`(json: String) {
        var tokens: List<Token> = JsonParser().tokenize(json)
        var expected: List<Token> = listOf(
            Token(TokenType.LEFT_BRACKET, "{"),
            Token(TokenType.DOUBLE_QUOTE, "\""),
            Token(TokenType.STRING, "key"),
            Token(TokenType.DOUBLE_QUOTE, "\""),
            Token(TokenType.COLON, ":"),
            Token(TokenType.DOUBLE_QUOTE, "\""),
            Token(TokenType.STRING, "value"),
            Token(TokenType.DOUBLE_QUOTE, "\""),
            Token(TokenType.RIGHT_BRACKET, "}"))
            assertEquals(expected, tokens)
    }

    @ParameterizedTest
    @MethodSource("jsonDocs")
    fun `should parse and serialize the json document`(json: String, expected: JsonObject) {
        val parser = JsonParser()
        val result: JsonObject = parser.parse(json)
        assertEquals(result, expected)
    }

    companion object {
        @JvmStatic
        fun jsonDocs() = listOf(
            Arguments.of("{\"key\":\"value\"}",
                JsonObject(mutableMapOf("key" to JsonString("value")))),
            Arguments.of( "{\"key\":\"\"}",
                JsonObject(mutableMapOf("key" to JsonString("")))),
            Arguments.of( "{\"key\":\"value\",\"foo\":\"bar\"}",
                JsonObject(mutableMapOf("key" to JsonString("value"), "foo" to JsonString("bar"))))
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