package com.gabriel.midi;
import android.media.midi.MidiReceiver;
import android.util.Log;
import com.unity3d.player.UnityPlayer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Receiver extends MidiReceiver{
    private static final BlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();

    public void onSend(byte[] msg, int offset, int count, long timestamp) { //put the message into a queue to minimize listener's usage time
        Log.v("Midi Received", "Byte data | " + msg[0] + " | " + msg[1] + " | " + msg[2] + " | " + msg[3] + " | " + msg[4] + " | " + msg[5]
        + " | " + msg[6] + " | " + msg[7] + " | " + msg[8] + " | " + msg[9] + " | " + msg[10] + " | " + msg[11] + " | " + msg[12] + " | " + msg[13]); // the whole byte[] is 1024, but the relevant indexes stop after index 50 in the worst scenario
        dataQueue.add(msg);
    }

    public void processMidiData() {
        int byteIndex = 1;
        byte[] data = dataQueue.poll();

        while (data != null && data[1] == -112 && byteIndex + 2 <= data.length) {
            if(data[byteIndex] == -112) {
                Log.v("On/Off Received", "Byte data | "  + data[byteIndex] + " | " + data[byteIndex + 1] + " | " + data[byteIndex + 2]);
                int note = data[byteIndex + 1]; // MIDI note number (60 = middle C /C3)
                int velocity = data[byteIndex + 2]; // Velocity of the note, values range 0-127

                if (velocity > 0) {// A key was pressed (velocity > 0)
                    onNotePressed(note, velocity);
                }
                else {// A key was released (velocity <= 0)
                    onNoteReleased(note);
                }
            } else if (data[byteIndex] == 0 && data[byteIndex + 1] == 0) { // End of current byte's used memory
                break;
            }
            byteIndex += 3;
        }
    }

    private void onNotePressed(int note, int velocity) {
        Log.v("Note Pressed", "Note pressed: " + note + ", Velocity: " + velocity );
        UnityPlayer.UnitySendMessage("Plugin", "ReceiveMIDI", "On: " + note + " Velocity: " + velocity);
    }

    private void onNoteReleased(int note) {
        Log.v("Note Released", "Note released: " + note);
        UnityPlayer.UnitySendMessage("Plugin", "ReceiveMIDI", "Off: " + (Math.abs(note) % 127) + " Velocity: " + 0);
    }
}
