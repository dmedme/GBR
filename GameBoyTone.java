/*#J2SE#*///<editor-fold>
//--import java.io.File;
//--import java.io.FileOutputStream;
//--import java.io.BufferedOutputStream;
//--import javax.sound.midi.Instrument;
//--import javax.sound.midi.MidiSystem;
//--import javax.sound.midi.Patch;
//--import javax.sound.midi.Sequence;
//--import javax.sound.midi.MidiDevice.Info;
//--import javax.sound.sampled.MidiMessage;
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.Control;
import javax.microedition.media.control.ToneControl;
import java.io.ByteArrayInputStream;
/*$!J2SE$*///</editor-fold>

/*****************************************************************************
 * GameBoyTone - Emulation of the sound capabilities of the GameBoy and
 * GameBoy Color.
 *
 * This implementation attempts to consume minimal system resources.
 */
class GameBoyTone implements Runnable, PlayerListener,GameBoyAudible {
private byte [] m_highMem;
/*
 * J2ME doesn't include java.lang.Math, but this implementation is good to 5
 * significant figures.
 *
 * Note that CLDC 1.0 doesn't provide ANY floating point support; preverify
 * fails in if we have floating point values. So we use integers, and multiply
 * by 10000.
 */
private static final int LN2I = 6931;
static int ln10000(int d)
{
int to_do;
int i;
int scale;

    if (d <= 1 )
        return 0;
    for (i = d, scale = 0; i > 1; i >>>= 1, scale++);
    d = (d * 10000) / (1 << scale);
    if (d >= 20000)
        to_do =  -10000; 
    else
        to_do = d - 10000;
    int ret = to_do;
    int pw = to_do; 
    boolean toggle = true;
    for (i = 2; i < 10; i++)
    {
        pw = pw * to_do/10000;
        if (toggle)
        {
            toggle = false;
            ret -= pw/i;
        }
        else
        {
            toggle = true;
            ret += pw/i;
        } 
    }
    if (d >= 20000)
        return -ret + scale * LN2I ;
    else
        return ret + scale * LN2I ;
}
/*
 * Convert frequency to tone number
 */
private byte tone(int hzFreq )
{
int note =(((ln10000((hzFreq * 1000)/8176)/10000)* 173123)/10000);

    if (note < 0)
        return ToneControl.SILENCE;
    else
    while (note > 127)
        note >>>= 1;
    return (byte) note;
}
/**

  Output SOUND 1 data (Quadrangular wave with sweep and envelope)

  At the end of the routine, the high memory locations need to be updated in
  the light of what has happened.

**/
private byte generateSound1()
{
    if ((m_highMem[GameBoyCPU.SND_STEREO] & 0x11) == 0)
        return ToneControl.SILENCE;

    int overallLength;
    if ((m_highMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER] & 0x40) == 0)
        overallLength = -1;
    else
    {
        if ((m_highMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER] & 0x80) != 0)
        {
            m_highMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER] &= 0x7F;
            m_highMem[GameBoyCPU.SND_STAT] |= 1;
        }
        else
        if ((m_highMem[GameBoyCPU.SND_STAT] & 1) == 0)
            return ToneControl.SILENCE;
        overallLength = 64 - (m_highMem[GameBoyCPU.SND_1_WAV_LEN] & 0x3F);
    }

    int freq11 = ((int) (((int)
            m_highMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER]) & 0x07) << 8)
            + ((int) m_highMem[GameBoyCPU.SND_1_FREQ_KICK_LOWER]);
    int hzFreq = 131072 / (2048 - freq11);
    int volume = (((m_highMem[GameBoyCPU.SND_1_ENV] & 0xF0) >>> 4));
    if (volume == 0)
        return ToneControl.SILENCE;
    if (overallLength > 0)
    {
        overallLength--;
        m_highMem[GameBoyCPU.SND_1_WAV_LEN] = (byte)
                 ((((int) m_highMem[GameBoyCPU.SND_1_WAV_LEN]) & 0xC0)
               | ((64 - overallLength) & 0x3F));
    }
    return tone(hzFreq);
}
/**

  Output SOUND 2 data (Quadrangular wave with envelope)

  At the end of the routine, the high memory locations need to be updated in
  the light of what has happened.

 **/
private byte generateSound2()
{
    if ((m_highMem[GameBoyCPU.SND_STEREO] & 0x22) == 0)
        return ToneControl.SILENCE;

    int overallLength;
    if ((m_highMem[GameBoyCPU.SND_2_FREQ_KICK_UPPER] & 0x40) == 0)
        overallLength = -1;
    else
    {
        if ((m_highMem[GameBoyCPU.SND_2_FREQ_KICK_UPPER] & 0x80) != 0)
        {
            m_highMem[GameBoyCPU.SND_2_FREQ_KICK_UPPER] &= 0x7F;
            m_highMem[GameBoyCPU.SND_STAT] |= 2;
        }
        else
        if ((m_highMem[GameBoyCPU.SND_STAT] & 2) == 0)
            return ToneControl.SILENCE;
        overallLength = 64 - (m_highMem[GameBoyCPU.SND_2_WAV_LEN] & 0x03F);
    }

    int freq11 = ((int) (((int)
            m_highMem[GameBoyCPU.SND_2_FREQ_KICK_UPPER]) & 0x07) << 8)
            + ((int) m_highMem[GameBoyCPU.SND_2_FREQ_KICK_LOWER]);
    int hzFreq = 131072 / (2048 - freq11);
    int volume = (((m_highMem[GameBoyCPU.SND_2_ENV] & 0xF0) >>> 4));
    if (volume == 0)
        return ToneControl.SILENCE;
    if (overallLength > 0)
    {
        overallLength--;
        m_highMem[GameBoyCPU.SND_2_WAV_LEN] = (byte)
                    ((((int) m_highMem[GameBoyCPU.SND_2_WAV_LEN]) & 0xC0)
                  | ((64 - overallLength) & 0x3F));
    }
    if (volume != 0)
        m_highMem[GameBoyCPU.SND_2_ENV] = (byte) (((byte)
                (m_highMem[GameBoyCPU.SND_2_ENV] & 0x0F)) | ((byte)
                       (volume << 4)));
    return tone(hzFreq);
}
/**

  Output SOUND 3 data (Wave memory pattern applied to frequency)

  At the end of the routine, the high memory locations need to be updated in
  the light of what has happened.

**/
private byte generateSound3()
{
    if ((m_highMem[GameBoyCPU.SND_STEREO] & 0x33) == 0
            || (m_highMem[GameBoyCPU.SND_3_ON_OFF] & 0x80) == 0)
        return ToneControl.SILENCE;

    int overallLength;
    if ((m_highMem[GameBoyCPU.SND_3_FREQ_KICK_UPPER] & 0x40) == 0)
        overallLength = -1;
    else
    {
        if ((m_highMem[GameBoyCPU.SND_3_FREQ_KICK_UPPER] & 0x80) != 0)
        {
            m_highMem[GameBoyCPU.SND_3_FREQ_KICK_UPPER] &= 0x7F;
            m_highMem[GameBoyCPU.SND_STAT] |= 4;
        }
        else
        if ((m_highMem[GameBoyCPU.SND_STAT] & 4) == 0)
            return ToneControl.SILENCE;
        overallLength = 256 -((int) m_highMem[GameBoyCPU.SND_3_LEN] & 0xFF);
    }

    int volumeFactor =(((m_highMem[GameBoyCPU.SND_3_VOLUME]) & 0x60) >>> 5);
/*
 * Volume factor 0 == mute, so shift 4 to clear.
 */
    volumeFactor = (volumeFactor == 0) ? 4 : (volumeFactor - 1);

    int freq11 = ((int) (((int)
            m_highMem[GameBoyCPU.SND_3_FREQ_KICK_UPPER]) & 0x07) << 8)
            + ((int) m_highMem[GameBoyCPU.SND_3_FREQ_KICK_LOWER]);
    int hzFreq = 131072 / (2048 - freq11);

    int volume = (((m_highMem[GameBoyCPU.SND_BNK_10] &0x0F)) >>> volumeFactor);
    if (volume == 0)
        return ToneControl.SILENCE;
    if (overallLength > 0)
    {
        overallLength--;
        m_highMem[GameBoyCPU.SND_3_LEN] = (byte) (256 - overallLength);
    }
    return tone(hzFreq);
}

/**

  Output SOUND 4 data (White Noise)

**/
private byte generateSound4()
{
    if ((m_highMem[GameBoyCPU.SND_STEREO] & 0x44) == 0)
        return ToneControl.SILENCE;
    int overallLength;
    if ((m_highMem[GameBoyCPU.SND_4_POLY_KICK_UPPER] & 0x40) == 0)
        overallLength = -1;
    else
    {
        if ((m_highMem[GameBoyCPU.SND_4_POLY_KICK_UPPER] & 0x80) != 0)
        {
            m_highMem[GameBoyCPU.SND_4_POLY_KICK_UPPER] &= 0x7F;
            m_highMem[GameBoyCPU.SND_STAT] |= 8;
        }
        else
        if ((m_highMem[GameBoyCPU.SND_STAT] & 8) == 0)
            return ToneControl.SILENCE;
        overallLength = 64 - (m_highMem[GameBoyCPU.SND_4_LEN] & 0x3F);
    }
    int polyClock = (((int)
            m_highMem[GameBoyCPU.SND_4_POLY_KICK_LOWER]) & 0xF0) >>> 4;
    int polyLength = (((m_highMem[GameBoyCPU.SND_4_POLY_KICK_LOWER]) & 0x8)
            != 0) ? 15 : 7;
    int polyFreq = ((m_highMem[GameBoyCPU.SND_4_POLY_KICK_LOWER]) & 0x07);
    boolean incFlag = ((m_highMem[GameBoyCPU.SND_4_ENV] & 0x8) != 0);
    int volume = (((m_highMem[GameBoyCPU.SND_4_ENV] & 0xF0) >>> 4));
    if (volume == 0)
        return ToneControl.SILENCE;
    int hzFreq = (polyFreq * polyClock + polyLength) % 4093;

    if (overallLength > 0)
    {
        overallLength--;
        m_highMem[GameBoyCPU.SND_4_LEN] = (byte) ((((int)
                          m_highMem[GameBoyCPU.SND_4_LEN]) & 0xC0)
                        | ((64 - overallLength) & 0x3F));
    }
    m_highMem[GameBoyCPU.SND_4_POLY_KICK_LOWER] = (byte) hzFreq;
    return tone(hzFreq);
}
/**

  Constructor. Note the high memory, and initialiase the sound system. Begin
  the sound output loop.

 **/
private Player m_player;
private ToneControl m_toneControl;
public GameBoyTone(byte[] highMem)
{
    m_highMem = highMem;
    try
    {
        m_player = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
        m_player.realize();
        Control [] cs = m_player.getControls();
        for (int i = 0; i < cs.length; i++)
             System.out.println("Control " + i + " = " + cs[i]);
        m_player.addPlayerListener(this);
        m_toneControl = (ToneControl) m_player.getControl("ToneControl");
        if (m_toneControl == null)
        {
            System.out.println("Unable to obtain ToneControl...");
            throw new Throwable();
        }
    }
    catch (Throwable e)
    {
        e.printStackTrace();
        System.out.println("Initialisation failed..." + e);
    }
    startThread();
}
private byte [] m_sequence;
public void outputSound(int msecsNeeded)
{
    if (((int) m_highMem[GameBoyCPU.SND_STAT] & 0x80) == 0
            || m_highMem[GameBoyCPU.SND_STEREO] == 0
// Always true!?  || (m_highMem[GameBoyCPU.SND_VOICE_INP] & 0x88) == 0
            )
        return;
    if (m_toneControl == null)
    {
        byte note1, note2, note3, note4;

        note2 = generateSound2();
        note1 = generateSound1();
        note3 = generateSound3();
        note4 = generateSound4();
//        System.out.println("Why is m_toneControl null?");
        if ((note1 != ToneControl.SILENCE)
         || (note2 != ToneControl.SILENCE)
         || (note3 != ToneControl.SILENCE)
         || (note4 != ToneControl.SILENCE))
        {
             msecsNeeded >>>= 2;
             try
             {
                 Manager.playTone(note4, msecsNeeded, 100);
                 Manager.playTone(note3, msecsNeeded, 100);
                 Manager.playTone(note1, msecsNeeded, 100);
                 Manager.playTone(note2, msecsNeeded, 100);
             }
             catch (Exception e)
             {
                 e.printStackTrace();
                 System.err.println("Problem " + e + " playing notes in outputSound()");
/*
 * It doesn't seem to recover from this, so give up on sound
 */
                 m_highMem[GameBoyCPU.SND_STAT] &= (byte) (~0x80);
                 m_highMem[GameBoyCPU.SND_STEREO] = 0;
             }
        }
        else
        {
            try
            {
                Thread.sleep(msecsNeeded);                
            }
            catch (Exception e)
            {
            }
        }
        return;
    }
    int tempo = 250 * 60/msecsNeeded;  // because tempo is beats per minute/4
    tempo = (tempo < 5) ? 5 : ((tempo > 127) ? 127 : tempo);
    m_sequence = new byte[38];
    m_sequence[0] = ToneControl.VERSION;
    m_sequence[1] = 1;
    m_sequence[2] = ToneControl.TEMPO;
    m_sequence[3] = (byte) tempo;   // Chosen so that the sequence lasts the msecsNeeded
    m_sequence[4] = ToneControl.RESOLUTION;
    m_sequence[5] = 4;   // Resolution is in quarters. Each voice gets 1/4
    m_sequence[6] = ToneControl.BLOCK_START;
    m_sequence[7] = 0;    // starting Sound1 part
    m_sequence[8] = generateSound1();
    m_sequence[9] = 1;   // Duration; 1/4 of the note
    m_sequence[10] = ToneControl.BLOCK_END; 
    m_sequence[11] = 0;   // ending Sound1 part
    m_sequence[12] = ToneControl.BLOCK_START;
    m_sequence[13] = 1;    // starting Sound2 part
    m_sequence[14] = generateSound2();
    m_sequence[15] = 1;   // Duration
    m_sequence[16] = ToneControl.BLOCK_END; 
    m_sequence[17] = 1;   // ending Sound2 part
    m_sequence[18] = ToneControl.BLOCK_START;
    m_sequence[19] = 2;    // starting Sound3 part
    m_sequence[20] = generateSound3();
    m_sequence[21] = 1;   // Duration
    m_sequence[22] = ToneControl.BLOCK_END; 
    m_sequence[23] = 2;   // ending Sound3 part
    m_sequence[24] = ToneControl.BLOCK_START;
    m_sequence[25] = 3;    // starting Sound4 part
    m_sequence[26] = generateSound4();
    m_sequence[27] = 1;   // Duration
    m_sequence[28] = ToneControl.BLOCK_END; 
    m_sequence[29] = 3;   // ending Sound4 part
    m_sequence[30] = ToneControl.PLAY_BLOCK;
    m_sequence[31] = 3;  // playing Sound1 part
    m_sequence[32] = ToneControl.PLAY_BLOCK;
    m_sequence[33] = 2;  // playing Sound2 part
    m_sequence[34] = ToneControl.PLAY_BLOCK;
    m_sequence[35] = 0;  // playing Sound3 part
    m_sequence[36] = ToneControl.PLAY_BLOCK;
    m_sequence[37] =  1;  // playing Sound4 part
    m_toneControl.setSequence(m_sequence);
    try
    {
        m_player.start();
    }
    catch (Exception e)
    {
        e.printStackTrace();
        System.err.println("Problem " + e + " in outputSound(): player state " +
                m_player.getState());
//        m_highMem[GameBoyCPU.SND_STAT] &= (byte) (~0x80);
//        m_highMem[GameBoyCPU.SND_STEREO] = 0;
    }
    return;
}
public void playerUpdate(Player pl, String str, Object o)
{
//    System.out.println("Player " + pl + " State " + pl.getState() + " String "
//                + str + " Object " + o);
    if (str.equals("endOfMedia") && m_running)
    {
        m_player.deallocate();
        outputSound(200);
    }
}
private Thread m_thread;
private boolean m_running;
private void startThread()
{
    if (m_thread == null)
    {
        m_running = true;
        m_thread = new Thread(this);
        m_thread.start();
    }
}


/**

  Called to signal the thread to stop

**/
public void stopThread()
{
    m_running = false;
    m_thread = null;
}

/**

  Called when the thread starts

**/
public void run()
{
    m_thread.setPriority(Thread.MIN_PRIORITY);

    try
    {
        while (m_running)
        {
/*#J2SE#*///<editor-fold>
//--            togo = m_buffer.length - m_soundLine.available();
//--            if (togo < bytesNeeded)
//--                outputSound(64);
//--            else
//--                Thread.sleep(44);
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
        int state = m_player.getState();
        if (state == Player.REALIZED)
            outputSound(200);
        else
        if (state == Player.PREFETCHED)
            m_player.start();
//        else
//            System.out.println("Thread loop finds state " + state);
        Thread.sleep(250);
/*$!J2SE$*///</editor-fold>
        }
    }
    catch (Throwable e)
    {
        e.printStackTrace();
        System.err.println("Problem making sound; Player: " + m_player + " State: " +
                        m_player.getState() + " ToneControl: " +
                          m_toneControl + " Sequence: " + m_sequence);
/*
 * J2SE 1.4 required ...
 *
 * StackTraceElement stack[] = e.getStackTrace();
 *  
 *  stack[0] contains the method that created the exception.
 *  stack[stack.length-1] contains the oldest method call.
 *  Enumerate each stack element.
 *
 *     for (int i=0; i<stack.length; i++)
 *     {
 *         String filename = stack[i].getFileName();
 *         if (filename == null) {
 *             // The source filename is not available
 *         }
 *         String className = stack[i].getClassName();
 *         String methodName = stack[i].getMethodName();
 *         boolean isNativeMethod = stack[i].isNativeMethod();
 *         int line = stack[i].getLineNumber();
 *         System.out.println(className+"."+methodName+ " line " + line);
 *     }
 */
    }
}
}
