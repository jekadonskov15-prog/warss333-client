package ru.wars333.client.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import ru.wars333.client.util.Gui;
import ru.wars333.client.util.Input;

public class Wars333TitleScreen extends Screen {
    public Wars333TitleScreen() {
        super(Text.literal("wars333"));
    }

    @Override
    protected void init() {
        Input.sync();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        Gui.beginFrame();
        int w = width, h = height;
        MenuBackground.draw(ctx, w, h, mx, my, true);

        int cell = Math.max(3, Math.min(7, w / 80));
        int logoY = (int) (h * 0.14f);
        Logo.draw(ctx, w / 2, logoY, cell);
        Gui.ctext(ctx, "V I S U A L   C L I E N T   ·   1 . 2 1 . 1 1", w / 2, logoY + Logo.height(cell) + 8, 0xFF9C94C4);

        int bw = 170, bh = 22, gap = 6;
        int x = (w - bw) / 2;
        int y = (int) (h * 0.46f);

        if (Gui.button(ctx, x, y, bw, bh, "Одиночная игра", mx, my)) client.setScreen(new SelectWorldScreen(this));
        y += bh + gap;
        if (Gui.button(ctx, x, y, bw, bh, "Сетевая игра", mx, my)) client.setScreen(new MultiplayerScreen(this));
        y += bh + gap;
        if (Gui.button(ctx, x, y, bw, bh, "Аккаунты", mx, my)) client.setScreen(new AccountScreen(this));
        y += bh + gap;
        if (Gui.button(ctx, x, y, bw, bh, "Настройки", mx, my)) client.setScreen(new OptionsScreen(this, client.options));
        y += bh + gap;
        if (Gui.button(ctx, x, y, bw, bh, "Выход", mx, my)) client.scheduleStop();

        Gui.text(ctx, "wars333 · Fabric 1.21.11 · RShift — меню модулей", 6, h - 12, 0xFF8F88AE);
        String acc = "Аккаунт: " + client.getSession().getUsername();
        Gui.text(ctx, acc, w - Gui.tw(acc) - 6, h - 12, 0xFFB9B2D6);
    }
}
