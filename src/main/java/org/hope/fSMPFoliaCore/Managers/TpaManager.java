package org.hope.fSMPFoliaCore.Managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks pending TPA requests. Key = target UUID, value = requester UUID + timestamp. */
public class TpaManager {

    public record Request(UUID from, long createdAt) {}

    // target -> request (only one pending request per target at a time)
    private final Map<UUID, Request> pending = new ConcurrentHashMap<>();

    private static final long TIMEOUT_MS = 30_000L;

    /** Store a request. Overwrites any prior pending request to the same target. */
    public void createRequest(UUID from, UUID to) {
        pending.put(to, new Request(from, System.currentTimeMillis()));
    }

    /**
     * Returns the pending request for {@code target}, or {@code null} if none / expired.
     * Expired requests are cleaned up on access.
     */
    public Request getRequest(UUID target) {
        Request req = pending.get(target);
        if (req == null) return null;
        if (System.currentTimeMillis() - req.createdAt() > TIMEOUT_MS) {
            pending.remove(target);
            return null;
        }
        return req;
    }

    /** Remove and return the pending request for {@code target}. */
    public Request removeRequest(UUID target) {
        Request req = pending.remove(target);
        if (req == null) return null;
        if (System.currentTimeMillis() - req.createdAt() > TIMEOUT_MS) return null;
        return req;
    }

    /** Cancel any outgoing requests made BY this player (called on quit). */
    public void cancelOutgoing(UUID from) {
        pending.entrySet().removeIf(e -> e.getValue().from().equals(from));
    }

    /** Cancel any incoming requests TO this player (called on quit). */
    public void cancelIncoming(UUID to) {
        pending.remove(to);
    }
}
