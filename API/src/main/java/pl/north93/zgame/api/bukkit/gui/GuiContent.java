package pl.north93.zgame.api.bukkit.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import com.google.common.base.Preconditions;

import pl.north93.zgame.api.global.messages.TranslatableString;
import pl.north93.zgame.api.global.utils.Vars;

public class GuiContent extends GuiContainerElement
{
    private final Gui gui;
    
    private TranslatableString title = TranslatableString.of("");
    
    private GuiCanvas renderedCanvas;
    
    public GuiContent(Gui gui, int height)
    {
        super(9, height);
        this.gui = gui;
        this.renderedCanvas = new GuiCanvas(9, height);
    }
    
    public GuiCanvas getRenderedCanvas()
    {
        return renderedCanvas;
    }
    
    public TranslatableString getTitle()
    {
        return title;
    }
    
    public void setTitle(TranslatableString title)
    {
        Preconditions.checkArgument(title != null, "Title cannot be null");
        
        this.title = title;
        markDirty();
    }
    
    public void setHeight(int height)
    {
        setSize(9, height);
        renderedCanvas.resize(9, height);
    }
    
    public void renderContent()
    {
        renderedCanvas.clear();
        render(renderedCanvas);
    }
    
    public void renderToInventory(Player player, Inventory inv)
    {
        Preconditions.checkState(inv.getType() == InventoryType.CHEST);
        
        inv.clear();
        
        for ( int i = 0; i < renderedCanvas.getWidth(); i++ )
        {
            for ( int j = 0; j < renderedCanvas.getHeight(); j++ )
            {
                GuiIcon icon = renderedCanvas.getGuiIconInSlot(i, j);
                if ( icon != null )
                {
                    inv.setItem(j * 9 + i, icon.toItemStack(player, gui.getVariables()));
                }
            }
        }
    }
    
    @Override
    public void setSize(int width, int height)
    {
        Preconditions.checkArgument(width == 9, "Content width must be equal 9");
        
        super.setSize(width, height);
    }
}