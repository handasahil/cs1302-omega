package cs1302.omega;

import cs1302.game.DemoGame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

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
    HBox searchTools;
    HBox bookInfo;
    VBox bookStats;

    TextField titleSearch;
    TextField authorSearch;
    ImageView bookCover;
    Button searchButton;
    Image image;

    Text title;
    Text author;
    Text language;
    Text publishedDate;

    /**
     * Constructs an {@code OmegaApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public OmegaApp() {
        mainPane = new VBox();
        searchTools = new HBox();
        bookInfo = new HBox();
        bookStats = new VBox();

        titleSearch = new TextField("Search for a book");
        authorSearch = new TextField("Search for the author");
        searchButton = new Button("Find");
        bookCover = new ImageView();

        title = new Text("Title: ");
        author = new Text("Author: ");
        language = new Text("Language: ");
        publishedDate = new Text("Originally Published: ");
    }

    @Override
    public void init() {
        image = new Image("https://arthurmillerfoundation.org/wp-content/uploads/2018/06/default-placeholder.png");
        bookCover.setImage(image);
        bookCover.setFitWidth(300);
        bookCover.setPreserveRatio(true);

        searchTools.getChildren().addAll(titleSearch, authorSearch, searchButton);
        bookStats.getChildren().addAll(title, author, language, publishedDate);
        bookInfo.getChildren().addAll(bookCover, bookStats);
        mainPane.getChildren().addAll(searchTools, bookInfo);

        EventHandler<ActionEvent> mouseClickHandler = (ActionEvent e) -> {
             this.loadBook(e);
        };

        searchButton.setOnAction(mouseClickHandler);
    }

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        Scene scene = new Scene(mainPane);

        // setup stage
        stage.setTitle("OmegaApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    private static final String apiKey = "AIzaSyBiLx_44pYN7U2zIl8DP7Uny0Nbb-7KaXk";

    private static class VolumeInfo {
        String title;
        String[] authors;
        String publishedDate;
        ImageLinks imageLinks;
        String language;
    }

    private static class ImageLinks {
        String thumbnail;
    }

    /**
     * Represents a Google Books API item.
     */
    private static class GoogleBooksItem{
        String kind;
        String id;
        VolumeInfo volumeInfo;
    } // OpenLibraryDoc

    /**
     * Represents a Google Books API result.
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


    private void loadBook(ActionEvent event) {
        this.search(titleSearch.getText(), authorSearch.getText())
            .ifPresent(response -> loadContent(response));
    }

    /**
     * An example of some things you can do with a response.
     * @param result the ope library search result
     */
    private void loadContent(GoogleBooksResult result) {
        GoogleBooksItem book = result.items[0];
        GoogleBooksItem book1 = result.items[1];
        VolumeInfo info = book.volumeInfo;

        title.setText("Title: " + info.title);
        author.setText("Author: " + book.volumeInfo.authors[0]);
        language.setText("Language: " + book.volumeInfo.language);
        publishedDate.setText("Originally Published: " + book.volumeInfo.publishedDate);

        if (info.imageLinks != null) {
            image = new Image(info.imageLinks.thumbnail);
        } else if (book1.volumeInfo.authors[0] == info.authors[0]
            && book1.volumeInfo.imageLinks != null) {
            image = new Image(book1.volumeInfo.imageLinks.thumbnail);
        } else {
            System.out.println("no available thumnail");
        }
        System.out.println("image url: " + result.items[0].volumeInfo.imageLinks.thumbnail);
        bookCover.setImage(image);
        System.out.println("first title: " + result.items[0].volumeInfo.title);
        for (GoogleBooksItem item : result.items) {
            System.out.println("NEW BOOK!!!!!!!!!!!!!!!!!!!!");
            System.out.println(item.volumeInfo.title);
        } // for
    } // loadContent

        /**
     * Return an {@code Optional} describing the root element of the JSON
     * response for a "search" query.
     * @param q query string
     * @return an {@code Optional} describing the root element of the response
     */
    private Optional<GoogleBooksResult> search(String title, String author) {
        System.out.printf("Searching for: %s\n", title);
        System.out.println("This may take some time to download...");
        try {
            String url =  String.format(
                "%s?q=%s+%s%s",
                this.ENDPOINT,
                URLEncoder.encode(title, StandardCharsets.UTF_8),
                "inauthor:",
                URLEncoder.encode(author, StandardCharsets.UTF_8));
            url += "&key=" + apiKey;
            // System.out.println("a: " + url);
//            String newUrl = "https://www.googleapis.com/books/v1/volumes?q=the+bad+beginning+inauthor:snicket&key=AIzaSyBiLx_44pYN7U2zIl8DP7Uny0Nbb-7KaXk";
            System.out.println("b: " + url);
            String json = this.fetchString(url);
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
    private String fetchString(String uri) throws IOException, InterruptedException {
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
//        System.out.println(response.body());
        return response.body();

    } // fetchString

} // OmegaApp
