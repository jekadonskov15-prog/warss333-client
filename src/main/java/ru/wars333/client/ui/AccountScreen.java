package ru.wars333.client.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.wars333.client.util.AccountManager;
import ru.wars333.client.util.Gui;
import ru.wars333.client.util.Input;

import java.util.ArrayList;
import java.util.List;

/** Смена аккаунта: список сохранённых ников, добавление, случайный ник. */
public class AccountScreen extends Screen {
    private static final int ROWS = 6;

    private final Screen parent;
    private TextFieldWidget field;
    private int scroll;
    private String status = "";
    private int px, py;
    private final int pw = 240, ph = 214;

    public AccountScreen(Screen parent) {
        super(Text.literal("Аккаунты"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Input.sync();
        px = (width - pw) / 2;
        py = Math.max(4, (height - ph) / 2);
        field = new TextFieldWidget(this.textRenderer, px + 8, py + 166, pw - 16, 18, Text.literal("Ник"));
        field.setMaxLength(16);
        field.setPlaceholder(Text.literal("Ник (3-16: a-z, 0-9, _)"));
        addDrawableChild(field);
        setInitialFocus(field);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int max = Math.max(0, AccountManager.accounts.size() - ROWS);
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(vertical)));
        return true;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        Gui.beginFrame();
        MenuBackground.draw(ctx, width, height, mx, my, false);
        Gui.panel(ctx, px, py, pw, ph, 0.3);

        Gui.ctext(ctx, "АККАУНТЫ", width / 2, py + 8, 0xFFFFFFFF);
        Gui.ctext(ctx, "Сейчас: " + AccountManager.current(), width / 2, py + 22, 0xFFB9B2D6);

        List<String> list = new ArrayList<>(AccountManager.accounts);
        String toRemove = null;
        for (int i = 0; i < ROWS; i++) {
            int idx = i + scroll;
            int y = py + 38 + i * 20;
            if (idx >= list.size()) {
                ctx.fill(px + 8, y, px + pw - 8, y + 18, 0x50000000);
                continue;
            }
            String name = list.get(idx);
            boolean cur = name.equals(AccountManager.current());
            if (Gui.button(ctx, px + 8, y, pw - 32, 18, (cur ? "● " : "") + name, mx, my)) {
                status = AccountManager.login(name) ? "Вошли как " + name : "Не удалось войти";
            }
            if (Gui.button(ctx, px + pw - 22, y, 14, 18, "x", mx, my)) toRemove = name;
        }
        if (toRemove != null) AccountManager.remove(toRemove);

        field.render(ctx, mx, my, delta);
        if (field.isFocused() && Input.keyEdge(GLFW.GLFW_KEY_ENTER)) addFromField();

        int bw = (pw - 16 - 8) / 3;
        int by = py + 188;
        if (Gui.button(ctx, px + 8, by, bw, 18, "Добавить", mx, my)) addFromField();
        if (Gui.button(ctx, px + 8 + bw + 4, by, bw, 18, "Случайный", mx, my)) field.setText(AccountManager.randomName());
        if (Gui.button(ctx, px + 8 + (bw + 4) * 2, by, bw, 18, "Назад", mx, my)) close();

        if (!status.isEmpty()) Gui.ctext(ctx, status, width / 2, py + ph + 6, 0xFFB9B2D6);
        Gui.ctext(ctx, "Офлайн-ники: работают на серверах без проверки лицензии", width / 2, py + ph + 18, 0xFF8F88AE);
    }

    private void addFromField() {
        String n = field.getText().trim();
        if (AccountManager.add(n)) {
            field.setText("");
            status = "Добавлен: " + n;
        } else {
            status = "Некорректный или уже есть такой ник";
        }
    }
}
