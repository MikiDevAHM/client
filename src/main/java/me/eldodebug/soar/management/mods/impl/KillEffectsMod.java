package me.eldodebug.soar.management.mods.impl;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import me.eldodebug.soar.Glide;
import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.injection.interfaces.IMixinRenderManager;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventLoadWorld;
import me.eldodebug.soar.management.event.impl.EventRender3D;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.block.Block;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

public class KillEffectsMod extends Mod {

	private ArrayList<EntityLivingBase> trackedEntities = new ArrayList<>();
	private int entityID;
	private ArrayList<PhysicsParticle> physicsParticles = new ArrayList<>();

	private BooleanSetting soundSetting = new BooleanSetting(TranslateText.SOUND, this, true);
	private ComboSetting effectSetting = new ComboSetting(TranslateText.EFFECT, this, TranslateText.BLOOD, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.LIGHTING), new Option(TranslateText.FLAMES), new Option(TranslateText.CLOUD), new Option(TranslateText.BLOOD), new Option(TranslateText.PHYSICS))));

	private NumberSetting multiplierSetting = new NumberSetting(TranslateText.MULTIPLIER, this, 1, 1, 10, true);

	public KillEffectsMod() {
		super(TranslateText.KILL_EFFECTS, TranslateText.KILL_EFFECTS_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void onUpdate(EventUpdate event) {
		for (Object obj : mc.theWorld.loadedEntityList) {
			if (obj instanceof EntityLivingBase && obj != mc.thePlayer) {
				EntityLivingBase entity = (EntityLivingBase) obj;
				if (!trackedEntities.contains(entity)) {
					trackedEntities.add(entity);
				}
			}
		}

		ArrayList<EntityLivingBase> toRemove = new ArrayList<>();
		for (EntityLivingBase entity : trackedEntities) {
			if (entity.deathTime == 1) {
				spawnKillEffect(entity);
				toRemove.add(entity);
			}
		}
		trackedEntities.removeAll(toRemove);
	}

	private void spawnKillEffect(EntityLivingBase target) {
		if (mc.thePlayer.ticksExisted > 10) {

			Option option = effectSetting.getOption();

			if(option.getTranslate().equals(TranslateText.LIGHTING)) {

				EntityLightningBolt entityLightningBolt = new EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ);
				mc.theWorld.addEntityToWorld(entityID--, entityLightningBolt);

				if (soundSetting.isToggled()) {
					mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("ambient.weather.thunder"), ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
				}
			} else if(option.getTranslate().equals(TranslateText.FLAMES)) {

				for (int i = 0; i < multiplierSetting.getValueInt(); i++) {
					mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.FLAME);
				}

				if (soundSetting.isToggled()) {
					mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("item.fireCharge.use"), ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
				}
			} else if(option.getTranslate().equals(TranslateText.CLOUD)) {

				for (int i = 0; i < multiplierSetting.getValueInt(); i++) {
					mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.CLOUD);
				}

				if (soundSetting.isToggled()) {
					mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("fireworks.twinkle"), ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
				}
			} else if(option.getTranslate().equals(TranslateText.BLOOD)) {

				for (int i = 0; i < 50; i++) {
					mc.theWorld.spawnParticle(EnumParticleTypes.BLOCK_CRACK, target.posX, target.posY + target.height - 0.75, target.posZ, 0, 0, 0, Block.getStateId(Blocks.redstone_block.getDefaultState()));
				}

				if (soundSetting.isToggled()) {
					mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation("dig.stone"), 4.0F, 1.2F, ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
				}
			} else if(option.getTranslate().equals(TranslateText.PHYSICS)) {

				Random random = new Random();
				int particleCount = multiplierSetting.getValueInt() * 10;

				for (int i = 0; i < particleCount; i++) {
					double offsetX = (random.nextDouble() - 0.5) * 0.5;
					double offsetY = (random.nextDouble() - 0.5) * 0.5 + target.height / 2;
					double offsetZ = (random.nextDouble() - 0.5) * 0.5;

					double velocityX = (random.nextDouble() - 0.5) * 0.3;
					double velocityY = random.nextDouble() * 0.4 + 0.1;
					double velocityZ = (random.nextDouble() - 0.5) * 0.3;

					physicsParticles.add(new PhysicsParticle(
							target.posX + offsetX,
							target.posY + offsetY,
							target.posZ + offsetZ,
							velocityX,
							velocityY,
							velocityZ
					));
				}

				if (soundSetting.isToggled()) {
					mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation("dig.stone"), 2.0F, 0.8F, ((float) target.posX), ((float) target.posY), ((float) target.posZ)));
				}
			}
		}
	}

	@EventTarget
	public void onLoadWorld(EventLoadWorld event) {
		entityID = 0;
		physicsParticles.clear();
		trackedEntities.clear();
	}

	@EventTarget
	public void onTick(EventTick event) {
		for (PhysicsParticle particle : new ArrayList<>(physicsParticles)) {
			particle.update();

			if (particle.age > 100 || particle.posY < 0) {
				physicsParticles.remove(particle);
			}
		}
	}

	@EventTarget
	public void onRender3D(EventRender3D event) {
		for (PhysicsParticle particle : physicsParticles) {
			particle.render(event.getPartialTicks());
		}
	}

	private class PhysicsParticle {
		private double posX, posY, posZ;
		private double prevPosX, prevPosY, prevPosZ;
		private double velocityX, velocityY, velocityZ;
		private int age;
		private double size = 0.05;
		private static final double GRAVITY = 0.03;
		private static final double BOUNCE_DAMPING = 0.6;
		private static final double FRICTION = 0.98;

		public PhysicsParticle(double x, double y, double z, double vx, double vy, double vz) {
			this.posX = x;
			this.posY = y;
			this.posZ = z;
			this.prevPosX = x;
			this.prevPosY = y;
			this.prevPosZ = z;
			this.velocityX = vx;
			this.velocityY = vy;
			this.velocityZ = vz;
			this.age = 0;
		}

		public void update() {
			age++;

			prevPosX = posX;
			prevPosY = posY;
			prevPosZ = posZ;

			velocityY -= GRAVITY;

			posX += velocityX;
			posY += velocityY;
			posZ += velocityZ;

			velocityX *= FRICTION;
			velocityZ *= FRICTION;

			BlockPos blockPos = new BlockPos(posX, posY - size, posZ);
			Block blockBelow = mc.theWorld.getBlockState(blockPos).getBlock();

			if (blockBelow != Blocks.air && posY - (int)posY < size) {
				posY = (int)posY + size;
				velocityY = -velocityY * BOUNCE_DAMPING;

				if (Math.abs(velocityY) < 0.01) {
					velocityY = 0;
					velocityX *= 0.8;
					velocityZ *= 0.8;
				}
			}

			BlockPos blockPosX = new BlockPos(posX, posY, posZ);
			Block blockAtX = mc.theWorld.getBlockState(blockPosX).getBlock();
			if (blockAtX != Blocks.air) {
				velocityX = -velocityX * BOUNCE_DAMPING;
				posX -= velocityX * 2;
			}

			BlockPos blockPosZ = new BlockPos(posX, posY, posZ);
			Block blockAtZ = mc.theWorld.getBlockState(blockPosZ).getBlock();
			if (blockAtZ != Blocks.air) {
				velocityZ = -velocityZ * BOUNCE_DAMPING;
				posZ -= velocityZ * 2;
			}
		}

		public void render(float partialTicks) {
			double interpX = prevPosX + (posX - prevPosX) * partialTicks;
			double interpY = prevPosY + (posY - prevPosY) * partialTicks;
			double interpZ = prevPosZ + (posZ - prevPosZ) * partialTicks;

			double renderX = interpX - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosX();
			double renderY = interpY - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosY();
			double renderZ = interpZ - ((IMixinRenderManager)mc.getRenderManager()).getRenderPosZ();

			Color color = Glide.getInstance().getColorManager().getCurrentColor().getColor1();

			GlStateManager.pushMatrix();
			GlStateManager.translate(renderX, renderY, renderZ);
			GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableCull();

			float alpha = Math.max(0, 1 - (age / 50f));

			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();

			GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);

			float size = (float) (this.size * alpha);

			mc.getTextureManager().bindTexture(new ResourceLocation("soar/circle.png"));
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			worldrenderer.pos(-size, -size, 0).tex(0,0).endVertex();
			worldrenderer.pos(-size, size, 0).tex(0,1).endVertex();
			worldrenderer.pos(size, size, 0).tex(1,1).endVertex();
			worldrenderer.pos(size, -size, 0).tex(1,0).endVertex();
			tessellator.draw();

			GlStateManager.enableLighting();
			GlStateManager.disableBlend();
			GlStateManager.enableCull();
			GlStateManager.popMatrix();
		}
	}
}
