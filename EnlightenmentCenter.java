package SprintBot2;
import battlecode.common.*;

public class EnlightenmentCenter extends Robot
{
    int roundsSinceSlanderer = 0;

    int[] unitIDs = new int[1500];
    int[] unitFlags = new int[1500];
    int roundsSinceNuke = 0;

    boolean duping = false;

    boolean [][] isSectorExplored = new boolean[32][32];
    int [][] sectorDeaths = new int[32][32];

    boolean muckrakerAround = false;

    int unitIDCount = 0;

    MapLocation target = null;
    int targetHealth = 0;

    MapLocation neutralTarget;
    int neutralTargetHealth = 0;
    int neutralNukeId = 0;

    public void AwakeData() throws GameActionException
    {
        super.AwakeData();
        baseLocation = position;
    }
    public void Awake() throws GameActionException
    {
        super.Awake();
        tryBuildRobotAnywhere(RobotType.SLANDERER,130);
    }
    public void UpdateData() throws GameActionException
    {
        /*if (rc.getRoundNum() > 150)
            rc.resign();*/
        super.UpdateData();

        roundsSinceSlanderer++;

        int currErr = 0;

        muckrakerAround = false;
        for (RobotInfo ri : allEnemies){
            if (ri.getType() == RobotType.MUCKRAKER){
                muckrakerAround = true;
                break;
            }
        }

        duping = false;
        for (RobotInfo ri : allAllies){
            if (ri.getType() == RobotType.POLITICIAN && ri.getInfluence() > 20*rc.getInfluence()){
                duping = true;
                break;
            }
        }

        for(int i = 0; i < unitIDCount; i++) {
            int id = unitIDs[i];
            if(id < 9000){ 
                if(i > 1 && unitIDs[i - 1] < 9000){
                    unitIDs[i - 1] = id + 1;
                }
                i += id;
            }
            else {
                if(rc.canGetFlag(id)) {
                    int f = rc.getFlag(id);
                    switch (f&7){
                        case 0:
                            unitFlags[i] = f;
                            break;
                        case 1:
                            //System.out.println("abort attack flag " + f + " from " + id);
                            rc.setFlag(0);
                            target = null;
                            targetHealth = 0;
                            break;
                        case 5:
                            if (neutralTarget != null){
                                MapLocation loc = extractLocationNeutral(f);
                                if (position.distanceSquaredTo(neutralTarget) > position.distanceSquaredTo(loc)){
                                    neutralTarget = loc;
                                    neutralTargetHealth = extractHealthNeutral(f);
                                }
                                else if (neutralTarget.isWithinDistanceSquared(loc, 8) && extractHealthNeutral(f) == 520){
                                    neutralTarget = null;
                                    neutralNukeId = 0;
                                    neutralTargetHealth = 0;
                                }
                            }
                            else{
                                neutralTarget = extractLocationNeutral(f);
                                neutralTargetHealth = extractHealthNeutral(f);
                            }
                            break;
                        case 3:
                            //System.out.println("attack flag "+ f+" from " + id);
                            //System.out.println(target);
                            if (target != null){
                                MapLocation loc = extractLocation(f);
                                if (position.distanceSquaredTo(target) > position.distanceSquaredTo(loc)){
                                    target = loc;
                                    rc.setFlag(f);
                                }
                            }
                            else{
                                target = extractLocation(f);
                                rc.setFlag(f);
                            }
                            break;
                        case 7:
                            //.out.println("health flag "+ f+" from " + id);
                            targetHealth = extractEnemyBaseHealth(f);
                            break;
                    }

                }
                else {
                    unitIDs[i] = 0;
                    if(i == 0 || unitIDs[i - 1] < 9000) {               
                        currErr = i;
                    }
                    else {
                        unitIDs[currErr]++;
                    }
                }
            }
        }
    }
    public void Update() throws GameActionException
    {
        super.Update();
        if (rc.getInfluence() < 100000000) {
            tryDupe();
        }
        //System.out.println("target: " + target);
        //System.out.println("boost: " + rc.getEmpowerFactor(rc.getTeam(), 0));
        /*if (rc.getRoundNum() > 500)
            rc.resign();*/
        /*if (neutralTarget != null)
            rc.setIndicatorDot(neutralTarget, 0,0,0);*/
        if (neutralNukeId > 0 && !rc.canGetFlag(neutralNukeId)){
            neutralNukeId = 0;
            neutralTarget = null;
            neutralTargetHealth = 0;
        }
        if (roundsSinceSlanderer > 50 && !muckrakerAround && rc.getInfluence() > 100){
            if (tryBuildRobotAnywhere(RobotType.SLANDERER, Math.min(rc.getInfluence()/2, 300)))
                roundsSinceSlanderer = 0;
        }
        roundsSinceNuke++;
        if (rc.getEmpowerFactor(rc.getTeam(), 0) < dupingMargin && !duping) {
            if (muckrakerAround) {
                //System.out.println("muckraker around");
                tryBuildRobotAnywhere(RobotType.POLITICIAN, Math.min(100,Math.max(20, rc.getInfluence()/200)));
            }
            if (target != null && roundsSinceNuke > 40) {
                //System.out.println("target health: " + targetHealth);
                //System.out.println("has target");
                if (tryBuildRobotAnywhere(RobotType.POLITICIAN, Math.max(Math.max(targetHealth, 101), rc.getInfluence() / 20)))
                    roundsSinceNuke = 0;
            }
            if (neutralTarget != null && neutralNukeId == 0){
                //System.out.println("trying to attack neutral");
                if (tryBuildRobotAnywhere(RobotType.POLITICIAN, Math.max(101, Math.max(neutralTargetHealth+11,rc.getInfluence()/20)))){
                    //System.out.println("sent to " + neutralTarget + " with health " + neutralTargetHealth);
                    neutralNukeId = unitIDs[unitIDCount-1];
                    rc.setFlag(flagNeutralBase(neutralTarget, neutralNukeId%500) ^ 1);
                    Clock.yield();
                    Clock.yield();
                }
            }
            //else {
                //System.out.println("not much");
                int k = (int) (Math.random() * 10);
                if (k == 0 && rc.getConviction() > rc.getRoundNum())
                    tryBuildRobotAnywhere(RobotType.POLITICIAN, Math.min(100,Math.max(20, rc.getInfluence()/200)));
                else
                    tryBuildRobotAnywhere(RobotType.MUCKRAKER, 1);
            //}
        }

        // explorers/attacc

        if (rc.getRoundNum() > 700){
            tryBid(Math.max(1, rc.getInfluence()/1000));
        }
        /*int leftOver = rc.getInfluence() - rc.getRoundNum() / 2;
        if(leftOver > 0){
            leftOver = Math.min(leftOver, 4);
            tryBid(leftOver);
        }*/
    }

    public boolean tryDupe() throws GameActionException{
        RobotController rc = this.rc;
        if (rc.getEmpowerFactor(rc.getTeam(), 20) > dupingMargin) {
            int x = rc.getInfluence() - 20;
            int margin = 50;
            double fact = rc.getEmpowerFactor(rc.getTeam(), 20);
            x = Math.min(x, (int) ((900000000 - rc.getInfluence()) / fact));
            if (rc.getEmpowerFactor(rc.getTeam(), 11) * x > x + 10 + margin) {
                tryBuildRobotStraight(RobotType.POLITICIAN, x);
                return true;
            }
        }
        return false;
    }

    public boolean tryBuildRobotStraight(RobotType rt, int cost) throws GameActionException{
        RobotController rc = this.rc;
        if (rc.canBuildRobot(rt, Direction.NORTH, cost)){
            rc.buildRobot(rt, Direction.NORTH, cost);
            return true;
        }
        if (rc.canBuildRobot(rt, Direction.EAST, cost)){
            rc.buildRobot(rt, Direction.EAST, cost);
            return true;
        }
        if (rc.canBuildRobot(rt, Direction.SOUTH, cost)){
            rc.buildRobot(rt, Direction.SOUTH, cost);
            return true;
        }
        if (rc.canBuildRobot(rt, Direction.WEST, cost)){
            rc.buildRobot(rt, Direction.WEST, cost);
            return true;
        }
        return false;
    }
    public boolean tryBuildRobot(RobotType rt, Direction dir, int cost) throws GameActionException{
        RobotController rc = this.rc;
        if (rc.canBuildRobot(rt, dir, cost)){
            rc.buildRobot(rt, dir, cost);
            RobotInfo ri = rc.senseRobotAtLocation(position.add(dir));
            unitIDs[unitIDCount] = ri.getID();
            unitIDCount++;
            return true;
        }
        return false;
    }
    public boolean tryBuildRobotAnywhere(RobotType rt, int cost) throws GameActionException{
        for (Direction dir : Direction.values()){
            if (tryBuildRobot(rt, dir, cost))
                return true;
        }
        return false;
    }
    public boolean tryBid(int x) throws GameActionException{
        RobotController rc = this.rc;
        if (rc.canBid(x)) {
            rc.bid(x);
            return true;
        }
        return false;
    }
}
