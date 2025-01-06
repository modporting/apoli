package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContext;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.util.context.ContextType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(LootContext.class)
public class LootContextMixin implements ReplacingLootContext {

    @Unique
    private ContextType apoli$lootContextType;

    @Unique
    private final Set<LootTable> apoli$replacedTables = new HashSet<>();

    @Override
    public void apoli$setType(ContextType type) {
        apoli$lootContextType = type;
    }

    @Override
    public ContextType apoli$getType() {
        return apoli$lootContextType;
    }

    @Override
    public void apoli$setReplaced(LootTable table) {
        apoli$replacedTables.add(table);
    }

    @Override
    public boolean apoli$isReplaced(LootTable table) {
        return apoli$replacedTables.contains(table);
    }
}
