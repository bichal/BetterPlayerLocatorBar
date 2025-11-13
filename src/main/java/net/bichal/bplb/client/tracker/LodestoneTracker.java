package net.bichal.bplb.client.tracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LodestoneTracker {
    private static final Map<UUID, LodestoneData> lodestoneMarkers = new HashMap<>();

    public static void updateLodestones(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        PlayerEntity player = client.player;
        lodestoneMarkers.clear();

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.COMPASS) {
                LodestoneTrackerComponent tracker = stack.get(DataComponentTypes.LODESTONE_TRACKER);
                if (tracker != null && tracker.target().isPresent()) {
                    GlobalPos globalPos = tracker.target().get();
                    if (globalPos.dimension().equals(client.world.getRegistryKey())) {
                        BlockPos pos = globalPos.pos();
                        UUID id = UUID.nameUUIDFromBytes(pos.toShortString().getBytes());
                        String name = stack.getName().getString();
                        lodestoneMarkers.put(id, new LodestoneData(id, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, name));
                    }
                }
            }
        }
    }

    public static Collection<LodestoneData> getLodestoneMarkers() {
        return lodestoneMarkers.values();
    }

    public static boolean isLodestoneId(UUID id) {
        return lodestoneMarkers.containsKey(id);
    }

    public static String getLodestoneNameByUuid(UUID id) {
        LodestoneData data = lodestoneMarkers.get(id);
        return data != null ? data.name() : "Lodestone";
    }

    public record LodestoneData(UUID id, double x, double y, double z, String name) {}
}
