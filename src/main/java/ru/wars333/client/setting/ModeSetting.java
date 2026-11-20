package ru.wars333.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ModeSetting extends Setting {
    public final String[] modes;
    public int index;

    public ModeSetting(String name, int index, String... modes) {
        super(name);
        this.modes = modes;
        this.index = index;
    }

    public String get() {
        return modes[index];
    }

    public void next() {
        index = (index + 1) % modes.length;
    }

    public void prev() {
        index = (index - 1 + modes.length) % modes.length;
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(index);
    }

    @Override
    public void load(JsonElement element) {
        int i = element.getAsInt();
        if (i >= 0 && i < modes.length) index = i;
    }
}
