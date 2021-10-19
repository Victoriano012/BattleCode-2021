package SprintBot2;

import battlecode.common.*;

import java.lang.*;

public class Comms {
    RobotController rc;
    Robot me;
    public int[][] baseInfo = new int[32][32]; //igual que la flag
    public int[][] baseHp = new int[32][32];
    public int[][] enemyInfo = new int[32][32]; //igual que la flag
    public int[][] explored = new int[32][32];
    public int northSector = -1, eastSector = -1, southSector = -1, westSector = -1;
    public StringBuffer knownIds = new StringBuffer("");
    public StringBuffer baseIds = new StringBuffer("");
    public StringBuffer info = new StringBuffer("");
    public StringBuffer newInfo = new StringBuffer("");

    public Comms(RobotController rc, Robot me){
        this.rc = rc;
        this.me = me;
    }

    public void UpdateAll() throws GameActionException{
        readEverything();
        updateInfo();
        updateFlag();
    }

    public void readEverything() throws GameActionException{
        RobotController rc = this.rc;
        RobotInfo[] allAllies = me.allAllies;
        if (rc.getType() == RobotType.ENLIGHTENMENT_CENTER){
            StringBuffer knownIds = this.knownIds;
            for (RobotInfo ri : allAllies){
                String s = "x" + ri.getID();
                if (knownIds.indexOf(s) == -1)
                    knownIds.append(s);
            }
            int i = 0;
            while (i < knownIds.length()){
                int j = knownIds.indexOf("x", i+1);
                if (j == -1){
                    j = knownIds.length();
                }
                int id = Integer.parseInt(knownIds.substring(i+1, j));
                if (rc.canGetFlag(id)){
                    readFlag(rc.getFlag(id));
                    i = j;
                }
                else{
                    knownIds.delete(i, j);
                }
            }
        }
        else{
            StringBuffer baseIds = this.baseIds;
            for (RobotInfo ri : allAllies){
                int id = ri.getID();
                if (rc.canGetFlag(id)){
                    readFlag(rc.getFlag(id));
                }
            }
            int i = 0;
            while (i < baseIds.length()){
                int j = baseIds.indexOf("x", i+1);
                if (j == -1){
                    j = baseIds.length();
                }
                int id = Integer.parseInt(baseIds.substring(i+1, j));
                if (rc.canGetFlag(id)){
                    readFlag(rc.getFlag(id));
                    i = j;
                }
                else{
                    baseIds.delete(i, j);
                }
            }
        }
    }

    void readFlag(int f){
        int[][] baseInfo = this.baseInfo;
        int[][] baseHp = this.baseHp;
        int[][] enemyInfo = this.enemyInfo;
        int[][] explored = this.explored;
        StringBuffer knownIds = this.knownIds;
        StringBuffer baseIds = this.baseIds;
        StringBuffer info = this.info;
        StringBuffer newInfo = this.newInfo;
        int i, prevF;
        boolean useful;
        StringBuffer s;
        int x, y;
        switch (f&15){
            case 0:
                i = extractId(f);
                if (i > 0) {
                    s = new StringBuffer("x" + i);
                    if (baseIds.indexOf(s.toString()) != -1) {
                        baseIds.append(s);
                        knownIds.append(s);
                        String s2 = "x" + f;
                        newInfo.append(s2);
                        info.append(s2);
                    }
                }
                break;
            case 1:
                i = extractId(f);
                s = new StringBuffer("x" + i);
                if (knownIds.indexOf(s.toString()) != -1){
                    knownIds.append(s);
                }
                break;
            case 2:
                prevF = buildFlagBorderNS(northSector, southSector);
                useful = false;
                if (northSector == -1){
                    i = extractBorder1(f);
                    if (i != -1){
                        northSector = i;
                        useful = true;
                    }
                }
                else
                    f = (f | (northSector << 19) | 0x40000);
                if (southSector == -1){
                    i = extractBorder2(f);
                    if (i != -1){
                        southSector = i;
                        useful = true;
                    }
                }
                else
                    f = (f | (southSector << 13) | 0x1000);
                if (useful){
                    String s2 = "x" + prevF;
                    int idx = newInfo.indexOf(s2);
                    if (idx >= 0)
                        newInfo.delete(idx, s2.length());
                    String s3 = "x" + f;
                    newInfo.append(s3);
                    idx = info.indexOf(s2);
                    if (idx >= 0)
                        info.delete(idx, s2.length());
                    info.append(s3);
                }
                break;
            case 3:
                prevF = buildFlagBorderEW(eastSector, westSector);
                useful = false;
                if (eastSector == -1){
                    i = extractBorder1(f);
                    if (i != -1){
                        eastSector = i;
                        useful = true;
                    }
                }
                else
                    f = (f | (eastSector << 19) | 0x40000);
                if (westSector == -1){
                    i = extractBorder2(f);
                    if (i != -1){
                        westSector = i;
                        useful = true;
                    }
                }
                else
                    f = (f | (westSector << 13) | 0x1000);
                if (useful){
                    String s2 = "x" + prevF;
                    int idx = newInfo.indexOf(s2);
                    if (idx >= 0)
                        newInfo.delete(idx, s2.length());
                    String s3 = "x"+f;
                    newInfo.append(s3);
                    idx = info.indexOf(s2);
                    if (idx >= 0)
                        info.delete(idx, s2.length());
                    info.append(s3);
                }
                break;
            case 4:
                i = extractRound(f);
                x = extractSectorX(f);
                y = extractSectorY(f);
                if (extractRound(enemyInfo[x][y]) < i){
                    String s2 = "x" + enemyInfo[x][y];
                    int idx = newInfo.indexOf(s2);
                    String s3 = "x"+f;
                    if ((enemyInfo[x][y] & 0x30) != (f & 0x30)){
                        if (idx >= 0)
                            newInfo.delete(idx, s2.length());
                        newInfo.append(s3);
                    }
                    else{
                        if (idx >= 0) {
                            newInfo.delete(idx, s2.length());
                            newInfo.append(s3);
                        }
                    }
                    idx = info.indexOf(s2);
                    if (idx >= 0)
                        info.delete(idx, s2.length());
                    info.append(s3);
                    enemyInfo[x][y] = f;
                }
                break;
            case 5:
                i = extractRound(f);
                x = extractSectorX(f);
                y = extractSectorY(f);
                int otheri = extractRound(baseInfo[x][y]);
                if ((((baseInfo[x][y] & 0x30) != 0x20) && (otheri < i || ((f & 0x30) == 0x20))) || (((baseInfo[x][y] & 0x30) == 0x20) && (otheri > i || ((f & 0x30) == 0x20)))){
                    String s2 = "x" + baseInfo[x][y];
                    String s3 = "x" + f;
                    int idx = newInfo.indexOf(s2);
                    if ((baseInfo[x][y] & 0x30) != (f & 0x30)){
                        if (idx >= 0)
                            newInfo.delete(idx, s2.length());
                        newInfo.append(s3);
                    }
                    else{
                        if (idx >= 0) {
                            newInfo.delete(idx, s2.length());
                            newInfo.append(s3);
                        }
                    }
                    idx = info.indexOf(s2);
                    if (idx >= 0)
                        info.delete(idx, s2.length());
                    info.append(s3);
                    baseInfo[x][y] = f;
                }
                break;
            case 6:
                x = extractSectorX(f);
                y = extractSectorY(f);
                if (explored[x][y] != f){
                    explored[x][y] = f;
                    String s1 = "x"+f;
                    newInfo.append(s1);
                    info.append(s1);
                }
                break;
            case 7:
                i = extractHealth(f);
                x = extractSectorX(f);
                y = extractSectorY(f);
                baseHp[x][y] = i;
                break;
        }
    }

    public void updateFlag() throws GameActionException{
        RobotController rc = this.rc;
        StringBuffer newInfo = this.newInfo;
        StringBuffer info = this.info;
        if (newInfo.length() > 0){
            int l = 0;
            int j = newInfo.indexOf("x", 1);
            if (j == -1)
                j = newInfo.length();
            rc.setFlag(Integer.parseInt(newInfo.substring(l+1, j)));
            newInfo.delete(l, j);
        }
        else if (info.length() > 0){
            int k = 0;
            int i = 0;
            while (i > -1){
                i = info.indexOf("x", i);
                k++;
            }
            k = (int)(Math.random()*k)%k;
            i = 0;
            for (int j = 0; j < k; j++)
                i = info.indexOf("x", i);
            int j = info.indexOf("x", i);
            if (j == -1)
                j = info.length();
            rc.setFlag(Integer.parseInt(info.substring(i+1, j)));
        }
    }

    public void updateInfo(){
        RobotController rc = this.rc;
        MapLocation position = rc.getLocation();
        int[][] explored = this.explored;
        StringBuffer info = this.info;
        StringBuffer newInfo = this.newInfo;
        RobotInfo[] allEnemies = me.allEnemies;
        RobotInfo[] allAllies = me.allAllies;
        RobotInfo[] allNeutrals = me.allNeutrals;
        StringBuffer baseIds = this.baseIds;
        StringBuffer knownIds = this.knownIds;
        if (northSector == -1){
            if (!rc.canSenseLocation(position.translate(0, 4))){
                northSector = getSectorY(position.translate(0,3));
                readFlag(buildFlagBorderNS(northSector, -1));
            }
        }
        if (southSector == -1){
            if (!rc.canSenseLocation(position.translate(0, -4))){
                southSector = getSectorY(position.translate(0,-3));
                readFlag(buildFlagBorderNS(-1, southSector));
            }
        }
        if (eastSector == -1){
            if (!rc.canSenseLocation(position.translate(4, 0))){
                eastSector = getSectorY(position.translate(3,0));
                readFlag(buildFlagBorderEW(eastSector, -1));
            }
        }
        if (westSector == -1){
            if (!rc.canSenseLocation(position.translate(-4, 0))){
                westSector = getSectorY(position.translate(-3,0));
                readFlag(buildFlagBorderEW(-1, westSector));
            }
        }
        int currSectorX = getSectorX(position);
        int currSectorY = getSectorY(position);
        if (explored[currSectorX][currSectorY] == 0){
            explored[currSectorX][currSectorY] = buildExploreFlag(currSectorX, currSectorY);
            String s = "x" + explored[currSectorX][currSectorY];
            newInfo.append(s);
            info.append(s);
        }
        boolean muckrakers = false, slanderers = false;
        for (RobotInfo ri : allEnemies){
            switch (ri.getType()){
                case MUCKRAKER:
                    muckrakers = true;
                    break;
                case SLANDERER:
                    slanderers = true;
                    break;
                case ENLIGHTENMENT_CENTER:
                    int sx = getSectorX(ri.getLocation());
                    int sy = getSectorY(ri.getLocation());
                    int f = buildBaseFlag(sx, sy, rc.getRoundNum(), 1);
                    readFlag(f);
                    f = buildEnemyHealthFlag(sx, sy, ri.getConviction());
                    readFlag(f);
                    break;
            }
        }
        int f = buildEnemiesFlag(currSectorX, currSectorY, rc.getRoundNum(), muckrakers, slanderers);
        readFlag(f);
        for (RobotInfo ri : allNeutrals){
            int sx = getSectorX(ri.getLocation());
            int sy = getSectorY(ri.getLocation());
            f = buildBaseFlag(sx, sy, ri.getConviction(), 2);
            readFlag(f);
        }
        for (RobotInfo ri : allAllies){
            int id = ri.getID();
            String s = "x"+id;
            if (ri.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                int sx = getSectorX(ri.getLocation());
                int sy = getSectorY(ri.getLocation());
                f = buildBaseFlag(sx, sy, rc.getRoundNum(), 3);
                readFlag(f);
                if (baseIds.indexOf(s) == -1) {
                    baseIds.append(s);
                    knownIds.append(s);
                    newInfo.append(buildBaseIdFlag(id));
                }
            }
            else {
                if (knownIds.indexOf(s) == -1) {
                    knownIds.append(s);
                    newInfo.append(buildUnitIdFlag(id));
                }
            }
        }
    }

    final int extractSectorX(int f){
        return (f>>19);
    }
    final int extractSectorY(int f){
        return ((f>>14)&31);
    }
    final int extractHealth(int f){
        return ((f >> 4)&1023);
    }
    final int extractRound(int f){ //Round/8
        return ((f >> 6)&255);
    }
    final int extractId(int f){
        return (f >> 4);
    }
    final int extractBorder1(int f){
        return (f & 0x40000) == 0x40000 ? ((f >> 19) & 31) : -1;
    }
    final int extractBorder2(int f){
        return (f & 0x1000) == 0x1000 ? ((f >> 13) & 31) : -1;
    }
    final int buildFlagBorderNS(int x, int y){
        return ((x == -1 ? 0 : ((x << 1) | 1)) << 18) | ((y == -1 ? 0 : ((y << 1) | 1)) << 12) | 2;
    }
    final int buildFlagBorderEW(int x, int y){
        return ((x == -1 ? 0 : ((x << 1) | 1)) << 18) | ((y == -1 ? 0 : ((y << 1) | 1)) << 12) | 3;
    }
    final int getSectorX(MapLocation loc){
        return loc.x/4%32;
    }
    final int getSectorY(MapLocation loc){
        return loc.y/4%32;
    }
    final int buildExploreFlag(int x, int y){
        return (x << 19) | (y << 14) | 6;
    }
    final int buildBaseFlag(int x, int y, int round, int type){
        return (x << 19) | (y << 14) | ((round/8) << 6) | (type << 4) | 5;
    }
    final int buildEnemyHealthFlag(int x, int y, int health){
        return (x << 19) | (y << 14) | (Math.min((health/64), 1023)<<4) | 6;
    }
    final int buildEnemiesFlag(int x, int y, int round, boolean muck, boolean sland){
        return (x << 19) | (y << 14) | ((round/8) << 6) | (muck ? 32 : 0) | (sland ? 16 : 0) | 4;
    }
    final int buildBaseIdFlag(int id){
        return (id << 4);
    }
    final int buildUnitIdFlag(int id){
        return (id << 4) | 1;
    }
}