package me.lumen.comprehensiveEconomy.economy;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AccountTests {
    private ServerMock server;
    private ComprehensiveEconomy comprehensiveEconomy;
    @BeforeEach
    public void setup(){
        server = MockBukkit.mock();
        comprehensiveEconomy = MockBukkit.load(ComprehensiveEconomy.class);
    }
    @AfterEach
    public void teardown(){
        MockBukkit.unmock();
    }
    @Test
    public void transactionTest() {
        //add players
        PlayerMock payer = server.addPlayer();
        PlayerMock receiver = server.addPlayer();
        //block cache
        Account.setCache(payer, 200);
        Account.setCache(receiver, 200);
        //get accounts
        Account payerAccount = Account.getAccount(payer);
        Account receiverAccount = Account.getAccount(receiver);
        //transfer
        payerAccount.transfer(100, receiverAccount);
        //check
        Assertions.assertEquals(100, payerAccount.getBalance());
        Assertions.assertEquals(300, receiverAccount.getBalance());
    }

    @Test
    public void noMoneyTest() {
        PlayerMock player = server.addPlayer();
        Account.setCache(player, 200);
        Account account = Account.getAccount(player);
        account.withdraw(account.getBalance() + 1000);
        Assertions.assertEquals(200, account.getBalance());
    }


    @Test
    public void BalTopTest() throws ExecutionException, InterruptedException {
        PlayerMock player1 = server.addPlayer();
        PlayerMock player2 = server.addPlayer();
        Account.setCache(player1, 3000);
        Account.setCache(player2, 260);
        Account account1 = Account.getAccount(player1);
        Account account2 = Account.getAccount(player2);
        CompletableFuture<Void> future = new CompletableFuture<>();
        account1.saveChangesAnd(() -> account2.saveChangesAnd(() -> Account.refreshTopBalancesAnd(() -> {
            try {
                Account.TopAccount topAccount1 = Account.getNumber(0);
                Account.TopAccount topAccount2 = Account.getNumber(1);
                //not null
                Assertions.assertNotNull(topAccount1);
                Assertions.assertNotNull(topAccount2);
                //right balances
                Assertions.assertEquals(account1.getBalance(), topAccount1.getBalance());
                Assertions.assertEquals(account2.getBalance(), topAccount2.getBalance());
                //right names
                Assertions.assertEquals(player1.getName(), topAccount1.getName());
                Assertions.assertEquals(player2.getName(), topAccount2.getName());
                //right uuid as well
                Assertions.assertEquals(player1.getUniqueId(), topAccount1.getUuid());
                Assertions.assertEquals(player2.getUniqueId(), topAccount2.getUuid());
                future.complete(null);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        })));
        future.get();
    }

    @Test
    public void sellTest(){
        PlayerMock player = server.addPlayer();
        Account.setCache(player, 200);
        Account account = Account.getAccount(player);
        comprehensiveEconomy.getConfig().set("sell.minecraft:shield.price", 20);
        ItemStack shield = ItemStack.of(Material.SHIELD, 2);
        Assertions.assertTrue(account.sell(shield, 2));
        Assertions.assertEquals(240, account.getBalance());
    }


}
