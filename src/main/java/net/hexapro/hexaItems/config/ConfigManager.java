package net.hexapro.hexaItems.config;

import net.hexapro.hexaItems.HexaItems;
import net.hexapro.hexaItems.util.ItemBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final HexaItems plugin;
    private FileConfiguration config;
    private File configFile;
    private File itemsFolder;

    // ← fields at the top
    private final Map<String, ItemStack> builtItems = new HashMap<>();
    private final Map<String, Map<String, List<String>>> itemActions = new HashMap<>();
    private final List<String> edibleItems = new ArrayList<>();

    public ConfigManager(HexaItems plugin) {
        this.plugin = plugin;
        this.itemsFolder = new File(plugin.getDataFolder(), "items");
        setup();
    }

    private void setup() {
        // --- config.yml
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // --- items/ folder
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
            plugin.saveResource("items/examples/exampleitem.yml", false);
        }

        // --- load all items
        loadItems(); // ← called here
    }

    // ← loadItems goes here
    private void loadItems() {
        builtItems.clear();
        itemActions.clear();
        edibleItems.clear();

        List<File> allFiles = getAllYmlFiles(itemsFolder);

        for (File file : allFiles) {
            String id = file.getName().replace(".yml", "");
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ItemStack item = ItemBuilder.buildItem(id, cfg);

            if (item != null) {
                builtItems.put(id, item);

                // load actions
                Map<String, List<String>> triggers = new HashMap<>();
                for (String trigger : List.of("right_click", "left_click", "eat", "hit",
                        "equipped", "unequipped", "while_equipped",
                        "drop", "sneak", "jump", "mob_drop")) {
                    List<String> actions = cfg.getStringList("actions." + trigger);
                    if (!actions.isEmpty()) triggers.put(trigger, actions);
                }
                itemActions.put(id, triggers);

                // track edible items
                if (cfg.contains("food")) edibleItems.add(id);

                plugin.getLogger().info("Loaded item: " + id);
            } else {
                plugin.getLogger().warning("Failed to load item '" + id + "'! Check the 'material' field in " + file.getName());
            }
        }
    }

    // ← getAllYmlFiles goes here
    private List<File> getAllYmlFiles(File folder) {
        List<File> result = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(getAllYmlFiles(file));
            } else if (file.getName().endsWith(".yml")) {
                result.add(file);
            }
        }
        return result;
    }

    // ← reload goes here
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadItems();
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ← getters go here at the bottom
    public ItemStack getBuiltItem(String id) { return builtItems.get(id); }
    public List<String> getActions(String id, String trigger) {
        Map<String, List<String>> triggers = itemActions.get(id);
        if (triggers == null) return null;
        return triggers.get(trigger);
    }
    public List<String> getEdibleItems() { return edibleItems; }

    public String getString(String path, String def) { return config.getString(path, def); }
    public int getInt(String path, int def) { return config.getInt(path, def); }
    public boolean getBoolean(String path, boolean def) { return config.getBoolean(path, def); }
    public List<String> getStringList(String path) { return config.getStringList(path); }
    public void set(String path, Object value) { config.set(path, value); save(); }
}