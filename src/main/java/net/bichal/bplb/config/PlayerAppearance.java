package net.bichal.bplb.config;

import org.jetbrains.annotations.Nullable;

public class PlayerAppearance {
    @Nullable
    public Integer color;
    @Nullable
    public String dotId;
    @Nullable
    public String borderId;
    @Nullable
    public String arrowId;

    public PlayerAppearance() {
        this.color = null;
        this.dotId = null;
        this.borderId = null;
        this.arrowId = null;
    }

    public void copyFrom(PlayerAppearance source) {
        this.color = source.color;
        this.dotId = source.dotId;
        this.borderId = source.borderId;
        this.arrowId = source.arrowId;
    }

    public PlayerAppearance deepCopy() {
        PlayerAppearance copy = new PlayerAppearance();
        copy.copyFrom(this);
        return copy;
    }
}
