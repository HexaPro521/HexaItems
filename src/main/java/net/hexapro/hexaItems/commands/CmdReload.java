package net.hexapro.hexaItems.commands;

import net.hexapro.hexaItems.HexaItems;
import org.bukkit.command.CommandSender;

public class CmdReload {

    public static void execute(CommandSender sender, String[] args) {
        HexaItems.getInstance().getConfigManager().reload();
        sender.sendMessage("§aHexaItems reloaded!");
    }
}