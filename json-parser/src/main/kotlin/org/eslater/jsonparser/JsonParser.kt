package org.eslater.jsonparser

class JsonParser {
    fun parse(json: String): JsonObject {
        val tokens: List<Token> = tokenize(json)
        val tokenItr = tokens.iterator()
        val keyValueMap: MutableMap<String, JsonValue> = mutableMapOf()
        var token = getNextTokenAndFailIfNotType(tokenItr, TokenType.START_OBJECT)
        while (token.type != TokenType.END_OBJECT) {
            val keyToken = getNextTokenAndFailIfNotType(tokenItr, TokenType.STRING)
            getNextTokenAndFailIfNotType(tokenItr, TokenType.COLON)
            val valueToken = getNextTokenAndFailIfNotType(tokenItr, TokenType.STRING)
            keyValueMap[keyToken.value] = JsonString(valueToken.value)
            token = tokenItr.next() //either comma or end object
        }
        return JsonObject(keyValueMap)
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
        val iterator: Iterator<Char> = text.iterator()
        val word = StringBuilder()

        fun parseBetweenQuotes(): String {
            var value = StringBuilder()
            while (iterator.hasNext()) {
                val char = iterator.next()
                if (char == '"')
                    break;
                value.append(char)
            }
            return value.toString()
        }

        var isValue = false
        while (iterator.hasNext()) {
            val char: Char = iterator.next()
            when (char) {
                '{' -> {
                    tokens.add(Token(TokenType.START_OBJECT, char.toString()))
                }
                '}' -> {
                    tokens.add(Token(TokenType.END_OBJECT, char.toString()))
                    isValue = false
                }
                ':' -> {
                    tokens.add(Token(TokenType.COLON, char.toString()))
                    isValue = true
                }
                '"' -> {
                    tokens.add(Token(TokenType.STRING, parseBetweenQuotes()))
                    if (isValue) isValue = false
                }
                ',' -> {
                    if (isValue) tokens.add(Token(TokenType.STRING, word.toString()))
                    tokens.add(Token(TokenType.COMMA, char.toString()))
                    isValue = false
                    word.clear()
                }
                else -> {
                    if (char.isWhitespace()) {
                        continue
                    } else if (isValue) {
                        word.append(char)
                    } else {
                        throw JsonParseException("unexpected character $char")
                    }
                }
            }
        }
        return tokens
    }
}
enum class TokenType {
    STRING, COLON, COMMA, START_OBJECT, END_OBJECT
}

data class Token(val type: TokenType, val value: String)

sealed class JsonValue
data class JsonObject(var value: MutableMap<String, JsonValue> = mutableMapOf())
data class JsonString(val value: String) : JsonValue()
class JsonParseException(message: String) : Exception(message)