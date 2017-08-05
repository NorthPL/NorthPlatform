package pl.arieals.globalshops.server.impl;

import static java.text.MessageFormat.format;


import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import org.diorite.utils.lazy.LazyValue;

import pl.arieals.globalshops.server.IPlayerContainer;
import pl.arieals.globalshops.shared.GroupType;
import pl.arieals.globalshops.shared.Item;
import pl.arieals.globalshops.shared.ItemsGroup;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;

class PlayerContainerImpl implements IPlayerContainer
{
    @Inject
    private PlayerDataService     service;
    @Inject
    private GlobalShopsServer     shopsServer;
    private LazyValue<PlayerData> playerData;
    private final Player player;

    public PlayerContainerImpl(final Player player)
    {
        this.player = player;
        this.playerData = new LazyValue<>(() -> this.service.getData(player));
    }

    @Override
    public Collection<Item> getBoughtItems(final ItemsGroup group)
    {
        final PlayerData data = this.playerData.get();
        return data.getBoughtItems().keySet().stream().map(id -> this.shopsServer.getItem(id)).collect(Collectors.toList());
    }

    @Override
    public boolean hasBoughtItem(final Item item)
    {
        final PlayerData data = this.playerData.get();
        return data.getBoughtItems().containsKey(item.getId());
    }

    @Override
    public int getBoughtItemLevel(final Item item)
    {
        final PlayerData data = this.playerData.get();
        return data.getBoughtItems().getOrDefault(item.getId(), 0);
    }

    @Override
    public Item getActiveItem(final ItemsGroup group)
    {
        if (group.getGroupType() == GroupType.MULTI_BUY)
        {
            throw new IllegalArgumentException();
        }

        final PlayerData data = this.playerData.get();
        final String activeItemId = data.getActiveItems().get(group.getId());
        if (activeItemId == null)
        {
            return null;
        }
        return this.shopsServer.getItem(activeItemId);
    }

    @Override
    public void addItem(final Item item)
    {
        this.service.addItem(this.player, item.getId());
        this.playerData.reset();
    }

    @Override
    public void markAsActive(final Item item)
    {
        final ItemsGroup group = item.getGroup();
        if (group.getGroupType() == GroupType.MULTI_BUY)
        {
            throw new IllegalArgumentException();
        }

        if (! this.hasBoughtItem(item))
        {
            throw new IllegalStateException(format("Item {0} isn't bought.", item.getId()));
        }

        this.service.setActiveItem(this.player, group.getId(), item.getId());
        this.playerData.reset();
    }
}
