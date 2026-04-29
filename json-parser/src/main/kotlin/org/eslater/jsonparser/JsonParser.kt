package org.eslater.jsonparser

class JsonParser {
    fun parse(json: String): JsonObject {
        val tokens: List<Token> = tokenize(json)
        val tokenItr = tokens.iterator()
        getNextTokenAndFailIfNotType(tokenItr, TokenType.LEFT_BRACKET)
        val parsed = JsonObject()
        val keyValueMap: MutableMap<String, JsonValue> = mutableMapOf()
        while (tokenItr.hasNext()) {
            val token = tokenItr.next()
            if (token.type == TokenType.RIGHT_BRACKET) {
                if (tokenItr.hasNext())
                    throw JsonParseException("unexpected token ${token.type}")
                parsed.value = keyValueMap;
                break;
            } else if (token.type == TokenType.DOUBLE_QUOTE) {
                val key = getNextTokenAndFailIfNotType(tokenItr, TokenType.STRING)
                getNextTokenAndFailIfNotType(tokenItr, TokenType.DOUBLE_QUOTE)
                getNextTokenAndFailIfNotType(tokenItr, TokenType.COLON)
                getNextTokenAndFailIfNotType(tokenItr, TokenType.DOUBLE_QUOTE)
                val value = getNextTokenAndFailIfNotType(tokenItr, TokenType.STRING)
                getNextTokenAndFailIfNotType(tokenItr, TokenType.DOUBLE_QUOTE)
                keyValueMap.put(key.value, JsonString(value.value))
            } else {
                throw JsonParseException("unexpected next token ${token.type}")
            }
        }
        return parsed
    }

    private fun getNextTokenAndFailIfNotType(iterator: Iterator<Token>, type: TokenType): Token {
        if (!iterator.hasNext()) {
            throw JsonParseException("Unexcepted end of input")
        }
        val token = iterator.next();
        if (token.type != type) {
            throw JsonParseException("unexpected next token, expected $type")
        }
        return token
    }

    fun tokenize(text: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var consumingWord = false
        val word = StringBuilder()
        for (i in 0..<text.length) {
            val char = text[i]
            val token: Token? = when (char) {
                '{' -> {
                    Token(TokenType.LEFT_BRACKET, char.toString())
                }
                '}' -> {
                    Token(TokenType.RIGHT_BRACKET, char.toString())
                }
                ':' ->  {
                    Token(TokenType.COLON, char.toString())
                }
                '"' ->  {
                    consumingWord = !consumingWord
                    if (!consumingWord) {
                        tokens.add(Token(TokenType.STRING, word.toString()))
                        word.clear()
                    }
                    Token(TokenType.DOUBLE_QUOTE, char.toString())
                }
                else -> {
                    if (consumingWord) {
                        word.append(char)
                    } else if (char.isWhitespace()) {
                        continue
                    } else {
                        throw JsonParseException("unexpected character at position $i")
                    }
                    null
                }
            }
            if (token != null) {
                tokens.add(token)
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
data class JsonObject(var value: MutableMap<String, JsonValue> = mutableMapOf())
data class JsonString(val value: String) : JsonValue()
class JsonParseException(message: String) : Exception(message)