package game;

import java.awt.Point;

public class TrailPoint {
    public Point pos;
    public int life; // 0..255 alpha for drawing or life count
    
    public TrailPoint(Point p, int life) {
        this.pos = p;
        this.life = life;
    }
}
