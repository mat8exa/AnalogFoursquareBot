import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Place implements Serializable {
    //private long placeID;
    private PlaceType type;
    private Location location;
    // private List<Long> sameCompany = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    //private String description;
    private String name;

    Place() {
    }
    Place(PlaceType type, String name, Location location) {
        this.type = type;
        this.name = name;
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*public void setDescription(String description) {
        this.description = description;
    }*/

    public void setLocation(Location location) { this.location = location; }

    public void setType(PlaceType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public Location getLocation() {
        return location;
    }

    public PlaceType getType() {
        return type;
    }

    /*public String getDescription() {
        return description;
    }*/

    public void addComment(String title, String text, String name) {
        Comment comment = new Comment(title, text);
        comments.add(comment);
    }

    public double getDistance(Location location) {
        float r = 6371;
        double dist = r * 2 * Math.asin(Math.sqrt((Math.pow(Math.sin((location.getLatitude()
                - this.location.getLatitude()) / 2), 2)
                + Math.cos(location.getLatitude()) * Math.cos(this.location.getLatitude())
                * Math.pow(Math.sin((location.getLongitude() - this.location.getLongitude()) / 2), 2))));
        return dist;
    }
}