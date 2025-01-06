package io.github.apace100.apoli.access;

import net.minecraft.loot.LootTable;
import net.minecraft.util.context.ContextType;

public interface ReplacingLootContext {

    void apoli$setType(ContextType type);

    ContextType apoli$getType();

    void apoli$setReplaced(LootTable table);

    boolean apoli$isReplaced(LootTable table);
}
