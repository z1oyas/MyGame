package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Hero implements Person {

    // Размер спрайта: отрисовка и коллизия совпадают (world units).
    // Ширина 2f соответствует ширине walkable-коридора (~2 тайла).
    private static final float DRAW_W = 2f;
    private static final float DRAW_H = 3f;

    // Скорость движения (world units в секунду).
    private static final float SPEED = 5f;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> currentAnimation;
    private final Animation<TextureRegion> runUpAnimation;
    private final Animation<TextureRegion> runDownAnimation;

    private enum MoveDir { IDLE, LEFT, RIGHT, UP, DOWN }
    private MoveDir lastDir = MoveDir.IDLE;
    private float stateTime;

    private final Vector2 position = new Vector2();
    private final Array<Polygon> walkableZones;
    private Rectangle form;
    private boolean isMoving;
    private boolean isForward = true;

    public Hero(float x, float y, Array<Polygon> walkableZones) {
        this.walkableZones = walkableZones;
        idleAnimation = AnimationLoader.fromFiles(0.25f,
            "gg2/idle1.png", "gg2/idle2.png");
        runAnimation = AnimationLoader.fromFiles(0.12f,
            "gg2/run1.png", "gg2/run2.png", "gg2/run3.png", "gg2/run4.png");
        currentAnimation = idleAnimation;
        runUpAnimation   = AnimationLoader.fromFiles(0.12f, "gg2/run_up1.png", "gg2/run_up2.png");
        runDownAnimation = AnimationLoader.fromFiles(0.12f, "gg2/run_down1.png", "gg2/run_down2.png");

        position.set(x, y);
        form = new Rectangle(x, y, DRAW_W, DRAW_H);

        // Спавн вне walkable-зоны (по нижней кромке): предупреждение, но не блокируем спавн.
        boolean spawnInside = isInsideWalkable(position.x, position.y)
                           && isInsideWalkable(position.x + DRAW_W / 2f, position.y)
                           && isInsideWalkable(position.x + DRAW_W, position.y);
        if (!spawnInside) {
            Gdx.app.log("Hero", "Спавн (" + x + ", " + y + ") вне walkable-зоны");
        }
    }

    @Override
    public void render(Batch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime, true);
        float sx = isForward ? 1f : -1f; // разворот через scaleX, без мутации кадров
        batch.draw(frame, position.x, position.y,
            DRAW_W / 2f, DRAW_H / 2f, DRAW_W, DRAW_H, sx, 1f, 0f);
    }

    @Override
    public void dispose() {
        AnimationLoader.dispose(idleAnimation);
        AnimationLoader.dispose(runAnimation);
        AnimationLoader.dispose(runUpAnimation);
        AnimationLoader.dispose(runDownAnimation);
    }

    public void moveTo(Vector2 direction) {
        isMoving = !direction.isZero();

        if (!isMoving) {
            lastDir = MoveDir.IDLE;
            currentAnimation = idleAnimation;
        } else if (Math.abs(direction.y) > Math.abs(direction.x)) {
            if (direction.y > 0) { lastDir = MoveDir.UP;   currentAnimation = runUpAnimation; }
            else                 { lastDir = MoveDir.DOWN;  currentAnimation = runDownAnimation; }
        } else {
            if (direction.x > 0) { lastDir = MoveDir.RIGHT; isForward = true; }
            else                  { lastDir = MoveDir.LEFT;  isForward = false; }
            currentAnimation = runAnimation;
        }

        if (isMoving) {
            float delta = Gdx.graphics.getDeltaTime();
            float nx = position.x + direction.x * SPEED * delta;
            float ny = position.y + direction.y * SPEED * delta;

            float bottomY = position.y;
            boolean canX = isInsideWalkable(nx,               bottomY)
                               && isInsideWalkable(nx + DRAW_W / 2f, bottomY)
                               && isInsideWalkable(nx + DRAW_W,      bottomY);

            boolean canY = isInsideWalkable(position.x,               ny)
                               && isInsideWalkable(position.x + DRAW_W / 2f, ny)
                               && isInsideWalkable(position.x + DRAW_W,      ny);

            if (canX) position.x = nx;
            if (canY) position.y = ny;
            form.setPosition(position);
        }
    }

    private boolean isInsideWalkable(float cx, float cy) {
        for (Polygon zone : walkableZones) {
            if (zone.contains(cx, cy)) return true;
        }
        return false;
    }

    public Vector2 getPosition() { return position; }
    public Rectangle getBoundares() { return form; }
}
