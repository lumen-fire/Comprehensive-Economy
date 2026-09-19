package me.lumen.comprehensiveEconomy.menu.bounty;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.bounty.Bounty;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.utils.DatabaseDebounce;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class BountyDialogMenu {

    private final Player viewer;
    private Bounty bounty;
    public BountyDialogMenu(Player viewer, Bounty bounty) {
        this.viewer = viewer;
        this.bounty = bounty;
    }

    private void showSubtractingMenu(Consumer<Player> back, @Nullable String error){
        Dialog dialog = Dialog.create(dialogRegistryBuilderFactory -> dialogRegistryBuilderFactory.empty()
                .base(buildBase("Subtract from " + bounty.createName() + "'s bounty", error))
                .type(buildBitsInputType((player, account, amount) -> {
                    //if amount to subtract is more than there is
                    if (amount > bounty.getSetBounty(player)) {
                        showSubtractingMenu(back, "Cannot set more than your bounty is set to");
                        return;
                    }
                    account.depositAndSave(amount);
                    bounty.subtractFromBounty(player, amount);
                    player.sendMessage(Component.text("Subtracted " + CurrencyUtils.format(amount) + " from your bounty on " + bounty.createName()).color(NamedTextColor.YELLOW));
                    back.accept(player);
                }, s -> showSubtractingMenu(back, s))));
        viewer.showDialog(dialog);
    }

    public void showSubtractingMenu(Consumer<Player> back){
        showSubtractingMenu(back, null);
    }

    private void showSettingMenu(Consumer<Player> back, @Nullable String error){
        Dialog dialog = Dialog.create(dialogRegistryBuilderFactory -> dialogRegistryBuilderFactory.empty()
                .base(buildBase("Set " + bounty.createName() + "'s bounty", error))
                .type(buildBitsInputType((player, account, amount) -> {
                    double oldAmount = bounty.getSetBounty(player);
                    double cost = amount - oldAmount;
                    if (!account.withdrawAndSave(cost)){
                        showSettingMenu(back, "You need " + CurrencyUtils.format(cost));
                        return;
                    }
                    if (Bounty.getBounty(bounty.uuid()) != bounty){
                        bounty = Bounty.getOrCreateBounty(bounty.uuid());
                    }
                    bounty.setBounty(player, amount);
                    player.sendMessage(Component.text("Set your bounty on " + bounty.createName() + " to " + CurrencyUtils.format(amount)).color(NamedTextColor.GREEN));
                    back.accept(player);
                }, s -> showSettingMenu(back, s)))
        );
        viewer.showDialog(dialog);
    }

    public void showSettingMenu(Consumer<Player> back){
        showSettingMenu(back, null);
    }

    private void showAddingMenu(Consumer<Player> back, @Nullable String error) {
        Dialog dialog = Dialog.create(dialogRegistryBuilderFactory -> dialogRegistryBuilderFactory.empty()
                .base(buildBase("Add to " + bounty.createName() + "'s bounty", error))
                .type(buildBitsInputType((player, account, amount) -> {
                    if (!account.withdrawAndSave(amount)){
                        showAddingMenu(back, "You need " + CurrencyUtils.format(amount));
                        return;
                    }
                    if (Bounty.getBounty(bounty.uuid()) != bounty){
                        bounty = Bounty.getOrCreateBounty(bounty.uuid());
                    }
                    bounty.addToBounty(player, amount);
                    player.sendMessage(Component.text("Added " + CurrencyUtils.format(amount) + " to your bounty on " + bounty.createName()).color(NamedTextColor.GREEN));
                    back.accept(player);
                }, s -> showAddingMenu(back, s)))
        );
        viewer.showDialog(dialog);
    }

    public void showAddingMenu(Consumer<Player> back) {
        showAddingMenu(back, null);
    }

    public static void showSettingNewBountyMenu(@NonNull Player viewer, Consumer<Player> back) {
        viewer.showDialog(getPlayerSelectorDialog(null, wanted -> {
            Bounty bounty = Bounty.getOrCreateBounty(wanted);
            BountyDialogMenu menu = new BountyDialogMenu(viewer, bounty);
            menu.showSettingMenu(back);
        }, viewer));
    }

    private @NonNull DialogType buildBitsInputType(TriConsumer<Player, Account, @NonNull Double> logic, Consumer<String> showError){
        return DialogType.confirmation(ActionButton.builder(Component.text("Confirm"))
                .action(DialogAction.customClick((response, audience) -> {
                    if (!(audience instanceof Player player)) return;

                    if (DatabaseDebounce.blocked(player)){
                        showError.accept(DatabaseDebounce.getTooFastMessageString(player));
                        return;
                    }
                    DatabaseDebounce.update(player, 100);

                    String amountString = response.getText("amount");
                    if (amountString == null) {
                        showError.accept(CurrencyUtils.getCurrencyNamePlural() + " field is required ");
                        return;
                    }
                    double amount;
                    try {
                        amount = CurrencyUtils.round(Double.parseDouble(amountString));
                    } catch (NumberFormatException e) {
                        showError.accept("Not a number");
                        return;
                    }

                    if (amount <= 0) {
                        showError.accept("Amount must be greater than 0");
                        return;
                    }

                    Account account = Account.getAccount(player);
                    logic.accept(player, account, amount);
                }, ClickCallback.Options.builder().build()))
                .build(), ActionButton.builder(Component.text("Cancel")).build());
    }

    private @NonNull DialogBase buildBase(String title, @Nullable String error){
        List<DialogBody> body = new ArrayList<>();
        String name = bounty.createName();
        if (bounty.hasSetBounty(viewer)) body.add(DialogBody.plainMessage(Component.text("Your current bounty on " + name + ": " + CurrencyUtils.format(bounty.getSetBounty(viewer))).color(NamedTextColor.GREEN)));
        body.add(DialogBody.plainMessage(Component.text("Total bounty on " + name + ": " + CurrencyUtils.format(bounty.getSetBounty(viewer))).color(NamedTextColor.GREEN)));
        if (error != null) body.add(DialogBody.plainMessage(Component.text("Error: " + error).color(NamedTextColor.RED)));
        return DialogBase.builder(Component.text(title))
                .body(body)
                .inputs(List.of(DialogInput.text("amount", Component.text(CurrencyUtils.getCurrencyNamePlural())).build()))
                .build();
    }

    private static @NonNull Dialog getPlayerSelectorDialog(@Nullable String error, Consumer<OfflinePlayer> selected, Player viewer){
        return Dialog.create(dialogRegistryBuilderFactory -> {
            Component label;
            if (error == null) {
                label = Component.text("Type in a player");
            } else {
                label = Component.text("Type in a player - ").append(Component.text("error: " + error).color(NamedTextColor.RED));
            }

            //default to 150 (which is Minecraft's default)
            int width = 150;
            if (error != null) {
                width = (60/11) * 29 + (60/11) * error.length();
            }

            List<ActionButton> buttons = new ArrayList<>();
            buttons.add(ActionButton.builder(Component.text("Confirm type-in"))
                    .action(DialogAction.customClick((response, audience) -> {
                        String name = response.getText("name");
                        if (name == null || name.isEmpty()) {
                            audience.showDialog(getPlayerSelectorDialog("player field cannot be empty", selected, viewer));
                            return;
                        }
                        if (name.equals(viewer.getName())) {
                            audience.showDialog(getPlayerSelectorDialog("you cannot choose yourself", selected, viewer));
                            return;
                        }
                        Bukkit.getScheduler().runTaskAsynchronously(ComprehensiveEconomy.getPlugin(), () -> {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                                audience.showDialog(getPlayerSelectorDialog(name + " has never joined the server", selected, viewer));
                                return;
                            }
                            selected.accept(offlinePlayer);
                        });
                    }, ClickCallback.Options.builder().build()))
                    .build());
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player == viewer) continue;
                if (Bounty.hasBounty(player)){
                    Bounty bounty = Bounty.getBounty(player);
                    assert  bounty != null;

                    if (bounty.hasSetBounty(viewer)) {
                    buttons.add(ActionButton.builder(Component.text(player.getName()).color(NamedTextColor.GREEN))
                            .action(DialogAction.customClick((response, audience) -> selected.accept(player), ClickCallback.Options.builder().build()))
                            .tooltip(Component.text("Bounty: " + CurrencyUtils.format(bounty.getTotal())).color(NamedTextColor.GOLD)
                                            .append(Component.text("\nYour bounty on them: " + CurrencyUtils.format(bounty.getSetBounty(viewer))).color(NamedTextColor.GOLD)))
                            .build());
                    } else {
                        buttons.add(ActionButton.builder(Component.text(player.getName()).color(NamedTextColor.GREEN))
                                .action(DialogAction.customClick((response, audience) -> selected.accept(player), ClickCallback.Options.builder().build()))
                                .tooltip(Component.text("Bounty: " + CurrencyUtils.format(bounty.getTotal())).color(NamedTextColor.GOLD))
                                .build());
                    }

                } else {
                    buttons.add(ActionButton.builder(Component.text(player.getName()).color(NamedTextColor.GREEN))
                            .action(DialogAction.customClick((response, audience) -> selected.accept(player), ClickCallback.Options.builder().build()))
                            .build());
                }
            }

            dialogRegistryBuilderFactory.empty()
                    .base(DialogBase.builder(Component.text("Choose a player"))
                            .inputs(List.of(DialogInput.text("name", label).maxLength(32).width(width).build()))
                            .body(List.of(DialogBody.plainMessage(Component.text("Select or type in a player"))))
                            .build())
                    .type(DialogType.multiAction(buttons).build());
        });
    }

}
