package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Tower implements Person, Animated{
    public  final  Vector2 position = new Vector2();
    private final static int FRAME_COLS = 1;
    private final static int FRAME_ROWS = 2;
    private final float size = 256;
    private final float halfSize = size / 2;
    private final Vector2 angle = new Vector2();
    Animation<TextureRegion> movingTower;
    Animation<TextureRegion> findHeroMoving;
    float stateTime;
    boolean findHero;
    Texture movingSheet;
    Rectangle form;

    public Tower(float x, float y, String TowerSpritePath, String TowerFindHeroPath){
    position.set(x,y);
    movingTower = makeAnimationPersona(TowerSpritePath,FRAME_COLS,FRAME_ROWS,1.25f);
    findHeroMoving = makeAnimationPersona(TowerFindHeroPath,FRAME_COLS,FRAME_ROWS,3.00f);
    stateTime = 0f;
    findHero = false;
    form = new Rectangle(x,y,size+4, size+4);
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
    public void findHeroChecker(Rectangle heroPostion){
        findHero = form.overlaps(heroPostion);
//        if (position.epsilonEquals(heroPostion)) findHero =true;
    }
    public void findHeroChecker(Vector2 heroPostion){
        if(position.x == heroPostion.x && position.y == heroPostion.y)findHero =true;
//        if (position.epsilonEquals(heroPostion)) findHero =true;
    }
}
