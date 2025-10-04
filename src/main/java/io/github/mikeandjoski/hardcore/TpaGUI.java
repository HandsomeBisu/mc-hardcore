package io.github.mikeandjoski.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class TpaGUI {

    public static final String GUI_TITLE = "§l텔레포트 요청";

    public static void openGUI(Player sender) {
        // Calculate size needed (multiples of 9)
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(sender); // Don't show self

        if (onlinePlayers.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "현재 텔레포트 요청을 보낼 수 있는 다른 플레이어가 없습니다.");
            return;
        }

        int size = (onlinePlayers.size() / 9 + 1) * 9; // At least 1 row
        if (size > 54) size = 54; // Max 6 rows

        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        for (Player target : onlinePlayers) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
            meta.setOwningPlayer(target); // Set the head to the target player
            meta.setDisplayName(ChatColor.YELLOW + target.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7클릭하여 텔레포트 요청");
            meta.setLore(lore);
            playerHead.setItemMeta(meta);
            gui.addItem(playerHead);
        }

        sender.openInventory(gui);
    }
}
