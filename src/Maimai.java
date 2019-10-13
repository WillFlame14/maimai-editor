package maimai;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.JFXPanel;

public class Maimai {

    public static void main(String[] args)throws IOException {
        Song zephyranthes = new Song(new File("Zephyranthes.json"));
        final JFXPanel fxPanel = new JFXPanel();        //to initialize toolkit for the media player
        
        GUI window = new GUI("Maimai", zephyranthes);
        window.setFocusable(true);
        window.setVisible(true);
        window.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                Judgement eval = Song.check(e.getKeyCode(), window.activenotes);
                if(eval != Judgement.ERROR)
                    System.out.println(eval);
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        window.requestFocus();
    }

}

enum Judgement {
    ERROR(0), MISS(1), GOOD(2), GREAT(3), PERFECT(4), CRITICAL(5);
    int id;
    
    Judgement (int id) {
        this.id = id;
    }
    
    public static Judgement evaluate(double distance) {
        if(distance < 5)      //TODO: Make dependent on bpm
            return CRITICAL;
        if(distance < 10)     
            return PERFECT;
        if(distance < 15)      
            return GREAT;
        if(distance < 20)
            return GOOD;
        return MISS;
    }
    
    public static Judgement idToJudgement (int id) {
        switch (id) {
            case 1:
                return Judgement.MISS;
            case 2:
                return Judgement.GOOD;
            case 3: 
                return Judgement.GREAT;
            case 4:
                return Judgement.PERFECT;
            case 5:
                return Judgement.CRITICAL;
            default:
                return Judgement.ERROR;
        }
    }
    
    public static Judgement average(Judgement j1, Judgement j2) {
        return idToJudgement((int)Math.round((j1.id + j2.id) / 2.0));
    }
    
    public String toString() {
        switch(this) {
            case PERFECT:
                return "Perfect";
            case GOOD:
                return "Good";
            case MISS:    
                return "Miss";
            case ERROR:
                return "AAAAAAAA";
            default:
                return "BBBBBBBBBBB";
        }
    }
}

class JudgementMissException extends Exception {
    public JudgementMissException(String message) {
        super(message);
    }
}

enum Difficulty {
    EASY, BASIC, ADVANCED, EXPERT, MASTER, REMASTER;
}
