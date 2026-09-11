package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class AsyncScreenshotMod extends Mod {

	private static AsyncScreenshotMod instance;
	
	private BooleanSetting messageSetting = new BooleanSetting(TranslateText.MESSAGE, this, true);
	private BooleanSetting clipboardSetting = new BooleanSetting(TranslateText.CLIPBOARD, this, false);
	
	public AsyncScreenshotMod() {
		super(Icons.SCREENSHOT_24, TranslateText.ASYNC_SCREENSHOT, TranslateText.ASYNC_SCREENSHOT_DESCRIPTION, ModCategory.OTHER, "patcher", false);
		
		instance = this;
	}

	public static AsyncScreenshotMod getInstance() {
		return instance;
	}

	public BooleanSetting getMessageSetting() {
		return messageSetting;
	}

	public BooleanSetting getClipboardSetting() {
		return clipboardSetting;
	}
}
