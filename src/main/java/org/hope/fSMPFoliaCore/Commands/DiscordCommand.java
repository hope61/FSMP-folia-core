package org.hope.fSMPFoliaCore.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hope.fSMPFoliaCore.Managers.ConfigManager;
import org.hope.fSMPFoliaCore.Managers.LangManager;
import org.jetbrains.annotations.NotNull;

public class DiscordCommand implements CommandExecutor {

    private static final String PADDING = "              ";
    private final ConfigManager configManager;
    private final LangManager lang;

    public DiscordCommand(ConfigManager configManager, LangManager lang) {
        this.configManager = configManager;
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!configManager.isDiscordEnabled()) {
            return true;
        }

        sender.sendMessage(buildDiscordMessage());
        return true;
    }

    private Component buildDiscordMessage() {
        Component message = Component.text(lang.getDiscordMessage())
                .color(configManager.getMessageColor());

        Component button = Component.text()
                .append(Component.text("➤ ", configManager.getBarColor()))
                .append(Component.text(lang.getDiscordButtonText(), configManager.getButtonColor()).decorate(TextDecoration.BOLD))
                .append(Component.text(" ◀", configManager.getBarColor()))
                .clickEvent(ClickEvent.openUrl(configManager.getDiscordLink()))
                .hoverEvent(HoverEvent.showText(
                        Component.text(lang.getDiscordHoverText())
                                .color(configManager.getButtonHoverColor())))
                .build();

        return Component.text()
                .appendNewline()
                .append(Component.text(PADDING)).append(message)
                .appendNewline()
                .append(Component.text(PADDING)).append(button)
                .appendNewline()
                .build();
    }
}
