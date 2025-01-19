package org.example.application;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A custom `JTextPane` that provides basic syntax highlighting for Java-like code.
 * <p>
 * The class highlights:
 * - Keywords (e.g., `if`, `while`, `for`, `return`).
 * - Comments (single-line starting with `//`).
 * - Import statements (e.g., `import java.util.List;`).
 * - Access modifiers and class declarations (e.g., `public`, `private`, `class`).
 * <p>
 * Syntax highlighting is triggered when the user types specific keys such as
 * `SPACE`, `ENTER`, or `TAB`. The highlighting is applied asynchronously to avoid
 * blocking the UI thread.
 * <p>
 * Note: This implementation is simplified and focuses on basic patterns for syntax highlighting.
 * It can be extended for more advanced features like multi-line comments or string literals.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class SyntaxHighlightTextPane extends JTextPane {


    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b(if|while|for|return|do|else)\\b");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("//[^\n]*"); // Single-line comments
    private static final Pattern IMPORT_PATTERN = Pattern.compile("\\bimport\\s+([\\w\\.]+);\\b");
    private static final Pattern ACCESS_MODIFIER_PATTERN = Pattern.compile("\\b(public|private|protected|final|static|class)\\b");  // Modificatori de acces și declarații de clase

    /**
     * Constructs a `SyntaxHighlightTextPane` and adds a `KeyListener` to trigger syntax highlighting
     * when certain keys (e.g., `SPACE`, `ENTER`, `TAB`) are pressed.
     */
    public SyntaxHighlightTextPane() {

        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Do nothing for keyTyped
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Do nothing for keyPressed
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Trigger highlighting only on SPACE, ENTER, or TAB
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB) {
                    highlightSyntax();
                }
            }
        });
    }

    /**
     * Applies syntax highlighting to the entire text content of the `JTextPane`.
     * This method identifies keywords, comments, import statements, and access modifiers
     * using regular expressions and applies the corresponding styles.
     */
    private void highlightSyntax() {
        StyledDocument doc = getStyledDocument();

        // Perform highlighting asynchronously to avoid blocking the UI thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Reset all existing styles
                doc.setCharacterAttributes(0, doc.getLength(), getStyle("default"), true);

                // Retrieve the entire text content
                String text = doc.getText(0, doc.getLength());

                // Apply highlighting for different patterns
                applyRegex(doc, text, 0, KEYWORD_PATTERN, "keyword", Color.BLUE, true);
                applyRegex(doc, text, 0, COMMENT_PATTERN, "comment", Color.GRAY, false);
                applyRegex(doc, text, 0, IMPORT_PATTERN, "import", Color.ORANGE, true);
                applyRegex(doc, text, 0, ACCESS_MODIFIER_PATTERN, "accessModifier", Color.ORANGE, true);

            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Applies a given regex pattern to the text content and styles matching regions.
     *
     * @param doc       The `StyledDocument` of the `JTextPane`.
     * @param text      The full text content of the document.
     * @param offset    The starting offset for applying styles.
     * @param pattern   The `Pattern` to match in the text.
     * @param styleName The name of the style to apply.
     * @param color     The color to use for the matched text.
     * @param bold      Whether the matched text should be bold.
     */
    private void applyRegex(StyledDocument doc, String text, int offset, Pattern pattern, String styleName, Color color, boolean bold) {
        Style style = addStyle(styleName, null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            doc.setCharacterAttributes(offset + matcher.start(), matcher.end() - matcher.start(), style, false);
        }
    }
}
