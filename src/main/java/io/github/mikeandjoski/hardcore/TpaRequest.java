package io.github.mikeandjoski.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class TpaRequest extends BukkitRunnable {

    private final TpaManager tpaManager;
    private final UUID requesterId;
    private final UUID targetId;

    public TpaRequest(TpaManager tpaManager, UUID requesterId, UUID targetId) {
        this.tpaManager = tpaManager;
        this.requesterId = requesterId;
        this.targetId = targetId;
    }

    @Override
    public void run() {
        Player target = Bukkit.getPlayer(targetId);
        if (target != null && tpaManager.hasRequest(target)) {
            Player requester = tpaManager.getRequester(target);
            if (requester != null && requester.getUniqueId().equals(requesterId)) {
                tpaManager.removeRequest(target);
                target.sendMessage(ChatColor.RED + requester.getName() + "님의 텔레포트 요청이 만료되었습니다.");
                requester.sendMessage(ChatColor.RED + target.getName() + "님에게 보낸 텔레포트 요청이 만료되었습니다.");
            }
        }
    }
}
