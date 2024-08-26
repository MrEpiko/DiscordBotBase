package me.mrepiko.discordbotbase.components.general;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;

@Getter
public class BasicComponentHandler extends ComponentHandler {

    private int timeout;
    private int deleteAfterTimeout;
    private int rowIndex;
    private boolean disableOnceUsed;
    private boolean disableAllOnceUsed;
    @Setter(AccessLevel.PROTECTED) private ItemComponent itemComponent;

    public BasicComponentHandler(String name) {
        super(name);
        setupProperties();
    }

    @Override
    protected void setupProperties() {
        super.setupProperties();
        JsonObject properties = getProperties();
        if (properties == null) return;
        disableOnceUsed = properties.has("disable_once_used") && properties.get("disable_once_used").getAsBoolean();
        disableAllOnceUsed = properties.has("disable_all_once_used") && properties.get("disable_all_once_used").getAsBoolean();
        rowIndex = (properties.has("row_index")) ? properties.get("row_index").getAsInt() : 0;
        timeout = (properties.has("timeout")) ? properties.get("timeout").getAsInt() : 0;
        deleteAfterTimeout = (properties.has("delete_after_timeout")) ? properties.get("delete_after_timeout").getAsInt() : 0;
    }

}
