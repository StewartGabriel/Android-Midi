package com.gabriel.midi;
import android.app.Activity;
import android.content.Context;
import android.media.midi.*; //Not too sure which things I'll need in the future yet, so I'm including them all for now, also it's cleaner
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.IOException;

public class PlugInInstance {
    private static Activity unityActivity; //THIS NEEDS TO BE KEPT STATIC FOR UNITY TO BE ABLE TO USE IT, IGNORE THE MEMORY LEAK
    private MidiManager midiManager;
    private MidiDevice midiDevice;
//    private Thread listener;
//    private Thread processor;
//    private boolean stopThreads = false;
    public static void receiveUnityActivity(Activity tActivity) { //For the device to know it should only be aware of Unity
        unityActivity = tActivity;
    }

    public void createMidiManager() {//Initialize the midiManager, called on startup
        Context context = unityActivity.getApplicationContext();

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            Log.v("MIDI Manager", "Manger created");
            midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        } else {
            midiManager = null;
        }
    }

    public void createPort() { // Connect to the midi device
        if (midiManager.getDevices().length != 0) {
            Log.v("Create Port", "Attempting to create port...");
            Log.v("Devices Connected", midiManager.getDevices()[0].toString());
            openMidiDevice(midiManager.getDevices()[0]);
        }
    }

    public void openMidiDevice(MidiDeviceInfo deviceInfo) { // Return String for debugging in Unity
        midiManager.openDevice(deviceInfo, midiDevice -> {
            if (midiDevice != null) {
                this.midiDevice = midiDevice;
                // Set up the MIDI input port
                MidiDeviceInfo.PortInfo[] ports = midiDevice.getInfo().getPorts();
                for(MidiDeviceInfo.PortInfo port : ports) {
                    Log.v("Ports", port.toString());
                }
                MidiOutputPort outputPort = midiDevice.openOutputPort(0);  // Port index 0 is usually the first output port (in my piano, it is the only port)
                Log.v("Output Port", "OutputPort: " + outputPort);

                if (outputPort != null) { // Set up a listener for incoming MIDI messages
                    listenToMidiMessages(outputPort);
                }
            }
        }, null);
    }

    private void listenToMidiMessages(MidiOutputPort outputPort) { // Parallel processing to receive and interpret midi messages
        Receiver receiver = new Receiver(); // Custom Receiver class, extends MidiReceiver
        Thread listener = new Thread(() -> outputPort.connect(receiver));
        listener.setPriority(Thread.MAX_PRIORITY);
        Thread processor = new Thread(() -> {
            while(true) {
                receiver.processMidiData();
            }
        });
        processor.setPriority(Thread.MAX_PRIORITY - 1);
        listener.start();
        processor.start();
    }

    public int getDeviceAmount(){ // Check how many devices are connected
        int len = midiManager.getDevices().length;
        Log.v("Devices Connected", "" + len);
        if(len != 0) {
            for(MidiDeviceInfo device : midiManager.getDevices())
                for (MidiDeviceInfo.PortInfo port: device.getPorts())
                {
                    Log.v("Device Ports", port.toString());
                }
        }
        return midiManager.getDevices().length;
    }

    public int disconnectDevices() throws IOException {
        if(midiDevice != null){
//            if(listener != null && listener.isAlive()) { //Listener thread shouldn't have anything going, but just in case
//                stopThreads = true;
//                listener.interrupt();
//                Log.v("Thread Closed", "Listener Thread Closed");
//            }
//            if(processor != null && processor.isAlive()) { //Interrupt this thread to stop processing incoming data
//                processor.interrupt();
//                Log.v("Thread Closed", "Processor Thread Closed");
//            }
            midiDevice.close();
            Log.v("Closed Device", "Midi Device Closed");
            return 1;
        } else {
            Log.v("Closed Device", "No Device to Close");
            return 0;
        }
    }
}
