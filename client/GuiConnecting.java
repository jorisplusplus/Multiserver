package joris.multiserver;


import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;

public class GuiConnecting extends GuiScreen
{
    private String Target;
    private int delay;

    public GuiConnecting(String target) {
        this.Target = target;
        this.delay  = 0;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel", new Object[0])));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.delay++;
        if(this.delay == 30 ) {
            ServerData serverData = new ServerData( "Change server", this.Target );
            FMLClientHandler.instance().setupServerList();
            FMLClientHandler.instance().connectToServer( new GuiMainMenu(), serverData );
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if( button.id == 0 ) {
            this.mc.displayGuiScreen( new GuiMainMenu() );
        }
    }

    @Override
    public void drawScreen( int par1, int par2, float par3) {
        this.drawDefaultBackground();
        this.drawCenteredString( this.fontRendererObj, "Joining server...", this.width / 2, this.height / 2 - 50, 16777215 );
        super.drawScreen( par1, par2, par3 );
    }
}

