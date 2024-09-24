package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Tower implements Person, Animated{
    public  final  Vector2 position = new Vector2();
    private final static int FRAME_COLS = 2;
    private final static int FRAME_ROWS = 1;
    private final float size = 64;
    private final float halfSize = size / 2;
    private final Vector2 angle = new Vector2();
    Animation<TextureRegion> movingTower;
    Animation<TextureRegion> findHeroMoving;
    float stateTime;
    boolean findHero;
    Texture movingSheet;

    public Tower(float x, float y, String TowerSpritePath, String TowerFindHeroPath){
    position.set(x,y);
    movingTower = makeAnimationPersona(TowerSpritePath,FRAME_COLS,FRAME_ROWS);
    findHeroMoving = makeAnimationPersona(TowerFindHeroPath,FRAME_COLS,FRAME_ROWS);
    stateTime = 0f;
    findHero = false;
    }

    @Override
    public void render(Batch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion currentFrame;
        currentFrame = !findHero?renderAnimationPersona(movingTower,stateTime): renderAnimationPersona(findHeroMoving, stateTime);
        batch.draw(
            currentFrame,
            position.x,
            position.y,
            halfSize,
            halfSize,
            size,
            size,
            1,
            1,
            angle.angleDeg()
        );
    }
// нафига? то есть текстура то в интерфейсе бьется
    @Override
    public void dispose() {
        movingSheet.dispose();

    }

    public void moveTo(Vector2 direction) {

    }
    //todo - получить позицию героя. если совпала с нашей - то мы меняем анимацию
    private void findHeroChecker(Vector2 heroPostion){
        if (position.epsilonEquals(heroPostion)) findHero =true;
    }
}
