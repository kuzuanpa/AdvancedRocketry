package zmaster587.advancedRocketry.atmosphere;

public class AtmosphereTypes {
    public static final AtmosphereBase AIR = new AtmosphereBase(false, true, "air");
    public static final AtmosphereBase PRESSURIZEDAIR = new AtmosphereBase(false, true, true, "PressurizedAir");
    public static final AtmosphereBase LOWOXYGEN = new AtmosphereLowOxygen(true, false, false, "lowO2");
    public static final AtmosphereBase VACUUM = new AtmosphereVacuum();
    public static final AtmosphereBase HIGHPRESSURE = new AtmosphereHighPressure(true, true, true, "HighPressure");
    public static final AtmosphereBase SUPERHIGHPRESSURE = new AtmosphereSuperHighPressure(true, false, true, "SuperHighPressure");
    public static final AtmosphereBase VERYHOT = new AtmosphereVeryHot(true, false, true, "VeryHot");
    public static final AtmosphereBase SUPERHEATED = new AtmosphereSuperheated(true, false, true, "Superheated");
    public static final AtmosphereBase NOO2 = new AtmosphereNoOxygen(true, false, false, "NoO2");
    public static final AtmosphereBase HIGHPRESSURENOO2 = new AtmosphereHighPressureNoOxygen(true, false, false, "HighPressureNoO2");
    public static final AtmosphereBase SUPERHIGHPRESSURENOO2 = new AtmosphereSuperHighPressureNoOxygen(true, false, false, "SuperHighPressureNoO2");
    public static final AtmosphereBase VERYHOTNOO2 = new AtmosphereVeryHotNoOxygen(true, false, false, "VeryHotNoO2");
    public static final AtmosphereBase SUPERHEATEDNOO2 = new AtmosphereSuperheatedNoOxygen(true, false, false, "SuperheatedNoOxygen");
}
