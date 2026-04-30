package org.eslater.jsonparser

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenizerTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "{\"key\":\"value\"}",
        "{   \"key\":\"value\"    }",
        "   {\"key\":\"value\"}  "
    ])
    fun `should tokenize the input`(json: String) {
        val tokens: List<Token> = Tokenizer().tokenize(json)
        val expected: List<Token> = listOf(
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.STRING, "value"),
            Token(TokenType.END_OBJECT, "}"))
        assertEquals(expected, tokens)
    }

    @Test
    fun `should handle array as start token`() {
        val json = "[\"foo\",\"bar\", true, 123]"
        var tokens: List<Token> = Tokenizer().tokenize(json)
        var expected: List<Token> = listOf(
            Token(TokenType.START_ARRAY, "["),
            Token(TokenType.STRING, "foo"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.STRING, "bar"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.BOOL, "true"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.NUMBER, "123"),
            Token(TokenType.END_ARRAY, "]"))
        assertEquals(expected, tokens)
    }

    @Test
    fun `should handle escaped quotes in a string`() {
        val json = "{\"key\":\"foo\\\"bar\"}"
        var tokens: List<Token> = Tokenizer().tokenize(json)
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
        val tokens: List<Token> = Tokenizer().tokenize(json)
        val expected: List<Token> = listOf(
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.STRING, """foo"bar\"""),
            Token(TokenType.END_OBJECT, "}"))
        assertEquals(expected, tokens)
    }

    @Test
    fun `should properly tokenize an array`() {
        val json = "{\"key\":[\"foo\", 123, null, true, false]}"
        val tokens: List<Token> = Tokenizer().tokenize(json)
        val expected: List<Token> = listOf(
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.START_ARRAY, "["),
            Token(TokenType.STRING, "foo"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.NUMBER, "123"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.NULL, "null"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.BOOL, "true"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.BOOL, "false"),
            Token(TokenType.END_ARRAY, "]"),
            Token(TokenType.END_OBJECT, "}"))
        assertEquals(expected, tokens)
    }

    @Test
    fun `should properly tokenize a nested array`() {
        val json = "{\"key\":[\"foo\", true, [123, null], false]}"
        val tokens: List<Token> = Tokenizer().tokenize(json)
        val expected: List<Token> = listOf(
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.START_ARRAY, "["),
            Token(TokenType.STRING, "foo"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.BOOL, "true"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.START_ARRAY, "["),
            Token(TokenType.NUMBER, "123"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.NULL, "null"),
            Token(TokenType.END_ARRAY, "]"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.BOOL, "false"),
            Token(TokenType.END_ARRAY, "]"),
            Token(TokenType.END_OBJECT, "}"))
        assertEquals(expected, tokens)
    }

    @Test
    fun `should properly a nested object`() {
        val json = "{\"key\":{\"key1\":\"foo\",\"key2\":123,\"key3\":null,\"key4\":true,\"key5\":[false,\"foo\",null, 123]}}"
        val tokens: List<Token> = Tokenizer().tokenize(json)
        val expected: List<Token> = listOf(
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key1"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.STRING, "foo"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.STRING, "key2"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.NUMBER, "123"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.STRING, "key3"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.NULL, "null"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.STRING, "key4"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.BOOL, "true"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.STRING, "key5"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.START_ARRAY, "["),
            Token(TokenType.BOOL, "false"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.STRING, "foo"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.NULL, "null"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.NUMBER, "123"),
            Token(TokenType.END_ARRAY, "]"),
            Token(TokenType.END_OBJECT, "}"),
            Token(TokenType.END_OBJECT, "}"))
        assertEquals(expected, tokens)
    }

    @Test
    fun `should handle newlines, carriage returns, and tabs`() {
        val json = "\n{\n\"key\"\n:\r\"value\"\t\n}\n"
        val tokens: List<Token> = Tokenizer().tokenize(json)
        val expected: List<Token> = listOf(
            Token(TokenType.START_OBJECT, "{"),
            Token(TokenType.STRING, "key"),
            Token(TokenType.COLON, ":"),
            Token(TokenType.STRING, "value"),
            Token(TokenType.END_OBJECT, "}"))
        assertEquals(expected, tokens)
    }
}