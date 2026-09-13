package me.eldodebug.soar.management.mods.impl;


import com.google.common.collect.Lists;
import com.glideclient.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.types.Rect;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.EaseBackIn;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.buffer.ScreenAnimation;
import me.eldodebug.soar.utils.render.EntityProjection;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class TargetInfoMod extends HUDMod {

    private SimpleAnimation healthAnimation = new SimpleAnimation();
    private SimpleAnimation armorAnimation = new SimpleAnimation();
    private ScreenAnimation screenAnimation = new ScreenAnimation();
    private Animation introAnimation;

    private ComboSetting positionType = new ComboSetting(TranslateText.POSITION, this, TranslateText.HUD, Lists.<Option>newArrayList(
            new Option(TranslateText.HUD),
            new Option(TranslateText.WORLD)
    ));

    private String name;
    private float health, armor;
    private ResourceLocation head;
    private Entity target;
    private float x, y, w, h;

    public TargetInfoMod() {
        super(Icons.PERSON_NOTE_24, TranslateText.TARGET_INFO, TranslateText.TARGET_INFO_DESCRIPTION, "targethud", true);
    }

    @Override
    public void setup() {
        introAnimation = new EaseBackIn(320, 1.0F, 2.0F);
        introAnimation.setDirection(Direction.BACKWARDS);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) mc.objectMouseOver.entityHit;
            if (!player.isInvisible() && !player.isDead) {
                target = player;
            }
        }
        if (this.isEditing()) {
            target = mc.thePlayer;
        }
        introAnimation.setDirection(!(mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof AbstractClientPlayer && !mc.objectMouseOver.entityHit.isInvisible() && !mc.objectMouseOver.entityHit.isDead) ? Direction.BACKWARDS : Direction.FORWARDS);
        if (target != null) {
            name = target.getName();
            if (target instanceof EntityLivingBase) {
                health = ((EntityLivingBase) target).getHealth() / ((EntityLivingBase) target).getMaxHealth();
                armor = Math.min(((EntityLivingBase) target).getTotalArmorValue(), 20);
                if (target instanceof AbstractClientPlayer) {
                    head = ((AbstractClientPlayer) target).getLocationSkin();
                    name = ((AbstractClientPlayer) target).getGameProfile().getName();
                } else {
                    head = null;
                }
            }
        }
        ScaledResolution sr = new ScaledResolution(mc);
        int factor = sr.getScaleFactor();
        if (positionType.getOption().getTranslate() == TranslateText.WORLD) {
            Rect position = EntityProjection.getInstance().getPositionMap().get(target);
            if (position != null) {
                x = position.x / factor;
                y = position.y / factor;
                w = position.width / factor;
                h = position.height / factor;
            }
        } else {
            x = this.getX();
            y = this.getY();
            w = this.getWidth();
            h = this.getHeight();
        }
        Entity finalTarget = target;
        screenAnimation.wrap(() -> drawNanoVG(finalTarget), x, y, w, h, 2 - introAnimation.getValueFloat(), introAnimation.getValueFloat());
    }

    private void drawNanoVG(Entity target) {
        if (name == null || head == null) {
            return;
        }

        float nameWidth = this.getTextWidth(name, 10.2F, getHudFont(2));
        int width = 140;

        if (nameWidth + 48F > width) {
            width = (int) (width + nameWidth - 89);
        }

        healthAnimation.setAnimation(health, 16);
        armorAnimation.setAnimation(armor, 16);

        NanoVGManager nvg = Glide.getInstance().getNanoVGManager();

        this.drawBackground(-(getX() * getScale()) + (x - (width - w) / 2f), -(getY() * getScale()) + (y - (getHeight() - h) / 2f), getWidth(), 43, 6 * getScale());
        nvg.translate(x - (width - w) / 2f, y - (getHeight() - h) / 2f);
        nvg.translate(-(getX() * getScale()), -(getY() * getScale()));

        if (head != null) {
            this.drawPlayerHead(head, 5, 5, 36, 36, 6);
        }
        this.drawText(name, 45.5F, 8F, 10.2F, getHudFont(2));

        this.drawText(LegacyIcon.HEART_FILL, 51, 25.5F, 11, Fonts.LEGACYICON);
        this.drawText(LegacyIcon.HEART, 51, 25.5F, 11, Fonts.LEGACYICON);
        this.drawArc(56.5F, 30.5F, 9F, -90F, -90F + 360, 1.6F, this.getFontColor(120));
        this.drawArc(56.5F, 30.5F, 9F, -90F, -90F + (360 * healthAnimation.getValue()), 1.6F);

        this.drawText(LegacyIcon.SHIELD_FILL, 75F, 25.5F, 11, Fonts.LEGACYICON);
        this.drawText(LegacyIcon.SHIELD, 75F, 25.5F, 11, Fonts.LEGACYICON);
        this.drawArc(80.5F, 30.5F, 9F, -90F, -90F + 360, 1.6F, this.getFontColor(120));
        this.drawArc(80.5F, 30.5F, 9F, -90F, -90F + (18 * armorAnimation.getValue()), 1.6F);

        this.setWidth(width);
        this.setHeight(46);
    }
}

