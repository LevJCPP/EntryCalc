import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Input two numbers (roman or arabic) and an operation you want to perform.");
        System.out.println("Available operations: + - / *. Example: 2 + 2.");
        System.out.println("Note that only numbers between 1 to 10 (inclusive) are allowed.");
        System.out.println("Input an empty line if you wish to exit.");

        for (Scanner in = new Scanner(System.in);;) {
            try {
                System.out.print("In: ");
                String input = in.nextLine();

                if (input.isEmpty()) {
                    break;
                }

                String result = calc(input);
                System.out.printf("Out: %s\n", result);

            // Does not terminate the program for the purpose of easier testing.
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String calc(String input) throws IllegalArgumentException {
        if (input.isEmpty()) throw new IllegalArgumentException("Empty input string.");

        // Split and validate string.
        String[] tokens = input.split(" ");
        if (tokens.length != 3) throw new IllegalArgumentException("Invalid input string.");
        if (!tokens[1].matches("[-+*/]")) throw new IllegalArgumentException("Invalid operation.");

        NumberType numberType = getNumbersType(tokens);
        int firstNumber = numberType == NumberType.ARABIC ?
                Integer.parseInt(tokens[0]) : convertRomanToArabic(tokens[0]);
        int secondNumber = numberType == NumberType.ARABIC ?
                Integer.parseInt(tokens[2]) : convertRomanToArabic(tokens[2]);

        if (firstNumber == 0 || firstNumber > 3999 || secondNumber == 0 || secondNumber > 3999)
            throw new IllegalArgumentException("Only numbers between 1 to 10 (inclusive) are allowed.");

        int result = 0;
        switch (tokens[1]) {
            case "+" -> result = firstNumber + secondNumber;
            case "-" -> result = firstNumber - secondNumber;
            case "*" -> result = firstNumber * secondNumber;
            case "/" -> result = firstNumber / secondNumber;
        }

        return numberType == NumberType.ARABIC ?
                String.valueOf(result) : convertArabicToRoman(result);
    }

    static NumberType getNumbersType(final String[] tokens) throws IllegalArgumentException {
        if (tokens.length != 3) throw new IllegalArgumentException("Invalid token array.");

        NumberType firstNumber = checkNumber(tokens[0]);
        if (firstNumber == NumberType.INVALID) throw new IllegalArgumentException("First number is invalid.");

        NumberType secondNumber = checkNumber(tokens[2]);
        if (secondNumber == NumberType.INVALID) throw new IllegalArgumentException("Second number is invalid.");

        if (firstNumber != secondNumber) throw new IllegalArgumentException("Numbers must be of same number system.");
        return firstNumber;
    }

    static NumberType checkNumber(final String number) {
        String arabicRegex = "[0-9]+";
        if (number.matches(arabicRegex)) return NumberType.ARABIC;

        String romanRegex = "[IVXLCDM]+";
        if (number.matches(romanRegex)) return NumberType.ROMAN;

        return NumberType.INVALID;
    }

    static int convertRomanToArabic(final String number) throws IllegalArgumentException {
        // Create an array list of roman numerals in a number.
        ArrayList<Roman> romans = new ArrayList<>();
        for (int i = 0; i != number.length(); ++i)
            romans.add(Roman.valueOf(number.substring(i, i + 1)));

        int result = 0;
        for (int repeats = 1; !romans.isEmpty();) {
            Roman current = romans.remove(0);

            if (!romans.isEmpty()) {
                // Check for illegal numeral repeats.
                if (romans.get(0) == current) {
                    if ((current != Roman.M && current != Roman.C && current != Roman.X && current != Roman.I)
                            || repeats == 3)
                        throw new IllegalArgumentException("Invalid roman number.");
                    else ++repeats;
                } else if (repeats > 0 && current.isSuffix(romans.get(0))){
                    throw new IllegalArgumentException("Invalid roman number.");
                }
                else repeats = 1;
            }

            // If current number is less than next (could be a suffix), also check whether it is a valid suffix.
            if (!romans.isEmpty() && current.isSuffix(romans.get(0))) {
                if (!current.isValidSuffix(romans.get(0))) throw new IllegalArgumentException("Invalid roman number.");
                result += current.combine(romans.remove(0));
            } else {
                result += current.toInt();
            }
        }

        return result;
    }

    static String convertArabicToRoman(int number) throws IllegalArgumentException {
        if (number <= 0)
            throw new IllegalArgumentException("Result is non-positive, thus cannot be represented by roman numbers.");

        if (number > 3999)
            throw new IllegalArgumentException("Cannot convert arabic number larger than 3999 to roman.");

        final String[] romanNumerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        final int[] romanToArabic = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

        StringBuilder romanNumber = new StringBuilder();
        for (int i = 0; i != romanNumerals.length; ++i) {
            while (number >= romanToArabic[i]) {
                romanNumber.append(romanNumerals[i]);
                number -= romanToArabic[i];
            }
        }
        return romanNumber.toString();
    }
}

enum NumberType {
    INVALID,
    ROMAN,
    ARABIC
}

enum Roman {
    I(1), V(5), X(10), L(50), C(100), D(500), M(1000);
    final int value;
    Roman(int value) {
        this.value = value;
    }
    int toInt() {
        return value;
    }

    // Check whether a suffix is valid.
    boolean isValidSuffix(Roman next) {
        if ((this == C && (next == M || next == D))
                || (this == X && (next == C || next == L))
                || (this == I && (next == X || next == V)))
            return true;
        else return this.value > next.value;
    }

    // Check whether a numeral could be a suffix.
    boolean isSuffix(Roman next) {
        return this.value < next.value;
    }

    // Get a suffixed value.
    int combine(Roman next) {
        return next.value - this.value;
    }
}
