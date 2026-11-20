package ru.wars333.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import ru.wars333.client.mixin.MinecraftClientAccessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/** Список офлайн-аккаунтов (ники) и переключение сессии. */
public final class AccountManager {
    public static final List<String> accounts = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AccountManager() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("wars333_accounts.json");
    }

    public static boolean valid(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{3,16}");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try {
            JsonObject o = JsonParser.parseString(Files.readString(f)).getAsJsonObject();
            accounts.clear();
            if (o.has("accounts")) {
                for (var e : o.getAsJsonArray("accounts")) accounts.add(e.getAsString());
            }
        } catch (IOException | RuntimeException ignored) {
        }
    }

    public static void save() {
        JsonObject o = new JsonObject();
        JsonArray a = new JsonArray();
        for (String s : accounts) a.add(s);
        o.add("accounts", a);
        try {
            Files.writeString(file(), GSON.toJson(o));
        } catch (IOException ignored) {
        }
    }

    public static boolean add(String name) {
        if (!valid(name) || accounts.contains(name)) return false;
        accounts.add(name);
        save();
        return true;
    }

    public static void remove(String name) {
        accounts.remove(name);
        save();
    }

    public static String current() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    public static String randomName() {
        String[] a = {"Dark", "Nova", "Star", "Void", "Lunar", "Neon", "Astro", "Frost"};
        Random r = new Random();
        return a[r.nextInt(a.length)] + "_" + (100 + r.nextInt(900)) + (char) ('a' + r.nextInt(26));
    }

    /** офлайн-вход: работает только на серверах с отключённой онлайн-проверкой */
    public static boolean login(String name) {
        if (!valid(name)) return false;
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        Session session = new Session(name, uuid, "0", Optional.empty(), Optional.empty());
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).wars333$setSession(session);
        return true;
    }
}
