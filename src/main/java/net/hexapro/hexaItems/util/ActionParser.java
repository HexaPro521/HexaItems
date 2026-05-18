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

                case "PROJECTILE" -> {
                    // [PROJECTILE] type:speed:yield:incendiary
                    // [PROJECTILE] FIREBALL:1.5:3.0:true
                    // [PROJECTILE] ARROW:2.0
                    // [PROJECTILE] SNOWBALL:1.0
                    try {
                        String[] projParts = actionValue.split(":");
                        if (projParts.length == 0) return;

                        String type  = projParts[0].toUpperCase();
                        double speed = projParts.length > 1 ? Double.parseDouble(projParts[1]) : 1.0;
                        float yield  = projParts.length > 2 ? Float.parseFloat(projParts[2])  : 2.0f;
                        boolean incendiary = projParts.length > 3 && Boolean.parseBoolean(projParts[3]);

                        org.bukkit.util.Vector direction = player.getLocation().getDirection().multiply(speed);
                        org.bukkit.Location spawnLoc = player.getEyeLocation().add(direction);

                        switch (type) {
                            case "FIREBALL" -> {
                                org.bukkit.entity.Fireball e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.Fireball.class);
                                e.setDirection(direction);
                                e.setShooter(player);
                                e.setYield(yield);
                                e.setIsIncendiary(incendiary);
                            }
                            case "SMALL_FIREBALL", "SMALLFIREBALL" -> {
                                org.bukkit.entity.SmallFireball e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.SmallFireball.class);
                                e.setDirection(direction);
                                e.setShooter(player);
                            }
                            case "ARROW" -> {
                                org.bukkit.entity.Arrow e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.Arrow.class);
                                e.setVelocity(direction);
                                e.setShooter(player);
                                e.setPickupStatus(org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED);
                            }
                            case "SPECTRAL_ARROW" -> {
                                org.bukkit.entity.SpectralArrow e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.SpectralArrow.class);
                                e.setVelocity(direction);
                                e.setShooter(player);
                                e.setPickupStatus(org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED);
                            }
                            case "SNOWBALL" -> {
                                org.bukkit.entity.Snowball e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.Snowball.class);
                                e.setVelocity(direction);
                                e.setShooter(player);
                            }
                            case "EGG" -> {
                                org.bukkit.entity.Egg e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.Egg.class);
                                e.setVelocity(direction);
                                e.setShooter(player);
                            }
                            case "ENDER_PEARL" -> {
                                org.bukkit.entity.EnderPearl e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.EnderPearl.class);
                                e.setVelocity(direction);
                                e.setShooter(player);
                            }
                            case "TRIDENT" -> {
                                org.bukkit.entity.Trident e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.Trident.class);
                                e.setVelocity(direction);
                                e.setShooter(player);
                                e.setPickupStatus(org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED);
                            }
                            case "WITHER_SKULL" -> {
                                org.bukkit.entity.WitherSkull e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.WitherSkull.class);
                                e.setDirection(direction);
                                e.setShooter(player);
                                e.setCharged(incendiary);
                            }
                            case "DRAGON_FIREBALL" -> {
                                org.bukkit.entity.DragonFireball e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.DragonFireball.class);
                                e.setDirection(direction);
                                e.setShooter(player);
                            }
                            case "WIND_CHARGE" -> {
                                org.bukkit.entity.WindCharge e = player.getWorld().spawn(spawnLoc, org.bukkit.entity.WindCharge.class);
                                e.setVelocity(direction);
                                e.setShooter(player);
                            }
                            default -> player.sendMessage("§cUnknown projectile type: " + type);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
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