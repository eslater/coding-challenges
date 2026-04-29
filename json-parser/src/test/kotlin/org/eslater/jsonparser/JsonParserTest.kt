package org.eslater.jsonparser

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonParserTest {

    @Test
    fun `should tokenize the input`() {
        var json = "{\"key\":\"value\"}"
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
}