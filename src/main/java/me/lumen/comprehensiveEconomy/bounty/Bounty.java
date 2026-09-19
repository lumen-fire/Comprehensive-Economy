package me.lumen.comprehensiveEconomy.bounty;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.xf3d3.ultimateteams.api.UltimateTeamsAPI;
import dev.xf3d3.ultimateteams.models.Team;
import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.utils.DataStorage;
import me.lumen.comprehensiveEconomy.utils.UUIDPDCType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Bounty {
    private static final HashMap<UUID, Bounty> BOUNTIES = new HashMap<>();

    private final HashMap<UUID, Double> setBounties = new HashMap<>();
    private double total;
    private final UUID uuid;

    private static final String SET_BOUNTY;
    private static final String CLAIM_BOUNTY;
    private static final String LOAD_BOUNTIES;
    private static final String CACHE_BOUNTY;
    private static final String REMOVE_BOUNTY;

    static {
        try {
            SET_BOUNTY = DataStorage.getInstance().loadSqlFile("sql/bounty/setBounty.sql");
            CLAIM_BOUNTY =  DataStorage.getInstance().loadSqlFile("sql/bounty/claimBounty.sql");
            LOAD_BOUNTIES = DataStorage.getInstance().loadSqlFile("sql/bounty/load.sql");
            CACHE_BOUNTY = DataStorage.getInstance().loadSqlFile("sql/bounty/cache.sql");
            REMOVE_BOUNTY = DataStorage.getInstance().loadSqlFile("sql/bounty/removeBounty.sql");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Bounty(UUID uuid) {
        total = 0;
        this.uuid = uuid;
    }

    public double getTotal(){
        return total;
    }

    public void setBounty(@NonNull Player player, double value){
        Double old = setBounties.get(player.getUniqueId());

        if (old == null){
            total += value;
        } else {
            total += (value - old);
        }

        if (value == 0){
            removeBounty(player);
            return;
        }

        setBounties.put(player.getUniqueId(), value);
        saveBounty(player);
    }

    public UUID uuid(){
        return uuid;
    }

    public void setBountyAnd(@NonNull Player player, double value, Runnable runnable){
        Double old = setBounties.get(player.getUniqueId());

        if (old == null){
            total += value;
        } else {
            total += (value - old);
        }

        if (value == 0){
            removeBounty(player);
            return;
        }

        setBounties.put(player.getUniqueId(), value);
        saveBountyAnd(player, runnable);
    }

    /**
     * Saves the players bounty into the db to what it was set to
     * @param issuer the issuer of the bounty, this sets how much they have issued the bounty for
     */
    private void saveBounty(@NonNull Player issuer){
        double value = setBounties.get(issuer.getUniqueId());
        String issuerUUID = issuer.getUniqueId().toString();
        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(SET_BOUNTY);
            preparedStatement.setString(1, issuerUUID);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setDouble(3, value);
            preparedStatement.executeUpdate();
        });
    }

    private void saveBountyAnd(@NonNull Player issuer, Runnable runnable){
        double value = setBounties.get(issuer.getUniqueId());
        String issuerUUID = issuer.getUniqueId().toString();
        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(SET_BOUNTY);
            preparedStatement.setString(1, issuerUUID);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setDouble(3, value);
            preparedStatement.executeUpdate();
            runnable.run();
        });
    }

    public double getSetBounty(@NonNull Player player){
        return getSetBounty(player, 0);
    }

    public double getSetBounty(@NonNull Player player, double def){
        Double value = setBounties.get(player.getUniqueId());
        if (value == null){
            return def;
        }
        return value;
    }

    public void addToBounty(@NonNull Player player, @Range(from = 0L, to = Integer.MAX_VALUE) double value){
        total += value;
        setBounties.merge(player.getUniqueId(), value, Double::sum);
        saveBounty(player);
    }

    public @CanIgnoreReturnValue boolean subtractFromBounty(@NonNull Player player, @Range(from = 0L, to = Integer.MAX_VALUE) double value){
        Double old = setBounties.get(player.getUniqueId());

        if (old != null && old > value){
            total -= value;
            setBounties.put(player.getUniqueId(), old - value);
            saveBounty(player);
            return true;
        } else if (old != null && old == value){
            //if they are equal it would go to 0
            removeBounty(player);
            return true;
        }
        return false;
    }

    public void claim(@NonNull Player killer){
        Account killerAccount = Account.getAccount(killer);
        killerAccount.depositAndSave(this.total);
        //clear actual internal stored data for safety
        this.total = 0;
        this.setBounties.clear();

        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(CLAIM_BOUNTY);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        });

        BOUNTIES.remove(this.uuid);
    }

    public @NonNull String createName(){
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if (name == null){
            name = "player " + uuid;
        }
        return name;
    }

    public void removeBounty(@NonNull Player issuer){
        UUID issuerUniqueId = issuer.getUniqueId();
        double old = setBounties.get(issuer.getUniqueId());
        this.setBounties.remove(issuer.getUniqueId());
        this.total -= old;
        //if this brought the total to 0 it means this bounty no longer exists
        if (this.total == 0) BOUNTIES.remove(this.uuid);
        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(REMOVE_BOUNTY);
            preparedStatement.setString(1, issuerUniqueId.toString());
            preparedStatement.setString(2, this.uuid.toString());
            preparedStatement.executeUpdate();
        });
    }

    public boolean hasSetBounty(@NonNull Player player){
        return setBounties.containsKey(player.getUniqueId());
    }

    public static @Nullable Bounty getBounty(@NonNull OfflinePlayer player){
        return getBounty(player.getUniqueId());
    }

    public static @Nullable Bounty getBounty(@NonNull UUID uuid){
        return BOUNTIES.get(uuid);
    }

    /**
     * @param player the wanted player, does not have to be online as all bounties are cached
     * @return the bounty, or a new, empty one if not found
     */
    public static @NonNull Bounty getOrCreateBounty(@NonNull OfflinePlayer player){
        return getOrCreateBounty(player.getUniqueId());
    }

    public static @NonNull Bounty getOrCreateBounty(@NonNull UUID uuid){
        Bounty bounty = getBounty(uuid);
        if (bounty == null){
            bounty = new Bounty(uuid);
            BOUNTIES.put(uuid, bounty);
        }
        return bounty;
    }

    public static void loadBounties(){
        BOUNTIES.clear();
        DataStorage.getInstance().runInDatabase(connection -> {
            ResultSet resultSet = connection.prepareStatement(LOAD_BOUNTIES).executeQuery();
            while (resultSet.next()){
                UUID wanted = UUID.fromString(resultSet.getString("wanted"));
                getOrCreateBounty(wanted).total = resultSet.getDouble("total_bounty");
            }
        });
    }

    public static void uncache(@NonNull Player player){
        UUID uuid = player.getUniqueId();
        for (Bounty bounty : BOUNTIES.values()){
            bounty.setBounties.remove(uuid);
        }
    }

    @Contract(pure = true)
    public static @NonNull @Unmodifiable Map<UUID, Bounty> getBounties(){
        return Map.copyOf(BOUNTIES);
    }

    public static void cache(@NonNull Player player){
        UUID uuid = player.getUniqueId();
        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(CACHE_BOUNTY);
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UUID wanted = UUID.fromString(resultSet.getString("wanted"));
                Double value = resultSet.getDouble("bounty");
                getOrCreateBounty(wanted).setBounties.put(uuid, value);
            }
        });
    }

    private static final NamespacedKey CLAIM_KEY = new NamespacedKey(ComprehensiveEconomy.getPlugin(), "claim_bounty");
    private static final NamespacedKey SPAWN_DATE = new NamespacedKey(ComprehensiveEconomy.getPlugin(), "spawn_date");
    private static final NamespacedKey KILLER_ID = new NamespacedKey(ComprehensiveEconomy.getPlugin(), "killer");
    public void spawnClaimItem(@NonNull Location location, @NonNull Player killer) {
        UUID killerId = killer.getUniqueId();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        //synchronous from here
        Bukkit.getScheduler().runTask(ComprehensiveEconomy.getPlugin(), () -> {
            ItemStack itemStack = ItemStack.of(Material.PAPER);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.itemName(Component.text("Claim bounty on " + offlinePlayer.getName()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            itemMeta.lore(List.of(
                    Component.text("Right click to claim the bounty on " + offlinePlayer.getName()).color(NamedTextColor.LIGHT_PURPLE),
                    Component.text("You have two minutes to claim it!").color(NamedTextColor.LIGHT_PURPLE),
                    Component.text("Only the player's killer can claim it and teammates cannot").color(NamedTextColor.LIGHT_PURPLE),
                    Component.text("Expires at " + ZonedDateTime.now().plusMinutes(2).format(DateTimeFormatter.ofPattern("h:mm:ss a z", Locale.ENGLISH))).color(NamedTextColor.GRAY)
            ));
            itemMeta.getPersistentDataContainer().set(CLAIM_KEY, BountyPDCType.TYPE, Optional.of(this));
            itemMeta.getPersistentDataContainer().set(SPAWN_DATE, PersistentDataType.LONG, System.currentTimeMillis());
            itemMeta.getPersistentDataContainer().set(KILLER_ID, UUIDPDCType.TYPE, killerId);
            itemStack.setItemMeta(itemMeta);

            location.getWorld().dropItem(location, itemStack);
        });
    }

    public static void tryUseClaimItem(@NonNull PlayerInteractEvent event) {
        if (!event.hasItem() || !(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) return;
        ItemStack itemStack = event.getItem();
        //not null as already checked with event#hasItem()
        assert itemStack != null;
        Player player = event.getPlayer();

        Optional<Bounty> bountyOptional = itemStack.getPersistentDataContainer().get(CLAIM_KEY, BountyPDCType.TYPE);
        //would be null if the item is not a claim item
        //noinspection OptionalAssignedToNull
        if (bountyOptional == null) return;
        if (bountyOptional.isEmpty()){
            player.sendMessage(Component.text("That player no longer has a bounty set on them!").color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            itemStack.setAmount(0);
            return;
        }

        Bounty bounty = bountyOptional.get();

        Long spawnTime = itemStack.getPersistentDataContainer().get(SPAWN_DATE, PersistentDataType.LONG);
        if  (spawnTime == null) return;
        //(1 second = 1000ms) * (120 secs = 2min) = 120,000
        if (System.currentTimeMillis() - spawnTime > 120000) {
            player.sendMessage(Component.text("This bounty claim item has expired!").color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            itemStack.setAmount(0);
            return;
        }

        UUID killerId = itemStack.getPersistentDataContainer().get(KILLER_ID, UUIDPDCType.TYPE);
        if (killerId == null) return;
        if (!killerId.equals(player.getUniqueId())) {
            player.sendMessage(Component.text("Only the killer can claim the bounty!").color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("UltimateTeams")){
            Optional<Team> teamOptional = UltimateTeamsAPI.getInstance().findTeamByMember(bounty.uuid);
            if (teamOptional.isPresent()) {
                Team team = teamOptional.get();
                if (team.getOnlineMembers().contains(player)) {
                    player.sendMessage(Component.text("You cannot claim a bounty on one of your teammates!").color(NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    itemStack.setAmount(0);
                    return;
                }
            }
        }

        String name = Bukkit.getOfflinePlayer(bounty.uuid).getName();
        final double value = bounty.total;
        bounty.claim(player);
        itemStack.setAmount(0);
        Bukkit.getServer().sendMessage(Component.text(player.getName() + " claimed the bounty on " + name + " for " + CurrencyUtils.format(value)).color(NamedTextColor.GREEN));
    }

    public static boolean hasBounty(@NonNull OfflinePlayer player){
        return BOUNTIES.containsKey(player.getUniqueId());
    }

}
