package com.thef1xer.gateclient.modules.player;

import com.thef1xer.gateclient.events.EventSendPacket;
import com.thef1xer.gateclient.events.EventSetOpaqueCube;
import com.thef1xer.gateclient.modules.EnumModuleCategory;
import com.thef1xer.gateclient.modules.Module;
import com.thef1xer.gateclient.settings.FloatSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Freecam extends Module {
    public int lastThirdPerson;
    public EntityOtherPlayerMP camera;

    public FloatSetting verticalSpeed = new FloatSetting("Vertical Speed", "verticalspeed", 3F);
    public FloatSetting horizontalSpeed = new FloatSetting("Horizontal Speed", "horizontalspeed", 3F);

    public Freecam() {
        super("Freecam", "freecam", EnumModuleCategory.PLAYER, Keyboard.KEY_R);
        verticalSpeed.setParent("Speed");
        horizontalSpeed.setParent("Speed");
        this.addSettings(verticalSpeed, horizontalSpeed);
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisabled() {
        super.onDisabled();

        MinecraftForge.EVENT_BUS.unregister(this);
        if (Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().world.isRemote) {
            Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
            Minecraft.getMinecraft().gameSettings.thirdPersonView = lastThirdPerson;
            Minecraft.getMinecraft().world.removeEntity(camera);
        }
        camera = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().world == null || !Minecraft.getMinecraft().world.isRemote) {
            camera = null;
            return;
        }

        if (camera == null) {
            lastThirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;

            camera = new EntityOtherPlayerMP(Minecraft.getMinecraft().world, Minecraft.getMinecraft().getSession().getProfile());
            Minecraft.getMinecraft().world.addEntityToWorld(333393333, camera);
            camera.copyLocationAndAnglesFrom(Minecraft.getMinecraft().player);
            Minecraft.getMinecraft().setRenderViewEntity(camera);
            camera.noClip = true;
        }

        Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
        camera.inventory = Minecraft.getMinecraft().player.inventory;
        camera.setHealth(Minecraft.getMinecraft().player.getHealth());

        float forward = (Minecraft.getMinecraft().player.movementInput.forwardKeyDown ? horizontalSpeed.getValue() : 0) -
                (Minecraft.getMinecraft().player.movementInput.backKeyDown ? horizontalSpeed.getValue() : 0);
        float strafe = (Minecraft.getMinecraft().player.movementInput.leftKeyDown ? horizontalSpeed.getValue() : 0) -
                (Minecraft.getMinecraft().player.movementInput.rightKeyDown ? horizontalSpeed.getValue() : 0);
        float vertical = (Minecraft.getMinecraft().player.movementInput.jump ? verticalSpeed.getValue() : 0) -
                (Minecraft.getMinecraft().player.movementInput.sneak ? verticalSpeed.getValue() : 0);

        Vec3d vector = new Vec3d(strafe, vertical, forward).rotateYaw( (float) -Math.toRadians(camera.rotationYaw));
        camera.setPositionAndRotationDirect(camera.posX + vector.x, camera.posY + vector.y, camera.posZ + vector.z, camera.rotationYaw, camera.rotationPitch, 3, false);
    }

    @SubscribeEvent
    public void onOpaqueCube(EventSetOpaqueCube event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPackage(EventSendPacket event) {
        if (event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity useEntity = (CPacketUseEntity) event.getPacket();
            if (useEntity.getEntityFromWorld(Minecraft.getMinecraft().world) == Minecraft.getMinecraft().player) {
                event.setCanceled(true);
            }
        }
    }

}
