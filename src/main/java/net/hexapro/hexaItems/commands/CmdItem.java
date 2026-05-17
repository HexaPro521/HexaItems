package net.hexapro.hexaItems.commands;

import net.hexapro.hexaItems.HexaItems;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CmdItem {

    public static void execute(CommandSender sender, String[] args) {
        if (args.length == 4 && args[1].equalsIgnoreCase("give")) {

            // --- check player exists
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage("§cPlayer '" + args[2] + "' not found or is offline.");
                return;
            }

            // --- get the item from ConfigManager
            String id = args[3].toLowerCase();
            ItemStack item = HexaItems.getInstance().getConfigManager().getBuiltItem(id);

            // --- check item exists
            if (item == null) {
                sender.sendMessage("§cItem '" + id + "' does not exist! Check your console for errors.");
                return;
            }

            // --- give the item
            target.getInventory().addItem(item);
            sender.sendMessage("§aGave §f" + target.getName() + " §a1x §f" + id);
            target.sendMessage("§aYou received §f" + id + " §afrom §f" + sender.getName());

        } else {
            sender.sendMessage("§cUsage: /hexaitems item give <player> <item_id>");
        }
    }
}