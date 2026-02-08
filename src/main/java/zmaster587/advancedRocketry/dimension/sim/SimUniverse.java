package zmaster587.advancedRocketry.dimension.sim;

import zmaster587.advancedRocketry.entity.EntityCelestialBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimUniverse {
    private static final SimUniverse INSTANCE = new SimUniverse();
    public static SimUniverse getInstance() { return INSTANCE; }

    public static final double GRAVITY_CONSTANT = 0.01D;
    private final Map<String, SimBody> bodiesMap = new HashMap<>();
    private final List<SimBody> rootBodies = new ArrayList<>();

    public void init(List<? extends ISimStellar> configs) {
        bodiesMap.clear();
        rootBodies.clear();

        for (ISimStellar config : configs) {
            SimBody body = new SimBody(config);
            bodiesMap.put(config.getID(), body);
        }

        for (SimBody body : bodiesMap.values()) {
            String parentID = body.config.getParentID();

            if (parentID != null && bodiesMap.containsKey(parentID)) {
                SimBody parent = bodiesMap.get(parentID);
                body.setParent(parent);
                parent.addChild(body);
            } else {
                rootBodies.add(body);
            }
        }
    }
    public void stop(){
        bodiesMap.clear();
        rootBodies.clear();
    }

    public void tick() {
        for (SimBody root : rootBodies) {
            root.update(0.1D);
        }
    }

    public SimBody getBody(String id) {
        return bodiesMap.get(id);
    }

    public List<SimBody> getAllBodies() {
        return new ArrayList<>(bodiesMap.values());
    }

    public static class SimBody {
        private final ISimStellar config;
        public double x, y, z;
        private double currentAngle;
        private double angularVelocity;

        private SimBody parent;
        private final List<SimBody> children = new ArrayList<>();
        private EntityCelestialBody realEntity;

        public SimBody(ISimStellar config) {
            this.config = config;
            this.currentAngle = config.getInitialAngle();

            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        public void setParent(SimBody parent) {
            this.parent = parent;
            calculateOrbitPhysics();
        }

        public void addChild(SimBody child) {
            this.children.add(child);
        }

        public ISimStellar getConfig() { return config; }
        public void setEntity(EntityCelestialBody entity) { this.realEntity = entity; }
        public EntityCelestialBody getEntity() { return realEntity; }
        public boolean hasEntity() {
            return realEntity != null && !realEntity.isDead;
        }

        private void calculateOrbitPhysics() {
            if (parent == null) return;

            double r = config.getOrbitRadius();
            double M = parent.config.getMass();

            if (r <= 0) r = 0.1;

            this.angularVelocity = Math.sqrt((SimUniverse.GRAVITY_CONSTANT * M) / Math.pow(r, 3));
        }

        public void update(double timeDelta) {
            if (parent == null) {
                this.x = config.getStaticX();
                this.y = config.getStaticY();
                this.z = config.getStaticZ();
            } else {
                this.currentAngle += this.angularVelocity * timeDelta;
                if (this.currentAngle > Math.PI * 2) {
                    this.currentAngle -= Math.PI * 2;
                }

                double r = config.getOrbitRadius();
                double localX = Math.cos(this.currentAngle) * r;
                double localZ = Math.sin(this.currentAngle) * r;

                double theta = config.getInclination();

                double tiltedY = -localZ * Math.sin(theta);
                double tiltedZ = localZ * Math.cos(theta);

                this.x = parent.x + localX;
                this.y = parent.y + tiltedY;
                this.z = parent.z + tiltedZ;
            }

            for (SimBody child : children) {
                child.update(timeDelta);
            }
        }

    }
}