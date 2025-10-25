package Checkers;

import javax.sound.sampled.*;

/**
 * Handles sound playback for the Checkers game.
 */
public class SoundManager {
    /**
     * Plays a .wav sound from the /sounds/ folder asynchronously.
     *
     * @param soundName name of the sound file (without extension)
     */
    public static void playSound(String soundName) {
        new Thread(() -> {
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    SoundManager.class.getResource("/sounds/" + soundName + ".wav")
                );
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                System.err.print("Failed to play sound: " + soundName);
                System.err.print(" (" + e.getMessage() + ")");
                System.err.println("");
            }
        }).start();
    }
    
}
