import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/** Paints rounded keys with hover, pressed, and keyboard-focus feedback. */
final class CalculatorButton extends JButton {
    private static final long serialVersionUID = 1L;
    private static final Color FOCUS = new Color(153, 198, 255);
    private final Color fill;

    /** Configures a rounded key from one shared definition of its color group. */
    CalculatorButton(String text) {
        super(text);
        KeyStyle style = KeyStyle.forCommand(text);
        fill = style.background;
        setForeground(style.foreground);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setFocusPainted(false);
        setRolloverEnabled(true);
        setOpaque(false);
    }

    /** Draws smooth key surfaces and a visible focus indicator before the label. */
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color background = fill;
        if (getModel().isPressed()) {
            background = fill.darker();
        } else if (getModel().isRollover()) {
            background = fill.brighter();
        }
        canvas.setColor(background);
        canvas.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 24, 24);
        if (isFocusOwner()) {
            canvas.setColor(FOCUS);
            canvas.setStroke(new BasicStroke(2));
            canvas.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 24, 24);
        }
        canvas.dispose();
        super.paintComponent(graphics);
    }

    /** Defines each key group's background and foreground together. */
    private enum KeyStyle {
        DIGIT(new Color(255, 128, 41), new Color(18, 22, 31)),
        OPERATOR(new Color(62, 52, 39), new Color(255, 211, 137)),
        EDIT(new Color(48, 52, 62), Color.WHITE),
        RESET(new Color(255, 45, 120), new Color(18, 22, 31)),
        EQUALS(new Color(255, 211, 137), new Color(18, 22, 31));

        private final Color background;
        private final Color foreground;

        /** Associates both colors with a single semantic key group. */
        KeyStyle(Color background, Color foreground) {
            this.background = background;
            this.foreground = foreground;
        }

        /** Classifies a key once for both its surface and label colors. */
        static KeyStyle forCommand(String command) {
            KeyStyle style;
            if (command.matches("[0-9]")) {
                style = DIGIT;
            } else {
                style = switch (command) {
                    case "+", "−", "×", "÷", "xʸ" -> OPERATOR;
                    case "AC" -> RESET;
                    case "=" -> EQUALS;
                    default -> EDIT;
                };
            }
            return style;
        }
    }
}
