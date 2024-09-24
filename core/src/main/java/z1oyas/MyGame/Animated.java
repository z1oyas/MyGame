package z1oyas.MyGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public interface Animated
{
    void  moveTo(Vector2 direction);

    default Animation<TextureRegion> makeAnimationPersona(String personaMovingPath, int FRAME_COLS, int FRAME_ROWS) {
        //sprite.png
        Texture movingSheet = null;

        if (!(personaMovingPath == null))
            movingSheet = new Texture(personaMovingPath);

        TextureRegion[][] tmp = TextureRegion.split(movingSheet,
            movingSheet.getWidth() / FRAME_COLS,
            movingSheet.getHeight() / FRAME_ROWS);

        TextureRegion[] frames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int n = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                frames[n] = tmp[i][j];
                n++;
            }
        }
        return new Animation<>(0.25f, frames);
    }
    default TextureRegion renderAnimationPersona(Animation<TextureRegion> a, float deltaTime){
        return a.getKeyFrame(deltaTime, true);
    }
}
