package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventAttackEntity;
import me.eldodebug.soar.management.event.impl.EventRender3D;
import me.eldodebug.soar.management.event.impl.EventUpdate;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.proj.Projection;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SuperHeroFxMod extends Mod {
    private final List<HeroTextParticle> particles = new ArrayList<>();

    public SuperHeroFxMod() {
        super(TranslateText.SUPERHEROFX_NAME, TranslateText.SUPERHEROFX_DESCRIPTION, ModCategory.RENDER);
    }

    private static final String[] WORDS = {
            "POW!", "BAM!", "OUCH!", "ZAP!", "WHAM!", "CRIT!", "SMASH!", "BOOM!", "KAPOW!", "BANG!", "SLAM!", "WHACK!", "THWACK!", "ZING!", "BOP!", "CLANG!", "CLASH!", "KABLAM!", "SPLAT!", "THUD!"
    };

    private static final Color[] COLORS = {
            Color.RED, Color.YELLOW, Color.ORANGE, Color.CYAN, Color.MAGENTA, Color.GREEN
    };

    private int attackCounter = 0;

    @EventTarget
    private void onEntityHurt(EventAttackEntity event) {
        if (event.getEntity() == null || event.getEntity() == mc.thePlayer || !(event.getEntity() instanceof EntityLivingBase)) {
            return;
        }
        attackCounter++;

        if (attackCounter % 2 == 0)
            spawnHeroFX((EntityLivingBase) event.getEntity());
    }

    @EventTarget
    private void onUpdate(EventUpdate event) {
        Random rand = new Random();
        for (Iterator<HeroTextParticle> it = particles.iterator(); it.hasNext(); ) {
            HeroTextParticle p = it.next();

            p.prevPos = p.pos;
            p.pos = p.pos.add(p.velocity);

            double gravity = -0.008 - rand.nextDouble() * 0.006;
            double drag = 0.98 - rand.nextDouble() * 0.02;

            p.velocity = p.velocity.addVector(0, gravity, 0);
            p.velocity = new Vec3(
                    p.velocity.xCoord * drag,
                    p.velocity.yCoord,
                    p.velocity.zCoord * drag
            );

            p.age++;

            if (p.age > p.maxAge) {
                it.remove();
            }
        }
    }

    @EventTarget
    public void onRenderWorldLast(EventRender3D event) {
        EntityPlayerSP player = mc.thePlayer;
        double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
        double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
        double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

        NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
        ScaledResolution sr = new ScaledResolution(mc);
        float scaleFactor = sr.getScaleFactor();

        nvg.setupAndDraw(() -> {
            for (HeroTextParticle p : particles) {
                double interpX = p.prevPos.xCoord + (p.pos.xCoord - p.prevPos.xCoord) * event.getPartialTicks();
                double interpY = p.prevPos.yCoord + (p.pos.yCoord - p.prevPos.yCoord) * event.getPartialTicks();
                double interpZ = p.prevPos.zCoord + (p.pos.zCoord - p.prevPos.zCoord) * event.getPartialTicks();

                float wx = (float) (interpX - px);
                float wy = (float) (interpY - py);
                float wz = (float) (interpZ - pz);

                Vec3 screenPos = Projection.w2s(wx, wy, wz);
                if (screenPos.zCoord > 1.0) {
                    continue;
                }

                float distance = MathHelper.sqrt_float(wx * wx + wy * wy + wz * wz);
                float distanceScale = MathHelper.clamp_float(1.0f / (distance * 0.15f + 0.3f), 0.3f, 2.5f);

                float alpha = 1f - (p.age / (float) (p.maxAge));
                alpha = MathHelper.clamp_float(alpha, 0.1f, 1f);

                int a = (int) (alpha * 255f);

                nvg.drawText(
                        p.text,
                        (float) screenPos.xCoord / scaleFactor, (float) screenPos.yCoord / scaleFactor,
                        new Color((p.color >> 16) & 0xFF, (p.color >> 8) & 0xFF, p.color & 0xFF, a),
                        p.scale * 12f * distanceScale,
                        Fonts.BANGERS
                );
            }
        });
    }


    private void spawnHeroFX(EntityLivingBase entity) {
        Random rand = new Random();

        String word = WORDS[rand.nextInt(WORDS.length)];

        Vec3 pos = new Vec3(
                entity.posX,
                entity.posY + entity.height * 0.8,
                entity.posZ
        );

        Vec3 vel = new Vec3(
                (rand.nextDouble() - 0.5) * (0.15 + rand.nextDouble() * 0.15),
                0.05 + rand.nextDouble() * 0.08,
                (rand.nextDouble() - 0.5) * (0.15 + rand.nextDouble() * 0.15)
        );

        HeroTextParticle p = new HeroTextParticle(
                pos,
                vel,
                word,
                15 + rand.nextInt(15),
                COLORS[rand.nextInt(COLORS.length)].getRGB()
        );

        p.scale = 0.8f + rand.nextFloat() * 0.6f;

        particles.add(p);
    }


    public static class HeroTextParticle {
        public Vec3 pos;
        public Vec3 prevPos;
        public Vec3 velocity;
        public String text;
        public int age;
        public int maxAge;

        public float scale;
        public int color;

        public HeroTextParticle(Vec3 pos, Vec3 velocity, String text, int maxAge, int color) {
            this.pos = pos;
            this.prevPos = pos;
            this.velocity = velocity;
            this.text = text;
            this.age = 0;
            this.maxAge = maxAge;
            this.scale = 1.0f;
            this.color = color;
        }
    }
}
