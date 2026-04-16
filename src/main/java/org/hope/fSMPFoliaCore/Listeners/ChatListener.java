package org.hope.fSMPFoliaCore.Listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.hope.fSMPFoliaCore.hooks.VaultHook;

public class ChatListener implements Listener {
    private final LangManager lang;
    private final VaultHook vault;

    public ChatListener(LangManager lang, VaultHook vault) {
        this.lang = lang;
        this.vault = vault;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        Component prefix = Component.empty();
        Component suffix = Component.empty();

        if (vault != null && vault.isAvailable()) {
            String rawPrefix = vault.getPrefix(player);
            String rawSuffix = vault.getSuffix(player);

            if (!rawPrefix.isEmpty()) {
                prefix = deserializeFormatted(rawPrefix);
            }
            if (!rawSuffix.isEmpty()) {
                suffix = deserializeFormatted(rawSuffix);
            }
        }

        Component finalPrefix = prefix;
        Component finalSuffix = suffix;

        event.renderer((source, displayName, message, viewer) ->
                Component.text()
                        .append(finalPrefix)
                        .append(Component.text(player.getName()).color(lang.primary()))
                        .append(finalSuffix)
                        .append(Component.text(lang.getChatSeparator()).color(lang.secondary()))
                        .append(message.color(lang.white()))
                        .build()
        );
    }

    /**
     * Deserialize a prefix/suffix string that may use either:
     * - Legacy '&' color codes (e.g. "&6[Admin] &r")
     * - MiniMessage tags (e.g. "<gold>[Admin]</gold> ")
     * Tries MiniMessage first (detected by presence of '<'); falls back to legacy.
     */
    private static Component deserializeFormatted(String raw) {
        if (raw.contains("<") && raw.contains(">")) {
            try {
                return MiniMessage.miniMessage().deserialize(raw);
            } catch (Exception ignored) {}
        }
        // Also handle §-section-sign codes (raw from some plugins)
        String normalized = raw.replace('§', '&');
        return LegacyComponentSerializer.legacyAmpersand().deserialize(normalized);
    }
}
