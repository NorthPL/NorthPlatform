package pl.north93.zgame.api.global.messages;

import java.util.Locale;

/**
 * Prosty enum ulatwiajacy odmiane przez liczby.
 */
public enum PluralForm
{
    ONE,
    SOME,
    MANY;

    public String getName()
    {
        return this.name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Aby uzyc tej metody klucz musi miec postac: costam.ONE/costam.SOME/costam.MANY
     * Jako klucz podajemy wtedy costam, a num jako nasza liczbe.
     * Dostajemy klucz z doklejona wlasciwa forma.
     *
     * @param key nazwa klucza bez kropki na koncu.
     * @param num ilosc przez ktora odmieniamy.
     * @return klucz we wlasciwej formie.
     */
    public static String transformKey(final String key, final int num)
    {
        return key + "." + get(num).getName();
    }

    public static PluralForm get(final int num)
    {
        if (num == 1)
        {
            return ONE;
        }
        else if (num < 5)
        {
            return SOME;
        }

        return MANY;
    }
}
