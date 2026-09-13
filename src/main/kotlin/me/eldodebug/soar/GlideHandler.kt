package me.eldodebug.soar

import me.eldodebug.soar.management.event.impl.*
import org.apache.commons.lang3.StringUtils

import me.eldodebug.soar.gui.modmenu.GuiModMenu
import me.eldodebug.soar.management.cape.CapeManager
import me.eldodebug.soar.management.cape.impl.Cape
import me.eldodebug.soar.management.event.EventTarget
import me.eldodebug.soar.management.profile.Profile
import me.eldodebug.soar.utils.OptifineUtils
import me.eldodebug.soar.utils.TargetUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.util.ResourceLocation

class GlideHandler {

    private val mc: Minecraft = Minecraft.getMinecraft()
    private var instance: Glide = Glide.getInstance()
    private lateinit var prevOfflineName: String
    private lateinit var offlineSkin: ResourceLocation

    @EventTarget
    fun onTick(event: EventTick) {
        OptifineUtils.disableFastRender()
    }

    @EventTarget
    fun onJoinServer(event: EventJoinServer) {
        for (p: Profile in instance.getProfileManager().profiles) {
            if (p.serverIp.isNotEmpty() && StringUtils.containsIgnoreCase(event.ip, p.serverIp)) {
                instance.getModManager().disableAll()
                instance.getProfileManager().load(p.jsonFile)
                break
            }
        }

        instance.getRestrictedMod().joinServer(event.ip)
    }

    @EventTarget
    fun onLoadWorld(event: EventLoadWorld) {
        instance.getRestrictedMod().joinWorld()
    }

    @EventTarget
    fun onUpdate(event: EventUpdate) {
        TargetUtils.onUpdate()
    }

    @EventTarget
    fun onClickMouse(event: EventClickMouse) {
        if (mc.gameSettings.keyBindTogglePerspective.isPressed) {
            mc.gameSettings.thirdPersonView = (mc.gameSettings.thirdPersonView + 1) % 3
            mc.renderGlobal.setDisplayListEntitiesDirty()
        }
    }

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        if (event.packet is S2EPacketCloseWindow && mc.currentScreen is GuiModMenu) {
            event.isCancelled = true
        }
    }

    @EventTarget
    fun onCape(event: EventLocationCape) {
        val capeManager: CapeManager = instance.getCapeManager()
        if (event.playerInfo != null && event.playerInfo.gameProfile.id.equals(mc.thePlayer.gameProfile.id)) {
            val currentCape: Cape = capeManager.currentCape

            if (currentCape != capeManager.getCapeByName("None")) {
                event.isCancelled = true
                event.cape = currentCape.cape
            }
        }
    }
}
