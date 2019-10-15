package maimai;

public class Note {
    int position;
    String type;
    
    public Note (int pos) {
        position = pos;
    }
}

class SingleNote extends Note {
    public SingleNote(int pos) {
        super(pos);
        type = "Single";
    }
}

class DoubleNote extends Note {
    int pos2;
    
    public DoubleNote (int pos1, int pos2) {
        super(pos1);
        this.pos2 = pos2;
        type = "Double";
    }
}

class HoldNote extends Note {
    double duration;
    
    public HoldNote (int pos, double dur) {
        super(pos);
        duration = dur;
        type = "Hold";
    }
}

class DoubleHoldNote extends Note {
    int pos2;
    double duration;
    
    public DoubleHoldNote (int pos1, int pos2, double dur) {
        super(pos1);
        this.pos2 = pos2;
        duration = dur;
        type = "DoubleHold";
    }
}

class CircleNote extends Note {
    int posfinal;
    
    public CircleNote (int posInitial, int posFinal) {
        super(posInitial);
        posfinal = posFinal;
        type = "Circle";
    }
}

class AcrossNote extends Note {
    int posfinal;
    
    public AcrossNote (int posInitial, int posFinal) {
        super(posInitial);
        posfinal = posFinal;
        type = "Across";
    }
}

class TapNote extends Note {
    double radius;
    
    public TapNote (int pos, double rad) {
        super(pos);
        radius = rad;
        type = "Tap";
    }
}

class Sequence {
    int bar;
    double beat, time;
    Note note;
    
    public Sequence(int bar, double beat, Note note, double time) {
        this.bar = bar;
        this.beat = beat;
        this.note = note;
        this.time = time;
    }
}

class ActiveNote {
    double radius;
    Note note;
    int complete;
    Judgement halfjudgement;
    
    public ActiveNote(Note note) {
        this.note = note;
        radius = 0;
        complete = -1;
    }
    
    public static Pair convertCartesian(double radius, int position) {
        return new Pair(radius * Math.cos(5 * Math.PI / 8 - Math.PI * position / 4), radius * Math.sin(5 * Math.PI / 8 - Math.PI * position / 4));
    }
}

class Pair {
    double a, b;
    
    public Pair(double a, double b) {
        this.a = a;
        this.b = b;
    }
}