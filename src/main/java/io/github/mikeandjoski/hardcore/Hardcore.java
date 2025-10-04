package io.github.mikeandjoski.hardcore;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

import io.github.mikeandjoski.hardcore.GUIListener;
import io.github.mikeandjoski.hardcore.PlayerStats;
import io.github.mikeandjoski.hardcore.StatEffectManager;
import io.github.mikeandjoski.hardcore.StaminaManager;
import io.github.mikeandjoski.hardcore.StatsCommand;
import io.github.mikeandjoski.hardcore.StatsManager;

public final class Hardcore extends JavaPlugin implements Listener {

    private static Hardcore instance;
    private static TpaManager tpaManager;

    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;
        tpaManager = new TpaManager();

        // Plugin startup logic
        getLogger().info("Hardcore plugin has been enabled!");

        statsManager = new StatsManager(this); // 플러그인 인스턴스 전달

        this.getCommand("stats").setExecutor(new StatsCommand(statsManager));
        this.getCommand("home").setExecutor(new HomeCommand(this));
        this.getCommand("tpa").setExecutor(new TpaCommand());
        this.getCommand("tpac").setExecutor(new TpaAcceptCommand());
        this.getCommand("tpaccept").setExecutor(new TpaAcceptCommand());
        this.getCommand("tpdeny").setExecutor(new TpaDenyCommand());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new GUIListener(statsManager), this);
        getServer().getPluginManager().registerEvents(new HomeGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new TpaGUIListener(), this);

        // Start StaminaManager task
        new StaminaManager(statsManager).runTaskTimer(this, 0L, 2L); // 0.1초마다 실행
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Hardcore plugin has been disabled!");
        statsManager.saveAllStats(); // 플러그인 종료 시 모든 스탯 저장
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        statsManager.addPlayer(event.getPlayer());
        getLogger().info(event.getPlayer().getName() + "'s stats loaded.");

        // 플레이어 접속 시 스탯 효과 적용
        PlayerStats stats = statsManager.getPlayerStats(event.getPlayer());
        if (stats != null) {
            StatEffectManager.applyStats(event.getPlayer(), stats);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        statsManager.removePlayer(event.getPlayer());
        getLogger().info(event.getPlayer().getName() + "'s stats unloaded and saved.");
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Monster) {
            LivingEntity monster = (LivingEntity) event.getEntity();

            // 넉백 저항 적용
            if (monster.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
                monster.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0); // 100% 넉백 저항
            }

            // 체력 2배~4배 증가
            if (monster.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                double originalMaxHealth = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                double multiplier = 2.0 + (Math.random() * 2.0); // 2.0 ~ 4.0 사이의 무작위 배율
                double newMaxHealth = originalMaxHealth * multiplier;

                monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
                monster.setHealth(newMaxHealth); // 현재 체력도 최대 체력으로 설정
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getCause() == DamageCause.FALL && event.getDamage() > 0) {
                // 2초에서 5초 사이의 무작위 지속 시간
                int durationInSeconds = (int) ((Math.random() * 3) + 2);
                int durationInTicks = durationInSeconds * 20;

                // 구속 I 효과 적용
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationInTicks, 0, false, false));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player killer = event.getEntity().getKiller();
            LivingEntity victim = event.getEntity();

            PlayerStats stats = statsManager.getPlayerStats(killer);
            if (stats != null) {
                // 몹 체력에 비례하여 경험치 부여 (예: 체력 1당 0.5 경험치)
                double expGain = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 0.5;
                stats.addAttackPowerExp(expGain);
                killer.sendMessage(String.format("§e%.1f 공격력 경험치 획득 §7(%.0f / %.0f)", expGain, stats.getAttackPowerExp(), stats.getAttackPowerLevelUpExp()));

                // 레벨업 확인 및 처리
                if (stats.getAttackPowerExp() >= stats.getAttackPowerLevelUpExp()) {
                    // 레벨업은 GUI에서 클릭으로만 가능하도록 메시지만 보냄
                    killer.sendMessage("§b공격력 스탯을 업그레이드 할 수 있습니다. §e/stats §b명령어로 확인하세요.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // 리스폰 시 달리기 상태를 강제로 해제하여 버그 방지
        player.setSprinting(false);

        PlayerStats stats = statsManager.getPlayerStats(player);
        if (stats != null) {
            stats.setCurrentStamina(stats.getMaxStamina());
            stats.setExhausted(false);
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack consumedItem = event.getItem();
        Material itemType = consumedItem.getType();

        // 익히지 않은 고기 목록
        List<Material> rawMeats = Arrays.asList(
            Material.BEEF, Material.PORKCHOP, Material.CHICKEN,
            Material.RABBIT, Material.MUTTON, Material.COD, Material.SALMON
        );

        if (rawMeats.contains(itemType)) {
            Player player = event.getPlayer();

            // 3초에서 10초 사이의 무작위 지속 시간
            int durationInSeconds = 3 + (int) (Math.random() * 8); // 3-10초
            int durationInTicks = durationInSeconds * 20;

            // 독 I 효과 적용
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, durationInTicks, 0, false, false));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Mob) {
            Player player = (Player) event.getDamager();
            Mob mob = (Mob) event.getEntity();

            // 평화로운 동물 목록
            List<EntityType> peacefulAnimals = Arrays.asList(
                EntityType.PIG, EntityType.SHEEP, EntityType.COW,
                EntityType.HORSE, EntityType.CHICKEN
            );

            if (peacefulAnimals.contains(mob.getType())) {
                // 동물을 공격 대상으로 설정
                mob.setTarget(player);
            }
        }
    }    public static Hardcore getInstance() {
        return instance;
    }

    public static TpaManager getTpaManager() {
        return tpaManager;
    }
}
