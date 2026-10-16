package nemesis.plugins.minimal;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import usa.kitikat.plugin.LauncherPlugin;
import usa.kitikat.plugin.PluginContext;

/**
 * Плагин «Минимал»: спокойный графит с мягким свечением, без лишнего —
 * и сдержанный тёплый акцент. Изображений нет, только градиенты.
 */
public final class MinimalPlugin implements LauncherPlugin {

    private static final int BG_WIDTH = 1600;
    private static final int BG_HEIGHT = 900;

    @Override
    public String id() {
        return "minimal";
    }

    @Override
    public String name() {
        return "Минимал";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public String description() {
        return "Чистый графит с мягким свечением — ничего лишнего. "
                + "Акцент становится тёплым и сдержанным.";
    }

    @Override
    public boolean enabledByDefault() {
        return false;   // меняет фон — включаем по желанию
    }

    @Override
    public void onLoad(PluginContext context) {
        context.setBackground(buildBackground());
        context.setAccent("#d97706");
        context.log("минималистичное оформление включено");
    }

    /** Иконка для библиотеки плагинов — «затмение»: кольцо и диск. */
    @Override
    public Node icon() {
        Circle ring = new Circle(20, 22, 15.5);
        ring.setFill(null);
        ring.setStroke(Color.web("#cbd5e1"));
        ring.setStrokeWidth(2.6);

        Circle disc = new Circle(29, 15, 8.5, Color.web("#d97706"));
        disc.setEffect(new DropShadow(8, Color.web("#d97706", 0.6)));

        Group root = new Group(ring, disc);
        return root;
    }

    /* ================= фон ================= */

    private Image buildBackground() {
        try {
            Pane root = new Pane();
            root.resize(BG_WIDTH, BG_HEIGHT);
            root.setStyle("-fx-background-color: transparent;");

            // вертикальный графит
            Rectangle base = new Rectangle(0, 0, BG_WIDTH, BG_HEIGHT);
            base.setFill(Color.web("#12161d"));

            // мягкое свечение в верхней трети
            Circle glow = new Circle(BG_WIDTH * 0.62, BG_HEIGHT * 0.24, 520);
            glow.setFill(new RadialGradient(
                    0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#2b3646", 0.85)),
                    new Stop(1, Color.web("#2b3646", 0))
            ));

            // второе, холоднее и слабее — левее
            Circle glow2 = new Circle(BG_WIDTH * 0.18, BG_HEIGHT * 0.62, 420);
            glow2.setFill(new RadialGradient(
                    0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#1f2a3a", 0.7)),
                    new Stop(1, Color.web("#1f2a3a", 0))
            ));

            // тонкие диагонали — едва заметный рисунок
            Group lines = new Group();
            for (int i = 0; i < 7; i++) {
                double offset = i * 260 - 400;
                Line line = new Line(offset, BG_HEIGHT, offset + 420, 0);
                line.setStroke(Color.web("#ffffff", 0.035));
                line.setStrokeWidth(1.2);
                lines.getChildren().add(line);
            }

            // виньетка по краям
            Rectangle vignetteTop = new Rectangle(0, 0, BG_WIDTH, 90);
            vignetteTop.setFill(new javafx.scene.paint.LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#000000", 0.42)),
                    new Stop(1, Color.web("#000000", 0))
            ));
            Rectangle vignetteBottom = new Rectangle(0, BG_HEIGHT - 140,
                    BG_WIDTH, 140);
            vignetteBottom.setFill(new javafx.scene.paint.LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#000000", 0)),
                    new Stop(1, Color.web("#000000", 0.5))
            ));

            root.getChildren().addAll(
                    base, glow, glow2, lines, vignetteTop, vignetteBottom);

            var parameters = new javafx.scene.SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage snapshot = new WritableImage(BG_WIDTH, BG_HEIGHT);
            root.snapshot(parameters, snapshot);
            return snapshot;
        } catch (Throwable ex) {
            return gradientFallback();
        }
    }

    /** Запасной фон: вертикальный градиент тех же цветов. */
    private Image gradientFallback() {
        WritableImage image = new WritableImage(BG_WIDTH, BG_HEIGHT);
        PixelWriter writer = image.getPixelWriter();

        int[] top = {0x1b, 0x22, 0x2c};
        int[] bottom = {0x0d, 0x10, 0x15};

        int[] row = new int[BG_WIDTH];
        for (int y = 0; y < BG_HEIGHT; y++) {
            double t = y / (double) (BG_HEIGHT - 1);
            int r = (int) (top[0] + (bottom[0] - top[0]) * t);
            int g = (int) (top[1] + (bottom[1] - top[1]) * t);
            int b = (int) (top[2] + (bottom[2] - top[2]) * t);
            java.util.Arrays.fill(row, 0xFF000000 | (r << 16) | (g << 8) | b);
            writer.setPixels(0, y, BG_WIDTH, 1,
                    PixelFormat.getIntArgbPreInstance(), row, 0, BG_WIDTH);
        }
        return image;
    }
}
