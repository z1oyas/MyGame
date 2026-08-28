package z1oyas.MyGame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

public abstract class Collectible {
    protected float x, y;           // позиция в мировых единицах
    protected float radius = 0.6f;  // радиус подбора (в мировых единицах)
    protected boolean collected = false;

    public Collectible(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // вызывается, когда герой подобрал предмет
    public abstract void onCollect(Hero hero);

    // что нарисовать
    public abstract void render(SpriteBatch batch);

    // проверка — герой рядом?
    public boolean overlaps(Rectangle heroBounds) {
        // простая проверка: центр предмета внутри бокса героя с небольшим запасом
        return heroBounds.contains(x, y)
                   || Math.abs(heroBounds.x + heroBounds.width / 2 - x) < radius
                          && Math.abs(heroBounds.y + heroBounds.height / 2 - y) < radius;
    }

    public boolean isCollected() {
        return collected;
    }
}
