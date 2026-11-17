import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Provides a resizable, keyboard-accessible Swing calculator with a dark interface. */
public final class Calculator {
    private static final Color BACKGROUND = new Color(18, 22, 31);
    private static final Color MUTED = new Color(153, 166, 187);
    private final CalculatorModel model = new CalculatorModel();
    private final JTextField display = new JTextField("0");
    private final JLabel expression = new JLabel(" ", SwingConstants.RIGHT);

    /** Builds the display, keypad, and keyboard bindings on the event dispatch thread. */
    public Calculator() {
        JFrame frame = new JFrame("Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(0, 24));
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(26, 24, 22, 24));
        content.add(createDisplay(), BorderLayout.NORTH);
        content.add(createKeypad(content), BorderLayout.CENTER);
        JLabel hint = new JLabel("Enter  =    ·    Esc  clear    ·    Backspace  delete", SwingConstants.CENTER);
        hint.setForeground(MUTED);
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        content.add(hint, BorderLayout.SOUTH);
        frame.setContentPane(content);
        frame.setMinimumSize(new Dimension(360, 560));
        frame.setSize(400, 620);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        refresh();
    }

    /** Launches the desktop interface on Swing's event dispatch thread. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Calculator::new);
    }

    /** Creates the expression line and selectable result display. */
    private JPanel createDisplay() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        expression.setForeground(MUTED);
        expression.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        expression.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        expression.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setForeground(Color.WHITE);
        display.setBackground(BACKGROUND);
        display.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        display.setPreferredSize(new Dimension(300, 84));
        display.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        display.getAccessibleContext().setAccessibleName("Calculator result");
        display.addComponentListener(new ComponentAdapter() {
            /** Refits the result when the window changes size. */
            @Override
            public void componentResized(ComponentEvent event) {
                fitDisplay();
            }
        });
        panel.add(expression);
        panel.add(display);
        return panel;
    }

    /** Creates the keypad and maps equivalent physical keyboard commands. */
    private JPanel createKeypad(JPanel content) {
        JPanel keypad = new JPanel(new GridLayout(5, 4, 10, 10));
        keypad.setOpaque(false);
        String[] commands = {"xʸ", "(", ")", "÷", "7", "8", "9", "×",
                "4", "5", "6", "−", "1", "2", "3", "+", "AC", "0", "⌫", "="};
        for (String command : commands) {
            CalculatorButton button = new CalculatorButton(command);
            button.setAction(new CommandAction(command));
            button.getAccessibleContext().setAccessibleName(accessibleName(command));
            keypad.add(button);
            if (command.matches("[0-9]")) {
                bind(content, "typed " + command, command);
            }
        }
        String[][] shortcuts = {{"typed ^", "^"}, {"typed +", "+"}, {"typed -", "−"}, {"typed *", "×"},
                {"typed /", "÷"}, {"typed =", "="},
                {"ENTER", "="}, {"ESCAPE", "AC"}, {"BACK_SPACE", "⌫"},
                {"DELETE", "AC"}, {"typed (", "("}, {"typed )", ")"}};
        for (String[] shortcut : shortcuts) {
            bind(content, shortcut[0], shortcut[1]);
        }
        return keypad;
    }

    /** Supplies descriptive names for symbolic controls to assistive technology. */
    private String accessibleName(String command) {
        return switch (command) {
            case "AC" -> "Clear all";
            case "xʸ" -> "Raise to a power";
            case "⌫" -> "Delete last character";
            case "(" -> "Open parenthesis";
            case ")" -> "Close parenthesis";
            case "÷" -> "Divide";
            case "×" -> "Multiply";
            case "−" -> "Subtract or negate";
            case "+" -> "Add";
            case "=" -> "Equals";
            default -> command;
        };
    }

    /** Registers a shortcut that works while any calculator component has focus. */
    private void bind(JComponent component, String key, String command) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        component.getActionMap().put(key, new CommandAction(command));
    }

    /** Updates both display lines after a command. */
    private void refresh() {
        display.setText(model.getDisplay());
        expression.setText(model.getExpression().isEmpty() ? " " : model.getExpression());
        fitDisplay();
    }

    /** Reduces the result font to fit the available width without hiding its end. */
    private void fitDisplay() {
        int size = 52;
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, size);
        FontMetrics metrics = display.getFontMetrics(font);
        while (size > 14 && metrics.stringWidth(display.getText()) > display.getWidth() - 12) {
            font = font.deriveFont((float) --size);
            metrics = display.getFontMetrics(font);
        }
        display.setFont(font);
        display.setCaretPosition(display.getText().length());
    }

    /** Routes buttons and shortcuts through the same model command. */
    private final class CommandAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        /** Associates an action with its visible calculator command. */
        CommandAction(String command) {
            super(command);
        }

        /** Applies the command and redraws the display. */
        @Override
        public void actionPerformed(ActionEvent event) {
            model.accept((String) getValue(NAME));
            refresh();
        }
    }
}
