package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.PhasingPowerType;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow public abstract void reload();

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderPass;setRenderer(Ljava/lang/Runnable;)V", ordinal = 0), cancellable = true)
    private void skipSkyRenderingForPhasingBlindness(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {

        Entity cameraFocusedEntity = camera.getFocusedEntity();

        if (PowerHolderComponent.hasPowerType(cameraFocusedEntity, PhasingPowerType.class, p -> p.getRenderType() == PhasingPowerType.RenderType.BLINDNESS) && MiscUtil.getInWallBlockState(cameraFocusedEntity) != null) {
            ci.cancel();
        }

    }

    @Inject(method = "render", at = @At("HEAD"))
    private void updateChunksIfRenderChanged(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (ApoliClient.shouldReloadWorldRenderer) {
            reload();
            ApoliClient.shouldReloadWorldRenderer = false;
        }
    }

}
