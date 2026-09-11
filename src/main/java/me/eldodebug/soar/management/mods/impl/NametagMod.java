package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class NametagMod extends Mod {

	private static NametagMod instance;
	
	public NametagMod() {
		super(Icons.TAG_QUESTION_MARK_24, TranslateText.NAMETAG, TranslateText.NAMETAG_DESCRIPTION, ModCategory.PLAYER, "", false);
		
		instance = this;
	}

	public static NametagMod getInstance() {
		return instance;
	}
}
