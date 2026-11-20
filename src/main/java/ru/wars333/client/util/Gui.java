package ru.wars333.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Небольшой набор помощников для рисования UI клиента. */
public final class Gui {
    private static final long START = System.nanoTime();

    /** события мыши текущего кадра (заполняются в beginFrame) */
    public static boolean click, rclick, mclick, down;

    private Gui() {}

    public static void beginFrame() {
        click = Input.mouseEdge(0);
        rclick = Input.mouseEdge(1);
        mclick = Input.mouseEdge(2);
        down = Input.mouseDown(0);
    }

    public static float time() {
        return (System.nanoTime() - START) / 1_000_000_000f;
    }

    private static TextRenderer tr() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static int tw(String s) {
        return tr().getWidth(s);
    }

    public static boolean hover(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    public static void rect(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x, y, x + w, y + h, color);
    }

    public static void border(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x, y, x + w, y + 1, color);
        c.fill(x, y + h - 1, x + w, y + h, color);
        c.fill(x, y, x + 1, y + h, color);
        c.fill(x + w - 1, y, x + w, y + h, color);
    }

    /** тёмная стеклянная панель с акцентной линией сверху */
    public static void panel(DrawContext c, int x, int y, int w, int h, double phase) {
        c.fill(x, y, x + w, y + h, 0xB40a0614);
        border(c, x, y, w, h, 0x40FFFFFF);
        int acc = Theme.grad(phase);
        c.fill(x, y, x + w, y + 1, acc);
    }

    public static void text(DrawContext c, String s, int x, int y, int color) {
        c.drawTextWithShadow(tr(), s, x, y, color);
    }

    public static void ctext(DrawContext c, String s, int cx, int y, int color) {
        c.drawTextWithShadow(tr(), s, cx - tw(s) / 2, y, color);
    }

    public static void gradientText(DrawContext c, String s, int x, int y, double phase) {
        int cx = x;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            text(c, ch, cx, y, Theme.grad(phase + i * 0.07));
            cx += tw(ch);
        }
    }

    /** рисует кнопку, возвращает true в кадр нажатия */
    public static boolean button(DrawContext c, int x, int y, int w, int h, String label, int mx, int my) {
        boolean hov = hover(mx, my, x, y, w, h);
        c.fill(x, y, x + w, y + h, hov ? 0xDD1d1138 : 0xAA0c0818);
        int acc = Theme.grad(x * 0.003 + y * 0.002);
        border(c, x, y, w, h, hov ? acc : Colors.argb(90, acc));
        if (hov) c.fill(x, y + h - 2, x + w, y + h, acc);
        ctext(c, label, x + w / 2, y + (h - 8) / 2, hov ? 0xFFFFFFFF : 0xFFC9C2E0);
        return hov && click;
    }
}
