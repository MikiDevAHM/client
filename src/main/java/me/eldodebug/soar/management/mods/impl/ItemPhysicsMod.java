package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class ItemPhysicsMod extends Mod {

	private static ItemPhysicsMod instance;
	
	private NumberSetting speedSetting = new NumberSetting(TranslateText.SPEED, this, 1, 0.5, 4, false);
	
	public ItemPhysicsMod() {
		super(Icons.ARROW_SQUARE_DOWN_24, TranslateText.ITEM_PHYSICS, TranslateText.ITEM_PHYSICS_DESCRIPTION, ModCategory.RENDER, "gravity", false);
		
		instance = this;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		
		if(Items2DMod.getInstance().isToggled()) {
			Items2DMod.getInstance().setToggled(false);
		}
	}
	
	public static ItemPhysicsMod getInstance() {
		return instance;
	}

	public NumberSetting getSpeedSetting() {
		return speedSetting;
	}
}
