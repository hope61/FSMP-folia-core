package org.hope.fSMPFoliaCore.hooks;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private final Chat chat;
    private final Permission permission;

    public VaultHook() {
        RegisteredServiceProvider<Chat> chatProvider =
                Bukkit.getServicesManager().getRegistration(Chat.class);
        this.chat = chatProvider != null ? chatProvider.getProvider() : null;

        RegisteredServiceProvider<Permission> permProvider =
                Bukkit.getServicesManager().getRegistration(Permission.class);
        this.permission = permProvider != null ? permProvider.getProvider() : null;
    }

    public boolean isAvailable() {
        return chat != null;
    }

    public boolean isPermissionAvailable() {
        return permission != null;
    }

    public String getPrefix(Player player) {
        if (chat == null) return "";
        String value = chat.getPlayerPrefix(player.getWorld().getName(), player);
        return value != null ? value : "";
    }

    public String getSuffix(Player player) {
        if (chat == null) return "";
        String value = chat.getPlayerSuffix(player.getWorld().getName(), player);
        return value != null ? value : "";
    }

    /** Add a player to a group (used for rank rewards). Returns false if Vault Permission not available. */
    public boolean addGroup(Player player, String group) {
        if (permission == null) return false;
        return permission.playerAddGroup(player.getWorld().getName(), player, group);
    }

    /** Add a permission node to a player (used for home slot rewards). Returns false if not available. */
    public boolean addPermission(Player player, String perm) {
        if (permission == null) return false;
        return permission.playerAdd(player.getWorld().getName(), player, perm);
    }

    /** Check if player is in a group. */
    public boolean isInGroup(Player player, String group) {
        if (permission == null) return false;
        return permission.playerInGroup(player.getWorld().getName(), player, group);
    }
}
