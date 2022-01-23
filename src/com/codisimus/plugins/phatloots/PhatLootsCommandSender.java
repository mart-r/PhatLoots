package com.codisimus.plugins.phatloots;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Dispatches Commands for the PhatLoots Plugin
 *
 * @author Codisimus
 */
public class PhatLootsCommandSender implements ConsoleCommandSender {
    private final String NAME = "PhatLoots";
    private final String TAG = "[PhatLoots Command Sender] ";

    @Override
    public void sendMessage(String string) {
        PhatLoots.logger.info(TAG.concat(string));
    }

    @Override
    public void sendMessage(String[] strings) {
        for (String string: strings) {
            sendMessage(string);
        }
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String s) {
        this.sendMessage(s);
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String[] strings) {
        this.sendMessage(strings);
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Spigot spigot() {
        return null;
    }

    @Override
    public boolean isPermissionSet(String string) {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission prmsn) {
        return true;
    }

    @Override
    public boolean hasPermission(String string) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission prmsn) {
        return true;
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void removeAttachment(PermissionAttachment pa) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void recalculatePermissions() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setOp(boolean bln) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(String s) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return false;
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void sendRawMessage(String s) {
        throw new UnsupportedOperationException("Not supported.");
    }
        
    public void sendRawMessage(@Nullable UUID uuid, @NotNull String s) {
        this.sendRawMessage(s);
    }
}
