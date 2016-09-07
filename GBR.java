/*#!J2SE#*///<editor-fold>
//--import javax.microedition.midlet.MIDlet;
//--import javax.microedition.lcdui.Display;
//--import javax.microedition.lcdui.Displayable;
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
import java.applet.Applet;
import java.awt.Frame;
import java.awt.Container;
/*$J2SE$*///</editor-fold>

/**
   This is the startup stub for the MIDP 1.0 GBR Java GameBoy emulator, derived 
   from David Winchurch's GameBoyEmu.

   @author David Edwards
   @version 1.0
**/

/*#J2SE#*///<editor-fold>
public class GBR extends Applet
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
//--public final class GBR extends MIDlet
/*$!J2SE$*///</editor-fold>
{
   // Instance of GameScreen, that handles the UI in the MIDlet world
private GameBoyVideo gameScreen;

/*#DefaultConfiguration#*///<editor-fold>
//-- private final String romFileName = "/tetris.gb";
//-- private final int lowDelayAddr = 0x2ee;
//-- private final int highDelayAddr = 0x2f1;
/*$DefaultConfiguration$*///</editor-fold>
/*#TetrisDX#*///<editor-fold>
//-- private final String romFileName = "/tetrisdx.gb";
//-- private final int lowDelayAddr = 0x283;
//-- private final int highDelayAddr = 0x288;
/*$TetrisDX$*///</editor-fold>
/*#SuperMarioLand#*///<editor-fold>
//-- private final String romFileName = "/sml.gb";
//-- private final int lowDelayAddr = 0x297;
//-- private final int highDelayAddr = 0x29b;
/*$SuperMarioLand$*///</editor-fold>
/*#J2SE#*///<editor-fold>
public String romFileName = null;
public int lowDelayAddr = 0;
public int highDelayAddr = 0;
/*$J2SE$*///</editor-fold>
/*#Kwirk#*///<editor-fold>
//-- private final String romFileName = "/kwirk.gb";
//-- private final int lowDelayAddr = 0xfd3;
//-- private final int highDelayAddr = 0xfd7;
/*$Kwirk$*///</editor-fold>
/*#Frogger#*///<editor-fold>
//-- private final String romFileName = "/frogger.gbc";
//-- private final int lowDelayAddr = 0x27f;
//-- private final int highDelayAddr = 0x284;
/*$Frogger$*///</editor-fold>
/*
 *  Constructor. Does nothing.
 */

public GBR()
{
}
/**

  This is the main procedure called when GBR is run by Java in an Applet.

 **/
/*#J2SE#*///<editor-fold>
public void init() {}
/*$J2SE$*///</editor-fold>

/*#!J2SE#*///<editor-fold>
//--  public void startApp()
//--  {
//--        Displayable current = Display.getDisplay(this).getCurrent();
//--        if (current == null)
//--        {
//--            gameScreen = new GameBoyVideo(this, romFileName, lowDelayAddr,
//--                                           highDelayAddr);
//--            Display.getDisplay(this).setCurrent(gameScreen);
//--        }
//--        else
//--        {
//--            Display.getDisplay(this).setCurrent(current);
//--        }
//--  }
/*$!J2SE$*///</editor-fold>
public void start()
{
/*#J2SE#*///<editor-fold>
    setVisible(true);
    if (romFileName == null)
    {

        romFileName = getParameter("ROM"); 
        if (romFileName == null)
        {
            romFileName = "/tetris.gb";
            lowDelayAddr  = 0x2ee;
            highDelayAddr = 0x2f1;
        } 
        else
        {
            String xl = getParameter("lowDelay"); 
            String xh = getParameter("highDelay"); 
            if (xl != null && xh != null)
            {
                try
                {
                    lowDelayAddr  = Integer.parseInt(xl, 16);
                    highDelayAddr  = Integer.parseInt(xh, 16);
                }
                catch(Exception e) {}
            }
        }
    }
/*$J2SE$*///</editor-fold>
    if (gameScreen == null)
        gameScreen = new GameBoyVideo(this, romFileName, lowDelayAddr,
                                  highDelayAddr);
    else
        gameScreen.getCPU().startThread();
}
/**

  Run everything required for shutdown. Empty ...

 **/
/*#J2SE#*///<editor-fold>
public void stop()
{
    gameScreen.getCPU().stopThread();
}
/*$J2SE$*///</editor-fold>
public void pauseApp()
{
}

public void destroyApp(boolean unconditional)
{
}

public void quit()
{
/*#J2SE#*///<editor-fold>
    destroyApp(false);
    Container c = getParent();
    if ( c instanceof java.awt.Frame)
        System.exit(0);
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
//--        notifyDestroyed();
/*$!J2SE$*///</editor-fold>
}
/**

   Main procedure to allow the emulator to be run from the command line.

 **/
/*#J2SE#*///<editor-fold>
public static void main(String []args)
{
    java.awt.Frame holder = new java.awt.Frame();
    holder.setSize(660,600);
    holder.setVisible(true);
    GBR gbr = new GBR();
    holder.add(gbr);
    gbr.init();
    gbr.resize(660,600);
    if (args.length > 0)
    {
        gbr.romFileName = args[0];
        if (args.length > 2)
        {
            try
            {
                gbr.lowDelayAddr = Integer.parseInt(args[1], 16);
                gbr.highDelayAddr = Integer.parseInt(args[2], 16);
            }
            catch(Exception e) {}
        }
    }
    gbr.start();
    return;
}
/*$J2SE$*///</editor-fold>
} 
