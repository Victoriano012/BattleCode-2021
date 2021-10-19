package SprintBot2;
import battlecode.common.*;

public class Slanderer extends Unit{
    public void AwakeData() throws GameActionException{
        super.AwakeData();
    }
    public void Awake() throws GameActionException{
        super.Awake();
    }
    public void UpdateData() throws GameActionException{
        super.UpdateData();
        if (rc.getType() == RobotType.POLITICIAN){
            Politician newAI = new Politician();
            newAI.baseLocation = baseLocation;
            newAI.baseId = baseId;
            newAI.run(rc);
        }
    }
    public void Update() throws GameActionException{
        super.Update();
        for (RobotInfo ri : allEnemies){
            if (ri.getType() == RobotType.MUCKRAKER){
                move3Cheap(ri.getLocation().directionTo(position));
                break;
            }
        }
        if (baseLocation != null && !position.isWithinDistanceSquared(baseLocation, 30))
            pathfindTo(baseLocation);
        else if (position.isWithinDistanceSquared(baseLocation, 4))
            move3Cheap(baseLocation.directionTo(position));
    }

    public void move3Cheap(Direction dir) throws GameActionException{
        double best = 0.01;
        Direction bestDir = null;
        if (rc.canMove(dir)){
            double k = rc.sensePassability(position.add(dir));
            if (k> best){
                best = k;
                bestDir = dir;
            }
        }
        if (rc.canMove(dir.rotateLeft())){
            double k = rc.sensePassability(position.add(dir.rotateLeft()));
            if (k> best){
                best = k;
                bestDir = dir.rotateLeft();
            }
        }
        if (rc.canMove(dir.rotateRight())){
            double k = rc.sensePassability(position.add(dir.rotateRight()));
            if (k > best){
                best = k;
                bestDir = dir.rotateRight();
            }
        }
        if (bestDir != null){
            rc.move(bestDir);
        }
    }
}
