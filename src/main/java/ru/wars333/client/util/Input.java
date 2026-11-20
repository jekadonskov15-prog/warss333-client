package ru.wars333.client.util;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * Опрос клавиатуры и мыши напрямую через GLFW.
 * Так наши экраны не зависят от сигнатур событий Screen, которые меняются между версиями.
 */
public final class Input {
    private static final boolean[] KEYS = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private static final boolean[] MOUSE = new boolean[8];

    private Input() {}

    private static long handle() {
        return MinecraftClient.getInstance().getWindow().getHandle();
    }

    public static boolean keyDown(int key) {
        if (key < GLFW.GLFW_KEY_SPACE || key > GLFW.GLFW_KEY_LAST) return false;
        return GLFW.glfwGetKey(handle(), key) == GLFW.GLFW_PRESS;
    }

    /** true один раз в момент нажатия */
    public static boolean keyEdge(int key) {
        if (key < GLFW.GLFW_KEY_SPACE || key > GLFW.GLFW_KEY_LAST) return false;
        boolean down = keyDown(key);
        boolean edge = down && !KEYS[key];
        KEYS[key] = down;
        return edge;
    }

    public static boolean mouseDown(int button) {
        return GLFW.glfwGetMouseButton(handle(), button) == GLFW.GLFW_PRESS;
    }

    public static boolean mouseEdge(int button) {
        boolean down = mouseDown(button);
        boolean edge = down && !MOUSE[button];
        MOUSE[button] = down;
        return edge;
    }

    /** запоминает текущее состояние, чтобы удержанная клавиша не считалась новым нажатием */
    public static void sync() {
        long h = handle();
        for (int k = GLFW.GLFW_KEY_SPACE; k <= GLFW.GLFW_KEY_LAST; k++) {
            KEYS[k] = GLFW.glfwGetKey(h, k) == GLFW.GLFW_PRESS;
        }
        for (int b = 0; b < MOUSE.length; b++) {
            MOUSE[b] = GLFW.glfwGetMouseButton(h, b) == GLFW.GLFW_PRESS;
        }
    }

    public static String keyName(int key) {
        if (key < 0) return "—";
        switch (key) {
            case GLFW.GLFW_KEY_SPACE: return "SPACE";
            case GLFW.GLFW_KEY_ENTER: return "ENTER";
            case GLFW.GLFW_KEY_TAB: return "TAB";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RALT";
            case GLFW.GLFW_KEY_UP: return "UP";
            case GLFW.GLFW_KEY_DOWN: return "DOWN";
            case GLFW.GLFW_KEY_LEFT: return "LEFT";
            case GLFW.GLFW_KEY_RIGHT: return "RIGHT";
            default:
        }
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        String n = GLFW.glfwGetKeyName(key, 0);
        if (n != null) return n.toUpperCase();
        return "K" + key;
    }
}
