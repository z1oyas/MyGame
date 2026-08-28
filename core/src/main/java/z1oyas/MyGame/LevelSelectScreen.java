package z1oyas.MyGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.HashMap;

public class LevelSelectScreen implements Screen {
    private final MyGame game;
    private Stage stage;
    private Skin skin;

//    // Пока список уровней — просто пути к .tmx файлам.
//    private final String[] levels = {
//        "map_title.tmx"
//        // сюда позже добавишь "map_level2.tmx" и т.д.
//    };

        // Пока список уровней — просто пути к .tmx файлам.
    private final HashMap<String,String> levels = new HashMap<>();
    {
        levels.put("level 1", "map_title.tmx");
        // сюда позже добавишь "map_level2.tmx" и т.д.
    }


    public LevelSelectScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        for (String levelPath : levels.keySet()) {
            TextButton levelButton = new TextButton(levelPath, skin);
            levelButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new GameScreen(game)); // позже — new GameScreen(game, levelPath)
                }
            });
            table.add(levelButton).padBottom(10).row();
        }

        TextButton backButton = new TextButton("Назад", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });
        table.add(backButton).padTop(20);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
