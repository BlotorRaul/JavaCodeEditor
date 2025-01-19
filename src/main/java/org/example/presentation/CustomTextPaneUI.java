package org.example.presentation;

import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.*;
import java.awt.*;

/**
 * A custom UI for a {@link javax.swing.JTextPane} that highlights the background
 * of the current line where the caret is positioned.
 * <p>
 * This class extends {@link BasicTextPaneUI} and overrides the `paintSafely`
 * method to add a custom rendering of the current line's background.
 * <p>
 * Example usage:
 * <pre>
 * JTextPane textPane = new JTextPane();
 * textPane.setUI(new CustomTextPaneUI());
 * </pre>
 * <p>
 * Note: The background color for the highlighted line is set to a light lavender
 * (RGB: 230, 230, 250). This can be customized in the code.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class CustomTextPaneUI extends BasicTextPaneUI {
    @Override
    protected void paintSafely(Graphics g) {
        JTextComponent comp = getComponent();
        int caretPosition = comp.getCaretPosition();
        Element root = comp.getDocument().getDefaultRootElement();
        int lineIndex = root.getElementIndex(caretPosition);

        try {
            Element line = root.getElement(lineIndex);
            Rectangle rect = modelToView(comp, line.getStartOffset());
            if (rect != null) {
                g.setColor(new Color(230, 230, 250));
                g.fillRect(0, rect.y, comp.getWidth(), rect.height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.paintSafely(g);
    }
}
