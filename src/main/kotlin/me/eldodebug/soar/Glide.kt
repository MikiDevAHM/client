package me.eldodebug.soar

import java.io.File

import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu
import me.eldodebug.soar.gui.modmenu.GuiModMenu
import me.eldodebug.soar.management.mods.RestrictedMod
import me.eldodebug.soar.management.remote.blacklists.BlacklistManager
import me.eldodebug.soar.management.remote.discord.DiscordStats
import me.eldodebug.soar.management.remote.news.NewsManager
import me.eldodebug.soar.management.remote.update.Update
import me.eldodebug.soar.ui.ClickEffects
import me.eldodebug.soar.utils.Sound
import me.eldodebug.soar.utils.render.EntityProjection
import org.apache.commons.lang3.ArrayUtils

import me.eldodebug.soar.injection.mixin.GlideTweaker
import me.eldodebug.soar.logger.GlideLogger
import me.eldodebug.soar.management.cape.CapeManager
import me.eldodebug.soar.management.remote.changelog.ChangelogManager
import me.eldodebug.soar.management.color.ColorManager
import me.eldodebug.soar.management.command.CommandManager
import me.eldodebug.soar.management.event.EventManager
import me.eldodebug.soar.management.file.FileManager
import me.eldodebug.soar.management.language.LanguageManager
import me.eldodebug.soar.management.mods.ModManager
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod
import me.eldodebug.soar.management.nanovg.NanoVGManager
import me.eldodebug.soar.management.notification.NotificationManager
import me.eldodebug.soar.management.profile.ProfileManager
import me.eldodebug.soar.management.quickplay.QuickPlayManager
import me.eldodebug.soar.management.screenshot.ScreenshotManager
import me.eldodebug.soar.management.security.SecurityFeatureManager
import me.eldodebug.soar.management.waypoint.WaypointManager
import me.eldodebug.soar.utils.OptifineUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding

class Glide private constructor() {

    private val mc: Minecraft = Minecraft.getMinecraft()
    private var updateNeeded: Boolean = false


    lateinit var nanoVGManager: NanoVGManager
    private lateinit var fileManager: FileManager
    private lateinit var languageManager: LanguageManager
    private lateinit var eventManager: EventManager
    private lateinit var modManager: ModManager
    private lateinit var capeManager: CapeManager
    private lateinit var colorManager: ColorManager
    private lateinit var profileManager: ProfileManager
    private lateinit var commandManager: CommandManager
    private lateinit var screenshotManager: ScreenshotManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var securityFeatureManager: SecurityFeatureManager
    private lateinit var quickPlayManager: QuickPlayManager
    private lateinit var changelogManager: ChangelogManager
    private lateinit var newsManager: NewsManager
    private lateinit var discordStats: DiscordStats
    private lateinit var waypointManager: WaypointManager
    private lateinit var modMenu: GuiModMenu
    private lateinit var mainMenu: GuiGlideMainMenu

    private var launchTime: Long? = null
    private lateinit var firstLoginFile: File
    private lateinit var update: Update
    private lateinit var clickEffects: ClickEffects
    private lateinit var blacklistManager: BlacklistManager
    private lateinit var restrictedMod: RestrictedMod

    fun start() {
        try {
            OptifineUtils.disableFastRender()
            this.removeOptifineZoom()
        } catch (e: Exception) {
        }
        blacklistManager = BlacklistManager()
        restrictedMod = RestrictedMod()
        try {
            restrictedMod.shouldCheck = !System.getProperty("me.eldodebug.soar.glideclient.blacklistchecks", "true").equals("false", true)
        } catch (e: Exception) {
        }
        fileManager = FileManager()
        firstLoginFile = File(fileManager.cacheDir, "first.tmp")
        languageManager = LanguageManager()
        eventManager = EventManager()
        modManager = ModManager()

        modManager.init()

        capeManager = CapeManager()
        colorManager = ColorManager()
        profileManager = ProfileManager()

        modMenu = GuiModMenu()
        mainMenu = GuiGlideMainMenu()
        launchTime = System.currentTimeMillis()

        commandManager = CommandManager()
        screenshotManager = ScreenshotManager()
        notificationManager = NotificationManager()
        securityFeatureManager = SecurityFeatureManager()
        quickPlayManager = QuickPlayManager()
        changelogManager = ChangelogManager()
        newsManager = NewsManager()
        discordStats = DiscordStats()
        discordStats.check()
        update = Update()
        update.check()
        waypointManager = WaypointManager()

        eventManager.register(EntityProjection.getInstance())
        eventManager.register(GlideHandler())

        InternalSettingsMod.getInstance().isToggled = true
        clickEffects = ClickEffects()
        mc.updateDisplay()
    }

    fun stop() {
        nanoVGManager.destroy()
        profileManager.save()
        Sound.play("soar/audio/close.wav", true)
    }

    private fun removeOptifineZoom() {
        if (GlideTweaker.hasOptifine) {
            try {
                this.unregisterKeybind(GameSettings::class.java.getField("ofKeyBindZoom").get(mc.gameSettings) as KeyBinding)
            } catch (e: Exception) {
                GlideLogger.error("Failed to unregister zoom key", e)
            }
        }
    }

    private fun unregisterKeybind(key: KeyBinding) {
        if (listOf(*mc.gameSettings.keyBindings).contains(key)) {
            mc.gameSettings.keyBindings = ArrayUtils.remove(mc.gameSettings.keyBindings, listOf(*mc.gameSettings.keyBindings).indexOf(key))
            key.keyCode = 0
        }
    }

    companion object {
        private val instance: Glide = Glide()

        @JvmStatic
        fun getInstance(): Glide = instance
        @JvmField var started: Boolean = false
    }


    fun getFileManager(): FileManager = fileManager

    fun getLanguageManager(): LanguageManager = languageManager

    fun getEventManager(): EventManager = eventManager

    fun getDiscordStats(): DiscordStats = discordStats

    fun getProfileManager(): ProfileManager = profileManager

    fun getCapeManager(): CapeManager = capeManager

    fun getColorManager(): ColorManager = colorManager

    fun getModManager(): ModManager = modManager

    fun getRestrictedMod(): RestrictedMod = restrictedMod

    fun getBlacklistManager(): BlacklistManager = blacklistManager

    fun getNotificationManager(): NotificationManager = notificationManager

    fun getWaypointManager(): WaypointManager = waypointManager

    fun getQuickPlayManager(): QuickPlayManager = quickPlayManager

    fun getClickEffects(): ClickEffects = clickEffects

    fun getSecurityFeatureManager(): SecurityFeatureManager = securityFeatureManager

    fun getScreenshotManager(): ScreenshotManager = screenshotManager

    fun getCommandManager(): CommandManager = commandManager

    fun getChangelogManager(): ChangelogManager = changelogManager

    fun getNewsManager(): NewsManager = newsManager

    fun hasStarted(): Boolean = started

    fun getModMenu() = modMenu

    fun getMainMenu() = mainMenu

    fun getLaunchTime() = launchTime

    fun createFirstLoginFile() = getInstance().getFileManager().createFile(firstLoginFile)

    fun isFirstLogin() = !firstLoginFile.exists()

    fun getUpdateInstance() = update

    fun setUpdateNeeded(updateNeeded: Boolean) {
        this.updateNeeded = updateNeeded
    }

    fun getUpdateNeeded() = updateNeeded


}
