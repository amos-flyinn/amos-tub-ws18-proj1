package com.amos.server.mediadecoder;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;


final class NalTypes {
    public final static int SPS = 0x7;
    public final static int PPS = 0x8;
}


public class DecoderThread extends Thread
{
    private final static String TAG = "DecoderThread";

    // local constants
    private final static int FINISH_TIMEOUT = 5000;
    private final static int BUFFER_SIZE = 16384;
    private final static int NAL_SIZE_INC = 4096;
    private final static int MAX_READ_ERRORS = 300;

    // instance variables
    private MediaCodec decoder = null;
    private MediaFormat format;
    private boolean decoding = false;
    private int decoderState = 0;
    private Surface surface;
    private SurfaceHolder holder;
    // private Canvas canvas;
    private Bitmap bitmap;
    private byte[] buffer = null;
    private ByteBuffer[] inputBuffers = null;
    private long presentationTime;
    private long presentationTimeInc = 66666;
    private InputStream reader = null;
    // private Handler startVideoHandler;
    // private Runnable startVideoRunner;

    public native String stringFromJNI();

    //******************************************************************************
    // setSurface
    //******************************************************************************
    void setSurface(Surface surface)
    {
        this.surface = surface;
        // this.bitmap = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
        // this.startVideoHandler = handler;
        // this.startVideoRunner = runner;
        if (decoder != null)
        {
            if (surface != null)
            {
                boolean newDecoding = decoding;
                if (decoding)
                {
                    setDecodingState(false);
                }
                if (format != null)
                {
                    try
                    {
                        decoder.configure(format, surface, null, 0);
                    }
                    catch (Exception ex) {}
                    if (!newDecoding)
                    {
                        newDecoding = true;
                    }
                }
                if (newDecoding)
                {
                    setDecodingState(newDecoding);
                }
            }
            else if (decoding)
            {
                setDecodingState(false);
            }
        }
    }

    public void setInputStream(InputStream is) {
        reader = is;
    }

    //******************************************************************************
    // getMediaFormat
    //******************************************************************************
    MediaFormat getMediaFormat()
    {
        return format;
    }

    //******************************************************************************
    // setDecodingState
    //******************************************************************************
    private synchronized void setDecodingState(boolean newDecoding)
    {
        try
        {
            if (newDecoding != decoding && decoder != null)
            {
                if (newDecoding)
                {
                    decoder.start();
                }
                else
                {
                    decoder.stop();
                }
                decoding = newDecoding;
            }
        } catch (Exception ex) {}
    }

    //******************************************************************************
    // run
    //******************************************************************************
    @Override
    public void run()
    {
        byte[] nal = new byte[NAL_SIZE_INC];
        int nalLen = 0;
        int numZeroes = 0;
        int numReadErrors = 0;

        try
        {
            // create the decoder
            decoder = MediaCodec.createDecoderByType("video/avc");

            // create the reader
            buffer = new byte[BUFFER_SIZE];

            BufferedInputStream nreader = new BufferedInputStream(reader);

            // read until we're interrupted
            while (!isInterrupted())
            {
                // read from the stream
                int len = nreader.read(buffer);
                if (isInterrupted()) break;

                // process the input buffer
                if (len > 0)
                {
                    numReadErrors = 0;
                    for (int i = 0; i < len && !isInterrupted(); i++)
                    {
                        // add the byte to the NAL
                        if (nalLen == nal.length)
                        {
                            nal = Arrays.copyOf(nal, nal.length + NAL_SIZE_INC);
                        }
                        nal[nalLen++] = buffer[i];

                        // look for a header
                        if (buffer[i] == 0)
                        {
                            numZeroes++;
                        }
                        else
                        {
                            if (buffer[i] == 1 && numZeroes == 3)
                            {
                                if (nalLen > 4)
                                {
                                    int nalType = processNal(nal, nalLen - 4);
                                    if (isInterrupted()) break;
                                    if (nalType == -1)
                                    {
                                        nal[0] = nal[1] = nal[2] = 0;
                                        nal[3] = 1;
                                    }
                                }
                                nalLen = 4;
                            }
                            numZeroes = 0;
                        }
                    }
                }
                else
                {
                    numReadErrors++;
                    if (numReadErrors >= MAX_READ_ERRORS)
                    {
                        break;
                    }
                }

                // send an output buffer to the surface
                if (format != null && decoding)
                {
                    if (isInterrupted()) break;
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    int index;
                    do
                    {
                        index = decoder.dequeueOutputBuffer(info, 0);
                        if (isInterrupted()) break;
                        if (index >= 0)
                        {
                            // ByteBuffer output = decoder.getOutputBuffer(index);
                            // Log.d(TAG, String.format("Output: %d", output.remaining()));
                            decoder.releaseOutputBuffer(index, true);
                        }
                        Log.i(TAG, String.format("dequeueOutputBuffer index = %d", index));
                    } while (index >= 0);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // close the reader
        if (reader != null)
        {
            try
            {
                reader.close();
            }
            catch (Exception ex) {}
            reader = null;
        }

        // stop the decoder
        if (decoder != null)
        {
            try
            {
                setDecodingState(false);
                decoder.release();
            }
            catch (Exception ex) {}
            decoder = null;
        }
    }

    //******************************************************************************
    // processNal
    //******************************************************************************
    private int processNal(byte[] nal, int nalLen)
    {
        // get the NAL type
        int nalType = (nalLen > 4 && nal[0] == 0 && nal[1] == 0 && nal[2] == 0 && nal[3] == 1) ? (nal[4] & 0x1F) : -1;
        Log.i(TAG, String.format("NAL: type = %d, len = %d", nalType, nalLen));

        // process the first SPS record we encounter
        if (nalType == 7 && !decoding) {
            SpsParser parser = new SpsParser(nal, nalLen);
            format = MediaFormat.createVideoFormat("video/avc", parser.width, parser.height);
            presentationTimeInc = 66666;
            presentationTime = System.nanoTime() / 1000;
            // Log.i(TAG, String.format("SPS: %02X, %d x %d, %d", nal[4], parser.width, parser.height, presentationTimeInc));
            // Log.i(TAG, String.format("(raw) SPS: %s", Arrays.toString(nal)));
            decoder.configure(format, surface, null, 0);
            setDecodingState(true);
            inputBuffers = decoder.getInputBuffers();
        }

        // queue the frame
        if (nalType > 0 && decoding)
        {
            int index = decoder.dequeueInputBuffer(0);
            if (index >= 0)
            {
                ByteBuffer inputBuffer = inputBuffers[index];
                //ByteBuffer inputBuffer = decoder.getInputBuffer(index);
                inputBuffer.put(nal, 0, nalLen);
                // decoder.queueInputBuffer(index, 0, nalLen, presentationTime, 0);
                decoder.queueInputBuffer(index, 0, nalLen, presentationTime, 0);
                presentationTime += presentationTimeInc;
                // if ((nalType & 0xf) > 0) {
                //     decoder.queueInputBuffer(index, 0, nalLen, 0, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
                // } else {
                //     decoder.queueInputBuffer(index, 0, nalLen, 0, 0);
                //     presentationTime += presentationTimeInc;
                // }
            }
            //Log.info(String.format("dequeueInputBuffer index = %d", index));
        }
        return nalType;
    }

    static {
        System.loadLibrary("serverlib");
    }
}
