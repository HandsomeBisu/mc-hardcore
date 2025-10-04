package io.github.mikeandjoski.hardcore;

import org.bukkit.configuration.ConfigurationSection;

public class PlayerStats {

    private int healthLevel;
    private int staminaLevel;
    private int attackPowerLevel;
    private int defenseLevel;
    private double currentStamina;
    private boolean exhausted;

    private double attackPowerExp; // 공격력 경험치
    private double attackPowerLevelUpExp; // 다음 레벨업에 필요한 공격력 경험치

    public PlayerStats() {
        this.healthLevel = 1;
        this.staminaLevel = 1;
        this.attackPowerLevel = 1;
        this.defenseLevel = 1;
        this.currentStamina = getMaxStamina();
        this.exhausted = false;
        this.attackPowerExp = 0.0;
        this.attackPowerLevelUpExp = 100.0; // 초기 레벨업 필요 경험치
    }

    // ConfigurationSection에서 스탯을 불러오는 생성자
    public PlayerStats(ConfigurationSection config) {
        this.healthLevel = config.getInt("healthLevel", 1);
        this.staminaLevel = config.getInt("staminaLevel", 1);
        this.attackPowerLevel = config.getInt("attackPowerLevel", 1);
        this.defenseLevel = config.getInt("defenseLevel", 1);
        this.currentStamina = config.getDouble("currentStamina", getMaxStamina());
        this.exhausted = config.getBoolean("exhausted", false);
        this.attackPowerExp = config.getDouble("attackPowerExp", 0.0);
        this.attackPowerLevelUpExp = config.getDouble("attackPowerLevelUpExp", 100.0);
    }

    // 현재 스탯을 ConfigurationSection에 저장하는 메소드
    public void save(ConfigurationSection config) {
        config.set("healthLevel", healthLevel);
        config.set("staminaLevel", staminaLevel);
        config.set("attackPowerLevel", attackPowerLevel);
        config.set("defenseLevel", defenseLevel);
        config.set("currentStamina", currentStamina);
        config.set("exhausted", exhausted);
        config.set("attackPowerExp", attackPowerExp);
        config.set("attackPowerLevelUpExp", attackPowerLevelUpExp);
    }

    public double getMaxStamina() {
        return 100.0 + (this.staminaLevel - 1) * 30.0;
    }

    public double getCurrentStamina() {
        return currentStamina;
    }

    public void setCurrentStamina(double currentStamina) {
        this.currentStamina = currentStamina;
    }

    public boolean isExhausted() {
        return exhausted;
    }

    public void setExhausted(boolean exhausted) {
        this.exhausted = exhausted;
    }

    // Getters and Setters for each stat
    public int getHealthLevel() {
        return healthLevel;
    }

    public void setHealthLevel(int healthLevel) {
        this.healthLevel = healthLevel;
    }

    public int getStaminaLevel() {
        return staminaLevel;
    }

    public void setStaminaLevel(int staminaLevel) {
        this.staminaLevel = staminaLevel;
    }

    public int getAttackPowerLevel() {
        return attackPowerLevel;
    }

    public void setAttackPowerLevel(int attackPowerLevel) {
        this.attackPowerLevel = attackPowerLevel;
    }

    public int getDefenseLevel() {
        return defenseLevel;
    }

    public void setDefenseLevel(int defenseLevel) {
        this.defenseLevel = defenseLevel;
    }

    public double getAttackPowerExp() {
        return attackPowerExp;
    }

    public void setAttackPowerExp(double attackPowerExp) {
        this.attackPowerExp = attackPowerExp;
    }

    public double getAttackPowerLevelUpExp() {
        return attackPowerLevelUpExp;
    }

    public void setAttackPowerLevelUpExp(double attackPowerLevelUpExp) {
        this.attackPowerLevelUpExp = attackPowerLevelUpExp;
    }

    // 공격력 경험치 추가 및 레벨업 처리
    public void addAttackPowerExp(double amount) {
        this.attackPowerExp += amount;
        // 레벨업 조건은 GUIListener에서 처리할 예정
    }
}
