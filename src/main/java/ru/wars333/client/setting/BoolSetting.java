package ru.wars333.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolSetting extends Setting {
    public boolean value;

    public BoolSetting(String name, boolean value) {
        super(name);
        this.value = value;
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(value);
    }

    @Override
    public void load(JsonElement element) {
        value = element.getAsBoolean();
    }
}
