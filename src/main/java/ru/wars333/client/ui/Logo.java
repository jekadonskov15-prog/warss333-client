package ru.wars333.client.ui;

import net.minecraft.client.gui.DrawContext;
import ru.wars333.client.util.Colors;
import ru.wars333.client.util.Theme;

/** Пиксельный логотип WARS333 (рисуется прямоугольниками, поэтому масштабируется без текстур). */
public final class Logo {
    private static final String[][] GLYPHS = {
            {"X...X", "X...X", "X...X", "X.X.X", "X.X.X", "XX.XX", "X...X"}, // W
            {".XXX.", "X...X", "X...X", "XXXXX", "X...X", "X...X", "X...X"}, // A
            {"XXXX.", "X...X", "X...X", "XXXX.", "X.X..", "X..X.", "X...X"}, // R
            {".XXXX", "X....", "X....", ".XXX.", "....X", "....X", "XXXX."}, // S
            {"XXXXX", "....X", "...X.", "..XX.", "....X", "X...X", ".XXX."}, // 3
    };
    private static final int[] SEQ = {0, 1, 2, 3, 4, 4, 4};
    private static final int CELLS = SEQ.length * 6 - 1;

    private Logo() {}

    public static int width(int cell) {
        return CELLS * cell;
    }

    public static int height(int cell) {
        return 7 * cell;
    }

    public static void draw(DrawContext c, int centerX, int y, int cell) {
        int startX = centerX - width(cell) / 2;
        for (int pass = 0; pass < 2; pass++) {
            for (int gi = 0; gi < SEQ.length; gi++) {
                String[] g = GLYPHS[SEQ[gi]];
                for (int row = 0; row < 7; row++) {
                    for (int col = 0; col < 5; col++) {
                        if (g[row].charAt(col) != 'X') continue;
                        int cellIdx = gi * 6 + col;
                        int x = startX + cellIdx * cell;
                        int yy = y + row * cell;
                        if (pass == 0) {
                            c.fill(x + 2, yy + 3, x + cell + 1, yy + cell + 2, 0x66000000);
                        } else {
                            int color = Theme.grad(cellIdx / (double) CELLS);
                            c.fill(x, yy, x + cell - 1, yy + cell - 1, color);
                            c.fill(x, yy, x + cell - 1, yy + 1, Colors.argb(255, Colors.lerp(color, 0xFFFFFF, 0.45f)));
                        }
                    }
                }
            }
        }
    }
}
