package net.hexapro.hexaItems.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.hexapro.hexaItems.HexaItems;
import net.hexapro.hexaItems.util.ActionParser;
import net.hexapro.hexaItems.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActionListener implements Listener {

    private final Map<UUID, BukkitTask> passiveEffects = new HashMap<>();

    // --- gets the HexaItems ID from an item, or null if it's not a HexaItems item
    private String getHexaId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) return null;
        if (!item.getItemMeta().getPersistentDataContainer().has(ItemBuilder.HEXA_TAG, PersistentDataType.STRING)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ItemBuilder.HEXA_TAG, PersistentDataType.STRING);
    }

    // --- gets the actions for an item and trigger
    private List<String> getActions(String id, String trigger) {
        return HexaItems.getInstance().getActions(id, trigger);
    }

    // ==========================================
    // TRIGGER: EAT
    // ==========================================
    @EventHandler(priority = EventPriority.LOW)
    public void onEat(PlayerItemConsumeEvent event) {
        String id = getHexaId(event.getItem());
        if (id == null) return;
        if (!HexaItems.getInstance().getEdibleItems().contains(id)) return;

        List<String> actions = getActions(id, "eat");
        if (actions != null) {
            ActionParser.executeActions(event.getPlayer(), actions);
        }
    }

    // ==========================================
    // TRIGGER: RIGHT CLICK
    // ==========================================
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        String id = getHexaId(event.getItem());
        if (id == null) return;

        List<String> actions = getActions(id, "right_click");

        // don't cancel if it's a food item — let Minecraft handle eating
        boolean isFood = HexaItems.getInstance().getEdibleItems().contains(id);
        if (!isFood && actions != null) {
            event.setCancelled(true);
        }

        if (actions != null) ActionParser.executeActions(event.getPlayer(), actions);
    }

    // ==========================================
    // TRIGGER: LEFT CLICK
    // ==========================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return; // prevent double firing
        String id = getHexaId(event.getItem());
        if (id == null) return;

        event.setCancelled(true);
        List<String> actions = getActions(id, "left_click");
        if (actions != null) ActionParser.executeActions(event.getPlayer(), actions);
    }

    // ==========================================
    // TRIGGER: HIT
    // ==========================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        String id = getHexaId(player.getInventory().getItemInMainHand());
        if (id == null) return;

        List<String> actions = getActions(id, "hit");
        if (actions != null) ActionParser.executeActions(player, actions);
    }

    // ==========================================
    // TRIGGER: EQUIPPED & UNEQUIPPED
    // ==========================================
    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        // --- unequipped
        ItemStack oldItem = event.getOldItem();
        if (oldItem != null && oldItem.getType() != Material.AIR) {
            String id = getHexaId(oldItem);
            if (id != null) {
                List<String> actions = getActions(id, "unequipped");
                if (actions != null) ActionParser.executeActions(event.getPlayer(), actions);
                stopPassiveEffect(event.getPlayer());
            }
        }

        // --- equipped
        ItemStack newItem = event.getNewItem();
        if (newItem != null && newItem.getType() != Material.AIR) {
            String id = getHexaId(newItem);
            if (id != null) {
                List<String> actions = getActions(id, "equipped");
                if (actions != null) ActionParser.executeActions(event.getPlayer(), actions);

                // start passive effect if while_equipped actions exist
                List<String> passiveActions = getActions(id, "while_equipped");
                if (passiveActions != null && !passiveActions.isEmpty()) {
                    startPassiveEffect(event.getPlayer(), passiveActions);
                }
            }
        }
    }

    // ==========================================
    // TRIGGER: DROP
    // ==========================================
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        String id = getHexaId(event.getItemDrop().getItemStack());
        if (id == null) return;

        List<String> actions = getActions(id, "drop");
        if (actions != null) ActionParser.executeActions(event.getPlayer(), actions);
    }

    // ==========================================
    // TRIGGER: SNEAK
    // ==========================================
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        String id = getHexaId(event.getPlayer().getInventory().getItemInMainHand());
        if (id == null) return;

        List<String> actions = getActions(id, "sneak");
        if (actions != null) ActionParser.executeActions(event.getPlayer(), actions);
    }

    // ==========================================
    // TRIGGER: JUMP
    // ==========================================
    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        String id = getHexaId(event.getPlayer().getInventory().getItemInMainHand());
        if (id == null) return;

        List<String> actions = getActions(id, "jump");
        if (actions != null) ActionParser.executeActions(event.getPlayer(), actions);
    }

    // ==========================================
    // TRIGGER: MOB DROP
    // ==========================================
    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();

        ItemStack weapon = killer.getInventory().getItemInMainHand(); // ← use killer's weapon, not mob's
        String id = getHexaId(weapon);
        if (id == null) return;

        List<String> actions = getActions(id, "mob_drop");
        if (actions != null) ActionParser.executeActions(killer, actions);
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================
    private void startPassiveEffect(Player player, List<String> actions) {
        stopPassiveEffect(player); // stop any existing task first
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                HexaItems.getInstance(),
                () -> {
                    if (player.isOnline()) {
                        ActionParser.executeActions(player, actions);
                    }
                },
                20L,  // delay before first run (1 second)
                60L   // repeat every 3 seconds
        );
        passiveEffects.put(player.getUniqueId(), task);
    }

    private void stopPassiveEffect(Player player) {
        BukkitTask task = passiveEffects.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }
}