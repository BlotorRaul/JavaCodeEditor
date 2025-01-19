package org.example.controller;

import org.example.api.Judge0API;

/**
 * A controller class that serves as an intermediary between the application logic
 * and the Judge0 API. This class simplifies the process of submitting source code
 * for execution and retrieving the results.
 *
 * Example usage:
 * <pre>
 * Judge0Controller controller = new Judge0Controller();
 *
 * String languageId = "91"; // Language ID for Java
 * String sourceCode = "public class Main { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }";
 * String stdin = "";
 *
 * String result = controller.executeCode(languageId, sourceCode, stdin);
 * System.out.println("Execution Result: " + result);
 * </pre>
 *
 * Note: This class assumes that the Judge0 API is correctly configured with an API key
 * and other required settings.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class Judge0Controller {

    private final Judge0API api;

    /**
     * Constructs a new `Judge0Controller` and initializes an instance of the `Judge0API`.
     */
    public Judge0Controller() {
        this.api = new Judge0API();
    }

    /**
     * Submits the provided source code to the Judge0 API for execution and retrieves the result.
     *
     * This method performs the following steps:
     * 1. Creates a submission for the source code using the specified language ID and standard input (stdin).
     * 2. Waits for the code execution to complete.
     * 3. Retrieves and returns the execution result.
     *
     * @param languageId The ID of the programming language (e.g., "91" for Java).
     * @param sourceCode The source code to be executed.
     * @param stdin The input to be passed to the program during execution.
     * @return The result of the code execution, or an error message if an exception occurs.
     */
    public String executeCode(String languageId, String sourceCode, String stdin) {
        try {
            String token = api.createSubmission(languageId, sourceCode, stdin);
            System.out.println("Token received: " + token);

            Thread.sleep(1000);

            return api.getSubmissionResult(token);

        } catch (Exception ex) {
            ex.printStackTrace();
            return "An error occurred: " + ex.getMessage();
        }
    }
}
