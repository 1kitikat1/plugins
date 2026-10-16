package nemesis.plugins.synthwave;

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
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import usa.kitikat.plugin.LauncherPlugin;
import usa.kitikat.plugin.PluginContext;

/**
 * Плагин «Синтвейв»: ретро-фон (неоновое солнце, перспективная сетка,
 * звёзды) и яркий розовый акцент. Всё рисуется вектором — jar весит
 * несколько килобайт.
 */
public final class SynthwavePlugin implements LauncherPlugin {

    private static final int BG_WIDTH = 1600;
    private static final int BG_HEIGHT = 900;
    private static final double HORIZON = 560;

    @Override
    public String id() {
        return "synthwave";
    }

    @Override
    public String name() {
        return "Синтвейв";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public String description() {
        return "Ретро-фон: неоновое солнце над перспективной сеткой и звёзды. "
                + "Акцент лаунчера становится ярко-розовым, под неон.";
    }

    @Override
    public boolean enabledByDefault() {
        return false;   // меняет фон — включаем по желанию, чтобы
                        // не спорить с другими темами
    }

    @Override
    public void onLoad(PluginContext context) {
        context.setBackground(buildBackground());
        context.setAccent("#ff2fb9");
        context.log("синтвейв-оформление включено");
    }

    /** Иконка для библиотеки плагинов — неоновая искра. */
    @Override
    public Node icon() {
        Path spark = new Path();
        spark.getElements().addAll(
                new javafx.scene.shape.MoveTo(22, 3),
                new javafx.scene.shape.CubicCurveTo(
                        24.5, 16, 28, 19.5, 41, 22),
                new javafx.scene.shape.CubicCurveTo(
                        28, 24.5, 24.5, 28, 22, 41),
                new javafx.scene.shape.CubicCurveTo(
                        19.5, 28, 16, 24.5, 3, 22),
                new javafx.scene.shape.CubicCurveTo(
                        16, 19.5, 19.5, 16, 22, 3),
                new javafx.scene.shape.ClosePath()
        );
        spark.setFill(new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff7ad9")),
                new Stop(1, Color.web("#7c3aed"))
        ));

        Circle dot = new Circle(36, 8, 3.2, Color.web("#67e8f9"));

        Group root = new Group(spark, dot);
        root.setEffect(new DropShadow(9, Color.web("#ff2fb9", 0.75)));
        return root;
    }

    /* ================= фон ================= */

    private Image buildBackground() {
        try {
            Pane root = new Pane();
            root.resize(BG_WIDTH, BG_HEIGHT);
            root.setStyle("-fx-background-color: transparent;");

            // небо: глубокий фиолет → пурпур у горизонта
            Rectangle sky = new Rectangle(0, 0, BG_WIDTH, BG_HEIGHT);
            sky.setFill(new LinearGradient(
                    0, 0, 0, HORIZON / BG_HEIGHT, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#0d0020")),
                    new Stop(0.55, Color.web("#3b0f5e")),
                    new Stop(1, Color.web("#7a1f6b"))
            ));

            // звёзды
            Group stars = new Group();
            java.util.Random random = new java.util.Random(19_851_006);
            for (int i = 0; i < 120; i++) {
                double x = random.nextDouble() * BG_WIDTH;
                double y = random.nextDouble() * HORIZON * 0.9;
                Circle star = new Circle(x, y,
                        0.7 + random.nextDouble() * 1.5,
                        Color.web("#ffffff",
                                0.25 + random.nextDouble() * 0.6));
                stars.getChildren().add(star);
            }

            // неоновое солнце с полосами
            double sunX = BG_WIDTH / 2.0;
            double sunY = HORIZON - 60;
            double sunR = 190;

            Circle sun = new Circle(sunX, sunY, sunR);
            sun.setFill(new LinearGradient(
                    0, (sunY - sunR) / BG_HEIGHT, 0, (sunY + sunR) / BG_HEIGHT,
                    true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#ffe566")),
                    new Stop(0.55, Color.web("#ff9f43")),
                    new Stop(1, Color.web("#ff2fb9"))
            ));
            sun.setEffect(new DropShadow(60, Color.web("#ff2fb9", 0.55)));

            Group sunStripes = new Group();
            double stripeTop = sunY - 20;
            for (int i = 0; i < 7; i++) {
                double y = stripeTop + i * 22;
                if (y > sunY + sunR) break;
                double half = Math.sqrt(Math.max(0,
                        sunR * sunR - (y - sunY) * (y - sunY)));
                // полоса того же цвета неба, что и на этой высоте
                Rectangle stripe = new Rectangle(sunX - half - 2, y,
                        half * 2 + 4, 5 + i * 1.6);
                stripe.setFill(skyColorAt(y / (double) BG_HEIGHT));
                sunStripes.getChildren().add(stripe);
            }

            // земля
            Rectangle ground = new Rectangle(0, HORIZON,
                    BG_WIDTH, BG_HEIGHT - HORIZON);
            ground.setFill(Color.web("#08001a"));

            // перспективная сетка
            Group grid = new Group();
            double vanishingX = BG_WIDTH / 2.0;
            // вертикали
            for (int i = -14; i <= 14; i++) {
                double bottomX = vanishingX + i * 150;
                Line vertical = new Line(vanishingX, HORIZON,
                        bottomX, BG_HEIGHT);
                vertical.setStroke(Color.web("#ff2fb9", 0.55));
                vertical.setStrokeWidth(1.6);
                grid.getChildren().add(vertical);
            }
            // горизонтали (экспоненциально дальше от горизонта)
            double y = HORIZON + 4;
            double gap = 5;
            while (y < BG_HEIGHT) {
                Line horizontal = new Line(0, y, BG_WIDTH, y);
                horizontal.setStroke(Color.web("#ff2fb9", 0.5));
                horizontal.setStrokeWidth(1.6);
                grid.getChildren().add(horizontal);
                gap *= 1.35;
                y += gap;
            }

            // яркая линия горизонта
            Line horizonLine = new Line(0, HORIZON, BG_WIDTH, HORIZON);
            horizonLine.setStroke(Color.web("#67e8f9"));
            horizonLine.setStrokeWidth(3);
            horizonLine.setEffect(new DropShadow(24, Color.web("#67e8f9", 0.9)));

            root.getChildren().addAll(
                    sky, stars, sun, sunStripes,
                    ground, grid, horizonLine
            );

            var parameters = new javafx.scene.SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage snapshot = new WritableImage(BG_WIDTH, BG_HEIGHT);
            root.snapshot(parameters, snapshot);
            return snapshot;
        } catch (Throwable ex) {
            return gradientFallback();
        }
    }

    /** Цвет неба на высоте t (0..1) — для полос солнца. */
    private static Color skyColorAt(double t) {
        Color top = Color.web("#0d0020");
        Color middle = Color.web("#3b0f5e");
        Color bottom = Color.web("#7a1f6b");

        double limit = HORIZON / BG_HEIGHT;
        double local = Math.max(0, Math.min(1, t / limit));
        if (local < 0.55) {
            return top.interpolate(middle, local / 0.55);
        }
        return middle.interpolate(bottom, (local - 0.55) / 0.45);
    }

    /** Запасной фон: вертикальный градиент тех же цветов. */
    private Image gradientFallback() {
        WritableImage image = new WritableImage(BG_WIDTH, BG_HEIGHT);
        PixelWriter writer = image.getPixelWriter();

        int[] top = {0x0d, 0x00, 0x20};
        int[] bottom = {0x7a, 0x1f, 0x6b};

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
