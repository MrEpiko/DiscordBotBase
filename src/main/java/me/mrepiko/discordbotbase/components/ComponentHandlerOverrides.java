package me.mrepiko.discordbotbase.components;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;

@Getter
public class ComponentHandlerOverrides {

    private ItemComponent component;
    private int rowIndex;
    private boolean defer;
    private boolean ephemeralDefer;
    private int timeout;
    private int deleteAfterTimeout;
    private boolean disableOnceUsed;
    private boolean disableAllOnceUsed;
    private boolean invokerOnly;

    private boolean componentOverrode;
    private boolean rowIndexOverrode;
    private boolean deferOverrode;
    private boolean ephemeralDeferOverrode;
    private boolean timeoutOverrode;
    private boolean deleteAfterTimeoutOverrode;
    private boolean disableOnceUsedOverrode;
    private boolean disableAllOnceUsedOverrode;
    private boolean invokerOnlyOverrode;

    public void setComponent(ItemComponent component) {
        this.component = component;
        this.componentOverrode = true;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
        this.rowIndexOverrode = true;
    }

    public void setDefer(boolean defer) {
        this.defer = defer;
        this.deferOverrode = true;
    }

    public void setEphemeralDefer(boolean ephemeralDefer) {
        this.ephemeralDefer = ephemeralDefer;
        this.ephemeralDeferOverrode = true;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        this.timeoutOverrode = true;
    }

    public void setDeleteAfterTimeout(int deleteAfterTimeout) {
        this.deleteAfterTimeout = deleteAfterTimeout;
        this.deleteAfterTimeoutOverrode = true;
    }

    public void setDisableOnceUsed(boolean disableOnceUsed) {
        this.disableOnceUsed = disableOnceUsed;
        this.disableOnceUsedOverrode = true;
    }

    public void setDisableAllOnceUsed(boolean disableAllOnceUsed) {
        this.disableAllOnceUsed = disableAllOnceUsed;
        this.disableAllOnceUsedOverrode = true;
    }

    public void setInvokerOnly(boolean invokerOnly) {
        this.invokerOnly = invokerOnly;
        this.invokerOnlyOverrode = true;
    }

}
