package com.mcsdc.addon.mixin;

import com.mcsdc.addon.gui.EditFlagsScreen;
import com.mcsdc.addon.gui.ServerInfoScreen;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerStorage;
import com.mcsdc.addon.util.TicketIDGenerator;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuMixin extends Screen {

    protected GameMenuMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;I)Lnet/minecraft/client/gui/widget/Widget;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void oninitWidgets(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder){
        ServerInfo info = mc.getNetworkHandler().getServerInfo();
        if(info == null) return;

        adder.add(ButtonWidget.builder(Text.literal("Reconnect"), (button) -> {
            mc.world.disconnect(Text.of(""));
            ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), mc,
                ServerAddress.parse(info.address), info, false, null);

        }).width(204).build(), 2);

        McsdcSystem system = McsdcSystem.get();
        if (system.hasServerQueue()) {
            adder.add(ButtonWidget.builder(Text.literal("Next Server").formatted(Formatting.AQUA), button -> {
                ServerStorage nextServer = system.getNextServer();
                if (nextServer != null) {
                    String nextIp = nextServer.ip();
                    mc.world.disconnect(Text.of(""));
                    ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), mc,
                        ServerAddress.parse(nextIp), new ServerInfo("", nextIp, ServerInfo.ServerType.OTHER), false, null);
                } else {
                    mc.inGameHud.setOverlayMessage(Text.literal("No more servers left."), false);
                    mc.setScreen(new TitleScreen());
                }
            }).width(204).build(), 2);
        }

        if (TicketIDGenerator.isValidIPv4WithPort(info.address)){
            adder.add(ButtonWidget.builder(Text.literal("Info"), (button) -> {
                mc.setScreen(new ServerInfoScreen(info.address));
            }).width(204).build(), 2);

            adder.add(ButtonWidget.builder(Text.literal("Edit Flags"), (button) -> {
                mc.setScreen(new EditFlagsScreen(info.address));
            }).width(204).build(), 2);
        }
    }

}
