public enum PlaceType {
    SHOP,
    RESTAURANT,
    BAR,
    BUSINESS_CENTER,
    BANK;

    public static String[] toStringArray() {
        return new String[]{"shop", "restaurant", "bar", "business_center", "bank", "back"};
    }
}