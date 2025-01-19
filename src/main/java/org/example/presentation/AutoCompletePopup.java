package org.example.presentation;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Interface for notifying when a user selects a suggestion.
 */
interface AutoCompleteSelectionListener {
    /**
     * Called when a suggestion is selected by the user.
     *
     * @param suggestion The selected suggestion.
     */
    void onSuggestionSelected(String suggestion);
}

/**
 * A popup component for autocompletion, built using a {@link JPopupMenu} containing a {@link JList} of strings.
 * <p>
 * The popup:
 * - Displays a list of suggestions next to the caret in a {@link JTextPane}.
 * - Does not take focus away from the text pane.
 * - Notifies a listener when a suggestion is selected by the user.
 * <p>
 * Example usage:
 * <pre>
 * AutoCompleteSelectionListener listener = suggestion -> System.out.println("Selected: " + suggestion);
 * AutoCompletePopup popup = new AutoCompletePopup(listener);
 *
 * JTextPane textPane = new JTextPane();
 * List<String> suggestions = Arrays.asList("example", "sample", "autocomplete");
 * popup.showSuggestions(textPane, suggestions, true);
 * </pre>
 * <p>
 * Note: The popup supports keyboard navigation for moving the selection up and down
 * as well as mouse double-clicks for selecting an item.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class AutoCompletePopup {
    private final JPopupMenu popupMenu;
    private final JList<String> suggestionList;
    private final JScrollPane scrollPane;
    private List<String> currentSuggestions;
    private final AutoCompleteSelectionListener selectionListener;

    /**
     * Constructs an `AutoCompletePopup` with the specified selection listener.
     *
     * @param selectionListener A listener to be notified when a suggestion is selected.
     */
    public AutoCompletePopup(AutoCompleteSelectionListener selectionListener) {
        this.selectionListener = selectionListener;

        popupMenu = new JPopupMenu();
        popupMenu.setFocusable(false);

        suggestionList = new JList<>();
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFocusable(false);

        scrollPane = new JScrollPane(suggestionList);
        scrollPane.setPreferredSize(new Dimension(200, 100));
        popupMenu.add(scrollPane);

        // Dublu-click => select item and notify listener
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = suggestionList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        suggestionList.setSelectedIndex(index);
                        String selected = suggestionList.getSelectedValue();

                        // notify the class that we use
                        selectionListener.onSuggestionSelected(selected);

                        hidePopup();
                    }
                }
            }
        });
    }

    /**
     * Displays the popup near the caret in the specified {@link JTextPane}, with the provided suggestions.
     *
     * @param textPane       The text pane where the popup will be displayed.
     * @param suggestions    A list of suggestions to display.
     * @param resetSelection Whether to reset the selection to the first item.
     */
    public void showSuggestions(JTextPane textPane, List<String> suggestions, boolean resetSelection) {
        this.currentSuggestions = suggestions;
        if (suggestions == null || suggestions.isEmpty()) {
            popupMenu.setVisible(false);
            return;
        }

        suggestionList.setListData(suggestions.toArray(new String[0]));

        if (resetSelection) {
            suggestionList.setSelectedIndex(0);
        }

        try {
            Rectangle caretBounds = textPane.modelToView(textPane.getCaretPosition());
            int x = caretBounds.x;
            int y = caretBounds.y + caretBounds.height;
            popupMenu.show(textPane, x, y);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
            popupMenu.setVisible(false);
        }
    }

    /**
     * Checks if the popup is currently visible.
     *
     * @return `true` if the popup is visible, `false` otherwise.
     */
    public boolean isVisible() {
        return popupMenu.isVisible();
    }

    public void hidePopup() {
        popupMenu.setVisible(false);
    }

    /**
     * Checks if an item in the suggestion list is currently selected.
     *
     * @return `true` if an item is selected, `false` otherwise.
     */
    public boolean hasSelection() {
        return suggestionList.getSelectedIndex() >= 0;
    }

    /**
     * Retrieves the currently selected value in the suggestion list.
     *
     * @return The selected value, or `null` if no value is selected.
     */
    public String getSelectedValue() {
        return suggestionList.getSelectedValue();
    }

    /**
     * Moves the selection down to the next item in the list.
     */
    public void moveSelectionDown() {
        int current = suggestionList.getSelectedIndex();
        int size = suggestionList.getModel().getSize();
        if (current < size - 1) {
            suggestionList.setSelectedIndex(current + 1);
            suggestionList.ensureIndexIsVisible(current + 1);
        }
    }

    /**
     * Moves the selection up to the previous item in the list.
     */
    public void moveSelectionUp() {
        int current = suggestionList.getSelectedIndex();
        if (current > 0) {
            suggestionList.setSelectedIndex(current - 1);
            suggestionList.ensureIndexIsVisible(current - 1);
        }
    }
}
