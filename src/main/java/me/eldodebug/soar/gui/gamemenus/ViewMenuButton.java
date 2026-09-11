package me.eldodebug.soar.gui.gamemenus;

import me.eldodebug.soar.utils.mouse.MouseUtils;

import java.awt.Color;

public class ViewMenuButton {

    private final String icon;
    private final Color hoverColor;
    private final Color defaultColor;
    private final Runnable onClick;

    public ViewMenuButton(String icon, Color hoverColor, Runnable onClick) {
        this(icon, hoverColor, new Color(255, 255, 255), onClick);
    }

    public ViewMenuButton(String icon, Color hoverColor, Color defaultColor, Runnable onClick) {
        this.icon = icon;
        this.hoverColor = hoverColor;
        this.defaultColor = defaultColor;
        this.onClick = onClick;
    }

    public boolean isHovered(int mouseX, int mouseY, float x, float y, float size) {
        return MouseUtils.isInside(mouseX, mouseY, x, y, size, size);
    }

    public void execute() {
        if (onClick != null) {
            onClick.run();
        }
    }

    public String getIcon() {
        return icon;
    }

    public Color getHoverColor() {
        return hoverColor;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }
}