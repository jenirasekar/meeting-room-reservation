package com.meetingroom.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonUtil {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                @Override
                public void write(JsonWriter out, LocalDateTime value) throws IOException {
                    if (value == null) { out.nullValue(); return; }
                    out.value(value.format(DTF));
                }
                @Override
                public LocalDateTime read(JsonReader in) throws IOException {
                    if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                        in.nextNull(); return null;
                    }
                    return LocalDateTime.parse(in.nextString(), DTF);
                }
            })
            .create();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> T fromReader(BufferedReader reader, Class<T> clazz) throws IOException {
        return gson.fromJson(reader, clazz);
    }

    public static JsonObject parseJson(String json) {
        return gson.fromJson(json, JsonObject.class);
    }

    public static String parseBody(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    public static String success(String message) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", true);
        obj.addProperty("message", message);
        return gson.toJson(obj);
    }

    public static String success(String message, Object data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", true);
        obj.addProperty("message", message);
        obj.add("data", gson.toJsonTree(data));
        return gson.toJson(obj);
    }

    public static String error(String message) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", false);
        obj.addProperty("message", message);
        return gson.toJson(obj);
    }

    public static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isEmpty()) return null;
        return LocalDateTime.parse(s, DTF);
    }
}
