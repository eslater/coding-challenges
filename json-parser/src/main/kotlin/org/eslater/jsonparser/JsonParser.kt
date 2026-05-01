package org.eslater.jsonparser

class JsonParser {

    companion object {
        val START_TOKENS = listOf(TokenType.START_ARRAY, TokenType.START_OBJECT)
        val END_TOKENS = listOf(TokenType.END_ARRAY, TokenType.END_OBJECT)
        val VALUE_TOKENS = listOf(TokenType.STRING, TokenType.BOOL, TokenType.NULL, TokenType.LONG, TokenType.DOUBLE)
    }

    fun parse(json: String): JsonValue {
        if (json.isBlank()) throw JsonParseException("empty content")
        val tokens: List<Token> = Tokenizer().tokenize(json)
        val tokenItr = tokens.iterator()
        val startToken = getNextTokenAndFailIfNotType(tokenItr, START_TOKENS)
        val parsed = when (startToken.type) {
            TokenType.START_OBJECT -> parseObject(tokenItr)
            TokenType.START_ARRAY-> parseArray(tokenItr)
            else -> throw JsonParseException("Invalid start token type: $startToken")
        }
        if (tokenItr.hasNext()) throw JsonParseException("unexpected trailing tokens")
        return parsed
    }

    private fun parseObject(iterator: Iterator<Token>): JsonObject {
        val keyValueMap: MutableMap<String, JsonValue> = mutableMapOf()
        var keyToken = getNextTokenAndFailIfNotType(iterator, listOf(TokenType.STRING, TokenType.END_OBJECT))
        while(keyToken.type != TokenType.END_OBJECT) {
            getNextTokenAndFailIfNotType(iterator, TokenType.COLON)
            val valueToken = iterator.next()
            keyValueMap[keyToken.value] = when(valueToken.type) {
                TokenType.START_OBJECT -> parseObject(iterator)
                TokenType.START_ARRAY-> parseArray(iterator)
                else -> getJsonValue(valueToken)
            }
            val separator = getNextTokenAndFailIfNotType(iterator, listOf(TokenType.COMMA, TokenType.END_OBJECT))
            if (separator.type == TokenType.END_OBJECT) break
            keyToken = getNextTokenAndFailIfNotType(iterator, TokenType.STRING)
        }
        return JsonObject(keyValueMap)
    }

    private fun parseArray(iterator: Iterator<Token>): JsonArray {
        val list = mutableListOf<JsonValue>()
        var token = getNextTokenAndFailIfNotType(iterator, VALUE_TOKENS + START_TOKENS + END_TOKENS)
        while(token.type != TokenType.END_ARRAY) {
            when(token.type) {
                TokenType.START_OBJECT -> {
                    list.add(parseObject(iterator))
                }
                TokenType.START_ARRAY-> {
                    list.add(parseArray(iterator))
                }
                TokenType.COMMA -> {
                    token = getNextTokenAndFailIfComma(iterator)
                    continue
                }
                else -> {
                    list.add(getJsonValue(token))
                }
            }
            token = iterator.next() //either comma or end array
        }
        return JsonArray(list)
    }

    private fun getNextTokenAndFailIfComma(iterator: Iterator<Token>): Token {
        if (!iterator.hasNext()) throw JsonParseException("Unexpected end of input")
        val token = iterator.next()
        if (token.type == TokenType.COMMA) {
            throw JsonParseException("unexpected comma in array")
        }
        return token
    }

    private fun getNextTokenAndFailIfNotType(iterator: Iterator<Token>, type: TokenType): Token {
        return getNextTokenAndFailIfNotType(iterator, listOf(type))
    }

    private fun getNextTokenAndFailIfNotType(iterator: Iterator<Token>, types: List<TokenType>): Token {
        if (!iterator.hasNext()) throw JsonParseException("Unexpected end of input")
        val token = iterator.next()
        if (!types.contains(token.type)) {
            throw JsonParseException("unexpected next token, expected $types but got ${token.type}")
        }
        return token
    }

    private fun getJsonValue(token: Token): JsonValue {
        return when (token.type) {
            TokenType.STRING -> JsonString(token.value)
            TokenType.LONG -> JsonLong(token.value.toLong())
            TokenType.DOUBLE -> JsonDouble(token.value.toDouble())
            TokenType.BOOL -> JsonBoolean(if (token.value == "true") true else false)
            TokenType.NULL -> JsonNull
            else -> throw JsonParseException("Unexpected token type for Value node: ${token.type}")
        }
    }
}

//Json
sealed class JsonValue
sealed class JsonNumber: JsonValue()
data class JsonObject(val value: Map<String, JsonValue> = mapOf()) : JsonValue()
data class JsonArray(val value: List<JsonValue> = listOf()) : JsonValue()
data class JsonString(val value: String) : JsonValue()
data class JsonBoolean(val value: Boolean) : JsonValue()
data class JsonLong(val value: Long): JsonNumber()
data class JsonDouble(val value: Double): JsonNumber()
object JsonNull: JsonValue()
class JsonParseException(message: String) : Exception(message)