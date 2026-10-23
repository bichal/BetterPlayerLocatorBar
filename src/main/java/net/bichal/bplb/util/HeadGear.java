package net.bichal.bplb.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//? if <1.21.5 {
import net.minecraft.world.item.ArmorItem;
//?} else {
/*import net.minecraft.core.component.DataComponents;
*///?}

/** Whether a player's head slot hides them from the locator bar (mob heads, pumpkins; real armor does not). */
public final class HeadGear {
    private HeadGear() {}

    public static boolean hidesPlayer(Player target) {
        ItemStack head = target.getItemBySlot(EquipmentSlot.HEAD);
        return target.isShiftKeyDown() || target.isInvisible() || (!head.isEmpty() && !isArmor(head));
    }

    private static boolean isArmor(ItemStack stack) {
        //? if <1.21.5 {
        return stack.getItem() instanceof ArmorItem;
        //?} else {
        /*var equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.assetId().isPresent();
        *///?}
    }
}
