package net.hexapro.hexaItems;

import net.hexapro.hexaItems.commands.CmdBase;
import net.hexapro.hexaItems.config.ConfigManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import net.hexapro.hexaItems.listeners.ActionListener;

import java.util.List;

public class HexaItems extends JavaPlugin {

    // Variables
    private static HexaItems instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Accessing method
        instance = this;

        // Config setup
        configManager = new ConfigManager(this);

        // The commands setup
        getCommand("hexaitems").setExecutor(new CmdBase());

        // Listener setup
        getServer().getPluginManager().registerEvents(new ActionListener(), this);

        // onEnable message
        getLogger().info("HexaItems has been enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // onDisable message
        getLogger().info("HexaItems has been disabled");
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    public static HexaItems getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ItemStack getCustomItem(String id) {
        return configManager.getBuiltItem(id);
    }

    public List<String> getActions(String id, String trigger) {
        return configManager.getActions(id, trigger);
    }

    public List<String> getEdibleItems() {
        return configManager.getEdibleItems();
    }
}