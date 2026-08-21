package dev.rinchan.sharedlootr.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.rinchan.sharedlootr.SharedLootr;
import dev.rinchan.sharedlootr.marker.LootChestMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestRenderer.class)
abstract class ChestRendererMixin<T extends BlockEntity & LidBlockEntity> {
    @Unique
    private static final ResourceLocation OPENED_MARKER = ResourceLocation.fromNamespaceAndPath(
        SharedLootr.MOD_ID,
        "textures/gui/opened_loot_chest.png"
    );
    @Unique
    private static final int FULL_BRIGHT = 0x00F000F0;

    @Inject(method = "render", at = @At("TAIL"))
    private void sharedlootr$renderOpenedMarker(
        T chest,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffers,
        int packedLight,
        int packedOverlay,
        CallbackInfo callback
    ) {
        if (!(chest instanceof LootChestMarker marker) || !marker.wmf$isLootChest() || !marker.wmf$wasOpened()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.25D, 0.5D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.35F, -0.35F, 0.35F);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(OPENED_MARKER));
        vertex(vertices, matrix, -0.5F, -0.5F, 0F, 0F);
        vertex(vertices, matrix, 0.5F, -0.5F, 1F, 0F);
        vertex(vertices, matrix, 0.5F, 0.5F, 1F, 1F);
        vertex(vertices, matrix, -0.5F, 0.5F, 0F, 1F);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, float x, float y, float u, float v) {
        vertices.addVertex(matrix, x, y, 0F)
            .setColor(255, 255, 255, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT)
            .setNormal(0F, 0F, 1F);
    }
}
