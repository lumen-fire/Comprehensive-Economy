package me.lumen.comprehensiveEconomy.bounty;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.economy.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BountyTests {
    private ServerMock server;
    private PlayerMock wanted;
    private PlayerMock issuer;
    private PlayerMock killer;
    @BeforeEach
    public void setup(){
        server = MockBukkit.mock();
        MockBukkit.load(ComprehensiveEconomy.class);
        wanted = server.addPlayer();
        issuer = server.addPlayer();
        killer = server.addPlayer();
        Account.setCache(wanted, 0);
        Account.setCache(issuer, 0);
        Account.setCache(killer, 0);
    }
    @AfterEach
    public void tearDown(){
        MockBukkit.unmock();
    }

    @Test
    public void savingTest() throws ExecutionException, InterruptedException, TimeoutException {
        Bounty bounty = Bounty.getOrCreateBounty(wanted);
        CompletableFuture<Void> future = new CompletableFuture<>();
        bounty.setBountyAnd(server.addPlayer(), 10, () -> future.complete(null));
        future.get(2L, TimeUnit.SECONDS);
    }

    @Test
    public void setValueTest(){
        Bounty bounty = Bounty.getOrCreateBounty(wanted);
        bounty.setBounty(issuer, 10);

        Bounty fresh = Bounty.getOrCreateBounty(wanted);
        Assertions.assertEquals(10, fresh.getSetBounty(issuer));
        Assertions.assertEquals(10, fresh.getTotal());
    }

    @Test
    public void claimTest(){
        Bounty bounty = Bounty.getOrCreateBounty(wanted);
        bounty.setBounty(issuer, 20);
        bounty.claim(killer);

        Assertions.assertNull(Bounty.getBounty(wanted));
        Assertions.assertEquals(20, Account.getAccount(killer).getBalance());
    }

    @Test
    public void addTest(){
        Bounty bounty = Bounty.getOrCreateBounty(wanted);
        bounty.addToBounty(issuer, 10);

        Assertions.assertEquals(10, bounty.getSetBounty(issuer));
        Assertions.assertEquals(10, bounty.getTotal());
    }

    @Test
    public void subtractTestToMuch(){
        Bounty bounty = Bounty.getOrCreateBounty(wanted);
        bounty.subtractFromBounty(issuer, 10);

        Assertions.assertEquals(0, bounty.getSetBounty(issuer));
        Assertions.assertEquals(0, bounty.getTotal());
    }

    @Test
    public void subtractTest(){
        Bounty bounty = Bounty.getOrCreateBounty(wanted);
        bounty.setBounty(issuer, 20);
        bounty.subtractFromBounty(issuer, 10);

        Assertions.assertEquals(10,bounty.getSetBounty(issuer));
        Assertions.assertEquals(10, bounty.getTotal());
    }

    @Test
    public void removeTest() throws ExecutionException, InterruptedException, TimeoutException {
        Bounty bounty = Bounty.getOrCreateBounty(wanted);

        CompletableFuture<Void> future = new CompletableFuture<>();
        bounty.setBountyAnd(issuer, 10, () -> {
            try {
                bounty.removeBounty(issuer);
                Assertions.assertNull(Bounty.getBounty(issuer));
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        future.get(5, TimeUnit.SECONDS);
    }

}
