package zmaster587.advancedRocketry.dimension.sim;

import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;

import java.util.ArrayList;
import java.util.List;

public class AdvanceRocketrySimulateUniverseCompact {
    public static boolean debugMode = false;
    public static void init(){
        List<ISimStellar> convertedStellars = new ArrayList<>();
        for (StellarBody star : DimensionManager.getInstance().getStars()){
            convertedStellars.add(new SimStar(star.getId(), star.getName(), star.getMass(), star.getSize(), star.getPosX(),star.getPosY(),star.getPosZ()));
        }
        for (IDimensionProperties moon : DimensionManager.getInstance().getDimensionProperties()){
            if(DimensionManager.getInstance().getStar(moon.getId()) != null)continue;
            convertedStellars.add(new SimPlanet(moon.getId(), moon.getName(), moon.getMass(), moon.isGasGiant()?2.0:1.0, moon.getSolarOrbitalDistance(),moon.getOrbitPhi(), moon.getOrbitPhi(), moon.getParentPlanet() != -1? moon.getParentPlanet(): moon.getStarId()));
        }
        SimUniverse.getInstance().init(convertedStellars);
    }
    public static void stop(){
        SimUniverse.getInstance().stop();
    }
    public static void tick(){
        SimUniverse.getInstance().tick();
    }
    public static class SimStar implements ISimStellar{
        int id; String name; double mass, size, x,  y,  z;
        public SimStar(int id, String name, double mass, double size, double x, double y, double z){
            this.id = id;
            this.name = name;
            this.mass = mass;
            this.size = size;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        @Override public String getID() {return String.valueOf(id);}
        @Override public String getName() {return name;}
        @Override public double getMass() {return mass;}
        @Override public double getSize() {return size;}
        @Override public double getOrbitRadius() {return 0;}
        @Override public double getInitialAngle() {return 0;}
        @Override public double getInclination() {return 0;}
        @Override public double getStaticX() {return x;}
        @Override public double getStaticY() {return y;}
        @Override public double getStaticZ() {return z;}
        @Override public String getParentID() {return null;}
        @Override public boolean isStar() {return true;}
    }

    public static class SimPlanet implements ISimStellar{
        int id, parentID; String name; double mass, size, orbitRadius,  initialAngle,  inclination;
        public SimPlanet(int id, String name, double mass, double size, double orbitRadius, double initialAngle, double inclination, int parentID){
            this.id = id;
            this.name = name;
            this.size = size;
            this.mass = mass;
            this.orbitRadius = orbitRadius;
            this.initialAngle = initialAngle;
            this.inclination = inclination;
            this.parentID = parentID;
        }
        @Override public String getID() {return String.valueOf(id);}
        @Override public String getName() {return name;}
        @Override public double getMass() {return mass;}
        @Override public double getSize() {return size;}
        @Override public double getOrbitRadius() {return orbitRadius;}
        @Override public double getInitialAngle() {return initialAngle;}
        @Override public double getInclination() {return inclination;}
        @Override public double getStaticX() {return 0;}
        @Override public double getStaticY() {return 0;}
        @Override public double getStaticZ() {return 0;}
        @Override public String getParentID() {return String.valueOf(parentID);}
        @Override public boolean isStar() {return false;}
    }
}
