package SprintBot2;
import battlecode.common.*;

public class Politician extends Unit{

    MapLocation neutralTarget = null;

    public void AwakeData() throws GameActionException{
        super.AwakeData();
    }
    public void Awake() throws GameActionException{
        super.Awake();
        if (rc.getInfluence() >= 100 && baseId != -1 && rc.canGetFlag(baseId)){
            int f = rc.getFlag(baseId);
            if ((f & 7) == 4 && extractHealthNeutral(f) == rc.getID()%500){
                neutralTarget = extractLocationNeutral(f);
            }
        }
    }
    public void UpdateData() throws GameActionException{
        super.UpdateData();
        RobotController rc = this.rc;
    }
    public void Update() throws GameActionException{
        super.Update();
        /*if (exploring != null)
            explore();*/
        if (neutralTarget != null){
            captureNeutral();
        }
        else if (target != null){
            if (rc.getConviction() > 100)
                rushAsNuke();
            else
                rushAsSupport();
        }
        else {
            int countKills = 0;
            int furthest = 0;
            for (RobotInfo ri : allEnemies){
                if (ri.getLocation().isWithinDistanceSquared(position, 9) && ri.getInfluence() < 5){
                    countKills++;
                    furthest = Math.max(furthest, ri.getLocation().distanceSquaredTo(position));
                }
            }
            if (countKills >= 4 || (baseLocation != null && position.isWithinDistanceSquared(baseLocation, 40) && countKills >= 1)){
                tryEmpower(furthest);
            }
            explore();
        }
        //System.out.println("flag: " + rc.getFlag(rc.getID()));
        

    }

    public void captureNeutral() throws GameActionException{
        RobotController rc = this.rc;
        if (rc.getLocation().isWithinDistanceSquared(neutralTarget, 8)){
            for (RobotInfo ri : allNeutrals){
                if (ri.getLocation().isWithinDistanceSquared(neutralTarget,8))
                    neutralTarget = ri.getLocation();
            }
            RobotInfo obj = rc.senseRobotAtLocation(neutralTarget);
            if ((obj == null && neutralTarget.isWithinDistanceSquared(position, 1)) || (obj != null && obj.getTeam() != Team.NEUTRAL)) {
                flagTurns = 3;
                rc.setFlag(flagNeutralBase(neutralTarget, 520));
                neutralTarget = null;
                return;
            }
            if (obj != null) {
                int health = obj.getConviction();
                double damage = rc.getConviction() * rc.getEmpowerFactor(rc.getTeam(), 0) - 11;
                int targetCount = 0;
                for (RobotInfo ri : rc.senseNearbyRobots(position.distanceSquaredTo(neutralTarget))) {
                    targetCount++;
                }
                if (damage / targetCount > health || targetCount == 1)
                    tryEmpower(position.distanceSquaredTo(neutralTarget));
            }
        }
        pathfindTo(neutralTarget);
    }

    public void rushAsSupport() throws GameActionException{
        RobotController rc = this.rc;
        int countKills = 0;
        int furthest = 0;
        boolean strongerThanUs = false;
        for (RobotInfo ri : allEnemies){
            if (ri.getType() == RobotType.ENLIGHTENMENT_CENTER){
                if (ri.getLocation().isWithinDistanceSquared(target, 25)){
                    target = ri.getLocation();
                    break;
                }
                if (ri.getLocation().isWithinDistanceSquared(position, 9)){
                    strongerThanUs = true;
                    furthest = Math.max(furthest, ri.getLocation().distanceSquaredTo(position));
                }
            }
            else if (ri.getType() == RobotType.POLITICIAN && ri.getConviction() > rc.getConviction() && ri.getLocation().isWithinDistanceSquared(position, 9)){
                strongerThanUs = true;
                furthest = Math.max(furthest, ri.getLocation().distanceSquaredTo(position));
            }
            else if (ri.getLocation().isWithinDistanceSquared(position, 9) && ri.getInfluence() < 5){
                countKills++;
                furthest = Math.max(furthest, ri.getLocation().distanceSquaredTo(position));
            }
        }
        if (countKills >= 4 || (baseLocation != null && position.isWithinDistanceSquared(baseLocation, 40) && countKills >= 1)){
            tryEmpower(furthest);
        }
        for (RobotInfo ri : allNeutrals){
            if (ri.getLocation().isWithinDistanceSquared(target, 25)){
                target = ri.getLocation();
                break;
            }
            if (ri.getLocation().isWithinDistanceSquared(position, 9)){
                strongerThanUs = true;
                furthest = Math.max(furthest, ri.getLocation().distanceSquaredTo(position));
            }
        }

        //System.out.println("target: " + target);
        if (position.isAdjacentTo(target)){
            RobotInfo ri = rc.senseRobotAtLocation(target);
            //System.out.println("check");
            if (ri == null || ri.getType() != RobotType.ENLIGHTENMENT_CENTER || ri.getTeam() == rc.getTeam()){
                rc.setFlag(flagResetAttack());
                flagTurns = 2;
                //System.out.println("flaging end attack");
                return;
            }
        }
        if (strongerThanUs && rc.getConviction()*(rc.getEmpowerFactor(rc.getTeam(),0)) > rc.getConviction()){
            tryEmpower(furthest);
        }
        if (countKills >= 4)
            tryEmpower(furthest);
        //System.out.println("last part of rush as support");
        if (position.isWithinDistanceSquared(target, 15)){
            for(RobotInfo ri : rc.senseNearbyRobots(1, rc.getTeam())){
                if (ri.getType() == RobotType.POLITICIAN && ri.getInfluence() > 100){
                    //System.out.println("check if im an obstacle");
                    if (countKills >= 2 || ri.getLocation().isWithinDistanceSquared(target, 1))
                        tryEmpower(furthest);
                    for (Direction dir : Direction.values()){
                        if (!position.add(dir).isWithinDistanceSquared(ri.getLocation(), 1) && rc.canMove(dir))
                            rc.move(dir);
                    }
                }
            }
        }
        pathfindTo(target);
    }

    public void rushAsNuke() throws GameActionException{
        for (RobotInfo ri : allEnemies){
            if (ri.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                if (ri.getLocation().isWithinDistanceSquared(target, 25)) {
                    target = ri.getLocation();
                    break;
                }
            }
        }
        for (RobotInfo ri : allNeutrals){
            if (ri.getLocation().isWithinDistanceSquared(target, 25)) {
                target = ri.getLocation();
                break;
            }
        }
        //System.out.println("target: " + target);
        if (position.isAdjacentTo(target)){
            RobotInfo ri = rc.senseRobotAtLocation(target);
            //System.out.println("check");
            if (ri == null || ri.getType() != RobotType.ENLIGHTENMENT_CENTER || ri.getTeam() == rc.getTeam()){
                rc.setFlag(flagResetAttack());
                flagTurns = 2;
                //System.out.println("flaging end attack");
                return;
            }
        }
        if (rc.canSenseLocation(target)) {
            RobotInfo objective = rc.senseRobotAtLocation(target);
            if (objective != null && objective.getType() == RobotType.ENLIGHTENMENT_CENTER && objective.getTeam() != rc.getTeam()) {
                int health = objective.getConviction();
                double damage = rc.getConviction() * rc.getEmpowerFactor(rc.getTeam(), 0) - 11;
                int targetCount = 0;
                for (RobotInfo ri : rc.senseNearbyRobots(position.distanceSquaredTo(target))) {
                    targetCount++;
                }
                if (damage / targetCount > health || targetCount == 1)
                    tryEmpower(position.distanceSquaredTo(target));
            }
        }
        if (!position.isWithinDistanceSquared(target, 1)){
            pathfindTo(target);
        }
    }

    public void tryDupe() throws GameActionException{
        RobotController rc = this.rc;
        int baseId = this.baseId;
        int x = rc.getConviction();
        int margin = 50;
        if (rc.getEmpowerFactor(rc.getTeam(), 0) * x - 10 > x + margin) {
            if (position.isWithinDistanceSquared(baseLocation, 1)) {
                int hitCount = 0;
                if (rc.senseRobotAtLocation(position.add(Direction.NORTH)) != null) hitCount++;
                if (rc.senseRobotAtLocation(position.add(Direction.SOUTH)) != null) hitCount++;
                if (rc.senseRobotAtLocation(position.add(Direction.EAST)) != null) hitCount++;
                if (rc.senseRobotAtLocation(position.add(Direction.WEST)) != null) hitCount++;
                if ((rc.getEmpowerFactor(rc.getTeam(), 0) * x - 10) / hitCount > x + margin && rc.canSenseRobot(baseId)
                        && rc.getEmpowerFactor(rc.getTeam(), 0) * x - 10 + rc.senseRobot(baseId).getInfluence() < 1000000000) {
                    tryEmpower(1);
                }
            } else if (position.isWithinDistanceSquared(baseLocation, 40)) {
                pathfindTo(baseLocation);
            }
        }
    }

    public boolean tryEmpower(int x) throws GameActionException{
        RobotController rc = this.rc;
        if (rc.canEmpower(x)){
            rc.empower(x);
            return true;
        }
        return false;
    }
}
