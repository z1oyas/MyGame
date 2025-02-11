package z1oyas.MyGame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    private Hero me;
    private  Tower tower;
    private final List<Hero> enemies = new ArrayList<>();
    float unitScale = 1 / 16f;
    OrthogonalTiledMapRenderer renderer;
    OrthographicCamera camera;
    TiledMap map;

    private KeyboardAdapter inputProcessor = new KeyboardAdapter();

    @Override
    public void create () {
        map = new TmxMapLoader().load("map_title.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 50, 30);

        Gdx.input.setInputProcessor(inputProcessor);
        batch = new SpriteBatch();

        me = new Hero(10, 340,"cheersSprite1.png","sprite1.png");
        tower = new Tower(600,300,"tower1.png","towerfind1.png");
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
        ScreenUtils.clear(1, 1, 1, 1);

        camera.update();
        renderer.render();
        renderer.setView(camera);

        me.moveTo(inputProcessor.getDirection());
        tower.findHeroChecker(me.getBoundares());
        //me.rotateTo(inputProcessor.getMousePos());

        batch.begin();
        me.render(batch);
        tower.render(batch);
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
        tower.dispose();
        renderer.dispose();
        map.dispose();
    }
}
