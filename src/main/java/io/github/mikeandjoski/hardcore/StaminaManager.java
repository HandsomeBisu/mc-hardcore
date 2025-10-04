package io.github.mikeandjoski.hardcore;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StaminaManager extends BukkitRunnable {

    private final StatsManager statsManager;
    private final Map<UUID, Long> lastHeartbeatSound = new HashMap<>();
    private final Map<UUID, Long> lastExhaustedDamage = new HashMap<>();
    private final Map<UUID, Long> lastAnimalAttackTime = new HashMap<>();
    private final long HEARTBEAT_INTERVAL_TICKS = 30L; // 1.5초 (30틱)
    private final long EXHAUSTED_DAMAGE_INTERVAL_MS = 1000L; // 1초
    private final long ANIMAL_ATTACK_COOLDOWN_MS = 1000L; // 1초

    public StaminaManager(StatsManager statsManager) {
        this.statsManager = statsManager;
    }


    
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = statsManager.getPlayerStats(player);
            if (stats == null) continue;

            double maxStamina = stats.getMaxStamina();
            double currentStamina = stats.getCurrentStamina();
            boolean isSprinting = player.isSprinting();
            boolean isExhausted = stats.isExhausted();

            // 1. 탈진 상태에서 달릴 때: 데미지
            if (isSprinting && isExhausted) {
                long currentTime = System.currentTimeMillis();
                long lastDamageTime = lastExhaustedDamage.getOrDefault(player.getUniqueId(), 0L);

                if (currentTime - lastDamageTime >= EXHAUSTED_DAMAGE_INTERVAL_MS) {
                    player.damage(1.0); // 데미지 1.0 (반 칸)
                    lastExhaustedDamage.put(player.getUniqueId(), currentTime);
                }
                stats.setCurrentStamina(0); // 스태미나는 0으로 유지
            }
            // 2. 스태미너가 있는 상태에서 달릴 때: 소모
            else if (isSprinting) {
                double newStamina = currentStamina - 2.0; // 소모량
                stats.setCurrentStamina(Math.max(0, newStamina));
                if (newStamina <= 0) {
                    stats.setExhausted(true); // 탈진 상태 설정
                }
            }
            // 3. 걷거나 가만히 있을 때: 회복
            else {
                if (currentStamina < maxStamina) {
                    // 최대 스태미너에 비례하여 회복량을 조절 (레벨 1 기준, 100을 채우는 속도와 동일하게)
                    double baseRegenRate = isExhausted ? 0.5 : 1.0;
                    double regenRate = (maxStamina / 100.0) * baseRegenRate;

                    double newStamina = currentStamina + regenRate;
                    stats.setCurrentStamina(Math.min(maxStamina, newStamina));

                    // 탈진 상태에서 지구력이 30% 이상 회복되면 탈진 해제
                    if (isExhausted && newStamina >= maxStamina * 0.3) {
                        stats.setExhausted(false);
                        lastExhaustedDamage.remove(player.getUniqueId()); // 데미지 타이머 초기화
                    }
                }
            }

            // 액션바 표시
            if (stats.isExhausted()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c§l휴식을 취하십시오."));
            } else if (isSprinting || stats.getCurrentStamina() < maxStamina) {
                sendStaminaActionBar(player, stats.getCurrentStamina(), stats.getMaxStamina());
            } else { // 지구력이 가득 찼고 달리지 않을 때 액션바 지우기
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
            }


            // 지구력이 낮을 때 심장 박동 소리 재생
            if (currentStamina / maxStamina <= 0.25) {
                long lastPlayed = lastHeartbeatSound.getOrDefault(player.getUniqueId(), 0L);
                if ((System.currentTimeMillis() - lastPlayed) / 50 >= HEARTBEAT_INTERVAL_TICKS) {
                    player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
                    lastHeartbeatSound.put(player.getUniqueId(), System.currentTimeMillis());
                }
            } else {
                // 지구력이 낮지 않으면 타이머 초기화
                lastHeartbeatSound.remove(player.getUniqueId());
            }

            // 적대적인 동물 공격 로직
            List<EntityType> hostileAnimalTypes = Arrays.asList(
                EntityType.PIG, EntityType.SHEEP, EntityType.COW,
                EntityType.HORSE, EntityType.CHICKEN
            );

            List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
            for (Entity entity : nearbyEntities) {
                if (hostileAnimalTypes.contains(entity.getType()) && entity instanceof Creature) {
                    Creature creature = (Creature) entity;
                    // 몹의 타겟이 이 플레이어인지 확인
                    if (creature.getTarget() != null && creature.getTarget().equals(player)) {
                        // 플레이어를 향해 돌진하도록 속도를 설정
                        org.bukkit.util.Vector direction = player.getLocation().toVector().subtract(creature.getLocation().toVector()).normalize();
                        creature.setVelocity(direction.multiply(0.25)); // 속도 조절

                        // 플레이어와의 거리가 2블록 미만이면 공격
                        if (player.getLocation().distanceSquared(creature.getLocation()) < 4) { // distanceSquared is cheaper than distance
                            long currentTime = System.currentTimeMillis();
                            long lastAttack = lastAnimalAttackTime.getOrDefault(creature.getUniqueId(), 0L);

                            if (currentTime - lastAttack >= ANIMAL_ATTACK_COOLDOWN_MS) {
                                player.damage(1.0); // 데미지
                                lastAnimalAttackTime.put(creature.getUniqueId(), currentTime);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendStaminaActionBar(Player player, double current, double max) {
        double ratio = current / max;
        int filledBars = (int) (ratio * 20);

        ChatColor barColor;
        if (ratio > 0.5) {
            barColor = ChatColor.GREEN;
        } else if (ratio > 0.25) {
            barColor = ChatColor.YELLOW;
        } else {
            barColor = ChatColor.RED;
        }

        StringBuilder bar = new StringBuilder();
        bar.append(barColor);
        for (int i = 0; i < 20; i++) {
            if (i < filledBars) {
                bar.append("|");
            } else {
                // 첫 번째 빈 칸에만 회색을 적용하여 색상 코드가 중복되지 않게 함
                if (i == filledBars) {
                    bar.append(ChatColor.GRAY);
                }
                bar.append("|");
            }
        }

        String message = String.format("§l지구력 %s", bar.toString());
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
