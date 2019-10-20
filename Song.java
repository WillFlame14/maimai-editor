package maimai;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import org.json.simple.*;
import org.json.simple.parser.*;

public class Song {
    String title, artist;
    double bpm, crochet, offset, scrollspeed;
    int crochetsperbar;
    Conductor conductor;
    LinkedList<Sequence> notes = new LinkedList<>();
    
    public Song (File file) throws IOException {
        JSONParser parser = new JSONParser();

        try {
            JSONObject obj = (JSONObject) parser.parse(new FileReader(file));

            bpm = Double.parseDouble(obj.get("bpm") + "");
            crochet = 60 / bpm;     //how long one beat is, in seconds
            crochetsperbar = Integer.parseInt(obj.get("crochetsperbar") + "");
            offset = Double.parseDouble(obj.get("offset") + "");
            scrollspeed = GUI.hitradius / 250.0;

            conductor = new Conductor(bpm, 0, crochetsperbar, offset);

            JSONArray noteObjs = (JSONArray) obj.get("notes");
            for (Object o : noteObjs) {
                JSONObject noteObj = (JSONObject) o;
                int bar = Integer.parseInt(noteObj.get("bar") + "");
                double beat = Double.parseDouble(noteObj.get("beat") + "");
                int pos = Integer.parseInt(noteObj.get("position") + "");
                String type = noteObj.get("type") + "";
                Note note = new Note(0);
                
                int pos2;
                double duration, radius;
                
                switch(type) {
                    case "Single":
                        note = new SingleNote(pos);
                        break;
                    case "Double":
                        pos2 = Integer.parseInt(noteObj.get("position2") + "");
                        note = new DoubleNote(pos, pos2);
                        break;  
                    case "Hold":
                        duration = Double.parseDouble(noteObj.get("duration") + "");
                        note = new HoldNote(pos, duration);
                        break;
                    case "DoubleHold":
                        pos2 = Integer.parseInt(noteObj.get("position2") + "");
                        duration = Double.parseDouble(noteObj.get("duration") + "");
                        note = new DoubleHoldNote(pos, pos2, duration);
                        break;    
                    case "Circle":
                        pos2 = Integer.parseInt(noteObj.get("position2") + "");
                        boolean clockwise = Boolean.getBoolean(noteObj.get("clockwise") + "");
                        note = new CircleNote(pos, pos2, clockwise);
                        break;
                    case "Across":
                        pos2 = Integer.parseInt(noteObj.get("position2") + "");
                        note = new AcrossNote(pos, pos2);
                        break;
                    case "Tap":
                        radius = Double.parseDouble(noteObj.get("radius") + "");
                        note = new TapNote(pos, radius);
                        break;
                    default:
                        throw new ClassCastException("");
                }
                notes.add(new Sequence(bar, beat, note, ((bar * crochetsperbar + beat) * crochet + offset) * 1000));
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println("The song file was not found.");
        } catch (ClassCastException ce) {
            System.out.println("The song file was not formatted correctly.");
            ce.printStackTrace();
        } catch (ParseException pe) {
            System.out.println("The song file was not able to be read.");
            pe.printStackTrace();
        }
    }
    
    public void runSong(LinkedList<Sequence> map, ArrayList<ActiveNote> activenotes) {
        int counter = 0;
        while(counter < map.size() && map.get(counter).time <= conductor.songposition) {
            activenotes.add(new ActiveNote(map.get(counter).note));
            counter++;
        }
        for(int i = 0; i < counter; i++) {      //remove the used arrows
            map.remove(0);
        }
    }
    
    public static Judgement check(int keyCode, double customscrollspeed, ArrayList<ActiveNote> activenotes) {
        HashMap<Integer, ActiveNote> hittable = new HashMap<>(8);
        HashSet<Integer> positions = new HashSet<>();     //impossible to hit 2 notes with same position
        for(ActiveNote a: activenotes) {
            if(a.radius >= 100 && !positions.contains(a.note.position)) {        //once again, impossible to hit 2 notes with same position (doubles are exclusive) TODO: radius dependent on bpm
                switch(a.note.type) {       //TODO: circle and across notes
                    case "Double":
                        positions.add(((DoubleNote)a.note).pos2);
                        hittable.put(((DoubleNote)a.note).pos2, a);
                        break;
                    case "DoubleHoldNote":
                        positions.add(((DoubleHoldNote)a.note).pos2);
                        hittable.put(((DoubleHoldNote)a.note).pos2, a);
                        break; 
                }
                hittable.put(a.note.position, a);
                positions.add(a.note.position);
            }
        }
        Judgement judgement;
        ActiveNote toHit;
        switch(keyCode) {
            case KeyEvent.VK_0:
            case KeyEvent.VK_1:
            case KeyEvent.VK_2:
            case KeyEvent.VK_3:
            case KeyEvent.VK_4:
            case KeyEvent.VK_5:
            case KeyEvent.VK_6:
            case KeyEvent.VK_7:
            case KeyEvent.VK_8:
            case KeyEvent.VK_9:    
                int value = keyCode - 48;
                if(positions.contains(value)) {
                    toHit = hittable.get(value);
                    judgement = Judgement.evaluate(Math.abs(toHit.radius - GUI.hitradius), customscrollspeed);
                    if(toHit.note.type.equals("Single") || toHit.note.type.equals("Tap") || toHit.complete != -1) {     //single taps, or half the note has been completed already
                        activenotes.remove(hittable.get(value));
                        if(toHit.complete != -1) {      //average both judgements
                            return Judgement.average(judgement, toHit.halfjudgement);
                        }
                        return judgement; 
                    }
                    else {      //this is only the first half of the note
                        toHit.complete = value;
                        toHit.halfjudgement = judgement;
                    }
                }
                break;
            default:
                return Judgement.ERROR;
        }
        return Judgement.MISS;
    }
    
    public void checkfail(ArrayList<ActiveNote> activenotes)throws JudgementMissException {
        ArrayList<ActiveNote> toRemove = new ArrayList<>(10);
        boolean missed = false;
        for(ActiveNote an: activenotes) {
            switch(an.note.type) {
                case "Single":
                case "Double":
                    if(an.radius >= GUI.hitradius + 40) {
                        toRemove.add(an);
                        missed = true;
                    }
                    break;
                case "Hold":
                case "DoubleHold":
                    if(an.radius >= GUI.hitradius + ((HoldNote)an.note).duration * crochet * scrollspeed) {
                        System.out.println(((HoldNote)an.note).duration * crochet);
                        toRemove.add(an);
                        missed = true;
                    }
                    break;
                case "Circle":
                case "Across":
                    if(an.progress >= 1) {
                        toRemove.add(an);
                        missed = true;
                    }
                    break;
            }
        }
        if(missed) {
            for(ActiveNote an: toRemove) {
                activenotes.remove(an);
            }
            throw new JudgementMissException("");
        }
    }
} 
