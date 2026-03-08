package com.example.scicalc;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;

public class ScientificCalculatorApp extends JFrame {

    private final JTextField inputField = new JTextField();
    private final JTextArea historyArea = new JTextArea();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final DecimalFormat decimalFormat = new DecimalFormat("0.##########");

    public ScientificCalculatorApp() {
        super("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        initializeTopPanel();
        initializeButtonPanel();

        setVisible(true);
    }

    private void initializeTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(6, 6));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        inputField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        inputField.addActionListener(e -> evaluateExpression());

        historyArea.setEditable(false);
        historyArea.setRows(8);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JScrollPane historyScroll = new JScrollPane(historyArea);

        topPanel.add(inputField, BorderLayout.NORTH);
        topPanel.add(historyScroll, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
    }

    private void initializeButtonPanel() {
        String[][] buttons = {
                {"sin(", "cos(", "tan(", "sqrt("},
                {"asin(", "acos(", "atan(", "abs("},
                {"log(", "ln(", "pi", "e"},
                {"(", ")", "^", "!"},
                {"7", "8", "9", "/"},
                {"4", "5", "6", "*"},
                {"1", "2", "3", "-"},
                {"0", ".", "=", "+"},
                {"C", "DEL", "ANS", "HIST"}
        };

        JPanel grid = new JPanel(new GridLayout(buttons.length, buttons[0].length, 6, 6));
        grid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (String[] row : buttons) {
            for (String label : row) {
                JButton button = new JButton(label);
                button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
                button.addActionListener(e -> onButtonPressed(label));
                grid.add(button);
            }
        }

        add(grid, BorderLayout.CENTER);
    }

    private void onButtonPressed(String label) {
        switch (label) {
            case "=" -> evaluateExpression();
            case "C" -> inputField.setText("");
            case "DEL" -> {
                String text = inputField.getText();
                if (!text.isEmpty()) {
                    inputField.setText(text.substring(0, text.length() - 1));
                }
            }
            case "ANS" -> {
                String[] lines = historyArea.getText().split("\\R");
                for (int i = lines.length - 1; i >= 0; i--) {
                    if (lines[i].startsWith("= ")) {
                        inputField.setText(inputField.getText() + lines[i].substring(2));
                        break;
                    }
                }
            }
            case "HIST" -> historyArea.setText("");
            default -> inputField.setText(inputField.getText() + label);
        }
    }

    private void evaluateExpression() {
        String expression = inputField.getText().trim();
        if (expression.isEmpty()) {
            return;
        }

        try {
            double result = evaluator.evaluate(expression);
            String rendered = decimalFormat.format(result);
            historyArea.append(expression + System.lineSeparator());
            historyArea.append("= " + rendered + System.lineSeparator() + System.lineSeparator());
            inputField.setText(rendered);
        } catch (RuntimeException ex) {
            historyArea.append(expression + System.lineSeparator());
            historyArea.append("! Error: " + ex.getMessage() + System.lineSeparator() + System.lineSeparator());
            inputField.selectAll();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fallback to default LAF
        }

        SwingUtilities.invokeLater(ScientificCalculatorApp::new);
    }
}
