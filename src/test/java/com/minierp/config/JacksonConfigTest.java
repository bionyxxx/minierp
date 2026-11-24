package com.minierp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class JacksonConfigTest {

    record Sample(Instant instant, LocalDate date) {
    }

    private final JsonMapper mapper = buildMapper();

    @Test
    void datesAreSerializedWithLockedIsoFormat() {
        JsonNode json = mapper.readTree(mapper.writeValueAsString(
                new Sample(Instant.parse("2026-11-23T10:15:30.999999Z"), LocalDate.of(2026, 11, 23))));

        assertEquals("2026-11-23T10:15:30Z", json.get("instant").asString());
        assertEquals("2026-11-23", json.get("date").asString());
    }

    @Test
    void datesAreDeserializedFromTheLockedFormat() {
        Sample sample = mapper.readValue("{\"instant\":\"2026-11-23T10:15:30Z\",\"date\":\"2026-11-23\"}",
                Sample.class);

        assertEquals(Instant.parse("2026-11-23T10:15:30Z"), sample.instant());
        assertEquals(LocalDate.of(2026, 11, 23), sample.date());
    }

    private static JsonMapper buildMapper() {
        JsonMapper.Builder builder = JsonMapper.builder();
        new JacksonConfig().dateFormatsCustomizer().customize(builder);
        return builder.build();
    }
}
