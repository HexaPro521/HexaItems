package net.hexapro.hexaItems;

import net.hexapro.hexaItems.commands.HexaCommand;
import net.hexapro.hexaItems.listeners.ActionListener;
import net.hexapro.hexaItems.managers.RecipeManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HexaItems extends JavaPlugin {

    private static HexaItems instance;
    private final Map<String, ItemStack> customItemsMap = new HashMap<>();
    private final Set<String> edibleItems = new HashSet<>();
    private final Map<String, Map<String, List<String>>> customItemsActionsMap = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        File itemsFolder = new File(getDataFolder(), "items");
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
        }

        loadItems(itemsFolder);
        getLogger().info("Loaded " + customItemsMap.size() + " custom items!");

        getCommand("hexaItems").setExecutor(new HexaCommand());
        getServer().getPluginManager().registerEvents(new ActionListener(), this);

        RecipeManager.loadRecipes(getDataFolder());

        getLogger().info("HexaItems has been enabled!");
    }

    private void loadItems(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadItems(file);
            } else if (file.getName().endsWith(".yml")) {
                loadItemFile(file);
            }
        }
    }

    private void loadItemFile(File file) {
        String path = getDataFolder().toURI().relativize(file.toURI()).toString();
        String id = path.replace(".yml", "").replace("\\", "/").replaceFirst("^items/", "");

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ItemStack item = ItemBuilder.buildItem(id, config);

        if (item == null) {
            getLogger().warning("Failed to load item: " + id);
            return;
        }

        try {
            if (item.getType() == Material.AIR) {
                item.setType(Material.APPLE); // Force it to be an Apple
            }

            if (item.hasItemMeta()) {
                if (item.getItemMeta().hasItemMeta() && config.contains("food:")) {
                    try {
                        org.bukkit.inventory.meta.components.FoodComponent food = item.getItemMeta().getFood();
                        food.setNutrition(config.getInt("food.nutrition", 0));
                        food.setSaturation((float) config.getDouble("food.saturation", 0.0));
                        food.setCanAlwaysEat(config.getBoolean("food.can_always_equip", false));
                        item.getItemMeta().setFood(food);
                        item.setItemMeta(item.getItemMeta());
                    } catch (Exception ignored) {}
                }
            }

            customItemsMap.put(id, item);
            edibleItems.add(id.toLowerCase());

            Map<String, List<String>> actionMap = new HashMap<>();
            if (config.contains("actions:")) {
                for (String trigger : config.getConfigurationSection("actions").getKeys(false)) {
                    actionMap.put(trigger, config.getStringList("actions." + trigger));
                }
            }
            customItemsActionsMap.put(id, actionMap);

        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable() {}

    public static HexaItems getInstance() {
        return instance;
    }

    public ItemStack getCustomItem(String id) {
        return customItemsMap.get(id.toLowerCase());
    }

    public Set<String> getEdibleItems() {
        return edibleItems;
    }

    public List<String> getActions(String id, String trigger) {
        Map<String, List<String>> triggers = customItemsActionsMap.get(id.toLowerCase());
        if (triggers != null) {
            return triggers.get(trigger.toLowerCase());
        }
        return null;
    }
}
