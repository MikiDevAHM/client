package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.discord.DiscordRPC;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.nanovg.font.Icons;

public class DiscordRPCMod extends Mod {

	private DiscordRPC discord = new DiscordRPC();
	
	public DiscordRPCMod() {
		super(Icons.DISCORD, TranslateText.DISCORD_RPC, TranslateText.DISCORD_RPC_DESCRIPTION, ModCategory.OTHER, "", false);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		discord.start();
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		if(discord.isStarted()) {
			discord.stop();
		}
	}
}
