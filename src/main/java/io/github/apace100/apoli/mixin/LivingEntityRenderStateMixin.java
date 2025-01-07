package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ModifiedPoseHolder;
import io.github.apace100.apoli.access.PowerHoldingEntityRenderState;
import io.github.apace100.apoli.access.PseudoRenderDataHolder;
import io.github.apace100.apoli.access.PseudoRenderDataHoldingRenderState;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.util.ArmPoseReference;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;
@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements PowerHoldingEntityRenderState, ModifiedPoseHolder, PseudoRenderDataHoldingRenderState {
    @Unique
    private EntityPose apoli$modifiedEntityPose;


    @Override
    public Optional<EntityPose> apoli$getModifiedEntityPose() {
        return Optional.ofNullable(apoli$modifiedEntityPose);
    }

    @Override
    public void apoli$setModifiedEntityPose(EntityPose entityPose) {
        this.apoli$modifiedEntityPose = entityPose;
    }

    @Unique
    private Optional<PowerHolderComponent> powerHolderComponent;

    @Override
    public void apoli$setPowerHolder(Optional<PowerHolderComponent> component) {
        this.powerHolderComponent = component;
    }

    @Override
    public Optional<PowerHolderComponent> apoli$getPowerHolder() {
        return powerHolderComponent;
    }

    @Override
    public int apoli$getPseudoDeathTicks() {
        return pseudoDeathTicks;
    }

    @Override
    public int apoli$getPseudoFallFlyingTicks() {
        return pseudoFallFlyingTicks;
    }
    @Unique
    int pseudoDeathTicks = 0;
    @Unique
    int pseudoFallFlyingTicks = 0;
    @Override
    public void apoli$setPseudoDeathTicks(int ticks) {
        pseudoDeathTicks = ticks;
    }

    @Override
    public void apoli$setPseudoFallFlyingTicks(int ticks) {
        pseudoFallFlyingTicks = ticks;
    }
}
