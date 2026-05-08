package net.hexapro.hexaItems;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    public static ItemStack buildItem(String id, FileConfiguration config) {
        String materialName = config.getString("material", "STONE");
        Material material = Material.getMaterial(materialName.toUpperCase());
        
        if (material == null) {
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta == null) {
            return item;
        }

        // Set display name
        if (config.contains("name")) {
            meta.setDisplayName(colorize(config.getString("name")));
        }

        // Set lore
        if (config.contains("lore")) {
            List<String> lore = config.getStringList("lore");
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(colorize(line));
            }
            meta.setLore(coloredLore);
        }

        // Set custom model data
        if (config.contains("custom-model-data")) {
            meta.setCustomModelData(config.getInt("custom-model-data"));
        }

        // Set enchantments
        if (config.contains("enchantments")) {
            for (String enchant : config.getConfigurationSection("enchantments").getKeys(false)) {
                Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(enchant.toLowerCase()));
                if (enchantment != null) {
                    int level = config.getInt("enchantments." + enchant);
                    meta.addEnchant(enchantment, level, true);
                }
            }
        }

        // Set item flags
        if (config.contains("flags")) {
            for (String flag : config.getStringList("flags")) {
                try {
                    ItemFlag itemFlag = ItemFlag.valueOf(flag.toUpperCase());
                    meta.addItemFlags(itemFlag);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Set unbreakable
        if (config.getBoolean("unbreakable", false)) {
            meta.setUnbreakable(true);
        }

        // Set glow (using enchantment cache)
        if (config.getBoolean("glow", false)) {
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // Set leather armor color
        if (config.contains("color") && material.toString().contains("LEATHER_")) {
            String colorStr = config.getString("color");
            Color color = parseColor(colorStr);
            if (color != null && meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) meta).setColor(color);
            }
        }

        // Add PersistentDataContainer with item ID
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new org.bukkit.NamespacedKey(HexaItems.getInstance(), "item_id"), PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }

    private static String colorize(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }

    private static Color parseColor(String colorStr) {
        if (colorStr == null) return null;
        try {
            String[] parts = colorStr.split(",");
            if (parts.length == 3) {
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                return Color.fromRGB(r, g, b);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
