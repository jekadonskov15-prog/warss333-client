package ru.wars333.client.module;

import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import ru.wars333.client.setting.BoolSetting;
import ru.wars333.client.setting.ModeSetting;

/**
 * Optimizer: снижает настройки графики и нагрузку от самого клиента.
 * При выключении возвращает прежние значения.
 */
public class OptimizerMod extends Mod {
    public final ModeSetting profile = mode("Профиль", 0, "Баланс", "Максимум FPS");
    public final BoolSetting auto = bool("Авто-снижение эффектов при низком FPS", true);

    private boolean saved;
    private int sView, sSim, sBlend;
    private double sEntity;
    private boolean sShadows;
    private ParticlesMode sParticles;
    private CloudRenderMode sClouds;
    private GraphicsMode sGraphics;

    public OptimizerMod() {
        super("Optimizer", Category.SYSTEM, "Снижает нагрузку: настройки графики и эффекты клиента", false);
    }

    @Override
    public String suffix() {
        return profile.get();
    }

    @Override
    public void onEnable() {
        GameOptions o = mc.options;
        if (o == null) return;
        if (!saved) {
            sView = o.getViewDistance().getValue();
            sSim = o.getSimulationDistance().getValue();
            sBlend = o.getBiomeBlendRadius().getValue();
            sEntity = o.getEntityDistanceScaling().getValue();
            sShadows = o.getEntityShadows().getValue();
            sParticles = o.getParticles().getValue();
            sClouds = o.getCloudRenderMode().getValue();
            sGraphics = o.getGraphicsMode().getValue();
            saved = true;
        }
        apply();
    }

    @Override
    public void onDisable() {
        GameOptions o = mc.options;
        if (o == null || !saved) return;
        o.getViewDistance().setValue(sView);
        o.getSimulationDistance().setValue(sSim);
        o.getBiomeBlendRadius().setValue(sBlend);
        o.getEntityDistanceScaling().setValue(sEntity);
        o.getEntityShadows().setValue(sShadows);
        o.getParticles().setValue(sParticles);
        o.getCloudRenderMode().setValue(sClouds);
        o.getGraphicsMode().setValue(sGraphics);
        saved = false;
    }

    private int lastProfile = -1;

    @Override
    public void onTick() {
        // если сменили профиль в меню — применяем заново
        if (lastProfile != profile.index) {
            lastProfile = profile.index;
            if (saved) apply();
        }
    }

    private void apply() {
        GameOptions o = mc.options;
        boolean max = profile.index == 1;
        o.getViewDistance().setValue(Math.min(sView, max ? 6 : 10));
        o.getSimulationDistance().setValue(Math.min(sSim, max ? 5 : 8));
        o.getBiomeBlendRadius().setValue(Math.min(sBlend, max ? 0 : 1));
        o.getEntityDistanceScaling().setValue(Math.min(sEntity, max ? 0.5 : 0.75));
        o.getEntityShadows().setValue(false);
        o.getCloudRenderMode().setValue(max ? CloudRenderMode.OFF : CloudRenderMode.FAST);
        if (max) {
            o.getParticles().setValue(ParticlesMode.DECREASED);
            o.getGraphicsMode().setValue(GraphicsMode.FAST);
        }
    }

    /** множитель количества частиц/звёзд клиента: 1.0 без оптимизатора */
    public static float visualScale() {
        OptimizerMod o = ModuleManager.get(OptimizerMod.class);
        if (o == null || !o.enabled) return 1f;
        float s = o.profile.index == 0 ? 0.7f : 0.4f;
        if (o.auto.value && mc.getCurrentFps() < 40) s *= 0.6f;
        return s;
    }
}
