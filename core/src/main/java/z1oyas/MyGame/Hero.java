package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Hero implements Person {

    // Хитбокс: базовый кадр gg2/idle1.png = 145×204 px ⇒ world units = px / 16 (unitScale 1/16f).
    public static final float HERO_W = 145f / 16f;  // 9.0625
    public static final float HERO_H = 204f / 16f;  // 12.75

    // Визуальный размер спрайта (world units); коллизия остаётся HERO_W × HERO_H.
    private static final float DRAW_W = 2f;
    private static final float DRAW_H = 3f;

    // Скорость движения (world units в секунду).
    private static final float SPEED = 5f;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> currentAnimation;
    private float stateTime;

    private final Vector2 position = new Vector2();
    private Rectangle form;
    private boolean isMoving;
    private boolean isForward = true;

    public Hero(float x, float y) {
        idleAnimation = AnimationLoader.fromFiles(0.25f,
            "gg2/idle1.png", "gg2/idle2.png");
        runAnimation = AnimationLoader.fromFiles(0.12f,
            "gg2/run1.png", "gg2/run2.png", "gg2/run3.png", "gg2/run4.png");
        currentAnimation = idleAnimation;

        position.set(x, y);
        form = new Rectangle(x, y, HERO_W, HERO_H);
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
    }

    public void moveTo(Vector2 direction) {
        isMoving = !direction.isZero();
        if (direction.x > 0) isForward = true;
        else if (direction.x < 0) isForward = false;
        currentAnimation = isMoving ? runAnimation : idleAnimation;

        if (isMoving) {
            float delta = Gdx.graphics.getDeltaTime();
            position.add(direction.x * SPEED * delta, direction.y * SPEED * delta);
            form.setPosition(position);
        }
    }

    public Vector2 getPosition() { return position; }
    public Rectangle getBoundares() { return form; }
}
