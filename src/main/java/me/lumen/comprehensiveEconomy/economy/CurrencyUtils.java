package me.lumen.comprehensiveEconomy.economy;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CurrencyUtils {
    public static String getName(){
        return ComprehensiveEconomy.getPlugin().getConfig().getString("currency.name");
    }
    public static String getCurrencyNamePlural(){
        return ComprehensiveEconomy.getPlugin().getConfig().getString("currency.name-plural");
    }
    public static double getStarterCurrency(){
        return ComprehensiveEconomy.getPlugin().getConfig().getDouble("currency.starting-currency");
    }

    public static @NonNull String format(double amount) {
        if (amount == 1){
            String format = ComprehensiveEconomy.getPlugin().getConfig().getString("currency.format-singular");
            if (format == null) return amount + " " + getName();
            return format;
        }
        NumberFormat formatter = NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.SHORT);
        formatter.setMaximumFractionDigits(decimalPlaces());
        String format = ComprehensiveEconomy.getPlugin().getConfig().getString("currency.format");
        if (amount < 1000){
            if (format == null) return round(amount) + " " + getCurrencyNamePlural();
            return format.replace("$s", Double.toString(round(amount)));
        }
        if (format == null) return formatter.format(amount) + " " + getCurrencyNamePlural();
        return format.replace("$s", formatter.format(amount));
    }

    public static double round(double value){
        int places = ComprehensiveEconomy.getPlugin().getConfig().getInt("currency.rounding-places");
        if (places < 0){
            return value;
        }
        return Math.round(value * Math.pow(10, places)) / Math.pow(10, places);
    }

    public static int decimalPlaces(){
        return ComprehensiveEconomy.getPlugin().getConfig().getInt("currency.rounding-places");
    }

    /**
     * Gets the base sell price of a material
     */
    public static Optional<Double> getSellPrice(@NonNull Material material){
        String key = material.getKey().asString();
        if (isNumber(ComprehensiveEconomy.getPlugin().getConfig(), "sell." + key + ".price")){
            return Optional.of(ComprehensiveEconomy.getPlugin().getConfig().getDouble("sell." + key + ".price"));
        }
        else return Optional.empty();
    }

    public static Optional<Double> getTotalPrice(@NonNull ItemStack item){
        return getSellPrice(item.getType(), item.getAmount());
    }

    public static @NonNull Map<String, Double> getSellPrices(){
        Map<String, Double> sellPrices = new HashMap<>();
        ConfigurationSection prices = ComprehensiveEconomy.getPlugin().getConfig().getConfigurationSection("sell");
        if (prices != null){
            for (String key : prices.getKeys(false)) {
                if (isNumber(prices, key + ".price")) {
                    sellPrices.put(key, prices.getDouble(key + ".price"));
                }
            }
        }
        return sellPrices;
    }

    public static boolean isUnSellable(@NonNull Material material){
        String key = material.getKey().asString();
        return !isNumber(ComprehensiveEconomy.getPlugin().getConfig(), "sell." + key + ".price");
    }


    public static Optional<Double> getSellPrice(@NonNull Material material, int amount){
        String key = material.getKey().asString();
        if (isNumber(ComprehensiveEconomy.getPlugin().getConfig(), "sell." + key + ".price")){
            return Optional.of(ComprehensiveEconomy.getPlugin().getConfig().getDouble("sell." + key + ".price") * amount);
        }
        else return Optional.empty();
    }

    private static boolean isNumber(@NonNull MemoryConfiguration configuration, String key){
        return configuration.isDouble(key) || configuration.isInt(key) || configuration.isLong(key);
    }

    private static boolean isNumber(@NonNull ConfigurationSection configuration, String key){
        return configuration.isDouble(key) || configuration.isInt(key) || configuration.isLong(key);
    }

    private static boolean isWholeNumber(@NonNull ConfigurationSection configuration, String key){
        return configuration.isInt(key) || configuration.isLong(key);
    }

    public static @NonNull Map<String, Integer> getSlots(){
        Map<String, Integer> slots = new HashMap<>();
        ConfigurationSection items = ComprehensiveEconomy.getPlugin().getConfig().getConfigurationSection("sell");
        if (items != null) for (String key : items.getKeys(false)) {
            if (isWholeNumber(items, key + ".slot")) {
                slots.put(key, items.getInt(key + ".slot"));
            }
        }
        return slots;
    }

    public static @Nullable String getName(@NonNull Material material){
        String key = material.getKey().asString();
        return ComprehensiveEconomy.getPlugin().getConfig().getString("sell." + key + ".name");
    }

    public static @Nullable String getName(@NonNull String materialName){
        return ComprehensiveEconomy.getPlugin().getConfig().getString("sell." + materialName + ".name");
    }
}
