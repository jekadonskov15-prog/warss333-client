package ru.wars333.client.module;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.wars333.client.setting.ModeSetting;

import java.util.function.Predicate;

/**
 * AutoSwap: по кнопке меняет предмет в левой руке. Тотем в руке -> сфера (или другой выбранный предмет),
 * повторное нажатие возвращает тотем. Использует обычный клик SWAP по слоту инвентаря.
 */
public class AutoSwapMod extends Mod {
    public final ModeSetting item = mode("Предмет", 0, "Сфера", "Золотое яблоко", "Щит");
    private long last;

    public AutoSwapMod() {
        super("AutoSwap", Category.PLAYER, "Свап тотема в левой руке на сферу и обратно по кнопке", true);
        actionKey = true;
        key = GLFW.GLFW_KEY_X;
    }

    @Override
    public String suffix() {
        return item.get();
    }

    @Override
    public void onKey() {
        if (mc.player == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - last < 120) return;
        last = now;

        Predicate<ItemStack> totem = s -> s.isOf(Items.TOTEM_OF_UNDYING);
        Predicate<ItemStack> target = this::isTarget;
        ItemStack off = mc.player.getOffHandStack();

        int slot;
        if (totem.test(off)) slot = find(target);
        else if (target.test(off)) slot = find(totem);
        else {
            slot = find(totem);
            if (slot < 0) slot = find(target);
        }

        if (slot < 0) {
            mc.player.sendMessage(Text.literal("AutoSwap: нет предмета для обмена"), true);
            return;
        }
        int slotId = slot < 9 ? 36 + slot : slot;
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slotId, 40, SlotActionType.SWAP, mc.player);
    }

    private int find(Predicate<ItemStack> p) {
        for (int i = 0; i < 36; i++) {
            if (p.test(mc.player.getInventory().getStack(i))) return i;
        }
        return -1;
    }

    private boolean isTarget(ItemStack s) {
        if (s.isEmpty()) return false;
        switch (item.index) {
            case 1:
                return s.isOf(Items.GOLDEN_APPLE) || s.isOf(Items.ENCHANTED_GOLDEN_APPLE);
            case 2:
                return s.isOf(Items.SHIELD);
            default:
                if (!s.isOf(Items.PLAYER_HEAD)) return false;
                String n = s.getName().getString().toLowerCase();
                return n.contains("сфер") || n.contains("sphere");
        }
    }
}
