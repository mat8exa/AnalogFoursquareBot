import java.io.Serializable;

public class Comment implements Serializable {
    private String title;
    private String text;

    Comment(String title, String text) {
        this.title = title;
        this.text = text;
    }
}