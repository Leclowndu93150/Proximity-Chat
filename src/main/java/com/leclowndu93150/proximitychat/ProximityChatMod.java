package com.leclowndu93150.proximitychat;

import com.leclowndu93150.proximitychat.chat.ChatManager;
import com.leclowndu93150.proximitychat.command.ModCommands;
import com.leclowndu93150.proximitychat.config.ModConfig;
import com.leclowndu93150.proximitychat.data.DataManager;
import com.leclowndu93150.proximitychat.party.PartyManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ProximityChatMod.MOD_ID)
public class ProximityChatMod {
    public static final String MOD_ID = "proximitychat";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ProximityChatMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Proximity Chat Mod initialized");
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Proximity Chat Mod setup starting");

        event.enqueueWork(() -> {
            if (ModConfig.COMMON.enableFakenameIntegration.get() && ModList.get().isLoaded("fakename")) {
                LOGGER.info("Fakename integration enabled");
            }

            if (ModConfig.COMMON.enableChatHeadsIntegration.get() && ModList.get().isLoaded("chatheads")) {
                LOGGER.info("Chat Heads integration enabled");
            }
        });

        LOGGER.info("Proximity Chat Mod setup completed");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Server started, initializing data managers");
        DataManager.init(event.getServer());

        if (ModConfig.COMMON.persistPartiesOnRestart.get()) {
            PartyManager.init(event.getServer());
        } else {
            LOGGER.info("Party persistence disabled, starting with fresh party data");
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Server stopping, saving data");
        if (ModConfig.COMMON.persistPartiesOnRestart.get()) {
            DataManager.saveAll();
            PartyManager.saveAll();
        }
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent.Submitted event) {
        if (event.getPlayer() == null) return;

        if (event.getRawText().isEmpty()) {
            return;
        }

        event.setCanceled(true);

        try {
            if (PartyManager.isPartyChatEnabled(event.getPlayer().getUUID())) {
                PartyManager.getParty(event.getPlayer().getUUID()).ifPresent(party ->
                        ChatManager.sendPartyMessage(event.getPlayer(), party, event.getRawText())
                );
            } else {
                ChatManager.sendProximityMessage(event.getPlayer(), event.getRawText());
            }
        } catch (Exception e) {
            ProximityChatMod.LOGGER.error("Error processing chat message", e);
            event.setCanceled(false);
        }
    }

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        LOGGER.info("Registering commands");
        ModCommands.register(event.getDispatcher());
    }
}
