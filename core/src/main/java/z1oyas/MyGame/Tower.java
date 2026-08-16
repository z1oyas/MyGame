package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Tower implements Person {

    public enum Direction { LEFT, RIGHT, UP, DOWN }

    // Базовый кадр tower_look1.png = 90×179 px ⇒ world units = px / 16 (unitScale 1/16f).
    public static final float TOWER_W = 90f / 16f;    // 5.625
    public static final float TOWER_H = 179f / 16f;   // 11.1875
    // Радиус зоны слежения: 1.5 × ширина вышки (см. Level_Mechanics.md).
    private static final float ALERT_RADIUS = TOWER_W * 1.5f;

    public final Vector2 position = new Vector2();

    private final Animation<TextureRegion> lookLeft;
    private final Animation<TextureRegion> lookRight;
    private final Animation<TextureRegion> lookUp;
    private final Animation<TextureRegion> lookDown;
    private final Animation<TextureRegion> foundAnimation;
    private Animation<TextureRegion> currentAnimation;

    private final Vector2 heroCenter = new Vector2();
    private final Vector2 towerCenter = new Vector2();

    private boolean caught;
    private boolean tracking;
    private Direction direction = Direction.LEFT;
    private float stateTime;
    private Rectangle form;

    public Tower(float x, float y) {
        position.set(x, y);
        lookLeft  = AnimationLoader.fromFiles("tower/tower_look1.png");
        lookRight = AnimationLoader.fromFiles("tower/tower_look2.png");
        lookUp    = AnimationLoader.fromFiles("tower/tower_look3.png");
        lookDown  = AnimationLoader.fromFiles("tower/tower_look4.png");
        foundAnimation = AnimationLoader.fromFiles(0.2f,
            "tower/tower_found1.png", "tower/tower_found2.png",
            "tower/tower_found3.png", "tower/tower_found4.png");
        currentAnimation = lookLeft;
        form = new Rectangle(x, y, TOWER_W, TOWER_H);
    }

    @Override
    public void render(Batch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime, true);
        batch.draw(frame, position.x, position.y,
            TOWER_W / 2f, TOWER_H / 2f, TOWER_W, TOWER_H, 1f, 1f, 0f);
    }

    @Override
    public void dispose() {
        AnimationLoader.dispose(lookLeft);
        AnimationLoader.dispose(lookRight);
        AnimationLoader.dispose(lookUp);
        AnimationLoader.dispose(lookDown);
        AnimationLoader.dispose(foundAnimation);
    }

    /**
     * Обновляет состояние вышки по хитбоксу героя.
     * Приоритет: caught (overlap) &gt; tracking (alert zone) &gt; idle (look_left).
     */
    public void update(Rectangle heroBounds) {
        heroCenter.set(heroBounds.x + heroBounds.width / 2f,
                       heroBounds.y + heroBounds.height / 2f);
        towerCenter.set(position.x + TOWER_W / 2f,
                        position.y + TOWER_H / 2f);

        caught = form.overlaps(heroBounds);
        tracking = !caught && towerCenter.dst(heroCenter) < ALERT_RADIUS;

        // Направление взгляда — по доминирующей оси относительно центра вышки.
        float dx = heroCenter.x - towerCenter.x;
        float dy = heroCenter.y - towerCenter.y;
        if (Math.abs(dx) >= Math.abs(dy)) {
            direction = dx < 0 ? Direction.LEFT : Direction.RIGHT;
        } else {
            direction = dy < 0 ? Direction.DOWN : Direction.UP;
        }

        if (caught) {
            currentAnimation = foundAnimation;
        } else if (tracking) {
            switch (direction) {
                case LEFT:  currentAnimation = lookLeft;  break;
                case RIGHT: currentAnimation = lookRight; break;
                case UP:    currentAnimation = lookUp;    break;
                case DOWN:  currentAnimation = lookDown;  break;
            }
        } else {
            currentAnimation = lookLeft;
        }
    }

    public boolean isCaught() { return caught; }
    public boolean isTracking() { return tracking; }
    public Rectangle getBounds() { return form; }
}
