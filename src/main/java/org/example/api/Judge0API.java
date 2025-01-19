package org.example.api;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

/**
 * The `Judge0API` class provides methods to interact with the Judge0 API for
 * executing source code and retrieving the execution results.
 * <p>
 * This class allows you to:
 * - Submit code for execution by creating a submission.
 * - Retrieve the results of the execution using a token.
 * <p>
 * The class uses the OkHttp library for HTTP requests and the `ConfigLoader`
 * class to load configuration values like API key, host, and base URL.
 * <p>
 * Example usage:
 * <pre>
 *     Judge0API api = new Judge0API();
 *     String token = api.createSubmission("91", "class Main {...}", "");
 *     String result = api.getSubmissionResult(token);
 *     System.out.println(result);
 * </pre>
 * <p>
 * Note: Ensure that the configuration values (`api.key`, `api.host`, `base.url`) are correctly set in
 * the `config.properties` file or provided through other means.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class Judge0API {

    private static final String API_KEY = ConfigLoader.get("api.key");
    private static final String API_HOST = ConfigLoader.get("api.host");

    private static final String BASE_URL = ConfigLoader.get("base.url");


    private final OkHttpClient client = new OkHttpClient();

    /**
     * Creates a submission to the Judge0 API with the provided source code, language ID, and input.
     *
     * @param languageId The ID of the programming language (e.g., "91" for Java).
     * @param sourceCode The source code to be executed.
     * @param stdin      The standard input to be provided to the program.
     * @return A token that can be used to retrieve the result of the execution.
     * @throws IOException If an error occurs while communicating with the Judge0 API.
     */
    public String createSubmission(String languageId, String sourceCode, String stdin) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");

        JSONObject payload = new JSONObject();
        payload.put("language_id", Integer.parseInt(languageId));
        payload.put("source_code", sourceCode);
        payload.put("stdin", stdin);

        RequestBody body = RequestBody.create(mediaType, payload.toString());

        Request request = new Request.Builder()
                .url(BASE_URL + "?base64_encoded=false&wait=false&fields=*")
                .post(body)
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", API_HOST)
                .addHeader("Content-Type", "application/json")
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JSONObject jsonResponse = new JSONObject(response.body().string());
            return jsonResponse.getString("token");
        }
    }

    /**
     * Retrieves the result of a submission using the provided token.
     *
     * @param token The token associated with the submission.
     * @return The output of the execution, including errors or compile messages if present.
     * - If there is a runtime error, it returns "Runtime Error" along with the error message.
     * - If there is a compilation error, it returns "Compilation Error" along with the error message.
     * - If there is no output, it returns "No Output".
     * @throws IOException If an error occurs while communicating with the Judge0 API.
     */
    public String getSubmissionResult(String token) throws IOException {
        System.out.println("Token utilizat: " + token);


        Request request = new Request.Builder()
                .url(BASE_URL + "/" + token + "?base64_encoded=false&fields=*")
                .get()
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", API_HOST)
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JSONObject jsonResponse = new JSONObject(response.body().string());
            //System.out.println("JSON Response: " + jsonResponse.toString(2)); // Debug

            String stdout = jsonResponse.optString("stdout", "No output");

            String stderr = jsonResponse.optString("stderr", "No error");
            String compileOutput = jsonResponse.optString("compile_output", "No compile error");

            System.out.println(compileOutput);
            if (stderr != null && !stderr.isEmpty() && !stderr.equals("No error")) {
                return "Runtime Error:\n" + stderr;
            } else if (compileOutput != null && !compileOutput.isEmpty() && !compileOutput.equals("No compile error")) {
                return "Compilation Error:\n" + compileOutput;
            } else if (stdout == null || stdout.isEmpty()) {
                return "No Output: Verificați codul sursă.";
            } else {
                return stdout;
            }
        }
    }

}
