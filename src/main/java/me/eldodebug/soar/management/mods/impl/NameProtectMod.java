package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventText;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.TextSetting;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class NameProtectMod extends Mod {

	private TextSetting nameSetting = new TextSetting(TranslateText.NAME, this, "You");
	
	public NameProtectMod() {
		super(Icons.TAG_LOCK_24, TranslateText.NAME_PROTECT, TranslateText.NAME_PROTECT_DESCRIPTION, ModCategory.PLAYER, "nickhider", false);
	}
	
	@EventTarget
	public void onText(EventText event) {
		event.replace(mc.getSession().getUsername(), nameSetting.getText());
	}
}
