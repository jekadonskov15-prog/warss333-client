package ru.wars333.client.util;

public final class Colors {
    private Colors() {}

    public static int rgb(int r, int g, int b) {
        return ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
    }

    public static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /** ARGB из альфы и RGB (старшие байты rgb игнорируются) */
    public static int argb(int a, int rgb) {
        return (clamp255(a) << 24) | (rgb & 0xFFFFFF);
    }

    public static int lerp(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * t);
        int g = (int) (((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * t);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return rgb(r, g, b);
    }

    public static int hsb(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        float c = v * s;
        float x = c * (1 - Math.abs((h * 6) % 2 - 1));
        float m = v - c;
        float r, g, b;
        switch ((int) (h * 6)) {
            case 0: r = c; g = x; b = 0; break;
            case 1: r = x; g = c; b = 0; break;
            case 2: r = 0; g = c; b = x; break;
            case 3: r = 0; g = x; b = c; break;
            case 4: r = x; g = 0; b = c; break;
            default: r = c; g = 0; b = x; break;
        }
        return rgb((int) ((r + m) * 255), (int) ((g + m) * 255), (int) ((b + m) * 255));
    }
}
