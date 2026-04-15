package org.hope.fSMPFoliaCore.Managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManager {
    private final Map<UUID, UUID> replyTargets = new ConcurrentHashMap<>();

    public void setReplyTarget(UUID player, UUID target) {
        replyTargets.put(player, target);
        replyTargets.put(target, player);
    }

    public UUID getReplyTarget(UUID player) {
        return replyTargets.get(player);
    }

    public void removePlayer(UUID player) {
        UUID target = replyTargets.remove(player);
        if (target != null) replyTargets.remove(target);
    }
}
