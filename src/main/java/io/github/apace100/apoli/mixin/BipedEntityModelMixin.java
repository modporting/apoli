package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.util.ArmPoseReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends BipedEntityRenderState> extends EntityModel<T> implements ModelWithArms, ModelWithHead  {

    protected BipedEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "positionRightArm", at = @At(value = "HEAD"))
    private void apoli$overrideRightArmPose(T state, BipedEntityModel.ArmPose armPose, CallbackInfo ci, @Local(argsOnly = true) LocalRef<BipedEntityModel.ArmPose> pose) {
        pose.set(ArmPoseReference.getArmPose(state).orElse(pose.get()));
    }

    @Inject(method = "positionLeftArm", at = @At(value = "HEAD"))
    private void apoli$overrideLeftArmPose(T state, BipedEntityModel.ArmPose armPose, CallbackInfo ci, @Local(argsOnly = true) LocalRef<BipedEntityModel.ArmPose> pose) {
        pose.set(ArmPoseReference.getArmPose(state).orElse(pose.get()));
    }

}
