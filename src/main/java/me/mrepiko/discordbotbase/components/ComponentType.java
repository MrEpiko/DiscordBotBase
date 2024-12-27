package me.mrepiko.discordbotbase.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mrepiko.discordbotbase.mics.Identifiable;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum ComponentType implements Identifiable {
    BUTTON(1),
    DROPDOWN(2),
    MODAL(3),

    UNKNOWN(-1);

    private final int id;

    public static Optional<ComponentType> getById(int id) {
        return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst();
    }

    public String getIdAsString() {
        return String.valueOf(id);
    }

}
