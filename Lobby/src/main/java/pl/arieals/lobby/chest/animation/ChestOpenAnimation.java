package pl.arieals.lobby.chest.animation;

import org.bukkit.Location;
import org.bukkit.Particle;

import pl.arieals.lobby.chest.opening.ChestOpeningController;
import pl.north93.zgame.api.bukkit.server.IBukkitExecutor;
import pl.north93.zgame.api.global.component.annotations.bean.Inject;

class ChestOpenAnimation extends AbstractChestRotationAnimation
{
    private static final double START_SPEED  = 0.5;
    private static final double SPEED_GROVER = 0.04;
    private static final double MAX_SPEED    = 10;

    @Inject
    private IBukkitExecutor        bukkitExecutor;
    @Inject
    private ChestOpeningController openingController;

    private double currentSpeed = START_SPEED;
    private double currentRotation;
    private boolean isEnded;

    public ChestOpenAnimation(final AnimationInstance instance)
    {
        super(instance);
    }

    @Override
    void tick()
    {
        if (this.currentSpeed >= MAX_SPEED)
        {
            this.endAnimation();
            return;
        }

        if (this.currentRotation >= 360)
        {
            this.currentRotation = 0;
        }

        this.currentRotation += this.currentSpeed;
        this.setRotation((float) this.currentRotation);

        this.currentSpeed += SPEED_GROVER;
    }

    @Override
    void clicked()
    {
    }

    private void endAnimation()
    {
        if (this.isEnded)
        {
            // blokujemy kilkukrotne wykonanie
            return;
        }
        this.isEnded = true;

        // lokacja skrzynki, tu odpalamy particle
        final Location location = this.instance.getArmorStand().getLocation();

        // uruchamiamy to synchronicznie do serwera, bo ta metoda poleci na osobnym watku.
        this.bukkitExecutor.sync(() ->
        {
            // niszczymy animacje
            this.instance.setDestroyed();

            // uruchamiamy particle
            this.getPlayer().spawnParticle(Particle.EXPLOSION_LARGE, location, 10);

            // wyswietla wyniki otwierania
            this.openingController.showOpeningResults(this.getPlayer());
        });
    }
}