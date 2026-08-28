package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    private final MyGame game;
    private Hero me;
    private Tower tower;
    private final List<Hero> enemies = new ArrayList<>();
    float unitScale = 1 / 16f;
    OrthogonalTiledMapRenderer renderer;
    OrthographicCamera camera;
    TiledMap map;
    private float mapWidthWorld;
    private float mapHeightWorld;
    private final List<Collectible> collectibles = new ArrayList<>();
    private FinishZone finishZone;

    private KeyboardAdapter inputProcessor = new KeyboardAdapter();

    public GameScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show () {
        map = new TmxMapLoader().load("map_title.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        MapProperties prop = map.getProperties();
        int mapWidthTiles = prop.get("width", Integer.class);
        int mapHeightTiles = prop.get("height", Integer.class);
        int tileWidth = prop.get("tilewidth", Integer.class);
        int tileHeight = prop.get("tileheight", Integer.class);
        mapWidthWorld = mapWidthTiles * tileWidth * unitScale;
        mapHeightWorld = mapHeightTiles * tileHeight * unitScale;

        Array<Polygon> walkableZones = new Array<>();
        MapLayer walkableLayer = map.getLayers().get("walkable");
        if (walkableLayer != null) {
            for (PolygonMapObject obj : walkableLayer.getObjects().getByType(PolygonMapObject.class)) {
                float[] px = obj.getPolygon().getTransformedVertices();
                float[] world = new float[px.length];
                for (int i = 0; i < px.length; i++) {
                    world[i] = px[i] * unitScale;
                }
                walkableZones.add(new Polygon(world));
            }
        }

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 50, 30);

        Gdx.input.setInputProcessor(inputProcessor);

        float spawnX = Float.MAX_VALUE;
        float spawnY = 0f;
        for (Polygon zone : walkableZones) {
            float[] verts = zone.getTransformedVertices();
            for (int i = 0; i < verts.length; i += 2) {
                if (verts[i] < spawnX) {
                    spawnX = verts[i];
                    spawnY = verts[i + 1];
                }
            }
        }
        spawnY = Math.max(spawnY, 0f);

        me = new Hero(spawnX, spawnY, walkableZones);
        tower = new Tower(17, 18);

        // --- загрузка ягод ---
        MapLayer berriesLayer = map.getLayers().get("berries");
        if (berriesLayer != null) {
            for (MapObject obj : berriesLayer.getObjects()) {
                // точечный объект хранит координаты напрямую в свойствах
                float bx = obj.getProperties().get("x", Float.class) * unitScale;
                float by = obj.getProperties().get("y", Float.class) * unitScale;
                collectibles.add(new Berry(bx, by));
            }
        }
        // --- загрузка конфет ---
        MapLayer candiesLayer = map.getLayers().get("candies");
        if (candiesLayer != null) {
            for (MapObject obj : candiesLayer.getObjects()) {
                float cx = obj.getProperties().get("x", Float.class) * unitScale;
                float cy = obj.getProperties().get("y", Float.class) * unitScale;
                collectibles.add(new Candy(cx, cy));
            }
        }

// --- загрузка финиша ---
        MapLayer finishLayer = map.getLayers().get("finish");
        if (finishLayer != null) {
            for (RectangleMapObject obj : finishLayer.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = obj.getRectangle();
                finishZone = new FinishZone(
                    r.x * unitScale,
                    r.y * unitScale,
                    r.width * unitScale,
                    r.height * unitScale
                );
                break; // финиш один на уровень
            }
        }
    }

    @Override
    public void render (float delta) {
        ScreenUtils.clear(1, 1, 1, 1);

        me.update(delta);
        me.moveTo(inputProcessor.getDirection());
        // проверяем подбор предметов
        Rectangle heroBounds = me.getBoundares();
        for (Collectible c : collectibles) {
            if (!c.isCollected() && c.overlaps(heroBounds)) {
                c.onCollect(me);
            }
        }
        // удаляем собранные предметы
        collectibles.removeIf(Collectible::isCollected);

        // проверяем финиш
        if (finishZone != null && finishZone.checkTrigger(heroBounds)) {
            // пока просто логируем — анимацию добавим на следующем шаге
            Gdx.app.log("GAME", "Level finished!");
        }
        tower.update(me.getBoundares(), me.isInvisible());

        camera.position.set(
            me.getPosition().x,
            me.getPosition().y,
            0
        );

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

        renderer.setView(camera);
        renderer.render();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        me.render(game.batch);
        tower.render(game.batch);
        for (Collectible c : collectibles) {
            c.render(game.batch);
        }
        if (finishZone != null) {
            finishZone.render(game.batch);
        }
        game.batch.end();
    }

    @Override
    public void resize (int width, int height) {
    }

    @Override
    public void pause () {
    }

    @Override
    public void resume () {
    }

    @Override
    public void hide () {
    }

    @Override
    public void dispose () {
        me.dispose();
        tower.dispose();
        for (Collectible c : collectibles) {
            if (c instanceof Berry) ((Berry) c).dispose();
            if (c instanceof Candy) ((Candy) c).dispose();
        }
        renderer.dispose();
        map.dispose();
    }
}
