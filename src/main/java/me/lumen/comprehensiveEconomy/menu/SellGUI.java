package me.lumen.comprehensiveEconomy.menu;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.utils.InventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SellGUI implements InventoryHolder {
    private final Inventory inventory;

    public SellGUI() {
        int size = ComprehensiveEconomy.getPlugin().getConfig().getInt("sell-gui-rows", 3) * 9;
        this.inventory = ComprehensiveEconomy.getPlugin().getServer().createInventory(this, size, Component.text("Sell your items").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
        InventoryGUI.fillWithFiller(this.inventory);
        Map<String, Integer> slots = CurrencyUtils.getSlots();
        for (String key : slots.keySet()) {
            NamespacedKey namespacedKey = NamespacedKey.fromString(key);
            if (namespacedKey == null) continue;
            Material material = Registry.MATERIAL.get(namespacedKey);
            if (material == null) continue;
            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();
            //set name
            String name = CurrencyUtils.getName(material);
            if (name == null) continue;
            itemMeta.itemName(Component.text(name).color(NamedTextColor.GOLD));
            //set lore
            Optional<Double> possiblePrice = CurrencyUtils.getSellPrice(material);
            if (possiblePrice.isEmpty()) continue;
            itemMeta.lore(List.of(Component.text("Sells for: " + CurrencyUtils.format(possiblePrice.get())).color(NamedTextColor.GREEN), Component.text("Click to sell one").color(NamedTextColor.GREEN), Component.text("Shift click to sell all in your inventory").color(NamedTextColor.GREEN)));
            itemStack.setItemMeta(itemMeta);
            //set slot
            this.inventory.setItem(slots.get(key), itemStack);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void show(@NonNull Player player) {
        player.openInventory(this.inventory);
    }

    public void click(@NonNull InventoryClickEvent event) {
        int slot = event.getRawSlot();
        Map<String, Integer> slots = CurrencyUtils.getSlots();
        for (String itemKey : slots.keySet()){
            if (slots.get(itemKey) != slot) continue;
            //only then run logic
            //get the material clicked
            NamespacedKey namespacedKey = NamespacedKey.fromString(itemKey);
            if (namespacedKey == null) continue;
            Material material = Registry.MATERIAL.get(namespacedKey);
            if (material == null) continue;
            //get player - for some reason this returns a HumanEntity though
            HumanEntity human  = event.getWhoClicked();
            if (!(human instanceof Player player)) continue;
            Account account = Account.getAccount(player);
            if (event.isShiftClick()){
                if (!player.getInventory().contains(material)) {
                    player.sendMessage(Component.text("You do not have this item!").color(NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1,1);
                    continue;
                }
                //if shift click
                //loop through all their items and sell the ones of that material
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item == null) continue;
                    Material itemMaterial = item.getType();
                    if (!itemMaterial.getKey().asString().equals(itemKey)) continue;
                    account.sell(item, item.getAmount());
                    account.saveChanges();
                }
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1,1);
            }else {
                int foundSlot = player.getInventory().first(material);
                if (foundSlot == -1) {
                    player.sendMessage(Component.text("You do not have this item!").color(NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    continue;
                }
                ItemStack itemStack = player.getInventory().getItem(foundSlot);
                if (itemStack == null) continue;
                account.sell(itemStack);
                account.saveChanges();
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
        }
    }
}
