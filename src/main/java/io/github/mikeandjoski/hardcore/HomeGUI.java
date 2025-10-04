package io.github.mikeandjoski.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeGUI {

    public static final String GUI_TITLE = "§l내 집 목록";

    public static void openGUI(Player player, Hardcore plugin) {
        Inventory gui = Bukkit.createInventory(null, 9, GUI_TITLE);
        HomeManager homeManager = new HomeManager(plugin);
        homeManager.loadHomes(player);

        int unlockedSlots = homeManager.getUnlockedSlots();
        Map<Integer, Home> homes = homeManager.getHomes();

        for (int i = 0; i < 5; i++) {
            ItemStack item;
            ItemMeta meta;
            List<String> lore = new ArrayList<>();

            if (i < unlockedSlots) {
                Home home = homes.get(i);
                if (home != null) {
                    // Slot with a home set
                    item = new ItemStack(Material.RED_BED);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.GREEN + "§l" + home.getName());
                    Location homeLocation = home.getLocation();
                    lore.add(String.format("§7좌표: §e%d, %d, %d", homeLocation.getBlockX(), homeLocation.getBlockY(), homeLocation.getBlockZ()));
                    lore.add("");
                    lore.add("§a좌클릭하여 이동하기");
                    lore.add("§c쉬프트+우클릭하여 삭제하기");
                } else {
                    // Empty, unlocked slot
                    item = new ItemStack(Material.MAP);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.YELLOW + "§l빈 슬롯 " + (i + 1));
                    lore.add("§7이 슬롯은 비어있습니다.");
                    lore.add("");
                    lore.add("§b우클릭하여 현재 위치를 집으로 설정");
                }
            } else {
                // Locked slots
                item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "§l잠긴 슬롯 " + (i + 1));
                lore.add("§7이 슬롯은 잠겨있습니다.");

                // Cost is for the NEXT available slot, which is at index 'unlockedSlots'
                if (i == unlockedSlots) {
                    int cost = 10 + (unlockedSlots - 1) * 5;
                    lore.add("");
                    lore.add("§b클릭하여 해금 (비용: 다이아 §e" + cost + "§b개)");
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(i + 2, item); // Center the 5 slots
        }
        player.openInventory(gui);
    }
}