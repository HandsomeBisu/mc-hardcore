package io.github.mikeandjoski.hardcore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import io.github.mikeandjoski.hardcore.StatEffectManager;

public class GUIListener implements Listener {

    private final StatsManager statsManager;

    public GUIListener(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    public static int getUpgradeCost(int currentLevel) {
        return 5 + (currentLevel - 1);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§l스탯 정보")) {
            return; // 우리가 만든 GUI가 아니면 무시
        }

        event.setCancelled(true); // 아이템을 꺼내지 못하게 막기

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        PlayerStats stats = statsManager.getPlayerStats(player);
        int requiredLevel = 0;
        int currentLevel = 0;

        switch (clickedItem.getType()) {
            case GOLDEN_APPLE:
                currentLevel = stats.getHealthLevel();
                requiredLevel = getUpgradeCost(currentLevel);
                break;
            case SUGAR:
                currentLevel = stats.getStaminaLevel();
                requiredLevel = getUpgradeCost(currentLevel);
                break;
            case IRON_SWORD: // 공격력은 커스텀 경험치 사용
                if (stats.getAttackPowerExp() < stats.getAttackPowerLevelUpExp()) {
                    player.sendMessage(ChatColor.RED + "공격력 경험치가 부족합니다! (현재: " + String.format("%.0f", stats.getAttackPowerExp()) + " / 필요: " + String.format("%.0f", stats.getAttackPowerLevelUpExp()) + ")");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.4f); // 실패 효과음
                    player.closeInventory();
                    return;
                }
                // 경험치 차감 및 레벨업은 아래 switch문에서 처리
                break;
            case IRON_CHESTPLATE:
                currentLevel = stats.getDefenseLevel();
                requiredLevel = getUpgradeCost(currentLevel);
                break;
            default:
                return; // 스탯 아이템이 아니면 무시
        }

        // 공격력 외 다른 스탯은 마인크래프트 레벨 사용
        if (clickedItem.getType() != Material.IRON_SWORD && player.getLevel() < requiredLevel) {
            player.sendMessage(ChatColor.RED + "레벨이 부족합니다! (필요 레벨: " + requiredLevel + ")");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.4f); // 실패 효과음
            player.closeInventory();
            return;
        }

        boolean upgraded = false;
        switch (clickedItem.getType()) {
            case GOLDEN_APPLE:
                stats.setHealthLevel(stats.getHealthLevel() + 1);
                player.setLevel(player.getLevel() - requiredLevel);
                upgraded = true;
                break;
            case SUGAR:
                stats.setStaminaLevel(stats.getStaminaLevel() + 1);
                stats.setCurrentStamina(stats.getMaxStamina()); // 최대치로 채우기
                player.setLevel(player.getLevel() - requiredLevel);
                upgraded = true;
                break;
            case IRON_SWORD:
                // 공격력 레벨업 처리
                stats.setAttackPowerExp(stats.getAttackPowerExp() - stats.getAttackPowerLevelUpExp());
                stats.setAttackPowerLevel(stats.getAttackPowerLevel() + 1);
                stats.setAttackPowerLevelUpExp(stats.getAttackPowerLevelUpExp() * 1.2); // 다음 레벨업 필요 경험치 증가
                upgraded = true;
                break;
            case IRON_CHESTPLATE:
                stats.setDefenseLevel(stats.getDefenseLevel() + 1);
                player.setLevel(player.getLevel() - requiredLevel);
                upgraded = true;
                break;
        }

        if (upgraded) {
            player.sendMessage(ChatColor.GREEN + "레벨업에 성공했습니다!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f); // 성공 효과음
            
            // 스탯 효과 적용
            StatEffectManager.applyStats(player, stats);

            StatGUI.openGUI(player, stats); // GUI 새로고침
        } else {
            player.sendMessage(ChatColor.RED + "알 수 없는 오류가 발생했습니다.");
        }
    }
}