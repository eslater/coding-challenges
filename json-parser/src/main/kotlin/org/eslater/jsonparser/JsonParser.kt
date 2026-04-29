package org.eslater.jsonparser

class JsonParser {
    fun parse(json: String) {
        val tokens: List<Token> = tokenize(json)
    }

    fun tokenize(text: String): List<Token> {
        val tokens = mutableListOf<Token>()

        val word = StringBuilder()
        for (char in text) {
            val token = when (char) {
                '{' -> Token(TokenType.LEFT_BRACKET, char.toString())
                '}' -> Token(TokenType.RIGHT_BRACKET, char.toString())
                ':' -> Token(TokenType.COLON, char.toString())
                '"' -> Token(TokenType.DOUBLE_QUOTE, char.toString())
                else -> null
            }
            if (token != null) {
                if (!word.isEmpty()) {
                    tokens.add(Token(TokenType.STRING, word.toString()))
                    word.clear()
                }
                tokens.add(token)
            } else {
                word.append(char)
            }
        }
        return tokens
    }
}

enum class TokenType {
    LEFT_BRACKET, RIGHT_BRACKET, COLON, DOUBLE_QUOTE, STRING
}

data class Token(val type: TokenType, val value: String)

sealed class JsonValue
data class JsonObject(val value: Map<String, JsonValue>)
data class JsonString(val value: String) : JsonValue()