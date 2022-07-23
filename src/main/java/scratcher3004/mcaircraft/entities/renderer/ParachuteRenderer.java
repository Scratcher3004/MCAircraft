package scratcher3004.mcaircraft.entities.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import scratcher3004.mcaircraft.Mcaircraft;
import scratcher3004.mcaircraft.entities.Parachute;
import scratcher3004.mcaircraft.entities.model.ParachuteModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParachuteRenderer extends EntityRenderer<Parachute> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Mcaircraft.MODID, "textures/entity/parachute.png");
    private final ParachuteModel<Parachute> model;
    protected final List<RenderLayer<Parachute, ParachuteModel<Parachute>>> layers = Lists.newArrayList();

    public ParachuteRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        model = new ParachuteModel<>(pContext.bakeLayer(ParachuteModel.LAYER_LOCATION));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Parachute pEntity) {
        return TEXTURE;
    }

    @Override
    public void render(Parachute pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        float f6 = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pEntityYaw));
        pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
        pMatrixStack.translate(0.0D, -1.501F, 0.0D);

        //.this.model.prepareMobModel(pEntity, 0, 0, pPartialTicks);
        //this.model.setupAnim(pEntity, 0, 0, 0, pEntityYaw, f6);
        RenderType rendertype = this.model.renderType(this.getTextureLocation(pEntity));
        VertexConsumer vertexconsumer = pBuffer.getBuffer(rendertype);
        this.model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        for(RenderLayer<Parachute, ParachuteModel<Parachute>> renderLayer : this.layers) {
            renderLayer.render(pMatrixStack, pBuffer, pPackedLight, pEntity, 0, 0, pPartialTicks, 0, pEntityYaw, f6);
        }

        pMatrixStack.popPose();
    }
}
