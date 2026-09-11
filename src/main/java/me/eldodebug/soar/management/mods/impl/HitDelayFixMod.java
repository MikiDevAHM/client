package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class HitDelayFixMod extends Mod {

	private static HitDelayFixMod instance;
	
	public HitDelayFixMod() {
		super(Icons.WINDOW_DEV_TOOLS_24, TranslateText.HIT_DELAY_FIX, TranslateText.HIT_DELAY_FIX_DESCRIPTION, ModCategory.PLAYER, "nodelay", true);
		
		instance = this;
	}

	public static HitDelayFixMod getInstance() {
		return instance;
	}
}
