package ru.wars333.client.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.wars333.client.module.Category;
import ru.wars333.client.module.Mod;
import ru.wars333.client.module.ModuleManager;
import ru.wars333.client.setting.BoolSetting;
import ru.wars333.client.setting.ModeSetting;
import ru.wars333.client.setting.NumSetting;
import ru.wars333.client.setting.Setting;
import ru.wars333.client.util.Colors;
import ru.wars333.client.util.Gui;
import ru.wars333.client.util.Input;
import ru.wars333.client.util.Theme;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Меню модулей (Right Shift). ЛКМ — вкл/выкл, ПКМ — настройки, СКМ — назначить кнопку.
 * Ввод читается напрямую через GLFW (см. Input), поэтому не зависит от сигнатур событий Screen.
 */
public class ClickGuiScreen extends Screen {
    private static final int PW = 130, HH = 18, RH = 15;
    private static final int[] PX = new int[Category.values().length];
    private static final int[] PY = new int[Category.values().length];
    private static boolean placed;
    private static final Set<Mod> OPEN = new HashSet<>();

    private int dragPanel = -1, dragDx, dragDy;
    private Mod binding;
    private NumSetting dragNum;
    private int dragNumX, dragNumW;

    public ClickGuiScreen() {
        super(Text.literal("wars333"));
        if (!placed) {
            for (int i = 0; i < PX.length; i++) {
                PX[i] = 14 + i * (PW + 10);
                PY[i] = 14;
            }
            placed = true;
        }
    }

    @Override
    protected void init() {
        Input.sync();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return binding == null;
    }

    @Override
    public void close() {
        ModuleManager.save();
        super.close();
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        Gui.beginFrame();

        if (binding != null) {
            handleBinding();
        } else if (Input.keyEdge(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            close();
            return;
        }

        MenuBackground.draw(ctx, width, height, mx, my, false);
        ctx.fillGradient(0, 0, width, height, 0x55000000, 0x88000000);

        Category[] cats = Category.values();
        String tip = null;
        for (int i = 0; i < cats.length; i++) {
            List<Mod> mods = ModuleManager.in(cats[i]);
            int x = PX[i], y = PY[i];

            int totalH = HH;
            for (Mod m : mods) {
                totalH += RH;
                if (OPEN.contains(m)) totalH += m.settings.size() * RH;
            }

            // перетаскивание панели
            if (dragPanel == i) {
                if (Gui.down) {
                    PX[i] = Math.max(0, Math.min(width - PW, mx - dragDx));
                    PY[i] = Math.max(0, Math.min(height - HH, my - dragDy));
                    x = PX[i];
                    y = PY[i];
                } else {
                    dragPanel = -1;
                }
            } else if (dragPanel == -1 && Gui.click && Gui.hover(mx, my, x, y, PW, HH)) {
                dragPanel = i;
                dragDx = mx - x;
                dragDy = my - y;
            }

            ctx.fill(x, y, x + PW, y + totalH, 0xB40a0614);
            Gui.border(ctx, x, y, PW, totalH, 0x40FFFFFF);
            ctx.fill(x, y, x + PW, y + HH, 0xE0140c28);
            ctx.fill(x, y + HH - 1, x + PW, y + HH, Theme.grad(i * 0.3));
            Gui.gradientText(ctx, cats[i].title.toUpperCase(Locale.ROOT), x + 8, y + 5, i * 0.3);

            int ry = y + HH;
            for (Mod m : mods) {
                boolean hov = Gui.hover(mx, my, x, ry, PW, RH) && dragPanel == -1;
                int acc = Theme.grad(ry * 0.004 + i * 0.2);
                if (m.enabled) ctx.fill(x + 1, ry, x + PW - 1, ry + RH, Colors.argb(70, acc));
                if (hov) ctx.fill(x + 1, ry, x + PW - 1, ry + RH, 0x30FFFFFF);
                if (m.enabled) ctx.fill(x + 1, ry, x + 3, ry + RH, acc);
                Gui.text(ctx, m.name, x + 8, ry + 4, m.enabled ? acc : 0xFFB8B0D0);

                String keyText = binding == m ? "..." : (m.key >= 0 ? "[" + Input.keyName(m.key) + "]" : "");
                if (!keyText.isEmpty()) Gui.text(ctx, keyText, x + PW - Gui.tw(keyText) - 6, ry + 4, 0xFF8F88AE);

                if (hov) {
                    tip = m.desc;
                    if (Gui.click) m.toggle();
                    if (Gui.rclick && !m.settings.isEmpty()) {
                        if (!OPEN.remove(m)) OPEN.add(m);
                    }
                    if (Gui.mclick) binding = m;
                }
                ry += RH;

                if (OPEN.contains(m)) {
                    for (Setting s : m.settings) {
                        drawSetting(ctx, s, x, ry, mx, my);
                        ry += RH;
                    }
                }
            }
        }

        if (dragNum != null) {
            if (Gui.down) dragNum.setFromRatio((mx - dragNumX) / (double) dragNumW);
            else dragNum = null;
        }

        if (tip != null) Gui.ctext(ctx, tip, width / 2, height - 24, 0xFFFFFFFF);
        Gui.ctext(ctx, "ЛКМ — вкл/выкл   ПКМ — настройки   СКМ — кнопка (DEL — сбросить)   RShift — закрыть", width / 2, height - 12, 0xFF8F88AE);
    }

    private void drawSetting(DrawContext ctx, Setting s, int x, int y, int mx, int my) {
        int w = PW;
        boolean hov = Gui.hover(mx, my, x, y, w, RH) && dragPanel == -1;
        int acc = Theme.grad(y * 0.004);
        ctx.fill(x + 1, y, x + w - 1, y + RH, 0xC0080510);
        if (hov) ctx.fill(x + 1, y, x + w - 1, y + RH, 0x20FFFFFF);

        if (s instanceof BoolSetting b) {
            Gui.text(ctx, s.name, x + 10, y + 4, 0xFFB8B0D0);
            int bx = x + w - 24, by = y + 3;
            ctx.fill(bx, by, bx + 16, by + RH - 6, b.value ? Colors.argb(255, acc) : 0xFF2a2440);
            int knob = b.value ? bx + 9 : bx + 1;
            ctx.fill(knob, by + 1, knob + 6, by + RH - 7, 0xFFFFFFFF);
            if (hov && Gui.click) b.value = !b.value;
        } else if (s instanceof NumSetting n) {
            int fillW = (int) ((w - 2) * n.ratio());
            ctx.fill(x + 1, y + RH - 3, x + 1 + fillW, y + RH - 1, acc);
            Gui.text(ctx, s.name, x + 10, y + 3, 0xFFB8B0D0);
            String v = n.step >= 1 ? String.valueOf((int) n.value) : String.format(Locale.ROOT, "%.1f", n.value);
            Gui.text(ctx, v, x + w - Gui.tw(v) - 6, y + 3, 0xFFFFFFFF);
            if (hov && Gui.click) {
                dragNum = n;
                dragNumX = x + 1;
                dragNumW = w - 2;
            }
        } else if (s instanceof ModeSetting m) {
            Gui.text(ctx, s.name, x + 10, y + 4, 0xFFB8B0D0);
            String v = m.get();
            Gui.text(ctx, v, x + w - Gui.tw(v) - 6, y + 4, acc);
            if (hov && Gui.click) m.next();
            if (hov && Gui.rclick) m.prev();
        }
    }

    private void handleBinding() {
        for (int k = GLFW.GLFW_KEY_SPACE; k <= GLFW.GLFW_KEY_LAST; k++) {
            if (Input.keyEdge(k)) {
                if (k == GLFW.GLFW_KEY_ESCAPE || k == GLFW.GLFW_KEY_DELETE || k == GLFW.GLFW_KEY_BACKSPACE) binding.key = -1;
                else binding.key = k;
                binding = null;
                return;
            }
        }
    }
}
