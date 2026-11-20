package ru.wars333.client.module;

public class OptimizerMod extends Mod {
    public OptimizerMod() {
        super("Optimizer", Category.SYSTEM, "Отключён", false);
    }

    public static float visualScale() {
        return 1f;
    }
}
