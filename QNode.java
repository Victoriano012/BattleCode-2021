package SprintBot2;
import battlecode.common.*;

public class QNode
{
    public QNode next;
    MapLocation loc;
    Direction dir;
    double distance;
    
    public QNode (MapLocation loc, Direction dir, double distance){
        this.loc = loc;
        this.dir  = dir;
        this.distance = distance;
    }
}