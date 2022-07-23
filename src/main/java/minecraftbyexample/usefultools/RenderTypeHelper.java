package minecraftbyexample.usefultools;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

/*
 * A few useful functions to help with more-complicated rendering
 *
 * This class is adapted from part of the Botania Mod, thanks to Vazkii and WillieWillus
 * Get the Source Code in github (lots more examples in the original class):
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

public final class RenderTypeHelper extends RenderType {
  // extract the private (protected) transparency settings so that we can create custom RenderTypes with them.
  /*public static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY;
  public static final RenderState.TransparencyState NO_TRANSPARENCY;
  public static final RenderState.TransparencyState LIGHTNING_TRANSPARENCY;

  public static final RenderState.LayerState VIEW_OFFSET_Z_LAYERING;

  public static final RenderState.TargetState ITEM_ENTITY_TARGET;*/

  public static final RenderType MBE_LINE_DEPTH_WRITING_ON;  // draws lines which will only be drawn over by objects which are closer (unlike RenderType.LINES)
  public static final RenderType MBE_LINE_NO_DEPTH_TEST;  // draws lines on top of anything already drawn

  public static final RenderType MBE_TRIANGLES_NO_TEXTURE;  // draws triangles with a colour but no texture.

  private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                      int bufSize, boolean hasCrumbling, boolean sortOnUpload, CompositeState glState) {
    return RenderType.create(name, format, mode, bufSize, hasCrumbling, sortOnUpload, glState);
  }

  private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                      int bufSize, CompositeState glState) {
    return makeLayer(name, format, mode, bufSize, false, false, glState);
  }

  static {

    final boolean ENABLE_DEPTH_WRITING = true;
    final boolean ENABLE_COLOUR_COMPONENTS_WRITING = true;
    final RenderStateShard.WriteMaskStateShard WRITE_TO_DEPTH_AND_COLOR
            = new RenderStateShard.WriteMaskStateShard(ENABLE_DEPTH_WRITING, ENABLE_COLOUR_COMPONENTS_WRITING);

    final RenderStateShard.DepthTestStateShard NO_DEPTH_TEST = new RenderStateShard.DepthTestStateShard("always", GL11.GL_ALWAYS);

    final int INITIAL_BUFFER_SIZE = 128;
    final boolean AFFECTS_OUTLINE = false;
    RenderType.CompositeState renderState;
    renderState = RenderType.CompositeState.builder()
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(1)))
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(NO_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setWriteMaskState(WRITE_TO_DEPTH_AND_COLOR)
            .createCompositeState(AFFECTS_OUTLINE);
    MBE_LINE_DEPTH_WRITING_ON = makeLayer("mbe_line_1_depth_writing_on",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, INITIAL_BUFFER_SIZE, renderState);

    renderState = RenderType.CompositeState.builder()
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(1)))
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(NO_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setWriteMaskState(WRITE_TO_DEPTH_AND_COLOR)
            .setDepthTestState(NO_DEPTH_TEST)
            .createCompositeState(AFFECTS_OUTLINE);
    MBE_LINE_NO_DEPTH_TEST = makeLayer("mbe_line_1_no_depth_test",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, INITIAL_BUFFER_SIZE, renderState);

    renderState = RenderType.CompositeState.builder()
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(NO_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setWriteMaskState(WRITE_TO_DEPTH_AND_COLOR)
            .createCompositeState(AFFECTS_OUTLINE);
    MBE_TRIANGLES_NO_TEXTURE = makeLayer("mbe_triangles_no_texture",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, INITIAL_BUFFER_SIZE, renderState);
  }

  public RenderTypeHelper(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
    super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
  }
}