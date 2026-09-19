package me.lumen.comprehensiveEconomy.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;

public class InventoryGUI {
    private final static ItemStack FILLER_ITEM = getFillerItem();
    private static @NonNull ItemStack getFillerItem(){
        ItemStack item = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setHideTooltip(true);
        item.setItemMeta(meta);
        return item;
    }
    public static void fillWithFiller(@NonNull Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, FILLER_ITEM);
        }
    }
}
