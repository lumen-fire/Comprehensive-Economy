package me.lumen.comprehensiveEconomy.menu.bounty;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.bounty.Bounty;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.utils.InventoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class BountyInventoryGUI implements InventoryHolder {
    private Inventory inventory;
    private Bounty[][] pages;
    private int page;

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
    public BountyInventoryGUI() {
        inventory = ComprehensiveEconomy.getPlugin().getServer().createInventory(this, 54, Component.text("Bounties - p. 1").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    public void show(@NonNull Player player) {
        openPage(player, 0);
    }

    private void openPage(@NonNull Player player, int page){
        refresh();

        if (page < 0 || page >= pages.length) {
            page = this.page;
        }

        this.page = page;

        inventory = ComprehensiveEconomy.getPlugin().getServer().createInventory(this, 54, Component.text("Bounties - p." + (page + 1)).color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
        InventoryGUI.fillWithFiller(inventory);

        for (int i = 0; i < 45 && pages[page][i] != null; i++) {
            inventory.setItem(i, createItem(pages[page][i], player));
        }

        ItemStack back = ItemStack.of(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.customName(Component.text("Back").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);

        ItemStack next = ItemStack.of(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.customName(Component.text("Next").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        next.setItemMeta(nextMeta);


        ItemStack setNew = ItemStack.of(Material.WRITABLE_BOOK);
        ItemMeta setNewMeta = setNew.getItemMeta();
        setNewMeta.customName(Component.text("Set a bounty").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        setNew.setItemMeta(setNewMeta);

        inventory.setItem(45, back);
        inventory.setItem(53, next);
        inventory.setItem(49, setNew);
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1,1);
    }

    public void click(@NonNull InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (slot < 45 && slot >=0) {
            Bounty bounty = pages[page][slot];
            if (bounty == null) return;
            if (bounty.uuid().equals(player.getUniqueId())) return;
            BountyDialogMenu menu = new BountyDialogMenu(player, bounty);
            switch (event.getClick()) {
                case LEFT -> menu.showAddingMenu(viewer -> openPage(player, page));
                case SHIFT_LEFT -> menu.showSettingMenu(viewer -> openPage(player, page));
                case RIGHT -> {
                    if (!bounty.hasSetBounty(player)) return;
                    menu.showSubtractingMenu(viewer -> openPage(player, page));
                }
                case SHIFT_RIGHT -> {
                    if (!bounty.hasSetBounty(player)) return;
                    Account.getAccount(player).depositAndSave(bounty.getSetBounty(player));
                    bounty.removeBounty(player);
                    openPage(player, page);
                    player.sendMessage(Component.text("Removed your bounty on " + bounty.createName()).color(NamedTextColor.YELLOW));
                }
            }
        } else {
            switch (slot){
                case 45 -> openPage(player, page - 1);
                case 53 -> openPage(player, page + 1);
                case 49 -> BountyDialogMenu.showSettingNewBountyMenu(player, viewer -> openPage(viewer, page));
            }
        }
    }

    private void refresh(){
        InventoryGUI.fillWithFiller(inventory);

        ArrayList<Bounty> bountiesList = new ArrayList<>(Bounty.getBounties().values());
        bountiesList.sort(Comparator.comparingDouble(Bounty::getTotal).reversed());
        pages = new Bounty[Math.ceilDiv(bountiesList.size(), 45)][45];
        if (pages.length == 0) pages = new Bounty[1][45];
        int slot = 0;
        int page = 0;
        for (Bounty bounty : bountiesList) {

            if (slot >= 45) {
                page++;
                slot = 0;
            }

            pages[page][slot] = bounty;

            slot++;
        }
    }

    private static @NonNull ItemStack createItem(@NonNull Bounty bounty, Player viewer) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(bounty.uuid());
        String name = bounty.createName();
        ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwningPlayer(offlinePlayer);
        skullMeta.customName(Component.text(name).color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));

        if (bounty.hasSetBounty(viewer)){
            skullMeta.lore(List.of(
                    Component.text("Bounty: " + CurrencyUtils.format(bounty.getTotal())).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                    Component.text("Your bounty on them: " + CurrencyUtils.format(bounty.getSetBounty(viewer))).color(NamedTextColor.GOLD),
                    Component.text("Left click to add").color(NamedTextColor.GREEN),
                    Component.text("Right click to subtract").color(NamedTextColor.GREEN),
                    Component.text("Shift + left click to set").color(NamedTextColor.GREEN),
                    Component.text("Shift + right click to delete").color(NamedTextColor.RED).append(Component.text(" ⚠⚠⚠").color(NamedTextColor.YELLOW))
            ));
        } else if (bounty.uuid().equals(viewer.getUniqueId())) {
            skullMeta.lore(List.of(Component.text("Bounty: " + CurrencyUtils.format(bounty.getTotal())).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)));
        } else {
            skullMeta.lore(List.of(
                    Component.text("Bounty: " + CurrencyUtils.format(bounty.getTotal())).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                    Component.text("Left click to add").color(NamedTextColor.GREEN),
                    Component.text("Shift + left click to set").color(NamedTextColor.GREEN)
            ));
        }

        head.setItemMeta(skullMeta);

        return head;
    }
}
