package org.eslater.jsonparser

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonParserTest {

    @Test
    fun `handles empty object`() {
        val json = "{}"
        val result: JsonValue = JsonParser().parse(json)
        assertEquals(JsonObject(mutableMapOf()), result)
    }

    @Test
    fun `handles empty list`() {
        val json = "[]"
        val result: JsonValue = JsonParser().parse(json)
        assertEquals(JsonArray(mutableListOf()), result)
    }

    @Test
    fun `should properly parse arrays in JsonArray`() {
        val json = "{\"key\":[\"foo\", 123, null, true, false]}"
        val expected = JsonObject(mutableMapOf(
            "key" to JsonArray(mutableListOf(
                JsonString("foo"),
                JsonLong(123),
                JsonNull(),
                JsonBoolean(true),
                JsonBoolean(false)))))
        val parser = JsonParser()
        val result: JsonValue = parser.parse(json)
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("jsonDocs")
    fun `should parse and serialize the json document`(json: String, expected: JsonObject) {
        val parser = JsonParser()
        val result: JsonValue = parser.parse(json)
        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        fun jsonDocs() = listOf(
            Arguments.of("{\"key\":\"value\"}",
                JsonObject(mutableMapOf("key" to JsonString("value")))),
            Arguments.of( "{\"key\":\"\"}",
                JsonObject(mutableMapOf("key" to JsonString("")))),
            Arguments.of( "{\"key\":\"value\",\"foo\":\"bar\"}",
                JsonObject(mutableMapOf("key" to JsonString("value"), "foo" to JsonString("bar")))),
            Arguments.of("{\"key1\": true,\"key2\": false,\"key3\": null,\"key4\": \"value\",\"key5\": 101}",
                JsonObject(mutableMapOf(
                    "key1" to JsonBoolean(true),
                    "key2" to JsonBoolean(false),
                    "key3" to JsonNull(),
                    "key4" to JsonString("value"),
                    "key5" to JsonLong(101)))),
        )
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "xxx{\"key\":\"value\"}",
        "{\"key\":\"value\"}xxx",
        "{\"key\":\"value\"asdasd}",
        "{asdasd\"key\":\"value\"asdasd}",
        "{asdasd\"key\"::\"value\"asdasd}",
        "{asdasd\"key\"\"\"::\"value\"asdasd}",
        "{{\"key\"::\"value\"}",
        "{\"key\"::\"value\"}}",
        "{{\"key\"::\"value\"}}",
    ])
    fun `should throw JsonParse exception when the input is not valid json`(json: String) {
        val parser = JsonParser()
        assertThrows<JsonParseException> {
            parser.parse(json)
        }
    }

    @Test
    fun `should handle a complex object with lots of nesting`() {
        val json = """
            {
                "key1": true,
                "key2": false,
                "key3": null,
                "key4": ["value", null, ["foo", null], {"foo":"bar"}],
                "key5": {
                    "innerKey1": ["value", null, ["bar", null], true],
                    "innerKey2": "foo",
                    "innerKey3": {
                        "veryInnerKey1": "foo",
                        "veryInnerKey2": [{"foo":[true, false]}]
                    }
                }
            }
        """
        val expected = JsonObject(mutableMapOf(
            "key1" to JsonBoolean(true),
            "key2" to JsonBoolean(false),
            "key3" to JsonNull(),
            "key4" to JsonArray(mutableListOf(
                JsonString("value"),
                JsonNull(),
                JsonArray(mutableListOf(
                    JsonString("foo"),
                    JsonNull())),
                JsonObject(mutableMapOf(
                    "foo" to JsonString("bar"))))),
            "key5" to JsonObject(mutableMapOf(
                "innerKey1" to JsonArray(mutableListOf(
                    JsonString("value"),
                    JsonNull(),
                    JsonArray(mutableListOf(
                        JsonString("bar"),
                        JsonNull())),
                    JsonBoolean(true))),
                "innerKey2" to JsonString("foo"),
                "innerKey3" to JsonObject(mutableMapOf(
                    "veryInnerKey1" to JsonString("foo"),
                    "veryInnerKey2" to JsonArray(mutableListOf(
                        JsonObject(mutableMapOf(
                            "foo" to JsonArray(mutableListOf(
                                JsonBoolean(true),
                                JsonBoolean(false)))))))))))))

        val result: JsonValue = JsonParser().parse(json)
        assertEquals(expected, result)
    }

    @Test
    fun `should parse json array as the start token`() {
        val json = "[\"foo\", 123, null, true, false]"
        val expected = JsonArray(mutableListOf(
            JsonString("foo"),
            JsonLong(123),
            JsonNull(),
            JsonBoolean(true),
            JsonBoolean(false)))
        val parser = JsonParser()
        val result: JsonValue = parser.parse(json)
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "fail1.json",
        "fail2.json",
        "fail3.json",
        "fail4.json",
        "fail5.json",
        "fail6.json",
        "fail7.json",
        "fail8.json",
        "fail9.json",
        "fail10.json",
        "fail11.json",
        "fail12.json",
        "fail13.json",
        "fail14.json",
        "fail15.json",
        "fail16.json",
        "fail17.json",
//        "fail1d8.json", --this is enforcing a list depth of 20, which is not part of the json spec.
        "fail19.json",
        "fail20.json",
        "fail21.json",
        "fail22.json",
        "fail23.json",
        "fail24.json",
        "fail25.json",
        "fail26.json",
        "fail27.json",
        "fail28.json",
        "fail29.json",
        "fail30.json",
        "fail31.json",
        "fail32.json",
        "fail33.json",
        "pass1.json",
        "pass2.json",
        "pass3.json"
    ])
    fun `JSON dot org test`(fileName: String) {
        val text = File("src/test/resources/jsonorgtest/$fileName").readText()
        if (fileName.contains("pass")) {
            assertDoesNotThrow {
                JsonParser().parse(text)
            }
        } else {
            assertThrows<JsonParseException> {
                JsonParser().parse(text)
            }
        }
    }
}