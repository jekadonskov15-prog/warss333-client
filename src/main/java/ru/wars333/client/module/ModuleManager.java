package ru.wars333.client.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import ru.wars333.client.setting.Setting;
import ru.wars333.client.ui.ClickGuiScreen;
import ru.wars333.client.util.Input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    public static final List<Mod> MODS = new ArrayList<>();
    private static final Map<Class<? extends Mod>, Mod> BY_CLASS = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean started;

    private ModuleManager() {}

    public static void init() {
        add(new HudMod());
        add(new AmbienceMod());
        add(new ParticlesMod());
        add(new OptimizerMod());
        add(new AutoSwapMod());
        add(new AutoActionMod());
        load();
    }

    private static void add(Mod m) {
        MODS.add(m);
        BY_CLASS.put(m.getClass(), m);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Mod> T get(Class<T> type) {
        return (T) BY_CLASS.get(type);
    }

    public static List<Mod> in(Category category) {
        List<Mod> out = new ArrayList<>();
        for (Mod m : MODS) if (m.category == category) out.add(m);
        return out;
    }

    public static void tick(MinecraftClient mc) {
        if (!started) {
            started = true;
            for (Mod m : MODS) if (m.enabled) m.onEnable();
        }

        boolean noScreen = mc.currentScreen == null;

        if (mc.player == null || mc.world == null) {
            if (!noScreen) Input.sync();
            return;
        }

        if (noScreen && Input.keyEdge(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            mc.setScreen(new ClickGuiScreen());
            return;
        }
        if (!noScreen) Input.sync();

        for (Mod m : MODS) {
            if (m.enabled) m.onTick();
        }

        if (noScreen) {
            for (Mod m : MODS) {
                if (m.key >= 0 && Input.keyEdge(m.key)) {
                    if (m.actionKey) {
                        if (m.enabled) m.onKey();
                    } else {
                        m.toggle();
                    }
                }
            }
        }
    }

    public static void hud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;
        if (mc.player == null || mc.world == null) return;
        for (int layer = 0; layer <= 1; layer++) {
            for (Mod m : MODS) {
                if (m.hudLayer != layer) continue;
                m.updateAnim();
                if (m.enabled) m.onHud(ctx);
            }
        }
    }

    public static void shutdown() {
        save();
        for (Mod m : MODS) if (m.enabled) m.onDisable();
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("wars333.json");
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (Mod m : MODS) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.enabled);
            o.addProperty("key", m.key);
            JsonObject s = new JsonObject();
            for (Setting st : m.settings) s.add(st.name, st.save());
            o.add("settings", s);
            root.add(m.name, o);
        }
        try {
            Files.writeString(file(), GSON.toJson(root));
        } catch (IOException ignored) {
        }
    }

    private static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(f)).getAsJsonObject();
            for (Mod m : MODS) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("enabled")) m.enabled = o.get("enabled").getAsBoolean();
                if (o.has("key")) m.key = o.get("key").getAsInt();
                if (o.has("settings")) {
                    JsonObject s = o.getAsJsonObject("settings");
                    for (Setting st : m.settings) {
                        if (!s.has(st.name)) continue;
                        try {
                            st.load(s.get(st.name));
                        } catch (RuntimeException ignored) {
                        }
                    }
                }
            }
        } catch (IOException | RuntimeException ignored) {
        }
    }
}
