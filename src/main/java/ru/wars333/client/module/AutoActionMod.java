package ru.wars333.client.module;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import ru.wars333.client.setting.BoolSetting;
import ru.wars333.client.setting.ModeSetting;
import ru.wars333.client.setting.NumSetting;

/**
 * AutoAction: по кнопке переключает хотбар на слот с выбранным предметом (по умолчанию эндер-жемчуг),
 * использует его и возвращает прежний слот.
 */
public class AutoActionMod extends Mod {
    public final ModeSetting item = mode("Предмет", 0, "Эндер-жемчуг", "Фейерверк", "Заряд ветра");
    public final BoolSetting back = bool("Вернуть прежний слот", true);
    public final NumSetting delay = num("Задержка возврата, тики", 1, 0, 5, 1);

    private int prevSlot = -1;
    private int restoreIn = 0;

    public AutoActionMod() {
        super("AutoAction", Category.PLAYER, "Слот с жемчугом: переключился, бросил, вернулся", true);
        actionKey = true;
        key = GLFW.GLFW_KEY_C;
    }

    @Override
    public String suffix() {
        return item.get();
    }

    private Item target() {
        return switch (item.index) {
            case 1 -> Items.FIREWORK_ROCKET;
            case 2 -> Items.WIND_CHARGE;
            default -> Items.ENDER_PEARL;
        };
    }

    @Override
    public void onKey() {
        if (mc.player == null || mc.interactionManager == null || restoreIn > 0) return;

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(target())) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            mc.player.sendMessage(Text.literal("AutoAction: предмета нет в хотбаре"), true);
            return;
        }

        prevSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (back.value && prevSlot != slot) restoreIn = delay.intValue() + 1;
    }

    @Override
    public void onTick() {
        if (restoreIn > 0 && --restoreIn == 0 && mc.player != null && prevSlot >= 0) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
            prevSlot = -1;
        }
    }
}
