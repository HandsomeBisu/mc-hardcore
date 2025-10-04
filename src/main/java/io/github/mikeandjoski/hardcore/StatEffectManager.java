package io.github.mikeandjoski.hardcore;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import io.github.mikeandjoski.hardcore.PlayerStats;

public class StatEffectManager {

    public static void applyStats(Player player, PlayerStats stats) {
        // 체력 스탯 적용
        applyHealthStat(player, stats);
        // 방어력 스탯 적용
        applyDefenseStat(player, stats);
        // 공격력 스탯 적용
        applyAttackPowerStat(player, stats);

        // 여기에 나중에 다른 스탯(지구력 등) 적용 로직을 추가할 수 있습니다.
    }

    private static void applyHealthStat(Player player, PlayerStats stats) {
        int healthLevel = stats.getHealthLevel();
        // 기본 체력 12 (6칸) + (레벨 - 1) * 1 (0.5칸)
        double newMaxHealth = 12.0 + (healthLevel - 1);

        AttributeInstance maxHealthAttribute = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(newMaxHealth);
        }
    }

    private static void applyDefenseStat(Player player, PlayerStats stats) {
        AttributeInstance armorAttribute = player.getAttribute(Attribute.GENERIC_ARMOR);
        if (armorAttribute != null) {
            // 방어력 스탯 1레벨당 1 방어력 증가 (레벨 3까지는 페널티)
            double newArmor = (stats.getDefenseLevel() - 3) * 1.0;
            armorAttribute.setBaseValue(newArmor);
        }
    }

    private static void applyAttackPowerStat(Player player, PlayerStats stats) {
        AttributeInstance attackDamageAttribute = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackDamageAttribute != null) {
            // 기본 공격력 1.0 (바닐라 주먹 2.0보다 약함) + (레벨 - 1) * 0.25
            // 레벨 1: 1.0
            // 레벨 2: 1.25
            // 레벨 3: 1.5
            // 레벨 4: 1.75
            // 레벨 5: 2.0 (바닐라 주먹과 동일)
            double newAttackDamage = 1.0 + (stats.getAttackPowerLevel() - 1) * 0.25;
            attackDamageAttribute.setBaseValue(newAttackDamage);
        }
    }
}
