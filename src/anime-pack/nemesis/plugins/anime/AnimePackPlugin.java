package nemesis.plugins.anime;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.transform.Rotate;
import usa.kitikat.plugin.LauncherPlugin;
import usa.kitikat.plugin.PluginContext;


/**
 * Плагин «Аниме-пак»: милые иконки бокового меню — цветок сакуры,
 * котик, лапка, сердечко и искра для раздела «Плагины».
 *
 * <p>Всё нарисовано вектором прямо в коде: не тянет чужих картинок,
 * ресурсов и лишних мегабайт — jar весит килобайты. Фон не меняет,
 * только иконки.</p>
 */
public final class AnimePackPlugin implements LauncherPlugin {

    @Override
    public String id() {
        return "anime-pack";
    }

    @Override
    public String name() {
        return "Аниме-пак";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public String description() {
        return "Милые иконки меню вместо штриховых: цветок сакуры, котик, "
                + "лапка, сердечко и искра для раздела плагинов. Фон не меняет.";
    }

    @Override
    public void onLoad(PluginContext context) {
        // только иконки — фон и палитра остаются как их настроил пользователь
        context.setIconProvider(this::icon);
        context.log("аниме-иконки включены");
    }

    @Override
    public void onDisable() {
        // откат делает сам лаунчер: при перезагрузке провайдер иконок сбрасывается
    }

    /* ================= иконки (18×18) ================= */

    private Node icon(String kind) {
        return switch (kind == null ? "" : kind) {
            case "games" -> sakura();
            case "user" -> catFace();
            case "sliders" -> paw();
            case "close" -> heart();
            case "plugins" -> sparkle();
            default -> null;    // стандартная иконка
        };
    }

    /** Цветок сакуры — пять лепестков вокруг серединки. */
    private Node sakura() {
        javafx.scene.Group root = new javafx.scene.Group();

        Color petal = Color.web("#ff9ecb");
        for (int i = 0; i < 5; i++) {
            Ellipse leaf = new Ellipse(9, 4.8, 3.0, 4.4);
            leaf.setFill(petal);
            leaf.setStroke(Color.web("#ffd6e9"));
            leaf.setStrokeWidth(0.8);
            leaf.getTransforms().add(new Rotate(i * 72, 9, 9));
            root.getChildren().add(leaf);
        }

        Circle core = new Circle(9, 9, 2.1, Color.web("#ffe9a8"));
        root.getChildren().add(core);
        return root;
    }

    /** Иконка для библиотеки плагинов — та же сакура, но крупнее. */
    @Override
    public Node icon() {
        javafx.scene.Group root = (javafx.scene.Group) sakura();
        double scale = 44.0 / 18.0;
        root.getTransforms().add(new javafx.scene.transform.Scale(scale, scale));
        return root;
    }

    /** Мордочка котика. */
    private Node catFace() {
        javafx.scene.Group root = new javafx.scene.Group();

        Color fur = Color.web("#fff1f7");
        Color inner = Color.web("#ffb7d5");

        // уши
        Path leftEar = new Path();
        leftEar.getElements().addAll(
                new javafx.scene.shape.MoveTo(4.2, 8.0),
                new javafx.scene.shape.LineTo(3.4, 2.2),
                new javafx.scene.shape.LineTo(8.2, 5.6),
                new javafx.scene.shape.ClosePath()
        );
        Path rightEar = new Path();
        rightEar.getElements().addAll(
                new javafx.scene.shape.MoveTo(13.8, 8.0),
                new javafx.scene.shape.LineTo(14.6, 2.2),
                new javafx.scene.shape.LineTo(9.8, 5.6),
                new javafx.scene.shape.ClosePath()
        );
        leftEar.setFill(fur);
        rightEar.setFill(fur);

        // мордочка
        Circle face = new Circle(9, 10.6, 5.6, fur);

        // внутренняя часть ушей
        Path leftInner = new Path();
        leftInner.getElements().addAll(
                new javafx.scene.shape.MoveTo(5.2, 7.2),
                new javafx.scene.shape.LineTo(4.7, 3.8),
                new javafx.scene.shape.LineTo(7.4, 5.9),
                new javafx.scene.shape.ClosePath()
        );
        Path rightInner = new Path();
        rightInner.getElements().addAll(
                new javafx.scene.shape.MoveTo(12.8, 7.2),
                new javafx.scene.shape.LineTo(13.3, 3.8),
                new javafx.scene.shape.LineTo(10.6, 5.9),
                new javafx.scene.shape.ClosePath()
        );
        leftInner.setFill(inner);
        rightInner.setFill(inner);

        // глаза и нос
        Circle eyeLeft = new Circle(6.9, 10.2, 1.1, Color.web("#4a3040"));
        Circle eyeRight = new Circle(11.1, 10.2, 1.1, Color.web("#4a3040"));
        Circle nose = new Circle(9, 12.5, 0.9, Color.web("#ff6fae"));

        // усы
        Line whisker1 = new Line(2.5, 11.6, 5.4, 12.0);
        Line whisker2 = new Line(2.5, 13.4, 5.4, 13.2);
        Line whisker3 = new Line(15.5, 11.6, 12.6, 12.0);
        Line whisker4 = new Line(15.5, 13.4, 12.6, 13.2);
        for (Line whisker : new Line[]{whisker1, whisker2, whisker3, whisker4}) {
            whisker.setStroke(Color.web("#f3b7d2"));
            whisker.setStrokeWidth(0.9);
            whisker.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        }

        root.getChildren().addAll(
                leftEar, rightEar, leftInner, rightInner,
                face, eyeLeft, eyeRight, nose,
                whisker1, whisker2, whisker3, whisker4
        );
        return root;
    }

    /** Лапка — иконка настроек. */
    private Node paw() {
        javafx.scene.Group root = new javafx.scene.Group();

        Color pad = Color.web("#ff9ecb");

        Ellipse main = new Ellipse(9, 12.6, 4.4, 3.5);
        main.setFill(pad);

        double[][] toes = {{4.4, 8.8}, {7.5, 6.6}, {10.7, 6.6}, {13.6, 8.9}};
        root.getChildren().add(main);
        for (double[] toe : toes) {
            Ellipse finger = new Ellipse(toe[0], toe[1], 1.8, 2.2);
            finger.setFill(pad);
            root.getChildren().add(finger);
        }
        return root;
    }

    /** Сердечко — закрыть. */
    private Node heart() {
        Path shape = new Path();
        shape.getElements().addAll(
                new javafx.scene.shape.MoveTo(9, 15.4),
                new javafx.scene.shape.CubicCurveTo(
                        4.0, 11.6, 2.4, 9.0, 2.4, 6.6),
                new javafx.scene.shape.CubicCurveTo(
                        2.4, 4.2, 4.3, 2.6, 6.4, 2.6),
                new javafx.scene.shape.CubicCurveTo(
                        7.7, 2.6, 8.6, 3.4, 9, 4.4),
                new javafx.scene.shape.CubicCurveTo(
                        9.4, 3.4, 10.3, 2.6, 11.6, 2.6),
                new javafx.scene.shape.CubicCurveTo(
                        13.7, 2.6, 15.6, 4.2, 15.6, 6.6),
                new javafx.scene.shape.CubicCurveTo(
                        15.6, 9.0, 14.0, 11.6, 9, 15.4),
                new javafx.scene.shape.ClosePath()
        );
        shape.setFill(Color.web("#ff6fae"));
        shape.setStroke(Color.web("#ffd0e2"));
        shape.setStrokeWidth(0.8);
        return shape;
    }

    /** Искра — звёздочка для кнопки «Плагины». */
    private Node sparkle() {
        Path star = new Path();
        star.getElements().addAll(
                new javafx.scene.shape.MoveTo(9, 1.6),
                new javafx.scene.shape.CubicCurveTo(
                        10.2, 6.4, 11.6, 7.8, 16.4, 9),
                new javafx.scene.shape.CubicCurveTo(
                        11.6, 10.2, 10.2, 11.6, 9, 16.4),
                new javafx.scene.shape.CubicCurveTo(
                        7.8, 11.6, 6.4, 10.2, 1.6, 9),
                new javafx.scene.shape.CubicCurveTo(
                        6.4, 7.8, 7.8, 6.4, 9, 1.6),
                new javafx.scene.shape.ClosePath()
        );
        star.setFill(Color.web("#ff6fae"));
        star.setStroke(Color.web("#ffd0e2"));
        star.setStrokeWidth(0.8);
        return star;
    }
}
