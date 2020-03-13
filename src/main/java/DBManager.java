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

    public List<Place> searchByLocation(Location location) throws ThereIsNotPlacesInThisAreaException {
        ArrayList<Place> localPlaces = new ArrayList<>();
        for (Map.Entry<PlaceType, Set<Place>> entry : placeTypeMap.entrySet()) {
            for (Place place : entry.getValue()) {
                localPlaces.add(place);
            }
        }
        if (!localPlaces.isEmpty()) {
            Collections.sort(localPlaces,(place1, place2) -> {
                return (int) Math.floor((place1.getDistance(location) - place2.getDistance(location)) * 100);
            });
            if (localPlaces.size() >= 3) {
                return localPlaces.subList(0,3);
            }
            return localPlaces.subList(0, localPlaces.size());
        }
        throw new ThereIsNotPlacesInThisAreaException();
    }

    public void updateDB(Place place){
        Set<Place> set = null;
        if (placeTypeMap != null) {
            set = placeTypeMap.get(place.getType());
        } else {
            placeTypeMap = new HashMap<>();
        }
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(place);
        placeTypeMap.put(place.getType(), set);
    }
}