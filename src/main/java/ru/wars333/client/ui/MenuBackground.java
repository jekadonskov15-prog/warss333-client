package ru.wars333.client.ui;

import net.minecraft.client.gui.DrawContext;
import ru.wars333.client.util.Colors;
import ru.wars333.client.util.Gui;

import java.util.Random;

/** Анимированный космический фон для меню: туманности, звёзды, млечный путь, планета, падающая звезда. */
public final class MenuBackground {
    private static final int N = 170;
    private static final float[] SX = new float[N], SY = new float[N], SD = new float[N], SP = new float[N];
    private static final int[] SC = new int[N];

    private static final int B = 220;
    private static final float[] BU = new float[B], BO = new float[B], BB = new float[B], BM = new float[B];

    static {
        Random r = new Random(333);
        int[] cols = {0xFFFFFF, 0xC9A0FF, 0x9ad0ff, 0xFFC4EC};
        for (int i = 0; i < N; i++) {
            SX[i] = r.nextFloat();
            SY[i] = r.nextFloat();
            SD[i] = 0.2f + 0.8f * r.nextFloat();
            SP[i] = r.nextFloat() * 6.28f;
            SC[i] = cols[r.nextInt(cols.length)];
        }
        for (int i = 0; i < B; i++) {
            BU[i] = r.nextFloat();
            BO[i] = (float) r.nextGaussian() * 0.06f;
            BB[i] = 0.2f + 0.8f * r.nextFloat();
            BM[i] = r.nextFloat();
        }
    }

    private MenuBackground() {}

    public static void draw(DrawContext c, int w, int h, int mx, int my, boolean full) {
        float t = Gui.time();
        c.fillGradient(0, 0, w, h, 0xFF04020c, 0xFF140a30);

        blob(c, w * 0.25f + (float) Math.sin(t * 0.12) * w * 0.05f, h * 0.35f, w * 0.45f, h * 0.35f, 0x7a2cff, 14);
        blob(c, w * 0.72f, h * 0.55f + (float) Math.cos(t * 0.1) * h * 0.04f, w * 0.4f, h * 0.3f, 0xff3fa0, 11);
        blob(c, w * 0.5f, h * 0.82f, w * 0.6f, h * 0.25f, 0x1e7bff, 10);

        // млечный путь по диагонали
        for (int i = 0; i < B; i++) {
            float u = BU[i];
            float bx = u * w;
            float by = h * 0.85f - u * h * 0.7f;
            float x = bx + BO[i] * h * 0.45f;
            float y = by + BO[i] * h * 0.55f;
            float twk = 0.6f + 0.4f * (float) Math.sin(t * 1.3 + i);
            int a = (int) (110 * BB[i] * twk);
            int rgb = Colors.lerp(0xff5fd0, 0x5b7bff, BM[i]);
            c.fill((int) x, (int) y, (int) x + 1, (int) y + 1, Colors.argb(a, rgb));
        }

        float ox = (mx - w * 0.5f) * 0.02f, oy = (my - h * 0.5f) * 0.02f;
        for (int i = 0; i < N; i++) {
            int x = (int) (SX[i] * w - ox * SD[i]);
            int y = (int) (SY[i] * h - oy * SD[i]);
            float twk = 0.55f + 0.45f * (float) Math.sin(t * (0.8 + SD[i]) + SP[i]);
            int a = (int) (255 * SD[i] * twk);
            int size = SD[i] > 0.85f ? 2 : 1;
            c.fill(x, y, x + size, y + size, Colors.argb(a, SC[i]));
        }

        if (full) {
            planet(c, w, h, t);
            shootingStar(c, w, h, t);
        }

        // виньетка
        c.fillGradient(0, 0, w, h / 6, 0x88000000, 0x00000000);
        c.fillGradient(0, h - h / 5, w, h, 0x00000000, 0xA0000000);
    }

    private static void blob(DrawContext c, float cx, float cy, float rx, float ry, int rgb, int alpha) {
        for (int layer = 0; layer < 3; layer++) {
            float k = 1f - layer * 0.28f;
            float lrx = rx * k, lry = ry * k;
            for (int dy = (int) -lry; dy < lry; dy += 3) {
                float f = 1f - (dy * dy) / (lry * lry);
                if (f <= 0) continue;
                int half = (int) (lrx * Math.sqrt(f));
                int y = (int) (cy + dy);
                c.fill((int) cx - half, y, (int) cx + half, y + 3, Colors.argb(alpha, rgb));
            }
        }
    }

    private static void planet(DrawContext c, int w, int h, float t) {
        int cx = (int) (w * 0.84f), cy = (int) (h * 0.76f);
        int r = Math.max(18, (int) (h * 0.16f));
        blob(c, cx, cy, r * 1.9f, r * 1.9f, 0xff7ad9, 6);
        for (int dy = -r; dy <= r; dy++) {
            int hw = (int) Math.sqrt((double) r * r - (double) dy * dy);
            float band = 0.5f + 0.5f * (float) Math.sin(dy * 0.35 + t * 0.05);
            int base = Colors.lerp(0x5b2cd9, 0xff7ad9, band);
            int y = cy + dy;
            int left = (int) (-hw * 0.4f), right = (int) (hw * 0.4f);
            c.fill(cx - hw, y, cx + left, y + 1, Colors.argb(255, Colors.lerp(base, 0x000000, 0.2f)));
            c.fill(cx + left, y, cx + right, y + 1, Colors.argb(255, base));
            c.fill(cx + right, y, cx + hw, y + 1, Colors.argb(255, Colors.lerp(base, 0x000000, 0.55f)));
        }
    }

    private static void shootingStar(DrawContext c, int w, int h, float t) {
        float phase = (t % 5f) / 5f;
        if (phase > 0.2f) return;
        float p = phase / 0.2f;
        float hx = w * (0.95f - p * 0.65f);
        float hy = h * (0.04f + p * 0.4f);
        for (int k = 0; k < 16; k++) {
            int x = (int) (hx + k * 3.2f);
            int y = (int) (hy - k * 2.0f);
            int a = (int) (255 * (1f - k / 16f) * (1f - p * 0.6f));
            c.fill(x, y, x + 2, y + 2, Colors.argb(a, k < 3 ? 0xFFFFFF : 0xB9A8FF));
        }
    }
}
