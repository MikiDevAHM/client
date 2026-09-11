package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class ShinyPotsMod extends Mod {

	private static ShinyPotsMod instance;
	
	public ShinyPotsMod() {
		super(Icons.DRINK_BEER_24, TranslateText.SHINY_POTS, TranslateText.SHINY_POTS_DESCRIPTION, ModCategory.RENDER, "", false);
		
		instance = this;
	}

	public static ShinyPotsMod getInstance() {
		return instance;
	}
}
