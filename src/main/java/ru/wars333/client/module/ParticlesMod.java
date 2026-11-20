package ru.wars333.client.module;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import ru.wars333.client.setting.BoolSetting;
import ru.wars333.client.setting.ModeSetting;
import ru.wars333.client.setting.NumSetting;

import java.util.Random;

/** Красивые частицы, падающие сверху вокруг игрока (снег, лепестки, звёзды, искры, пепел). */
public class ParticlesMod extends Mod {
    public final ModeSetting type = mode("Тип", 1, "Снег", "Лепестки", "Звёзды", "Искры", "Пепел");
    public final NumSetting amount = num("Количество за тик", 4, 1, 20, 1);
    public final NumSetting radius = num("Радиус", 24, 8, 48, 2);
    public final NumSetting height = num("Высота спавна", 22, 6, 40, 2);
    public final BoolSetting onlySky = bool("Только под открытым небом", true);

    private final Random rnd = new Random();

    public ParticlesMod() {
        super("Particles", Category.VISUAL, "Красивые частицы, падающие с неба", true);
    }

    @Override
    public String suffix() {
        return type.get();
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null || mc.isPaused()) return;

        int n = (int) Math.ceil(amount.value * OptimizerMod.visualScale());
        double r = radius.value;
        boolean floaty = type.index == 2 || type.index == 3; // звёзды и искры почти не падают сами
        double hMax = floaty ? Math.min(height.value, 14) : height.value;
        ParticleEffect effect = switch (type.index) {
            case 0 -> ParticleTypes.SNOWFLAKE;
            case 1 -> ParticleTypes.CHERRY_LEAVES;
            case 2 -> ParticleTypes.END_ROD;
            case 3 -> ParticleTypes.FIREWORK;
            default -> ParticleTypes.ASH;
        };

        double bx = mc.player.getX(), by = mc.player.getY(), bz = mc.player.getZ();
        for (int i = 0; i < n; i++) {
            double x = bx + (rnd.nextDouble() * 2 - 1) * r;
            double z = bz + (rnd.nextDouble() * 2 - 1) * r;
            double y = by + (floaty ? 3 + rnd.nextDouble() * hMax : hMax + rnd.nextDouble() * 6);
            if (onlySky.value && !mc.world.isSkyVisible(BlockPos.ofFloored(x, y, z))) continue;
            double vx = 0.02 + rnd.nextGaussian() * 0.01;
            double vz = rnd.nextGaussian() * 0.01;
            double vy = floaty ? -0.03 - rnd.nextDouble() * 0.02 : -0.05 - rnd.nextDouble() * 0.04;
            mc.world.addParticleClient(effect, x, y, z, vx, vy, vz);
        }
    }
}
