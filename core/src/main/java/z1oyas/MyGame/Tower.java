package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Tower implements Person {

    // Порядок патрулирования: LEFT → DOWN → RIGHT → UP → LEFT.
    public enum Direction {
        LEFT, DOWN, RIGHT, UP;

        public Direction next() {
            switch (this) {
                case LEFT:  return DOWN;
                case DOWN:  return RIGHT;
                case RIGHT: return UP;
                default:    return LEFT; // UP → LEFT
            }
        }
    }

    // Размер вышки: отрисовка и коллизия совпадают (world units).
    private static final float DRAW_W = 5f;
    private static final float DRAW_H = 10f;
    // Радиус зоны обнаружения: 1.5 × ширина вышки (см. Level_Mechanics.md).
    private static final float ALERT_RADIUS = DRAW_W * 1.5f;
    // Время удержания одного направления патруля (секунды).
    private static final float PATROL_INTERVAL = 1.5f;

    public final Vector2 position = new Vector2();

    private final Animation<TextureRegion> lookLeft;
    private final Animation<TextureRegion> lookRight;
    private final Animation<TextureRegion> lookUp;
    private final Animation<TextureRegion> lookDown;
    private final Animation<TextureRegion> foundAnimation;
    private Animation<TextureRegion> currentSprite;

    private final Vector2 heroCenter = new Vector2();
    private final Vector2 towerCenter = new Vector2();

    private Direction patrolDirection = Direction.LEFT;
    private Direction lastFoundDirection; // null = герой вне зоны
    private float patrolTimer;
    private float stateTime;
    private Rectangle form;

    public Tower(float x, float y) {
        position.set(x, y);
        lookLeft  = AnimationLoader.fromFiles(1f, "tower/tower_look1.png");
        lookRight = AnimationLoader.fromFiles(1f, "tower/tower_look2.png");
        lookUp    = AnimationLoader.fromFiles(1f, "tower/tower_look3.png");
        lookDown  = AnimationLoader.fromFiles(1f, "tower/tower_look4.png");
        // Отдельных foundLeft/Right/Up/Down в assets нет — один набор found-кадров на все направления.
        foundAnimation = AnimationLoader.fromFiles(0.2f,
            "tower/tower_found1.png", "tower/tower_found2.png",
            "tower/tower_found3.png", "tower/tower_found4.png");
        currentSprite = lookLeft;
        form = new Rectangle(x, y, DRAW_W, DRAW_H);
    }

    @Override
    public void render(Batch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion frame = currentSprite.getKeyFrame(stateTime, true);
        batch.draw(frame, position.x, position.y,
            DRAW_W / 2f, DRAW_H / 2f, DRAW_W, DRAW_H, 1f, 1f, 0f);
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
     * Патруль по циклу LEFT→DOWN→RIGHT→UP; при герое в радиусе ALERT_RADIUS —
     * смотрит на него и показывает found-анимацию (таймер патруля не тикает).
     */
    public void update(Rectangle heroBounds) {
        heroCenter.set(heroBounds.x + heroBounds.width / 2f,
                       heroBounds.y + heroBounds.height / 2f);
        towerCenter.set(position.x + DRAW_W / 2f,
                        position.y + DRAW_H / 2f);

        float dx = heroCenter.x - towerCenter.x;
        float dy = heroCenter.y - towerCenter.y;
        boolean heroInRange = towerCenter.dst(heroCenter) < ALERT_RADIUS;

        if (heroInRange) {
            Direction foundDir;
            if (Math.abs(dx) >= Math.abs(dy)) {
                foundDir = dx < 0 ? Direction.LEFT : Direction.RIGHT;
            } else {
                foundDir = dy > 0 ? Direction.UP : Direction.DOWN;
            }
            lastFoundDirection = foundDir;
            currentSprite = foundAnimation; // патрульный таймер не продвигается
        } else {
            if (lastFoundDirection != null) {
                // герой только что вышел из зоны: продолжаем патруль с его направления
                patrolDirection = lastFoundDirection;
                patrolTimer = 0f;
                lastFoundDirection = null;
            }
            currentSprite = look(patrolDirection);
            patrolTimer += Gdx.graphics.getDeltaTime();
            if (patrolTimer >= PATROL_INTERVAL) {
                patrolDirection = patrolDirection.next();
                patrolTimer = 0f;
            }
        }
    }

    private Animation<TextureRegion> look(Direction d) {
        switch (d) {
            case LEFT:  return lookLeft;
            case DOWN:  return lookDown;
            case RIGHT: return lookRight;
            case UP:    return lookUp;
            default:    return lookLeft; // недостижимо
        }
    }

    public boolean isFindingHero() { return lastFoundDirection != null; }
    public Rectangle getBounds() { return form; }
}
