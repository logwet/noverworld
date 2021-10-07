package me.logwet.noverworld;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.SERVER)
public class NoverworldServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Noverworld.onInitialize();

        Noverworld.commonConfigHandler();
    }
}
