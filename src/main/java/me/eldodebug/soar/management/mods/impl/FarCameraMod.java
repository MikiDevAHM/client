package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventCameraRotation;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class FarCameraMod extends Mod {

	private NumberSetting rangeSetting = new NumberSetting(TranslateText.RANGE, this, 15, 0, 50, true);
	
	public FarCameraMod() {
		super(Icons.MEET_NOW_24, TranslateText.FAR_CAMERA, TranslateText.FAR_CAMERA_DESCRIPTION, ModCategory.RENDER, "", false);
	}

	@EventTarget
	public void onCameraRotation(EventCameraRotation event) {
		event.setThirdPersonDistance(rangeSetting.getValueFloat());
	}
}
