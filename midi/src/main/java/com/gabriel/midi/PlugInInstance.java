package com.gabriel.midi;
import android.app.Activity;
import android.content.Context;
import android.media.midi.*;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.util.Log;

public class PlugInInstance {

    private static Activity unityActivity; //This doesn't work unless it is static, so I'm just ignoring this leak for now
    private MidiManager midiManager;

    public static void receiveUnityActivity(Activity tActivity) //For the device to know it should only be aware of Unity
    {
        unityActivity = tActivity;
    }

    public void createMidiManager() //Initialize the midiManager, called on startup
    {
        Context context = unityActivity.getApplicationContext();

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            Log.v("MIDI Manager", "Manger created");
            midiManager = (MidiManager)context.getSystemService(Context.MIDI_SERVICE);
        }

        else
        {
            midiManager = null;
        }
    }

    public void createPort()
    {
        if(midiManager.getDevices().length != 0)
        {
            openMidiDevice(midiManager.getDevices()[0]);
        }
    }

    public void openMidiDevice(MidiDeviceInfo deviceInfo) { // Return String for debugging in Unity
        midiManager.openDevice(deviceInfo, midiDevice -> {

                if (midiDevice != null)
                {
                    // Set up the MIDI input port
                    MidiOutputPort outputPort = midiDevice.openOutputPort(0);  // Port index 0 is usually the first output port (in my piano, it is the only port)
                    Log.v("Output Port", "OutputPort: " + outputPort);

                    if (outputPort != null) // Set up a listener for incoming MIDI messages
                    {
                            listenToMidiMessages(outputPort);
                    }
                }
        }, null);
    }

    private void listenToMidiMessages(MidiOutputPort outputPort) {
        Receiver receiver = new Receiver(); // Custom Receiver class, extends MidiReceiver
        new Thread(() -> { // Thread to listen for MIDI events
                outputPort.connect(receiver);
        }).start();
    }


    public int getDeviceAmount() // Check how many devices are connected
    {
        return midiManager.getDevices().length;
    }


    //TABLET DEBUG METHODS
    public int Add(int i, int j)
    {
        return i + j;
    }

    public void Toast(String msg)
    {
        Toast.makeText(unityActivity, msg, Toast.LENGTH_SHORT).show();
    }
}
