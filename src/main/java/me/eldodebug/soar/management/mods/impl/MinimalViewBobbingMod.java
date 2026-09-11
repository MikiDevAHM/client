package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class MinimalViewBobbingMod extends Mod {

	private static MinimalViewBobbingMod instance;
	
	public MinimalViewBobbingMod() {
		super(Icons.DATA_SUNBURST_24, TranslateText.MINIMAL_VIEW_BOBBING, TranslateText.MINIMAL_VIEW_BOBBING_DESCRIPTION, ModCategory.RENDER, "", false);
		
		instance = this;
	}

	public static MinimalViewBobbingMod getInstance() {
		return instance;
	}
}
