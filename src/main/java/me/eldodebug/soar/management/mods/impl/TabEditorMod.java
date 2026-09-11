package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class TabEditorMod extends Mod {

	private static TabEditorMod instance;
	
	private BooleanSetting backgroundSetting = new BooleanSetting(TranslateText.BACKGROUND, this, true);
    private BooleanSetting headSetting = new BooleanSetting(TranslateText.HEAD, this, true);
    
	public TabEditorMod() {
		super(Icons.TEXT_BULLET_LIST_SQUARE_EDIT_24, TranslateText.TAB_EDITOR, TranslateText.TAB_EDITOR_DESCRIPTION, ModCategory.RENDER, "", false);
		
		instance = this;
	}

	public static TabEditorMod getInstance() {
		return instance;
	}

	public BooleanSetting getBackgroundSetting() {
		return backgroundSetting;
	}

	public BooleanSetting getHeadSetting() {
		return headSetting;
	}
}
