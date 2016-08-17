package name.abhijitsarkar.scala;

/**
 * @author Abhijit Sarkar
 */
public enum QualityFlag {
    GOOD("");

    private final String code;

    QualityFlag(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
