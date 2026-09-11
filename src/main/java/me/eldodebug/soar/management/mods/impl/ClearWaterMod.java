package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventWaterOverlay;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class ClearWaterMod extends Mod {
	
	public ClearWaterMod() {
		super(Icons.DROP_24, TranslateText.CLEAR_WATER, TranslateText.CLEAR_WATER_DESCRIPTION, ModCategory.RENDER, "", false);
	}

	@EventTarget
	public void onWaterOverlay(EventWaterOverlay event) {
		event.setCancelled(true);
	}
}
