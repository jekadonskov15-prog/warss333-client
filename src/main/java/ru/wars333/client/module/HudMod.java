package ru.wars333.client.module;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import ru.wars333.client.util.Colors;
import ru.wars333.client.util.Gui;
import ru.wars333.client.util.Theme;

import ru.wars333.client.setting.BoolSetting;
import ru.wars333.client.setting.ModeSetting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** HUD в стиле визуальных клиентов: ватермарк, список модулей, координаты, эффекты. */
public class HudMod extends Mod {
    public final BoolSetting watermark = bool("Ватермарк", true);
    public final BoolSetting list = bool("Список модулей", true);
    public final BoolSetting coords = bool("Координаты и BPS", true);
    public final BoolSetting effects = bool("Эффекты", true);
    public final ModeSetting color = mode("Цвет", 0, "Фиолетовый", "Розовый", "Аква", "Радуга", "По теме");

    private double lastX, lastZ;
    private boolean hasLast;
    private float bps;

    public HudMod() {
        super("HUD", Category.VISUAL, "Ватермарк, список модулей, координаты, эффекты", true);
        hidden = true;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        double x = mc.player.getX(), z = mc.player.getZ();
        if (hasLast) {
            float cur = (float) (Math.hypot(x - lastX, z - lastZ) * 20.0);
            bps = bps * 0.8f + cur * 0.2f;
        }
        lastX = x;
        lastZ = z;
        hasLast = true;
    }

    @Override
    public void onHud(DrawContext ctx) {
        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        int top = 6;
        if (watermark.value) top = drawWatermark(ctx) + 4;
        if (effects.value) drawEffects(ctx, top);
        if (coords.value) drawCoords(ctx, h);
        if (list.value) drawList(ctx, w);
    }

    private int drawWatermark(DrawContext ctx) {
        int ping = 0;
        if (mc.getNetworkHandler() != null && mc.player != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (e != null) ping = e.getLatency();
        }
        String brand = "wars333";
        String info = "  |  " + mc.getCurrentFps() + " fps  |  " + ping + " ms  |  " + mc.getSession().getUsername();
        int x = 6, y = 6;
        int w = Gui.tw(brand) + Gui.tw(info) + 12;
        int h = 16;
        Gui.panel(ctx, x, y, w, h, 0);
        Gui.gradientText(ctx, brand, x + 6, y + 4, 0);
        Gui.text(ctx, info, x + 6 + Gui.tw(brand), y + 4, 0xFFB9B2D6);
        for (int i = 0; i < w; i += 2) {
            ctx.fill(x + i, y + h - 1, Math.min(x + i + 2, x + w), y + h, Theme.grad(i / (double) w));
        }
        return y + h;
    }

    private void drawEffects(DrawContext ctx, int startY) {
        if (mc.player == null) return;
        int y = startY;
        for (StatusEffectInstance e : mc.player.getStatusEffects()) {
            String name = e.getEffectType().value().getName().getString();
            if (e.getAmplifier() > 0) name += " " + roman(e.getAmplifier() + 1);
            String time = e.isInfinite() ? "∞" : formatTicks(e.getDuration());
            int rowW = Gui.tw(name) + Gui.tw(time) + 18;
            boolean good = e.getEffectType().value().isBeneficial();
            int dot = good ? 0xFF5CFF9A : 0xFFFF5C6E;
            ctx.fill(6, y, 6 + rowW, y + 12, 0x960a0614);
            ctx.fill(6, y, 8, y + 12, dot);
            Gui.text(ctx, name, 12, y + 2, 0xFFFFFFFF);
            Gui.text(ctx, time, 6 + rowW - Gui.tw(time) - 4, y + 2, e.getDuration() < 200 && !e.isInfinite() ? 0xFFFF8A8A : 0xFFB9B2D6);
            y += 13;
        }
    }

    private void drawCoords(DrawContext ctx, int screenH) {
        if (mc.player == null) return;
        String pos = String.format("%d  %d  %d", (int) Math.floor(mc.player.getX()), (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()));
        String speed = String.format(java.util.Locale.ROOT, "%.1f", bps);
        int x = 6;
        int y = screenH - 28;
        int w = Math.max(Gui.tw("XYZ  " + pos), Gui.tw("BPS  " + speed)) + 12;
        ctx.fill(x, y, x + w, y + 22, 0x960a0614);
        ctx.fill(x, y, x + 2, y + 22, Theme.grad(0.4));
        Gui.text(ctx, "XYZ", x + 6, y + 3, Theme.grad(0.2));
        Gui.text(ctx, pos, x + 6 + Gui.tw("XYZ  "), y + 3, 0xFFFFFFFF);
        Gui.text(ctx, "BPS", x + 6, y + 12, Theme.grad(0.5));
        Gui.text(ctx, speed, x + 6 + Gui.tw("BPS  "), y + 12, 0xFFFFFFFF);
    }

    private void drawList(DrawContext ctx, int screenW) {
        List<Mod> shown = new ArrayList<>();
        for (Mod m : ModuleManager.MODS) {
            if (!m.hidden && m.anim > 0.02f) shown.add(m);
        }
        shown.sort(Comparator.comparingInt((Mod m) -> -fullWidth(m)));
        int y = 6;
        int i = 0;
        for (Mod m : shown) {
            int fw = fullWidth(m);
            int rowW = fw + 10;
            int slide = (int) ((1f - m.anim) * (rowW + 8));
            int x = screenW - rowW - 4 + slide;
            int acc = Theme.grad(i * 0.09);
            ctx.fill(x, y, x + rowW, y + 12, Colors.argb((int) (150 * m.anim), 0x0a0614));
            ctx.fill(x + rowW - 2, y, x + rowW, y + 12, acc);
            Gui.text(ctx, m.name, x + 4, y + 2, acc);
            String suf = m.suffix();
            if (suf != null) Gui.text(ctx, " " + suf, x + 4 + Gui.tw(m.name), y + 2, 0xFF8F88AE);
            y += 12;
            i++;
        }
    }

    private int fullWidth(Mod m) {
        String suf = m.suffix();
        return Gui.tw(m.name) + (suf == null ? 0 : Gui.tw(" " + suf));
    }

    private static String roman(int n) {
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n < r.length ? r[n] : String.valueOf(n);
    }

    private static String formatTicks(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
