package com.mcsdc.addon.commands;

import com.mcsdc.addon.Main;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class TicketIDCommand extends Command {
    public TicketIDCommand() {
        super("ticketID", "get servers MCSDC ticketID");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.executes(context -> {
            mc.keyboard.setClipboard(Main.getTicketID());
            return 1;
        });
    }
}
