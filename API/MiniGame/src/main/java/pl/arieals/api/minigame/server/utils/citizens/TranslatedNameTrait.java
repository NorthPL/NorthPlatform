package pl.arieals.api.minigame.server.utils.citizens;

import java.util.Optional;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import net.citizensnpcs.api.trait.Trait;
import pl.north93.northspigot.event.entity.EntityTrackedPlayerEvent;
import pl.north93.zgame.api.bukkit.hologui.hologram.IHologram;
import pl.north93.zgame.api.bukkit.hologui.hologram.TranslatableStringLine;
import pl.north93.zgame.api.global.messages.TranslatableString;

public class TranslatedNameTrait extends Trait
{
    private static final String TEAM_NAME = RandomStringUtils.randomAlphanumeric(16);
    private final TranslatableString[] nameLines;
    private IHologram hologram;

    public TranslatedNameTrait(final TranslatableString... nameLines)
    {
        super("translatedName");
        this.nameLines = nameLines;
    }

    @Override
    public void onAttach()
    {
        if (this.npc.isSpawned())
        {
            this.setup();
        }
    }

    @Override
    public void onSpawn()
    {
        this.setup();
    }

    @Override
    public void onDespawn()
    {
        this.destroy();
    }

    @Override
    public void onRemove()
    {
        this.destroy();
    }

    private void setup()
    {
        if (this.hologram != null)
        {
            return;
        }

        final Entity entity = this.getNPC().getEntity();
        entity.setCustomNameVisible(false);

        this.hologram = IHologram.createWithLowerLocation(entity.getLocation().add(0, entity.getHeight(), 0));
        for (int i = 0; i < this.nameLines.length; i++)
        {
            final int messageId = this.nameLines.length - i - 1;
            this.hologram.setLine(i, new TranslatableStringLine(this.nameLines[messageId]));
        }
    }

    private void destroy()
    {
        if (this.hologram == null)
        {
            return;
        }

        this.hologram.remove();
        this.hologram = null;
    }

    @EventHandler
    public void handleEntityBeingTracked(final EntityTrackedPlayerEvent event)
    {
        final Entity entity = this.getNPC().getEntity();
        if (! event.getEntity().equals(entity))
        {
            return;
        }

        final Player player = event.getPlayer();
        final Scoreboard scoreboard = player.getScoreboard();

        final Team hideNameTeam = this.getOrCreateTeam(scoreboard);
        if (hideNameTeam.hasEntry(this.npc.getName()))
        {
            return;
        }

        hideNameTeam.addEntry(this.npc.getName());
    }

    private Team getOrCreateTeam(final Scoreboard scoreboard)
    {
        return Optional.ofNullable(scoreboard.getTeam(TEAM_NAME)).orElseGet(() ->
        {
            final Team newTeam = scoreboard.registerNewTeam(TEAM_NAME);
            newTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

            return newTeam;
        });
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("nameLines", this.nameLines).toString();
    }
}
