package pl.north93.zgame.itemshop.controller.handlers;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import pl.arieals.api.minigame.shared.api.booster.type.ShopBooster;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;
import pl.north93.zgame.api.global.network.INetworkManager;
import pl.north93.zgame.api.global.network.players.Identity;
import pl.north93.zgame.itemshop.shared.IDataHandler;

public class BoosterDataHandler implements IDataHandler
{
    @Inject
    private INetworkManager networkManager;

    @Override
    public String getId()
    {
        return "booster";
    }

    @Override
    public boolean process(final Identity player, final Map<String, String> data)
    {
        final int time = Integer.parseInt(data.get("time")); // w sekundach

        return this.networkManager.getPlayers().access(player, playerObj ->
        {
            ShopBooster.extendBoost(playerObj, TimeUnit.SECONDS.toMillis(time));
        });
    }
}
