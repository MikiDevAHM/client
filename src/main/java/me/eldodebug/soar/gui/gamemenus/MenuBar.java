package me.eldodebug.soar.gui.gamemenus;

import eu.shoroa.contrib.render.Blur;
import me.eldodebug.soar.gui.gamemenus.views.SplashFade;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Font;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.other.DecelerateAnimation;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class MenuBar {

    private static final int ANIMATION_DURATION = 500; // ms
    private static final float MAX_FONT_BLUR = 12.0f;
    private static final float BUTTON_SIZE = 20f;
    private static final float BUTTON_SPACING = 25f;
    private static final float TOP_BAR_Y = 10f;

    private final List<ViewMenuButton> outgoingButtons = new ArrayList<>();
    private String outgoingTitle = null;

    private Animation outgoingAnim;
    private Animation incomingAnim;

    public void onViewChange(GlideScreen oldView, GlideScreen newView) {
        if (oldView != null) {
            outgoingButtons.clear();
            outgoingButtons.addAll(oldView.getMenuActions());
            outgoingTitle = oldView.getMenuName();
            outgoingAnim = new DecelerateAnimation(ANIMATION_DURATION, 1.0);
        }

        if (newView != null) {
            incomingAnim = new DecelerateAnimation(ANIMATION_DURATION, 1.0);
        }
    }

    public void draw(ScaledResolution sr, NanoVGManager nvg, int mouseX, int mouseY, GlideScreen currentView) {
        if (currentView == null || currentView instanceof SplashFade) {
            return;
        }

        if(InternalSettingsMod.getInstance().getBlurSetting().isToggled()){
            Blur.render(2f);
            Blur.drawBlurRect(0, 0, sr.getScaledWidth(), 36);
            nvg.drawRect(0, 0, sr.getScaledWidth(), 36, 0x05000000);
        } else {
            nvg.drawRect(0, 0, sr.getScaledWidth(), 36, 0x22000000);
        }

        nvg.drawTextWithShadow(Icons.GLIDE, 10f, TOP_BAR_Y - 2F, 0xDFFFFFFF, 5, 18, Fonts.ICON_FILLED);

        drawTitles(nvg, currentView);
        drawButtons(sr, nvg, mouseX, mouseY, currentView);
    }

    private void drawTitles(NanoVGManager nvg, GlideScreen currentView) {
        Color baseTitleColor = new Color(255, 255, 255, 223);

        if (outgoingAnim != null && outgoingTitle != null && !outgoingTitle.isEmpty()) {
            float progress = outgoingAnim.getValueFloat();
            float alpha = 1.0f - progress;
            float blur = progress * MAX_FONT_BLUR;

            if (alpha > 0.01f) {
                if (blur > 0.05f) nvg.fontBlur(blur);
                Color titleColor = ColorUtils.applyAlpha(baseTitleColor, alpha);
                drawText(nvg, outgoingTitle, 32, TOP_BAR_Y + 1, titleColor.getRGB(), blur, 5, 15, Fonts.MEDIUM);
                nvg.fontBlur(0f);
            }

            if (outgoingAnim.isDone()) {
                outgoingTitle = null;
            }
        }

        String currentTitle = currentView.getMenuName();
        if (currentTitle != null && !currentTitle.isEmpty()) {
            float progress = incomingAnim != null ? incomingAnim.getValueFloat() : 1.0f;
            float blur = (1.0f - progress) * MAX_FONT_BLUR;

            Color titleColor = ColorUtils.applyAlpha(baseTitleColor, progress);
            drawText(nvg, currentTitle, 32, TOP_BAR_Y + 1, titleColor.getRGB(), blur, 5, 15, Fonts.MEDIUM);
        }
    }

    private void drawButtons(ScaledResolution sr, NanoVGManager nvg, int mouseX, int mouseY, GlideScreen currentView) {
        if (outgoingAnim != null && !outgoingButtons.isEmpty()) {
            float progress = outgoingAnim.getValueFloat();
            float alpha = 1.0f - progress;
            float blur = progress * MAX_FONT_BLUR;

            if (alpha > 0.01f) {
                renderButtonSet(outgoingButtons, mouseX, mouseY, sr, nvg, alpha, blur, false);
            }

            if (outgoingAnim.isDone()) {
                outgoingButtons.clear();
                outgoingAnim = null;
            }
        }

        List<ViewMenuButton> actions = currentView.getMenuActions();
        if (!actions.isEmpty()) {
            float progress = incomingAnim != null ? incomingAnim.getValueFloat() : 1.0f;
            float blur = (1.0f - progress) * MAX_FONT_BLUR;

            renderButtonSet(actions, mouseX, mouseY, sr, nvg, progress, blur, true);
        }
    }

    private void renderButtonSet(List<ViewMenuButton> buttons, int mouseX, int mouseY, ScaledResolution sr, NanoVGManager nvg, float alpha, float blur, boolean allowHover) {
        float xOffset = sr.getScaledWidth() - BUTTON_SPACING;

        for (ViewMenuButton action : buttons) { // remember to add back checks for first login && !instance.isFirstLogin() at milestone 4 for OBE
            boolean hovered = allowHover && action.isHovered(mouseX, mouseY, xOffset, TOP_BAR_Y, BUTTON_SIZE);
            Color baseColor = hovered ? action.getHoverColor() : action.getDefaultColor();
            Color drawColor = ColorUtils.applyAlpha(baseColor, alpha);

            drawText(nvg, action.getIcon(), xOffset, TOP_BAR_Y, drawColor.getRGB(), blur, 5, 15, Fonts.ICON_OUTLINE);

            xOffset -= BUTTON_SPACING;
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, ScaledResolution sr, GlideScreen currentView) {
        if (mouseButton != 0 || currentView == null || currentView instanceof SplashFade) {
            return false;
        }

        float xOffset = sr.getScaledWidth() - BUTTON_SPACING;

        for (ViewMenuButton action : currentView.getMenuActions()) { // remember to add back checks for first login && !instance.isFirstLogin() at milestone 4 for OBE
            if (action.isHovered(mouseX, mouseY, xOffset, TOP_BAR_Y, BUTTON_SIZE)) {
                action.execute();
                return true;
            }

            xOffset -= BUTTON_SPACING;
        }
        return false;
    }

    public void drawText(NanoVGManager nvg, String text, float x, float y, int color, float blurRadius, float shadowRadius, float size, Font font) {
        int shadowColor = (((((color >> 24) & 0xFF) * 0x62) / 255) << 24);

        nvg.fontBlur(shadowRadius);
        nvg.drawText(text, x, y, shadowColor, size, font);

        nvg.fontBlur(blurRadius);
        nvg.drawText(text, x, y, color, size, font);

        nvg.fontBlur(0);
    }
}