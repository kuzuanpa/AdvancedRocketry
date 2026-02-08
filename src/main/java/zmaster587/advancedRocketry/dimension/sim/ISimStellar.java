package zmaster587.advancedRocketry.dimension.sim;

public interface ISimStellar {
    String getID();
    String getName();
    double getMass();
    double getSize();
    double getOrbitRadius();
    /**Only used in planet**/
    double getInitialAngle();
    /**Only used in planet**/
    double getInclination();
    /**Only used in star**/
    double getStaticX();
    /**Only used in star**/
    double getStaticY();
    /**Only used in star**/
    double getStaticZ();
    /**Only used in planet**/
    String getParentID();
    boolean isStar();
}
