package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.access.PseudoRenderDataHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModelColorPowerType;
import io.github.apace100.apoli.power.type.PosePowerType;
import io.github.apace100.apoli.util.ArmPoseReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }
    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
    public void addModelColorsToRenderer(AbstractClientPlayerEntity abstractClientPlayerEntity, PlayerEntityRenderState playerEntityRenderState, float f, CallbackInfo ci){
        modelColorPowers = PowerHolderComponent.getPowerTypes(abstractClientPlayerEntity, ModelColorPowerType.class);
    }
    @Unique
    private List<ModelColorPowerType> modelColorPowers;
    @WrapOperation(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 0))
    private void apoli$makeArmAndSleeveTransparent(ModelPart instance, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, Operation<Void> original, @Local(argsOnly = true) Identifier skinTextureId, @Local(argsOnly = true) VertexConsumerProvider vertexConsumers) {

        if (modelColorPowers.isEmpty()) {
            original.call(instance, matrices, vertices, light, overlay);
            return;
        }

        float red = modelColorPowers.stream().map(ModelColorPowerType::getRed).reduce((a, b) -> a * b).orElse(1.0f);
        float green = modelColorPowers.stream().map(ModelColorPowerType::getGreen).reduce((a, b) -> a * b).orElse(1.0f);
        float blue = modelColorPowers.stream().map(ModelColorPowerType::getBlue).reduce((a, b) -> a * b).orElse(1.0f);
        float alpha = modelColorPowers.stream().map(ModelColorPowerType::getAlpha).min(Float::compare).orElse(1.0f);

        instance.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(skinTextureId)), light, overlay, ColorHelper.fromFloats(alpha, red, green, blue));

    }

    @ModifyExpressionValue(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;isGliding:Z"))
    private boolean apoli$forceFallFlyingPose(boolean original, PlayerEntityRenderState state, @Share("applyPseudoFallFlyingTicks") LocalBooleanRef applyPseudoFallFlyingTicksRef, @Share("pseudoRoll") LocalIntRef pseudoRollRef) {

        if (original || !(state instanceof PseudoRenderDataHolder renderData)) {
            return original;
        }

        int pseudoRoll = renderData.apoli$getPseudoFallFlyingTicks();
        boolean apply = pseudoRoll > 0;

        pseudoRollRef.set(pseudoRoll);
        applyPseudoFallFlyingTicksRef.set(apply);

        return apply;

    }

    @ModifyExpressionValue(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;usingRiptide:Z"))
    private boolean apoli$accountForForcedRiptide(boolean original, PlayerEntityRenderState state) {
        return original || PosePowerType.hasEntityPose(state, EntityPose.SPIN_ATTACK);
    }
    //TODO RE-ENABLE - Farpo
    /*@ModifyExpressionValue(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;glidingTicks:I"))
    private int apoli$applyPseudoFallFlyingTicks(int original, AbstractClientPlayerEntity player, @Share("applyPseudoFallFlyingTicks") LocalBooleanRef applyPseudoFallFlyingTicksRef, @Share("pseudoRoll") LocalIntRef pseudoRollRef) {
        return applyPseudoFallFlyingTicksRef.get()
            ? pseudoRollRef.get()
            : original;
    }*/

    @ModifyReturnValue(method = "getArmPose(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/util/Arm;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;", at = @At("RETURN"))
    private static BipedEntityModel.ArmPose apoli$overrideArmPose(BipedEntityModel.ArmPose original, AbstractClientPlayerEntity player) {
        return ArmPoseReference
            .getArmPose(player)
            .orElse(original);
    }

}
