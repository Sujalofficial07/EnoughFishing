package com.mef.enoughfishing;

import com.mef.enoughfishing.command.CommandMEF;
import com.mef.enoughfishing.core.Config;
import com.mef.enoughfishing.core.FishingTracker;
import com.mef.enoughfishing.events.FishingEventHandler;
import com.mef.enoughfishing.events.RenderEventHandler;
import com.mef.enoughfishing.events.RenderWorldHandler;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
    modid                    = EnoughFishing.MOD_ID,
    name                     = EnoughFishing.MOD_NAME,
    version                  = EnoughFishing.VERSION,
    clientSideOnly           = true,
    acceptedMinecraftVersions = "[1.8.9]"
)
public class EnoughFishing {

    public static final String MOD_ID   = "enoughfishing";
    public static final String MOD_NAME = "Enough Fishing";
    public static final String VERSION  = "1.0.0";
    public static final Logger LOG      = LogManager.getLogger(MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static EnoughFishing INSTANCE;

    private Config         config;
    private FishingTracker tracker;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config  = new Config(event.getSuggestedConfigurationFile());
        tracker = FishingTracker.INSTANCE;
        LOG.info("[{}] Pre-init complete.", MOD_NAME);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new FishingEventHandler());
        MinecraftForge.EVENT_BUS.register(new RenderEventHandler());
        MinecraftForge.EVENT_BUS.register(new RenderWorldHandler()); // ← floating bobber timer
        ClientCommandHandler.instance.registerCommand(new CommandMEF());
        LOG.info("[{}] Initialized — Happy fishing!", MOD_NAME);
    }

    public Config         getConfig()  { return config;  }
    public FishingTracker getTracker() { return tracker; }
}
