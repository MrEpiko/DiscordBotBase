package me.mrepiko.discordbotbase.common;

import lombok.Getter;
import me.mrepiko.discordbotbase.common.database.DataSource;

public class Common {

    @Getter
    private static final DataSource dataSource = new DataSource();

}
