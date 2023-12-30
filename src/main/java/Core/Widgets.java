package Core;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.FloatStringConverter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Widgets {
    private Widgets() {}

    public static TextField CreateFloatTextField()
    {
        TextField textField = new TextField();

        // Define a pattern to allow only float numbers
        Pattern floatPattern = Pattern.compile("-?\\d*\\.?\\d*");

        // Create a UnaryOperator to filter the input
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (floatPattern.matcher(newText).matches()) {
                return change;
            } else {
                return null;
            }
        };

        // Create a TextFormatter with a FloatStringConverter and the filter
        TextFormatter<Float> floatTextFormatter = new TextFormatter<>(new FloatStringConverter(), 0.0f, filter);

        textField.setTextFormatter(floatTextFormatter);
        textField.setText("");

        return textField;
    }
}
