package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Candy extends Collectible {
    private final Texture texture;
    private static final float INVISIBILITY_DURATION = 5f; // секунд

    public Candy(float x, float y) {
        super(x, y);
        texture = new Texture(Gdx.files.internal("items/candy" + MathUtils.random(1, 3) + "_on_field.png"));
    }

    @Override
    public void onCollect(Hero hero) {
        collected = true;
        hero.playEatAnimation();
        hero.activateInvisibility(INVISIBILITY_DURATION);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!collected) {
            batch.draw(texture, x - 0.4f, y - 0.4f, 1.5f, 1.5f);
        }
    }

    public void dispose() {
        texture.dispose();
    }
}
