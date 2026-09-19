package me.lumen.comprehensiveEconomy.homes;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.utils.DataStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class Home {
    private final Location location;
    private final Player owner;
    private final String name;
    //cache for the homes
    private static final HashMap<Player, HashMap<String, Home>> cachedHomes = new HashMap<>();
    //constructor for object
    public Home(Player owner, Location location, String name){
        this.location = location;
        this.owner = owner;
        this.name = name;
    }
    /**save a home*/
    public void save() {
        cachedHomes.computeIfAbsent(owner, (ignored) -> new HashMap<>());
        cachedHomes.get(owner).put(name, this);
        String name = location.getWorld().getName();
        DataStorage.getInstance().runInDatabase(connection -> {
            String sql;
            try {
                sql = DataStorage.getInstance().loadSqlFile("sql/homes/createHome.sql");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, this.owner.getUniqueId().toString());
            ps.setString(2, this.name);
            ps.setString(3, name);
            ps.setDouble(4, this.location.x());
            ps.setDouble(5, this.location.y());
            ps.setDouble(6, this.location.z());
            ps.setFloat(7, this.location.getYaw());
            ps.setFloat(8, this.location.getPitch());
            ps.executeUpdate();
        });

    }
    /**get a players homes*/
    public static @NonNull List<String> getHomeNames(Player player) {
        cachedHomes.computeIfAbsent(player, (ignored) -> new HashMap<>());
        Collection<Home> homes = cachedHomes.get(player).values();
        List<String> homesList = new ArrayList<>();
        for (Home home : homes) {
            homesList.add(home.name);
        }
        return homesList;
    }

    public static @NonNull Collection<Home> getHomes(Player player) {
        cachedHomes.computeIfAbsent(player, (ignored) -> new HashMap<>());
        return cachedHomes.get(player).values();
    }

    public String toString(){
        return this.name;
    }
    /**teleport a player to a home*/
    public void teleport(){
        if (owner.isInsideVehicle() && owner.getVehicle() != null){
            Entity mount = owner.getVehicle();
            List<Entity> riders = mount.getPassengers();
            mount.teleportAsync(this.location);
            for (Entity rider : riders) {
                rider.teleportAsync(this.location);
            }
        }else{
            owner.teleportAsync(this.location);
        }
    }

    /**get a home from a uuid and name*/
    public static Home getHome(Player player, String name) {
        cachedHomes.computeIfAbsent(player, ignored -> new HashMap<>());
        return cachedHomes.get(player).get(name);
    }

    /**
     * Cache a players homes, should be run on join, is internally async
     * @param player the player, wow
     */
    public static void cache(@NonNull Player player) {
        UUID uuid = player.getUniqueId();
        DataStorage.getInstance().runInDatabase(connection -> {
            String sql;
            try {
                sql = DataStorage.getInstance().loadSqlFile("sql/homes/getHomes.sql");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                //return null if home does not exist
                if (rs.getString("world") == null) {
                    continue;
                }
                Location loc = new Location(
                        Bukkit.getWorld(rs.getString("world")),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch")
                );
                String name = rs.getString("name");
                cachedHomes.computeIfAbsent(player, ignored -> new HashMap<>());
                cachedHomes.get(player).put(name, new Home(player, loc, name));
            }
        });
    }

    /**
     * Teleport them home with a countdown, this cancels if they move
     * @param countdown the countdown, in seconds
     */
    public void countdownTeleport(long countdown){
        //get things for later
        Location loc = owner.getLocation();
        //initial message
        owner.showTitle(Title.title(Component.text(countdown).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD), Component.text("Teleporting to home " + name + " in " + countdown + " seconds").color(NamedTextColor.GREEN)));
        owner.playSound(owner.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        //set up the countdown
        for (long i = 1; i < countdown; i++){
            //must be final as I need to use in lambda
            final long seconds = i;
            //set the scheduler
            Bukkit.getScheduler().runTaskLater(ComprehensiveEconomy.getPlugin(), () -> {
                owner.showTitle(Title.title(Component.text(seconds).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD), Component.text("Teleporting to home " + name + " in " + seconds + " seconds").color(NamedTextColor.GREEN)));
                owner.playSound(owner.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            }, (countdown - i) * 20L);
        }
        //set up the teleport
        Bukkit.getScheduler().runTaskLater(ComprehensiveEconomy.getPlugin(), () -> {
            owner.clearTitle();
            //check not moved
            Location newLoc = owner.getLocation();
            if (!(newLoc.x() == loc.x() && newLoc.y() == loc.y() && newLoc.z() == loc.z())) {
                owner.sendMessage(Component.text("You moved! Teleport cancelled").color(NamedTextColor.RED));
                return;
            }
            double price = calculateTeleportCost(owner);
            //charge
            if (!Account.getAccount(owner).withdrawAndSave(price)){
                owner.sendMessage(Component.text("You do not have enough " + CurrencyUtils.getCurrencyNamePlural() + " to use your homes!").color(NamedTextColor.RED));
                owner.sendMessage(Component.text("You need " + CurrencyUtils.format(price) + " to teleport, consider lowering the amount of homes you have to reduce the price").color(NamedTextColor.RED));
                return;
            }
            teleport();
            owner.sendActionBar(Component.text("Teleported to home " + name + " for " + CurrencyUtils.format(price)).color(NamedTextColor.GREEN));
            owner.playSound(owner.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
        }, countdown * 20L);
    }

    /**
     * Uncache a players homes
     * @param player the player, wow
     */
    public static void uncache(Player player){
        cachedHomes.remove(player);
    }

    /**delete a home*/
    public void delete() {
        cachedHomes.computeIfAbsent(owner, ignored -> new HashMap<>());
        cachedHomes.get(owner).remove(this.name);
        //async from here
        DataStorage.getInstance().runInDatabase(connection -> {
            String sql;
            try {
                sql = DataStorage.getInstance().loadSqlFile("sql/homes/deleteHome.sql");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, this.owner.getUniqueId().toString());
            ps.setString(2, this.name);
            ps.executeUpdate();
        });
    }

    public static double calculateTeleportCost(Player player){
        int homes = getHomesCount(player);
        double base = ComprehensiveEconomy.getPlugin().getConfig().getDouble("homes.teleport.base-cost");
        double perHome = ComprehensiveEconomy.getPlugin().getConfig().getDouble("homes.teleport.cost-per-home");
        return base + homes * perHome;
    }

    public static double calculateSetCost(Player player){
        int homes = getHomesCount(player);
        double base = ComprehensiveEconomy.getPlugin().getConfig().getDouble("homes.set.base-cost");
        double perHome = ComprehensiveEconomy.getPlugin().getConfig().getDouble("homes.set.cost-per-home");
        return base + homes * perHome;
    }

    public static int getHomesCount(Player player){
        cachedHomes.computeIfAbsent(player, (ignored) -> new HashMap<>());
        return cachedHomes.get(player).size();
    }

}
