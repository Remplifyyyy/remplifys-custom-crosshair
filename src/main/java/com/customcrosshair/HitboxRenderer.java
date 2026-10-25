package com.customcrosshair;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

/**
 * Draws coloured hitboxes around players (and optionally mobs).
 * Boxes turn the "in range" colour when you are close enough to hit them.
 * Called every frame just before Minecraft draws its own debug shapes.
 */
public final class HitboxRenderer {
    private HitboxRenderer() { }

    public static void render() {
        CrosshairConfig cfg = CrosshairConfig.get();
        if (!cfg.hitboxesEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity me = mc.player;
        if (me == null || mc.world == null) return;

        float tick = mc.getRenderTickCounter().getTickProgress(true);

        switch (cfg.hitboxTargets) {
            case PLAYERS -> { for (PlayerEntity p : mc.world.getPlayers()) drawFor(cfg, me, p, tick); }
            default -> {
                for (Entity e : mc.world.getEntities()) {
                    if (cfg.hitboxTargets == CrosshairConfig.HitboxTargets.LIVING && !(e instanceof LivingEntity)) continue;
                    drawFor(cfg, me, e, tick);
                }
            }
        }
    }

    private static void drawFor(CrosshairConfig cfg, ClientPlayerEntity me, Entity e, float tick) {
        if (e == me || e.isSpectator() || e.isInvisibleTo(me)) return;

        Box box = e.getBoundingBox();
        boolean inRange = me.canInteractWithEntityIn(box, 0.0);
        if (!inRange && !cfg.hitboxShowOutOfRange) return;

        // move the box to where the entity is drawn this frame so it doesn't lag behind
        Vec3d smooth = e.getLerpedPos(tick).subtract(e.getEntityPos());
        Box drawn = box.offset(smooth);

        int color = inRange ? cfg.hitboxColor : cfg.hitboxOutColor;
        VisibilityConfigurable gizmo = GizmoDrawing.box(drawn, DrawStyle.stroked(color, cfg.hitboxWidth));
        if (cfg.hitboxThroughWalls) gizmo.ignoreOcclusion();
    }
}
