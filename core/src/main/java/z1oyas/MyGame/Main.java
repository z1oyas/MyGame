package z1oyas.MyGame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    float delta;
    private Hero me;
    private final List<Hero> enemies = new ArrayList<>();

    private KeyboardAdapter inputProcessor = new KeyboardAdapter();

    @Override
    public void create () {
        Gdx.input.setInputProcessor(inputProcessor);
        batch = new SpriteBatch();

        me = new Hero(100, 200,"cheersSprite.png","sprite.png");

//        List<Person> newEnemies = IntStream.range(0, 5)
//            .mapToObj(i -> {
//                int x = MathUtils.random(Gdx.graphics.getWidth());
//                int y = MathUtils.random(Gdx.graphics.getHeight());
//
//                return new Person(x, y, "sprite_bad_guys.png","sprite_bad_guys.png");
//            })
//            .collect(Collectors.toList());
//        enemies.addAll(newEnemies);
    }

    @Override
    public void render () {
        me.moveTo(inputProcessor.getDirection());
        //me.rotateTo(inputProcessor.getMousePos());
        ScreenUtils.clear(1, 1, 1, 1);
        batch.begin();
        me.render(batch);
//        enemies.forEach(enemy -> {
//            enemy.render(batch);
////            enemy.rotateTo(me.getPosition());
//        });
        batch.end();
    }

    @Override
    public void dispose () {
        batch.dispose();
        me.dispose();
    }
}
