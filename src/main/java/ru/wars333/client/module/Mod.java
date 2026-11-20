package ru.wars333.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import ru.wars333.client.setting.BoolSetting;
import ru.wars333.client.setting.ModeSetting;
import ru.wars333.client.setting.NumSetting;
import ru.wars333.client.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public abstract class Mod {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public final String name;
    public final Category category;
    public final String desc;

    public boolean enabled;
    /** GLFW key code, -1 = не назначено */
    public int key = -1;
    /** true: кнопка вызывает onKey(), false: кнопка включает/выключает модуль */
    public boolean actionKey;
    /** не показывать в списке модулей на HUD */
    public boolean hidden;
    /** 0 = рисуется под HUD (небо), 1 = сам HUD */
    public int hudLayer = 1;
    /** плавная анимация для списка модулей */
    public float anim;

    public final List<Setting> settings = new ArrayList<>();

    protected Mod(String name, Category category, String desc, boolean defaultEnabled) {
        this.name = name;
        this.category = category;
        this.desc = desc;
        this.enabled = defaultEnabled;
    }

    protected BoolSetting bool(String name, boolean def) {
        BoolSetting s = new BoolSetting(name, def);
        settings.add(s);
        return s;
    }

    protected NumSetting num(String name, double def, double min, double max, double step) {
        NumSetting s = new NumSetting(name, def, min, max, step);
        settings.add(s);
        return s;
    }

    protected ModeSetting mode(String name, int def, String... modes) {
        ModeSetting s = new ModeSetting(name, def, modes);
        settings.add(s);
        return s;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable();
        else onDisable();
    }

    public void updateAnim() {
        anim += ((enabled ? 1f : 0f) - anim) * 0.18f;
        if (Math.abs(anim - (enabled ? 1f : 0f)) < 0.01f) anim = enabled ? 1f : 0f;
    }

    public void onEnable() {}

    public void onDisable() {}

    public void onTick() {}

    public void onKey() {}

    public void onHud(DrawContext ctx) {}

    /** серый суффикс в списке модулей */
    public String suffix() {
        return null;
    }
}
