package me.eldodebug.soar.utils.render;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender3D;
import me.eldodebug.soar.types.Rect;
import me.eldodebug.soar.utils.proj.Projection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import java.util.HashMap;
import java.util.Map;

import static me.eldodebug.soar.utils.proj.Projection.VIEWPORT;

public class EntityProjection {
    private static EntityProjection instance;

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final float SMOOTHING_FACTOR = 0.85f;

    private final Map<Entity, Rect> positionMap = new HashMap<>();

    public EntityProjection() {
        instance = this;
        Glide.getInstance().getEventManager().register(this);
    }

    public static EntityProjection getInstance() {
        if (instance == null) {
            instance = new EntityProjection();
        }
        return instance;
    }

    @EventTarget
    private void onRender3D(EventRender3D event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        positionMap.clear();

        RenderManager renderManager = mc.getRenderManager();
        double viewerX = renderManager.viewerPosX;
        double viewerY = renderManager.viewerPosY;
        double viewerZ = renderManager.viewerPosZ;
        float partialTicks = event.getPartialTicks();

        float screenWidth = VIEWPORT.get(2);
        float screenHeight = VIEWPORT.get(3);

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == null || entity == mc.thePlayer) continue;
//            if (!shouldRender(entity)) continue;

            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - viewerX;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - viewerY;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - viewerZ;

            double halfWidth = entity.width / 2.0;
            double height = entity.height;

            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            boolean visible = false;

            for (int i = 0; i < 8; i++) {
                double cornerX = (i & 1) == 0 ? x - halfWidth : x + halfWidth;
                double cornerY = (i & 2) == 0 ? y : y + height;
                double cornerZ = (i & 4) == 0 ? z - halfWidth : z + halfWidth;

                Vec3 screenPos = Projection.w2s((float) cornerX, (float) cornerY, (float) cornerZ);

                if (screenPos != null && screenPos.zCoord > 0) {
                    visible = true;
                    minX = (float) Math.min(minX, screenPos.xCoord);
                    minY = (float) Math.min(minY, screenPos.yCoord);
                    maxX = (float) Math.max(maxX, screenPos.xCoord);
                    maxY = (float) Math.max(maxY, screenPos.yCoord);
                }
            }

            if (visible) {
                minX = Math.max(0, minX);
                minY = Math.max(0, minY);
                maxX = Math.min(mc.displayWidth, maxX);
                maxY = Math.min(mc.displayHeight, maxY);

                positionMap.put(entity, new Rect(minX, minY, maxX - minX, maxY - minY));
            }
        }
    }

    public Map<Entity, Rect> getPositionMap() {
        return positionMap;
    }

    public Rect getScreenPosition(Entity entity) {
        return positionMap.get(entity);
    }
}
