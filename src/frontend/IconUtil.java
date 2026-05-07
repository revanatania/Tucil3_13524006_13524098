import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashMap;

public class IconUtil {

    private static final HashMap<String, Image> CACHE = new HashMap<>();
    public static Node makeIcon(String[] paths, double size, String fallbackEmoji) {
        String key = paths[0]; 
        Image img = CACHE.get(key);

        if (img == null && !CACHE.containsKey(key)) {
            Image raw = loadImage(paths);
            if (raw != null && !raw.isError()) {
                Image cleaned = removeBlackBackground(raw);
                Image trimmed = trimTransparentBounds(cleaned);
                img = toSquareCanvas(trimmed);
            }
            CACHE.put(key, img); 
        }

        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            return iv;
        }

        
        Label lbl = new Label(fallbackEmoji);
        lbl.getStyleClass().add("tile-content-icon");
        return lbl;
    }

    private static Image removeBlackBackground(Image src) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();
        if (w <= 0 || h <= 0) return src;

        PixelReader pr = src.getPixelReader();
        if (pr == null) return src;

        WritableImage out = new WritableImage(w, h);
        PixelWriter pw = out.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = pr.getColor(x, y);
                double lum = 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
                if (lum < 0.10) {
                    pw.setColor(x, y, Color.TRANSPARENT);
                } else {
                    pw.setColor(x, y, c);
                }
            }
        }
        return out;
    }
    private static Image trimTransparentBounds(Image src) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();
        if (w <= 0 || h <= 0) return src;

        PixelReader pr = src.getPixelReader();
        if (pr == null) return src;

        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (pr.getColor(x, y).getOpacity() > 0.02) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < 0 || maxY < 0) return src; 

        int bw = maxX - minX + 1;
        int bh = maxY - minY + 1;
        WritableImage out = new WritableImage(bw, bh);
        PixelWriter pw = out.getPixelWriter();
        for (int y = 0; y < bh; y++) {
            for (int x = 0; x < bw; x++) {
                pw.setColor(x, y, pr.getColor(minX + x, minY + y));
            }
        }
        return out;
    }

    private static Image toSquareCanvas(Image src) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();
        if (w <= 0 || h <= 0) return src;

        int side = Math.max(w, h);
        WritableImage out = new WritableImage(side, side);
        PixelReader pr = src.getPixelReader();
        PixelWriter pw = out.getPixelWriter();
        if (pr == null) return src;

        int offX = (side - w) / 2;
        int offY = (side - h) / 2;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pw.setColor(offX + x, offY + y, pr.getColor(x, y));
            }
        }
        return out;
    }
 
    static Image loadImage(String[] paths) {
        for (String path : paths) {
            try {
                InputStream is = IconUtil.class.getResourceAsStream(path);
                if (is != null) {
                    Image img = new Image(is);
                    if (!img.isError()) {
                        System.out.println("[IconUtil] Loaded: " + path);
                        return img;
                    }
                }
            } catch (Exception ignored) {}
        }
        System.err.println("[IconUtil] Not found: " + paths[0]);
        return null;
    }
}
