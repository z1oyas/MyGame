package z1oyas.MyGame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
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
    private float mapWidthWorld;
    private float mapHeightWorld;

    private KeyboardAdapter inputProcessor = new KeyboardAdapter();

    @Override
    public void create () {
        map = new TmxMapLoader().load("map_title.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        MapProperties prop = map.getProperties();
        int mapWidthTiles = prop.get("width", Integer.class);
        int mapHeightTiles = prop.get("height", Integer.class);
        int tileWidth = prop.get("tilewidth", Integer.class);
        int tileHeight = prop.get("tileheight", Integer.class);
        mapWidthWorld = mapWidthTiles * tileWidth * unitScale;
        mapHeightWorld = mapHeightTiles * tileHeight * unitScale;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 50, 30);

        Gdx.input.setInputProcessor(inputProcessor);
        batch = new SpriteBatch();

        me = new Hero(0, 17);
        tower = new Tower(17, 18);
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

        me.moveTo(inputProcessor.getDirection());
        tower.update(me.getBoundares());

        // камера следует за героем (мировые единицы)
        camera.position.set(
            me.getPosition().x,
            me.getPosition().y,
            0
        );

        // клампинг камеры по границам карты
        float halfW = camera.viewportWidth / 2f;
        float halfH = camera.viewportHeight / 2f;
        if (mapWidthWorld > camera.viewportWidth) {
            camera.position.x = MathUtils.clamp(camera.position.x, halfW, mapWidthWorld - halfW);
        } else {
            camera.position.x = mapWidthWorld / 2f;
        }
        if (mapHeightWorld > camera.viewportHeight) {
            camera.position.y = MathUtils.clamp(camera.position.y, halfH, mapHeightWorld - halfH);
        } else {
            camera.position.y = mapHeightWorld / 2f;
        }

        camera.update();

        // setView ДО render (исправление порядка)
        renderer.setView(camera);
        renderer.render();

        // проекция batch в мировых координатах (камера 50×30 world units)
        batch.setProjectionMatrix(camera.combined);

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
