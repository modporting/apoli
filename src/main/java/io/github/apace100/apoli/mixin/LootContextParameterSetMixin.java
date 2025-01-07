package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContextParameterSet;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.util.context.ContextType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LootWorldContext.class)
public class LootContextParameterSetMixin implements ReplacingLootContextParameterSet {

    @Unique
    private ContextType apoli$lootContextType;

    @Override
    public void apoli$setType(ContextType type) {
        apoli$lootContextType = type;
    }

    @Override
    public ContextType apoli$getType() {
        return apoli$lootContextType;
    }

}
