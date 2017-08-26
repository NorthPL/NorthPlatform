package pl.arieals.minigame.goldhunter.abilities;

import pl.arieals.minigame.goldhunter.AbilityHandler;
import pl.arieals.minigame.goldhunter.GoldHunterPlayer;
import pl.arieals.minigame.goldhunter.effect.CallOfBloodEffect;

public class CallOfBloodAbility implements AbilityHandler
{
    public static final int EFFECT_DURATION = 400;
    public static final double BLOOD_TRIBUTE = 8;
    
    @Override
    public boolean onUse(GoldHunterPlayer player)
    {
        player.getAbilityTracker().suspendAbilityLoading();
        player.getEffectTracker().addEffect(new CallOfBloodEffect(), EFFECT_DURATION).onComplete(player.getAbilityTracker()::resetAbilityLoading);
        player.getPlayer().damage(BLOOD_TRIBUTE);
        return true;
    }
}