package ru.wars333.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumSetting extends Setting {
    public double value;
    public final double min, max, step;

    public NumSetting(String name, double value, double min, double max, double step) {
        super(name);
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public int intValue() {
        return (int) Math.round(value);
    }

    public void setFromRatio(double t) {
        t = Math.max(0, Math.min(1, t));
        double raw = min + t * (max - min);
        double snapped = Math.round(raw / step) * step;
        value = Math.max(min, Math.min(max, snapped));
    }

    public double ratio() {
        return (value - min) / (max - min);
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(value);
    }

    @Override
    public void load(JsonElement element) {
        value = Math.max(min, Math.min(max, element.getAsDouble()));
    }
}
