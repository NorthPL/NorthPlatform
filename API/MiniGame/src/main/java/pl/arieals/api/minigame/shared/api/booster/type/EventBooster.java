package pl.arieals.api.minigame.shared.api.booster.type;

import pl.arieals.api.minigame.shared.api.booster.IBooster;
import pl.north93.zgame.api.global.network.players.IPlayer;

public class EventBooster implements IBooster
{
    @Override
    public String getId()
    {
        return "event";
    }

    @Override
    public double getMultiplier(final IPlayer player)
    {
        return 0.25; // + 0.25
    }
}
