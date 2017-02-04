package pl.north93.zgame.skyblock.server.cmd;

import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.bukkit.BukkitApiCore;
import pl.north93.zgame.api.global.commands.Arguments;
import pl.north93.zgame.api.global.commands.NorthCommand;
import pl.north93.zgame.api.global.commands.NorthCommandSender;
import pl.north93.zgame.api.global.component.annotations.InjectComponent;
import pl.north93.zgame.api.global.component.annotations.InjectResource;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.utils.DateUtil;
import pl.north93.zgame.skyblock.api.cfg.IslandConfig;
import pl.north93.zgame.skyblock.api.player.SkyPlayer;
import pl.north93.zgame.skyblock.server.SkyBlockServer;
import pl.north93.zgame.skyblock.server.gui.IslandTypePicker;

public class CreateCmd extends NorthCommand
{
    private BukkitApiCore   apiCore;
    @InjectComponent("API.MinecraftNetwork.NetworkManager")
    private INetworkManager networkManager;
    @InjectComponent("SkyBlock.Server")
    private SkyBlockServer  server;
    @InjectResource(bundleName = "SkyBlock")
    private ResourceBundle  messages;

    public CreateCmd()
    {
        super("create", "stworz");
    }

    @Override
    public void execute(final NorthCommandSender sender, final Arguments args, final String label)
    {
        final SkyPlayer skyPlayer = SkyPlayer.get(this.networkManager.getOnlinePlayer(sender.getName()));
        if (skyPlayer.hasIsland())
        {
            sender.sendMessage(this.messages, "error.already_has_island");
            return;
        }

        if (! this.server.getServerManager().canGenerateIsland(skyPlayer))
        {
            final String createTime = DateUtil.formatDateDiff(skyPlayer.getIslandCooldown() + this.server.getSkyBlockConfig().getIslandGenerateCooldown());
            sender.sendMessage(this.messages, "error.create_cooldown", createTime);
            return;
        }

        final Player player = (Player) sender.unwrapped();

        final List<IslandConfig> islandTypes = this.server.getSkyBlockConfig().getIslandTypes();
        final List<IslandConfig> availableIslands = islandTypes.stream()
                                                               .filter(is -> player.hasPermission("skyblock.island." + is.getName()))
                                                               .collect(Collectors.toList());

        if (availableIslands.isEmpty())
        {
            sender.sendMessage(this.messages, "error.no_any_island_type_available");
        }
        else if (availableIslands.size() == 1)
        {
            this.server.getSkyBlockManager().createIsland(availableIslands.get(0).getName(), sender.getName());
        }
        else
        {
            this.showIslandPicker(player, availableIslands);
        }
    }

    private void showIslandPicker(final Player player, final List<IslandConfig> config)
    {
        final Consumer<String> callback = type -> this.server.getSkyBlockManager().createIsland(type, player.getName());
        final IslandTypePicker window = new IslandTypePicker("Wybierz rodzaj wyspy", config, callback);

        this.apiCore.getWindowManager().openWindow(player, window);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).toString();
    }
}