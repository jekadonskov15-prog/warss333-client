package ru.wars333.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.wars333.client.ui.Wars333TitleScreen;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    /** Подменяет ванильное главное меню на меню wars333. */
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen wars333$replaceTitle(Screen screen) {
        if (screen != null && screen.getClass() == TitleScreen.class) {
            return new Wars333TitleScreen();
        }
        return screen;
    }
}
