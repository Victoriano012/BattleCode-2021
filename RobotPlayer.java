package SprintBot2;
import battlecode.common.*;

public strictfp class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        try {
            Robot controller = null;
            switch (rc.getType()) {
                case ENLIGHTENMENT_CENTER:
                    controller = new EnlightenmentCenter();
                    break;
                case MUCKRAKER:
                    controller = new Muckraker();
                    break;
                case POLITICIAN:
                    controller = new Politician();
                    break;
                case SLANDERER:
                    controller = new Slanderer();
                    break;
            }
            controller.run(rc);
        } catch (Exception e) {
            System.out.println(rc.getType() + " Exception");
            e.printStackTrace();
            Clock.yield();
        }
    }
}
