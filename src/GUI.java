package maimai;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GUI extends JFrame implements ActionListener, MouseListener {
    static Timer t;
    static ArrayList<ActiveNote> activenotes = new ArrayList<>();
    Conductor conductor;     //bpm, songposition, crochetsperbar, offset
    static MediaPlayer mediaPlayer;
    static Song song;
    static Image singlenote, doublenote, holdnote, doubleholdnote, circlenote, acrossnote, tapnote, sliderarrow, hitarea;
    static double hitradius = 290, sliderspeed = 2;
    static int centrex = 275, centrey = 275;
    
    public GUI(String name, Song song)throws IOException {
        super(name);
        JPanel content = new JPanel();
        content.setLayout(new FlowLayout());
        Movement movement = new Movement(activenotes); // Updates the board
        conductor = song.conductor;
        this.song = song;
        
        String bip = "zephyranthes.mp3";
        Media hit = new Media(new File(bip).toURI().toString());
        mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.setVolume(0.5);
        mediaPlayer.play();
        
        singlenote = ImageIO.read(new File("single_note.png")).getScaledInstance(50, 50, BufferedImage.SCALE_DEFAULT);
        doublenote = ImageIO.read(new File("double_note.png")).getScaledInstance(50, 50, BufferedImage.SCALE_DEFAULT);
//        holdnote = ImageIO.read(new File("hold_note.png")).getScaledInstance(50, 50, BufferedImage.SCALE_DEFAULT);
//        doubleholdnote = ImageIO.read(new File("double_hold_note.png")).getScaledInstance(50, 50, BufferedImage.SCALE_DEFAULT);
        circlenote = ImageIO.read(new File("circle_note.png")).getScaledInstance(50, 50, BufferedImage.SCALE_DEFAULT);
//        acrossnote = ImageIO.read(new File("across_note.png")).getScaledInstance(50, 50, BufferedImage.SCALE_DEFAULT);
//        tapnote = ImageIO.read(new File("tap_note.png")).getScaledInstance(50, 50, BufferedImage.SCALE_DEFAULT);
        sliderarrow = ImageIO.read(new File("slider_arrow.png")).getScaledInstance(16, 16, BufferedImage.SCALE_DEFAULT);
        hitarea = ImageIO.read(new File("hit_area.png")).getScaledInstance(600, 600, BufferedImage.SCALE_DEFAULT);
        
        t = new Timer(3, movement);
        t.start();

        DrawArea board = new DrawArea(600, 600);
        content.add(board);
        content.addMouseListener(this);
        
        setContentPane(content);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    class Movement implements ActionListener {
        private ArrayList<ActiveNote> activenotes;
        
        public Movement(ArrayList<ActiveNote> f) {
            activenotes = f;
        }
        
        public void actionPerformed(ActionEvent e) {
            LinkedList<Sequence> notes = song.notes;
            conductor.update(mediaPlayer.getCurrentTime().toMillis());
            song.runSong(notes, activenotes);
            updateAllPos();       // Update all positions each timer tick
            try {
                song.checkfail(activenotes);
            }
            catch(JudgementMissException miss) {
                System.out.println("Miss");
            }
            repaint();
        }
        
        private void updateAllPos() {
            for (ActiveNote b: activenotes) {
                switch(b.note.type) {
                    case "Single":
                    case "Double":
                        b.radius += song.scrollspeed;
                        break;
                    case "Hold":
                    case "DoubleHold":
                        break;
                    case "Circle":
                    case "Across":
                        if(b.radius <= GUI.hitradius) {
                            b.radius += song.scrollspeed;
                        }
                        else {
                            System.out.println("SLIDING");
                            b.progress += 0.005;
                        }
                        break;
                }
            }
        }
    }
    
    public static void show(Graphics g) {
        g.drawImage(hitarea, 0, 0, null);      //hit area
        
        for(ActiveNote a: activenotes) {      //active notes
            Pair p1 = ActiveNote.convertCartesian(a.radius, a.note.position), p2;
            switch(a.note.type) {
                case "Single":
                    g.drawImage(singlenote, (int)p1.a + centrex, (int)p1.b + centrey, null);
                    break;
                case "Double":
                    p2 = ActiveNote.convertCartesian(a.radius, ((DoubleNote)a.note).pos2);
                    g.drawImage(doublenote, (int)p1.a + centrex, (int)p1.b + centrey, null);
                    g.drawImage(doublenote, (int)p2.a + centrex, (int)p2.b + centrey, null);
                    break;    
                case "Hold":
                    g.drawImage(holdnote, (int)p1.a + centrex, (int)p1.b + centrey, null);
                    break;
                case "DoubleHold":
                    p2 = ActiveNote.convertCartesian(a.radius, ((DoubleHoldNote)a.note).pos2);
                    g.drawImage(doubleholdnote, (int)p1.a + centrex, (int)p1.b + centrey, null);
                    g.drawImage(doubleholdnote, (int)p2.a + centrex, (int)p2.b + centrey, null);
                    break;    
                case "Circle":
                    if(a.radius >= GUI.hitradius && a.progress != 1) {     //in the middle of sliding
                        CircleNote thisnote = (CircleNote)a.note;
                        double initialAngle = 5 * Math.PI / 8 - Math.PI * thisnote.position / 4,
                                finalAngle = 5 * Math.PI / 8 - Math.PI * thisnote.posfinal / 4;
                        if(!thisnote.clockwise) {
                            if(initialAngle < finalAngle) {
                                initialAngle += Math.PI * 2;
                            }
                            for(double i = initialAngle; i >= finalAngle; i -= Math.PI / 20.0) {
                                p2 = ActiveNote.convertCartesianRadians(a.radius - 15, i);
                                g.drawImage(sliderarrow, (int)(p2.a + centrex + 17), (int)(p2.b + centrey + 17), null);
                            }
                        }
                        else {
                            if(initialAngle > finalAngle) {
                                initialAngle -= Math.PI * 2;
                            }
                            for(double i = initialAngle; i <= finalAngle; i += Math.PI / 20.0) {
                                p2 = ActiveNote.convertCartesianRadians(a.radius - 15, i);
                                g.drawImage(singlenote, (int)(p2.a + centrex + 17), (int)(p2.b + centrey + 17), null);
                            }
                        }
                    }
                    else {          //haven't started sliding yet
                        g.drawImage(circlenote, (int)p1.a + centrex, (int)p1.b + centrey, null);
                    }
                    break;  
                case "Across":
                    //p2 = ActiveNote.convertCartesian(a.radius, ((AcrossNote)a.note).posfinal);        //p2 is used later
                    g.drawImage(acrossnote, (int)p1.a + centrex, (int)p1.b + centrey, null);
                    //g.drawImage(acrossnote, (int)p2.a + centrex, (int)p2.b + centrey, null);
                    break;
                case "Tap":
                    g.drawImage(tapnote, (int)p1.a + centrex, (int)p1.b + centrey, null);
                    break;
            }
        }
    }

    class DrawArea extends JPanel {
        public DrawArea(int width, int height) {
            this.setPreferredSize(new Dimension(width, height));    // Size
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(createImage(), 0, 0, this);     // Draws the new image directly over the old one
        }
        
        private BufferedImage createImage() {       // Creates a new image where the current display is drawn
            BufferedImage bufferedImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
            GUI.show(bufferedImage.getGraphics());
            return bufferedImage;
        }
    }
    
    public ArrayList<ActiveNote> getActiveNotes() {
        return activenotes;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
