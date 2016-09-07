/*#!J2SE#*///<editor-fold>
//--  import javax.microedition.lcdui.Canvas;
//--  import javax.microedition.lcdui.Command;
//--  import javax.microedition.lcdui.CommandListener;
//--  import javax.microedition.lcdui.Image;
//--  import javax.microedition.lcdui.Graphics;
//--  import javax.microedition.lcdui.Displayable;
/*$!J2SE$*///</editor-fold>

/*#J2SE#*///<editor-fold>
import java.awt.Canvas;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.Color;
/*$J2SE$*///</editor-fold>


/**

This class is responsible for decoding the way the graphics work inside the
GameBoy so that they can be displayed on a Java-capable mobile phone.

@author David Winchurch, David Edwards
@version 1.0

**/

/*#!J2SE#*///<editor-fold>
//--  public final class GameBoyVideo extends Canvas implements CommandListener
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
public final class GameBoyVideo extends Canvas implements KeyListener,
            ActionListener
/*$J2SE$*///</editor-fold>
{
private final String romFileName;
private final int lowDelayAddr;
private final int highDelayAddr;
private GameBoyCPU CPU;    // The virtual CPU
public GameBoyCPU getCPU()
{
    return CPU;
}
//    A boolean array used to store the state of a key press
private boolean[] KEYS_PRESSED = {
        false, false, false, false, false, false,
        false, false};
//    An array used to set which key combinations do what allowing for
//    configurable keys
/*#!J2SE#*///<editor-fold>
//--private int[] KEYS={UP, DOWN, LEFT, RIGHT, GAME_A, GAME_B, FIRE, GAME_C,
//--                    GAME_D};
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
private int[] KEYS = {
        KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT,
        KeyEvent.VK_RIGHT, KeyEvent.VK_X, KeyEvent.VK_Z, KeyEvent.VK_ENTER,
        KeyEvent.VK_SHIFT, KeyEvent.VK_SPACE};
/*$J2SE$*///</editor-fold>
//    Which array position correspond to which keys
private static final int DIK_UP = 0;
private static final int DIK_DOWN = 1;
private static final int DIK_LEFT = 2;
private static final int DIK_RIGHT = 3;
private static final int DIK_A = 4;
private static final int DIK_B = 5;
private static final int DIK_START = 6;
private static final int DIK_SELECT = 7;
private static final int DIK_ALL_BUTTONS = 8;
private final GBR midlet;
/*#!J2SE#*///<editor-fold>
//--   private final Command exitCommand;
//--   private final Command newGameCommand;
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
private PopupMenu popupMenu = new PopupMenu("David Edwards");
private MenuItem newGame = new MenuItem("New Game"),
            exit = new MenuItem("Exit");
/*$J2SE$*///</editor-fold>
private int jumpFactor, xScale, yScale, pixelSize;
private int posX, posY;
/*#!J2SE#*///<editor-fold>
//--  static final int white = 0xffffff;
//--  static final int black = 0;
//--  static final int gray =  /* 0x4040f0 */  0x808080;
//--  static final int lightGray = /* 0xf08040 */ 0xc0c0c0;
/*$!J2SE$*///</editor-fold>
//    A reference to the colours used in black and white emulation mode
/*#!J2SE#*///<editor-fold>
//--  private static int[][] DEFAULTS={{white, lightGray},{gray, black}},
//--  BGPALETTE={{white, lightGray},{gray, black}},
//--  OBJ0PALETTE={{white, lightGray},{gray, black}},
//--  OBJ1PALETTE={{white, lightGray},{gray, black}};
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
private static Color[][]
            DEFAULTS = {
        { Color.white, Color.lightGray},
        { Color.gray, Color.black}},
            BGPALETTE = {
        { Color.white, Color.lightGray},
        { Color.gray, Color.black}},
            OBJ0PALETTE = {
        { Color.white, Color.lightGray},
        { Color.gray, Color.black}},
            OBJ1PALETTE = {
        { Color.white, Color.lightGray},
        { Color.gray, Color.black}};
/*$J2SE$*///</editor-fold>
/**

    Set up Black and White Colour emulation

 **/
public void bwSetup()
{
    BGPALETTE[0][0] = DEFAULTS[0][0];
    BGPALETTE[0][1] = DEFAULTS[0][1];
    BGPALETTE[1][0] = DEFAULTS[1][0];
    BGPALETTE[1][1] = DEFAULTS[1][1];
    OBJ0PALETTE[0][0] = DEFAULTS[0][0];
    OBJ0PALETTE[0][1] = DEFAULTS[0][1];
    OBJ0PALETTE[1][0] = DEFAULTS[1][0];
    OBJ0PALETTE[1][1] = DEFAULTS[1][1];
    OBJ1PALETTE[0][0] = DEFAULTS[0][0];
    OBJ1PALETTE[0][1] = DEFAULTS[0][1];
    OBJ1PALETTE[1][0] = DEFAULTS[1][0];
    OBJ1PALETTE[1][1] = DEFAULTS[1][1];
    return;
}

public void resetPalettes()
{
    for (int i = 0; i < 4; i++)
    {
        for (int j = 0; j < 8; j++)
        {
/*#!J2SE#*///<editor-fold>
//--          setBackColour(i, j, white);
//--          setForeColour(i, j, white);
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
            setBackColour(i, j, Color.white);
            setForeColour(i, j, Color.white);
/*$J2SE$*///</editor-fold>
        }
    }
    return;
}

/**

    Sets the background palette to the values specified by location 0x147 in
    HIGH_MEM

 **/

public void setBackPalette()
{
    for (int i = 0; i < 4; i++)
    {
        setBackColour(i, 0, BGPALETTE[
            GameBoyCPU.BIT(HIGH_MEM[GameBoyCPU.LCD_BACK_PALETTE],
                        (i << 1) + 1)]
          [GameBoyCPU.BIT(HIGH_MEM[GameBoyCPU.LCD_BACK_PALETTE], i << 1)]);
    }
}

/**

 Sets the foreground palette 0 to the values specified by location 0x148 in
 the High Memory of the GameBoy

 **/

public void setForePalette0()
{
    for (int i = 0; i < 4; i++)
        setForeColour(i, 0,
          OBJ0PALETTE[
                GameBoyCPU.BIT(HIGH_MEM[GameBoyCPU.LCD_SPR0_PALETTE],
            (i << 1) + 1)]
             [GameBoyCPU.BIT(HIGH_MEM[GameBoyCPU.LCD_SPR0_PALETTE], i << 1)]);
}
/**

    Sets the foreground palette 1 to the values specified by location 0x149
    in HIGH_MEM

 **/
public void setForePalette1()
{
    for (int i = 0; i < 4; i++)
    {
        setForeColour(i, 1, OBJ1PALETTE[
             GameBoyCPU.BIT(HIGH_MEM[GameBoyCPU.LCD_SPR1_PALETTE],
                          (i << 1) + 1)]
             [GameBoyCPU.BIT(HIGH_MEM[GameBoyCPU.LCD_SPR1_PALETTE], i << 1)]);
    }
}

/**

    Converts a 15 bit gameboy colour into a 24 bit java colour.

    @param c the colour for conversion
    @return the converted colour

 **/

public static
/*#!J2SE#*///<editor-fold>
//--  int
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
   Color
/*$J2SE$*///</editor-fold>
    getGBColour(int c)
{
/*#!J2SE#*///<editor-fold>
//--  return ( ((c&0x1F)<<19)| (((c>>5)&0x1F) <<11)| ((((c>>10)&0x1F)<<3)));
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
    return new Color(
                ((c & 0x1F) << 19) | (((c >> 5) & 0x1F) << 11)
                | ((((c >> 10) & 0x1F) << 3)));
/*$J2SE$*///</editor-fold>
}

/**

  Converts a 24 bit java colour into the upper 8 bits of a 15 bit gameboy colour

  @param c the colour for conversion
  @return the converted colour

 **/

public static int rgb2GBUpper(
/*#!J2SE#*///<editor-fold>
//--  int c
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
            Color col
/*$J2SE$*///</editor-fold>
                       )
{
/*#J2SE#*///<editor-fold>
int c = col.getRGB();
/*$J2SE$*///</editor-fold>

    return ((((c & 0xff) >> 3) << 10) + ((c & 0xff00) >> 6)) & 0x7F00;
}

/**

  Converts a 24 bit java colour into the lower 8 bits of a 15 bit gameboy colour

  @param c the colour for conversion
  @return the converted colour

 **/

public static int rgb2GBLower(
/*#!J2SE#*///<editor-fold>
//--  int c
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
            Color col
/*$J2SE$*///</editor-fold>
               )
{
/*#J2SE#*///<editor-fold>
int c = col.getRGB();
/*$J2SE$*///</editor-fold>

    return ((((c & 0xff00) >> 6) + ((c & 0xff0000) >> 19)) & 0xFF);
}

/**

 Converts a 24 bit java colour into 16 bits for a 15 bit gameboy colour

 @param c the colour for conversion
 @return the converted colour

 **/

public static char rgb2GBColour(int c)
{
    return (char) ((((((c >> 8) & 0xff) >> 3) << 5)
                + (((c & 0xff) >> 3) << 10) + (((c >> 16) & 0xff) >> 3))
                        & 0x7FFF);
}

/**

    Constructor. Creates the Frame and adds all the required components to it.
    Creates and instantiates all the global values and objects

 **/

public GameBoyVideo(GBR midlet, String fileName, int low, int high)
{
    this.midlet = midlet;
/*#!J2SE#*///<editor-fold>
//--  posY = getHeight();
//--  posX = getWidth();
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
    posY = midlet.getHeight();
    posX = midlet.getWidth();
    if (posX == 0 || posY == 0)
    {
        posX = 640;
        posY = 576;
        midlet.resize(posX, posY);
        midlet.setVisible(true);
    }
    setSize(posX, posY);
    midlet.add(this);
/*$J2SE$*///</editor-fold>
    skipFactor = 1
            + ((143 / posY < 159 / posX) ? (159 / posX) : (143 / posY));
    if (skipFactor == 1)
    {
        pixelSize = (posY / 144 > posX / 160) ? (posX / 160) : (posY / 144);
        xScale = 160;
        yScale = 144;
        jumpFactor = xScale;
    }
    else
    {
        pixelSize = 1;
        xScale = 160 / skipFactor;
        yScale = 144 / skipFactor;
        jumpFactor = xScale / skipFactor;
    }
//    System.out.println("posX " + posX + " posY " + posY + " skipFactor " +
//    skipFactor + " pixelSize " + pixelSize);
    posX = (posX - xScale * pixelSize) / 2;
    posY = (posY - yScale * pixelSize) / 2;
/*#!NoImage#*///<editor-fold>
/*#!J2SE#*///<editor-fold>
//--    screenImage = Image.createImage(xScale * pixelSize, yScale * pixelSize);
/*$J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
    setVisible(true);
    screenImage = createImage(xScale * pixelSize, yScale * pixelSize);
/*$J2SE$*///</editor-fold>
    screenGraphics = screenImage.getGraphics();
/*$!NoImage$*///</editor-fold>

    romFileName = fileName;
    lowDelayAddr = low;
    highDelayAddr = high;
    CPU = new GameBoyCPU(KEYS_PRESSED, this, romFileName, lowDelayAddr,
                         highDelayAddr);
    if (CPU.loadRom())
        CPU.fullReset();

/*
 * configure Screen commands
 */
/*#!J2SE#*///<editor-fold>
//--  exitCommand = new Command("Exit", Command.EXIT, 1);
//--  newGameCommand = new Command("New", Command.SCREEN, 2);
//--  addCommand(exitCommand);
//--  addCommand(newGameCommand);
//--  setCommandListener(this);
/*$!J2SE$*///</editor-fold>
//    Adds action listeners
/*#J2SE#*///<editor-fold>
    addKeyListener(this);
    newGame.addActionListener(this);
    exit.addActionListener(this);
    popupMenu.add(newGame);
    popupMenu.add(exit);
    add(popupMenu);
//    Define the Listener object for the mouse as a nested class
    this.addMouseListener(new MouseAdapter() {});
    enableEvents(AWTEvent.MOUSE_EVENT_MASK); 
//    Makes this the object in focus
    requestFocus();
/*$J2SE$*///</editor-fold>
//  Start the virtual CPU
    CPU.startThread();
}

/**

   This method is required to make the popup menu pop up.  It
   uses the low-level Java event handling mechanism to test all mouse
   events (except mouse motion events) to see if they are the platform-
   dependent popup menu trigger.  If so, it calls show() to pop the
   popup up.  If not, it passes the event to the superclass version of
   this method so that it is dispatched as usual and can be passed to
   the listener object registered by the init() method.

 **/
/*#J2SE#*///<editor-fold>
public void processMouseEvent(MouseEvent e)
{
    if ((popupMenu != null) && e.isPopupTrigger())
        popupMenu.show(this, e.getX(), e.getY());
    else
        super.processMouseEvent(e);
}
/*$J2SE$*///</editor-fold>
/**

    Paint method.

    @param g the specified context to use for updating.

 **/

public void paint(Graphics g)
{
    paintAll(g);
}

/**

    Handle User Commands

 **/

/*#!J2SE#*///<editor-fold>
//--  public void commandAction(Command c, Displayable d)
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
public void actionPerformed(ActionEvent event)
/*$J2SE$*///</editor-fold>
{
/*#J2SE#*///<editor-fold>
Object c = event.getSource();
    if (c == exit)
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
//--  if (c == exitCommand)
/*$!J2SE$*///</editor-fold>
    {
        CPU.stopThread();
        try
        {
            Thread.sleep(250);
        }
        catch (Exception ex)
        {}
        midlet.quit();
    }
    else
/*#!J2SE#*///<editor-fold>
//--    if (c == newGameCommand)
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
    if (c == newGame)
/*$J2SE$*///</editor-fold>
    {
        CPU.stopThread();
        try
        {
            Thread.sleep(250);
        }
        catch (Exception ex)
        {}
        LOW_MEM = null;
        HIGH_MEM = null;
        VRAM = null;
        pixelColourPriority = null;
        CPU = null;
        CPU = new GameBoyCPU(KEYS_PRESSED, this, romFileName, lowDelayAddr,
                         highDelayAddr);
        if (CPU.loadRom())
            CPU.fullReset();
/*#!J2SE#*///<editor-fold>
//--  screenGraphics.setColor(white);
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
        screenGraphics.setColor(Color.white);
/*$J2SE$*///</editor-fold>
        screenGraphics.fillRect(0, 0, xScale * pixelSize + 1,
                    yScale * pixelSize + 1);
        CPU.startThread();
    }
}

/**

  This is the key pressed event handler for GBR. It accepts KeyEvents generated
  by pressing a key when any component with a KeyListener has focus

  @param k The KeyEvent that was generated.

 **/

/*#!J2SE#*///<editor-fold>
//--  public void keyPressed(int keycode)
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
public void keyPressed(KeyEvent k)
/*$J2SE$*///</editor-fold>
{
/*#!J2SE#*///<editor-fold>
//--  int keypress = getGameAction(keycode);
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
    int keypress = k.getKeyCode();
/*$J2SE$*///</editor-fold>
//    checks to see if the correct keys have been pressed and sets the
//    corresponding boolean to true
    if (KEYS[DIK_ALL_BUTTONS] == keypress)
    {
        KEYS_PRESSED[DIK_A] = true;
        KEYS_PRESSED[DIK_B] = true;
        KEYS_PRESSED[DIK_START] = true;
        KEYS_PRESSED[DIK_SELECT] = true;
    }
    else
    if (KEYS[DIK_UP] == keypress)
        KEYS_PRESSED[DIK_UP] = true;
    else
    if (KEYS[DIK_DOWN] == keypress)
        KEYS_PRESSED[DIK_DOWN] = true;
    else
    if (KEYS[DIK_LEFT] == keypress)
        KEYS_PRESSED[DIK_LEFT] = true;
    else
    if (KEYS[DIK_RIGHT] == keypress)
            KEYS_PRESSED[DIK_RIGHT] = true;
    else
    if (KEYS[DIK_A] == keypress)
        KEYS_PRESSED[DIK_A] = true;
    else
    if (KEYS[DIK_B] == keypress)
        KEYS_PRESSED[DIK_B] = true;
    else
    if (KEYS[DIK_START] == keypress)
        KEYS_PRESSED[DIK_START] = true;
    else
    if (KEYS[DIK_SELECT] == keypress)
        KEYS_PRESSED[DIK_SELECT] = true;
}

/**

 This is the key released event handler for GBR. It accepts KeyEvents generated
 by releasing a key when any component with a KeyListener has focus

 @param k The KeyEvent that was generated.

**/

/*#!J2SE#*///<editor-fold>
//--  public void keyReleased(int keycode)
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
public void keyReleased(KeyEvent k)
/*$J2SE$*///</editor-fold>
{
/*#!J2SE#*///<editor-fold>
//--  int keypress = getGameAction(keycode);
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
int keypress = k.getKeyCode();
/*$J2SE$*///</editor-fold>
//    checks to see if the correct keys have been released and sets the
//     corresponding boolean to false
    if (KEYS[DIK_UP] == keypress)
        KEYS_PRESSED[DIK_UP] = false;
    else
    if (KEYS[DIK_DOWN] == keypress)
        KEYS_PRESSED[DIK_DOWN] = false;
    else
    if (KEYS[DIK_LEFT] == keypress)
        KEYS_PRESSED[DIK_LEFT] = false;
    else
    if (KEYS[DIK_RIGHT] == keypress)
        KEYS_PRESSED[DIK_RIGHT] = false;
    else
    if (KEYS[DIK_A] == keypress)
        KEYS_PRESSED[DIK_A] = false;
    else
    if (KEYS[DIK_B] == keypress)
        KEYS_PRESSED[DIK_B] = false;
    else
    if (KEYS[DIK_START] == keypress)
        KEYS_PRESSED[DIK_START] = false;
    else
    if (KEYS[DIK_SELECT] == keypress)
        KEYS_PRESSED[DIK_SELECT] = false;
    else
    if (KEYS[DIK_ALL_BUTTONS] == keypress)
    {
        KEYS_PRESSED[DIK_A] = false;
        KEYS_PRESSED[DIK_B] = false;
        KEYS_PRESSED[DIK_START] = false;
        KEYS_PRESSED[DIK_SELECT] = false;
    }
}

/**

 Method required by the interface ...

 **/
/*#J2SE#*///<editor-fold>
public void keyTyped(KeyEvent k) {}
/*$J2SE$*///</editor-fold>
//    Pointers to relevant areas in the Virtual Gameboy's memory arrays
private byte LOW_MEM[], HIGH_MEM[], VRAM[];
/*#!NoImage#*///<editor-fold>
private byte colour8;
//    The image that will be drawn to the screen
private Image screenImage;
//    The graphics surface to draw to
private Graphics screenGraphics;
/*$!NoImage$*///</editor-fold>

//    The global variables required to keep track of the graphics subsystems
private int winPoint, tileArea, bgPoint;
private byte[] pixelColourPriority;
//    array that stores details of what has been used for each pixel
//    arrays that store the colours for each palette
/*#!J2SE#*///<editor-fold>
//--  private int 
//--  backPal[][]  = new int[4][8],
//--  forePal[][]  = new int[4][8];
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
private Color
        backPal[][] = new Color[4][8],
        forePal[][] = new Color[4][8];
/*$J2SE$*///</editor-fold>
/*
 * Working variables - the program ends up smaller this way. Have to be careful
 * with threads, though.
 */
private int scrollX, scrollY, tile, tile4, xTile, yTile, bitX, bitY, colour,
        charBnkSlct,
        tileProp, winX, winY, currentSpr, sprX, sprY, sprF, sprSize,
        tx, ty, pcp,
        yLookUp, imageX, imageY,
        yTile5, bitY1, sprPerLine, len, x, i;
private boolean pixelPrior;
private int skipFactor;
///**
//
//    @param c encoding of the palette from which to derive a 24 bit value
//
// **/
// private static int get8bitColour(byte c)
// {
//     if ((c & 0x20) != 0)
//         return forePal[(c & 0x18)>>3][c&0x07]);
//     else
//         return backPal[(c & 0x18)>>3][c&0x07]);
// }
/**

  Initialiser. Assign the major arrays.

  @param lowMem pointer to the array corresponding to low memory
  @param highMem pointer to the array corresponding to 0xFE00 and higher
  @param vram pointer to the video graphics memory

 **/
void init(byte[] lowMem, byte[] highMem, byte[] vram)
{
/*
 * Provide global pointers to the virtual memory locations
 */
    LOW_MEM = lowMem;
    HIGH_MEM = highMem;
    VRAM = vram;
    pixelColourPriority = new byte[xScale * yScale];
}

/**

  @param g the graphics context to use for painting.

 **/
/*#J2SE#*///<editor-fold>
public void update(Graphics g)
{
    paintAll(g);
    return;
}
/*$J2SE$*///</editor-fold>

public void paintAll(Graphics g)
{
/*#NoImage#*///<editor-fold>
//--      int begX = g.getClipX();
//--       int begY = g.getClipY();
//--      int endX = begX + g.getClipWidth();
//--      if (endX > xScale)
//--          endX = xScale;
//--      int endY = begY + g.getClipHeight();
//--      if (endY > yScale)
//--          endY = yScale;
//--      for (int yLine = begY * xScale;
//--              begY < endY;
//--                  yLine += xScale, begY++)
//--          for (int xCol = begX; xCol < endX; xCol++)
//--          {
//--              g.setColor(get8bitColour(
//--                       pixelColourPriority[yLine + xCol]));
//--              g.fillRect(xCol, begY, 1, 1);
//--          }
/*$NoImage$*///</editor-fold>
/*#!NoImage#*///<editor-fold>
/*#!J2SE#*///<editor-fold>
//--  g.drawImage(screenImage, posX, posY, 0);
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
    g.drawImage(screenImage, posX, posY, null);
/*$J2SE$*///</editor-fold>
/*$!NoImage$*///</editor-fold>
 
}

/**

 This method decodes the 64 color tile based graphics system used in the
 GameBoy and converts it to a form displayable by the Java virtual machine.

 It calculates each pixel and sets it in the image.

 @returns the number of sprites on each line

**/

public int drawLine(int currentLine, boolean spritesOnly)
{
/*
 * Check the system is on a drawable line (143 lines out of 153 are visible)
 */

    if (currentLine >= 0x90 || (currentLine % skipFactor) != 0)
        return 0;
    yLookUp = jumpFactor * currentLine;
    imageY = pixelSize * currentLine / skipFactor;
    sprPerLine = 0;
/*    System.out.println("Y " + currentLine +
 *           (((HIGH_MEM[GameBoyCPU.LCD_CTRL] & 0x01) != 0)? " BG" : "") +
 *            (((HIGH_MEM[GameBoyCPU.LCD_CTRL] & 0x20) != 0)? " WN" : "") +
 *            (((HIGH_MEM[GameBoyCPU.LCD_CTRL] & 0x2) != 0)? " SP" : ""));
 *
 */
    if (!spritesOnly && ((HIGH_MEM[GameBoyCPU.LCD_CTRL] & 0x01) != 0))
    {
        scrollX = HIGH_MEM[GameBoyCPU.LCD_SCROLL_X] & 0xff;
        scrollY = (HIGH_MEM[GameBoyCPU.LCD_SCROLL_Y] & 0xff) + currentLine;
        if (scrollY > 255)
            scrollY -= 255;
        bitY = scrollY & 0x07;
        yTile = scrollY >>> 3;
        yTile5 = yTile << 5;
        for (x = 0, imageX = 0; x < 160;)
        {
/*
 * Work out which tile the pixel belongs to from the 32x32 tile map
 * Works out the x,y coordinate of the tile
 */
            xTile = scrollX >>> 3;
            bitX = scrollX & 0x07;
            len = 8 - bitX;
/*
 * For Gameboy Color, get the attributes of the current tile from the second
 * VRAM bank
 */
            tileProp = (int) (VRAM[0x2000 + bgPoint + (yTile5) + xTile]
                    & 0xff);
/*
 * Get the number of the tile the pixel belongs to
 */
            tile = (int) (VRAM[bgPoint + (yTile5) + xTile] & 0xFF);
/*
 * The tiles can be stored at two locations in memory. If it is 0x800 the tile
 * numbers are signed
 */
            if (tileArea == 0x800)
                tile ^= 0x80;
/*
 * Gameboy Color tiles can be x,y flipped depending on bits set.
 *
 * This code caters for this
 */
            if ((tileProp & 0x40) != 0)
                bitY1 = (7 - bitY) << 1;
            else
                bitY1 = bitY << 1;
 /*
  * Gameboy Color tile data can come from either VRAM bank depending on which
  * bits are set
  */
            charBnkSlct = ((tileProp & 0x08) != 0 ? 0x2000 : 0) + tileArea
                    + bitY1 + (tile << 4);
            pixelPrior = (tileProp & 0x80) != 0 ? true : false;
            for (i = 0; i < len && x < 160; i += skipFactor)
            {
                if ((tileProp & 0x20) == 0)
                    bitX = (7 - bitX);
/*
 * Calculate which of the 4 positions in one of the palette arrays the pixel
 * corresponds to
 */
                colour = ((((VRAM[charBnkSlct + 1] & 0xFF) >> (bitX)) & 0x01)
                        << 1)
                                | (((VRAM[charBnkSlct] & 0xFF) >> (bitX))
                                        & 0x01);
                bitX = imageX / pixelSize;
/*
 * Set the colour of the pixel. It makes a huge difference (about 50 percent)
 * to performance if we check the value hasn't changed.
 */
/*#NoImage#*///<editor-fold>
//--        pixelColourPriority[yLookUp+bitX]= (colour<<3) + (tileProp&0x07);
//--/*
//-- * Something like this is needed if we are to only repaint changed pixels.
//-- * The multiple repaints are very slow, however.
//-- */
//--//                   if (pixelColour[yLookUp+imageX] != (byte) colour)
//--//                   {
//--//                       pixelColour[yLookUp+imageX]= (byte) colour;
//--//                       repaint(imageX,
//--//                                   currentLine/skipFactor,1,1);
//--//                   }
//--
/*$NoImage$*///</editor-fold>
/*#!NoImage#*///<editor-fold>
//    if (imageX == 34 && currentLine/skipFactor == 16)
//    System.out.println("WHY (" + imageX + "," + (currentLine/skipFactor)
//    + "=" + (yLookUp + imageX) +")"
//    +Integer.toHexString(0xFF &
//    pixelColourPriority[yLookUp+bitX]));
                if ((pixelColourPriority[yLookUp + bitX] & 0xbf)
                        != ((colour << 3) + (tileProp & 0x7)))
                {
                    screenGraphics.setColor(backPal[colour][tileProp & 0x07]);
                    screenGraphics.fillRect(imageX, imageY, pixelSize,
                            pixelSize);
                    pixelColourPriority[yLookUp + bitX] = (byte) ((colour
                            << 3)
                                    + (tileProp & 0x7));
//    System.out.println("BG (" + imageX + "," + (currentLine/skipFactor)
//    + "=" + (yLookUp + imageX) +")"
//    +Integer.toHexString(0xFF &
//    pixelColourPriority[yLookUp+bitX]));
                }
/*$!NoImage$*///</editor-fold>
/*
 * The following data are used later to determine the pixel priority of the
 * three layers
 */
                if (pixelPrior)
                    pixelColourPriority[yLookUp + bitX] |= 0x40;
                else
                    pixelColourPriority[yLookUp + bitX] &= ~0x40;
/*
 * Sort out the wrapping round of tiles
 */
                scrollX += skipFactor;
                if (scrollX > 255)
                    scrollX -= 255;
                bitX = scrollX & 0x07;
                x += skipFactor;
                imageX += pixelSize;
            }
        }
    }
//    if ((HIGH_MEM[GameBoyCPU.LCD_CTRL]&0x20) != 0)
//        System.out.println("CL " + currentLine + " TST " +
//                            (((int) HIGH_MEM[0x14A]) &0xff));
    if (!spritesOnly && (HIGH_MEM[GameBoyCPU.LCD_CTRL] & 0x20) != 0
            && ((((int) HIGH_MEM[0x14A]) & 0xff) <= currentLine))
    {
/*
 * The windows x position is offset by 7
 */
        winX = (HIGH_MEM[GameBoyCPU.LCD_WIN_X] & 0xFF) - 7;
        winY = HIGH_MEM[GameBoyCPU.LCD_WIN_Y] & 0xFF;
        bitY = ((currentLine - winY)) & 0x07;
        bitY1 = bitY << 1;
        yTile = ((currentLine - winY)) >> 3;
        yTile5 = yTile << 5;
        for (x = 0, imageX = winX / skipFactor * pixelSize; x < (160 - winX);)
        {
            if (imageX >= 0)
            {
/*
 * Work out which tile the pixel belongs to from the 32x32 tile map
 */
                xTile = (x) >> 3;
/*
 * Work out the x,y coordinate of the tile
 */
                bitX = (x) & 0x07;
                len = 8 - bitX;
/*
 * For Gameboy Color, get the attributes of the current tile from the second
 * VRAM bank
 */
                tileProp = (((int) (VRAM[0x2000 + winPoint + (yTile5) + xTile]))
                        & 0xFF);
/*
 * Get the number of the tile the pixel belongs to
 */
                tile = (((int) (VRAM[winPoint + (yTile5) + xTile])) & 0xFF);
/*
 * The tile can be stored at two locations in memory. If it is 0x800 the tile
 * numbers are signed
 */
                if (tileArea == 0x800)
                    tile ^= 0x80;
/*
 * Gameboy Color tile data can come from either VRAM bank depending on which
 * bits are set
 */
                charBnkSlct = ((tileProp & 0x08) != 0 ? 0x2000 : 0)
                        + tileArea + bitY1 + (tile << 4);
                pixelPrior = (tileProp & 0x80) != 0 ? true : false;
                for (i = 0; i < len && x < (160 - winX); i += skipFactor)
                {
/*
 * Calculate which of the 4 positions in one of the palette arrays the pixel
 * corresponds to
 */
                    bitX = 7 - bitX;
                    colour = ((((VRAM[charBnkSlct + 1] & 0xFF) >> (bitX))
                            & 0x01)
                                    << 1)
                                            | (((VRAM[charBnkSlct] & 0xFF)
                                                    >> (bitX))
                                                            & 0x01);
                    bitX = imageX / pixelSize;
/*#/NoImage#*///<editor-fold>
//--        pixelColourPriority[yLookUp+bitX] = (colour<<3) + (tileProp&0x07);
//--/*
//-- * Something like this is needed to only repaint changed pixels. The
//-- * multiple repaints are very slow, however.
//-- */
//--//                   if (pixelColour[yLookUp+bitX] != (byte) colour)
//--//                   {
//--//                       pixelColour[yLookUp+bitX] = (byte) colour;
//--//                       repaint(imageX, currentLine/skipFactor,
//--//                                         pixelSize, pixelSize);
//--//                   }
//--
/*$NoImage$*///</editor-fold>
/*#/!NoImage#*///<editor-fold>
                    if ((pixelColourPriority[yLookUp + bitX] & 0xbf)
                            != ((colour << 3) + (tileProp & 0x7)))
                    {
                        screenGraphics.setColor(
                                backPal[colour][tileProp & 0x07]);
                        screenGraphics.fillRect(imageX, imageY, pixelSize,
                                pixelSize);
                        pixelColourPriority[yLookUp + bitX] = (byte) ((colour
                                << 3)
                                        + (tileProp & 0x7));
//    System.out.println("WN (" + imageX + "," + (currentLine/skipFactor)
//                        + "=" + (yLookUp + imageX) +")"
//                        +Integer.toHexString(0xFF &
//                               pixelColourPriority[yLookUp+bitX]));
                    }
/*$!NoImage$*///</editor-fold>
/*
 * The following information is used later to determine the pixel priority of
 * the three layers
 */
                    if (pixelPrior)
                        pixelColourPriority[yLookUp + bitX] |= 0x40;
                    else
                        pixelColourPriority[yLookUp + bitX] &= ~0x40;
                    x += skipFactor;
                    imageX += pixelSize;
                    bitX = (x) & 0x07;
                }
            }
            else   
            {
                x += skipFactor;
                imageX += pixelSize;
            }
        }
    }
    if ((HIGH_MEM[GameBoyCPU.LCD_CTRL] & 0x02) != 0)
    {
/*
 * Find the sprite size, 8*8 or 8*16
 */
        sprSize = ((HIGH_MEM[GameBoyCPU.LCD_CTRL] & 0x04) != 0 ? 15 : 7);
        for (currentSpr = 156; currentSpr >= 0; currentSpr -= 4)
        {
/*
 * Gets the Y position of the sprite (sprites are offset by 16 on the y axis)
 */
            sprY = (((int) HIGH_MEM[(currentSpr)]) & 0xFF) - 16;
/*
 * Is any part of the sprite on the current line?
 */
            if (sprY <= currentLine && (sprY + sprSize) >= currentLine)
            {
/*
 * Increment sprPerLine for each sprite found on this line.
 *
 * This value should be used for timing purposes in the virtual CPU (but it
 * isn't ...).
 */
                sprPerLine++;
/*
 * Get the Y coordinate of the line of the sprite we're working on
 */
                sprY = currentLine - sprY;
/*
 * Get the X position of the sprite (sprites are offset by 8 on the x axis)
 */
                sprX = ((int) (HIGH_MEM[(1 + currentSpr)]) & 0xFF) - 8;
/*
 * Get the tile used by the sprite
 */
                tile = ((int) HIGH_MEM[(2 + currentSpr)]) & 0xFF;
                tile4 = tile << 4;
/*
 * Get the sprite flags byte
 */
                sprF = ((int) HIGH_MEM[(3 + currentSpr)]) & 0xFF;
/*
 * Flip the sprite on the Y-axis if specified by the flags
 */
                if ((sprF & 0x40) != 0)
                    sprY = sprSize - sprY;
/*
 * Gameboy Color tile data can come from either VRAM bank depending on which
 * bits are set
 */
                charBnkSlct = (((sprF & 0x08) != 0) ? 0x2000 : 0) + tile4
                        + (sprY << 1);
                for (x = 0; x < 8; x++)
                {
/*
 * Get the actual X-coordinate screen position of the sprite pixel taking
 * X-axis flipping into account.
 */
                    tx = (((sprF & 0x20) != 0)
                            ? (sprX + (7 - x))
                            : (sprX + x));
                    if ((tx & (skipFactor - 1)) != 0 || tx < 0 || tx >= 160)
                        continue;
                    bitX = tx / skipFactor;
                    imageX = (bitX * pixelSize);
/*
 * Decide if the sprite pixel should be displayed or not.
 */
                    pcp = pixelColourPriority[yLookUp + bitX];
                    if (((pcp & 0x40) == 0
                            || (pcp & 0x40) != 0 && (pcp & 0x38) == 0)
                                    && ((sprF & 0x80) == 0
                                            || ((sprF & 0x80) != 0
                                                    && (pcp & 0x38) == 0)))
                    {
/*
 * Calculate which of the 4 positions in one of the palette arrays the pixel
 * corresponds to
 */
                        colour = (((((VRAM[charBnkSlct + 1] & 0xFF))
                                >> (7 - x))
                                        & 0x01)
                                                << 1)
                                                        | (((VRAM[charBnkSlct]
                                                                & 0xFF)
                                                            >> (7 - x)) & 0x01);
/*
 * Colour 0 is always transparent on sprites so that they don't appear as a
 * character in a box
 */
                        if (colour != 0)
                        {
/*#NoImage#*///<editor-fold>
//--// If repainting individually, do this.
//--//                              repaint(imageX,
//--//                                         currentLine/skipFactor,1,1);
/*$NoImage$*///</editor-fold>
/*#!NoImage#*///<editor-fold>
/*
 * I suspect that the test below always succeeds, since I have never seen the
 * sprites refreshed without the background also being drawn.
 */
                            if ((pcp & 0xbf)
                                    != (0x20 + (colour << 3)
                                    + ((LOW_MEM[0x143] & 0x80) != 0
                                            ? sprF & 0x07
                                            : ((sprF & 0x10) != 0 ? 1 : 0))))
                            {
                                pcp = ((LOW_MEM[0x143] & 0x80) != 0
                                        ? sprF & 0x07
                                        : ((sprF & 0x10) != 0 ? 1 : 0));
                                screenGraphics.setColor(forePal[colour][pcp]);
                                screenGraphics.fillRect(imageX, imageY, 
                                        pixelSize, pixelSize);
                                pixelColourPriority[yLookUp + bitX] = (byte)
                                  (0x20 + (colour << 3) + pcp);
//    System.out.println("SP (" + imageX + "," + (currentLine/skipFactor)
//                              + "=" + (yLookUp + bitX) +")"
//                              +Integer.toHexString(0xFF &
//                               pixelColourPriority[yLookUp+bitX]));
                            }
/*$!NoImage$*///</editor-fold>
                        }
                    }
                }
            }
        }
    }
//    if (currentLine == 0 || mods > 0)
//            System.out.println("Ln:" + currentLine + " mods: " + mods +
//              " spr " + sprPerLine);
    return sprPerLine > 10 ? 10 : sprPerLine;
}
 
/**

   Gets the specified background colour from the background palette array

   @return A color from the background palette

 **/

public
/*#!J2SE#*///<editor-fold>
//--  int
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
        Color
/*$J2SE$*///</editor-fold>
       getBackColour(int loc, int pal)
{
    return backPal[loc][pal];
}

/**

  Gets the specified foreground colour from the foreground palette array

  @return A Color from the foreground palette

 **/

public
/*#!J2SE#*///<editor-fold>
//--  int
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
        Color
/*$J2SE$*///</editor-fold>
    getForeColour(int loc, int pal)
{
    return forePal[loc][pal];
}

/**

   Invalidates colours so they get re-drawn. The invalidation for
   foreground colours should be quicker if the loop only looked at sprite
   locations.

   @param loc the color number between 0-3
   @param pal the palette number between 0-7
   @param fore This is the foreground (or not)

 **/

private void invalidateColour(int loc, int pal, boolean fore)
{
    if (pixelColourPriority == null)
        return;

    int key = loc << 3 + pal + (fore ? 0x20 : 0);
    for (int i = 0; i < pixelColourPriority.length; i++)
    {
        if ((pixelColourPriority[i] & 0xbf) == key)
            pixelColourPriority[i] = (byte) 0x80;
    }
}

/**

 Adds a color to the specified location in the background palette array

 @param loc the color number between 0-3
 @param pal the palette number between 0-7
 @param c the specified Color

 **/
public void setBackColour(int loc, int pal,
/*#!J2SE#*///<editor-fold>
//--  int
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
        Color
/*$J2SE$*///</editor-fold>
              c)
{
    if (backPal[loc][pal] != c)
    {
        backPal[loc][pal] = c;
        invalidateColour(loc, pal, false);
    }
}

/**

 Adds a Color to the specified location in the foreground palette array

 @param loc the color number between 0-3
 @param pal the palette number between 0-7
 @param c the specified Color

 **/

public void setForeColour(int loc, int pal,
/*#!J2SE#*///<editor-fold>
//--  int
/*$J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
        Color
/*$J2SE$*///</editor-fold>
          c)
{
    if (forePal[loc][pal] != c)
    {
        forePal[loc][pal] = c;
        //    invalidateColour(loc, pal, true);
    }
}

/**

 Sets the position in Vram from which the background tile pointers come from

 @param p the start of the background tile pointers data area

 **/

public void setBGPointers(int p)
{
    bgPoint = p;
}

/**

 Sets the position in Vram from which the window tile pointers come from

 @param p the start of the window tile pointers data area

 **/

public void setWinPointers(int p)
{
    winPoint = p;
}

/**

 Sets the position in Vram from which the tile data comes from

 @param p the start of the tile data area

 **/

public void setTileArea(int p)
{
    tileArea = p;
}
}
