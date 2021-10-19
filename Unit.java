package SprintBot2;
import battlecode.common.*;

public class Unit extends Robot
{
    int baseId = -1;
    int flagTurns = 0;
    double[][] pathfindingBestDistance = new double[13][13];
    double[][] pathfindingBestAdjacentDistance = new double[13][13];
    Direction[][] pathfindingBestDirection = new Direction[13][13];

    boolean[][] isSectorExplored = new boolean[32][32];
    boolean[][] containsBase = new boolean[32][32];
    MapLocation startingPosition;
    MapLocation target;
    MapLocation exploringLocation;

    public void AwakeData() throws GameActionException
    {
        super.AwakeData();
        RobotController rc = this.rc;
        startingPosition = rc.getLocation();
        for (RobotInfo rt : rc.senseNearbyRobots(2, rc.getTeam()))
        {
            if (rt.getType() == RobotType.ENLIGHTENMENT_CENTER){
                baseId = rt.getID();
                baseLocation = rt.getLocation();
                break;
            }
        }
    }
    public void Awake() throws GameActionException{
        super.Awake();
    }
    public void UpdateData() throws GameActionException{
        super.UpdateData();
        flagTurns--;
        RobotController rc = this.rc;
        position = rc.getLocation();
        boolean[][] isSectorExplored = this.isSectorExplored;
        boolean[][] containsBase = this.containsBase;
        MapLocation position = this.position;
        MapLocation startingPosition = this.startingPosition;

        isSectorExplored[(position.x - startingPosition.x + 64)/4][(position.y - startingPosition.y + 64)/4] = true;
        for (RobotInfo ri : allEnemies){
            if (ri.getType() == RobotType.ENLIGHTENMENT_CENTER){
                MapLocation enemyBaseLocation = ri.getLocation();
                containsBase[(enemyBaseLocation.x - startingPosition.x + 64)/4][(enemyBaseLocation.y - startingPosition.y + 64)/4] = true;
                if(baseId != -1 && rc.canGetFlag(baseId)) {
                    if (target != null && enemyBaseLocation.isWithinDistanceSquared(target, 25)) {

                        rc.setFlag(flagEnemyBaseHealth(ri.getInfluence()));
                        //System.out.println("flaging health: " + rc.getFlag(rc.getID()));
                        flagTurns = 2;
                    }
                    else {
                        rc.setFlag(flagAttack(enemyBaseLocation));
                        //System.out.println("ATACCC");
                        flagTurns = 5;
                    }
                }
                else{
                    baseId = -1;
                }
            }
        }
        for (RobotInfo ri : allAllies){
            if (ri.getType() == RobotType.ENLIGHTENMENT_CENTER){
                MapLocation loc = ri.getLocation();
                containsBase[(loc.x - startingPosition.x + 64)/4][(loc.y - startingPosition.y + 64)/4] = true;
            }
        }
        for (RobotInfo ri : allNeutrals){
            MapLocation enemyBaseLocation = ri.getLocation();
            containsBase[(enemyBaseLocation.x - startingPosition.x + 64)/4][(enemyBaseLocation.y - startingPosition.y + 64)/4] = true;
            if(baseId != -1 && rc.canGetFlag(baseId)) {
                rc.setFlag(flagNeutralBase(enemyBaseLocation, ri.getConviction()));
                flagTurns = 2;
            }
            else{
                baseId = -1;
            }
        }

        if (baseId != -1){
            if (rc.canGetFlag(baseId)) {
                int f = rc.getFlag(baseId);
                if ((f&1) == 1){
                    if (flagTurns <= 0)
                        rc.setFlag(f ^ 1);
                    target = extractLocation(f);
                }
                else if (f == 0){
                    target = null;
                    if (flagTurns <= 0)
                        rc.setFlag(flagLocation(position));
                }
            }
            else{
                baseId = -1;
            }
        }
        else{
            for (RobotInfo ri : allAllies){
                if (ri.getType() == RobotType.POLITICIAN){
                    int f = rc.getFlag(ri.getID());
                    if ((f & 3) == 2){
                        target = extractLocation(f);
                    }
                }
            }
        }
    }
    public void Update() throws GameActionException{
        super.Update();
    }

    public void explore() throws GameActionException{
        RobotController rc = this.rc;
        MapLocation position = rc.getLocation();
        if(exploringLocation == null){
            for(int i = 0; i < 30; i++){
                int rx = (int)Math.floor(Math.random() * 32);
                int ry = (int)Math.floor(Math.random() * 32);

                if(!isSectorExplored[rx][ry]){
                    int x = startingPosition.x + rx * 4 - 63;
                    int y = startingPosition.y + ry * 4 - 63;
                    exploringLocation = new MapLocation(x, y);
                    break;
                }
            }
        }

        if(exploringLocation != null) {
            if(position.isAdjacentTo(exploringLocation))
                exploringLocation = null;
            else{
                Direction dir = position.directionTo(exploringLocation);
                if (!rc.canSenseLocation(exploringLocation)){
                    int dx = dir.getDeltaX();
                    int dy = dir.getDeltaY();
                    if (dx != 0 && !rc.canSenseLocation(position.translate(3*dx,0))){
                        exploringLocation = null;
                        if (dx == 1){
                            int l = (position.x+7 - startingPosition.x + 64)/4;
                            for (int i = l; i < 32; i++){
                                for (int j = 0; j < 32; j++){
                                    isSectorExplored[i][j] = true;
                                }
                            }
                        }
                        else{
                            int l = (position.x-7 - startingPosition.x + 64)/4;
                            for (int i = l; i >= 0; i--){
                                for (int j = 0; j < 32; j++){
                                    isSectorExplored[i][j] = true;
                                }
                            }
                        }
                    }
                    if (dy != 0 && !rc.canSenseLocation(position.translate(0,3*dy))){
                        exploringLocation = null;
                        if (dy == 1){
                            int l = (position.y+7 - startingPosition.y + 64)/4;
                            for (int i = l; i < 32; i++){
                                for (int j = 0; j < 32; j++){
                                    isSectorExplored[j][i] = true;
                                }
                            }
                        }
                        else{
                            int l = (position.y-7 - startingPosition.y + 64)/4;
                            for (int i = l; i >= 0; i--){
                                for (int j = 0; j < 32; j++){
                                    isSectorExplored[j][i] = true;
                                }
                            }
                        }
                    }
                }
            }
            if (exploringLocation != null) {
                //rc.setIndicatorLine(position, exploringLocation, 0,0,0);
                if (rc.getEmpowerFactor(rc.getTeam(), 0) >= dupingMargin && baseLocation != null && position.isWithinDistanceSquared(baseLocation, 30)) {
                    Direction dirBase = position.directionTo(baseLocation);
                    Direction dir = position.directionTo(exploringLocation);
                    while (dir == dirBase || dir.rotateLeft() == dirBase || dir.rotateLeft().rotateLeft() == dirBase || dir.rotateRight() == dirBase || dir.rotateRight().rotateRight() == dirBase){
                        for (int i = 0; i < 30; i++) {
                            int rx = (int) Math.floor(Math.random() * 32);
                            int ry = (int) Math.floor(Math.random() * 32);

                            if (!isSectorExplored[rx][ry]) {
                                int x = startingPosition.x + rx * 4 - 63;
                                int y = startingPosition.y + ry * 4 - 63;
                                exploringLocation = new MapLocation(x, y);
                                break;
                            }
                        }
                        dir = position.directionTo(exploringLocation);
                    }
                }
                pathfindTo(exploringLocation);
            }
        }
        else{
            //System.out.println("everything explored");
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
    public boolean move3(Direction dir) throws GameActionException{
        return tryMove(dir) || tryMove(dir.rotateLeft()) || tryMove(dir.rotateRight());
    }
    public boolean move5(Direction dir) throws GameActionException{
        return tryMove(dir) || tryMove(dir.rotateLeft()) || tryMove(dir.rotateRight()) || tryMove(dir.rotateLeft().rotateLeft()) || tryMove(dir.rotateRight().rotateRight());
    }

    Direction[][][][] pathfindingD = {{{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{Direction.NORTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{Direction.WEST,Direction.NORTHWEST},{Direction.WEST,Direction.NORTHWEST},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.WEST,Direction.NORTH,Direction.NORTHEAST},{},{},{},{}},{{},{},{},{Direction.WEST},{Direction.WEST},{Direction.WEST,Direction.NORTHWEST},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{Direction.WEST},{},{},{}},{{},{},{},{Direction.WEST,Direction.EAST},{Direction.WEST,Direction.EAST},{Direction.WEST,Direction.EAST},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.WEST,Direction.EAST},{},{},{}},{{},{},{},{Direction.EAST},{Direction.EAST},{Direction.NORTHEAST,Direction.EAST},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.EAST},{},{},{}},{{},{},{},{},{Direction.NORTHEAST,Direction.EAST},{Direction.NORTHEAST,Direction.EAST},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.NORTHWEST,Direction.NORTH,Direction.EAST},{},{},{},{}},{{},{},{},{},{},{},{Direction.NORTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}}},
            {{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{Direction.NORTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{Direction.NORTHWEST,Direction.SOUTHEAST},{Direction.NORTHWEST},{Direction.NORTHWEST,Direction.NORTH},{Direction.NORTHWEST,Direction.NORTH},{Direction.NORTH,Direction.NORTHEAST},{},{},{},{}},{{},{},{},{},{Direction.SOUTHEAST},{Direction.NORTHWEST,Direction.SOUTHEAST},{Direction.NORTHWEST,Direction.NORTH},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{},{},{},{}},{{},{},{},{Direction.EAST},{Direction.EAST,Direction.SOUTHEAST},{Direction.EAST,Direction.SOUTHEAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.EAST},{},{},{}},{{},{},{},{Direction.EAST},{Direction.EAST,Direction.SOUTHEAST},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.EAST},{},{},{}},{{},{},{},{},{Direction.NORTHEAST,Direction.EAST},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTHWEST,Direction.NORTH,Direction.EAST,Direction.SOUTHEAST},{},{},{},{}},{{},{},{},{},{},{},{Direction.NORTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}}},
            {{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.NORTH,Direction.SOUTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{Direction.SOUTHEAST,Direction.SOUTH},{Direction.SOUTH},{Direction.NORTH,Direction.SOUTH},{Direction.NORTH},{Direction.NORTH,Direction.NORTHEAST},{},{},{},{}},{{},{},{},{},{Direction.SOUTHEAST,Direction.SOUTH},{Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTH,Direction.SOUTH},{Direction.NORTH,Direction.NORTHEAST},{Direction.NORTH,Direction.NORTHEAST},{},{},{},{}},{{},{},{},{Direction.EAST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.EAST},{},{},{}},{{},{},{},{Direction.EAST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST},{Direction.EAST},{},{},{}},{{},{},{},{},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTH},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTH,Direction.EAST,Direction.SOUTHEAST},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.NORTH,Direction.SOUTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}}},
            {{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.SOUTH},{},{},{},{},{},{}},{{},{},{},{},{Direction.SOUTHEAST,Direction.SOUTH},{Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTHWEST},{Direction.NORTHEAST,Direction.SOUTHWEST},{},{},{},{}},{{},{},{},{},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTH,Direction.SOUTHWEST},{Direction.NORTHEAST,Direction.SOUTHWEST},{Direction.NORTHEAST},{},{},{},{}},{{},{},{},{Direction.EAST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.NORTHEAST,Direction.EAST},{Direction.NORTHEAST,Direction.EAST},{Direction.EAST},{},{},{}},{{},{},{},{Direction.EAST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.NORTHEAST,Direction.EAST},{Direction.EAST},{},{},{}},{{},{},{},{},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST},{Direction.EAST,Direction.SOUTHEAST},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.SOUTH},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}}},
            {{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.SOUTH},{},{},{},{},{},{}},{{},{},{},{},{Direction.SOUTHEAST,Direction.SOUTH,Direction.WEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHWEST,Direction.WEST},{},{},{},{}},{{},{},{},{Direction.WEST},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHWEST,Direction.WEST},{Direction.WEST},{Direction.WEST},{},{},{}},{{},{},{},{Direction.EAST,Direction.WEST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.EAST,Direction.WEST},{Direction.EAST,Direction.WEST},{Direction.EAST,Direction.WEST},{},{},{}},{{},{},{},{Direction.EAST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.EAST,Direction.SOUTHEAST},{Direction.EAST},{Direction.EAST},{},{},{}},{{},{},{},{},{Direction.EAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH},{Direction.EAST,Direction.SOUTHEAST},{Direction.EAST,Direction.SOUTHEAST},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.SOUTH},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}}},
            {{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.SOUTH},{},{},{},{},{},{}},{{},{},{},{},{Direction.SOUTHEAST,Direction.SOUTH,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTHWEST,Direction.WEST},{},{},{},{}},{{},{},{},{Direction.WEST},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.WEST,Direction.NORTHWEST},{Direction.WEST},{},{},{}},{{},{},{},{Direction.WEST},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.WEST,Direction.NORTHWEST},{Direction.WEST,Direction.NORTHWEST},{Direction.WEST},{},{},{}},{{},{},{},{},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTHEAST,Direction.SOUTH},{Direction.SOUTHEAST,Direction.NORTHWEST},{Direction.NORTHWEST},{},{},{},{}},{{},{},{},{},{Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTHEAST,Direction.SOUTH},{Direction.SOUTHEAST,Direction.SOUTH},{Direction.SOUTHEAST},{Direction.SOUTHEAST,Direction.NORTHWEST},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.SOUTH},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}}},
            {{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.SOUTH,Direction.NORTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{Direction.SOUTH,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTH},{},{},{},{}},{{},{},{},{Direction.WEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.WEST},{},{},{}},{{},{},{},{Direction.WEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.WEST},{},{},{}},{{},{},{},{},{Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTH,Direction.NORTH},{Direction.NORTHWEST,Direction.NORTH},{Direction.NORTHWEST,Direction.NORTH},{},{},{},{}},{{},{},{},{},{Direction.SOUTH,Direction.SOUTHWEST},{Direction.SOUTH},{Direction.SOUTH,Direction.NORTH},{Direction.NORTH},{Direction.NORTHWEST,Direction.NORTH},{},{},{},{}},{{},{},{},{},{},{Direction.SOUTH},{Direction.SOUTH,Direction.NORTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}}},
            {{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{Direction.NORTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{Direction.WEST,Direction.NORTHWEST},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTH,Direction.NORTHEAST},{},{},{},{}},{{},{},{},{Direction.WEST},{Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{Direction.WEST},{},{},{}},{{},{},{},{Direction.WEST},{Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHWEST,Direction.WEST},{Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{Direction.WEST,Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{Direction.WEST},{},{},{}},{{},{},{},{},{Direction.SOUTHWEST},{Direction.SOUTHWEST,Direction.NORTHEAST},{Direction.NORTH,Direction.NORTHEAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{Direction.NORTHWEST,Direction.NORTH,Direction.NORTHEAST},{},{},{},{}},{{},{},{},{},{Direction.SOUTHWEST,Direction.NORTHEAST},{Direction.NORTHEAST},{Direction.NORTH,Direction.NORTHEAST},{Direction.NORTH,Direction.NORTHEAST},{Direction.NORTHWEST,Direction.NORTH},{},{},{},{}},{{},{},{},{},{},{},{Direction.NORTH},{Direction.NORTH},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}},{{},{},{},{},{},{},{},{},{},{},{},{},{}}}
    };

    public void pathfindTo(MapLocation target) throws GameActionException {
        if (!rc.isReady())
            return;
        //int r = rc.getRoundNum();
        //int b = Clock.getBytecodeNum();

        double[][] bestDistance = pathfindingBestDistance;
        double[][] bestAdjacentDistance = pathfindingBestAdjacentDistance;
        RobotController rc = this.rc;
        Direction dirToTarget = position.directionTo(target);
        Direction[][][] pathfindingDirs = pathfindingD[dirToTarget.ordinal()];

        MapLocation origin = rc.getLocation();

        int xOffset = origin.x - 6;
        int yOffset = origin.y - 6;

        QNode start = new QNode(null, Direction.CENTER, 100000000), end = start;

        //#region best distance init
        bestDistance[3][4] = 10000000;
        bestDistance[3][5] = 10000000;
        bestDistance[3][6] = 10000000;
        bestDistance[3][7] = 10000000;
        bestDistance[3][8] = 10000000;
        bestDistance[4][3] = 10000000;
        bestDistance[4][4] = 10000000;
        bestDistance[4][5] = 10000000;
        bestDistance[4][6] = 10000000;
        bestDistance[4][7] = 10000000;
        bestDistance[4][8] = 10000000;
        bestDistance[4][9] = 10000000;
        bestDistance[5][3] = 10000000;
        bestDistance[5][4] = 10000000;
        bestDistance[5][5] = 10000000;
        bestDistance[5][6] = 10000000;
        bestDistance[5][7] = 10000000;
        bestDistance[5][8] = 10000000;
        bestDistance[5][9] = 10000000;
        bestDistance[6][3] = 10000000;
        bestDistance[6][4] = 10000000;
        bestDistance[6][5] = 10000000;
        bestDistance[6][6] = 0;
        bestDistance[6][7] = 10000000;
        bestDistance[6][8] = 10000000;
        bestDistance[6][9] = 10000000;
        bestDistance[7][3] = 10000000;
        bestDistance[7][4] = 10000000;
        bestDistance[7][5] = 10000000;
        bestDistance[7][6] = 10000000;
        bestDistance[7][7] = 10000000;
        bestDistance[7][8] = 10000000;
        bestDistance[7][9] = 10000000;
        bestDistance[8][3] = 10000000;
        bestDistance[8][4] = 10000000;
        bestDistance[8][5] = 10000000;
        bestDistance[8][6] = 10000000;
        bestDistance[8][7] = 10000000;
        bestDistance[8][8] = 10000000;
        bestDistance[8][9] = 10000000;
        bestDistance[9][4] = 10000000;
        bestDistance[9][5] = 10000000;
        bestDistance[9][6] = 10000000;
        bestDistance[9][7] = 10000000;
        bestDistance[9][8] = 10000000;

        bestAdjacentDistance[3][4] = 10000000;
        bestAdjacentDistance[3][5] = 10000000;
        bestAdjacentDistance[3][6] = 10000000;
        bestAdjacentDistance[3][7] = 10000000;
        bestAdjacentDistance[3][8] = 10000000;
        bestAdjacentDistance[4][3] = 10000000;
        bestAdjacentDistance[4][4] = 10000000;
        bestAdjacentDistance[4][5] = 10000000;
        bestAdjacentDistance[4][6] = 10000000;
        bestAdjacentDistance[4][7] = 10000000;
        bestAdjacentDistance[4][8] = 10000000;
        bestAdjacentDistance[4][9] = 10000000;
        bestAdjacentDistance[5][3] = 10000000;
        bestAdjacentDistance[5][4] = 10000000;
        bestAdjacentDistance[5][5] = 0;
        bestAdjacentDistance[5][6] = 0;
        bestAdjacentDistance[5][7] = 0;
        bestAdjacentDistance[5][8] = 10000000;
        bestAdjacentDistance[5][9] = 10000000;
        bestAdjacentDistance[6][3] = 10000000;
        bestAdjacentDistance[6][4] = 10000000;
        bestAdjacentDistance[6][5] = 0;
        bestAdjacentDistance[6][6] = 0;
        bestAdjacentDistance[6][7] = 0;
        bestAdjacentDistance[6][8] = 10000000;
        bestAdjacentDistance[6][9] = 10000000;
        bestAdjacentDistance[7][3] = 10000000;
        bestAdjacentDistance[7][4] = 10000000;
        bestAdjacentDistance[7][5] = 0;
        bestAdjacentDistance[7][6] = 0;
        bestAdjacentDistance[7][7] = 0;
        bestAdjacentDistance[7][8] = 10000000;
        bestAdjacentDistance[7][9] = 10000000;
        bestAdjacentDistance[8][3] = 10000000;
        bestAdjacentDistance[8][4] = 10000000;
        bestAdjacentDistance[8][5] = 10000000;
        bestAdjacentDistance[8][6] = 10000000;
        bestAdjacentDistance[8][7] = 10000000;
        bestAdjacentDistance[8][8] = 10000000;
        bestAdjacentDistance[8][9] = 10000000;
        bestAdjacentDistance[9][4] = 10000000;
        bestAdjacentDistance[9][5] = 10000000;
        bestAdjacentDistance[9][6] = 10000000;
        bestAdjacentDistance[9][7] = 10000000;
        bestAdjacentDistance[9][8] = 10000000;
        //#endregion
        //rc.setIndicatorLine(origin, target, 0, 255, 0);
        for(Direction d : pathfindingDirs[6][6])
        {
            //rc.setIndicatorLine(origin, origin.add(d), 0, 0, 0);
            if(rc.canMove(d))
            {
                MapLocation loc = origin.add(d);
                QNode node = new QNode(loc, d, 0);
                end.next = node;
                end = node;
            }
        }

        bestDistance[6][6] = 0;

        Direction minDir = Direction.CENTER;
        double minDistToTarget = 1000000000;
        MapLocation minLoc = null;

        while(start != end)
        {
            //Clock.yield();
            start = start.next;

            //rc.setIndicatorDot(origin, 0, 255, 255);
            //rc.setIndicatorDot(target, 255, 170, 170);
            //Clock.yield();
            MapLocation loc = start.loc;
            double distance = start.distance;
            int arrX = loc.x - xOffset, arrY = loc.y - yOffset;

            if(bestDistance[arrX][arrY] > distance && loc.isWithinDistanceSquared(origin, 14))
            {
                bestDistance[arrX][arrY] = distance;

                Direction dir = start.dir;

                double newDistance = distance + 1 / rc.sensePassability(loc);

                //rc.setIndicatorDot(loc, 0, 0, 0);

                if(!loc.equals(target))
                {
                    for(Direction newDir : pathfindingDirs[arrX][arrY])
                    {
                        MapLocation newLoc = loc.add(newDir);

                        int newX = newLoc.x - xOffset;
                        int newY = newLoc.y - yOffset;

                        if(bestAdjacentDistance[newX][newY] > newDistance && rc.canSenseLocation(newLoc))
                        {
                            //Clock.yield();
                            bestAdjacentDistance[newX][newY] = newDistance;
                            end.next = new QNode(newLoc, dir, newDistance);
                            end = end.next;
                            //rc.setIndicatorDot(origin, 0, 255, 0);
                            //rc.setIndicatorDot(newLoc, 0, 0, 255);

                            //Clock.yield();
                        }
                    }
                }
                if (!origin.isWithinDistanceSquared(loc, 9))
                {
                    double dist = newDistance + 10*(double)octalDistance(loc, target);  // loc.distanceSquaredTo(target) /
                    if(dist < minDistToTarget || loc.equals(target)){
                        minDistToTarget = dist;
                        minDir = dir;
                        minLoc = loc;
                        if (loc.equals(target))
                            minDistToTarget = 0;
                        //System.out.println("new best: " + minDir + ", new minDist: " + minDistToTarget + ", bestLoc: " + loc);
                    }
                }
            }
        }

        //int b2 = Clock.getBytecodeNum();
        //int r2 = rc.getRoundNum();
        //System.out.println("bytecode: " + (b2 - b + (r2-r)*15000));
        //rc.setIndicatorDot(origin.add(minDir), 255, 0, 0);
        if(minDir == Direction.CENTER)
            return;

        //System.out.println("Absolute Best: " + bestDir + ", minDist: " + minDistToTarget);
        //rc.setIndicatorLine(origin, minLoc, 255, 255, 0);

        tryMove(minDir);
    }

    /*public void pathfindTo(MapLocation target) throws GameActionException {
        if (!rc.isReady())
            return;
        int b = Clock.getBytecodeNum();

        double[][] bestDistance = pathfindingBestDistance;
        double[][] bestAdjacentDistance = pathfindingBestAdjacentDistance;
        RobotController rc = this.rc;

        Direction[] pathfindingDirs = new Direction[5];

        MapLocation origin = rc.getLocation();

        Direction targetDir = origin.directionTo(target);

        pathfindingDirs[0] = targetDir;
        pathfindingDirs[1] = targetDir.rotateLeft();
        pathfindingDirs[2] = targetDir.rotateLeft().rotateLeft();
        pathfindingDirs[3] = targetDir.rotateRight();
        pathfindingDirs[4] = targetDir.rotateRight().rotateRight();
        
        int xOffset = origin.x - 6;
        int yOffset = origin.y - 6;

        QNode start = new QNode(null, Direction.CENTER, 100000000), end = start;

        //#region best distance init
        bestDistance[3][4] = 10000000;
        bestDistance[3][5] = 10000000;
        bestDistance[3][6] = 10000000;
        bestDistance[3][7] = 10000000;
        bestDistance[3][8] = 10000000;
        bestDistance[4][3] = 10000000;
        bestDistance[4][4] = 10000000;
        bestDistance[4][5] = 10000000;
        bestDistance[4][6] = 10000000;
        bestDistance[4][7] = 10000000;
        bestDistance[4][8] = 10000000;
        bestDistance[4][9] = 10000000;
        bestDistance[5][3] = 10000000;
        bestDistance[5][4] = 10000000;
        bestDistance[5][5] = 10000000;
        bestDistance[5][6] = 10000000;
        bestDistance[5][7] = 10000000;
        bestDistance[5][8] = 10000000;
        bestDistance[5][9] = 10000000;
        bestDistance[6][3] = 10000000;
        bestDistance[6][4] = 10000000;
        bestDistance[6][5] = 10000000;
        bestDistance[6][6] = 0;
        bestDistance[6][7] = 10000000;
        bestDistance[6][8] = 10000000;
        bestDistance[6][9] = 10000000;
        bestDistance[7][3] = 10000000;
        bestDistance[7][4] = 10000000;
        bestDistance[7][5] = 10000000;
        bestDistance[7][6] = 10000000;
        bestDistance[7][7] = 10000000;
        bestDistance[7][8] = 10000000;
        bestDistance[7][9] = 10000000;
        bestDistance[8][3] = 10000000;
        bestDistance[8][4] = 10000000;
        bestDistance[8][5] = 10000000;
        bestDistance[8][6] = 10000000;
        bestDistance[8][7] = 10000000;
        bestDistance[8][8] = 10000000;
        bestDistance[8][9] = 10000000;
        bestDistance[9][4] = 10000000;
        bestDistance[9][5] = 10000000;
        bestDistance[9][6] = 10000000;
        bestDistance[9][7] = 10000000;
        bestDistance[9][8] = 10000000;

        bestAdjacentDistance[3][4] = 10000000;
        bestAdjacentDistance[3][5] = 10000000;
        bestAdjacentDistance[3][6] = 10000000;
        bestAdjacentDistance[3][7] = 10000000;
        bestAdjacentDistance[3][8] = 10000000;
        bestAdjacentDistance[4][3] = 10000000;
        bestAdjacentDistance[4][4] = 10000000;
        bestAdjacentDistance[4][5] = 10000000;
        bestAdjacentDistance[4][6] = 10000000;
        bestAdjacentDistance[4][7] = 10000000;
        bestAdjacentDistance[4][8] = 10000000;
        bestAdjacentDistance[4][9] = 10000000;
        bestAdjacentDistance[5][3] = 10000000;
        bestAdjacentDistance[5][4] = 10000000;
        bestAdjacentDistance[5][5] = 0;
        bestAdjacentDistance[5][6] = 0;
        bestAdjacentDistance[5][7] = 0;
        bestAdjacentDistance[5][8] = 10000000;
        bestAdjacentDistance[5][9] = 10000000;
        bestAdjacentDistance[6][3] = 10000000;
        bestAdjacentDistance[6][4] = 10000000;
        bestAdjacentDistance[6][5] = 0;
        bestAdjacentDistance[6][6] = 0;
        bestAdjacentDistance[6][7] = 0;
        bestAdjacentDistance[6][8] = 10000000;
        bestAdjacentDistance[6][9] = 10000000;
        bestAdjacentDistance[7][3] = 10000000;
        bestAdjacentDistance[7][4] = 10000000;
        bestAdjacentDistance[7][5] = 0;
        bestAdjacentDistance[7][6] = 0;
        bestAdjacentDistance[7][7] = 0;
        bestAdjacentDistance[7][8] = 10000000;
        bestAdjacentDistance[7][9] = 10000000;
        bestAdjacentDistance[8][3] = 10000000;
        bestAdjacentDistance[8][4] = 10000000;
        bestAdjacentDistance[8][5] = 10000000;
        bestAdjacentDistance[8][6] = 10000000;
        bestAdjacentDistance[8][7] = 10000000;
        bestAdjacentDistance[8][8] = 10000000;
        bestAdjacentDistance[8][9] = 10000000;
        bestAdjacentDistance[9][4] = 10000000;
        bestAdjacentDistance[9][5] = 10000000;
        bestAdjacentDistance[9][6] = 10000000;
        bestAdjacentDistance[9][7] = 10000000;
        bestAdjacentDistance[9][8] = 10000000;
        //#endregion

        for(Direction d : pathfindingDirs)
        {
            if(rc.canMove(d))
            {
                MapLocation loc = origin.add(d);
                QNode node = new QNode(loc, d, 0);
                end.next = node;
                end = node;
            }
        }

        bestDistance[6][6] = 0;

        Direction minDir = Direction.CENTER;
        double minDistToTarget = 1000000000;
        MapLocation minLoc = null;

        while(start != end)
        {
            //Clock.yield();
            start = start.next;

            //rc.setIndicatorDot(origin, 0, 255, 255);
            //rc.setIndicatorDot(target, 255, 170, 170);
            //Clock.yield();
            MapLocation loc = start.loc;
            double distance = start.distance;
            int arrX = loc.x - xOffset, arrY = loc.y - yOffset;

            if(bestDistance[arrX][arrY] > distance && loc.isWithinDistanceSquared(origin, 14))
            {
                bestDistance[arrX][arrY] = distance;

                Direction dir = start.dir;    
                
                double newDistance = distance + 1 / rc.sensePassability(loc);
                
                //rc.setIndicatorDot(loc, 0, 0, 0);

                if(loc.isWithinDistanceSquared(origin, 9) && !loc.equals(target))
                {
                    for(Direction newDir : pathfindingDirs)
                    {
                        MapLocation newLoc = loc.add(newDir);
        
                        int newX = newLoc.x - xOffset;
                        int newY = newLoc.y - yOffset;
        
                        if(bestAdjacentDistance[newX][newY] > newDistance && rc.canSenseLocation(newLoc))
                        {
                            //Clock.yield();
                            bestAdjacentDistance[newX][newY] = newDistance;
                            end.next = new QNode(newLoc, dir, newDistance);
                            end = end.next;
                            //rc.setIndicatorDot(origin, 0, 255, 0);
                            //rc.setIndicatorDot(newLoc, 0, 0, 255);

                            //Clock.yield();
                        }
                    }
                }
                else
                {
                    double dist = newDistance + 10*(double)octalDistance(loc, target);  // loc.distanceSquaredTo(target) /
                    if(dist < minDistToTarget || loc.equals(target)){
                        minDistToTarget = dist;
                        minDir = dir;
                        minLoc = loc;
                        if (loc.equals(target))
                            minDistToTarget = 0;
                        //System.out.println("new best: " + minDir + ", new minDist: " + minDistToTarget + ", bestLoc: " + loc);
                    }
                }
            }
        }

        int b2 = Clock.getBytecodeNum();
        System.out.println("bytecode: " + (b2 - b));

        if(minDir == Direction.CENTER)
            return;
    
        //System.out.println("Absolute Best: " + bestDir + ", minDist: " + minDistToTarget);
        //rc.setIndicatorLine(origin, minLoc, 255, 255, 0);

        tryMove(minDir);
    }*/


    
    int octalDistance(MapLocation a, MapLocation b)
    {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));      
    }
    int octalDistance(int Ax, int Ay, int Bx, int By)
    {
        return Math.max(Math.abs(Ax - Bx), Math.abs(Ay - By));        
    }
    int octalDistance(int x, int y)
    {
        return Math.max(x, y);        
    }
}
