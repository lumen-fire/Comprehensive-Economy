package me.lumen.comprehensiveEconomy.menu;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PurchaseConfirmation {

    /**
     * Shows a purchase confirmation Dialog.
     * The timeout defaults to 90 seconds.
     * @param player the player to show it to
     * @param cost the cost of the purchase
     * @param purchaseName the name of the purchase (e.g "2 diamonds")
     * @param purchase the code to run when the purchase is made
     */
    public static void show(Player player, double cost, String purchaseName, Purchase purchase){
        show(player, cost, purchaseName, purchase, 1800);
    }
    /**
     * Shows a purchase confirmation Dialog.
     * @param player the player to show it to
     * @param cost the cost of the purchase
     * @param purchaseName the name of the purchase (e.g "2 diamonds")
     * @param purchase the code to run when the purchase is made
     * @param timeout the timeout for the dialog to close, in milliseconds
     */
    public static void show(@NonNull Player player, double cost, String purchaseName, Purchase purchase, long timeout){
        player.showDialog(getConfirmationDialog(cost, purchaseName, purchase));
        Bukkit.getScheduler().runTaskLater(ComprehensiveEconomy.getPlugin(), player::closeDialog, timeout);
    }

    private static @NonNull Dialog getConfirmationDialog(double cost, String purchaseName, Purchase purchase) {
        return Dialog.create(dialogRegistryBuilderFactory -> dialogRegistryBuilderFactory
                .empty()
                .type(DialogType.confirmation(
                                        ActionButton.builder(Component.text("Confirm").color(NamedTextColor.GREEN))
                                                .action(DialogAction.customClick(
                                                        (ignored, audience) -> {
                                                            if (audience instanceof Player player) {
                                                                Account account = Account.getAccount(player);
                                                                if (!account.withdrawAndSave(cost)) {
                                                                    player.closeDialog();
                                                                    player.sendMessage(Component.text("You do not have enough " + CurrencyUtils.getCurrencyNamePlural() + " to purchase " + purchaseName).color(NamedTextColor.RED));
                                                                    return;
                                                                }
                                                                player.sendMessage(Component.text("Completed purchase of " + purchaseName + " for " + CurrencyUtils.format(cost)).color(NamedTextColor.GREEN));
                                                                purchase.onPurchase(player);
                                                            }
                                                        }
                                                        , ClickCallback.Options.builder().build()))
                                                .build(), ActionButton.builder(Component.text("Cancel").color(NamedTextColor.RED)).build()))
                                .base(DialogBase.builder(Component.text("Confirm Purchase"))
                                        .body(List.of(DialogBody.plainMessage(Component.text("Confirm purchase: " + purchaseName + " for " + CurrencyUtils.format(cost)).color(NamedTextColor.GREEN))))
                                        .build()));
    }
}
