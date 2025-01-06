package io.github.apace100.apoli.loot.context;

import com.google.common.collect.BiMap;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.LootContextTypesAccessor;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextType;

public class ApoliLootContextTypes {

    public static final ContextType ANY = register(
        Apoli.identifier("any"),
            new ContextType.Builder()
            .allow(LootContextParameters.THIS_ENTITY)
            .allow(LootContextParameters.LAST_DAMAGE_PLAYER)
            .allow(LootContextParameters.DAMAGE_SOURCE)
            .allow(LootContextParameters.ATTACKING_ENTITY)
            .allow(LootContextParameters.DIRECT_ATTACKING_ENTITY)
            .allow(LootContextParameters.ORIGIN)
            .allow(LootContextParameters.BLOCK_STATE)
            .allow(LootContextParameters.BLOCK_ENTITY)
            .allow(LootContextParameters.TOOL)
            .allow(LootContextParameters.EXPLOSION_RADIUS)
    );

    private ApoliLootContextTypes() {}

    private static ContextType register(Identifier id, ContextType.Builder lootContextTypeBuilder) {

        ContextType lootContextType = lootContextTypeBuilder.build();
        BiMap<Identifier, ContextType> idAndLootContextTypeMap = LootContextTypesAccessor.getMap();

        if (idAndLootContextTypeMap.containsKey(id)) {
            throw new IllegalStateException("Loot table parameter set \"" + id + "\" is already registered!");
        }

        idAndLootContextTypeMap.put(id, lootContextType);
        return lootContextType;

    }

}
