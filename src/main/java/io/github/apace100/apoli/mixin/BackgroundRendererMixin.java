package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyCameraSubmersionTypePowerType;
import io.github.apace100.apoli.power.type.PhasingPowerType;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BackgroundRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class BackgroundRendererMixin {

//TODO Move to the correct mixin - Farpo
/*
    @ModifyExpressionValue(method = "", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 0))
    private static boolean apoli$nightVisionProxy(boolean original, @Local Entity cameraFocusedEntity) {
        return original
            || PowerHolderComponent.hasPowerType(cameraFocusedEntity, NightVisionPowerType.class);
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;"))
    private static CameraSubmersionType apoli$modifyCameraSubmersionType(CameraSubmersionType original, Camera camera) {
        return PowerHolderComponent.getPowerTypes(camera.getFocusedEntity(), ModifyCameraSubmersionTypePowerType.class, true)
            .stream()
            .filter(p -> p.doesModify(original) && p.isActive())
            .findFirst()
            .map(ModifyCameraSubmersionTypePowerType::getNewType)
            .orElse(original);
    }
*/

    @ModifyExpressionValue(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;"))
    private static CameraSubmersionType apoli$modifyCameraSubmersionTypeFog(CameraSubmersionType original, Camera camera) {
        return PowerHolderComponent.getPowerTypes(camera.getFocusedEntity(), ModifyCameraSubmersionTypePowerType.class, true)
            .stream()
            .filter(p -> p.doesModify(original) && p.isActive())
            .findFirst()
            .map(ModifyCameraSubmersionTypePowerType::getNewType)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getFogColor", at = @At(value = "RETURN"))
    private static Vector4f modifyFogColor(Vector4f original, Camera camera) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            if(PowerHolderComponent.getPowerTypes(camera.getFocusedEntity(), PhasingPowerType.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPowerType.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState(camera.getFocusedEntity()) != null) {
                    return new Vector4f(0, 0, 0, original.w);
                }
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "applyFog", at = @At(value = "TAIL"))
    private static Fog modifyFogData(Fog original, Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, @Local BackgroundRenderer.FogData fogData) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            List<PhasingPowerType> phasings = PowerHolderComponent.getPowerTypes(camera.getFocusedEntity(), PhasingPowerType.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPowerType.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((LivingEntity)camera.getFocusedEntity()) != null) {
                    float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPowerType.RenderType.BLINDNESS).map(PhasingPowerType::getViewDistance).min(Float::compareTo).get();
                    if (fogData.fogType == BackgroundRenderer.FogType.FOG_SKY) {
                        fogData.fogStart = 0.0f;
                        fogData.fogEnd = view * 0.8f;
                    } else {
                        fogData.fogStart = view * 0.25f;
                        fogData.fogEnd = view;
                    }
                    return new Fog(fogData.fogStart, fogData.fogEnd, fogData.fogShape, color.x, color.y, color.z, color.w);
                }
            }
        }
        return original;
    }
}
