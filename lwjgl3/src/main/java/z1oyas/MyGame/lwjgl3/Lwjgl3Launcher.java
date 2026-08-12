package z1oyas.MyGame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import z1oyas.MyGame.Main;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("MyGame");
        config.setWindowedMode(800, 480);
        config.setResizable(false);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new Main(), config);
    }
}
