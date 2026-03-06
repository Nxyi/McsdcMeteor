package com.mcsdc.addon.mixin;

import com.mcsdc.addon.gui.EditFlagsScreen;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerStorage;
import com.mcsdc.addon.util.TicketIDGenerator;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
    private DirectionalLayoutWidget grid;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V", shift = At.Shift.BEFORE))
    private void addButtons(CallbackInfo ci) {
        McsdcSystem system = McsdcSystem.get();
        String ip = system.getLastServer();
        if (TicketIDGenerator.isValidIPv4WithPort(ip)) {
            grid.add(new ButtonWidget.Builder(Text.literal("Edit Flags"),
                    button -> mc.setScreen(new EditFlagsScreen(ip))).build());
        }

        if (system.hasServerQueue()) {
            grid.add(new ButtonWidget.Builder(Text.literal("Next Server").formatted(Formatting.AQUA), button -> {
                ServerStorage nextServer = system.getNextServer();
                if (nextServer != null) {
                    String nextIp = nextServer.ip();
                    ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), mc,
                        ServerAddress.parse(nextIp), new ServerInfo("", nextIp, ServerInfo.ServerType.OTHER), false, null);
                } else {
                    mc.inGameHud.setOverlayMessage(Text.literal("No more servers left."), false);
                    mc.setScreen(new TitleScreen());
                }
            }).build());
        }
    }

}
