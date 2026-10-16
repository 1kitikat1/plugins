package nemesis.plugins.theme;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import usa.kitikat.plugin.LauncherPlugin;
import usa.kitikat.plugin.PluginContext;

/**
 * Плагин «Цвет из фона»: смотрит на текущие обоины лаунчера, вычисляет
 * самый заметный (сочный) цвет и перекрашивает под него весь лаунчер —
 * кнопки, слайдеры, чекбоксы, подсветку карточек.
 *
 * <p>Пересобирается автоматически, когда пользователь меняет фон
 * в настройках ({@link PluginContext#onWallpaperChanged}).</p>
 */
public final class ThemeFromBackgroundPlugin implements LauncherPlugin {

    @Override
    public String id() {
        return "theme-from-background";
    }

    @Override
    public String name() {
        return "Цвет из фона";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public String description() {
        return "Определяет главный цвет текущих обоев и перекрашивает весь "
                + "лаунчер под него: кнопки, слайдеры, чекбоксы и подсветку. "
                + "Сам меняется при смене фона.";
    }

    @Override
    public void onLoad(PluginContext context) {
        apply(context);
        context.onWallpaperChanged(() -> apply(context));
        context.log("цвет подобран под фон");
    }

    /** Иконка для библиотеки плагинов — капля с градиентом. */
    @Override
    public javafx.scene.Node icon() {
        javafx.scene.shape.Path drop = new javafx.scene.shape.Path();
        drop.getElements().addAll(
                new javafx.scene.shape.MoveTo(22, 4),
                new javafx.scene.shape.CubicCurveTo(
                        22, 4, 35, 19.5, 35, 28),
                new javafx.scene.shape.ArcTo(13, 13, 0, 9, 28,
                        false, true),
                new javafx.scene.shape.CubicCurveTo(
                        9, 19.5, 22, 4, 22, 4),
                new javafx.scene.shape.ClosePath()
        );
        drop.setFill(new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#8b5cf6")),
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#ec4899"))
        ));
        drop.setStroke(javafx.scene.paint.Color.web("#d7c6ff"));
        drop.setStrokeWidth(1.5);

        javafx.scene.Group root = new javafx.scene.Group(drop);
        root.setEffect(new javafx.scene.effect.DropShadow(
                8, javafx.scene.paint.Color.web("#8b5cf6", 0.55)));
        return root;
    }

    private void apply(PluginContext context) {
        String hex = accentFromImage(context.currentWallpaper());
        if (hex != null) {
            context.setAccent(hex);
        }
    }

    /**
     * Возвращает акцентный hex из изображения либо null
     * (на слишком однотонном/тёмном фоне лучше оставить текущую тему).
     */
    static String accentFromImage(Image image) {
        if (image == null || image.getWidth() < 2 || image.getHeight() < 2) {
            return null;
        }

        PixelReader reader = image.getPixelReader();
        if (reader == null) {
            return null;
        }

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int stepX = Math.max(1, width / 160);
        int stepY = Math.max(1, height / 90);

        // 1) собираем самые насыщенные пиксели, взвешивая по насыщенности
        double r = 0, g = 0, b = 0, weightSum = 0;
        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                int argb = reader.getArgb(x, y);
                if (((argb >>> 24) & 0xFF) < 100) {
                    continue;   // прозрачный пиксель
                }
                double rr = (argb >> 16) & 0xFF;
                double gg = (argb >> 8) & 0xFF;
                double bb = argb & 0xFF;

                double max = Math.max(rr, Math.max(gg, bb));
                double min = Math.min(rr, Math.min(gg, bb));
                double value = max / 255.0;
                double saturation = max == 0 ? 0 : (max - min) / max;

                if (value < 0.16 || saturation < 0.14) {
                    continue;   // слишком тёмный или серый
                }
                double weight = saturation * saturation;
                r += rr * weight;
                g += gg * weight;
                b += bb * weight;
                weightSum += weight;
            }
        }

        if (weightSum < 0.0001) {
            return null;    // насыщенного цвета во фронте нет
        }

        int red = (int) Math.round(r / weightSum);
        int green = (int) Math.round(g / weightSum);
        int blue = (int) Math.round(b / weightSum);

        return toAccentHex(red, green, blue);
    }

    /**
     * Приводит усреднённый цвет к «акцентному тону»: насыщенность не ниже
     * 62%, яркость в комфортных для тёмного интерфейса границах.
     */
    private static String toAccentHex(int red, int green, int blue) {
        double rr = red / 255.0;
        double gg = green / 255.0;
        double bb = blue / 255.0;

        double max = Math.max(rr, Math.max(gg, bb));
        double min = Math.min(rr, Math.min(gg, bb));
        double delta = max - min;

        double hue;
        if (delta < 0.0001) {
            hue = 0;
        } else if (max == rr) {
            hue = 60 * (((gg - bb) / delta) % 6);
        } else if (max == gg) {
            hue = 60 * ((bb - rr) / delta + 2);
        } else {
            hue = 60 * ((rr - gg) / delta + 4);
        }
        if (hue < 0) {
            hue += 360;
        }

        double saturation = max == 0 ? 0 : delta / max;
        double value = max;

        saturation = Math.max(0.62, Math.min(0.92, saturation));
        value = Math.max(0.55, Math.min(0.74, value));

        int rgb = hsvToRgb(hue, saturation, value);
        return String.format("#%06x", rgb & 0xFFFFFF);
    }

    private static int hsvToRgb(double hue, double saturation, double value) {
        double c = value * saturation;
        double x = c * (1 - Math.abs((hue / 60) % 2 - 1));
        double m = value - c;

        double rr, gg, bb;
        if (hue < 60) {
            rr = c; gg = x; bb = 0;
        } else if (hue < 120) {
            rr = x; gg = c; bb = 0;
        } else if (hue < 180) {
            rr = 0; gg = c; bb = x;
        } else if (hue < 240) {
            rr = 0; gg = x; bb = c;
        } else if (hue < 300) {
            rr = x; gg = 0; bb = c;
        } else {
            rr = c; gg = 0; bb = x;
        }

        int red = (int) Math.round((rr + m) * 255);
        int green = (int) Math.round((gg + m) * 255);
        int blue = (int) Math.round((bb + m) * 255);
        return (red << 16) | (green << 8) | blue;
    }
}
