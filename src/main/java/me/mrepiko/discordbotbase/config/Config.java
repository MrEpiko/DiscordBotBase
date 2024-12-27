package me.mrepiko.discordbotbase.config;

import com.google.gson.*;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.*;

@Getter
public class Config {

    private final File file;
    private final JsonObject main;

    public Config(String dir, String fileName) {
        this(new File(dir, fileName));
    }

    public Config(File dir, String fileName) {
        this(new File(dir, fileName));
    }

    public Config(String fileName) {
        this(new File(fileName));
    }

    @SneakyThrows
    public Config(File file) {
        this.file = file;
        this.main = new Gson().fromJson(new FileReader(file), JsonObject.class);
    }

    @SneakyThrows
    public void save() {
        try (Writer out = new FileWriter(file, false)) {
            String input = main.toString();
            out.write(new GsonBuilder().setPrettyPrinting().create().toJson(new Gson().fromJson(input, JsonObject.class)));
        }
    }

    public boolean isEmpty() {
        return main.entrySet().isEmpty();
    }

    public boolean has(String key) {
        return main.has(key);
    }

    public String getString(String key, String defaultValue) {
        if (main.has(key)) return main.get(key).getAsString();
        main.addProperty(key, defaultValue);
        save();
        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        if (main.has(key)) return main.get(key).getAsInt();
        main.addProperty(key, defaultValue);
        save();
        return defaultValue;
    }

    public Double getDouble(String key, Double defaultValue) {
        if (main.has(key)) return main.get(key).getAsDouble();
        main.addProperty(key, defaultValue);
        save();
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (main.has(key)) return main.get(key).getAsBoolean();
        main.addProperty(key, defaultValue);
        save();
        return defaultValue;
    }

    public JsonObject getJsonObject(String key, JsonObject defaultValue) {
        if (main.has(key)) return main.get(key).getAsJsonObject();
        main.add(key, defaultValue);
        save();
        return defaultValue;
    }

    public JsonArray getJsonArray(String key, JsonArray defaultValue) {
        if (main.has(key)) return main.get(key).getAsJsonArray();
        main.add(key, defaultValue);
        save();
        return defaultValue;
    }

    public JsonElement get(String key, JsonElement defaultValue) {
        if (main.has(key)) return main.get(key);
        main.add(key, defaultValue);
        save();
        return defaultValue;
    }

    public String getString(String key) {
        JsonElement value = main.get(key);
        if (value == null) throw new IllegalArgumentException("Key " + key + " was not found");
        return value.getAsString();
    }

    public int getInt(String key) {
        JsonElement value = main.get(key);
        if (value == null) throw new IllegalArgumentException("Key " + key + " was not found");
        return value.getAsInt();
    }

    public Double getDouble(String key) {
        JsonElement value = main.get(key);
        if (value == null) throw new IllegalArgumentException("Key " + key + " was not found");
        return value.getAsDouble();
    }

    public boolean getBoolean(String key) {
        JsonElement value = main.get(key);
        if (value == null) throw new IllegalArgumentException("Key " + key + " was not found");
        return value.getAsBoolean();
    }

    public JsonObject getJsonObject(String key) {
        JsonElement value = main.get(key);
        if (value == null) throw new IllegalArgumentException("Key " + key + " was not found");
        return value.getAsJsonObject();
    }

    public JsonArray getJsonArray(String key) {
        JsonElement value = main.get(key);
        if (value == null) throw new IllegalArgumentException("Key " + key + " was not found");
        return (JsonArray) value;
    }

    public JsonElement get(String key) {
        JsonElement value = main.get(key);
        if (value == null) throw new IllegalArgumentException("Key " + key + " was not found");
        return value;
    }

    public void expect(String key, String defaultValue) {
        Object value = main.get(key);
        if (value == null) {
            main.addProperty(key, defaultValue);
            save();
        }
    }

    public void expect(String key, int defaultValue) {
        Object value = main.get(key);
        if (value == null) {
            main.addProperty(key, defaultValue);
            save();
        }
    }

    public void expect(String key, Double defaultValue) {
        Object value = main.get(key);
        if (value == null) {
            main.addProperty(key, defaultValue);
            save();
        }
    }

    public void expect(String key, boolean defaultValue) {
        Object value = main.get(key);
        if (value == null) {
            main.addProperty(key, defaultValue);
            save();
        }
    }

    public void expect(String key, JsonObject defaultValue) {
        Object value = main.get(key);
        if (value == null) {
            main.add(key, defaultValue);
            save();
        }
    }

    public void expect(String key, JsonArray defaultValue) {
        Object value = main.get(key);
        if (value == null) {
            main.add(key, defaultValue);
            save();
        }
    }

}
