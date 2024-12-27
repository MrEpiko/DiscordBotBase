package me.mrepiko.discordbotbase.mics;

import me.mrepiko.discordbotbase.config.Config;

public interface Configurable {

    Config getConfig();
    void loadConfig();

}
