package com.mcsdc.addon.mixin;


import com.mcsdc.addon.gui.EditFlagsScreen;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.util.TicketIDGenerator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {

    @Shadow
    @Final
    private DirectionalLayoutWidget grid;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V", shift = At.Shift.BEFORE))
    private void addButtons(CallbackInfo ci) {
        String ip = McsdcSystem.get().getLastServer();
        if (TicketIDGenerator.isValidIPv4WithPort(ip)){
            grid.add(new ButtonWidget.Builder(Text.literal("Edit Flags"), button -> MinecraftClient.getInstance().setScreen(new EditFlagsScreen(ip))).build());
        }
    }

}
