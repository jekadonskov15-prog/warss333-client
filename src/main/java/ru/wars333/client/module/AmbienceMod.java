package ru.wars333.client.module;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import ru.wars333.client.setting.BoolSetting;
import ru.wars333.client.setting.ModeSetting;
import ru.wars333.client.setting.NumSetting;
import ru.wars333.client.util.Colors;
import ru.wars333.client.util.Gui;

import java.util.Random;

/**
 * Ambience: темы неба. Цвет неба и облаков меняется через миксин,
 * а галактика (звёзды, млечный путь) и полярное сияние рисуются поверх сцены в небесных координатах.
 */
public class AmbienceMod extends Mod {
    public record Palette(int sky, int tint, int starA, int starB, int aurA, int aurB, float aurora, int bandA, int bandB) {
        static Palette lerp(Palette a, Palette b, float t) {
            return new Palette(
                    Colors.lerp(a.sky, b.sky, t), Colors.lerp(a.tint, b.tint, t),
                    Colors.lerp(a.starA, b.starA, t), Colors.lerp(a.starB, b.starB, t),
                    Colors.lerp(a.aurA, b.aurA, t), Colors.lerp(a.aurB, b.aurB, t),
                    a.aurora + (b.aurora - a.aurora) * t,
                    Colors.lerp(a.bandA, b.bandA, t), Colors.lerp(a.bandB, b.bandB, t));
        }
    }

    private static final Palette[] THEMES = {
            // Галактика
            new Palette(0x140730, 0x6a2cff, 0xFFFFFF, 0xC9A0FF, 0x7a3cff, 0xff4fd8, 0.45f, 0xff5fd0, 0x5b7bff),
            // Аврора
            new Palette(0x06192c, 0x14d9a0, 0xDFFFFA, 0x8affea, 0x2bff9a, 0x7a5cff, 1.0f, 0x35e0b0, 0x4a8cff),
            // Туманность
            new Palette(0x2a0f45, 0xff3fa0, 0xFFE0F5, 0x8fc4ff, 0xff4fb0, 0x5a8cff, 0.55f, 0xff6fc0, 0x6f9bff),
            // Закат
            new Palette(0x6b2a5e, 0xff8a3c, 0xFFE8CC, 0xFFB5A0, 0xff9a3c, 0xff4f7a, 0.0f, 0xffb070, 0xff7090),
            // Кровавая луна
            new Palette(0x2e0606, 0xff1f2f, 0xFFC8C8, 0xFF7070, 0xff2f3f, 0xff8a3c, 0.0f, 0xff4040, 0xff9050),
            // Океан
            new Palette(0x05324d, 0x14b8ff, 0xD6F7FF, 0x7fe3ff, 0x1ee6c8, 0x4a7bff, 0.5f, 0x4ae0ff, 0x6a8cff),
    };

    public final ModeSetting theme = mode("Тема", 0, "Галактика", "Аврора", "Туманность", "Закат", "Кровавая луна", "Океан", "Цикл");
    public final NumSetting cycle = num("Смена темы, сек", 30, 8, 120, 1);
    public final NumSetting skyStrength = num("Цвет неба, %", 75, 0, 100, 5);
    public final NumSetting tint = num("Тонировка экрана, %", 12, 0, 40, 1);
    public final BoolSetting stars = bool("Звёзды и галактика", true);
    public final NumSetting starCount = num("Звёзд", 500, 100, 1200, 50);
    public final NumSetting starBright = num("Яркость звёзд, %", 100, 20, 100, 5);
    public final BoolSetting aurora = bool("Полярное сияние", true);
    public final BoolSetting day = bool("Показывать днём", false);

    // --- звёздное поле (генерируется один раз) ---
    private static final int GEN = 600, BAND = 900, TOTAL = GEN + BAND;
    private static final float[] SX = new float[TOTAL], SY = new float[TOTAL], SZ = new float[TOTAL];
    private static final float[] SB = new float[TOTAL], SMIX = new float[TOTAL], SPH = new float[TOTAL], SSP = new float[TOTAL];
    private static final byte[] SSIZE = new byte[TOTAL];

    static {
        Random r = new Random(333);
        for (int i = 0; i < GEN; i++) {
            double az = r.nextDouble() * Math.PI * 2;
            double y = 0.06 + 0.94 * r.nextDouble();
            double rad = Math.sqrt(1 - y * y);
            SX[i] = (float) (Math.cos(az) * rad);
            SY[i] = (float) y;
            SZ[i] = (float) (Math.sin(az) * rad);
            SB[i] = 0.45f + 0.55f * r.nextFloat();
            double s = r.nextDouble();
            SSIZE[i] = (byte) (s < 0.86 ? 0 : s < 0.97 ? 1 : 2);
        }
        // Млечный путь: большая окружность, почти вертикальная, с гауссовым разбросом
        double[] n = normalize(0.9, 0.12, 0.4);
        double[] u = normalize(n[1] * 0 - n[2] * 1, n[2] * 0 - n[0] * 0, n[0] * 1 - n[1] * 0);
        double[] v = {n[1] * u[2] - n[2] * u[1], n[2] * u[0] - n[0] * u[2], n[0] * u[1] - n[1] * u[0]};
        for (int i = GEN; i < TOTAL; i++) {
            double th = r.nextDouble() * Math.PI * 2;
            double off = r.nextGaussian() * 0.11;
            double px = Math.cos(th) * u[0] + Math.sin(th) * v[0] + off * n[0];
            double py = Math.cos(th) * u[1] + Math.sin(th) * v[1] + off * n[1];
            double pz = Math.cos(th) * u[2] + Math.sin(th) * v[2] + off * n[2];
            double[] d = normalize(px, py, pz);
            SX[i] = (float) d[0];
            SY[i] = (float) d[1];
            SZ[i] = (float) d[2];
            float center = (float) Math.exp(-off * off / 0.012);
            SB[i] = 0.12f + 0.38f * center * r.nextFloat();
            SSIZE[i] = (byte) (r.nextDouble() < 0.06 ? 1 : 0);
        }
        for (int i = 0; i < TOTAL; i++) {
            SMIX[i] = r.nextFloat();
            SPH[i] = r.nextFloat() * 6.2831f;
            SSP[i] = 0.6f + r.nextFloat() * 2.2f;
        }
    }

    private static double[] normalize(double x, double y, double z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        if (l < 1e-9) return new double[]{0, 1, 0};
        return new double[]{x / l, y / l, z / l};
    }

    private float skyVis;

    public AmbienceMod() {
        super("Ambience", Category.VISUAL, "Небо: галактика, аврора, закат и другие темы", true);
        hudLayer = 0;
    }

    @Override
    public String suffix() {
        return theme.get();
    }

    // ------------------------------------------------------------------ палитра

    public static Palette current() {
        AmbienceMod a = ModuleManager.get(AmbienceMod.class);
        if (a == null) return THEMES[0];
        if (a.theme.index < THEMES.length) return THEMES[a.theme.index];
        float period = (float) Math.max(5, a.cycle.value);
        float t = Gui.time() / period;
        int i = ((int) Math.floor(t)) % THEMES.length;
        float f = t - (float) Math.floor(t);
        f = Math.max(0f, Math.min(1f, (f - 0.7f) / 0.3f));
        f = f * f * (3 - 2 * f);
        return Palette.lerp(THEMES[i], THEMES[(i + 1) % THEMES.length], f);
    }

    // ------------------------------------------------------------------ вызывается миксином

    /** итоговый цвет неба (ARGB) или null, если менять не надо */
    public static Integer skyColor(int vanilla) {
        AmbienceMod a = ModuleManager.get(AmbienceMod.class);
        if (a == null || !a.enabled || a.skyStrength.value <= 0) return null;
        int c = Colors.lerp(vanilla & 0xFFFFFF, current().sky(), (float) (a.skyStrength.value / 100.0));
        return 0xFF000000 | c;
    }

    public static Integer cloudColor(int vanilla) {
        AmbienceMod a = ModuleManager.get(AmbienceMod.class);
        if (a == null || !a.enabled || a.skyStrength.value <= 0) return null;
        Palette p = current();
        int c = Colors.lerp(vanilla & 0xFFFFFF, Colors.lerp(p.sky(), p.tint(), 0.35f), (float) (a.skyStrength.value / 100.0) * 0.8f);
        return 0xFF000000 | c;
    }

    // ------------------------------------------------------------------ рендер

    @Override
    public void onHud(DrawContext ctx) {
        if (mc.world == null || mc.player == null) return;
        Palette p = current();
        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        float scale = OptimizerMod.visualScale();

        if (tint.value > 0) {
            int a = (int) (tint.value * 2.55);
            ctx.fillGradient(0, 0, w, (int) (h * 0.65f), Colors.argb(a, p.tint()), Colors.argb(0, p.tint()));
            ctx.fillGradient(0, (int) (h * 0.8f), w, h, Colors.argb(0, p.tint()), Colors.argb(a / 2, p.tint()));
        }

        if (stars.value || aurora.value) renderSky(ctx, p, w, h, scale);
    }

    private void renderSky(DrawContext ctx, Palette p, int w, int h, float scale) {
        ClientWorld world = mc.world;
        if (world.getRegistryKey() != World.OVERWORLD) return;

        float target = world.isSkyVisible(mc.player.getBlockPos()) ? 1f : 0f;
        skyVis += (target - skyVis) * 0.06f;
        if (skyVis < 0.02f) return;

        long tod = world.getTimeOfDay() % 24000L;
        float night;
        if (tod >= 13000 && tod <= 23000) night = 1f;
        else if (tod > 12000 && tod < 13000) night = (tod - 12000) / 1000f;
        else if (tod > 23000) night = (24000 - tod) / 1000f;
        else night = 0f;
        float vis = Math.max(night, day.value ? 0.8f : 0f) * skyVis;
        if (world.isRaining()) vis *= 0.35f;
        if (vis < 0.02f) return;

        Camera cam = mc.gameRenderer.getCamera();
        double yaw = Math.toRadians(cam.getYaw());
        double pitch = Math.toRadians(cam.getPitch());
        double fov = mc.options.getFov().getValue();

        double cy = Math.cos(yaw), sy = Math.sin(yaw), cp = Math.cos(pitch), sp = Math.sin(pitch);
        double fx = -sy * cp, fy = -sp, fz = cy * cp;
        double rx = -cy, rz = -sy;
        double ux = -rz * fy, uy = rz * fx - rx * fz, uz = rx * fy;
        double proj = (h * 0.5) / Math.tan(Math.toRadians(fov) * 0.5);
        float time = Gui.time();
        double rot = time * 0.0042;
        double cr = Math.cos(rot), sr = Math.sin(rot);

        if (stars.value) {
            int n = (int) (starCount.value * scale);
            int g = Math.min(GEN, (int) (n * 0.4));
            int b = Math.min(BAND, n - g);
            float bright = (float) (starBright.value / 100.0);
            for (int k = 0; k < g + b; k++) {
                int i = k < g ? k : GEN + (k - g);
                double x0 = SX[i], z0 = SZ[i], y = SY[i];
                double x = x0 * cr - z0 * sr;
                double z = x0 * sr + z0 * cr;
                float ef = (float) Math.min(1.0, (y - 0.06) / 0.2);
                if (ef <= 0) continue;
                double dz = x * fx + y * fy + z * fz;
                if (dz < 0.08) continue;
                double px = (x * rx + z * rz) / dz * proj + w * 0.5;
                double py = h * 0.5 - (x * ux + y * uy + z * uz) / dz * proj;
                if (px < -2 || px > w + 2 || py < -2 || py > h + 2) continue;
                float twk = 0.75f + 0.25f * (float) Math.sin(time * SSP[i] + SPH[i]);
                int a = (int) (255 * vis * SB[i] * twk * ef * bright);
                if (a < 8) continue;
                int rgb = i < GEN ? Colors.lerp(p.starA(), p.starB(), SMIX[i]) : Colors.lerp(p.bandA(), p.bandB(), SMIX[i]);
                int ix = (int) px, iy = (int) py;
                switch (SSIZE[i]) {
                    case 0:
                        ctx.fill(ix, iy, ix + 1, iy + 1, Colors.argb(a, rgb));
                        break;
                    case 1:
                        ctx.fill(ix, iy, ix + 2, iy + 2, Colors.argb(a, rgb));
                        break;
                    default:
                        ctx.fill(ix, iy, ix + 2, iy + 2, Colors.argb(a, 0xFFFFFF));
                        int ga = a / 3;
                        ctx.fill(ix - 2, iy, ix, iy + 2, Colors.argb(ga, rgb));
                        ctx.fill(ix + 2, iy, ix + 4, iy + 2, Colors.argb(ga, rgb));
                        ctx.fill(ix, iy - 2, ix + 2, iy, Colors.argb(ga, rgb));
                        ctx.fill(ix, iy + 2, ix + 2, iy + 4, Colors.argb(ga, rgb));
                        break;
                }
            }
        }

        if (aurora.value && p.aurora() > 0.02f && scale > 0.5f) {
            renderAurora(ctx, p, w, h, vis, time, fx, fy, fz, rx, rz, ux, uy, uz, proj);
        }
    }

    private void renderAurora(DrawContext ctx, Palette p, int w, int h, float vis, float time,
                              double fx, double fy, double fz, double rx, double rz,
                              double ux, double uy, double uz, double proj) {
        final int steps = 96;
        double[] sx = new double[steps + 1], yb = new double[steps + 1], yt = new double[steps + 1];
        boolean[] ok = new boolean[steps + 1];
        for (int layer = 0; layer < 2; layer++) {
            float layerAlpha = layer == 0 ? 1f : 0.55f;
            for (int i = 0; i <= steps; i++) {
                double az = i * (Math.PI * 2 / steps) + time * 0.01 * (layer + 1);
                double wave = Math.sin(az * 3 + time * 0.5 + layer * 2) * 0.10 + Math.sin(az * 7 - time * 0.3) * 0.04;
                double e1 = 0.20 + layer * 0.12 + wave;
                double e2 = e1 + 0.42 + 0.16 * Math.sin(az * 2 - time * 0.4 + layer);
                double[] a = project(az, e1, fx, fy, fz, rx, rz, ux, uy, uz, proj, w, h);
                double[] b = project(az, e2, fx, fy, fz, rx, rz, ux, uy, uz, proj, w, h);
                ok[i] = a != null && b != null;
                if (ok[i]) {
                    sx[i] = a[0];
                    yb[i] = a[1];
                    yt[i] = b[1];
                }
            }
            for (int i = 0; i < steps; i++) {
                if (!ok[i] || !ok[i + 1]) continue;
                int x0 = (int) Math.min(sx[i], sx[i + 1]);
                int x1 = (int) Math.max(sx[i], sx[i + 1]) + 1;
                if (x1 - x0 > 60 || x1 < 0 || x0 > w) continue;
                int top = (int) ((yt[i] + yt[i + 1]) * 0.5);
                int bot = (int) ((yb[i] + yb[i + 1]) * 0.5);
                if (bot - top < 2) continue;
                float mix = 0.5f + 0.5f * (float) Math.sin(i * 0.25 + time * 0.3 + layer);
                int rgb = Colors.lerp(p.aurA(), p.aurB(), mix);
                int a = (int) (95 * vis * p.aurora() * layerAlpha * (0.6f + 0.4f * (float) Math.sin(i * 0.5 + time)));
                if (a < 4) continue;
                ctx.fillGradient(x0, top, x1, bot, Colors.argb(0, rgb), Colors.argb(a, rgb));
            }
        }
    }

    /** азимут/высота (в радианах) -> координаты на экране, либо null если точка позади камеры */
    private static double[] project(double az, double elev, double fx, double fy, double fz, double rx, double rz,
                                    double ux, double uy, double uz, double proj, int w, int h) {
        double ce = Math.cos(elev);
        double x = Math.cos(az) * ce, y = Math.sin(elev), z = Math.sin(az) * ce;
        double dz = x * fx + y * fy + z * fz;
        if (dz < 0.08) return null;
        double px = (x * rx + z * rz) / dz * proj + w * 0.5;
        double py = h * 0.5 - (x * ux + y * uy + z * uz) / dz * proj;
        return new double[]{px, py};
    }
}
