package z1oyas.MyGame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import z1oyas.MyGame.KeyboardAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    float delta;
    private Person me;
    private final List<Person> enemies = new ArrayList<>();

    private KeyboardAdapter inputProcessor = new KeyboardAdapter();

    @Override
    public void create () {
        Gdx.input.setInputProcessor(inputProcessor);
        batch = new SpriteBatch();

        me = new Person(100, 200,"cheersSprite.png","sprite.png");

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
