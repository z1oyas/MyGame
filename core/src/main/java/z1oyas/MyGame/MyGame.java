package z1oyas.MyGame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MyGame extends Game {
    public SpriteBatch batch; // общий batch на все экраны

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose(); // задиспоузит текущий screen
        batch.dispose();
    }
}
