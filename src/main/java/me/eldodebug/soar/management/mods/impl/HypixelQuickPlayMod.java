package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.nanovg.font.Icons;
import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.gui.GuiQuickPlay;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;

public class HypixelQuickPlayMod extends Mod {

	private KeybindSetting keybindSetting = new KeybindSetting(TranslateText.KEYBIND, this, Keyboard.KEY_N);
	
	public HypixelQuickPlayMod() {
		super(Icons.FAST_FORWARD_24, TranslateText.HYPIXEL_QUICK_PLAY, TranslateText.HYPIXEL_QUICK_PLAY_DESCRIPTION, ModCategory.PLAYER, "", false);
	}

	@EventTarget
	public void onKey(EventKey event) {
		
		if(event.getKeyCode() == keybindSetting.getKeyCode()) {
			mc.displayGuiScreen(new GuiQuickPlay());
		}
	}
}
