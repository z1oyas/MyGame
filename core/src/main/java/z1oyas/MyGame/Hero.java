package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// класс объектов на поле: конфеты

public class Hero implements Person, Animated {
    Animation<TextureRegion> walkAnimation;
    Animation<TextureRegion> cheersAnimation;

    private final float size = 64;
    private final float halfSize = size / 2;

    private final Vector2 position = new Vector2();
    private final Vector2 angle = new Vector2();
    private float stateTime;
    Texture movingSheet;
    Rectangle form;

    private final static int FRAME_COLS = 4;
    private final static int FRAME_ROWS = 2;

    private boolean isMoving;
    private boolean isForvard;
    //При создании объекта сразу делать ему спрайт шит готовый для рендереинга. То есть, вызывать для всех его спрайтов метод
    //makeAnimationPersona. Далее, в moveTo переключать на движение, если есть оно

    // сделать так, чтобы у каждого было разное кол во движений
    public Hero(float x, float y, String textureCheerName, String textureWalkName) {
        //подготавливаем все движения персонажей
        form = new Rectangle(x,y,size,size);
        walkAnimation = makeAnimationPersona(textureWalkName,FRAME_COLS,FRAME_ROWS, 0.25f);
        cheersAnimation = makeAnimationPersona(textureCheerName, FRAME_COLS,FRAME_ROWS,0.25f);
//        texture = new Texture(textureName);
//        textureRegion = new TextureRegion(texture);
        stateTime = 0f;
        position.set(x, y);
        isMoving = false;
        isForvard =true;
    }
    @Override
    public void render(Batch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion currentFrame;
        if(isMoving){
            currentFrame = renderAnimationPersona(walkAnimation,stateTime);
            }
        else {
            currentFrame = walkAnimation.getKeyFrame(0);
        }
        //batch.draw(currentFrame,position.x,position.y);
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
//
    @Override
    public void dispose() {
        movingSheet.dispose();
    }

    public void moveTo(Vector2 direction) {
        isMoving = !direction.isZero();

        if (direction.x>0&&!isForvard){ //вправо, рожа влево
            flipDirectionFrame();
            isForvard = true;
        }
        else if (direction.x<0&&isForvard){ //влево, рожа вправо
            flipDirectionFrame();
            isForvard = false;
        }
        if (isMoving)  {
            position.add(direction);
            form.setPosition(position);//подвинули наш квадрат
        }
    }

//    public void rotateTo(Vector2 mousePos) {
//        angle.set(mousePos).sub(position.x + halfSize, position.y + halfSize);
//    }
    private  void flipDirectionFrame(){
        for(TextureRegion frame : walkAnimation.getKeyFrames()){
            frame.flip(true,false);
        }
    }

    public Vector2 getPosition() {
        return position;
    }
    public Rectangle getBoundares() {
        return form;
    }

}
