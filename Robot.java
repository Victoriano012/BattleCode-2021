package SprintBot2;
import battlecode.common.*;

public class Robot {
    RobotController rc;
    MapLocation position;
    RobotInfo[] allEnemies;
    RobotInfo[] allAllies;
    RobotInfo[] allNeutrals;
    MapLocation baseLocation;

    double dupingMargin = 100000000;

    public void run(RobotController rc) throws GameActionException{
        this.rc = rc;
        AwakeData();
        UpdateData();
        Awake();
        Update();
        Clock.yield();
        while (true){
            UpdateData();
            Update();
            Clock.yield();
        }
    }
    public void AwakeData() throws GameActionException{
        position = rc.getLocation();
    }
    public void Awake() throws GameActionException{

    }
    public void UpdateData() throws GameActionException{
        RobotController rc = this.rc;
        allEnemies = rc.senseNearbyRobots(1000, rc.getTeam().opponent());
        allAllies = rc.senseNearbyRobots(1000, rc.getTeam());
        allNeutrals = rc.senseNearbyRobots(1000, Team.NEUTRAL);
    }
    public void Update() throws GameActionException{

    }

    MapLocation toMapLocation(int x, int y){
        int px = position.x;
        int py = position.y;
        x += (px & 0xFFFFFF80); // x += px/128*128;
        y += (py & 0xFFFFFF80); // y += py/128*128;
        if (x - px >= 64)
            x -= 128;
        if (y - py >= 64)
            y -= 128;
        if (px - x >= 64)
            x += 128;
        if (py - y >= 64)
            y += 128;
        return new MapLocation(x, y);
    }

    int flagAttack(MapLocation attackLoc){
        return ((attackLoc.y % 128) << 10) | ((attackLoc.x % 128) << 3) | 3;
    }
    int flagEnemyBaseHealth(int enemyBaseHealth){
        return (enemyBaseHealth << 3) | 7;
    }
    int flagResetAttack(){
        return 1;
    }
    int flagLocation(MapLocation loc) {
        return ((loc.y % 128) << 10) | ((loc.x % 128) << 3) | 0; 
    }
    int flagNeutralBase(MapLocation loc, int health){
        //System.out.println("flagging neutral at " + loc + " with health " + health);
        return ((health << 13) | ((loc.y%128)/4 << 8) | ((loc.x%128)/4 << 3) | 5);
    }
    MapLocation extractLocation(int flag){
        flag >>= 3;
        int x = flag % 128;
        flag >>= 7;
        int y = flag % 128;
        return toMapLocation(x, y);
    }
    MapLocation extractLocationNeutral(int flag){
        flag >>= 3;
        int x = (flag%32)*4+1;
        flag >>= 5;
        int y = (flag%32)*4+1;
        return toMapLocation(x, y);
    }
    int extractHealthNeutral(int flag){
        flag >>= 13;
        return flag;
    }
    int extractEnemyBaseHealth(int flag){
        return flag >> 3;
    }
}
