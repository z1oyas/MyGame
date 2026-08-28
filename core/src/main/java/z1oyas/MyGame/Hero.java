package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

    private static final int MAX_HEALTH = 100;
    private static final float EAT_DURATION = 0.5f;

    private int health = MAX_HEALTH;
    private float invisibilityTimer = 0f;
    private float eatTimer = 0f;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> currentAnimation;
    private final Animation<TextureRegion> runUpAnimation;
    private final Animation<TextureRegion> runDownAnimation;
    private final Animation<TextureRegion> invisibleAnimation;
    private final Animation<TextureRegion> invisibleRunAnimation;
    private final Animation<TextureRegion> invisibleRunUpAnimation;
    private final Animation<TextureRegion> invisibleRunDownAnimation;
    private final Animation<TextureRegion> eatAnimation;
    private final Animation<TextureRegion> happyAnimation;
    private final Animation<TextureRegion> detectedAnimation;

    private enum MoveDir { IDLE, LEFT, RIGHT, UP, DOWN }
    private MoveDir lastDir = MoveDir.IDLE;
    private float stateTime;

    private final Vector2 position = new Vector2();
    private final Array<Rectangle> blockedZones;
    private final float mapWidth;
    private final float mapHeight;
    private Rectangle form;
    private boolean isMoving;
    private boolean isForward = true;
    private boolean finished = false;
    private boolean detected = false;

    public Hero(float x, float y, Array<Rectangle> blockedZones, float mapWidth, float mapHeight) {
        this.blockedZones = blockedZones;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        idleAnimation = AnimationLoader.fromFiles(0.25f,
            "gg2/idle1.png", "gg2/idle2.png");
        runAnimation = AnimationLoader.fromFiles(0.12f,
            "gg2/run1.png", "gg2/run2.png", "gg2/run3.png", "gg2/run4.png");
        currentAnimation = idleAnimation;
        runUpAnimation   = AnimationLoader.fromFiles(0.12f, "gg2/run_up1.png", "gg2/run_up2.png");
        runDownAnimation = AnimationLoader.fromFiles(0.12f, "gg2/run_down1.png", "gg2/run_down2.png");
        invisibleAnimation        = AnimationLoader.fromFiles(0.25f, "gg2/invisible_run1.png", "gg2/invisible_run2.png");
        invisibleRunAnimation     = AnimationLoader.fromFiles(0.12f, "gg2/invisible_run1.png", "gg2/invisible_run2.png");
        invisibleRunUpAnimation   = AnimationLoader.fromFiles(0.12f, "gg2/invisible_run_up1.png", "gg2/invisible_run_up2.png");
        invisibleRunDownAnimation = AnimationLoader.fromFiles(0.12f, "gg2/invisible_run_down1.png", "gg2/invisible_run_down2.png");
        eatAnimation = AnimationLoader.fromFiles(0.25f, "gg2/eat.png", "gg2/eat2.png");
        happyAnimation = AnimationLoader.fromFiles(0.25f, "gg2/happy_full_health.png");
        detectedAnimation = AnimationLoader.fromFiles(0.25f, "gg2/detected1.png", "gg2/detected2.png");

        position.set(x, y);
        form = new Rectangle(x, y, DRAW_W, DRAW_H);

        // Спавн вне проходимой зоны (по нижней кромке): предупреждение, но не блокируем спавн.
        boolean spawnInside = isInsideWalkable(position.x, position.y)
                                  && isInsideWalkable(position.x + DRAW_W / 2f, position.y)
                                  && isInsideWalkable(position.x + DRAW_W, position.y);
        if (!spawnInside) {
            Gdx.app.log("Hero", "Спавн (" + x + ", " + y + ") вне проходимой зоны");
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
        AnimationLoader.dispose(invisibleAnimation);
        AnimationLoader.dispose(invisibleRunAnimation);
        AnimationLoader.dispose(invisibleRunUpAnimation);
        AnimationLoader.dispose(invisibleRunDownAnimation);
        AnimationLoader.dispose(eatAnimation);
        AnimationLoader.dispose(happyAnimation);
        AnimationLoader.dispose(detectedAnimation);
    }

    public void moveTo(Vector2 direction) {
        if (finished) {
            currentAnimation = happyAnimation;
            return;
        }
        isMoving = !direction.isZero();

        if (!isMoving) {
            lastDir = MoveDir.IDLE;
        } else if (Math.abs(direction.y) > Math.abs(direction.x)) {
            if (direction.y > 0) lastDir = MoveDir.UP;
            else                 lastDir = MoveDir.DOWN;
        } else {
            if (direction.x > 0) { lastDir = MoveDir.RIGHT; isForward = true; }
            else                  { lastDir = MoveDir.LEFT;  isForward = false; }
        }

        // Приоритет анимации: eat -> detected -> невидимость -> обычная.
        if (eatTimer > 0) {
            currentAnimation = eatAnimation;
        } else if (detected && !isInvisible()) {
            currentAnimation = detectedAnimation;
        } else if (isInvisible()) {
            switch (lastDir) {
                case UP:   currentAnimation = invisibleRunUpAnimation; break;
                case DOWN: currentAnimation = invisibleRunDownAnimation; break;
                case LEFT:
                case RIGHT: currentAnimation = invisibleRunAnimation; break;
                case IDLE:
                default:   currentAnimation = invisibleAnimation; break;
            }
        } else {
            switch (lastDir) {
                case UP:   currentAnimation = runUpAnimation; break;
                case DOWN: currentAnimation = runDownAnimation; break;
                case LEFT:
                case RIGHT: currentAnimation = runAnimation; break;
                case IDLE:
                default:   currentAnimation = idleAnimation; break;
            }
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

    // Проходимость теперь определяется "от противного": всё внутри карты
    // и вне блокирующих прямоугольников считается доступным для ходьбы.
    private boolean isInsideWalkable(float cx, float cy) {
        if (cx < 0 || cy < 0 || cx > mapWidth || cy > mapHeight) return false;
        for (Rectangle block : blockedZones) {
            if (block.contains(cx, cy)) return false;
        }
        return true;
    }

    public Vector2 getPosition() { return position; }
    public Rectangle getBoundares() { return form; }

    public void heal(int amount) {
        health = Math.min(health + amount, MAX_HEALTH);
    }

    public void activateInvisibility(float duration) {
        invisibilityTimer = duration;
    }

    public void playEatAnimation() {
        eatTimer = EAT_DURATION;
        stateTime = 0f;
        currentAnimation = eatAnimation;
    }

    public void playHappyAnimation() {
        finished = true;
        stateTime = 0f;
        currentAnimation = happyAnimation;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    public boolean isInvisible() {
        return invisibilityTimer > 0;
    }

    public void update(float delta) {
        if (invisibilityTimer > 0) {
            invisibilityTimer = Math.max(invisibilityTimer - delta, 0f);
        }
        if (eatTimer > 0) {
            eatTimer = Math.max(eatTimer - delta, 0f);
        }
    }
}
