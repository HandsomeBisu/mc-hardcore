package io.github.mikeandjoski.hardcore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class TpaGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TpaGUI.GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);
        Player sender = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) {
            return;
        }

        SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null) {
            return;
        }

        Player target = (Player) meta.getOwningPlayer();

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "해당 플레이어는 현재 오프라인입니다.");
            sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            sender.closeInventory();
            return;
        }

        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.RED + "자신에게 텔레포트 요청을 보낼 수 없습니다.");
            sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            sender.closeInventory();
            return;
        }

        TpaManager tpaManager = Hardcore.getTpaManager();

        if (tpaManager.hasSentRequest(sender)) {
            sender.sendMessage(ChatColor.RED + "이미 보낸 텔레포트 요청이 있습니다. 응답을 기다리거나 /tpacancel 로 취소하세요.");
            sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            sender.closeInventory();
            return;
        }

        if (tpaManager.hasRequest(target)) {
            sender.sendMessage(ChatColor.RED + target.getName() + "님은 이미 다른 플레이어의 텔레포트 요청을 받았습니다.");
            sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            sender.closeInventory();
            return;
        }

        tpaManager.addRequest(sender, target);
        sender.sendMessage(ChatColor.GREEN + target.getName() + "님에게 텔레포트 요청을 보냈습니다. 60초 내에 응답해야 합니다.");
        target.sendMessage(ChatColor.YELLOW + sender.getName() + "님이 당신에게 텔레포트 요청을 보냈습니다.");
        target.sendMessage(ChatColor.YELLOW + "수락하려면 /tpac 또는 /tpaccept, 거절하려면 /tpdeny 를 입력하세요. (60초)");
        sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        sender.closeInventory();
    }
}
