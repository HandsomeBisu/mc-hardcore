package io.github.mikeandjoski.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaManager {

    // Target UUID -> Requester UUID
    private final Map<UUID, UUID> requests = new HashMap<>();
    // Requester UUID -> Target UUID (to prevent spamming multiple people)
    private final Map<UUID, UUID> reverseRequests = new HashMap<>();

    public boolean hasRequest(Player target) {
        return requests.containsKey(target.getUniqueId());
    }

    public Player getRequester(Player target) {
        UUID requesterId = requests.get(target.getUniqueId());
        if (requesterId == null) {
            return null;
        }
        return Bukkit.getServer().getPlayer(requesterId);
    }

    public void addRequest(Player requester, Player target) {
        // Schedule request removal after 60 seconds
        new TpaRequest(this, requester.getUniqueId(), target.getUniqueId()).runTaskLater(Hardcore.getInstance(), 20L * 60);
        requests.put(target.getUniqueId(), requester.getUniqueId());
        reverseRequests.put(requester.getUniqueId(), target.getUniqueId());
    }

    public void removeRequest(Player target) {
        UUID requesterId = requests.remove(target.getUniqueId());
        if (requesterId != null) {
            reverseRequests.remove(requesterId);
        }
    }

    public void removeRequestByRequester(Player requester) {
        UUID targetId = reverseRequests.remove(requester.getUniqueId());
        if (targetId != null) {
            requests.remove(targetId);
        }
    }

    public boolean hasSentRequest(Player requester) {
        return reverseRequests.containsKey(requester.getUniqueId());
    }
}