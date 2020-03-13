import org.jetbrains.annotations.NotNull;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyAbilityExtension implements AbilityExtension {
    private SilentSender silent;
    private DBManager dbManager;
    public class Interaction {
        private String action;
        private PlaceType type;
        private String name;
        private Location location;

        Interaction () { super(); };

        public void cleanInteraction () {
            action = null;
            type = null;
            name = "";
            location = null;
        }
    }
    private Map<Long, Interaction> interactionMap = new HashMap<>();


    public MyAbilityExtension(SilentSender silent, DBContext db) {
        this.silent = silent;
        dbManager = new DBManager(db);
    }

    public Ability start() {
        return Ability
                .builder()
                .name("start")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    interactionMap.put(ctx.chatId(), new Interaction());
                    silent.execute(Keyboard.addKeyboard(new String[]{"add place", "add comment",
                            "filter by type", "find nearby"}, ctx.update(), "Chose action:"));
                })
                .build();
    }

    public Reply keyboard() {
        return Reply.of(update -> {
            Interaction interaction = interactionMap.get(update.getMessage().getChatId());
            switch (update.getMessage().getText()) {
                case ("add comment"):
                    interaction.action = "add comment";
                    silent.execute(Keyboard.addKeyboard(new String[]{"add place", "find by name",
                            "find on map"}, update, "Chose action:"));
                    break;
                case ("add place"):
                    interaction.action = "add place";
                    silent.execute(Keyboard.addKeyboard(PlaceType.toStringArray() ,
                            update, "Choose type:"));
                    break;
                case ("filter by type"):
                    interaction.action = "filter by type";
                    break;
                case ("find nearby"):
                    interaction.action = "find nearby";
                    silent.send("Send your location with a point on map", update.getMessage().getChatId());
                    break;
            }
            interactionMap.put(update.getMessage().getChatId(), interaction);
        }, update -> Arrays.asList(Constants.startKeyboardWords).contains(update.getMessage().getText()));
    }

    public Ability name() {
        return Ability
                .builder()
                .name("name")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    Interaction interaction = interactionMap.get(ctx.chatId());
                    interaction.name="";
                    for (int i = 0; i < ctx.arguments().length; i++){
                        interaction.name += ctx.arguments()[i]+" ";
                    }
                    if (interaction.action.equalsIgnoreCase("add place")) {
                        silent.send("Send a location of the place with a point on map", ctx.chatId());
                        if (interaction.location != null && interaction.type != null) {
                            dbManager.updateDB(new Place(interaction.type, interaction.name, interaction.location));
                            silent.send("New places was successfully added! :)", ctx.chatId());
                            interaction.cleanInteraction();
                            interactionMap.put(ctx.chatId(), interaction);
                        }
                    }
                })
                .build();
    }

    public Reply location() {
        return Reply.of(update -> {
            Interaction interaction = interactionMap.get(update.getMessage().getChatId());
            interaction.location = update.getMessage().getLocation();
            if (interaction.action.equalsIgnoreCase("add place")) {
                interactionMap.put(update.getMessage().getChatId(), interaction);
                if (interaction.name != null && interaction.type != null) {
                    dbManager.updateDB(new Place(interaction.type, interaction.name, interaction.location));
                    silent.send("New places was successfully added! :)", update.getMessage().getChatId());
                    interaction.cleanInteraction();
                    interactionMap.put(update.getMessage().getChatId(), interaction);
                }
            } else if (interaction.action.equalsIgnoreCase("find nearby")) {
                try {
                    silent.send("3 nearest places in this area:", update.getMessage().getChatId());
                    for (Place currentPlace : dbManager.searchByLocation(interaction.location)) {
                        String message = currentPlace.getName() + "\n" + currentPlace.getType().name();
                        silent.send(message, update.getMessage().getChatId());
                        SendLocation sendLocation = new SendLocation().setLatitude(currentPlace.getLocation().getLatitude());
                        sendLocation.setLongitude(currentPlace.getLocation().getLongitude());
                        silent.execute(sendLocation.setChatId(update.getMessage().getChatId()));
                    }
                    interaction.location = null;
                    interactionMap.put(update.getMessage().getChatId(), interaction);
                } catch (ThereIsNotPlacesInThisAreaException e) {
                    silent.send("There're no added places in this area", update.getMessage().getChatId());
                    e.printStackTrace();
                }
            }
            silent.execute(Keyboard.addKeyboard(new String[]{"add place", "add comment",
                            "filter by type", "find nearby"}, ctx.update(), "Chose action:"));
        }, Flag.LOCATION);
    }


    public Reply type() {
        return Reply.of(update -> {
            Interaction interaction = interactionMap.get(update.getMessage().getChatId());
            switch (update.getMessage().getText()) {
                case ("shop"):
                    interaction.type = PlaceType.SHOP;
                    break;
                case ("restaurant"):
                    interaction.type = PlaceType.RESTAURANT;
                    break;
                case ("business_center"):
                    interaction.type = PlaceType.BUSINESS_CENTER;
                    break;
                case ("bar"):
                    interaction.type = PlaceType.BAR;
                    break;
                case ("bank"):
                    interaction.type = PlaceType.BANK;
                    break;
                case ("back"):
                    silent.execute(Keyboard.addKeyboard(new String[]{"add place", "add comment",
                            "filter by type", "find nearby"}, update, "Chose action:"));
            }
            interactionMap.put(update.getMessage().getChatId(), interaction);
            if (interaction.action.equalsIgnoreCase("add place")) {
                interactionMap.put(update.getMessage().getChatId(), interaction);
                silent.send("Send name of the place with /name + it's name", update.getMessage().getChatId());
                if (interaction.name != null && interaction.location != null) {
                    dbManager.updateDB(new Place(interaction.type, interaction.name, interaction.location));
                    silent.send("New places was successfully added", update.getMessage().getChatId());
                    interaction.cleanInteraction();
                    interactionMap.put(update.getMessage().getChatId(), interaction);
                }
            } else if (interaction.action.equalsIgnoreCase("find by type")) {
                try {
                    for (Place currentPlace : dbManager.searchByType(interaction.type)) {
                        String message = currentPlace.getName() + "\n" + currentPlace.getType().toString() + "\n";
                        silent.send(message + currentPlace.getLocation(), update.getMessage().getChatId());
                        interaction.location = null;
                    }
                    ;
                } catch (ThereIsNotSuchPlaceTypeException e) {
                    silent.send("There wasn't added places of such type", update.getMessage().getChatId());
                    e.printStackTrace();
                }
            }
        }, update -> Arrays.asList(Constants.types).contains(update.getMessage().getText()));
    }
}
