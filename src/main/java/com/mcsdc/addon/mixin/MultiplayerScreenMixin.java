package com.mcsdc.addon.mixin;

import com.mcsdc.addon.gui.McsdcScreen;
import com.mcsdc.addon.gui.ServerInfoScreen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    @Shadow
    protected ServerSelectionList serverSelectionList;
    private Button getInfoButton;

    protected MultiplayerScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/JoinMultiplayerScreen;onSelectedChange()V"))
    private void onInit(CallbackInfo info) {
        this.addRenderableWidget(
            new Button.Builder(
                Component.literal("MCSDC"),
                onPress -> {
                    if (this.minecraft == null) return;
                    this.minecraft.setScreen(new McsdcScreen((JoinMultiplayerScreen) (Object) this));
                }
            )
                .pos(150, 3)
                .width(80)
                .build()
        );

        this.getInfoButton = this.addRenderableWidget(
            new Button.Builder(
                Component.literal("Server Info"),
                onPress -> {
                    if (this.minecraft == null) return;
                    ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
                    if (entry instanceof ServerSelectionList.OnlineServerEntry onlineEntry) {
                        this.minecraft.setScreen(new ServerInfoScreen(onlineEntry.getServerData().ip));
                    }
                }
            )
                .pos(150 + 80 + 5, 3)
                .width(80)
                .build()
        );
    }

    @Inject(method = "onSelectedChange", at = @At("TAIL"))
    private void onSelectedChangeTail(CallbackInfo info) {
        ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
        this.getInfoButton.active = entry instanceof ServerSelectionList.OnlineServerEntry;
    }
}
