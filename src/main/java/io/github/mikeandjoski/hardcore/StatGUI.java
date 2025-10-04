package io.github.mikeandjoski.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatGUI {

    public static void openGUI(Player player, PlayerStats stats) {
        Inventory gui = Bukkit.createInventory(null, 27, "§l스탯 정보");

        // Health Stat Item
        ItemStack health = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta healthMeta = health.getItemMeta();
        healthMeta.setDisplayName("§c체력");
        int healthCost = GUIListener.getUpgradeCost(stats.getHealthLevel());
        double currentMaxHealth = 12.0 + (stats.getHealthLevel() - 1);
        List<String> healthLore = new ArrayList<>();
        healthLore.add("§7현재 레벨: §e" + stats.getHealthLevel());
        healthLore.add("§7현재 최대 체력: §e" + String.format("%.1f", currentMaxHealth));
        healthLore.add("");
        healthLore.add("§a클릭하여 업그레이드 (비용: §e" + healthCost + "§a레벨)");
        healthMeta.setLore(healthLore);
        health.setItemMeta(healthMeta);

        // Stamina Stat Item
        ItemStack stamina = new ItemStack(Material.SUGAR);
        ItemMeta staminaMeta = stamina.getItemMeta();
        staminaMeta.setDisplayName("§a지구력");
        int staminaCost = GUIListener.getUpgradeCost(stats.getStaminaLevel());
        double currentMaxStamina = stats.getMaxStamina();
        List<String> staminaLore = new ArrayList<>();
        staminaLore.add("§7현재 레벨: §e" + stats.getStaminaLevel());
        staminaLore.add("§7현재 최대 지구력: §e" + String.format("%.0f", currentMaxStamina));
        staminaLore.add("");
        staminaLore.add("§a클릭하여 업그레이드 (비용: §e" + staminaCost + "§a레벨)");
        staminaMeta.setLore(staminaLore);
        stamina.setItemMeta(staminaMeta);

        // Attack Power Stat Item
        ItemStack attackPower = new ItemStack(Material.IRON_SWORD);
        ItemMeta attackPowerMeta = attackPower.getItemMeta();
        attackPowerMeta.setDisplayName("§9공격력");
        double currentAttackDamage = 1.0 + (stats.getAttackPowerLevel() - 1) * 0.25;
        List<String> attackLore = new ArrayList<>();
        attackLore.add("§7현재 레벨: §e" + stats.getAttackPowerLevel());
        attackLore.add("§7현재 공격력: §e" + String.format("%.2f", currentAttackDamage));
        attackLore.add("§7경험치: §e" + String.format("%.0f", stats.getAttackPowerExp()) + " §7/ §e" + String.format("%.0f", stats.getAttackPowerLevelUpExp()));
        attackLore.add("");
        attackLore.add("§a클릭하여 레벨업");
        attackPowerMeta.setLore(attackLore);
        attackPower.setItemMeta(attackPowerMeta);

        // Defense Stat Item
        ItemStack defense = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta defenseMeta = defense.getItemMeta();
        defenseMeta.setDisplayName("§8방어력");
        int defenseCost = GUIListener.getUpgradeCost(stats.getDefenseLevel());
        double currentArmor = (stats.getDefenseLevel() - 3) * 1.0;
        List<String> defenseLore = new ArrayList<>();
        defenseLore.add("§7현재 레벨: §e" + stats.getDefenseLevel());
        defenseLore.add("§7현재 방어력: §e" + String.format("%.1f", currentArmor));
        defenseLore.add("");
        defenseLore.add("§a클릭하여 업그레이드 (비용: §e" + defenseCost + "§a레벨)");
        defenseMeta.setLore(defenseLore);
        defense.setItemMeta(defenseMeta);

        gui.setItem(10, health);
        gui.setItem(12, stamina);
        gui.setItem(14, defense);
        gui.setItem(16, attackPower);

        player.openInventory(gui);
    }
}