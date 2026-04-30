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
            val valueToken = getNextTokenAndFailIfNotValidValue(tokenItr)
            keyValueMap[keyToken.value] = getJsonValue(valueToken)
            token = tokenItr.next() //either comma or end object
        }
        return JsonObject(keyValueMap)
    }

    private fun getNextTokenAndFailIfNotValidValue(iterator: Iterator<Token>): Token {
        val validTypes = listOf(TokenType.STRING, TokenType.NUMBER, TokenType.BOOL, TokenType.NULL)
        if (!iterator.hasNext()) throw JsonParseException("Unexcepted end of input")
        val token = iterator.next();
        if (!validTypes.contains(token.type)) throw JsonParseException("unexpected next token, expected valid value type")
        return token
    }

    private fun getNextTokenAndFailIfNotType(iterator: Iterator<Token>, type: TokenType): Token {
        if (!iterator.hasNext()) throw JsonParseException("Unexcepted end of input")
        val token = iterator.next();
        if (token.type != type) throw JsonParseException("unexpected next token, expected $type")
        return token
    }

    private fun getJsonValue(token: Token): JsonValue {
        return when (token.type) {
            TokenType.STRING -> JsonString(token.value)
            TokenType.NUMBER -> JsonNumber(token.value.toLong())
            TokenType.BOOL -> JsonBoolean(if (token.value == "true") true else false)
            TokenType.NULL -> JsonNull()
            else -> throw JsonParseException("Unexpected token type for Value node: ${token.type}")
        }
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

        fun inferTypeAndCreateToken(word: String): Token {
            if (word == "null") return Token(TokenType.NULL, word)
            if (word == "true" || word == "false") return Token(TokenType.BOOL, word)
            if (word.toLongOrNull() != null) return Token(TokenType.NUMBER, word)
            throw JsonParseException("value is of an unknown type")
        }

        var isValue = false
        while (iterator.hasNext()) {
            val char: Char = iterator.next()
            when (char) {
                '{' -> {
                    tokens.add(Token(TokenType.START_OBJECT, char.toString()))
                }
                '}' -> {
                    if (isValue) tokens.add(inferTypeAndCreateToken(word.toString()))
                    isValue = false
                    word.clear()
                    tokens.add(Token(TokenType.END_OBJECT, char.toString()))
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
                    if (isValue) tokens.add(inferTypeAndCreateToken(word.toString()))
                    isValue = false
                    word.clear()
                    tokens.add(Token(TokenType.COMMA, char.toString()))
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

//Token
enum class TokenType {
    STRING, COLON, COMMA, START_OBJECT, END_OBJECT, NULL, BOOL, NUMBER
}
data class Token(val type: TokenType, val value: String)

//Json
sealed class JsonValue
data class JsonObject(var value: MutableMap<String, JsonValue> = mutableMapOf())
data class JsonString(val value: String) : JsonValue()
data class JsonNull(val value: String? = null) : JsonValue()
data class JsonBoolean(val value: Boolean) : JsonValue()
data class JsonNumber(val value: Long) : JsonValue()
class JsonParseException(message: String) : Exception(message)