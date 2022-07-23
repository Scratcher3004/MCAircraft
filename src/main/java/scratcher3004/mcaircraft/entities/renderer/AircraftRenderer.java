package scratcher3004.mcaircraft.entities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.data.EmptyModelData;
import scratcher3004.mcaircraft.entities.Aircraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

public class AircraftRenderer<Type extends Aircraft> extends EntityRenderer<Type> {

    public AircraftRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(Type entity) {
        return entity.GetTextureResource();
        //return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
        // we're returning this texture because the model has been stitched into the texture sheet for block models
        // Normally entities have their own ResourceLocation (TextureAtlasSprite) which is not stitched together.
        // See vanilla for examples; e.g. DragonFireballRenderer
    }

    /**
     * Render the model for the boomerang.
     * @param boomerangEntity
     * @param entityYaw
     * @param partialTicks
     * @param matrixStack
     * @param renderBuffers
     * @param packedLightIn
     */
    public void render(Type boomerangEntity, float entityYaw, float partialTicks,
                       PoseStack matrixStack, MultiBufferSource renderBuffers, int packedLightIn)
    {
        BakedModel boomerangModel = Minecraft.getInstance().getModelManager().getModel(boomerangEntity.GetModelResource());

        matrixStack.pushPose();
        PoseStack.Pose currentMatrix = matrixStack.last();

        // rotate to set the pitch and yaw correctly.
        // For an entity like an arrow, the pitch is the up/down direction angle, and the yaw is the heading (eg north, east, etc)
        // The boomerang is a bit different because it is flipping end-over-end.
        // The three rotations required are (see also boomerang_rotations.png):
        // 1) The "end over end" rotation of the spinning boomerang
        // 2) The 90 degree rotation ("pitch") so that the top face of the boomerang is pointing in the correct elevation (pitch)
        //    (the model has its top face pointing directly up, but in flight it needs to point sideways)
        // 3) The "yaw" which is the direction that the boomerang is heading in.
        // 3D rotations roll/pitch/yaw are hard to get right.  The axis and correct order aren't obvious unless you're a lot smarter than I am.

        // We must also smooth out the motion by linearly interpolating between the "tick" frames using the partialTicks
        // i.e. if we are rendering at 80 frames a second, then render is called four times between ticks.
        // If the yaw changes from 50 degrees to 58 degrees during the tick, then the four yaw values are
        // first call: partialTick = 0 --> yaw = 50
        // second call : partialTick = 0.25 -> yaw is 25% of the way from 50 to 58, i.e. 52 degrees
        // third call: partialTick = 0.5 -> yaw = 54 degrees
        // fourth call: partialTick = 0.75 -> yaw = 56 degrees
        float directionOfMotion = Mth.lerp(partialTicks, boomerangEntity.yRotO, boomerangEntity.getYRot());
        float directionOfBoomerangTopFace = directionOfMotion + 90;

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-1 * directionOfBoomerangTopFace));        // yaw
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(                                                  // pitch
                Mth.lerp(partialTicks, -boomerangEntity.xRotO, -boomerangEntity.getXRot())));

        // rotate the boomerang end-over-end
        //matrixStack.mulPose(Vector3f.YP.rotationDegrees(                                       // end-over-end
        //        boomerangEntity.getEndOverEndRotation(partialTicks)) );



        // 3D rotations roll/pitch/yaw are hard to get right.  The axis and correct order aren't obvious.
        //  I've found it easiest to just test it interactively, using the DebugSettings method eg using the combinations below
        //  but it's still quite awkward.
        //  See the DebugSettings class for further information on how to interactively set these parameters

        final float SCALE_FACTOR = 1 / boomerangEntity.getToyScale();
        matrixStack.scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);

        Color blendColour = Color.WHITE;
        float red = blendColour.getRed() / 255.0F;
        float green = blendColour.getGreen() / 255.0F;
        float blue = blendColour.getBlue() / 255.0F;

        // we're going to use the block renderer to render our model, even though it's not a block, because we baked
        //   our entity model as if it were a block model.
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        VertexConsumer vertexBuffer = renderBuffers.getBuffer(RenderType.solid());
        dispatcher.getModelRenderer().renderModel(currentMatrix, vertexBuffer, null, boomerangModel,
                red, green, blue, packedLightIn, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

        matrixStack.popPose(); // restore the original transformation matrix + normals matrix

        //super.render(boomerangEntity, entityYaw, partialTicks, matrixStack, renderBuffers, packedLightIn);  // renders labels
        // other useful examples of projectile entity rendering code are in ArrowRenderer, ItemRenderer extends EntityRenderer<ItemEntity>, and
        //   SpriteRenderer
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
