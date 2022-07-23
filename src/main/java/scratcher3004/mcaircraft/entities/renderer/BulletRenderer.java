package scratcher3004.mcaircraft.entities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import scratcher3004.mcaircraft.Mcaircraft;
import scratcher3004.mcaircraft.entities.Bullet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BulletRenderer<Type extends Bullet> extends EntityRenderer<Type> {

    public BulletRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(Type entity) {
        return new ModelResourceLocation(new ResourceLocation(Mcaircraft.MODID, "parachute.png"), "");
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
        // TODO
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
