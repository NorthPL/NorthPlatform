package pl.north93.zgame.api.global.messages;

import java.text.MessageFormat;
import java.util.Locale;

public interface Messageable
{
    /**
     * Zwraca język używany przez ten obiekt (np.gracza)
     *
     * @return język.
     */
    Locale getMyLocale();

    /**
     * Wysyła surową wiadomość do gracza.
     * Nie zostaną przeprowadzone na niej żadne operacje.
     *
     * @param message treść wiadomości.
     */
    void sendRawMessage(String message);

    /**
     * Wysyła surową wiadomość do gracza. Bez tłumaczenia.
     * Obsługuje parametry według MessageFormat.
     *
     * @param message treść wiadomości.
     * @param params parametry.
     */
    default void sendRawMessage(final String message, final Object... params)
    {
        this.sendRawMessage(MessageFormat.format(message, params));
    }

    /**
     * Wysyła przetłumaczoną wiadomość do gracza.
     *
     * @param messagesBox obiekt przechowujący wiadomości.
     * @param key nazwa klucza wiadomości.
     */
    default void sendMessage(final MessagesBox messagesBox, final String key)
    {
        this.sendRawMessage(messagesBox.getMessage(this.getMyLocale(), key), true);
    }

    /**
     * Wysyła przetłumaczoną wiadomość do gracza.
     * Obsługuje parametry według MessageFormat.
     *
     * @param messagesBox obiekt przechowujący wiadomości.
     * @param key nazwa klucza wiadomości.
     * @param params parametry.
     */
    default void sendMessage(final MessagesBox messagesBox, final String key, final Object... params)
    {
        this.sendRawMessage(messagesBox.getMessage(this.getMyLocale(), key), params);
    }
}
