package pl.north93.zgame.api.bukkit.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import pl.north93.zgame.api.global.component.annotations.bean.Inject;

public abstract class AbstractCountdown extends BukkitRunnable
{
    @Inject
    private static JavaPlugin PLUGIN;
    private int time;

    public AbstractCountdown(final int time)
    {
        this.time = time;
    }

    public final void start(final int every)
    {
        this.runTaskTimer(PLUGIN, 0, every);
    }

    @Override
    public final void run()
    {
        if (this.time == 0)
        {
            this.end();
            this.cancel();
        }
        else
        {
            this.loop(this.time);
            this.time--;
        }
    }

    public final int getTime()
    {
        return this.time;
    }

    protected abstract void loop(int time);

    protected abstract void end();

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("time", this.time).toString();
    }
}
