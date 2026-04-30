package org.eslater.jsonparser

class JsonParser {
    fun parse(json: String): JsonObject {
        val tokens: List<Token> = Tokenizer().tokenize(json)
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
}

//Json
sealed class JsonValue
data class JsonObject(var value: MutableMap<String, JsonValue> = mutableMapOf())
data class JsonArray(var value: MutableList<JsonValue> = mutableListOf()) : JsonValue()
data class JsonString(val value: String) : JsonValue()
data class JsonNull(val value: String? = null) : JsonValue()
data class JsonBoolean(val value: Boolean) : JsonValue()
data class JsonNumber(val value: Long) : JsonValue()
class JsonParseException(message: String) : Exception(message)