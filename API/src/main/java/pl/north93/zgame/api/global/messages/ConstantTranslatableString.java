package pl.north93.zgame.api.global.messages;

import java.util.Locale;
import java.util.Objects;

import net.md_5.bungee.api.chat.BaseComponent;
import pl.north93.zgame.api.bukkit.utils.chat.ChatUtils;
import pl.north93.zgame.api.global.utils.Vars;

class ConstantTranslatableString extends TranslatableString
{
    public static final TranslatableString EMPTY = new ConstantTranslatableString(""); // uzywane w TranslatableString#empty()
    private final BaseComponent fixedValue;

    ConstantTranslatableString(final String legacyText)
    {
        this(ChatUtils.fromLegacyText(legacyText));
    }

    ConstantTranslatableString(final BaseComponent fixedValue)
    {
        this.fixedValue = fixedValue;
    }
    
    @Override
    public BaseComponent getValue(Locale locale, Vars<Object> params)
    {
        return fixedValue;
    }

    @Override
    public int hashCode()
    {
        return fixedValue.hashCode(); // todo Przy zmianie na BaseComponent ta linijka stracila sens?
    }

    @Override
    public boolean equals(Object obj)
    {
        if ( this == obj )
        {
            return true;
        }
        
        if ( obj == null || obj.getClass() != this.getClass() )
        {
            return false;
        }
        
        ConstantTranslatableString other = (ConstantTranslatableString) obj;
        return Objects.equals(other.fixedValue, this.fixedValue);
    }

    @Override
    public String toString()
    {
        return "ConstantTranslatableString [fixedValue=" + fixedValue + "]";
    }
}
