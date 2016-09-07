/*#J2SE#*///<editor-fold>
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.Arrays;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
//--import javax.microedition.media.Manager;
//--import javax.microedition.media.Player;
//--import javax.microedition.media.Control;
//--import java.io.ByteArrayInputStream;
/*$!J2SE$*///</editor-fold>

/*****************************************************************************
 * GameBoySound - Emulation of the sound capabilities of the GameBoy and
 * GameBoy Color by generating samples.
 *
 * This implementation attempts to follow GameBoy Sound Document version 1.2
 *
 * The sound is almost recognisable if the sample rate is 44100 Hz.
 * It is not so good at 8192 Hz, but the compute load, of course, is hugely
 * reduced.
 */
class GameBoySound implements Runnable, GameBoyAudible {

/*#J2SE#*///<editor-fold>
private   SourceDataLine  mSoundLine;
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
//--private Player  mSoundLine;
/*$!J2SE$*///</editor-fold>
private int mSampleRate =
/*#J2SE#*///<editor-fold>
          24000
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
//--           8192
/*$!J2SE$*///</editor-fold>
;   //  8192 is recognisable. No attempt at Hi-Fi
/*
 * Sound System time intervals and phase positions, measured in samples
 */
private int mSweepPhase;
private int mOverallCycle = mSampleRate >>> 8;
private int mSnd1OverallPhase;
private int mSnd2OverallPhase;
private int mSnd3OverallPhase;
private int mSnd4OverallPhase;
private int mSnd1EnvelopePhase;
private int mSnd1DutyPhase;
private int mSnd1DutyStep;
private int mSnd2EnvelopePhase;
private int mSnd2DutyPhase;
private int mSnd2DutyStep;
private int mSnd3DutyPhase;
private int mSnd3DutyStep;
private int mSnd4EnvelopePhase;
private int mBufferMSec = 64;    //    Add in medium size pieces.
private byte[] mHighMem;
private byte[] mBuffer;
private static final int mHeadLen =
/*#!J2SE#*///<editor-fold>
//--        44
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
        0
/*$J2SE$*///</editor-fold>
        ;
private boolean mSnd1HzToggle;
private int mSnd1HzPhase;
private boolean mSnd2HzToggle;
private int mSnd2HzPhase;
private boolean mSnd3HzToggle;
private int mSnd3HzPhase;
private boolean mSnd4HzToggle;
private int mSnd4HzPhase;
/*
 *  Time units.
 *  -   Overall Length is in 1/256 seconds; maximum 63, or continuous
 *      -   With a 44100 sample rate, maximum length would be 10853 frames
 *          of 1 byte each per channel (8 bit stereo PCM)
 *      -   With a 30 frame/second refresh rate, samples per video frame would
 *          be 1470
 *      -   About 8 1/2 time units per video frame.
 *      -   About 172 samples per time unit
 *  -   Sweep time is in 1/128 of a second. So 2 length units per sweep unit
 *  -   Envelope time is in 1/64 of a second. So 4 length units per envelope
 *      step unit. Envelope step length is multiple of this, 0 - 7.
 *
 *  The question is, how do we get the sound pumped out at a constant rate. The
 *  real game boy may run these bits in parallel, but this program won't.
 *
 *  Each variant is characterised by:
 *  -   The number of frequency toggles (sign reversals) for the basic step
 *      -   This is the Cycle variable.
 *      -   Position in the Cycle is the Phase variable
 *  -   The number of variant steps in the variant cycle
 *      -   This is the Length variable
 *      -   Position in the Length is the Step variable
 *  -   The variable families are as follows.
 *      -   hz for the base frequency (hzHalfCycle, hzPhase)
 *      -   overall for the overall duration (overallCycle, OverallPhase,
 *          overallLength, which counts down)
 *      -   duty for the Duty Cycle. By experiment (listening to the output)
 *          we use a half frequency cycle as the duty length, and compute the
 *          phase within it. We use 8 times (<< 3) for Sound1 and Sound2,
 *          and 32 times (<< 5) for Sound 3.
 *      -   sweep for the Sweep Cycle. (sweepCycle, sweepPhase). Additionally,
 *          there is a sweep factor, for the changes in frequency.
 *      -   envelope for the Envelope Cycle. (envelopeCycle, EnvelopePhase)
 *
 * The following (less efficient) code represents what the skips on Sound1
 * and Sound2 are trying to achieve.
 *
 */
//private static final boolean [][] dutyFactor =
//{{false, true, false, false, false, false, false, false},
//{true,true,false,false,false,false,false,false},
//{true,true,true,true,false,false,false,false},
//{false,false,true,true,true,true,true,true}};
///**
//
//  Look up whether or not we should be silent.
//
// **/
//private byte dutyPCM(int hzPhase, int hzHalfCycle,
//                       int volume, boolean toggle, int dutyPattern)
//{
//    hzPhase = (hzPhase << 3)/hzHalfCycle;
//    if (dutyFactor[dutyPattern][hzPhase])
//        return (byte) (toggle ? volume : -volume);
//    else
//        return 0;
//}
///**
//
//
//  Pick the correct wave pattern.
//
// **/
//private byte wavePCM(int hzPhase, int hzHalfCycle,
//                        boolean toggle, int volumeFactor)
//{
//    hzPhase = (hzPhase << 5)/hzHalfCycle;
//
//    if ((hzPhase & 1) != 0)
//        return (byte) (toggle
//                ?   (((mHighMem[GameBoyCPU.SND_BNK_10 +
//                         (hzPhase >>> 1)] &0x0F)) >>> volumeFactor)
//                :  -(((mHighMem[GameBoyCPU.SND_BNK_10 +
//                         (hzPhase >>> 1)] &0x0F)) >>> volumeFactor));
//    else
//        return (byte) (toggle
//                ?   ((((mHighMem[GameBoyCPU.SND_BNK_10
//                          + (hzPhase >>> 1)] & 0xF0) >>> 4)) >>> volumeFactor)
//                :   -((((mHighMem[GameBoyCPU.SND_BNK_10
//                          + (hzPhase >>> 1)] &0xF0) >>> 4)) >>> volumeFactor));
//}

/**

  Output SOUND 1 data (Quadrangular wave with sweep and envelope)

  At the end of the routine, the high memory locations need to be updated in
  the light of what has happened.

**/
private void generateSound1(byte[] b, int length)
{
    if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x11) == 0)
        return;

    int overallLength;
    if ((mHighMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER] & 0x40) == 0)
        overallLength = -1;
    else
    {
        if ((mHighMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER] & 0x80) != 0)
        {
            mHighMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER] &= 0x7F;
            mHighMem[GameBoyCPU.SND_STAT] |= 1;
            mSnd1EnvelopePhase = 0;
            mSnd1DutyStep = 0;
            mSnd1DutyPhase = 0;
            mSweepPhase = 0;
            mSnd1OverallPhase = 0;
        }
        else
        if ((mHighMem[GameBoyCPU.SND_STAT] & 1) == 0)
            return;
        overallLength = 64 - (mHighMem[GameBoyCPU.SND_1_WAV_LEN] & 0x3F);
    }

    int freq11 = ((int) (((int)
            mHighMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER]) & 0x07) << 8)
            + ((int) mHighMem[GameBoyCPU.SND_1_FREQ_KICK_LOWER]);
    int hzFreq = 131072 / (2048 - freq11);
    int volume = (((mHighMem[GameBoyCPU.SND_1_ENV] & 0xF0) >>> 4));

    while (hzFreq > (mSampleRate >> 1))
        hzFreq >>>= 1; /* Anti-aliasing - octave shifts */
    int sweepCycle = (((((mHighMem[GameBoyCPU.SND_1_ENT]) & 0x70) >>> 4)
            + 1) >>> 1) * (mOverallCycle + mOverallCycle);
    int sweepFactor = ((mHighMem[GameBoyCPU.SND_1_ENT]) & 0x07);
    boolean decFlag = (((mHighMem[GameBoyCPU.SND_1_ENT]) & 0x08) != 0);
    int envelopeCycle = (mHighMem[GameBoyCPU.SND_1_ENV] & 0x7)
                             * (mOverallCycle << 2);
    boolean incFlag = ((mHighMem[GameBoyCPU.SND_1_ENV] & 0x8) != 0);
    int hzHalfCycle = (mSampleRate / hzFreq) >>> 1;
    if (hzHalfCycle < 1)
        hzHalfCycle = 1;
/*
 * Code to avoid looping through zero values when adjusting bytes
 */
    int dutyPattern = (mHighMem[GameBoyCPU.SND_1_WAV_LEN] & 0xC0) >>> 6;
    int dutyLength = 8;
    int skip = 0;
    if (dutyPattern == 0)
    {
        if (mSnd1DutyStep == 0)
        {
            skip = (hzHalfCycle >>> 3) -  mSnd1HzPhase;
            if (skip <= 0)
                skip = 0;
            else
            {
                mSnd1HzPhase += skip;
                mSnd1EnvelopePhase += skip;
                mSweepPhase += skip;
                mSnd1DutyStep = 1;
                mSnd1DutyPhase = 0;
            }
        }
        else
        if (mSnd1DutyStep != 1)
        {
            skip = hzHalfCycle + (hzHalfCycle >>> 3) -  mSnd1HzPhase;
            if (skip <= 0)
                skip = 0;
            else
            {
                mSnd1HzToggle = mSnd1HzToggle ? false : true;
                mSnd1HzPhase = (hzHalfCycle >>> 3);
                mSnd1EnvelopePhase += skip;
                mSweepPhase += skip;
                mSnd1DutyStep = 1;
                mSnd1DutyPhase = 0;
            }
        }
    }
    else
    if (dutyPattern != 3
      && ((mSnd1DutyStep > 1 && dutyPattern == 1) 
       || (mSnd1DutyStep > 3 && dutyPattern == 2)))
    {
        skip = hzHalfCycle -  mSnd1HzPhase;
        if (skip <= 0)
            skip = 0;
        else
        {
            mSnd1HzToggle = mSnd1HzToggle ? false : true;
            mSnd1HzPhase = 0;
            mSnd1EnvelopePhase += skip;
            mSweepPhase += skip;
            mSnd1DutyStep = 0;
            mSnd1DutyPhase = 0;
        }
    }
    else
    if (dutyPattern == 3 && mSnd1DutyStep < 2)
    {
        skip = (hzHalfCycle >>> 2) -  mSnd1HzPhase;
        if (skip <= 0)
            skip = 0;
        else
        {
            mSnd1HzPhase = (hzHalfCycle >>> 2);
            mSnd1EnvelopePhase += skip;
            mSweepPhase += skip;
            mSnd1DutyStep = 2;
            mSnd1DutyPhase = 0;
        }
    }
    for (int i = mHeadLen + skip + skip; i < length;)
    {
        if (sweepCycle != 0)
        {
            mSweepPhase++;
            if (mSweepPhase >= sweepCycle)
            {
                mSweepPhase = 0;
                if (decFlag)
                {
                    if (sweepFactor != 0)
                        freq11 = freq11 - (freq11 >> sweepFactor);
                }
                else
                    freq11 = freq11 + (freq11 >> sweepFactor);
                if (freq11 >= 2048 || freq11 <= 0)
                {
                    mHighMem[GameBoyCPU.SND_STAT] &= ~1;
                    break;
                }
                mHighMem[GameBoyCPU.SND_1_FREQ_KICK_LOWER] = (byte)
                            (freq11 & 0xFF);
                mHighMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER] = (byte)
                            (((byte)
                                 ((mHighMem[GameBoyCPU.SND_1_FREQ_KICK_UPPER])
                                               & 0xF8))
                                    | ((byte) ((freq11 >>> 8) & 0x7)));
                hzFreq = 131072 / (2048 - freq11);
                while (hzFreq > (mSampleRate >> 1))
                    hzFreq >>>= 1; /* Anti-aliasing - octave shifts */
                mOverallCycle = ((mSampleRate << 8)/hzFreq) >>> 8;
                sweepCycle = (((((mHighMem[GameBoyCPU.SND_1_ENT]) & 0x70)
                     >>> 4) + 1) >>> 1) * (mOverallCycle + mOverallCycle);
                envelopeCycle = (mHighMem[GameBoyCPU.SND_1_ENV] & 0x7)
                             * (mOverallCycle << 2);
                hzHalfCycle = (mSampleRate / hzFreq) >>> 1;
                if (hzHalfCycle < 1)
                    hzHalfCycle = 1;
                mSnd1HzPhase = 0;
            }
        }
        if (envelopeCycle != 0)
        {
            mSnd1EnvelopePhase++;
            if (mSnd1EnvelopePhase >= envelopeCycle)
            {
                if (!incFlag)
                {
                    if (volume > 0)
                        volume--;
                }
                else
                if (volume < 15)
                    volume++;
                mHighMem[GameBoyCPU.SND_1_ENV] =  (byte) (
                           (mHighMem[GameBoyCPU.SND_1_ENV] & 0x0F)
                        | (((volume << 4) & 0xF0)));
                mSnd1EnvelopePhase = 0;
            }
        }
        mSnd1OverallPhase++;
        if (mSnd1OverallPhase >= mOverallCycle)
        {
            overallLength--;
            if (overallLength == 0)
            {
                mHighMem[GameBoyCPU.SND_STAT] &= ~1;
                mHighMem[GameBoyCPU.SND_1_WAV_LEN] &= ~0x3F;
                break;
            }
            mSnd1OverallPhase = 0;
        }
        mSnd1HzPhase++;
        if (mSnd1HzPhase >= hzHalfCycle)
        {
            mSnd1HzPhase = 0;
            mSnd1HzToggle = mSnd1HzToggle ? false : true;
        }
//        int pcm = dutyPCM(mSnd1HzPhase, hzHalfCycle, volume,
//                               mSnd1HzToggle, dutyPattern);
        int pcm = mSnd1HzToggle ? -volume : volume;
        if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x1) != 0)
            b[i] += pcm;
        i++;
        if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x10) != 0)
            b[i] += pcm;
        i++;
        mSnd1DutyPhase += 8;
        if (mSnd1DutyPhase >= hzHalfCycle)
        {
            mSnd1DutyPhase = 0;
            if (dutyPattern == 0)
            {
                skip = (hzHalfCycle + (hzHalfCycle >>> 3) -  mSnd1HzPhase);
                if (skip <= 0)
                    skip = 0;
                else
                {
                    mSnd1HzToggle = mSnd1HzToggle ? false : true;
                    skip = (skip < 0) ? 0: skip;
                    mSnd1HzPhase = (hzHalfCycle >>> 3);
                    mSnd1EnvelopePhase += skip;
                    mSweepPhase += skip;
                    mSnd1DutyStep = 1;
                    i += (skip + skip);
                }
            }
            else
            if (dutyPattern != 3
              && ((mSnd1DutyStep > 0 && dutyPattern == 1) 
               || (mSnd1DutyStep > 2 && dutyPattern == 2)))
            {
                skip = hzHalfCycle - mSnd1HzPhase;
                if (skip <= 0)
                    skip = 0;
                else
                {
                    mSnd1HzToggle = mSnd1HzToggle ? false : true;
                    mSnd1HzPhase = 0;
                    mSnd1EnvelopePhase += skip;
                    mSweepPhase += skip;
                    mSnd1DutyStep = 0;
                    mSnd1DutyPhase = 0;
                    i += (skip + skip);
                }
            }
            else
            if (dutyPattern == 3 && mSnd1DutyStep >= 7)
            {
                skip = (hzHalfCycle >> 2) -  mSnd1HzPhase;
                if (skip <= 0)
                    skip = 0;
                else
                {
                    mSnd1HzToggle = mSnd1HzToggle ? false : true;
                    mSnd1HzPhase = (hzHalfCycle >>> 2);
                    mSnd1EnvelopePhase += skip;
                    mSweepPhase += skip;
                    mSnd1DutyStep = 2;
                    i += (skip + skip);
                }
            }
            else
                mSnd1DutyStep++;
        }
    }
    if (overallLength > 0)
        mHighMem[GameBoyCPU.SND_1_WAV_LEN] = (byte)
                 ((((int) mHighMem[GameBoyCPU.SND_1_WAV_LEN]) & 0xC0)
               | ((64 - overallLength) & 0x3F));
}

/**

  Output SOUND 2 data (Quadrangular wave with envelope)

  At the end of the routine, the high memory locations need to be updated in
  the light of what has happened.

 **/
private void generateSound2(byte[] b, int length)
{
    if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x22) == 0)
        return;

    int overallLength;
    if ((mHighMem[GameBoyCPU.SND_2_FREQ_KICK_UPPER] & 0x40) == 0)
        overallLength = -1;
    else
    {
        if ((mHighMem[GameBoyCPU.SND_2_FREQ_KICK_UPPER] & 0x80) != 0)
        {
            mHighMem[GameBoyCPU.SND_2_FREQ_KICK_UPPER] &= 0x7F;
            mHighMem[GameBoyCPU.SND_STAT] |= 2;
            mSnd2EnvelopePhase = 0;
            mSnd2DutyStep = 0;
            mSnd2DutyPhase = 0;
            mSnd2OverallPhase = 0;
        }
        else
        if ((mHighMem[GameBoyCPU.SND_STAT] & 2) == 0)
            return;
        overallLength = 64 - (mHighMem[GameBoyCPU.SND_2_WAV_LEN] & 0x03F);
    }

    int freq11 = ((int) (((int)
            mHighMem[GameBoyCPU.SND_2_FREQ_KICK_UPPER]) & 0x07) << 8)
            + ((int) mHighMem[GameBoyCPU.SND_2_FREQ_KICK_LOWER]);
    int hzFreq = 131072 / (2048 - freq11);
    int volume = (((mHighMem[GameBoyCPU.SND_2_ENV] & 0xF0) >>> 4));

    while (hzFreq > (mSampleRate >> 1))
        hzFreq >>>= 1; /* Anti-aliasing - octave shifts */

    int envelopeCycle = (mHighMem[GameBoyCPU.SND_2_ENV] & 0x7 )
                             * (mOverallCycle << 2);
    boolean incFlag = ((mHighMem[GameBoyCPU.SND_2_ENV] & 0x8) != 0);
    int hzHalfCycle = (mSampleRate / hzFreq) >>> 1;
    if (hzHalfCycle < 1)
        hzHalfCycle = 1;
    int dutyPattern = (mHighMem[GameBoyCPU.SND_2_WAV_LEN] & 0xC0) >>> 6;
    int skip = 0;
    if (dutyPattern == 0)
    {
        if (mSnd2DutyStep == 0)
        {
            skip = (hzHalfCycle >>> 3) -  mSnd2HzPhase;
            if (skip <= 0)
                skip = 0;
            else
            {
                mSnd2HzPhase += skip;
                mSnd2EnvelopePhase += skip;
                mSnd2DutyStep = 1;
                mSnd2DutyPhase = 0;
            }
        }
        else
        if (mSnd2DutyStep != 1)
        {
            skip = hzHalfCycle + (hzHalfCycle >>> 3) -  mSnd2HzPhase;
            if (skip <= 0)
                skip = 0;
            else
            {
                mSnd2HzToggle = mSnd2HzToggle ? false : true;
                mSnd2HzPhase = (hzHalfCycle >>> 3);
                mSnd2EnvelopePhase += skip;
                mSnd2DutyStep = 1;
                mSnd2DutyPhase = 0;
            }
        }
    }
    else
    if (dutyPattern != 3
      && ((mSnd2DutyStep > 1 && dutyPattern == 1) 
       || (mSnd2DutyStep > 3 && dutyPattern == 2)))
    {
        skip = hzHalfCycle -  mSnd2HzPhase;
        if (skip <= 0)
            skip = 0;
        else
        {
            mSnd2HzToggle = mSnd2HzToggle ? false : true;
            mSnd2HzPhase = 0;
            mSnd2EnvelopePhase += skip;
            mSnd2DutyStep = 0;
            mSnd2DutyPhase = 0;
        }
    }
    else
    if (dutyPattern == 3 && mSnd2DutyStep < 2)
    {
        skip = (hzHalfCycle >>> 2) -  mSnd2HzPhase;
        if (skip <= 0)
            skip = 0;
        else
        {
            mSnd2HzPhase = (hzHalfCycle >>> 2);
            mSnd2EnvelopePhase += skip;
            mSnd2DutyStep = 2;
            mSnd2DutyPhase = 0;
        }
    }
    for (int i = mHeadLen + skip + skip; i < length;)
    {
        if (envelopeCycle != 0)
        {
            mSnd2EnvelopePhase++;
            if (mSnd2EnvelopePhase >= envelopeCycle)
            {
                if (!incFlag)
                {
                    if (volume > 0)
                        volume--;
                }
                else
                if (volume < 15)
                    volume++;
                mHighMem[GameBoyCPU.SND_2_ENV] = (byte) (
                        (mHighMem[GameBoyCPU.SND_2_ENV] & 0x0F)
                        | (((volume << 4) & 0xF0)));
                mSnd2EnvelopePhase = 0;
            }
        }
        mSnd2OverallPhase++;
        if (mSnd2OverallPhase >= mOverallCycle)
        {
            overallLength--;
            if (overallLength == 0)
            {
                mHighMem[GameBoyCPU.SND_STAT] &= ~2;
                mHighMem[GameBoyCPU.SND_2_WAV_LEN] &= (byte) 0xC0;
                break;
            }
            mSnd2OverallPhase = 0;
        }

        mSnd2HzPhase++;
        if (mSnd2HzPhase >= hzHalfCycle)
        {
            mSnd2HzPhase = 0;
            mSnd2HzToggle = mSnd2HzToggle ? false : true;
        }
        int pcm = mSnd2HzToggle ? -volume : volume;
        if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x2) != 0)
            b[i] += pcm;
        i++;
        if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x20) != 0)
            b[i] += pcm;
        i++;
        mSnd2DutyPhase += 8;
        if (mSnd2DutyPhase >= hzHalfCycle)
        {
            mSnd2DutyPhase = 0;
            if (dutyPattern == 0)
            {
                skip = (hzHalfCycle + (hzHalfCycle >>> 3) -  mSnd2HzPhase);
                if (skip <= 0)
                    skip = 0;
                else
                {
                    mSnd2HzToggle = mSnd2HzToggle ? false : true;
                    mSnd2HzPhase = (hzHalfCycle >>> 3);
                    mSnd2EnvelopePhase += skip;
                    mSnd2DutyStep = 1;
                    i += (skip + skip);
                }
            }
            else
            if (dutyPattern != 3
              && ((mSnd2DutyStep > 0 && dutyPattern == 1) 
               || (mSnd2DutyStep > 2 && dutyPattern == 2)))
            {
                skip = hzHalfCycle - mSnd2HzPhase;
                if (skip <= 0)
                    skip = 0;
                else
                {
                    mSnd2HzToggle = mSnd2HzToggle ? false : true;
                    mSnd2HzPhase = 0;
                    mSnd2EnvelopePhase += skip;
                    mSnd2DutyStep = 0;
                    mSnd2DutyPhase = 0;
                    i += (skip + skip);
                }
            }
            else
            if (dutyPattern == 3 && mSnd2DutyStep >= 7)
            {
                skip = (hzHalfCycle >> 2) -  mSnd2HzPhase;
                if (skip <= 0)
                    skip = 0;
                else
                {
                    mSnd2HzToggle = mSnd2HzToggle ? false : true;
                    mSnd2HzPhase = (hzHalfCycle >>> 2);
                    mSnd2EnvelopePhase += skip;
                    mSnd2DutyStep = 2;
                    i += (skip + skip);
                }
            }
            else
                mSnd2DutyStep++;
        }
    }
    if (overallLength > 0)
        mHighMem[GameBoyCPU.SND_2_WAV_LEN] = (byte)
                    ((((int) mHighMem[GameBoyCPU.SND_2_WAV_LEN]) & 0xC0)
                  | ((64 - overallLength) & 0x3F));
    if (volume != 0)
        mHighMem[GameBoyCPU.SND_2_ENV] = (byte) (((byte)
                (mHighMem[GameBoyCPU.SND_2_ENV] & 0x0F)) | ((byte)
                       (volume << 4)));
}

/**

  Output SOUND 3 data (Wave memory pattern applied to frequency)

  At the end of the routine, the high memory locations need to be updated in
  the light of what has happened.

**/
private void generateSound3(byte[] b, int length)
{
    if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x33) == 0
            || (mHighMem[GameBoyCPU.SND_3_ON_OFF] & 0x80) == 0)
        return;

    int overallLength;
    if ((mHighMem[GameBoyCPU.SND_3_FREQ_KICK_UPPER] & 0x40) == 0)
        overallLength = -1;
    else
    {
        if ((mHighMem[GameBoyCPU.SND_3_FREQ_KICK_UPPER] & 0x80) != 0)
        {
            mHighMem[GameBoyCPU.SND_3_FREQ_KICK_UPPER] &= 0x7F;
            mHighMem[GameBoyCPU.SND_STAT] |= 4;
            mSnd3DutyStep = 0;
            mSnd3DutyPhase = 0;
            mSnd3OverallPhase = 0;
        }
        else
        if ((mHighMem[GameBoyCPU.SND_STAT] & 4) == 0)
            return;
        overallLength = 256 -((int) mHighMem[GameBoyCPU.SND_3_LEN] & 0xFF);
    }

    int volumeFactor =(((mHighMem[GameBoyCPU.SND_3_VOLUME]) & 0x60) >>> 5);
/*
 * Volume factor 0 == mute, so shift 4 to clear.
 */
    volumeFactor = (volumeFactor == 0) ? 4 : (volumeFactor - 1);

    int freq11 = ((int) (((int)
            mHighMem[GameBoyCPU.SND_3_FREQ_KICK_UPPER]) & 0x07) << 8)
            + ((int) mHighMem[GameBoyCPU.SND_3_FREQ_KICK_LOWER]);
    int hzFreq = 131072 / (2048 - freq11);

    int hzHalfCycle = (mSampleRate / hzFreq) >>> 1;
    if (hzHalfCycle < 1)
        hzHalfCycle = 1;
    int volume;
    if ((mSnd3DutyStep & 1) != 0)
        volume = (((mHighMem[GameBoyCPU.SND_BNK_10 +
                         (mSnd3DutyStep >>> 1)] &0x0F)) >>> volumeFactor);
    else
        volume = ((((mHighMem[GameBoyCPU.SND_BNK_10
                     + (mSnd3DutyStep >>> 1)] & 0xF0) >>> 4))
                            >>> volumeFactor);

    while (hzFreq > (mSampleRate >> 1))
        hzFreq >>>= 1; /* Anti-aliasing - octave shifts */

    for (int i = mHeadLen; i < length;)
    {
        mSnd3OverallPhase++;
        if (mSnd3OverallPhase >= mOverallCycle)
        {
            overallLength--;
            if (overallLength == 0)
            {
                mHighMem[GameBoyCPU.SND_STAT] &= ~4;
                mHighMem[GameBoyCPU.SND_3_LEN] = 0;
                break;
            }
            mSnd3OverallPhase = 0;
        }

        mSnd3HzPhase++;
        if (mSnd3HzPhase >= hzHalfCycle)
        {
            mSnd3HzToggle = mSnd3HzToggle ? false : true;
            mSnd3HzPhase = 0;
        }
//        int pcm = adjustPCM(mSnd3HzPhase, hzHalfCycle, volume,
//                            mSnd3HzToggle);
//        int pcm = mSnd3HzToggle ?  volume : -volume;
//        int pcm = volume - 8;
        mSnd3DutyPhase += 32;
        if (mSnd2DutyPhase >= hzHalfCycle)
        {
            mSnd3DutyStep++;
            if (mSnd3DutyStep >= 32)
                mSnd3DutyStep = 0;
            mSnd3DutyPhase = 0;
            if ((mSnd3DutyStep & 1) != 0)
                volume = (((mHighMem[GameBoyCPU.SND_BNK_10 +
                         (mSnd3DutyStep >>> 1)] &0x0F)) >>> volumeFactor);
            else
                volume = ((((mHighMem[GameBoyCPU.SND_BNK_10
                     + (mSnd3DutyStep >>> 1)] & 0xF0) >>> 4))
                            >>> volumeFactor);
        }
//        int pcm = wavePCM(mSnd3HzPhase, hzHalfCycle, 
//                            mSnd3HzToggle, volumeFactor);
        int pcm = mSnd3HzToggle ? -volume : volume;
        if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x3) != 0)
            b[i] += pcm;
        i++;
        if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x30) != 0)
            b[i] += pcm;
        i++;
    }
    if (overallLength > 0)
        mHighMem[GameBoyCPU.SND_3_LEN] = (byte) (256 - overallLength);
}

/**

  Output SOUND 4 data (White Noise)

  I have no idea how the prescaler/counter random number generator works, so
  here is a linearly congruent algorithm.

  At the end of the routine, the high memory locations are updated...

**/
private void generateSound4(byte[] b, int length)
{
    if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x44) == 0)
        return;

    int overallLength;
    if ((mHighMem[GameBoyCPU.SND_4_POLY_KICK_UPPER] & 0x40) == 0)
        overallLength = -1;
    else
    {
        if ((mHighMem[GameBoyCPU.SND_4_POLY_KICK_UPPER] & 0x80) != 0)
        {
            mHighMem[GameBoyCPU.SND_4_POLY_KICK_UPPER] &= 0x7F;
            mHighMem[GameBoyCPU.SND_STAT] |= 8;
            mSnd4EnvelopePhase = 0;
            mSnd4OverallPhase = 0;
        }
        else
        if ((mHighMem[GameBoyCPU.SND_STAT] & 8) == 0)
            return;
        overallLength = 64 - (mHighMem[GameBoyCPU.SND_4_LEN] & 0x3F);
    }

/*
 * I don't understand how this works, so use a linearly congruent random
 * number generator instead ...
 */
    int polyClock = (((int)
            mHighMem[GameBoyCPU.SND_4_POLY_KICK_LOWER]) & 0xF0) >>> 4;
    int polyLength = (((mHighMem[GameBoyCPU.SND_4_POLY_KICK_LOWER]) & 0x8)
            != 0) ? 15 : 7;
    int polyFreq = ((mHighMem[GameBoyCPU.SND_4_POLY_KICK_LOWER]) & 0x07);
    boolean incFlag = ((mHighMem[GameBoyCPU.SND_4_ENV] & 0x8) != 0);
    int volume = (((mHighMem[GameBoyCPU.SND_4_ENV] & 0xF0) >>> 4));
/*
 * Just to make sure that user settings change things somewhat
 */
    int hzFreq = (polyFreq * polyClock + polyLength) % 4093;

    int hzHalfCycle = (mSampleRate / hzFreq) >>> 1;
    if (hzHalfCycle < 1)
        hzHalfCycle = 1;

    int envelopeCycle = (mHighMem[GameBoyCPU.SND_4_ENV] & 0x7)
                             * (mOverallCycle << 2);
    for (int i = mHeadLen; i < length;)
    {
        if (envelopeCycle != 0)
        {
            mSnd4EnvelopePhase++;
            if (mSnd4EnvelopePhase >= envelopeCycle)
            {
                if (!incFlag)
                {
                    if (volume > 0)
                        volume--;
                }
                else
                if (volume < 15)
                    volume++;
                mHighMem[GameBoyCPU.SND_4_ENV] = (byte)
                          ((mHighMem[GameBoyCPU.SND_4_ENV] & 0x0F)
                        | (((volume << 4) & 0xF0)));
                mSnd4EnvelopePhase = 0;
            }
        }
        mSnd4OverallPhase++;
        if (mSnd4OverallPhase >= mOverallCycle)
        {
            overallLength--;
            if (overallLength == 0)
            {
                mHighMem[GameBoyCPU.SND_STAT] &= ~8;
                mHighMem[GameBoyCPU.SND_4_LEN] &= (byte) 0xC0;
                break;
            }
            mSnd4OverallPhase = 0;
            mSnd4HzPhase = 0;
            if (hzFreq <= 4093 / 47)
                hzFreq = (hzFreq * 47) % 4093;
            else
            {
                hzFreq = 47 * (hzFreq / (4093 / 47))
                        - (4093 % 47) * (hzFreq / (4093 / 47));
                if (hzFreq < 0)
                    hzFreq += 4093;
            }
            hzHalfCycle = (mSampleRate / hzFreq) >>> 1;
            if (hzHalfCycle < 1)
                hzHalfCycle = 1;
        }

        mSnd4HzPhase++;
        if (mSnd4HzPhase >= hzHalfCycle)
        {
            mSnd4HzToggle = mSnd4HzToggle ? false : true;
            mSnd4HzPhase = 0;
        }
        int pcm = mSnd4HzToggle ?  volume : -volume;
        if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x4) != 0)
            b[i] += pcm;
        i++;
        if ((mHighMem[GameBoyCPU.SND_STEREO] & 0x40) != 0)
            b[i] += pcm;
        i++;
    }
    if (overallLength > 0)
        mHighMem[GameBoyCPU.SND_4_LEN] = (byte) ((((int)
                          mHighMem[GameBoyCPU.SND_4_LEN]) & 0xC0)
                        | ((64 - overallLength) & 0x3F));
    mHighMem[GameBoyCPU.SND_4_POLY_KICK_LOWER] = (byte) hzFreq;
}

//    BufferedOutputStream m_log;
/**

  Constructor. Note the high memory, and initialiase the sound system. Begin
  the sound output loop.

 **/
public GameBoySound(byte[] highMem)
{
    mHighMem = highMem;
//        String [] poss = Manager.getSupportedContentTypes(null);
//        for (int i = 0; i < poss.length; i++)
//             System.out.println("Content Type: " + i + " - " + poss[i]);
    mSoundLine = initSound();

/*#J2SE#*///<editor-fold>
//        try
//        {
//            m_log = new BufferedOutputStream(new FileOutputStream (new
//            File("sound.pcm")));
//        }
//        catch (Exception e) {}
/*$J2SE$*///</editor-fold>
        startThread();

}

/**

  Initialise the Java Sound System. Return an object used for sound output

 **/


private
/*#J2SE#*///<editor-fold>
        SourceDataLine
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
//--            Player
/*$!J2SE$*///</editor-fold>
        initSound()
{
    try
    {
/*#J2SE#*///<editor-fold>
        AudioFormat format = new AudioFormat(
                         AudioFormat.Encoding.PCM_SIGNED,
                              mSampleRate, 8, 2, 2, mSampleRate, true);
        DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class,
                                                   format);
        System.out.println("Sound Info: " + lineInfo.toString());
        if (!AudioSystem.isLineSupported(lineInfo))
            System.out.println("Error: Audio system non-functional");
        else
        {
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(
                                                               lineInfo);
            int bufLength = (mSampleRate * mBufferMSec) / 500;
            mBuffer = new byte[bufLength];
            line.open(format, bufLength);
            line.start();
            System.out.println("Initialised sound: " + line.toString());
            return line;
        }
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
//--        mBuffer = new byte[mHeadLen + (mSampleRate * mBufferMSec)];
//--        System.arraycopy(wavHeader, 0,  mBuffer, 0, mHeadLen - 4);
//--        ByteArrayInputStream bias = new ByteArrayInputStream(mBuffer);
//--        mSoundLine = Manager.createPlayer(bias, "audio/x-wav");
//--        mSoundLine.realize();
//--        return mSoundLine;
/*$!J2SE$*///</editor-fold>
    }
    catch (Exception e)
    {
        System.out.println("Error: Sound initialisation failed" + e);
    }
    return null;
}

/**

   Add a lump of sound data to the buffer.

   Called from the thread's loop, or the CPU loop (more likely).

   What should probably happen is that the buffer associated with this class
   would be populated when the INITIALISE flags are set for SOUND 1, SOUND 2,
   SOUND 3, SOUND 4. This buffer would then get flushed to the sound hardware
   when there was no more space.

   The big problem is that the sound sounds AWFUL.

**/
/*#!J2SE#*///<editor-fold>
//--private static final byte [] wavHeader = {
//--                      0x52, 0x49, 0x46, 0x46, (byte) 0xac, 0x46, 0x25, 0x00,
//--                             0x57, 0x41, 0x56, 0x45, 0x66, 0x6d, 0x74, 0x20,
//--                             0x10, 0x00, 0x00, 0x00, 0x01, 0x00, 0x02, 0x00,
//--                             0x00, 0x20, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00,
//--                             0x02, 0x00, 0x08, 0x00, 0x64, 0x61, 0x74, 0x61};
//--
/*$!J2SE$*///</editor-fold>
public void outputSound(int msecsNeeded)
{
    if (((int) mHighMem[GameBoyCPU.SND_STAT] & 0x80) == 0
            || mHighMem[GameBoyCPU.SND_STEREO] == 0
// Always true!?  || (mHighMem[GameBoyCPU.SND_VOICE_INP] & 0x88) == 0
//            || mSoundLine == null
/*#!J2SE#*///<editor-fold>
//-- //           || (mSoundLine != null && mSoundLine.getState() != Player.PREFETCHED)
/*$!J2SE$*///</editor-fold>
            )
        return;
    int bytesNeeded = (mSampleRate * msecsNeeded) / 500;
    if ((bytesNeeded & 1) == 1)
        bytesNeeded++;
/*#!J2SE#*///<editor-fold>
//--    mBuffer[mHeadLen - 4] = (byte) (bytesNeeded & 0xFF);
//--    mBuffer[mHeadLen - 3] = (byte) ((bytesNeeded & 0xFF00) >>> 8);
//--    mBuffer[mHeadLen - 2] = (byte) ((bytesNeeded & 0xFF0000) >>> 16);
//--    mBuffer[mHeadLen - 1] = (byte) ((bytesNeeded & 0xFF000000) >>>24);
//--    for (int i = mHeadLen + bytesNeeded - 1; i >= mHeadLen; i--)
//--        mBuffer[i] = 0;
/*$!J2SE$*///</editor-fold>
/*#J2SE#*///<editor-fold>
    if (bytesNeeded > mSoundLine.available())
    {
//            System.out.println("msecsNeeded: " + msecsNeeded +
//                " bytesNeeded: " + framesNeeded +
//                " Available : " + mSoundLine.available());
        return;
    }
/*$J2SE$*///</editor-fold>
    generateSound1(mBuffer, bytesNeeded);
    generateSound2(mBuffer, bytesNeeded);
    if (((int) mHighMem[GameBoyCPU.SND_3_ON_OFF] & 0x80) != 0)
        generateSound3(mBuffer, bytesNeeded);
    generateSound4(mBuffer, bytesNeeded);
/*
 * Final Balance? This isn't set as I expect.... The ON/OFF flags are always
 * zero (off?)
 * bits 0-2 Left volume
 * bit 3    Left On/Off
 * bits 4-6 Right Volume
 * bit 7    Right On/Off
 */
//    System.out.println("'Voice!?' :" +
//    Integer.toHexString(((int) mHighMem[GameBoyCPU.SND_VOICE_INP]) & 0xFF));
    if ((mHighMem[GameBoyCPU.SND_VOICE_INP] & 0x4) != 0)
    {
        for (int i = mHeadLen + bytesNeeded - 2; i >= mHeadLen; i -= 2)
            mBuffer[i] += mBuffer[i];
    }
    else
    if ((mHighMem[GameBoyCPU.SND_VOICE_INP] & 0x2) == 0)
    {
        for (int i = mHeadLen + bytesNeeded - 2; i >= mHeadLen; i -= 2)
            mBuffer[i] >>= 1;
    }
    if ((mHighMem[GameBoyCPU.SND_VOICE_INP] & 0x40) != 0)
    {
        for (int i = mHeadLen + bytesNeeded - 1; i > mHeadLen; i -= 2)
            mBuffer[i] += mBuffer[i];
    }
    else
    if ((mHighMem[GameBoyCPU.SND_VOICE_INP] & 0x20) == 0)
    {
        for (int i = mHeadLen + bytesNeeded - 1; i > mHeadLen; i -= 2)
            mBuffer[i] >>= 1;
    }
/*#!J2SE#*///<editor-fold>
//--    for (int i = mHeadLen + bytesNeeded - 1; i >= mHeadLen; i--)
//--        mBuffer[i] += (byte) (mBuffer[i] + 0x80);
//--    try
//--    {
//--        mSoundLine.start();
//--    }
//--    catch (Throwable e)
//--    {
//--        e.printStackTrace();
//--        System.out.println("Sound failed " + e);
//--    }
/*$!J2SE$*///</editor-fold>

/*#J2SE#*///<editor-fold>
    mSoundLine.write(mBuffer, 0, bytesNeeded);
//       try
//       {
//           m_log.write(mBuffer, 0, bytesNeeded);
//       }
//       catch (Exception e) {}
    java.util.Arrays.fill(mBuffer, 0, bytesNeeded, (byte) 0);
/*$J2SE$*///</editor-fold>
    return;
}
private Thread m_thread;
private boolean mRunning;
private void startThread()
{
    if (m_thread == null)
    {
        mRunning = true;
        m_thread = new Thread(this);
        m_thread.start();
    }
}

/**

  Called to signal the thread to stop

**/
public void stopThread()
{
    mRunning = false;
    m_thread = null;
}

/**

  Called when the thread starts

**/
public void run()
{
//    m_thread.setPriority(Thread.MIN_PRIORITY);

    int bytesNeeded = (mSampleRate * 32) / 500;
    if ((bytesNeeded & 1) == 1)
        bytesNeeded++;
    int togo = 0;
    try
    {
        while (mRunning)
        {
/*#J2SE#*///<editor-fold>
            togo = mBuffer.length - mSoundLine.available();
            if (togo < bytesNeeded)
                outputSound(32);
            else
                Thread.sleep(10);
/*$J2SE$*///</editor-fold>
/*#!J2SE#*///<editor-fold>
//--        int state = mSoundLine.getState();
//--        if (state == Player.PREFETCHED)
//--            mSoundLine.deallocate();
//--        if ((state == Player.PREFETCHED)
//--          || (state == Player.REALIZED))
//--            outputSound(32);
//--        else
//--            System.out.println("Thread loop finds state " + state);
//--        Thread.sleep(32);
/*$!J2SE$*///</editor-fold>
        }
    }
    catch (Throwable e)
    {
        e.printStackTrace();
        System.err.println("Problem making sound");
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
