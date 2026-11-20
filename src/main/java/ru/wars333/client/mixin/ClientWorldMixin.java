package ru.wars333.client.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.wars333.client.module.AmbienceMod;

/**
 * Цвет неба и облаков для Ambience.
 * require = 0: если в этой версии сигнатура другая, миксин молча не применится и игра не упадёт
 * (небо тогда остаётся ванильным, а галактика/аврора/тонировка работают без миксина).
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(method = "getSkyColor(Lnet/minecraft/util/math/Vec3d;F)I", at = @At("RETURN"), cancellable = true, require = 0)
    private void wars333$sky(Vec3d cameraPos, float tickProgress, CallbackInfoReturnable<Integer> cir) {
        Integer c = AmbienceMod.skyColor(cir.getReturnValue());
        if (c != null) cir.setReturnValue(c);
    }

    @Inject(method = "getCloudsColor(F)I", at = @At("RETURN"), cancellable = true, require = 0)
    private void wars333$clouds(float tickProgress, CallbackInfoReturnable<Integer> cir) {
        Integer c = AmbienceMod.cloudColor(cir.getReturnValue());
        if (c != null) cir.setReturnValue(c);
    }
}
