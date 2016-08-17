package name.abhijitsarkar.scala;

/**
 * @author Abhijit Sarkar
 */
public enum MeasurementFlag {
    NOT_APPLICABLE(""),
    PRECIPITATION_TOTAL_12HR("B"),
    PRECIPITATION_TOTAL_6HR("D"),
    HOURLY_TEMP("H"),
    KNOTS("K"),
    LAGGED("L"),
    OKATAS("O"),
    MISSING_PRESUMED_ZERO("P"),
    TRACE("T"),
    WIND_DIRECTION("W");

    private final String code;

    MeasurementFlag(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
