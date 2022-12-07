package cs1302.omega;

import cs1302.game.DemoGame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class OmegaApp extends Application {

    Stage stage;
    VBox mainPane;
    TextField search;

    /**
     * Constructs an {@code OmegaApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public OmegaApp() {
        mainPane = new VBox();
        search = new TextField("Search for a book!");
    }

    @Override
    public void init() {
        mainPane.getChildren().add(search);
    }

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        // demonstrate how to load local asset using "file:resources/"
        Image bannerImage = new Image("file:resources/readme-banner.png");
        ImageView banner = new ImageView(bannerImage);
        banner.setPreserveRatio(true);
        banner.setFitWidth(640);


        // setup scene
        mainPane.getChildren().add(banner);
        Scene scene = new Scene(mainPane);

        // setup stage
        stage.setTitle("OmegaApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    //     /**
    //  * Represents an Open Library Search API document.
    //  */
    // private static class OpenLibraryDoc {
    //     String type;
    //     String title;
    // } // OpenLibraryDoc

    // /**
    //  * Represents an Open Library Search API result.
    //  */
    // private static class OpenLibraryResult {
    //     int numFound;
    //     OpenLibraryDoc[] docs;
    // } // OpenLibraryResult

    // /** HTTP client. */
    // public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
    //     .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
    //     .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
    //     .build();                                     // builds and returns a HttpClient object

    // /** Google {@code Gson} object for parsing JSON-formatted strings. */
    // public static Gson GSON = new GsonBuilder()
    //     .setPrettyPrinting()                          // enable nice output when printing
    //     .create();                                    // builds and returns a Gson object

    // private static final String ENDPOINT = "https://openlibrary.org/search.json";

    // public static void main(String[] args) {
    //     OpenLibrarySearchApi
    //         .search("the lord of the rings")
    //         .ifPresent(response -> example1(response));
    // } // main

    // /**
    //  * An example of some things you can do with a response.
    //  * @param result the ope library search result
    //  */
    // private static void example1(OpenLibraryResult result) {
    //     // print what we found
    //     System.out.printf("numFound = %d\n", result.numFound);
    //     for (OpenLibraryDoc doc: result.docs) {
    //         System.out.println(doc.title);
    //     } // for
    // } // example1

    // /**
    //  * Return an {@code Optional} describing the root element of the JSON
    //  * response for a "search" query.
    //  * @param q query string
    //  * @return an {@code Optional} describing the root element of the response
    //  */
    // public static Optional<OpenLibraryResult> search(String q) {
    //     System.out.printf("Searching for: %s\n", q);
    //     System.out.println("This may take some time to download...");
    //     try {
    //         String url =  String.format(
    //             "%s?q=%s",
    //             OpenLibrarySearchApi.ENDPOINT,
    //             URLEncoder.encode(q, StandardCharsets.UTF_8));
    //         String json = OpenLibrarySearchApi.fetchString(url);
    //         OpenLibraryResult result = GSON.fromJson(json, OpenLibraryResult.class);
    //         return Optional.<OpenLibraryResult>ofNullable(result);
    //     } catch (IllegalArgumentException | IOException | InterruptedException e) {
    //         return Optional.<OpenLibraryResult>empty();
    //     } // try
    // } // search

    // /**
    //  * Returns the response body string data from a URI.
    //  * @param uri location of desired content
    //  * @return response body string
    //  * @throws IOException if an I/O error occurs when sending or receiving
    //  * @throws InterruptedException if the HTTP client's {@code send} method is
    //  *    interrupted
    //  */
    // private static String fetchString(String uri) throws IOException, InterruptedException {
    //     HttpRequest request = HttpRequest.newBuilder()
    //         .uri(URI.create(uri))
    //         .build();
    //     HttpResponse<String> response = HTTP_CLIENT
    //         .send(request, BodyHandlers.ofString());
    //     final int statusCode = response.statusCode();
    //     if (statusCode != 200) {
    //         throw new IOException("response status code not 200:" + statusCode);
    //     } // if
    //     return response.body().trim();
    // } // fetchString

} // OmegaApp
