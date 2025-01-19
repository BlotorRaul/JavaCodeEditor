package org.example.controller;

import org.example.application.AutoCompleteTrie;

import java.util.List;
import java.util.function.Consumer;

/**
 * A controller class that acts as an intermediary for managing the `AutoCompleteTrie` component.
 * <p>
 * This controller provides methods to:
 * - Load a list of keywords into the trie.
 * - Fetch suggestions asynchronously based on a given prefix.
 * - Clean up resources when the component is no longer needed.
 * <p>
 * Note: Ensure to call {@link #shutdown()} to release resources when the controller is no longer needed.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class AutoCompleteTrieController {
    private final AutoCompleteTrie autoCompleteTrie;

    public AutoCompleteTrieController() {
        this.autoCompleteTrie = new AutoCompleteTrie();
    }

    /**
     * Loads a list of keywords into the `AutoCompleteTrie` for use in autocomplete suggestions.
     *
     * @param keywords The list of keywords to load into the trie.
     */
    public void loadKeywords(List<String> keywords) {
        autoCompleteTrie.loadKeywords(keywords);
    }

    /**
     * Fetches autocomplete suggestions for a given prefix.
     * The result is returned asynchronously on the Event Dispatch Thread (EDT) via the provided callback.
     *
     * @param prefix   The prefix to search for in the trie.
     * @param callback A callback to handle the list of suggestions returned.
     */
    public void getSuggestions(String prefix, Consumer<List<String>> callback) {
        autoCompleteTrie.getSuggestions(prefix, callback);
    }

    /**
     * Releases resources used by the `AutoCompleteTrie`, such as the executor service.
     * <p>
     * This method should be called when the component is no longer needed to avoid resource leaks.
     */
    public void shutdown() {
        autoCompleteTrie.shutdown();
    }
}
