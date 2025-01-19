package org.example.application;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * A standalone component that uses a Trie data structure to provide autocomplete functionality.
 * <p>
 * Operations like loading keywords and fetching suggestions are performed on a separate thread
 * to avoid blocking the UI thread.
 * <p>
 * This class supports:
 * - Asynchronous keyword loading into the Trie.
 * - Fetching suggestions for a given prefix with results returned on the Event Dispatch Thread (EDT).
 * <p>
 * Example usage:
 * <pre>
 *     AutoCompleteTrie autoComplete = new AutoCompleteTrie();
 *     autoComplete.loadKeywords(Arrays.asList("public", "private", "protected"));
 *
 *     autoComplete.getSuggestions("pri", suggestions -> {
 *         System.out.println("Suggestions: " + suggestions);
 *     });
 *
 *     autoComplete.shutdown();
 * </pre>
 * <p>
 * Note: Call {@link #shutdown()} to stop the executor service when the component is no longer needed.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class AutoCompleteTrie {

    private final Trie trie;
    private final ExecutorService executor;

    /**
     * Creates a new instance of the AutoCompleteTrie component.
     * Initializes an empty Trie and a single-threaded executor for background operations.
     */
    public AutoCompleteTrie() {
        this.trie = new Trie();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Loads a list of keywords into the Trie asynchronously.
     *
     * @param keywords The list of keywords to load into the Trie.
     */
    public void loadKeywords(List<String> keywords) {
        executor.submit(() -> {
            for (String word : keywords) {
                trie.insert(word);
            }
            System.out.println("[AutoCompleteTrie] Keywords have been loaded into the Trie.");
        });
    }

    /**
     * Fetches suggestions for the given prefix asynchronously.
     * The results are returned to the provided callback on the Event Dispatch Thread (EDT).
     *
     * @param prefix   The prefix to search for in the Trie.
     * @param callback A callback that will receive the list of suggestions(Deliver the results back on the UI thread)
     */
    public void getSuggestions(String prefix, Consumer<List<String>> callback) {
        executor.submit(() -> {

            List<String> found = trie.searchPrefix(prefix);

            SwingUtilities.invokeLater(() -> callback.accept(found));
        });
    }

    /**
     * Shuts down the executor service.
     * Should be called when the component is no longer needed to release resources.
     */
    public void shutdown() {
        executor.shutdown();
    }

    // ------------------------------------------------
    // Internal Trie Implementation
    // ------------------------------------------------

    /**
     * A simple Trie (prefix tree) implementation for storing and searching keywords.
     */
    private static class Trie {
        private final TrieNode root;

        public Trie() {
            this.root = new TrieNode();
        }

        /**
         * Inserts a word into the Trie.
         * Only considers lowercase letters [a-z].
         * Convert to lowercase and ignore non-[a-z] characters.
         *
         * @param word The word to insert into the Trie.
         */
        public void insert(String word) {

            String w = word.toLowerCase();
            TrieNode current = root;
            for (char c : w.toCharArray()) {
                if (c < 'a' || c > 'z') {
                    continue;
                }
                int index = c - 'a';
                if (current.children[index] == null) {
                    current.children[index] = new TrieNode();
                }
                current = current.children[index];
            }
            current.endOfWord = true;
        }

        /**
         * Searches for words that start with the given prefix.
         *
         * @param prefix The prefix to search for.
         * @return A list of words that start with the given prefix.
         */
        public List<String> searchPrefix(String prefix) {
            List<String> results = new ArrayList<>();
            String p = prefix.toLowerCase();
            TrieNode current = root;

            for (char c : p.toCharArray()) {
                if (c < 'a' || c > 'z') {
                    return results;
                }
                int index = c - 'a';
                if (current.children[index] == null) {
                    return results;
                }
                current = current.children[index];
            }
            // Collect all words starting from this node
            collectWords(current, new StringBuilder(p), results);

            return results;
        }

        /**
         * Collects all words from the given Trie node.
         *
         * @param node    The starting node.
         * @param prefix  The prefix built so far.
         * @param results The list where collected words are added.
         */
        private void collectWords(TrieNode node, StringBuilder prefix, List<String> results) {
            if (node.endOfWord) {
                results.add(prefix.toString());
            }
            for (int i = 0; i < 26; i++) {
                TrieNode child = node.children[i];
                if (child != null) {
                    prefix.append((char) (i + 'a'));
                    collectWords(child, prefix, results);
                    prefix.deleteCharAt(prefix.length() - 1);
                }
            }
        }
    }

    /**
     * Represents a node in the Trie.
     */
    private static class TrieNode {
        boolean endOfWord;
        TrieNode[] children;

        public TrieNode() {
            this.endOfWord = false;
            this.children = new TrieNode[26]; //[a-z]
        }
    }

    // ------------------------------------------------
    // Optional Test Method
    // ------------------------------------------------

    public static void main(String[] args) {
        AutoCompleteTrie autoComplete = new AutoCompleteTrie();
        // Exemplu de cuvinte
        List<String> words = Arrays.asList("class", "abstract", "private", "public",
                "protected", "package", "boolean");
        autoComplete.loadKeywords(words);

        // Căutăm prefixul "p" și afișăm
        autoComplete.getSuggestions("prr", (found) -> {
            System.out.println("Sugestii pentru prefixul 'p': " + found);
            autoComplete.shutdown();
        });
    }
}
