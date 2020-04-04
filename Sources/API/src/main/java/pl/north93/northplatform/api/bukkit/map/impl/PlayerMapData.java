package pl.north93.northplatform.api.bukkit.map.impl;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import lombok.ToString;
import pl.north93.northplatform.api.bukkit.map.IMapCanvas;

/**
 * Klasa przechowujaca stan map u danego gracza.
 * <p>
 * Dane sa przechowywane jako WeakHashMap wiec nie ma potrzeby
 * recznego zwalniania identyfikatorow. Zapewnia to sensowne
 * cachowanie identyfikatorow.
 */
@ToString
class PlayerMapData
{
    private final Player player;
    private final Map<MapImpl, MapContainer> mapping;
    private int latestId;

    public PlayerMapData(final Player player)
    {
        this.player = player;
        this.mapping = new WeakHashMap<>();
    }

    /**
     * Metoda zwracajaca ID przypisane do tej mapy dla tego gracza.
     *
     * @param map Mapa ktorej powiazane ID sprawdzamy.
     * @return ID powiazane z ta mapa.
     */
    public int getMapId(final MapImpl map)
    {
        final MapContainer container = this.getOrComputeContainer(map);
        return container.getId();
    }

    public boolean isMapVisible(final MapImpl map)
    {
        return map.isTrackedBy(this.player);
    }

    public boolean isClientCanvasMatchesServer(final MapImpl map)
    {
        final MapContainer container = this.getOrComputeContainer(map);

        final IMapCanvas serverCanvas = container.getServerCanvas();
        if (serverCanvas == null)
        {
            return true; // na pewno?
        }

        final IMapCanvas clientCanvas = container.getClientCanvas();
        if (clientCanvas == null)
        {
            return false;
        }

        // porownujemy canvas widoczny u klienta i na serwerze
        return serverCanvas.equals(clientCanvas);
    }

    public boolean hasAnyServerCanvas(final MapImpl map)
    {
        final MapContainer container = this.getOrComputeContainer(map);
        return container.getServerCanvas() != null;
    }

    public void uploadServerCanvasToClient(final MapImpl map)
    {
        final MapContainer container = this.getOrComputeContainer(map);
        // delegujemy wyslanie pakietu do specjalnej klasy
        CanvasUploader.doUpload(this.player, container.getId(), container.getClientCanvas(), container.getServerCanvas());
        // zapisujemy nowy canvas widoczny u klienta
        container.setClientCanvas(container.getServerCanvas());
    }

    public void resetAllClientSideCanvases()
    {
        // usuwamy aktualny stan wszystkich kanw u klienta
        for (final MapContainer container : this.mapping.values())
        {
            container.setClientCanvas(null);
        }
    }

    public MapContainer getOrComputeContainer(final MapImpl map)
    {
        return this.mapping.computeIfAbsent(map, this::generateNewContainer);
    }

    private MapContainer generateNewContainer(final MapImpl map)
    {
        final Collection<Integer> values = this.mapping.values().stream().map(MapContainer::getId).collect(Collectors.toSet());
        do
        {
            this.latestId = (++this.latestId) % Short.MAX_VALUE;
        } while (values.contains(this.latestId));

        final MapContainer container = new MapContainer(this.latestId);
        this.mapping.put(map, container);
        return container;
    }
}

@ToString
final class MapContainer
{
    private final int id;
    private IMapCanvas serverCanvas;
    private IMapCanvas clientCanvas;

    public MapContainer(final int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return this.id;
    }

    public IMapCanvas getServerCanvas()
    {
        return this.serverCanvas;
    }

    public void setServerCanvas(final IMapCanvas serverCanvas)
    {
        this.serverCanvas = serverCanvas;
    }

    public IMapCanvas getClientCanvas()
    {
        return this.clientCanvas;
    }

    public void setClientCanvas(final IMapCanvas clientCanvas)
    {
        this.clientCanvas = clientCanvas;
    }
}