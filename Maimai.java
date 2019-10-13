package maimai;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

public class Maimai {

    public static void main(String[] args)throws IOException {
        Song zephyranthes = new Song(new File("Zephyranthes.json"));
        
        GUI window = new GUI("Maimai", zephyranthes);
        window.setFocusable(true);
        window.setVisible(true);
        window.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                Judgement eval = Song.check(e.getKeyCode(), window.activenotes);
                System.out.println(eval);
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        window.requestFocus();
    }

}

enum Judgement {
    MISS, GOOD, GREAT, PERFECT, CRITICAL;
}

class JudgementMissException extends Exception {
    public JudgementMissException(String message) {
        super(message);
    }
}

enum Difficulty {
    EASY, BASIC, ADVANCED, EXPERT, MASTER, REMASTER;
}
