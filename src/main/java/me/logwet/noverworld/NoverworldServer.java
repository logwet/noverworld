package me.logwet.noverworld;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.Level;

public class NoverworldServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Noverworld.log(Level.INFO, "Using Noverworld v" + Noverworld.VERSION + " by logwet!");

        Noverworld.commonConfigHandler();
    }
}
