package net.hexapro.hexaItems.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

public class ActionParser {

    public static void executeActions(Player player, List<String> actions) {
        if (actions == null || actions.isEmpty()) return;

        for (String actionStr : actions) {
            if (!actionStr.startsWith("[") || !actionStr.contains("]")) continue;

            String[] parts = actionStr.split("]", 2);
            if (parts.length < 2) continue;

            String actionType = parts[0].substring(1).toUpperCase();
            String actionValue = parts[1].trim().replace("%player_name%", player.getName());

            switch (actionType) {

                // --- MESSAGE
                case "MESSAGE" -> player.sendMessage(
                        MiniMessage.miniMessage().deserialize("<not_italic>" + translateCodes(actionValue)));

                // --- TITLE
                case "TITLE" -> player.showTitle(net.kyori.adventure.title.Title.title(
                        MiniMessage.miniMessage().deserialize("<not_italic>" + translateCodes(actionValue)),
                        MiniMessage.miniMessage().deserialize(""),
                        net.kyori.adventure.title.Title.Times.times(
                                java.time.Duration.ofMillis(10),
                                java.time.Duration.ofMillis(60),
                                java.time.Duration.ofMillis(10))));

                // --- CONSOLE COMMAND
                case "COMMANDOP", "CONSOLE" -> Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        translateCodes(actionValue).replace("%player_name%", player.getName()));

                // --- PLAYER COMMAND
                case "COMMAND" -> player.performCommand(
                        translateCodes(actionValue).replace("%player_name%", player.getName()));

                // --- SOUND
                case "SOUND" -> {
                    String[] soundParts = actionValue.split(":");
                    try {
                        NamespacedKey soundKey = NamespacedKey.minecraft(soundParts[0].toLowerCase());
                        Sound sound = Registry.SOUNDS.get(soundKey);
                        if (sound != null) {
                            float volume = soundParts.length > 1 ? Float.parseFloat(soundParts[1]) : 1.0f;
                            float pitch  = soundParts.length > 2 ? Float.parseFloat(soundParts[2]) : 1.0f;
                            player.playSound(player.getLocation(), sound, volume, pitch);
                        }
                    } catch (Exception ignored) {}
                }

                // --- POTION EFFECT
                case "EFFECT" -> {
                    String[] effectParts = actionValue.split(":");
                    if (effectParts.length == 3) {
                        try {
                            NamespacedKey key = NamespacedKey.minecraft(effectParts[0].toLowerCase());
                            PotionEffectType type = Registry.EFFECT.get(key);
                            if (type != null) {
                                int amplifier = Integer.parseInt(effectParts[1]) - 1;
                                int duration  = Integer.parseInt(effectParts[2]);
                                player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                            }
                        } catch (Exception ignored) {}
                    }
                }

                // --- VELOCITY
                case "VELOCITY" -> {
                    String[] velParts = actionValue.split(":");
                    if (velParts.length == 3) {
                        try {
                            double x = Double.parseDouble(velParts[0]);
                            double y = Double.parseDouble(velParts[1]);
                            double z = Double.parseDouble(velParts[2]);
                            player.setVelocity(new Vector(x, y, z));
                        } catch (Exception ignored) {}
                    }
                }

                // --- HEAL
                case "HEAL" -> {
                    try {
                        double healAmount = Double.parseDouble(actionValue);
                        player.setHealth(Math.min(player.getHealth() + healAmount, player.getMaxHealth()));
                    } catch (Exception ignored) {}
                }

                // --- DAMAGE
                case "DAMAGE" -> {
                    try {
                        player.damage(Double.parseDouble(actionValue));
                    } catch (Exception ignored) {}
                }

                // --- XP
                case "XP" -> {
                    try {
                        player.giveExp(Integer.parseInt(actionValue));
                    } catch (Exception ignored) {}
                }

                // --- GIVE ITEM
                case "GIVE" -> {
                    String[] giveParts = actionValue.split(":");
                    Material mat = Material.matchMaterial(giveParts[0]);
                    if (mat != null) {
                        int amount = 1;
                        if (giveParts.length > 1) {
                            try { amount = Integer.parseInt(giveParts[1]); } catch (Exception ignored) {}
                        }
                        player.getInventory().addItem(new ItemStack(mat, amount));
                    }
                }

                // --- DROP ITEM
                case "DROP" -> {
                    String[] dropParts = actionValue.split(":");
                    Material dropMat = Material.matchMaterial(dropParts[0]);
                    if (dropMat != null) {
                        int dropAmount = 1;
                        if (dropParts.length > 1) {
                            try { dropAmount = Integer.parseInt(dropParts[1]); } catch (Exception ignored) {}
                        }
                        player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(dropMat, dropAmount));
                    }
                }

                // --- FORCE DROP HELD ITEM
                case "FORCE_DROP" -> {
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (heldItem.getType() != Material.AIR) {
                        if (heldItem.getAmount() > 1) {
                            heldItem.setAmount(heldItem.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        }
                        player.getWorld().dropItemNaturally(player.getLocation(), heldItem.clone());
                    }
                }
            }
        }
    }

    // --- translate & color codes to MiniMessage
    private static String translateCodes(String text) {
        if (text == null) return "";
        return text
                .replace("&0", "<black>").replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>")
                .replace("&8", "<dark_gray>").replace("&9", "<blue>")
                .replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>")
                .replace("&e", "<yellow>").replace("&f", "<white>")
                .replace("&k", "<obfuscated>").replace("&l", "<bold>")
                .replace("&m", "<strikethrough>").replace("&n", "<underlined>")
                .replace("&o", "<italic>").replace("&r", "<reset>");
    }
}