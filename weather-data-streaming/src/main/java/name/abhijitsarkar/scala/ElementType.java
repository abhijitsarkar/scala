package name.abhijitsarkar.scala;

/**
 * @author Abhijit Sarkar
 */
public enum ElementType {
    PRECIPITATION("PRCP"),
    SNOWFALL_MILLIS("SNOW"),
    SNOW_DEPTH_MILLIS("SNWD"),
    MAX_TEMP("TMAX"),
    MIN_TEMP("TMIN"),
    OTHER(".*");

    private final String code;

    ElementType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
