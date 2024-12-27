package me.mrepiko.discordbotbase.mics;

import java.util.Optional;

public interface Identifiable {

    int getId();

    default String getIdAsString() {
        return "";
    }

    static Optional<Identifiable> getById(int id) {
        return Optional.empty();
    }

}
