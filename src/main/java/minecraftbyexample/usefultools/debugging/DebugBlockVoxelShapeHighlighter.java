package minecraftbyexample.usefultools.debugging;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.lang.reflect.Field;

/**
 * Created by TGG on 27/06/2019.
 */
public class DebugBlockVoxelShapeHighlighter {
  @SubscribeEvent
  public static void onDrawBlockHighlightEvent(DrawSelectionEvent.HighlightBlock event) {
    HitResult rayTraceResult = event.getTarget();
    if (rayTraceResult.getType() != HitResult.Type.BLOCK) return;
    Level world;

    try {
      world = getPrivateWorldFromWorldRenderer(event.getLevelRenderer());
    } catch (IllegalAccessException | ObfuscationReflectionHelper.UnableToFindFieldException e) {
      if (!loggedReflectionError) LOGGER.error("Could not find WorldRenderer.world");
      loggedReflectionError = true;
      return;
    }

    BlockPos blockpos = ((BlockHitResult) rayTraceResult).getBlockPos();
    BlockState blockstate = world.getBlockState(blockpos);
    if (blockstate.isAir() || !world.getWorldBorder().isWithinBounds(blockpos)) return;

    final Color SHAPE_COLOR = Color.RED;
    final Color RENDERSHAPE_COLOR = Color.BLUE;
    final Color COLLISIONSHAPE_COLOR = Color.GREEN;
    final Color RAYTRACESHAPE_COLOR = Color.MAGENTA;

    boolean showshape = DebugSettings.getDebugParameter("showshape").isPresent();
    boolean showrendershapeshape = DebugSettings.getDebugParameter("showrendershape").isPresent();
    boolean showcollisionshape = DebugSettings.getDebugParameter("showcollisionshape").isPresent();
    boolean showraytraceshape = DebugSettings.getDebugParameter("showraytraceshape").isPresent();

    if (!(showshape || showrendershapeshape || showcollisionshape || showraytraceshape)) return;

    Camera activeRenderInfo = event.getCamera();
    CollisionContext iSelectionContext = CollisionContext.of(activeRenderInfo.getEntity());
    PoseStack matrixStack = event.getPoseStack();
    if (showshape) {
      VoxelShape shape = blockstate.getShape(world, blockpos, iSelectionContext);
      drawSelectionBox(event.getMultiBufferSource(), matrixStack, blockpos, activeRenderInfo, shape, SHAPE_COLOR);
    }
    if (showrendershapeshape) {
      VoxelShape shape = blockstate.getShape(world, blockpos);
      drawSelectionBox(event.getMultiBufferSource(), matrixStack, blockpos, activeRenderInfo, shape, RENDERSHAPE_COLOR);
    }
    if (showcollisionshape) {
      VoxelShape shape = blockstate.getCollisionShape(world, blockpos, iSelectionContext);
      drawSelectionBox(event.getMultiBufferSource(), matrixStack, blockpos, activeRenderInfo, shape, COLLISIONSHAPE_COLOR);
    }
    if (showraytraceshape) {
      VoxelShape shape = blockstate.getCollisionShape(world, blockpos, iSelectionContext);
      drawSelectionBox(event.getMultiBufferSource(), matrixStack, blockpos, activeRenderInfo, shape, RAYTRACESHAPE_COLOR);
    }
    event.setCanceled(true);
  }

  // The world field is private so we need a trick to get access to it
  // we need to use the srg name for it to work robustly:
  // see here:   https://mcp.thiakil.com/#/search
  //   and here: https://jamieswhiteshirt.github.io/resources/know-your-tools/  
  private static Level getPrivateWorldFromWorldRenderer(LevelRenderer worldRenderer) throws IllegalAccessException, ObfuscationReflectionHelper.UnableToFindFieldException {
    if (worldField == null) {
      worldField = ObfuscationReflectionHelper.findField(LevelRenderer.class, "field_72769_h");
    }
    return (Level) worldField.get(worldRenderer);
  }

  private static Field worldField;
  private static boolean loggedReflectionError = false;

  /**
   * copied from LevelRenderer; starting from the code marked with iprofiler.endStartSection("outline");
   *
   * @param activeRenderInfo
   */
  private static void drawSelectionBox(MultiBufferSource renderTypeBuffers, PoseStack matrixStack,
                                      BlockPos blockPos, Camera activeRenderInfo, VoxelShape shape, Color color) {
    RenderType renderType = RenderType.lines();
    VertexConsumer vertexBuilder = renderTypeBuffers.getBuffer(renderType);
    LevelRenderer lr;

    double eyeX = activeRenderInfo.getPosition().x;
    double eyeY = activeRenderInfo.getPosition().y;
    double eyeZ = activeRenderInfo.getPosition().z;
    final float ALPHA = 0.5f;
    drawShapeOutline(matrixStack, vertexBuilder, shape,
            blockPos.getX() - eyeX, blockPos.getY() - eyeY, blockPos.getZ() - eyeZ,
            color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, ALPHA);

  }

  private static void drawShapeOutline(PoseStack matrixStack,
                                       VertexConsumer vertexBuilder,
                                       VoxelShape voxelShape,
                                       double originX, double originY, double originZ,
                                       float red, float green, float blue, float alpha) {

    Matrix4f matrix4f = matrixStack.last().pose();
    voxelShape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
      vertexBuilder.vertex(matrix4f, (float)(x0 + originX), (float)(y0 + originY), (float)(z0 + originZ)).color(red, green, blue, alpha).endVertex();
      vertexBuilder.vertex(matrix4f, (float)(x1 + originX), (float)(y1 + originY), (float)(z1 + originZ)).color(red, green, blue, alpha).endVertex();
    });
  }
  private static final Logger LOGGER = LogManager.getLogger();
}
