package com.leclowndu93150.proximitychat.data;

import com.leclowndu93150.proximitychat.party.PartyManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leclowndu93150.proximitychat.ProximityChatMod;
import java.nio.file.*;
import java.io.*;
import java.nio.file.attribute.FileTime;

public class DataManager {
    private static final String MOD_DATA_DIR = "proximitychat";
    private static MinecraftServer server;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init(MinecraftServer server) {
        DataManager.server = server;
        createDirectories();
    }

    public static Path getModDirectory() {
        return server.getWorldPath(LevelResource.ROOT).resolve(MOD_DATA_DIR);
    }

    public static void createDirectories() {
        try {
            Files.createDirectories(getModDirectory());
            Files.createDirectories(getModDirectory().resolve("parties"));
            Files.createDirectories(getModDirectory().resolve("players"));
        } catch (IOException e) {
            ProximityChatMod.LOGGER.error("Failed to create directories: ", e);
        }
    }

    public static void saveJson(String subDir, String fileName, Object data) {
        try {
            Path path = getModDirectory().resolve(subDir).resolve(fileName + ".json");
            String json = GSON.toJson(data);
            Files.writeString(path, json);
        } catch (IOException e) {
            ProximityChatMod.LOGGER.error("Failed to save file {}/{}.json: ", subDir, fileName, e);
        }
    }

    public static <T> T loadJson(String subDir, String fileName, Class<T> type) {
        try {
            Path path = getModDirectory().resolve(subDir).resolve(fileName + ".json");
            if (Files.exists(path)) {
                String json = Files.readString(path);
                return GSON.fromJson(json, type);
            }
        } catch (IOException e) {
            ProximityChatMod.LOGGER.error("Failed to load file {}/{}.json: ", subDir, fileName, e);
        }
        return null;
    }

    public static void saveAll() {
        try {
            ProximityChatMod.LOGGER.info("Saving all proximity chat data...");

            PartyManager.saveAll();

            Files.setLastModifiedTime(
                    getModDirectory().resolve("parties"),
                    FileTime.fromMillis(System.currentTimeMillis())
            );
            Files.setLastModifiedTime(
                    getModDirectory().resolve("players"),
                    FileTime.fromMillis(System.currentTimeMillis())
            );

            ProximityChatMod.LOGGER.info("All proximity chat data saved successfully!");
        } catch (IOException e) {
            ProximityChatMod.LOGGER.error("Failed to save data: ", e);
        }
    }
}