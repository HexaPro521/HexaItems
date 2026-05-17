package net.hexapro.hexaItems.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CmdBase implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // If they just type /hexaitems or /hi with nothing else
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /" + label + " <item|reload>");
            return true;
        }

        // Check the first argument
        if (args[0].equalsIgnoreCase("item")) {
            // Send them to the Item command logic
            CmdItem.execute(sender, args);
        }
        else if (args[0].equalsIgnoreCase("reload")) {
            // Send them to the Reload command logic
            CmdReload.execute(sender, args);
        }
        else {
            sender.sendMessage("§cUnknown sub-command. Use /" + label + " <item|reload>");
        }

        return true;
    }
}