public interface Constants {
    enum startKeyboardAnswers {
        SELECT("Select");

        String select;

        startKeyboardAnswers(String select) {
            this.select = select;
        }

        @Override
        public String toString() {
            return select;
        }
    }

    String[] startKeyboardWords = new String[]{
            "add place",
            "add comment",
            "find nearby",
            "filter by type",
            "find by name",
            "add new"
    };

    String[] types = new String[]{
            "bar",
            "restaurant",
            "bank",
            "shop",
            "business_center",
            "back"
    };

    String[] addingKeyboardWords = new String[]{
            "add name",
            "add description",
            "add location"
    };
}