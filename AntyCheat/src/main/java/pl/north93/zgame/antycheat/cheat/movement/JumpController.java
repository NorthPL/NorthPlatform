package pl.north93.zgame.antycheat.cheat.movement;

import static pl.north93.zgame.antycheat.utils.AntyCheatMath.cosineSimilarity;


import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import pl.north93.zgame.antycheat.analysis.FalsePositiveProbability;
import pl.north93.zgame.antycheat.analysis.SingleAnalysisResult;
import pl.north93.zgame.antycheat.event.impl.ClientMoveTimelineEvent;
import pl.north93.zgame.antycheat.event.impl.VelocityAppliedTimelineEvent;
import pl.north93.zgame.antycheat.timeline.DataKey;
import pl.north93.zgame.antycheat.timeline.PlayerData;
import pl.north93.zgame.antycheat.timeline.PlayerTickInfo;
import pl.north93.zgame.antycheat.utils.DistanceUtils;
import pl.north93.zgame.antycheat.utils.EntityUtils;
import pl.north93.zgame.antycheat.utils.location.RichEntityLocation;

public class JumpController
{
    private static final String RISING_IN_FALL_STAGE       = "Player is rising in FALL stage.";
    private static final String MAX_HEIGHT_EXCEEDED        = "Player exceeded max jump height.";
    private static final String HORIZONTAL_RISING_EXCEEDED = "Player exceeded rising horizontal distance.";
    private static final String INCONSISTENCY_START_VECTOR = "Player ignored their start vector.";
    private static final double MIN_HEIGHT_TO_EXCEED = 0.26;
    private static final DataKey<JumpController> KEY = new DataKey<>("jumpController", JumpController::new);

    public static JumpController get(final PlayerData playerData)
    {
        return playerData.get(KEY);
    }

    private final PlayerData playerData;

    /** Aktualny jump stage gracza */
    private JumpStage jumpStage = JumpStage.GROUND;
    public enum JumpStage
    {
        GROUND,
        RISE,
        FALL
    }

    /**Liczba pakietów w których gracz spadał, jest używane dla rzadkich false-positive
     * gdy czasami rejestrujemy że gracz powinien zacząć spadać, a tak naprawdę zaczyna skok.
     * @see #tryEnterFallingStage()
     */
    private int startFallingPackets;

    /** Lokacja gdzie entity zaczęło skok lub upadek */
    private RichEntityLocation startLocation;

    /** Informacje o ticku w którym zaczęto wznoszenie lub upadek */
    private PlayerTickInfo startTickInfo;

    /** Startowe velocity ustawiane przez zewnętrzne źródło; może być nullem! */
    private Vector startVelocity;

    // konstruktor
    public JumpController(final PlayerData playerData)
    {
        this.playerData = playerData;
    }

    // metoda wejsciowa analizujaca event
    public SingleAnalysisResult handleMovement(final PlayerTickInfo tickInfo, final ClientMoveTimelineEvent event)
    {
        final SingleAnalysisResult singleAnalysisResult = SingleAnalysisResult.create();

        if (event.isFromOnGround() && ! event.isToOnGround())
        {
            //Bukkit.broadcastMessage("wystartowano z ziemi");
            this.tearOffGround(tickInfo, event, singleAnalysisResult);
        }
        else if (! event.isFromOnGround() && ! event.isToOnGround())
        {
            this.flyHandle(tickInfo, event, singleAnalysisResult);
        }
        else if (event.isToOnGround())
        {
            this.handleLanding(event);
        }

        return singleAnalysisResult;
    }

    public void forceReset()
    {
        this.jumpStage = JumpStage.GROUND;
        this.startFallingPackets = 0;
        this.startVelocity = null;
        this.startTickInfo = null;
        this.startLocation = null;
    }

    public void changeVelocity(final VelocityAppliedTimelineEvent newVelocity)
    {
        if (this.jumpStage == JumpStage.GROUND)
        {
            //Bukkit.broadcastMessage("velocity zmienione na ziemi");
            this.startVelocity = newVelocity.getVelocity();
            return;
        }

        this.forceReset();
        this.startVelocity = newVelocity.getVelocity();
        //Bukkit.broadcastMessage("velocity zmienione w powietrzu + reset jump controllera");
    }

    // gracz odrywa sie od ziemi. Skacze lub spada.
    private void tearOffGround(final PlayerTickInfo tickInfo, final ClientMoveTimelineEvent event, final SingleAnalysisResult result)
    {
        if (this.jumpStage == JumpStage.GROUND)
        {
            // rejestrujemy pierwsza pozycje startowa, nawet jak klient wysle kilka pakietow
            this.startLocation = event.getFrom();
            this.startTickInfo = tickInfo;
        }

        final double heightDiff = event.getTo().getY() - event.getFrom().getY();
        if (heightDiff >= 0)
        {
            this.jumpStage = JumpStage.RISE;
        }
        else
        {
            //Bukkit.broadcastMessage("fall in tearOffGround heightDiff:" + heightDiff);
            //this.jumpStage = JumpStage.FALL;
            this.tryEnterFallingStage();
        }

        //Bukkit.broadcastMessage("tearOffGround new stage: " + this.jumpStage);
    }

    // gracz jest w powietrzu
    private void flyHandle(final PlayerTickInfo tickInfo, final ClientMoveTimelineEvent event, final SingleAnalysisResult result)
    {
        final RichEntityLocation toLocation = event.getTo();
        final RichEntityLocation fromLocation = event.getFrom();

        if (toLocation.getY() >= fromLocation.getY()) // unosi sie
        {
            if (this.jumpStage == JumpStage.FALL)
            {
                // gracz podczas gdy powinien opadac zaczal sie unosic
                final FalsePositiveProbability risingInFallStageFalsePositive;
                if (toLocation.isStands() || toLocation.getDistanceToGround() <= 1.25)
                {
                    risingInFallStageFalsePositive = FalsePositiveProbability.HIGH;
                }
                else
                {
                    risingInFallStageFalsePositive = FalsePositiveProbability.MEDIUM;
                }

                result.addViolation(MovementViolation.SURVIVAL_FLY, RISING_IN_FALL_STAGE, risingInFallStageFalsePositive);
            }
            else if (this.jumpStage == JumpStage.RISE)
            {
                // na razie wszystko jest dobrze. teraz weryfikujemy czy gracz może skakać na taką wysokość.
                this.verifyPlayerHeightRising(tickInfo, event, result);

                // resetujemy ilosc pakietów o opadaniu. Bo się w końcu unosimy.
                this.startFallingPackets = 0;
            }
            else
            {
                // Gracz zaczyna się unosić gdy jest w stage GROUND, oznacza to problemy z flagą onGround.
                // Teoretycznie możnaby triggerowac jakies violation, ale zajmuje sie tym OnGroundManipulationChecker
                this.startLocation = fromLocation;
                this.startTickInfo = tickInfo;
                this.jumpStage = JumpStage.RISE;
            }
        }
        else if (toLocation.getY() < fromLocation.getY()) // opada
        {
            if (this.jumpStage == JumpStage.FALL)
            {
                // ok, sprawdzamy wysokosc/szybkosc
            }
            else if (this.jumpStage == JumpStage.RISE)
            {
                // Gracz podczas lotu w górę zaczął opadać, to oznacza że mógł osiągnąć maksymalną wysokość skoku.
                if (this.tryEnterFallingStage())
                {
                    this.startLocation = fromLocation;
                    this.startTickInfo = tickInfo;
                }
                //Bukkit.broadcastMessage("fall in flyHandle/jumpStage==rise");
                //this.jumpStage = JumpStage.FALL;
            }
            else
            {
                // Gracz zaczyna opadać gdy jest w stage GROUND, oznacza to problemy z flagą onGround.
                // Teoretycznie możnaby triggerowac jakies violation, ale zajmuje sie tym OnGroundManipulationChecker
                if (this.tryEnterFallingStage())
                {
                    this.startLocation = fromLocation;
                    this.startTickInfo = tickInfo;
                }
                //Bukkit.broadcastMessage("fall in flyHandle/jumpStage==ground");
                //this.jumpStage = JumpStage.FALL;
            }
        }
        //Bukkit.broadcastMessage("flyHandle new stage: " + this.jumpStage);
    }

    // gracz laduje na ziemi
    private void handleLanding(final ClientMoveTimelineEvent event)
    {
        if (this.jumpStage == JumpStage.GROUND)
        {
            return;
        }

        this.forceReset();
        //Bukkit.broadcastMessage("handleLanding new stage: " + this.jumpStage);
    }

    // próbujemy rozpoczac opadanie, ale mozemy to zrobic dopiero po kilku pakietach dla pewnosci
    // bo inaczej lapiemy false-positive podczas energicznego skakania.
    private boolean tryEnterFallingStage()
    {
        if (this.startFallingPackets <= 1)
        {
            this.startFallingPackets++;
            return false;
        }

        this.startFallingPackets = 0;
        this.startVelocity = null;
        this.jumpStage = JumpStage.FALL;
        return true;
    }

    // weryfikuje wysokość na etapie unoszenia się gracza
    private void verifyPlayerHeightRising(final PlayerTickInfo tickInfo, final ClientMoveTimelineEvent event, final SingleAnalysisResult result)
    {
        final RichEntityLocation to = event.getTo();

        final Vector startVector = this.deduceStartVelocity(event.getOwner());

        final double jumpHeight = to.getY() - this.startLocation.getY();
        final double maxHeight = EntityUtils.maxHeightByStartVelocity(startVector.getY());

        // sprawdzamy czy i o ile gracz przekroczył maksymalną wysokość
        final double maxHeightExceeded = jumpHeight - maxHeight;
        if (maxHeightExceeded > MIN_HEIGHT_TO_EXCEED && maxHeightExceeded <= 1)
        {
            result.addViolation(MovementViolation.SURVIVAL_FLY, MAX_HEIGHT_EXCEEDED, FalsePositiveProbability.HIGH);
        }
        else if (maxHeightExceeded > 1 && maxHeightExceeded <= 4)
        {
            result.addViolation(MovementViolation.SURVIVAL_FLY, MAX_HEIGHT_EXCEEDED, FalsePositiveProbability.MEDIUM);
        }
        else if (maxHeightExceeded > 4)
        {
            result.addViolation(MovementViolation.SURVIVAL_FLY, MAX_HEIGHT_EXCEEDED, FalsePositiveProbability.LOW);
        }

        final double horizontalDistanceFromStart = DistanceUtils.xzDistance(this.startLocation, to);
        final double expectedHorizontalDistance = 2.6 + maxHeight * 0.1;

        // sprawdzamy czy gracz sie wysunął się zbyt daleko na osiach xz
        final double horizontalExceeded = horizontalDistanceFromStart - expectedHorizontalDistance;
        if (horizontalExceeded > 1)
        {
            result.addViolation(MovementViolation.SURVIVAL_FLY, HORIZONTAL_RISING_EXCEEDED, FalsePositiveProbability.LOW);
        }
        else if (horizontalExceeded > 0)
        {
            result.addViolation(MovementViolation.SURVIVAL_FLY, HORIZONTAL_RISING_EXCEEDED, FalsePositiveProbability.MEDIUM);
        }

        // porównujemy wektor ruchu w tym evencie do początkowego wektora ruchu
        final Vector currentMovementVector = event.getFrom().vectorToOther(to);
        final double cosineSimilarity = cosineSimilarity(startVector.clone().normalize(), currentMovementVector.normalize());
        if (to.getDistanceToGround() >= 0.1 && maxHeightExceeded < -0.15)
        {
            if (to.getDistanceToGround() >= 0.25 && cosineSimilarity <= 0.1)
            {
                // dodatkowo zwiekszamy wymagana wysokosc zeby uniknac bolesnych false-positives
                result.addViolation(MovementViolation.SURVIVAL_FLY, INCONSISTENCY_START_VECTOR, FalsePositiveProbability.LOW);
            }
            else if (cosineSimilarity <= 0.2)
            {
                result.addViolation(MovementViolation.SURVIVAL_FLY, INCONSISTENCY_START_VECTOR, FalsePositiveProbability.MEDIUM);
            }
            else if (cosineSimilarity < 0.35)
            {
                result.addViolation(MovementViolation.SURVIVAL_FLY, INCONSISTENCY_START_VECTOR, FalsePositiveProbability.HIGH);
            }
            //Bukkit.broadcastMessage(ChatColor.RED + "v:" + cosineSimilarity + " maxHeightExceeded:" + maxHeightExceeded);
        }
    }

    // staramy sie obliczyc wektor z jakim wystartował gracz
    private Vector deduceStartVelocity(final Player player)
    {
        // uznajemy że gracz zawsze może osiągnąć wysokość normalnego skoku.
        // Bez tego czasami łapiemy dziwne false-positives przy intensywnym skakaniu z piruetami.
        final double normalJumpVelocity = this.calculateJumpVelocity(player);
        if (this.startVelocity != null)
        {
            final double newY = Math.max(normalJumpVelocity, this.startVelocity.getY());
            return new Vector(this.startVelocity.getX(), newY, this.startVelocity.getZ());
        }
        else
        {
            final Vector velocityFromTick = this.startTickInfo.getProperties().getVelocity();
            final double newY = Math.max(normalJumpVelocity, velocityFromTick.getY());
            return new Vector(velocityFromTick.getX(), newY, velocityFromTick.getZ());
        }
    }

    private double calculateJumpVelocity(final Player player)
    {
        double velocity = 0.42;
        final PotionEffect potionEffect = player.getPotionEffect(PotionEffectType.JUMP);
        if (potionEffect != null)
        {
            velocity += (potionEffect.getAmplifier() + 1) * 0.1;
        }

        return velocity;
    }

    // pobiera z linii czasu ostatnie ustawione dla gracza velocity
    /*private Vector computeVelocityFromTimeline()
    {
        final TimelineWalker walkerForScope = this.playerData.getTimeline().createWalkerForScope(TimelineAnalyserConfig.Scope.SECOND);
        final VelocityAppliedTimelineEvent velocityAppliedTimelineEvent = walkerForScope.previous(VelocityAppliedTimelineEvent.class);
        if (velocityAppliedTimelineEvent != null)
        {
            //Bukkit.broadcastMessage("odczytano velocity z timeline: " + velocityAppliedTimelineEvent.getVelocity().getY());
            return velocityAppliedTimelineEvent.getVelocity();
        }

        return null;
    }*/
}