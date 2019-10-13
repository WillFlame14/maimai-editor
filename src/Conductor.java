package maimai;

public class Conductor {
    double bpm;
    double crochet;
    int crochetsperbar;
    double songposition;
    double offset;
    
    public Conductor (double bpm, double songposition, int cpb, double offset) {
        this.bpm = bpm;
        crochet = 60 / bpm;
        crochetsperbar = cpb;
        this.songposition = songposition;
        this.offset = offset;
    }
    
    public void update(double position) {
        songposition = position - offset;
    }
}
