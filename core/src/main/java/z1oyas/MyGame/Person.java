package z1oyas.MyGame;

import com.badlogic.gdx.graphics.g2d.Batch;
//походу надо делать все объекты фигурами, а которые натягивается текстура. Тогда можно будет их коллизии считаь
public interface Person {
    void render(Batch batch);
    void dispose();
}
