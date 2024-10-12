package lab1;

public class Validator {
    // Validation methods
    public static boolean validateX(float x) {
        return x >= -4 && x <= 4;
    }

    public static boolean validateY(float y) {
        return y >= -3 && y <= 5;
    }

    public static boolean validateR(float r) {
        return r >= 1 && r <= 5 && r % 0.5 == 0;
    }
}
