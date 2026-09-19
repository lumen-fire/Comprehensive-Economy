package me.lumen.comprehensiveEconomy.economy;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.utils.DataStorage;
import me.lumen.comprehensiveEconomy.utils.DatabaseDebounce;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.*;
import org.jspecify.annotations.NonNull;

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Account {
    private static final String SQLSave;
    private static final String SQLQuery;
    private static final String SQLBalTop;
    static {
        try {
            SQLSave = DataStorage.getInstance().loadSqlFile("sql/economy/save.sql");
            SQLQuery = DataStorage.getInstance().loadSqlFile("sql/economy/cache.sql");
            SQLBalTop = DataStorage.getInstance().loadSqlFile("sql/economy/baltop.sql");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    /**HashMap to get accounts by UUID*/
    private static final HashMap<UUID, Account> accounts = new HashMap<>();
    private final UUID uuid;
    private double balance;
    @ApiStatus.Internal
    private Account(UUID uuid, double balance){
        this.uuid = uuid;
        this.balance = balance;
    }

    public double getBalance(){
        return this.balance;
    }

    public void setBalanceAndSave(double balance){
        this.balance = balance;
        saveChanges();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public @CanIgnoreReturnValue boolean withdrawAndSave(double amount){
        if (!withdraw(amount)) return false;
        saveChanges();
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public @CanIgnoreReturnValue boolean withdraw(double amount){
        if (this.balance - amount < 0){
            return false;
        }
        this.balance -= amount;
        return true;
    }

    public @CanIgnoreReturnValue boolean deposit(double amount){
        if (this.balance + amount < 0){
            return false;
        }
        this.balance += amount;
        return true;
    }

    public @CanIgnoreReturnValue boolean depositAndSave(double amount){
        if (!deposit(amount)) return false;
        saveChanges();
        return true;
    }

    public boolean has(double amount) {
        return this.balance >= amount;
    }


    public @CanIgnoreReturnValue boolean sell(@NonNull ItemStack item, @Range(from = 0, to = 99) int amount) {
        Optional<Double> basePrice = CurrencyUtils.getSellPrice(item.getType());
        if (basePrice.isPresent()) {
            double price = basePrice.get() * amount;
            //check not negative or anything
            if (this.balance + price <= 0){
                return false;
            }
            //check they have enough
            if (item.getAmount() < amount){
                return false;
            }
            item.subtract(amount);
            this.balance += price;
            return true;
        }
        return false;
    }

    public @CanIgnoreReturnValue boolean sell(@NonNull ItemStack item) {
        return sell(item, 1);
    }

    public UUID uuid(){
        return this.uuid;
    }


    public static void unCache(@NonNull Player player){
        accounts.remove(player.getUniqueId());
    }
    /**Gets an account, or returns a new one if not found*/
    public static @NotNull Account getAccount(@NonNull Player player){
        Account account = accounts.get(player.getUniqueId());
        if (account == null){
            throw new NullPointerException("Account is null, either called too early or player is not online");
        }
        return account;
    }

    /**
     * Saves changes to database
     */
    public void saveChanges(){
        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLSave);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setDouble(2, balance);
            preparedStatement.executeUpdate();
        });
    }

    /**
     * Saves changes to database and runs the runnable once saved
     */
    public void saveChangesAnd(Runnable runnable){
        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLSave);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setDouble(2, balance);
            preparedStatement.executeUpdate();
            runnable.run();
        });
    }

    public static void cache(@NonNull Player player){
        UUID uuid = player.getUniqueId();
        //skip if already cached
        if (accounts.containsKey(uuid) || accounts.get(uuid) != null) return;
        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLQuery);
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            //only cache if found
            if (resultSet.next()) {
                //skip if already cached
                if (accounts.containsKey(uuid) || accounts.get(uuid) != null) return;
                double bits = resultSet.getDouble("bits");
                accounts.put(uuid, new Account(uuid, bits));
            } else {
                Account account = new Account(uuid, CurrencyUtils.getStarterCurrency());
                //skip if already cached
                if (accounts.containsKey(uuid)) {
                    return;
                }
                account.saveChanges();
                //if not found put default and save
                accounts.putIfAbsent(uuid, account);
            }
        });
    }

    /**
     * Sets the player's cached account to an account with the specified balance.
     * Useful for making sure an account is set
     */
    public static void setCache(@NonNull Player player, double value){
        UUID uuid = player.getUniqueId();
        accounts.put(uuid, new Account(uuid, value));
    }



    /**
     * Transfer money to another account.
     * @param amount the amount of money
     * @param toAccount the account to transfer to
     */
    public @CanIgnoreReturnValue boolean transferAndSave(double amount, @NonNull Account toAccount){
        if (!transfer(amount, toAccount)) return false;
        //save
        //might not work if crashes halfway, but whatever
        saveChanges();
        toAccount.saveChanges();
        return true;
    }

    public @CanIgnoreReturnValue boolean transfer(@Range(from = 0, to = Integer.MAX_VALUE) double amount, @NonNull Account toAccount){
        if (this.balance - amount < 0){
            return false;
        }
        if (amount < 0) throw new IllegalArgumentException("Amount is negative");
        this.balance -= amount;
        toAccount.balance += amount;
        return true;
    }



    /**
     * This is async and involves db lookup, so could take a while to execute
     * @param player the offline player you want, do not use it on a player that is online, it simply wastes time
     * @param consumer the logic to run once the offline player's account has been got - do be warned it is async, you will have to set it back to sync, don't forget to use saveChanges()
     */
    public static void executeOnOfflinePlayer(@NonNull OfflinePlayer player, Consumer<Account> consumer){
        UUID uuid = player.getUniqueId();
        //just pass on to uuid method
        executeOnOfflinePlayer(uuid, consumer);
    }

    /**
     * This is async and involves db lookup, so could take a while to execute
     * @param uuid, the uuid, avoid setting it to a uuid that no player has - though you could, it's just pointless
     * @param consumer the logic to run once the offline player's account has been got - do be warned it is async, you will have to set it back to sync, don't forget saveChanges()
     */
    public static void executeOnOfflinePlayer(UUID uuid, Consumer<Account> consumer) {
        DataStorage.getInstance().runInDatabase(connection -> {
            //query to get current balance
            PreparedStatement get = connection.prepareStatement(SQLQuery);
            get.setString(1, uuid.toString());
            ResultSet resultSet = get.executeQuery();
            double bits = resultSet.getDouble("bits");
            //run whatever logic they want
            consumer.accept(new Account(uuid, bits));
        });
    }

    private static List<TopAccount> balTops = new ArrayList<>();
    public static void refreshTopBalances(){
        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLBalTop);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<TopAccount> newBalTops = new ArrayList<>();

            while (resultSet.next()) {
                String uuidString = resultSet.getString("uuid");
                UUID uuid = UUID.fromString(uuidString);

                String name = Bukkit.getOfflinePlayer(uuid).getName();
                double balance = resultSet.getDouble("bits");

                newBalTops.add(new TopAccount(name, balance, uuid));
            }

            balTops = List.copyOf(newBalTops);
        });
    }

    public static void refreshTopBalancesAnd(Runnable runnable) {
        DataStorage.getInstance().runInDatabase(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLBalTop);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<TopAccount> newBalTops = new ArrayList<>();

            while (resultSet.next()) {
                String uuidString = resultSet.getString("uuid");
                UUID uuid = UUID.fromString(uuidString);

                String name = Bukkit.getOfflinePlayer(uuid).getName();
                double balance = resultSet.getDouble("bits");

                newBalTops.add(new TopAccount(name, balance, uuid));
            }

            balTops = List.copyOf(newBalTops);

            runnable.run();
        });
    }

    /**
     * Gets the top account at this index
     * @param index the index - 0 indexed
     * @return the {@link TopAccount} at this index - 0 indexed
     */
    public static @Nullable TopAccount getNumber(@Range(from = 0, to = 9) int index){
        try {
            return balTops.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static @NonNull @Unmodifiable List<TopAccount> getTopAccounts(){
        //new list to prevent changing
        return List.copyOf(balTops);
    }

    /**
     * @param name the name of the player
     * @param consumer what to do with the retrieved account
     * @param onErr gets called on an error
     */
    public static void executeOnName(String name, Consumer<Account> consumer, Consumer<String> onErr){
        Bukkit.getScheduler().runTaskAsynchronously(ComprehensiveEconomy.getPlugin(), () -> {
            Player online = Bukkit.getPlayer(name);
            if (online != null) {
                Account account = Account.getAccount(online);
                consumer.accept(account);
                return;
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            if (!offlinePlayer.hasPlayedBefore()){
                onErr.accept("Player " + name + " has never played before.");
                return;
            }
            executeOnOfflinePlayer(offlinePlayer, consumer);
        });
    }


    private static final NamespacedKey CHEQUE_AMOUNT = new NamespacedKey(ComprehensiveEconomy.getPlugin(), "amount");
    public ItemStack withdrawToCheque(double amount){
        ItemStack cheque = ItemStack.of(Material.PAPER);
        ItemMeta meta = cheque.getItemMeta();
        meta.itemName(Component.text("Cheque for " + CurrencyUtils.format(amount)).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        meta.lore(List.of(Component.text("Right click to get " + CurrencyUtils.format(amount)).color(NamedTextColor.GOLD)));
        meta.getPersistentDataContainer().set(CHEQUE_AMOUNT, PersistentDataType.DOUBLE, amount);
        cheque.setItemMeta(meta);
        return cheque;
    }

    public static void tryCacheCheque(@NonNull PlayerInteractEvent event){
        if (!event.hasItem()) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        event.setCancelled(true);
        ItemStack cheque = event.getItem();
        assert cheque != null;

        Double amount = cheque.getPersistentDataContainer().get(CHEQUE_AMOUNT, PersistentDataType.DOUBLE);
        if (amount == null || amount <= 0) return;

        Player player = event.getPlayer();
        Account account = Account.getAccount(player);

        account.depositAndSave(amount);
        cheque.setAmount(0);

        player.sendMessage(Component.text("Cached cheque for " + CurrencyUtils.format(amount)).color(NamedTextColor.GOLD));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    /**
     * Not an actual account, cannot be written to, is only as recent as the last baltop refresh, only stores a name, uuid and balance
     */
    public static class TopAccount{
        private final String name;
        private final double balance;
        private final UUID uuid;
        private TopAccount(String name, double balance, UUID uuid){
            this.name = name;
            this.balance = balance;
            this.uuid = uuid;
        }

        public String getName() {
            if (name == null){
                return "player " + uuid;
            }
            return name;
        }

        public double getBalance() {
            return balance;
        }

        public UUID getUuid() {
            return uuid;
        }

        public OfflinePlayer createOfflinePlayer(){
            return Bukkit.getOfflinePlayer(uuid);
        }
    }
    /**Utils for integrating into Vault*/
    public static class VaultUtils {
        private static final UnsupportedOperationException ERROR_OFFLINE_PLAYER = new UnsupportedOperationException("Cannot get balance of an offline player, as Vault does not currently support async lookups so would freeze the thread!");
        public static final UnsupportedOperationException BANKS_NOT_SUPPORTED = new UnsupportedOperationException("Banks are not supported - yet!");
        public static <T> T getForPotentiallyOffline(@NonNull OfflinePlayer player, Function<Account, T> function) {
            if (player.isOnline()){
                //basic instance check
                if (player instanceof Player online){
                    Account account = getAccount(online);
                    return function.apply(account);
                }
                //could be not an instance of it, but online, unlikely though
                Player online = player.getPlayer();
                //can't be null, already checked with .isOnline()
                assert online != null;
                return function.apply(getAccount(online));
            }
            throw ERROR_OFFLINE_PLAYER;
        }

        public static <T> T getForName(String name, Function<Account, T> function){
            Player player = Bukkit.getPlayerExact(name);
            if (player == null){
                throw ERROR_OFFLINE_PLAYER;
            }
            return function.apply(getAccount(player));
        }

        /**
         * @param player the player
         * @param amount the amount to add, but you can just make it negative to subtract
         * @return an economy response to use
         */
        @Contract("_, _ -> new")
        public static @NonNull EconomyResponse transactionForPotentiallyOffline(@NonNull OfflinePlayer player, double amount) {
            //if they are online it is pretty easy
            if (player.isOnline()){
                //basic instance check
                if (player instanceof Player online){
                    Account account = getAccount(online);
                    //debounce
                    if (DatabaseDebounce.blocked(online)) {
                        online.sendActionBar(DatabaseDebounce.getTooFastMessage(online));
                        return new EconomyResponse(0, account.balance, EconomyResponse.ResponseType.FAILURE, "Too fast!");
                    }
                    DatabaseDebounce.update(online, 100);

                    account.balance = account.balance + amount;
                    account.saveChanges();
                    return new EconomyResponse(amount, account.balance, EconomyResponse.ResponseType.SUCCESS, null);
                }
                //could be not an instance of it, but online, unlikely though
                Player online = player.getPlayer();
                //can't be null, already checked with .isOnline()
                assert online != null;
                Account account = getAccount(online);
                //debounce
                if (DatabaseDebounce.blocked(online)) {
                    online.sendActionBar(DatabaseDebounce.getTooFastMessage(online));
                    return new EconomyResponse(0, account.balance, EconomyResponse.ResponseType.FAILURE, "Too fast!");
                }
                DatabaseDebounce.update(online, 100);

                account.balance = account.balance + amount;
                account.saveChanges();
                return new EconomyResponse(amount, account.balance, EconomyResponse.ResponseType.SUCCESS, null);
            } else {
                executeOnOfflinePlayer(player, account -> {
                    account.balance += amount;
                    account.saveChanges();
                });
                return new EconomyResponse(amount, -1, EconomyResponse.ResponseType.FAILURE, "Transaction still undergoing... don't worry, balance will still be changed...");
            }
        }

        @Contract("_, _ -> new")
        public static @NonNull EconomyResponse transactionForName(String name, double amount) {
            Player player = Bukkit.getPlayerExact(name);
            if (player != null){
                Account account = getAccount(player);
                //debounce
                if (DatabaseDebounce.blocked(player)) {
                    player.sendActionBar(DatabaseDebounce.getTooFastMessage(player));
                    return new EconomyResponse(0, account.balance, EconomyResponse.ResponseType.FAILURE, "Too fast!");
                }
                DatabaseDebounce.update(player, 100);

                account.balance = account.balance + amount;
                account.saveChanges();
                return new EconomyResponse(amount, account.balance, EconomyResponse.ResponseType.SUCCESS, null);
            }
            //not sure if this is async safe, so it could be laggy, but this shouldn't be called anyway
            UUID uuid = Bukkit.getPlayerUniqueId(name);
            executeOnOfflinePlayer(uuid, account -> {
                account.balance += amount;
                account.saveChanges();
            });
            return new EconomyResponse(amount, -1, EconomyResponse.ResponseType.FAILURE, "Transaction still undergoing... don't worry, balance will still be changed...");
        }
    }
}
