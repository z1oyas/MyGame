package z1oyas.MyGame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class FinishZone {
    private final Rectangle bounds; // зона в мировых единицах
    private boolean triggered = false;

    public FinishZone(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
    }

    public boolean checkTrigger(Rectangle heroBounds) {
        if (!triggered && bounds.overlaps(heroBounds)) {
            triggered = true;
            return true; // сигнал GameScreen — уровень завершён
        }
        return false;
    }

    public boolean isTriggered() {
        return triggered;
    }

    // временно: рисуем полупрозрачный прямоугольник для отладки
    // потом заменишь на свою анимацию
    public void render(SpriteBatch batch) {
        // пока пусто — добавим анимацию на следующем шаге
    }
}
