package scratcher3004.mcaircraft.entities.model;

// Made with Blockbench 4.2.4
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import scratcher3004.mcaircraft.Mcaircraft;
import scratcher3004.mcaircraft.entities.Parachute;

public class ParachuteModel<T extends Parachute> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation(Mcaircraft.MODID, "parachute_model"), "main");
	private final ModelPart All;

	public ParachuteModel(ModelPart root) {
		this.All = root.getChild("All");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create().texOffs(0, 63).addBox(-8.0F, -1.0F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
		PartDefinition Parachute = All.addOrReplaceChild("Parachute", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -66.0F, -8.0F, 32.0F, 5.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition Top2_r1 = Parachute.addOrReplaceChild("Top2_r1", CubeListBuilder.create().texOffs(0, 21).addBox(-15.0F, -26.0F, -9.0F, 30.0F, 5.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -40.0F, 0.0F, 0.5236F, 0.0F, 0.0F));
		PartDefinition Top1_r1 = Parachute.addOrReplaceChild("Top1_r1", CubeListBuilder.create().texOffs(0, 42).addBox(-15.0F, -26.0F, -7.0F, 30.0F, 5.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -40.0F, 0.0F, -0.5236F, 0.0F, 0.0F));
		PartDefinition Strain4_r1 = Parachute.addOrReplaceChild("Strain4_r1", CubeListBuilder.create().texOffs(68, 63).addBox(0.0F, -60.0F, -1.0F, 1.0F, 60.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, -8.0F, 0.1745F, 0.0F, 0.1745F));
		PartDefinition Strain3_r1 = Parachute.addOrReplaceChild("Strain3_r1", CubeListBuilder.create().texOffs(64, 63).addBox(-1.0F, -60.0F, -1.0F, 1.0F, 60.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, -8.0F, 0.1745F, 0.0F, -0.1745F));
		PartDefinition Strain2_r1 = Parachute.addOrReplaceChild("Strain2_r1", CubeListBuilder.create().texOffs(72, 63).addBox(0.0F, -60.0F, 0.0F, 1.0F, 60.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, 8.0F, -0.1745F, 0.0F, 0.1745F));
		PartDefinition Strain1_r1 = Parachute.addOrReplaceChild("Strain1_r1", CubeListBuilder.create().texOffs(76, 63).addBox(-1.0F, -60.0F, 0.0F, 1.0F, 60.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, 8.0F, -0.1745F, 0.0F, -0.1745F));
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		All.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}