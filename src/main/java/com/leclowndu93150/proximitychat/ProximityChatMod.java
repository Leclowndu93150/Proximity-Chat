package com.leclowndu93150.proximitychat;

import com.leclowndu93150.proximitychat.chat.ChatManager;
import com.leclowndu93150.proximitychat.command.ModCommands;
import com.leclowndu93150.proximitychat.config.ModConfig;
import com.leclowndu93150.proximitychat.data.DataManager;
import com.leclowndu93150.proximitychat.party.PartyManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ProximityChatMod.MOD_ID)
public class ProximityChatMod {
    public static final String MOD_ID = "proximitychat";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ProximityChatMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Proximity Chat Mod initializing");
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, ModConfig.SERVER_SPEC);
        modEventBus.addListener(this::setup);
        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("Proximity Chat Mod initialized");
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Proximity Chat Mod setup starting");

        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("fakename")) {
                LOGGER.info("Fakename integration enabled");
            }

            if (ModList.get().isLoaded("chatheads")) {
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
    public void onChat(ServerChatEvent event) {
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
