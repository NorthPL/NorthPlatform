package pl.north93.zgame.auth.server;

import static org.bukkit.ChatColor.translateAlternateColorCodes;


import java.text.MessageFormat;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.messages.Messages;
import pl.north93.zgame.api.global.messages.MessagesBox;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.auth.api.IAuthManager;
import pl.north93.zgame.auth.api.player.AuthPlayer;

public class PlayerListeners implements Listener
{
    @Inject
    private INetworkManager networkManager;
    @Inject @Messages("NoPremiumAuth")
    private MessagesBox     messages;
    private IAuthManager    authManager;

    public PlayerListeners(final IAuthManager authManager)
    {
        this.authManager = authManager;
    }

    private void sendMessage(final CommandSender sender, final String message, final Object... args)
    {
        final Locale locale;
        if (sender instanceof Player)
        {
            locale = Locale.forLanguageTag(((Player) sender).spigot().getLocale());
        }
        else
        {
            locale = Locale.getDefault();
        }
        final String msg = MessageFormat.format(this.messages.getMessage(locale, message), args);
        sender.sendMessage(translateAlternateColorCodes('&', msg));
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        if (this.authManager.isLoggedIn(player.getUniqueId()))
        {
            return;
        }

        final AuthPlayer authPlayer = AuthPlayer.get(this.networkManager.getOnlinePlayer(player.getName()));
        if (authPlayer.isRegistered())
        {
            this.sendMessage(player, "join.login");
        }
        else
        {
            this.sendMessage(player, "join.register");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onChat(final AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();
        if (! this.authManager.isLoggedIn(player.getUniqueId()))
        {
            this.sendMessage(event.getPlayer(), "error.first_login_or_register");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(final PlayerCommandPreprocessEvent event)
    {
        if (this.authManager.isLoggedIn(event.getPlayer().getUniqueId()))
        {
            return;
        }

        final String cmd = StringUtils.split(event.getMessage(), ' ')[0];

        if (cmd.equalsIgnoreCase("/l") || cmd.equalsIgnoreCase("/login") || cmd.equalsIgnoreCase("/zaloguj") ||
                    cmd.equalsIgnoreCase("/register") || cmd.equalsIgnoreCase("/zarejestruj"))
        {
            return;
        }

        event.setCancelled(true);
        this.sendMessage(event.getPlayer(), "error.first_login_or_register");
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(final PlayerMoveEvent event)
    {
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
        {
            return;
        }

        final Player player = event.getPlayer();
        if (this.authManager.isLoggedIn(player.getUniqueId()))
        {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event)
    {
        final HumanEntity player = event.getWhoClicked();
        if (! this.authManager.isLoggedIn(player.getUniqueId()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(final PlayerDropItemEvent event)
    {
        final Player player = event.getPlayer();
        if (! this.authManager.isLoggedIn(player.getUniqueId()))
        {
            this.sendMessage(event.getPlayer(), "error.first_login_or_register");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeldItemChange(final PlayerItemHeldEvent event)
    {
        final Player player = event.getPlayer();
        if (! this.authManager.isLoggedIn(player.getUniqueId()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEat(final PlayerItemConsumeEvent event)
    {
        final Player player = event.getPlayer();
        if (! this.authManager.isLoggedIn(player.getUniqueId()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event)
    {
        final Player player = event.getPlayer();
        if (! this.authManager.isLoggedIn(player.getUniqueId()))
        {
            this.sendMessage(event.getPlayer(), "error.first_login_or_register");
            event.setCancelled(true);
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}
