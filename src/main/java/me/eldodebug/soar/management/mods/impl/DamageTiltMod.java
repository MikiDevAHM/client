package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class DamageTiltMod extends Mod {

	private static DamageTiltMod instance;
	
	public DamageTiltMod() {
		super(Icons.VIDEO_PERSON_24, TranslateText.DAMAGE_TILT, TranslateText.DAMAGE_TILT_DESCRIPTION, ModCategory.PLAYER, "true", false);
		
		instance = this;
	}

	public static DamageTiltMod getInstance() {
		return instance;
	}
}
