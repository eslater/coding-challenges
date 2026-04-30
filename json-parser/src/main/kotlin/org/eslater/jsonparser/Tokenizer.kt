package org.eslater.jsonparser

class Tokenizer {

    fun tokenize(text: String): List<Token> {
        val iterator: ListIterator<Char> = text.toList().listIterator()
        val tokens: List<Token> = tokenizeObject(iterator)
        checkRemainingCharactersAndErrorIfNotWhiteSpace(iterator)
        return tokens
    }

    fun checkRemainingCharactersAndErrorIfNotWhiteSpace(iterator: ListIterator<Char>) {
        while (iterator.hasNext()) {
            val char = iterator.next()
            if (char.isWhitespace() || char == '\n' || char == '\r' || char == '\t') {
                continue
            }
            throw JsonParseException("unexpected trailing tokens")
        }
    }

    private fun tokenizeObject(iterator: ListIterator<Char>): List<Token> {
        val tokens = mutableListOf<Token>()
        while (iterator.hasNext()) {
            val char = iterator.next()
            when (char) {
                '{' -> {
                    tokens.add(Token(TokenType.START_OBJECT, char.toString()))
                }
                '}' -> {
                    tokens.add(Token(TokenType.END_OBJECT, char.toString()))
                    return tokens;
                }
                ':' -> {
                    tokens.add(Token(TokenType.COLON, char.toString()))
                    tokens.addAll(tokenizeValue(iterator))
                }
                '"' -> {
                    tokens.add(Token(TokenType.STRING, parseBetweenQuotes(iterator)))
                }
                ',' -> {
                    tokens.add(Token(TokenType.COMMA, char.toString()))
                }
                ' ', '\n', '\t', '\r' -> {
                    continue
                }
                else -> {
                    println("Unexpected character: $char tokens: $tokens")
                    throw JsonParseException("unexpected character $char")
                }
            }
        }
        throw JsonParseException("unexpected end of input")
    }

    private fun tokenizeArray(iterator: ListIterator<Char>): List<Token> {
        val tokens = mutableListOf<Token>()
        while (iterator.hasNext()) {
            when (val char: Char = iterator.next()) {
                '{' -> {
                    tokens.add(Token(TokenType.START_OBJECT, char.toString()))
                    tokens.addAll(tokenizeObject(iterator))
                }
                '[' -> {
                    tokens.add(Token(TokenType.START_ARRAY, char.toString()))
                    tokens.addAll(tokenizeArray(iterator))
                }
                ']' -> {
                    tokens.add(Token(TokenType.END_ARRAY, char.toString()))
                    return tokens
                }
                '"' -> {
                    tokens.add(Token(TokenType.STRING, parseBetweenQuotes(iterator)))
                }
                ',' -> {
                    tokens.add(Token(TokenType.COMMA, char.toString()))
                    tokens.addAll(tokenizeValue(iterator))
                }
                ' ', '\n', '\t', '\r' -> {
                    continue
                }
                else -> {
                    iterator.previous()
                    tokens.addAll(tokenizeValue(iterator))
                }
            }
        }
        throw JsonParseException("unexpected end of input")
    }

    private fun tokenizeValue(iterator: ListIterator<Char>): List<Token> {
        val word = StringBuilder()
        while (iterator.hasNext()) {
            when (val char: Char = iterator.next()) {
                '"' -> {
                    return listOf(Token(TokenType.STRING, parseBetweenQuotes(iterator)))
                }
                ',' -> {
                    return listOf(inferTypeAndCreateToken(word.toString()),
                        Token(TokenType.COMMA, char.toString()))
                }
                '[' -> {
                    return buildList {
                        add(Token(TokenType.START_ARRAY, char.toString()))
                        addAll(tokenizeArray(iterator))
                    }
                }
                '{' -> {
                    return buildList {
                        add(Token(TokenType.START_OBJECT, char.toString()))
                        addAll(tokenizeObject(iterator))
                    }
                }
                ']' -> {
                    iterator.previous() //we don't want to process END_ARRAY here
                    return listOf(inferTypeAndCreateToken(word.toString()))
                }
                '}' -> {
                    iterator.previous() //we don't want to process END_OBJECT here
                    return listOf(inferTypeAndCreateToken(word.toString()))
                }
                ' ', '\n', '\t', '\r' -> {
                    continue
                } else -> {
                    word.append(char)
                }
            }
        }
        throw JsonParseException("unexpected character $word")
    }

    fun parseBetweenQuotes(iterator: Iterator<Char>): String {
        var value = StringBuilder()
        while (iterator.hasNext()) {
            when(val char = iterator.next()) {
                '\\' -> {
                    if (!iterator.hasNext()) throw JsonParseException("Unexpected end of input")
                    value.append(getEscapeChar(iterator.next()))
                }
                '"' -> {
                    break
                }
                else -> {
                    value.append(char)
                }
            }
        }
        return value.toString()
    }

    fun getEscapeChar(char: Char): String {
        return when (char) {
            '\\' -> "\\"
            '\'' -> "\'"
            '"' -> "\""
            'n' -> "\n"
            't' -> "\t"
            'r' -> "\r"
            'b' -> "\b"
            else -> throw JsonParseException("Unexpected escape sequence in string value")
        }
    }

    fun inferTypeAndCreateToken(word: String): Token {
        if (word == "null") return Token(TokenType.NULL, word)
        if (word == "true" || word == "false") return Token(TokenType.BOOL, word)
        if (word.toLongOrNull() != null) return Token(TokenType.NUMBER, word)
        throw JsonParseException("value is of an unknown type")
    }

}

enum class TokenType {
    STRING, COLON, COMMA, START_OBJECT, END_OBJECT, NULL, BOOL, NUMBER, START_ARRAY, END_ARRAY
}
data class Token(val type: TokenType, val value: String)
