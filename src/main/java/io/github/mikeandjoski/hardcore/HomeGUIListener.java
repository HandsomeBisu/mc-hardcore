package io.github.mikeandjoski.hardcore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeGUIListener implements Listener {

    private final Hardcore plugin;
    private final Map<UUID, Integer> setHomeMode = new HashMap<>();

    public HomeGUIListener(Hardcore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(HomeGUI.GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot() - 2; // Adjust for the offset in the GUI
        HomeManager homeManager = new HomeManager(plugin);
        homeManager.loadHomes(player);

        // Handle Teleport
        if (clickedItem.getType() == Material.RED_BED && event.getClick() == ClickType.LEFT) {
            Home home = homeManager.getHome(slot);
            if (home != null) {
                player.teleport(home.getLocation());
                player.sendMessage(ChatColor.GREEN + "'" + home.getName() + "' 집으로 이동했습니다.");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
            }
        }

        // Handle Delete Home
        if (clickedItem.getType() == Material.RED_BED && event.getClick() == ClickType.SHIFT_RIGHT) {
            Home home = homeManager.getHome(slot);
            if (home != null) {
                homeManager.deleteHome(slot);
                homeManager.saveHomes(player);
                player.sendMessage(ChatColor.RED + "'" + home.getName() + "' 집을 삭제했습니다.");
                player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
                HomeGUI.openGUI(player, plugin); // Refresh GUI
            }
        }

        // Handle Initiate Set Home
        if (clickedItem.getType() == Material.MAP && event.getClick() == ClickType.RIGHT) {
            setHomeMode.put(player.getUniqueId(), slot);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "채팅으로 설정할 집의 이름을 입력하세요. 취소하려면 '취소'라고 입력하세요.");
        }

        // Handle Unlock Slot
        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            int currentUnlocked = homeManager.getUnlockedSlots();
            if (currentUnlocked >= 5) {
                player.sendMessage(ChatColor.RED + "모든 집 슬롯이 이미 해금되었습니다.");
                return;
            }

            // The slot being clicked is `currentUnlocked`, cost is for this new slot.
            int cost = 10 + (currentUnlocked - 1) * 5;

            if (player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), cost)) {
                player.getInventory().removeItem(new ItemStack(Material.DIAMOND, cost));
                homeManager.unlockSlot();
                homeManager.saveHomes(player);

                player.sendMessage(ChatColor.GREEN + "새로운 집 슬롯을 해금했습니다!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                HomeGUI.openGUI(player, plugin); // Refresh GUI
            } else {
                player.sendMessage(ChatColor.RED + "슬롯을 해금하려면 다이아몬드가 " + cost + "개 필요합니다.");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                player.closeInventory();
            }
        }

        // TODO: Handle delete
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (setHomeMode.containsKey(playerId)) {
            event.setCancelled(true);
            String message = event.getMessage();
            int slot = setHomeMode.get(playerId);

            if (message.equalsIgnoreCase("취소")) {
                setHomeMode.remove(playerId);
                player.sendMessage(ChatColor.RED + "집 설정을 취소했습니다.");
                return;
            }

            // Bukkit API calls should be run on the main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                HomeManager homeManager = new HomeManager(plugin);
                homeManager.loadHomes(player);
                homeManager.setHome(slot, message, player.getLocation());
                homeManager.saveHomes(player);
                setHomeMode.remove(playerId);
                player.sendMessage(ChatColor.GREEN + "'" + message + "' 이름으로 집을 설정했습니다!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            });
        }
    }
}
