package me.lumen.comprehensiveEconomy.homes;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.economy.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;

public class HomeTests {
    private ServerMock server;
    @BeforeEach
    void setup() {
        server = MockBukkit.mock();
        MockBukkit.load(ComprehensiveEconomy.class);
    }
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void getHomesTest(){
        PlayerMock playerMock = server.addPlayer();
        Account.setCache(playerMock, 0);
        Home home = new Home(playerMock, playerMock.getLocation(), "test");
        home.save();
        ArrayList<String> ids = new ArrayList<>();
        for (Home looped : Home.getHomes(playerMock)){
            ids.add(looped.toString());
        }
        Assertions.assertTrue(ids.contains("test"));
    }
}
