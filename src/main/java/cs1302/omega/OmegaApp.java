package cs1302.omega;

import cs1302.game.DemoGame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
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
    VBox authorSuggestions;
    HBox otherWorks;

    TextField titleSearch;
    TextField authorSearch;
    ImageView bookCover;
    Button searchButton;
    Image image;

    Text title;
    Text author;
    Text language;
    Text publishedDate;

    Text byAuthor;
    GridPane booksContainer;
    ImageView[] books;
    // ImageView book1;
    // ImageView book2;
    // ImageView book3;
    // ImageView book4;

    String authorName;
    String authorKey;
    String[] otherBooks;
    int loadedBooks;
    /**
     * Constructs an {@code OmegaApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public OmegaApp() {
        mainPane = new VBox();
        searchTools = new HBox();
        bookInfo = new HBox();
        bookStats = new VBox();
        authorSuggestions = new VBox();
        otherWorks = new HBox();

        titleSearch = new TextField("Search for a book");
        authorSearch = new TextField("Search for the author");
        searchButton = new Button("Find");
        bookCover = new ImageView();

        title = new Text("Title: ");
        author = new Text("Author: ");
        language = new Text("Language: ");
        publishedDate = new Text("Originally Published: ");

        byAuthor = new Text("More by author: ");
        booksContainer = new GridPane();
        books = new ImageView[4];
        for(int i = 0; i < books.length; i++) {
            books[i] = new ImageView();
        }
        // book1 = new ImageView();
        // book2 = new ImageView();
        // book3 = new ImageView();
        // book4 = new ImageView();

        otherBooks = new String[4];
        loadedBooks = 0;
    }

    @Override
    public void init() {
        image = new Image("https://arthurmillerfoundation.org/wp-content/uploads/2018/06/default-placeholder.png");
        bookCover.setImage(image);
        bookCover.setFitWidth(300);
        bookCover.setPreserveRatio(true);

        int i = 0;
        for (ImageView cover : books) {
            cover.setImage(image);
            cover.setFitWidth(150);
            cover.setPreserveRatio(true);
            booksContainer.add(cover, i, 0);
            i++;
        }



        // book1.setImage(image);
        // book1.setFitWidth(150);
        // book1.setPreserveRatio(true);

        // book2.setImage(image);
        // book2.setFitWidth(150);
        // book2.setPreserveRatio(true);

        // book3.setImage(image);
        // book3.setFitWidth(150);
        // book3.setPreserveRatio(true);

        // book4.setImage(image);
        // book4.setFitWidth(150);
        // book4.setPreserveRatio(true);

        searchTools.getChildren().addAll(titleSearch, authorSearch, searchButton);
        bookStats.getChildren().addAll(title, author, language, publishedDate);
        // otherWorks.getChildren().addAll(book1, book2, book3, book4);
        authorSuggestions.getChildren().addAll(byAuthor, booksContainer);
        bookInfo.getChildren().addAll(bookCover, bookStats);
        mainPane.getChildren().addAll(searchTools, bookInfo, authorSuggestions);

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

    private static class OpenLibraryDoc {
        String key;
    }

    private static class OpenLibraryAuthorResult {
        OpenLibraryDoc[] docs;
    }

    private static class OpenLibraryEntry {
        String title;
    }

    private static class OpenLibraryWorksResult {
        OpenLibraryEntry[] entries;
    }

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private static final String GOOGLE_ENDPOINT = "https://www.googleapis.com/books/v1/volumes";
    private static final String OPEN_LIBRARY_ENDPOINT = "https://openlibrary.org/search/authors";
    private static final String AUTHOR_WORKS_ENDPOINT = "https://openlibrary.org/authors/";

    private void loadBook(ActionEvent event) {
        this.googleBooksSearch(titleSearch.getText(), authorSearch.getText())
            .ifPresent(response -> loadContent(response));
        this.openLibraryAuthorSearch().ifPresent(response -> loadAuthorKey(response));
        this.openLibraryWorksSearch().ifPresent(response -> loadOtherBooks(response));
        System.out.println("1st cover picture outside of method: " + otherBooks[0]);
        for (int i = 0; i < otherBooks.length; i++) {
            System.out.println("url at position " + i + ": " + otherBooks[i]);
            Image otherCover = new Image(otherBooks[i]);
            books[i].setImage(otherCover);
        }

        // Image otherCover = new Image(otherBooks[0]);
        // books[0].setImage(otherCover);

        // otherCover = new Image(otherBooks[1]);
        // books[1].setImage(otherCover);

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
        author.setText("Author: " + info.authors[0]);
        language.setText("Language: " + info.language);
        publishedDate.setText("Originally Published: " + book.volumeInfo.publishedDate);
        authorName = info.authors[0];

        System.out.println(book1.volumeInfo.authors[0]);
        System.out.println(info.authors[0]);
        System.out.println(book1.volumeInfo.imageLinks.thumbnail);

        if (info.imageLinks != null) {
            image = new Image(info.imageLinks.thumbnail);
        } else {
            image = new Image(book1.volumeInfo.imageLinks.thumbnail);
        }

        //     if (book1.volumeInfo.authors[0] == info.authors[0]) {
        //     image = new Image(book1.volumeInfo.imageLinks.thumbnail);
        // } else {
        //     System.out.println("no available thumnail");
        // }

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
    private Optional<GoogleBooksResult> googleBooksSearch(String title, String author) {
        System.out.printf("Searching for: %s\n", title);
        System.out.println("This may take some time to download...");
        try {
            String url =  String.format(
                "%s?q=%s+%s%s",
                this.GOOGLE_ENDPOINT,
                URLEncoder.encode(title, StandardCharsets.UTF_8),
                "inauthor:",
                URLEncoder.encode(author, StandardCharsets.UTF_8));
            url += "&key=" + apiKey;
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

    private void loadAuthorKey(OpenLibraryAuthorResult result) {
        authorKey = result.docs[0].key;
        System.out.println("authorKey: " + authorKey);
    }

     /**
     * Return an {@code Optional} describing the root element of the JSON
     * response for a "search" query.
     * @param q query string
     * @return an {@code Optional} describing the root element of the response
     */
    public Optional<OpenLibraryAuthorResult> openLibraryAuthorSearch() {
        System.out.printf("Searching for: %s\n", authorName);
        System.out.println("This may take some time to download...");
        try {
            String url =  String.format(
                "%s%s?q=%s",
                this.OPEN_LIBRARY_ENDPOINT,
                ".json",
                URLEncoder.encode(authorName, StandardCharsets.UTF_8).replace("+", "%20"));
            String json = this.fetchString(url);
            OpenLibraryAuthorResult result = GSON.fromJson(json, OpenLibraryAuthorResult.class);
            return Optional.<OpenLibraryAuthorResult>ofNullable(result);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            return Optional.<OpenLibraryAuthorResult>empty();
        } // try
    } // search

    private void loadOtherBooks(OpenLibraryWorksResult result) {
        int index = 0;
        for (int i = 0; index < 4; i++) {
            final int indexExt = index;
            System.out.println("Other title: " + result.entries[i].title);
            System.out.println("by this author: " + authorName);
            this.googleBooksSearch(result.entries[i].title, authorName)
                .ifPresent(response -> loadImageURL(response, indexExt));
            if (otherBooks[index] != null) {
                index++;
            }
        }
    }

    private void loadImageURL(GoogleBooksResult result, int index) {
        if (result.items != null) {
            if (result.items[0].volumeInfo.imageLinks != null) {
                System.out.println("imageLink.thumbnail: " + result.items[0].volumeInfo.imageLinks.thumbnail);
                otherBooks[index] = result.items[0].volumeInfo.imageLinks.thumbnail;
                System.out.println("value should be changed: " + otherBooks[index]);
            } else {
                otherBooks[index] = null;
            }
        }
        System.out.println("loaded url: " + otherBooks[index]);
    }

    /**
     * Return an {@code Optional} describing the root element of the JSON
     * response for a "search" query.
     * @param q query string
     * @return an {@code Optional} describing the root element of the response
     */
    public Optional<OpenLibraryWorksResult> openLibraryWorksSearch() {
        System.out.printf("Searching for: %s\n", authorKey);
        System.out.println("This may take some time to download...");
        try {
            String url = this.AUTHOR_WORKS_ENDPOINT + authorKey + "/works.json";
            String json = this.fetchString(url);
            OpenLibraryWorksResult result = GSON.fromJson(json, OpenLibraryWorksResult.class);
            return Optional.<OpenLibraryWorksResult>ofNullable(result);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            return Optional.<OpenLibraryWorksResult>empty();
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
        System.out.println(response.body());
        return response.body();

    } // fetchString

} // OmegaApp
