package me.lumen.comprehensiveEconomy.economy;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Map;
import java.util.Optional;

public class CurrencyUtilsTests {
    private ComprehensiveEconomy comprehensiveEconomy;
    @BeforeEach
    public void setup(){
        MockBukkit.mock();
        comprehensiveEconomy = MockBukkit.load(ComprehensiveEconomy.class);
    }
    @AfterEach
    public void tearDown(){
        MockBukkit.unmock();
    }

    @Test
    public void roundingTest(){
        comprehensiveEconomy.getConfig().set("currency.rounding-places", 1);
        double rounded = CurrencyUtils.round(21.58);
        Assertions.assertEquals(21.6, rounded);
    }

    @Test
    public void sellPriceTestMaterial(){
        comprehensiveEconomy.getConfig().set("sell.minecraft:diamond.price", 10);
        Optional<Double> price = CurrencyUtils.getSellPrice(Material.DIAMOND);
        Assertions.assertTrue(price.isPresent());
        Assertions.assertEquals(10, price.get());
    }

    @Test
    public void sellPriceTestItemStack(){
        comprehensiveEconomy.getConfig().set("sell.minecraft:minecart.price", 20);
        ItemStack itemStack = ItemStack.of(Material.MINECART, 2);
        Optional<Double> price = CurrencyUtils.getTotalPrice(itemStack);
        Assertions.assertTrue(price.isPresent());
        Assertions.assertEquals(40, price.get());
    }

    @Test
    public void sellPricesTest(){
        comprehensiveEconomy.getConfig().set("sell.minecraft:stone.price", 5);
        Map<String, Double> prices =  CurrencyUtils.getSellPrices();
        Assertions.assertEquals(5, prices.get("minecraft:stone"));
    }

    @Test
    public void isUnSellableTest(){
        comprehensiveEconomy.getConfig().set("sell.minecraft:stone.price", 5);
        Assertions.assertFalse(CurrencyUtils.isUnSellable(Material.STONE));
        Assertions.assertTrue(CurrencyUtils.isUnSellable(Material.ACACIA_BOAT));
    }

    @Test
    public void slotsTest(){
        comprehensiveEconomy.getConfig().set("sell.minecraft:stone.slot", 5);
        Map<String, Integer> slots = CurrencyUtils.getSlots();
        Assertions.assertEquals(5, slots.get("minecraft:stone"));
    }
}
