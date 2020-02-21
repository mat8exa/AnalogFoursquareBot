import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.util.*;

public class DBManager {
    private Map<PlaceType, Set<Place>> placeTypeMap;

    DBManager(DBContext db) {
        Map<PlaceType, Set<Place>> placeTypeMap = db.getMap("placeMap");
    }

    public Set<Place> searchByType(PlaceType type) throws ThereIsNotSuchPlaceTypeException {
        switch (type) {
            case SHOP:
                return placeTypeMap.get(PlaceType.SHOP);
            case BAR:
                return placeTypeMap.get(PlaceType.BAR);
            case BANK:
                return placeTypeMap.get(PlaceType.BANK);
            case RESTAURANT:
                return placeTypeMap.get(PlaceType.RESTAURANT);
            case BUSINESS_CENTER:
                return placeTypeMap.get(PlaceType.BUSINESS_CENTER);
        }
        throw new ThereIsNotSuchPlaceTypeException();
    }

    public Set<Place> searchByLocation(Location location, double radius) throws ThereIsNotPlacesInThisAreaException {
        Set<Place> localPlacesSet = new HashSet<>();
        for (Map.Entry<PlaceType, Set<Place>> entry : placeTypeMap.entrySet()) {
            for (Place place : entry.getValue()) {
                if (place.getDistance(location) <= radius) {
                    localPlacesSet.add(place);
                }
            }
        }
        if (!localPlacesSet.isEmpty()) {
            return localPlacesSet;
        }
        throw new ThereIsNotPlacesInThisAreaException();
    }

    public void updateDB(Place place) {
        placeTypeMap.get(place.getType()).add(place);
    }

    static class ThereIsNotSuchPlaceTypeException extends Exception {
        ThereIsNotSuchPlaceTypeException() {
            System.out.println("There isn't such type of place");
        }
    }

    static class ThereIsNotPlacesInThisAreaException extends Exception {
        ThereIsNotPlacesInThisAreaException() {
            System.out.println("There is no places in this area");
        }
    }

}
