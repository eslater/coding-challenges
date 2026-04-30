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
            } else if (token.type == TokenType.KEY) {
                val value = getNextTokenAndFailIfNotType(tokenItr, TokenType.STRING_VALUE)
                keyValueMap[token.value] = JsonString(value.value)
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
        var buildingKey = false
        var buildingValue = false
        var buildingQuotedValue = false
        val word = StringBuilder()
        for (i in 0..<text.length) {
            val char = text[i]
            val token: Token? = when (char) {
                '{' -> {
                    Token(TokenType.LEFT_BRACKET, char.toString())
                }
                '}' -> {
                    if (buildingValue) {
                        buildingValue = false
                        tokens.add(Token(TokenType.STRING_VALUE, word.toString()))
                        word.clear()
                    }
                    Token(TokenType.RIGHT_BRACKET, char.toString())
                }
                ':' ->  {
                    buildingValue = !buildingValue
                    continue
                }
                ',' -> {
                    continue
                }
                '"' ->  {
                    if (buildingValue && !buildingQuotedValue) {
                        buildingValue = false
                        buildingQuotedValue = true
                        continue
                    } else if (buildingQuotedValue) {
                        buildingQuotedValue = false
                        tokens.add(Token(TokenType.STRING_VALUE, word.toString()))
                        word.clear()
                        continue
                    }
                    buildingKey = !buildingKey
                    if (!buildingKey) {
                        tokens.add(Token(TokenType.KEY, word.toString()))
                        word.clear()
                    }
                    continue
                }
                else -> {
                    if (buildingValue || buildingKey || buildingQuotedValue) {
                        word.append(char)
                    } else if (char.isWhitespace()) {
                        continue
                    } else {
                        throw JsonParseException("unexpected character $char at position $i")
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
    LEFT_BRACKET, RIGHT_BRACKET, KEY, STRING_VALUE
}

data class Token(val type: TokenType, val value: String)

sealed class JsonValue
data class JsonObject(var value: MutableMap<String, JsonValue> = mutableMapOf())
data class JsonString(val value: String) : JsonValue()
class JsonParseException(message: String) : Exception(message)