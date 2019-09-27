package pl.north93.northplatform.controller.servers.scaler.value;

import pl.north93.northplatform.controller.servers.groups.LocalManagedServersGroup;

/**
 * Reprezentuje wartosc wedlug ktorej mozna skonfigurowac skalowanie serwerow.
 */
public interface IScalingValue
{
    /**
     * Unikalny identyfikator wartosci uzywany w konfiguracji.
     *
     * @return unikalny identyfikator wartosci.
     */
    String getId();

    /**
     * Oblicza wartosc dla danej grupy serwerow i ja zwraca.
     * <p>
     * Wartosc jest cachowana podczas jednego cyklu generowania decyzji.
     * {@link pl.north93.northplatform.controller.servers.scaler.ValueFetchCache}
     *
     * @param managedServersGroup Grupa serwerow dla ktorej obliczamy wartosc.
     * @return Obliczona wartosc.
     */
    double calculate(LocalManagedServersGroup managedServersGroup);
}