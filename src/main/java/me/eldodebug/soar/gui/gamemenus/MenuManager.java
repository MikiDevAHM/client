package me.eldodebug.soar.gui.gamemenus;

import eu.shoroa.contrib.render.Blur;
import com.glideclient.Glide;
import com.glideclient.GlideMeta;
import me.eldodebug.soar.gui.gamemenus.backgrounds.BackgroundsHandler;
import me.eldodebug.soar.gui.gamemenus.views.BackgroundSelector;
import me.eldodebug.soar.gui.gamemenus.views.MainMenuClassic;
import me.eldodebug.soar.gui.gamemenus.views.SplashFade;
import me.eldodebug.soar.gui.gamemenus.views.UpdateScreen;
import me.eldodebug.soar.management.event.impl.EventRenderNotification;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.easing.EaseInOutCubic;
import me.eldodebug.soar.utils.animation.normal.easing.EaseInOutSine;
import me.eldodebug.soar.utils.buffer.ScreenAlpha;
import me.eldodebug.soar.utils.buffer.ScreenAnimation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MenuManager extends GuiScreen {

    private static final float MAX_ZOOM_BLUR = 8f;
    private static final float MAX_DISSOLVE_BLUR = 5f;
    private static final int TRANSITION_DURATION = 500;
    private static final float LOGO_PIVOT_X = 20f;
    private static final float LOGO_PIVOT_Y = 20f;

    private enum TransitionType {
        NONE,
        ZOOM_FROM_LOGO,
        DISSOLVE
    }

    private GlideScreen currentView;
    private GlideScreen outgoingView;
    private final BackgroundsHandler backgroundsHandler;
    private final MenuBar menuBar;
    public static MenuManager menuManager;

    private final List<GlideScreen> views = new ArrayList<>();

    private TransitionType transitionType = TransitionType.NONE;
    private Animation transitionAnimation;

    private final ScreenAnimation zoomTransition = new ScreenAnimation();
    private final ScreenAlpha outgoingDissolve = new ScreenAlpha();
    private final ScreenAlpha incomingDissolve = new ScreenAlpha();

    public MenuManager() {
        menuManager = this;
        Glide instance = Glide.getInstance();
        backgroundsHandler = new BackgroundsHandler();
        menuBar = new MenuBar();
        addViews();
        setView(instance);
    }

    @Override
    public void initGui() {
        if (currentView != null) {
            currentView.initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        backgroundsHandler.update(width, height);

        ScaledResolution sr = new ScaledResolution(mc);
        Glide instance = Glide.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        backgroundsHandler.draw(sr, instance, nvg, partialTicks);
        Blur.render(5f);

        drawCurrentView(mouseX, mouseY, partialTicks);

        nvg.setupAndDraw(() -> drawNanoVG(sr, instance, nvg, mouseX, mouseY));
        nvg.setupAndDraw(() -> new EventRenderNotification().call());
    }

    private void drawCurrentView(int mouseX, int mouseY, float partialTicks) {
        if (currentView == null) return;

        if (transitionAnimation == null || transitionAnimation.isDone()) {
            currentView.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        float progress = transitionAnimation.getValueFloat();

        switch (transitionType) {
            case ZOOM_FROM_LOGO:
                float zoomBlur = (1f - progress) * MAX_ZOOM_BLUR;
                zoomTransition.wrap(
                        null,
                        () -> currentView.drawScreen(mouseX, mouseY, partialTicks),
                        LOGO_PIVOT_X, LOGO_PIVOT_Y, 0f, 0f,
                        progress, progress, false, zoomBlur
                );
                break;

            case DISSOLVE:
                float dissolveBlur = (1f - Math.abs(progress - 0.5f) * 2f) * MAX_DISSOLVE_BLUR;
                if (outgoingView != null) {
                    outgoingDissolve.wrap(
                            () -> outgoingView.drawScreen(mouseX, mouseY, partialTicks),
                            1.0f - progress, dissolveBlur
                    );
                }
                incomingDissolve.wrap(
                        () -> currentView.drawScreen(mouseX, mouseY, partialTicks),
                        progress, dissolveBlur
                );
                break;

            default:
                currentView.drawScreen(mouseX, mouseY, partialTicks);
        }

        if (transitionAnimation.isDone()) {
            transitionType = TransitionType.NONE;
            transitionAnimation = null;
            outgoingView = null;
        }
    }

    private void drawNanoVG(ScaledResolution sr, Glide instance, NanoVGManager nvg, int mouseX, int mouseY) {
        if (GlideMeta.BUILD_TYPE == GlideMeta.Type.DEV) {
            nvg.drawTextWithShadow("DEVELOPMENT TEST BUILD, EXPECT BROKEN STUFF", 15, height - 35, 0xFFFF2323, 10, 12, Fonts.SEMIBOLD);
            nvg.drawTextWithShadow(GlideMeta.VERSION_NUMBER + ", " + GlideMeta.VERSION_IDENTIFIER + ", " + GlideMeta.BUILD_TYPE.kind + ", " + GlideMeta.SITE, 15, height - 22, 0xFFFF2323, 10, 10, Fonts.SEMIBOLD);
        }

        menuBar.draw(sr, nvg, mouseX, mouseY, currentView);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution sr = new ScaledResolution(mc);

        if (menuBar.mouseClicked(mouseX, mouseY, mouseButton, sr, currentView)) {
            return;
        }

        if (currentView != null) {
            currentView.mouseClicked(mouseX, mouseY, mouseButton);
        }

        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (IOException ignored) {}
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (currentView != null) {
            currentView.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (currentView != null) {
            currentView.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void onGuiClosed() {
        if (currentView != null) {
            currentView.onGuiClosed();
        }
    }

    public GlideScreen getCurrentView() {
        return currentView;
    }

    public void setCurrentView(GlideScreen newView) {
        GlideScreen oldView = this.currentView;

        if (oldView != null) {
            oldView.onSceneClosed();
        }

        this.currentView = newView;
        this.outgoingView = oldView;

        menuBar.onViewChange(oldView, newView);

        if (oldView != null && newView != null) {
            transitionType = (oldView instanceof SplashFade && newView instanceof MainMenuClassic)
                    ? TransitionType.ZOOM_FROM_LOGO
                    : TransitionType.DISSOLVE;

            transitionAnimation = new EaseInOutCubic(TRANSITION_DURATION, 1.0);
        } else {
            transitionType = TransitionType.NONE;
            transitionAnimation = null;
        }

        if (this.currentView != null) {
            this.currentView.initScene();
            this.currentView.initGui();
        }
    }

    public void addViews() {
        views.add(new BackgroundSelector(this));
        views.add(new MainMenuClassic(this));
        views.add(new SplashFade(this));
        views.add(new UpdateScreen(this));
    }

    public void setView(Glide instance) {
        if (!instance.started) {
            setCurrentView(getViewByClass(SplashFade.class));
        } else if (instance.isFirstLogin()) {
            setCurrentView(getViewByClass(MainMenuClassic.class));
        } else {
            if (instance.getUpdateNeeded()) {
                setCurrentView(getViewByClass(UpdateScreen.class));
            } else {
                setCurrentView(getViewByClass(MainMenuClassic.class));
            }
        }
    }

    public GlideScreen getViewByClass(Class<? extends GlideScreen> clazz) {
        if (clazz == null){
            clazz = MainMenuClassic.class;
        }
        for (GlideScreen v : views) {
            if (v.getClass().equals(clazz)) {
                return v;
            }
        }
        return null;
    }

    public Color getBackgroundColor() {
        return new Color(230, 230, 230, 120);
    }

    public static MenuManager getMenuManager() {
        return menuManager;
    }
}