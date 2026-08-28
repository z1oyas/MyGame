package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    private final MyGame game;
    private Hero me;
    private final List<Tower> towers = new ArrayList<>();
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

        // --- загрузка блокирующих (непроходимых) зон ---
        Array<Rectangle> blockedZones = new Array<>();
        MapLayer blockedLayer = map.getLayers().get("blocked");
        if (blockedLayer != null) {
            for (RectangleMapObject obj : blockedLayer.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = obj.getRectangle();
                blockedZones.add(new Rectangle(
                    r.x * unitScale,
                    r.y * unitScale,
                    r.width * unitScale,
                    r.height * unitScale
                ));
            }
        } else {
            Gdx.app.log("GameScreen", "Слой 'blocked' не найден — коллизии отсутствуют");
        }

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 50, 30);

        Gdx.input.setInputProcessor(inputProcessor);

        // Спавн героя — точка, отмеченная на карте слоем "start".
        float spawnX = 0f;
        float spawnY = 0f;
        boolean hasStart = false;
        MapLayer startLayer = map.getLayers().get("start");
        if (startLayer != null) {
            for (RectangleMapObject obj : startLayer.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = obj.getRectangle();
                spawnX = r.x * unitScale;
                spawnY = r.y * unitScale;
                hasStart = true;
                break; // старт один на уровень
            }
        }
        if (!hasStart) {
            Gdx.app.log("GameScreen", "Слой 'start' не найден — спавн по умолчанию (0,0)");
        }

        me = new Hero(spawnX, spawnY, blockedZones, mapWidthWorld, mapHeightWorld);

        // --- загрузка вышек (точки на слое "tower") ---
        MapLayer towerLayer = map.getLayers().get("tower");
        if (towerLayer != null) {
            for (MapObject obj : towerLayer.getObjects()) {
                float tx = obj.getProperties().get("x", Float.class) * unitScale;
                float ty = obj.getProperties().get("y", Float.class) * unitScale;
                towers.add(new Tower(tx, ty));
            }
        } else {
            Gdx.app.log("GameScreen", "Слой 'tower' не найден — вышки не заспавнены");
        }

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
            me.playHappyAnimation();
            Gdx.app.log("GAME", "Level finished!");
        }
        for (Tower t : towers) {
            t.update(me.getBoundares(), me.isInvisible());
        }

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
        for (Tower t : towers) {
            t.render(game.batch);
        }
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
        for (Tower t : towers) {
            t.dispose();
        }
        for (Collectible c : collectibles) {
            if (c instanceof Berry) ((Berry) c).dispose();
            if (c instanceof Candy) ((Candy) c).dispose();
        }
        renderer.dispose();
        map.dispose();
    }
}
