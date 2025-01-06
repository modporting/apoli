package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EquippableItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<EquippableItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("equipment_slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT.optional(), Optional.empty()),
        data -> new EquippableItemConditionType(
            data.get("equipment_slot")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("equipment_slot", conditionType.equipmentSlot)
    );

    private final Optional<AttributeModifierSlot> equipmentSlot;

    public EquippableItemConditionType(Optional<AttributeModifierSlot> equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public boolean test(World world, ItemStack stack) {
        if(stack.contains(DataComponentTypes.EQUIPPABLE)){
            return equipmentSlot.map(slot -> slot.matches(stack.get(DataComponentTypes.EQUIPPABLE).slot())).orElse(true);
        }
        return false;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return ItemConditionTypes.EQUIPPABLE;
    }

}
