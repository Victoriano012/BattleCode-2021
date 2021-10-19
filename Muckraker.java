package SprintBot2;
import battlecode.common.*;

public class Muckraker extends Unit{
    boolean isWall = false;

    public void AwakeData() throws GameActionException{
        super.AwakeData();
    }
    public void Awake() throws GameActionException{
        super.Awake();
    }
    public void UpdateData() throws GameActionException{
        super.UpdateData();
    }
    public void Update() throws GameActionException{
        super.Update();
        int scoreHeavy = 0;
        int scoreHeavyInRange = 0;
        MapLocation heaviestInRange = null;
        MapLocation heaviest = null;
        for (RobotInfo ri : allEnemies){
            if (ri.getType() == RobotType.SLANDERER){
                int i = ri.getInfluence();
                if (position.isWithinDistanceSquared(ri.getLocation(), 13)){
                    if (i > scoreHeavyInRange){
                        scoreHeavyInRange = i;
                        heaviestInRange = ri.getLocation();
                    }
                }
                if (i > scoreHeavy){
                    scoreHeavy = i;
                    heaviest = ri.getLocation();
                }
            }
        }
        if (heaviestInRange != null){
            tryExpose(heaviestInRange);
        }
        else if (heaviest != null){
            pathfindTo(heaviest);
        }
        else {
            explore();
        }
    }

    public boolean tryMove(Direction dir) throws GameActionException{
        RobotController rc = this.rc;
        if (rc.canMove(dir)){
            rc.move(dir);
            return true;
        }
        return false;
    }

    public boolean tryExpose(MapLocation ml) throws GameActionException{
        RobotController rc = this.rc;
        if (rc.canExpose(ml)){
            rc.expose(ml);
            return true;
        }
        return false;
    }
}
