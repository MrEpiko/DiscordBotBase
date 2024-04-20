package me.mrepiko.discordbotbase.common.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Getter
public class Config {

    private final File file;
    @Delegate private final JsonObject main;

    public Config(String path) throws IOException {
        this.file = new File(path);
        this.main = (this.file.exists()) ? new Gson().fromJson(Files.newBufferedReader(this.file.toPath()), JsonObject.class) : new JsonObject();
    }

}
