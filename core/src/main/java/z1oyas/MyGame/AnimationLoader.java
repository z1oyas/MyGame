package z1oyas.MyGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Собирает Animation&lt;TextureRegion&gt; из списка отдельных PNG-файлов
 * (а не из равномерного спрайт-листа).
 */
public final class AnimationLoader {

    private AnimationLoader() {}

    /** Анимация из файлов с длительностью кадра по умолчанию 0.15 с. */
    public static Animation<TextureRegion> fromFiles(String... paths) {
        return fromFiles(0.15f, paths);
    }

    /** Анимация из файлов с заданной длительностью кадра. */
    public static Animation<TextureRegion> fromFiles(float frameDuration, String... paths) {
        TextureRegion[] frames = new TextureRegion[paths.length];
        for (int i = 0; i < paths.length; i++) {
            frames[i] = new TextureRegion(new Texture(paths[i]));
        }
        return new Animation<>(frameDuration, frames);
    }

    /** Освобождает текстуры всех кадров анимации (каждый кадр — отдельная текстура). */
    public static void dispose(Animation<TextureRegion> animation) {
        if (animation == null) return;
        for (TextureRegion frame : animation.getKeyFrames()) {
            if (frame != null && frame.getTexture() != null) {
                frame.getTexture().dispose();
            }
        }
    }
}
