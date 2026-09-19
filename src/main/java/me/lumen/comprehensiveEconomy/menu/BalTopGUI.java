package me.lumen.comprehensiveEconomy.menu;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.utils.InventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BalTopGUI implements InventoryHolder {
    private final Inventory inventory;

    /**
     * Make sure to construct a new one each time to get the new/updated values!
     */
    public BalTopGUI(){
        //two rows of 9
        this.inventory = ComprehensiveEconomy.getPlugin().getServer().createInventory(this, 18, Component.text("BalTop Menu - top 10 richest").color(NamedTextColor.DARK_PURPLE));
        InventoryGUI.fillWithFiller(this.inventory);
        int position = 0;
        for (Account.TopAccount topAccount : Account.getTopAccounts()){
            position++;
            ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setOwningPlayer(topAccount.createOfflinePlayer());
            headMeta.setRarity(ItemRarity.COMMON);
            headMeta.customName(Component.text(topAccount.getName()).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_PURPLE));
            headMeta.lore(List.of(
                    Component.text("Number " + position + " richest player on the server").color(NamedTextColor.GOLD),
                    Component.text("Balance: " + CurrencyUtils.format(topAccount.getBalance())).color(NamedTextColor.GOLD),
                    Component.text("Can be up to 1 min out of date").color(NamedTextColor.RED)
            ));
            head.setItemMeta(headMeta);
            this.inventory.setItem(position - 1, head);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void display(@NonNull HumanEntity viewer){
        viewer.openInventory(this.inventory);
    }
}
