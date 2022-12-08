package cs1302.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Example using Open Library Search API.
 *
 * <p>
 * To run this example on Odin, use the following commands:
 *
 * <pre>
 * $ mvn clean compile
 * $ mvn exec:java -Dexec.mainClass=cs1302.api.OpenLibrarySearchApi
 * </pre>
 */
public class GoogleBooksApi {

    private static final String apiKey = "AIzaSyBiLx_44pYN7U2zIl8DP7Uny0Nbb-7KaXk";

    private static class VolumeInfo {
        String title;
        String[] authors;
    }

/**
     * Represents an Open Library Search API document.
     */
    private static class GoogleBooksItem{
        String kind;
        String title;
        String id;
        VolumeInfo volumeInfo;
    } // OpenLibraryDoc

    /**
     * Represents an Open Library Search API result.
     */
    private static class GoogleBooksResult {
        int totalItems;
        GoogleBooksItem[] items;
    } // OpenLibraryResult

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private static final String ENDPOINT = "https://www.googleapis.com/books/v1/volumes";

    public static void main(String[] args) {
        GoogleBooksApi
            .search("the bad beginning")
            .ifPresent(response -> example1(response));
    } // main

    /**
     * An example of some things you can do with a response.
     * @param result the ope library search result
     */
    private static void example1(GoogleBooksResult result) {
        // print what we found
        System.out.println(result.items.length);
        System.out.printf("totalItems = %d\n", result.totalItems);
        System.out.println("first title: " + result.items[0].volumeInfo.title);
        for (GoogleBooksItem item : result.items) {
            System.out.println("NEW BOOK!!!!!!!!!!!!!!!!!!!!");
            System.out.println(item.title);
        } // for
    } // example1

    /**
     * Return an {@code Optional} describing the root element of the JSON
     * response for a "search" query.
     * @param q query string
     * @return an {@code Optional} describing the root element of the response
     */
    public static Optional<GoogleBooksResult> search(String q) {
        System.out.printf("Searching for: %s\n", q);
        System.out.println("This may take some time to download...");
        try {
            String url =  String.format(
                "%s?q=%s",
                GoogleBooksApi.ENDPOINT,
                URLEncoder.encode(q, StandardCharsets.UTF_8));
            url += "key=" + apiKey;
            System.out.println("a: " + url);
            String newUrl = "https://www.googleapis.com/books/v1/volumes?q=the+bad+beginning+inauthor:snicket&key=AIzaSyBiLx_44pYN7U2zIl8DP7Uny0Nbb-7KaXk";
            System.out.println("b: " + newUrl);
            String json = GoogleBooksApi.fetchString(newUrl);
            GoogleBooksResult result = GSON.fromJson(json, GoogleBooksResult.class);
            System.out.println("The result has been processed");
            return Optional.<GoogleBooksResult>ofNullable(result);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            System.out.println("The result is empty");
            return Optional.<GoogleBooksResult>empty();
        } // try
    } // search

    /**
     * Returns the response body string data from a URI.
     * @param uri location of desired content
     * @return response body string
     * @throws IOException if an I/O error occurs when sending or receiving
     * @throws InterruptedException if the HTTP client's {@code send} method is
     *    interrupted
     */
    private static String fetchString(String uri) throws IOException, InterruptedException {
        System.out.println(uri);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .build();
        HttpResponse<String> response = HTTP_CLIENT
            .send(request, BodyHandlers.ofString());
        final int statusCode = response.statusCode();
        if (statusCode != 200) {
            System.out.println("The request did not work");
            throw new IOException("response status code not 200:" + statusCode);
        } // if
          System.out.println(response.body() + "test");
        return response.body();
    } // fetchString

} // OpenLibrarySearchApi
