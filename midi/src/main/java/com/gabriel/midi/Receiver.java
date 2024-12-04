package com.gabriel.midi;
import android.media.midi.MidiReceiver;
import android.util.Log;
import com.unity3d.player.UnityPlayer;

public class Receiver extends MidiReceiver{

    public void onSend(byte[] msg, int offset, int count, long timestamp)
    {
        processMidiData(msg, count);
    }

    void processMidiData(byte[] data, int count)
    {
        if (count > 0) {
            Log.v("Databytes Received","Byte data | " + data[0] + " | " + data[1] + " | " + data[2] + " | " + data[3] + " | " + data[4] + " | " + data[5]);
            int type = data[1]; // Message type, from my testing, -112 means a it's a note on/off message
            int note = data[2]; // MIDI note number (60 = middle C /C3)
            int velocity = data[3]; // Velocity of the note, can values range 0-127

            if (type == -112) // A note message is received
            {
                Log.v("Midi Received", "Byte data | " + data[0] + " | " + data[1] + " | " + data[2] + " | " + data[3] + " | " + data[4] + " | " + data[5]);

                if (velocity > 0) // A key was pressed (velocity > 0)
                {
                    onNotePressed(note, velocity);
                }

                else  // A key was released (velocity == 0)
                {
                    onNoteReleased(note);
                }
            }
        }
    }

    private void onNotePressed(int note, int velocity)
    {
        // TODO return note pressed back to Unity somehow
        Log.v("Note Pressed", "Note pressed: " + note + ", Velocity: " + velocity );
        UnityPlayer.UnitySendMessage("Plugin", "ReceiveMIDI", "On: " + note + " Velocity: " + velocity);
    }

    private void onNoteReleased(int note)
    {
        // TODO return note released back to Unity somehow
        Log.v("Note Released", "Note released: " + note);
        UnityPlayer.UnitySendMessage("Plugin", "ReceiveMIDI", "Off: " + note + " Velocity: " + 0);
    }

}
