package me.lumen.comprehensiveEconomy.menu;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.homes.Home;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class HomeMenu {

    public static @NonNull Dialog getSetHomeMenu(int homeNumber) {
        return Dialog.create(dialogRegistryBuilderFactory -> {
            double predictedCost = ComprehensiveEconomy.getPlugin().getConfig().getDouble("homes.teleport.cost-per-home") * homeNumber + ComprehensiveEconomy.getPlugin().getConfig().getDouble("homes.teleport.base-cost");
                    dialogRegistryBuilderFactory.empty()
                            .base(DialogBase.builder(Component.text("Set a new home").color(NamedTextColor.GREEN))
                                    .inputs(List.of(DialogInput.text("name", Component.text("Home Name")).maxLength(20).initial("home " + homeNumber).build()))
                                    .body(List.of(DialogBody.plainMessage(Component.text("Set home #" + homeNumber + " for " + CurrencyUtils.format(predictedCost)).color(NamedTextColor.GREEN))))
                                    .build())
                            .type(DialogType.confirmation(
                                    ActionButton.builder(Component.text("Confirm"))
                                            .action(DialogAction.customClick((response, audience) -> {
                                                if (audience instanceof Player player) {
                                                    String homeName = response.getText("name");
                                                    if (homeName == null || homeName.isEmpty()) {
                                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                                                        return;
                                                    }
                                                    if (Home.getHomesCount(player) >= 5){
                                                        player.closeDialog();
                                                        player.sendMessage(Component.text("You have reached the maximum amount of homes!").color(NamedTextColor.RED));
                                                        return;
                                                    }
                                                    double cost = Home.calculateSetCost(player);
                                                    Account account = Account.getAccount(player);
                                                    if (!account.withdrawAndSave(cost)) {
                                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                                                        player.closeDialog();
                                                        player.sendMessage(Component.text("You do not have enough " + CurrencyUtils.getCurrencyNamePlural() + " to set this home!").color(NamedTextColor.RED));
                                                        player.sendMessage(Component.text("You need " + CurrencyUtils.format(cost) + " to set this home!").color(NamedTextColor.RED));
                                                        return;
                                                    }
                                                    Location loc = player.getLocation();
                                                    Home home = new Home(player, loc, homeName);
                                                    home.save();
                                                    player.closeDialog();
                                                    String locationString = loc.x() + ", " + loc.y() + ", " + loc.z() + " in " + loc.getWorld().getName() + " pitch: " + loc.getPitch() + " yaw: " + loc.getYaw();
                                                    player.sendMessage(Component.text("Set home " + homeName + " to " + locationString + " for " + CurrencyUtils.format(cost)).color(NamedTextColor.GREEN));
                                                }
                                            }, ClickCallback.Options.builder().build()))
                                            .build(),
                                    ActionButton.builder(Component.text("Cancel"))
                                            .build()
                            ));
                }
        );
    }

    public static @NonNull Dialog getDeleteHomeMenu(Collection<Home> homes){
        return Dialog.create(dialogRegistryBuilderFactory -> {
            List<ActionButton> buttons = new ArrayList<>();
            for (Home home : homes){
                ActionButton button = ActionButton.builder(Component.text(home.toString()).color(NamedTextColor.RED))
                        .tooltip(Component.text("Click to delete this home!").color(NamedTextColor.RED))
                        .action(DialogAction.customClick((response, audience) -> audience.showDialog(getDeletionConfirmation(home)), ClickCallback.Options.builder().build()))
                        .width(50)
                        .build();
                buttons.add(button);
            }
            dialogRegistryBuilderFactory.empty()
                    .type(DialogType.multiAction(buttons, ActionButton.builder(Component.text("Cancel")).build(), 5))
                    .base(DialogBase.builder(Component.text("Delete a home").color(NamedTextColor.RED))
                            .body(List.of(DialogBody.plainMessage(Component.text("Homes: " + homes.size()).color(NamedTextColor.YELLOW))))
                            .build());
        });
    }

    private static @NonNull Dialog getDeletionConfirmation(Home home){
        return Dialog.create(dialogRegistryBuilderFactory -> dialogRegistryBuilderFactory.empty()
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Confirm"))
                                .action(DialogAction.customClick((response, audience) -> {
                                    audience.closeDialog();
                                    home.delete();
                                    audience.sendMessage(Component.text("Deleted home " + home).color(NamedTextColor.YELLOW));
                                }, ClickCallback.Options.builder().build()))
                                .build(),
                        ActionButton.builder(Component.text("Cancel")).build()
                        ))
                .base(DialogBase.builder(Component.text("Delete home " + home).color(NamedTextColor.RED))
                        .body(List.of(DialogBody.plainMessage(Component.text("Are you sure you want to delete home " + home + "?").color(NamedTextColor.YELLOW))))
                        .build()));
    }

    public static @NonNull Dialog getHomesMenu(Collection<Home> homes){
        return Dialog.create(dialogRegistryBuilderFactory -> {
            List<ActionButton> buttons = new ArrayList<>();

            for (Home home : homes){
                ActionButton button = ActionButton.builder(Component.text(home.toString()).color(NamedTextColor.YELLOW))
                        .width(50)
                        .tooltip(Component.text("Teleport to home " + home).color(NamedTextColor.YELLOW))
                        .action(DialogAction.customClick((response, audience) -> {
                            if (!(audience instanceof Player player)) return;
                            double price = Home.calculateTeleportCost(player);
                            if (!Account.getAccount(player).has(price)){
                                player.closeDialog();
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1,1);
                                player.sendMessage(Component.text("You do not have enough " + CurrencyUtils.getCurrencyNamePlural() + " to use your homes!").color(NamedTextColor.RED));
                                player.sendMessage(Component.text("You need " + CurrencyUtils.format(price) + " to teleport, consider lowering the amount of homes you have to reduce the price").color(NamedTextColor.RED));
                                return;
                            }
                            player.closeDialog();
                            home.countdownTeleport(10);
                        }, ClickCallback.Options.builder().build()))
                        .build();
                buttons.add(button);
            }

            buttons.add(ActionButton.builder(Component.text("Set").color(NamedTextColor.GREEN))
                    .action(DialogAction.staticAction(ClickEvent.showDialog(getSetHomeMenu(homes.size() + 1))))
                    .width(50)
                    .tooltip(Component.text("Set another home at your location").color(NamedTextColor.GREEN))
                    .build());

            if (!homes.isEmpty()){
                buttons.add(ActionButton.builder(Component.text("Delete").color(NamedTextColor.RED))
                        .tooltip(Component.text("Delete a home").color(NamedTextColor.RED))
                        .width(50)
                        .action(DialogAction.staticAction(ClickEvent.showDialog(getDeleteHomeMenu(homes))))
                        .build());
            }

            dialogRegistryBuilderFactory.empty()
                    .type(DialogType.multiAction(buttons, ActionButton.builder(Component.text("Cancel")).build(), 5))
                    .base(DialogBase.builder(Component.text("Homes Menu").color(NamedTextColor.GREEN)).build());
        });
    }

    public static void showHomesMenu(@NonNull Player player){
        player.showDialog(getHomesMenu(Home.getHomes(player)));
    }

}
