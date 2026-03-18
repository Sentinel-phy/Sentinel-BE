package com.gupex.sentinel.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

public class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .setPrettyPrinting()
            .create();

    private static final Gson GSON_COMPACT = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();

    public static String toJson(Object obj) {
        return GSON_COMPACT.toJson(obj);
    }

    public static String toPrettyJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    private static class InstantAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            String value = in.nextString();
            return value == null ? null : Instant.parse(value);
        }
    }
}
