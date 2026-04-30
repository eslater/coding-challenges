package org.eslater.jsonparser

class JsonParser {

    fun parse(json: String): JsonObject {
        val tokens: List<Token> = Tokenizer().tokenize(json)
        val tokenItr = tokens.iterator()
        getNextTokenAndFailIfNotType(tokenItr, TokenType.START_OBJECT)
        val parsed: JsonObject = parseObject(tokenItr)
        if (tokenItr.hasNext()) throw JsonParseException("unexpected trailing tokens")
        return parsed
    }

    fun parseObject(iterator: Iterator<Token>): JsonObject {
        val keyValueMap: MutableMap<String, JsonValue> = mutableMapOf()
        do {
            val keyToken = getNextTokenAndFailIfNotType(iterator, TokenType.STRING)
            getNextTokenAndFailIfNotType(iterator, TokenType.COLON)
            var token = iterator.next()
            when(token.type) {
                TokenType.START_OBJECT -> {
                    keyValueMap[keyToken.value] = parseObject(iterator)
                }
                TokenType.START_ARRAY-> {
                    keyValueMap[keyToken.value] = parseArray(iterator)
                }
                else -> {
                    keyValueMap[keyToken.value] = getJsonValue(token)
                }
            }
            token = iterator.next() //either comma or end object
        } while(token.type != TokenType.END_OBJECT)
        return JsonObject(keyValueMap)
    }

    private fun parseArray(iterator: Iterator<Token>): JsonArray {
        val list = mutableListOf<JsonValue>()
        var token = iterator.next()
        while(token.type != TokenType.END_ARRAY) {
            when(token.type) {
                TokenType.START_OBJECT -> {
                    list.add(parseObject(iterator))
                }
                TokenType.START_ARRAY-> {
                    list.add(parseArray(iterator))
                }
                TokenType.COMMA -> {
                    token = iterator.next()
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

    private fun getNextTokenAndFailIfNotType(iterator: Iterator<Token>, type: TokenType): Token {
        if (!iterator.hasNext()) throw JsonParseException("Unexcepted end of input")
        val token = iterator.next();
        if (token.type != type) {
            throw JsonParseException("unexpected next token, expected $type but got ${token.type}")
        }
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
data class JsonObject(var value: MutableMap<String, JsonValue> = mutableMapOf()) : JsonValue()
data class JsonArray(var value: MutableList<JsonValue> = mutableListOf()) : JsonValue()
data class JsonString(val value: String) : JsonValue()
data class JsonNull(val value: String? = null) : JsonValue()
data class JsonBoolean(val value: Boolean) : JsonValue()
data class JsonNumber(val value: Long) : JsonValue()
class JsonParseException(message: String) : Exception(message)