package Core;

public class Utils {
    private Utils() {}

    // For some reason simple value == "-" does not work
    public static boolean IsOnlyNegativeSign(String value)
    {
        return value.length() == 1 && value.charAt(0) == '-';
    }

}
