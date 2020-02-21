import org.jetbrains.annotations.NotNull;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;

public class MyAbilityExtension implements AbilityExtension {
    private SilentSender silent;
    DBContext db;
    String action = null;
    Place newPlace;
    PlaceType type;
    String name;
    Location location;


    public MyAbilityExtension(SilentSender silent, DBContext db) {
        this.silent = silent;
        this.db = db;
    }

    public Reply keyboard() {
        return Reply.of(update -> {
            if (!update.getMessage().equals("/start")) {
                action = update.getMessage().getText();
            }
            switch (update.getMessage().getText()) {
                case ("/start"):
                    DBManager dbManager = new DBManager(db);
                    silent.execute(Keyboard.addKeyboard(new String[]{"add place", "add comment",
                            "filter by type", "find nearby"}, update, "Chose action:"));
                    break;
                case ("add comment"):
                    silent.execute(Keyboard.addKeyboard(new String[]{"add new", "find by name",
                            "find on map"}, update, "Chose action:"));
                    break;
                case ("add place"):
                case ("add new"):
                    newPlace = new Place();
                    silent.execute(Keyboard.addKeyboard(PlaceType.toStringArray(),
                            update, "Choose type:"));
                    break;
            }
        }, update -> Arrays.asList(Constants.startKeyboardWords).contains(update.getMessage().getText()));
    }

    public Ability name() {
        return Ability
                .builder()
                .name("name")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    DBManager dbManager = new DBManager(db);
                    name = ctx.update().getMessage().getText();
                    if (action.equalsIgnoreCase("add new") || action.equalsIgnoreCase("add place")) {
                        newPlace.setName(name);
                        if (!newPlace.getLocation().equals(null) && !newPlace.getType().equals(null)) {
                            dbManager.updateDB(newPlace);
                            newPlace = null;
                            silent.send("New places was successfully added", ctx.chatId());
                            action = null;
                        }
                    }
                })
                .build();
    }

    public Reply location() {
        return Reply.of(update -> {
            DBManager dbManager = new DBManager(db);
            location = update.getMessage().getLocation();
            if (action.equalsIgnoreCase("add new") || action.equalsIgnoreCase("add place")) {
                newPlace.setLocation(location);
                if (!newPlace.getName().equals(null) && !newPlace.getType().equals(null)) {
                    dbManager.updateDB(newPlace);
                    newPlace = null;
                    silent.send("New places was successfully added", update.getMessage().getChatId());
                    action = null;
                }
            } else if (action.equalsIgnoreCase("find nearby")) {
                try {
                    silent.send("Places in this area:", update.getMessage().getChatId());
                    for (Place currentPlace : dbManager.searchByLocation(location, 3000)) {
                        String message = currentPlace.getName() + "\n" + currentPlace.getType().toString() + "\n";
                        silent.send(message + currentPlace.getLocation(), update.getMessage().getChatId());
                        location = null;
                    }
                } catch (DBManager.ThereIsNotPlacesInThisAreaException e) {
                    silent.send("There're no added places in this area", update.getMessage().getChatId());
                    e.printStackTrace();
                }
            }
        }, Flag.LOCATION);
    }


    public Reply type() {
        return Reply.of(update -> {
            DBManager dbManager = new DBManager(db);
            switch (update.getMessage().getText()) {
                case ("shop"):
                    type = PlaceType.SHOP;
                    break;
                case ("restaurant"):
                    type = PlaceType.RESTAURANT;
                    break;
                case ("business_center"):
                    type = PlaceType.BUSINESS_CENTER;
                    break;
                case ("bar"):
                    type = PlaceType.BAR;
                    break;
                case ("bank"):
                    type = PlaceType.BANK;
                    break;
            }
            if (action.equalsIgnoreCase("add new") || action.equalsIgnoreCase("add place")) {
                newPlace.setType(type);
                if (!newPlace.getName().equals(null) && !newPlace.getLocation().equals(null)) {
                    dbManager.updateDB(newPlace);
                    newPlace = null;
                    silent.send("New places was successfully added", update.getMessage().getChatId());
                    action = null;
                }
            } else if (action.equalsIgnoreCase("find by type")) {
                try {
                    for (Place currentPlace : dbManager.searchByType(type)) {
                        String message = currentPlace.getName() + "\n" + currentPlace.getType().toString() + "\n";
                        silent.send(message + currentPlace.getLocation(), update.getMessage().getChatId());
                        location = null;
                    }
                    ;
                } catch (DBManager.ThereIsNotSuchPlaceTypeException e) {
                    silent.send("There wasn't added places of such type", update.getMessage().getChatId());
                    e.printStackTrace();
                }
            }
        }, update -> Arrays.asList(Constants.types).contains(update.getMessage().getText()));
    }
}


