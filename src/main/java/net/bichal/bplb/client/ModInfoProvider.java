package net.bichal.bplb.client;

import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColor;
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColorValue;
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink;
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue;
import net.bichal.bichalutils.util.Logger;

public record ModInfoProvider(String id) implements ResourcefulConfigInfo {
    @Override public TranslatableValue title() {
        Logger.info(id);
        return new TranslatableValue(id);
    }

    @Override public TranslatableValue description() {
        return new TranslatableValue("Configuration for Better Player Locator Bar");
    }

    @Override public String icon() {
        return "box";
    }

    @Override public ResourcefulConfigColor color() {
        return (ResourcefulConfigColorValue) () -> "#FF00FF";
    }

    @Override public ResourcefulConfigLink[] links() {
        return new ResourcefulConfigLink[0];
    }

    @Override public boolean isHidden() {
        return false;
    }
}
