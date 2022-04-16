package me.thef1xer.gateclient.modules.player;

import me.thef1xer.gateclient.events.SendPacketEvent;
import me.thef1xer.gateclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;

public class AntiHunger extends Module {
    public static final AntiHunger INSTANCE = new AntiHunger();

    public AntiHunger() {
        super("Anti Hunger", "antihunger", ModuleCategory.PLAYER);
    }

    public void onSendPacket(SendPacketEvent event) {
        if (event.getPacket() instanceof CPacketEntityAction) {
            CPacketEntityAction packetEntityAction = (CPacketEntityAction) event.getPacket();

            // Cancel player sprinting (serverside)
            if (packetEntityAction.getAction() == CPacketEntityAction.Action.START_SPRINTING) {
                event.setCanceled(true);
            }
        } else if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packetPlayer = (CPacketPlayer) event.getPacket();

            // Cancel player onGround (makes food level decrease slowly)
            if (Minecraft.getMinecraft().player.fallDistance == 0 && !Minecraft.getMinecraft().playerController.getIsHittingBlock()) {
                packetPlayer.onGround = false;
            }
        }
    }
}
