package ru.wars333.client.module;

import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
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
    private CloudRenderMode sClouds;

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
            sClouds =
