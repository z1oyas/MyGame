package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
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
    private static final float ALERT_RADIUS = DRAW_W * 2f;
    // Время удержания одного направления патруля (секунды).
    private static final float PATROL_INTERVAL = 1.5f;

    // Насколько быстро конус света поворачивается к новому направлению (градусов/сек).
    private static final float LIGHT_TURN_SPEED = 220f;

    // Половина угла раствора конуса (градусы). Подбери под ширину своей текстуры light.png —
    // если конус на глаз кажется шире/уже реальной зоны обнаружения, поменяй это число.
    private static final float LIGHT_HALF_FOV_DEG = 38f;

    // Прозрачность конуса в режиме патруля / когда герой обнаружен.
    private static final float LIGHT_ALPHA_PATROL = 0.28f;
    private static final float LIGHT_ALPHA_FOUND  = 0.55f;

    // Цвет света в патруле (жёлтый) и в состоянии тревоги (красный).
    private static final float PATROL_R = 1f,    PATROL_G = 0.95f, PATROL_B = 0.55f;
    private static final float ALERT_R  = 1f,    ALERT_G  = 0.15f, ALERT_B  = 0.15f;
    // Скорость перехода цвета патруль↔тревога (доля пути в секунду; больше = быстрее).
    private static final float LIGHT_COLOR_LERP_SPEED = 6f;

    // Текущий цвет света (реально отрисовываемый — плавно едет к целевому).
    private float currentLightR = PATROL_R;
    private float currentLightG = PATROL_G;
    private float currentLightB = PATROL_B;

    public final Vector2 position = new Vector2();

    // Статичный спрайт корпуса башни (один ассет — направление больше не влияет на спрайт башни).
    private final TextureRegion towerMain;
    // Спрайт-конус света (картинка "веер" с вершиной внизу).
    private final TextureRegion lightCone;
    // Ширина/высота текстуры конуса в world units — подбери под свой арт.
    private final float lightConeWidth;
    private final float lightConeHeight;

    private final Animation<TextureRegion> foundAnimation;
    private boolean showingFoundSprite;

    private final Vector2 heroCenter = new Vector2();
    private final Vector2 towerCenter = new Vector2();

    private Direction patrolDirection = Direction.LEFT;
    private Direction lastFoundDirection; // null = герой вне зоны
    private float patrolTimer;
    private float stateTime;
    private Rectangle form;

    // Угол конуса света: текущий (реально отрисовываемый) и целевой (куда должен повернуться).
    // 0° соответствует направлению UP — исходной ориентации текстуры конуса (вершина внизу, веер вверх).
    private float currentLightAngle = directionToAngle(Direction.LEFT);
    private float targetLightAngle  = currentLightAngle;

    public Tower(float x, float y) {
        position.set(x, y);

        towerMain = new TextureRegion(new Texture(Gdx.files.internal("tower/tower_main.png")));
        lightCone = new TextureRegion(new Texture(Gdx.files.internal("tower/light.png")));

        // ВАЖНО: конус рисуется от вершины (origin внизу), поэтому lightConeHeight —
        // это и есть физическая дальность света. Она должна СОВПАДАТЬ с ALERT_RADIUS,
        // иначе видимый свет и реальная зона обнаружения разъедутся (герой "стоит в свете",
        // но алерт не срабатывает, либо наоборот).
        lightConeHeight = ALERT_RADIUS;
        // Ширину конуса у основания подбери под пропорции своего ассета (веер довольно широкий).
        lightConeWidth  = ALERT_RADIUS * 0.5f;

        foundAnimation = AnimationLoader.fromFiles(0.2f,
            "tower/tower_main_found.png", "tower/tower_main_found.png",
            "tower/tower_main_found.png", "tower/tower_main_found.png");

        form = new Rectangle(x, y, DRAW_W, DRAW_H);
    }

    @Override
    public void render(Batch batch) {
        stateTime += Gdx.graphics.getDeltaTime();

        // Свет рисуем ПОД башней, чтобы корпус его перекрывал сверху.
        renderLight(batch);

        if (showingFoundSprite) {
            TextureRegion frame = foundAnimation.getKeyFrame(stateTime, true);
            batch.draw(frame, position.x, position.y,
                DRAW_W / 2f, DRAW_H / 2f, DRAW_W, DRAW_H, 1f, 1f, 0f);
        } else {
            batch.draw(towerMain, position.x, position.y,
                DRAW_W / 2f, DRAW_H / 2f, DRAW_W, DRAW_H, 1f, 1f, 0f);
        }
    }

    private void renderLight(Batch batch) {
        towerCenter.set(position.x + DRAW_W / 2f, position.y + DRAW_H / 2f);

        float alpha = isFindingHero() ? LIGHT_ALPHA_FOUND : LIGHT_ALPHA_PATROL;
        // Цвет и прозрачность задаём здесь, а не редактируем PNG — так можно менять их динамически.
        batch.setColor(currentLightR, currentLightG, currentLightB, alpha);

        batch.draw(lightCone,
            towerCenter.x - lightConeWidth / 2f, towerCenter.y,  // позиция низа текстуры (вершина конуса) в центре башни
            lightConeWidth / 2f, 0f,                              // origin: центр по X, низ (вершина) по Y
            lightConeWidth, lightConeHeight,
            1f, 1f,
            currentLightAngle);

        batch.setColor(1f, 1f, 1f, 1f); // сброс цвета, чтобы не окрасить всё остальное
    }

    @Override
    public void dispose() {
        towerMain.getTexture().dispose();
        lightCone.getTexture().dispose();
        AnimationLoader.dispose(foundAnimation);
    }

    /**
     * Патруль по циклу LEFT→DOWN→RIGHT→UP; при герое в радиусе ALERT_RADIUS —
     * конус света плавно доворачивается на героя и включается found-анимация корпуса
     * (таймер патруля не тикает, пока герой обнаружен).
     */
    public void update(Rectangle heroBounds) {
        heroCenter.set(heroBounds.x + heroBounds.width / 2f,
            heroBounds.y + heroBounds.height / 2f);
        towerCenter.set(position.x + DRAW_W / 2f,
            position.y + DRAW_H / 2f);

        float dx = heroCenter.x - towerCenter.x;
        float dy = heroCenter.y - towerCenter.y;
        boolean withinDistance = towerCenter.dst(heroCenter) < ALERT_RADIUS;

        // Угол, куда СЕЙЧАС реально смотрит конус (используем currentLightAngle,
        // а не targetLightAngle — важно: пока конус доворачивается, герой ещё не считается
        // обнаруженным, даже если целевое направление уже "на него").
        float lightFacingAngle = (90f + currentLightAngle) % 360f;
        float angleToHero = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angleToHero < 0f) angleToHero += 360f;
        boolean withinCone = angularDiff(lightFacingAngle, angleToHero) <= LIGHT_HALF_FOV_DEG;

        boolean heroInRange = withinDistance && withinCone;

        if (heroInRange) {
            Direction foundDir;
            if (Math.abs(dx) >= Math.abs(dy)) {
                foundDir = dx < 0 ? Direction.LEFT : Direction.RIGHT;
            } else {
                foundDir = dy > 0 ? Direction.UP : Direction.DOWN;
            }
            lastFoundDirection = foundDir;
            showingFoundSprite = true;
            targetLightAngle = directionToAngle(foundDir);
        } else {
            if (lastFoundDirection != null) {
                // герой только что вышел из зоны/сектора: продолжаем патруль с его направления
                patrolDirection = lastFoundDirection;
                patrolTimer = 0f;
                lastFoundDirection = null;
            }
            showingFoundSprite = false;
            targetLightAngle = directionToAngle(patrolDirection);

            patrolTimer += Gdx.graphics.getDeltaTime();
            if (patrolTimer >= PATROL_INTERVAL) {
                patrolDirection = patrolDirection.next();
                patrolTimer = 0f;
            }
        }

        currentLightAngle = lerpAngle(currentLightAngle, targetLightAngle,
            LIGHT_TURN_SPEED * Gdx.graphics.getDeltaTime());

        // Целевой цвет зависит от того, обнаружен ли герой прямо сейчас.
        float targetR = isFindingHero() ? ALERT_R : PATROL_R;
        float targetG = isFindingHero() ? ALERT_G : PATROL_G;
        float targetB = isFindingHero() ? ALERT_B : PATROL_B;

        float colorT = Math.min(1f, LIGHT_COLOR_LERP_SPEED * Gdx.graphics.getDeltaTime());
        currentLightR += (targetR - currentLightR) * colorT;
        currentLightG += (targetG - currentLightG) * colorT;
        currentLightB += (targetB - currentLightB) * colorT;
    }

    /**
     * Сопоставление направления углу поворота конуса.
     * 0° = UP, потому что текстура light.png нарисована вершиной вниз, веером вверх —
     * это её "нулевая" (неповёрнутая) ориентация. Дальше углы идут против часовой стрелки,
     * как и вращение в batch.draw(...).
     */
    private static float directionToAngle(Direction d) {
        switch (d) {
            case UP:    return 0f;
            case LEFT:  return 90f;
            case DOWN:  return 180f;
            case RIGHT: return 270f;
            default:    return 0f;
        }
    }

    // Плавный поворот по кратчайшей дуге на окружности (0..360), без скачков через 0/360.
    private static float lerpAngle(float current, float target, float maxDelta) {
        float diff = ((target - current + 540f) % 360f) - 180f; // разница в диапазоне [-180, 180]
        float step = Math.signum(diff) * Math.min(Math.abs(diff), maxDelta);
        float result = current + step;
        return (result + 360f) % 360f;
    }

    // Кратчайшая угловая разница между двумя углами (всегда >= 0, максимум 180).
    // Используется, чтобы понять, попадает ли герой в раствор конуса (angleToHero) относительно
    // текущего направления света (lightFacingAngle) — вне зависимости от направления обхода круга.
    private static float angularDiff(float a, float b) {
        float diff = Math.abs((a - b + 540f) % 360f - 180f);
        return diff;
    }

    public boolean isFindingHero() { return lastFoundDirection != null; }
    public Rectangle getBounds() { return form; }
}
