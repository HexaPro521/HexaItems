package net.hexapro.hexaItems.util;

import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.keys.SoundEventKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import org.bukkit.inventory.meta.components.FoodComponent;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    public static final NamespacedKey HEXA_TAG = new NamespacedKey("hexaitems", "id");

    public static ItemStack buildItem(String id, FileConfiguration config) {

        // --- MATERIAL
        Material material = Material.matchMaterial(config.getString("material"));
        if (material == null) {
            Bukkit.getLogger().warning("[HexaItems] Item '" + id + "' is missing the 'material' field!");
            return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        // --- NAME
        String nameConfig = config.getString("name");
        if (nameConfig == null) {
            Bukkit.getLogger().warning("[HexaItems] Item '" + id + "' is missing the 'material' field!");
            return null;
        } else {
            Component nameComponent = MiniMessage.miniMessage()
                    .deserialize(translateCodes(nameConfig))
                    .decoration(TextDecoration.ITALIC, false);
            meta.displayName(nameComponent);
        }

        // --- LORE
        List<String> loreStr = config.getStringList("lore");
        if (!loreStr.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreStr) {
                lore.add(MiniMessage.miniMessage()
                        .deserialize(translateCodes(line))
                        .decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        }

        // --- ITEM MODEL
        String modelString = config.getString("item_model");
        if (modelString != null) {
            try {
                NamespacedKey modelKey = modelString.contains(":")
                        ? NamespacedKey.fromString(modelString.toLowerCase())
                        : NamespacedKey.minecraft(modelString.toLowerCase());
                if (modelKey != null) meta.setItemModel(modelKey);
            } catch (Exception ignored) {}
        }

        // --- FOOD COMPONENT
        if (config.contains("food")) {
            try {
                // food stats — on meta
                FoodComponent food = meta.getFood();
                food.setNutrition(config.getInt("food.nutrition", 0));
                food.setSaturation((float) config.getDouble("food.saturation", 0.0));
                food.setCanAlwaysEat(config.getBoolean("food.can_always_eat", false));
                meta.setFood(food);

                // apply meta first
                item.setItemMeta(meta);

                // make it consumable — on item!
                item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                        .consumeSeconds((float) config.getDouble("food.consume_seconds", 1.6f))
                        .animation(ItemUseAnimation.EAT)
                        .sound(SoundEventKeys.ENTITY_GENERIC_EAT)
                        .build());

                // refresh meta so later code still works
                meta = item.getItemMeta();

            } catch (Exception e) { e.printStackTrace(); }
        }

        // --- STACKABLE
        if (config.contains("stackable")) {
            Object stackable = config.get("stackable");

            if (stackable instanceof Boolean) {
                // true = 64, false = 1
                meta.setMaxStackSize((Boolean) stackable ? 64 : 1);
            } else if (stackable instanceof Integer) {
                // custom stack size between 1 and 99
                int size = Math.max(1, Math.min(99, (Integer) stackable));
                meta.setMaxStackSize(size);
            }
        }

        // --- WEAPON STATS
        if (config.contains("weapon")) {
            try {
                if (config.contains("weapon.attack_damage")) {
                    NamespacedKey key = new NamespacedKey("hexaitems", id + "_damage");
                    AttributeModifier mod = new AttributeModifier(
                            key, config.getDouble("weapon.attack_damage"),
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.MAINHAND);
                    meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, mod);
                }
                if (config.contains("weapon.attack_speed")) {
                    NamespacedKey key = new NamespacedKey("hexaitems", id + "_speed");
                    AttributeModifier mod = new AttributeModifier(
                            key, config.getDouble("weapon.attack_speed"),
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.MAINHAND);
                    meta.addAttributeModifier(Attribute.ATTACK_SPEED, mod);
                }
                if (config.contains("weapon.attack_knockback")) {
                    NamespacedKey key = new NamespacedKey("hexaitems", id + "_knockback");
                    AttributeModifier mod = new AttributeModifier(
                            key, config.getDouble("weapon.attack_knockback"),
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.MAINHAND);
                    meta.addAttributeModifier(Attribute.ATTACK_KNOCKBACK, mod);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // --- ARMOR STATS
        if (config.contains("armor")) {
            try {
                EquipmentSlot equipSlot = EquipmentSlot.valueOf(
                        config.getString("armor.slot", "CHEST").toUpperCase());
                EquipmentSlotGroup slotGroup = switch (equipSlot) {
                    case HEAD -> EquipmentSlotGroup.HEAD;
                    case CHEST -> EquipmentSlotGroup.CHEST;
                    case LEGS -> EquipmentSlotGroup.LEGS;
                    case FEET -> EquipmentSlotGroup.FEET;
                    default -> EquipmentSlotGroup.ANY;
                };

                var equippable = meta.getEquippable();
                equippable.setSlot(equipSlot);
                meta.setEquippable(equippable);

                if (config.contains("armor.armor")) {
                    NamespacedKey key = new NamespacedKey("hexaitems", id + "_armor");
                    AttributeModifier mod = new AttributeModifier(
                            key, config.getDouble("armor.armor"),
                            AttributeModifier.Operation.ADD_NUMBER, slotGroup);
                    meta.addAttributeModifier(Attribute.ARMOR, mod);
                }
                if (config.contains("armor.armor_toughness")) {
                    NamespacedKey key = new NamespacedKey("hexaitems", id + "_toughness");
                    AttributeModifier mod = new AttributeModifier(
                            key, config.getDouble("armor.armor_toughness"),
                            AttributeModifier.Operation.ADD_NUMBER, slotGroup);
                    meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, mod);
                }
                if (config.contains("armor.knockback_resistance")) {
                    NamespacedKey key = new NamespacedKey("hexaitems", id + "_kbr");
                    AttributeModifier mod = new AttributeModifier(
                            key, config.getDouble("armor.knockback_resistance"),
                            AttributeModifier.Operation.ADD_NUMBER, slotGroup);
                    meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, mod);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // --- APPLY TAG AND META
        item.setItemMeta(meta);
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        // Note: get a fresh meta after setItemMeta
        ItemMeta finalMeta = item.getItemMeta();
        finalMeta.getPersistentDataContainer().set(HEXA_TAG, PersistentDataType.STRING, id);
        item.setItemMeta(finalMeta);

        return item;
    }

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