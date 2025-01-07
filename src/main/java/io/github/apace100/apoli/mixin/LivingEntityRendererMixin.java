package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.access.ModifiedPoseHolder;
import io.github.apace100.apoli.access.PowerHoldingEntityRenderState;
import io.github.apace100.apoli.access.PseudoRenderDataHolder;
import io.github.apace100.apoli.access.PseudoRenderDataHoldingRenderState;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin <T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> implements FeatureRendererContext<S, M> {

    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @ModifyReturnValue(method = "isShaking", at = @At("RETURN"))
    private boolean apoli$letEntitiesShakeTheirBodies(boolean original, S state) {
        return original || PowerHolderComponent.hasPowerType((PowerHoldingEntityRenderState) state, ShakingPowerType.class);
    }

    @ModifyExpressionValue(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;hasOutline:Z"))
    private boolean apoli$preventOutlineWhenInvisible(boolean original, S state) {
        return !PowerHolderComponent.hasPowerType((PowerHoldingEntityRenderState) state, InvisibilityPowerType.class, Predicate.not(InvisibilityPowerType::shouldRenderOutline)) && original;
    }
    //TODO Figure out what's wrong - Farpo
    @WrapOperation(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer apoli$useTranslucentRenderLayerWhenVisible(LivingEntityRenderer instance, S state, boolean showBody, boolean translucent, boolean showOutline, Operation<RenderLayer> original) {
        return original.call(instance, state, showBody, translucent || showBody && PowerHolderComponent.hasPowerType((PowerHoldingEntityRenderState) state, ModelColorPowerType.class, ModelColorPowerType::isTranslucent), showOutline);
    }

    @WrapWithCondition(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/EntityRenderState;FF)V"))
    private boolean apoli$preventFeatureRender(FeatureRenderer instance, MatrixStack stack, VertexConsumerProvider vertexConsumerProvider, int i, S s, float v, float y) {
        return (!(instance instanceof ArmorFeatureRenderer<?, ?, ?>) || !PowerHolderComponent.hasPowerType((PowerHoldingEntityRenderState) s, InvisibilityPowerType.class, Predicate.not(InvisibilityPowerType::shouldRenderArmor)))
            && !PowerHolderComponent.hasPowerType((PowerHoldingEntityRenderState) s, PreventFeatureRenderPowerType.class, p -> p.doesApply(instance));
    }

    @WrapOperation(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void apoli$renderColorChangedModel(EntityModel instance, MatrixStack stack, VertexConsumer vertexConsumer, int light, int overlay, int color, Operation<Void> original, S s) {

        List<ModelColorPowerType> modelColorPowers = PowerHolderComponent.getPowerTypes((PowerHoldingEntityRenderState) s, ModelColorPowerType.class);
        if (modelColorPowers.isEmpty()) {
            original.call(instance, stack, vertexConsumer, light, overlay, color);
            return;
        }

        //  TODO: Implement custom blending modes for blending colors -eggohito
        float newRed = modelColorPowers
            .stream()
            .map(ModelColorPowerType::getRed)
            .reduce((float) ColorHelper.getRed(color) / 255, (a, b) -> a * b);
        float newGreen = modelColorPowers
            .stream()
            .map(ModelColorPowerType::getGreen)
            .reduce((float) ColorHelper.getGreen(color) / 255, (a, b) -> a * b);
        float newBlue = modelColorPowers
            .stream()
            .map(ModelColorPowerType::getBlue)
            .reduce((float) ColorHelper.getBlue(color) / 255, (a, b) -> a * b);

        float oldAlpha = (float) ColorHelper.getAlpha(color) / 255;
        float newAlpha = modelColorPowers
            .stream()
            .map(ModelColorPowerType::getAlpha)
            .min(Float::compareTo)
            .map(alphaFactor -> oldAlpha * alphaFactor)
            .orElse(oldAlpha);

        original.call(instance, stack, vertexConsumer, light, overlay, ColorHelper.fromFloats(newAlpha, newRed, newGreen, newBlue));

    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;usingRiptide:Z"))
    private boolean apoli$forceRiptidePose(boolean original, S state) {
        return original || PosePowerType.hasEntityPose(state, EntityPose.SPIN_ATTACK);
    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "FIELD", ordinal = 0, target = "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;deathTime:F"))
    private float apoli$forceDyingPose(float original, S state, @Share("applyPseudoDeathTicks") LocalBooleanRef applyPseudoDeathTicksRef, @Share("pseudoDeathTicks") LocalIntRef pseudoDeathTicksRef) {

        if (original > 0 || !(state instanceof PseudoRenderDataHolder renderData)) {
            return original;
        }

        int pseudoDeathTicks = renderData.apoli$getPseudoDeathTicks();

        pseudoDeathTicksRef.set(pseudoDeathTicks);
        applyPseudoDeathTicksRef.set(pseudoDeathTicks > 0);

        return pseudoDeathTicks;

    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;deathTime:F", ordinal = 1))
    private float apoli$applyPseudoDeathTicks(float original, @Share("applyPseudoDeathTicks") LocalBooleanRef applyPseudoDeathTicksRef, @Share("pseudoDeathTicks") LocalIntRef pseudoDeathTicksRef) {
        return applyPseudoDeathTicksRef.get()
            ? pseudoDeathTicksRef.get()
            : original;
    }
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void addDataToRenderState(T livingEntity, S state, float f, CallbackInfo ci){
        if(state instanceof PowerHoldingEntityRenderState){
            ((PowerHoldingEntityRenderState) state).apoli$setPowerHolder(PowerHolderComponent.getOptional(livingEntity));
        }
        if (state instanceof PseudoRenderDataHoldingRenderState pseudoRenderDataHoldingRenderState && livingEntity instanceof PseudoRenderDataHolder dataHolder) {
            pseudoRenderDataHoldingRenderState.apoli$setPseudoDeathTicks(dataHolder.apoli$getPseudoDeathTicks());
            pseudoRenderDataHoldingRenderState.apoli$setPseudoFallFlyingTicks(dataHolder.apoli$getPseudoFallFlyingTicks());
        }
        if(state instanceof ModifiedPoseHolder statePoseHolder && livingEntity instanceof ModifiedPoseHolder entityPoseHolder){
            entityPoseHolder.apoli$getModifiedEntityPose().ifPresent(statePoseHolder::apoli$setModifiedEntityPose);
        }
    }

}
