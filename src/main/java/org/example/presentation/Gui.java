package org.example.presentation;

import org.example.controller.Judge0Controller;
import org.example.controller.AutoCompleteTrieController;
import org.example.controller.AutoSaveController;
import org.example.application.SyntaxHighlightTextPane;
// Asigură-te că ai importurile pentru:
// - CustomTextPaneUI (evidențiere rând curent)
// - AutoCompletePopup (popup cu JList)
// - AutoCompleteSelectionListener (interfață pt selectare)

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

/**
 * The main GUI class that serves as a rich text editor with various features:
 * <ul>
 *     <li>Line numbering and current line highlighting.</li>
 *     <li>Autocomplete functionality using a popup and navigation via arrow keys/Enter/Mouse.</li>
 *     <li>"Run" button to execute code using Judge0 API.</li>
 *     <li>Autosave functionality every 2 minutes.</li>
 *     <li>Toggle between Light and Dark themes with proper text and background updates.</li>
 * </ul>
 * <p>
 * This class integrates multiple features into a single GUI-based editor, ideal for demonstrating
 * programming and editing utilities.
 * <p>
 * Example usage:
 * <pre>
 * public class Main {
 *     public static void main(String[] args) {
 *         new Gui();
 *     }
 * }
 * </pre>
 * <p>
 * Note: The Judge0 API must be properly configured for the "Run" functionality.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class Gui {
    // Controllers
    private final Judge0Controller judge0Controller;
    private final AutoCompleteTrieController autoCompleteController;
    private final AutoSaveController autoSaveController;

    // Main components
    private final JFrame frame;
    private final JPanel mainPanel;
    private final JTextArea outputArea;
    private final JTextArea lineNumbers;
    private final SyntaxHighlightTextPane textPane;
    private final JButton runButton;
    private final JButton toggleThemeButton;

    // Autocomplete popup
    private final AutoCompletePopup autoCompletePopup;
    private String lastPrefix = "";

    // Theme flag
    private boolean isDarkMode = false; // false = Light Mode, true = Dark Mode

    /**
     * Constructs the GUI and initializes all components and features, including:
     * <ul>
     *     <li>Syntax highlighting using {@link SyntaxHighlightTextPane}.</li>
     *     <li>Line numbering synchronized with the text editor.</li>
     *     <li>Autocomplete suggestions using {@link AutoCompletePopup}.</li>
     *     <li>Code execution via Judge0 API.</li>
     *     <li>Autosave functionality.</li>
     * </ul>
     */
    public Gui() {
        judge0Controller = new Judge0Controller();
        autoCompleteController = new AutoCompleteTrieController();

        frame = new JFrame("Editor + Autocomplete + Autosave Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Initialize text pane with syntax highlighting and custom UI
        textPane = new SyntaxHighlightTextPane();
        textPane.setOpaque(true);
        textPane.setHighlighter(null);
        textPane.setUI(new CustomTextPaneUI());

        //Initialize autocomplete popup
        autoCompletePopup = new AutoCompletePopup(suggestion -> {
            replaceLastWordInTextPane(textPane, suggestion);
        });

        //Start autosave functionality
        autoSaveController = new AutoSaveController(textPane);
        autoSaveController.startAutoSave();

        //Load Java keywords for autocomplete
        List<String> javaKeywords = Arrays.asList(
                "class", "abstract", "private", "public", "protected",
                "package", "boolean", "int", "float", "long",
                "double", "extends", "implements", "return", "static",
                "strictfp", "switch", "synchronized", "throw", "throws"
        );
        autoCompleteController.loadKeywords(javaKeywords);

        //Add line numbering
        lineNumbers = new JTextArea("1");
        lineNumbers.setEditable(false);
        lineNumbers.setFocusable(false);
        lineNumbers.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lineNumbers.setMargin(new Insets(0, 0, 0, 10));

        //Initialize output area for code execution results
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        //Add "Run" button
        runButton = new JButton("Run");
        runButton.setPreferredSize(new Dimension(100, 40));
        runButton.setFont(new Font("Arial", Font.BOLD, 14));
        runButton.setBorder(BorderFactory.createLineBorder(new Color(0, 105, 217), 2));
        runButton.setFocusPainted(false);
        runButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover: culoare la intrare/ieșire mouse
        runButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Nuanță de hover
                if (isDarkMode) {
                    runButton.setBackground(new Color(80, 80, 80));
                } else {
                    runButton.setBackground(new Color(0, 102, 204));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                applyTheme();
            }
        });

        // At "Run" => sent the code to the Judge0
        runButton.addActionListener(e -> {
            String code = textPane.getText();
            String languageId = "91"; // Java (Judge0 ID)
            String stdin = "";
            String result = judge0Controller.executeCode(languageId, code, stdin);
            outputArea.setText(result);
        });

        // Add caret listener for current line highlighting
        textPane.addCaretListener(e -> textPane.repaint());

        // Synchronize line numbering with text changes
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLineNumbers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLineNumbers();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLineNumbers();
            }

            private void updateLineNumbers() {
                int lines = textPane.getDocument().getDefaultRootElement().getElementCount();
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= lines; i++) {
                    sb.append(i).append("\n");
                }
                lineNumbers.setText(sb.toString());
            }
        });

        // Handle autocomplete navigation via keyboard
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (autoCompletePopup.isVisible()) {
                    int code = e.getKeyCode();
                    switch (code) {
                        case KeyEvent.VK_DOWN:
                            autoCompletePopup.moveSelectionDown();
                            e.consume();
                            break;
                        case KeyEvent.VK_UP:
                            autoCompletePopup.moveSelectionUp();
                            e.consume();
                            break;
                        case KeyEvent.VK_ENTER:
                            if (autoCompletePopup.hasSelection()) {
                                String selected = autoCompletePopup.getSelectedValue();
                                replaceLastWordInTextPane(textPane, selected);
                                autoCompletePopup.hidePopup();
                                e.consume();
                            }
                            break;
                        default:
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Avoid reacting to UP, DOWN, or ENTER keys as they are already handled for navigation
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_ENTER) {
                    return;
                }

                // Extract the last word (prefix) from the text in the text pane
                String text = textPane.getText();
                String prefix = getLastWord(text);

                // If the prefix has changed, fetch and update autocomplete suggestions
                if (!prefix.equals(lastPrefix)) {
                    lastPrefix = prefix;
                    autoCompleteController.getSuggestions(prefix, sugestii -> {
                        autoCompletePopup.showSuggestions(textPane, sugestii, true);
                    });
                }
            }
        });

        // Wrap text pane and line numbers into a scroll pane
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setRowHeaderView(lineNumbers);

        // Add "Toggle Theme" button
        toggleThemeButton = new JButton("Toggle Theme");
        toggleThemeButton.setPreferredSize(new Dimension(130, 40));
        toggleThemeButton.setFont(new Font("Arial", Font.BOLD, 14));
        toggleThemeButton.setFocusPainted(false);
        toggleThemeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleThemeButton.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            applyTheme();
        });

        // Add buttons to a panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(runButton);
        buttonPanel.add(toggleThemeButton);

        // Set up main panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setPreferredSize(new Dimension(400, 100));
        mainPanel.add(outputScrollPane, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        //(Light)
        applyTheme();

        frame.setVisible(true);
        textPane.requestFocusInWindow();
        textPane.setCaretPosition(0);
    }

    /**
     * Extracts the last word (prefix) from a given text.
     *
     * @param text The full text.
     * @return The last word or prefix.
     */
    private String getLastWord(String text) {
        String[] tokens = text.split("\\s+");
        if (tokens.length == 0) {
            return "";
        }
        return tokens[tokens.length - 1];
    }

    /**
     * Replaces the last word in the {@link JTextPane} with the selected suggestion.
     *
     * @param textPane The text pane where the replacement occurs.
     * @param suggestion The suggestion to replace the last word with.
     */
    private void replaceLastWordInTextPane(JTextPane textPane, String suggestion) {
        String fullText = textPane.getText();
        int lastSpace = Math.max(fullText.lastIndexOf(' '), fullText.lastIndexOf('\n'));
        if (lastSpace < 0) {
            textPane.setText(suggestion + " ");
        } else {
            String newText = fullText.substring(0, lastSpace + 1) + suggestion + " ";
            textPane.setText(newText);
        }
        textPane.setCaretPosition(textPane.getDocument().getLength());
    }

    /**
     * Applies the current theme (Light or Dark) to all components and refreshes the UI.
     */
    private void applyTheme() {
        if (isDarkMode) {
            // ========== DARK MODE ==========
            mainPanel.setBackground(new Color(48, 48, 48));
            frame.getContentPane().setBackground(new Color(48, 48, 48));

            lineNumbers.setBackground(new Color(60, 63, 65));
            lineNumbers.setForeground(new Color(210, 210, 210));

            textPane.setBackground(new Color(43, 43, 43));
            textPane.setForeground(new Color(240, 240, 240));
            textPane.setCaretColor(new Color(240, 240, 240)); // important

            outputArea.setBackground(new Color(43, 43, 43));
            outputArea.setForeground(new Color(240, 240, 240));

            runButton.setBackground(new Color(77, 77, 77));
            runButton.setForeground(new Color(230, 230, 230));

            toggleThemeButton.setBackground(new Color(77, 77, 77));
            toggleThemeButton.setForeground(new Color(230, 230, 230));

        } else {
            // ========== LIGHT MODE ==========
            mainPanel.setBackground(new Color(240, 240, 240));
            frame.getContentPane().setBackground(new Color(240, 240, 240));

            lineNumbers.setBackground(new Color(230, 230, 230));
            lineNumbers.setForeground(new Color(50, 50, 50));

            textPane.setBackground(Color.WHITE);
            textPane.setForeground(Color.BLACK);
            textPane.setCaretColor(Color.BLACK);  // important

            outputArea.setBackground(new Color(250, 250, 250));
            outputArea.setForeground(Color.BLACK);

            runButton.setBackground(new Color(0, 123, 255));
            runButton.setForeground(Color.WHITE);

            toggleThemeButton.setBackground(new Color(200, 200, 200));
            toggleThemeButton.setForeground(Color.BLACK);
        }

        // (fix bug text alb/negru)
        textPane.updateUI();
        textPane.repaint();

        // show others UI
        frame.repaint();
    }


    // update lineNumber
    private void updateLineNumbers() {
        int lines = textPane.getDocument().getDefaultRootElement().getElementCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(i).append("\n");
        }
        lineNumbers.setText(sb.toString());
    }
}
