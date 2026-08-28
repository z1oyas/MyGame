package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Berry extends Collectible {
    private final Texture texture;
    private static final int HEAL_AMOUNT = 20;

    public Berry(float x, float y) {
        super(x, y);
        texture = new Texture(Gdx.files.internal("items/berry" + MathUtils.random(1, 3) + "_on_field.png"));
    }

    @Override
    public void onCollect(Hero hero) {
        collected = true;
        hero.playEatAnimation();
        hero.heal(HEAL_AMOUNT);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!collected) {
            // 1x1 мировая единица = 1 тайл, размер предмета 0.8x0.8
            batch.draw(texture, x - 0.4f, y - 0.4f, 1.5f, 1.5f);
        }
    }

    public void dispose() {
        texture.dispose();
    }
}
