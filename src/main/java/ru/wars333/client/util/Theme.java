package ru.wars333.client.util;

import ru.wars333.client.module.AmbienceMod;
import ru.wars333.client.module.HudMod;
import ru.wars333.client.module.ModuleManager;

/** Акцентные цвета клиента (градиент, который плавно переливается). */
public final class Theme {
    private Theme() {}

    private static final int[][] PAIRS = {
            {0x7a3cff, 0xd24bff},
            {0xff4fa3, 0xff9a5c},
            {0x1ee6c8, 0x4a8cff},
    };

    /** t — смещение по фазе (разные элементы HUD получают разные оттенки). Возвращает ARGB с альфой FF. */
    public static int grad(double t) {
        HudMod hud = ModuleManager.get(HudMod.class);
        int mode = hud == null ? 0 : hud.color.index;
        double time = Gui.time();
        if (mode == 3) {
            float h = (float) (((time * 0.08 + t) % 1.0 + 1.0) % 1.0);
            return 0xFF000000 | Colors.hsb(h, 0.65f, 1f);
        }
        int c1, c2;
        if (mode == 4) {
            AmbienceMod.Palette p = AmbienceMod.current();
            c1 = p.aurA();
            c2 = p.aurB();
        } else {
            c1 = PAIRS[mode][0];
            c2 = PAIRS[mode][1];
        }
        float k = (float) (0.5 + 0.5 * Math.sin(time * 1.2 + t * 3.0));
        return 0xFF000000 | Colors.lerp(c1, c2, k);
    }
}
