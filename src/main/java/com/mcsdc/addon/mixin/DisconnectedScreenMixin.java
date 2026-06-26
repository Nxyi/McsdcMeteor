package com.mcsdc.addon.mixin;

import com.mcsdc.addon.gui.EditFlagsScreen;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerStorage;
import com.mcsdc.addon.util.TicketIDGenerator;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {

    @Shadow
    @Final
    private LinearLayout layout;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;arrangeElements()V", shift = At.Shift.BEFORE))
    private void addButtons(CallbackInfo ci) {
        McsdcSystem system = McsdcSystem.get();
        String ip = system.getLastServer();
        if (TicketIDGenerator.isValidIPv4WithPort(ip)) {
            layout.addChild(new Button.Builder(Component.literal("Edit Flags"),
                button -> mc.setScreen(new EditFlagsScreen(ip))).build());
        }

        if (system.hasServerQueue()) {
            layout.addChild(new Button.Builder(Component.literal("Next Server").withStyle(ChatFormatting.AQUA), button -> {
                ServerStorage nextServer = system.getNextServer();
                if (nextServer != null) {
                    String nextIp = nextServer.ip();
                    ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()), mc,
                        ServerAddress.parseString(nextIp), new ServerData("", nextIp, ServerData.Type.OTHER), false, null);
                } else {
                    mc.gui.setOverlayMessage(Component.literal("No more servers left."), false);
                    mc.setScreen(new TitleScreen());
                }
            }).build());
        }
    }

}
