package z1oyas.MyGame;

public enum Level {
    LEVEL_1("map_title.tmx"),
    LEVEL_2("map_level2.tmx");

    public final String mapPath;
    Level(String mapPath) { this.mapPath = mapPath; }
}
