package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ModifiedPoseHolder;
import io.github.apace100.apoli.util.ArmPoseReference;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;
@Mixin(BipedEntityRenderState.class)
public class BipedEntityRenderStateMixin implements ModifiedPoseHolder {

    @Unique
    private EntityPose apoli$modifiedEntityPose;

    @Unique
    private ArmPoseReference apoli$modifiedArmPose;

    @Override
    public Optional<EntityPose> apoli$getModifiedEntityPose() {
        return Optional.ofNullable(apoli$modifiedEntityPose);
    }

    @Override
    public void apoli$setModifiedEntityPose(EntityPose entityPose) {
        this.apoli$modifiedEntityPose = entityPose;
    }

    @Override
    public Optional<ArmPoseReference> apoli$getModifiedArmPose() {
        return Optional.ofNullable(apoli$modifiedArmPose);
    }

    @Override
    public void apoli$setModifiedArmPose(ArmPoseReference armPose) {
        this.apoli$modifiedArmPose = armPose;
    }
}
