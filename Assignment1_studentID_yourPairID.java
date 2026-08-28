import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import javax.swing.*;

public class Assignment1_studentID_yourPairID extends JPanel implements Runnable {

    // animation timing
    // 0.0 -> 6.8    Scene 1: Night stargazing, zoom into eye, 1st-person POV sky & blink, eye close -> flashback
    // 6.8 -> 11.8   Scene 2 (Memory 1): Football match & bicycle kick
    // 11.8 -> 17.8  Scene 3 (Memory 2): Epic childhood toy sword fight
    // 17.8 -> 22.8  Scene 4 (Memory 3): Playing in a cheerful forest stream
    // 22.8 -> 28.8  Scene 5 (Memory 4): Riding bicycles at sunset countryside (ref/scene5)
    // 28.8 -> 34.8  Scene 6 (Memory 5): Watching TV together - Ultraman vs Godzilla! (ref/scene6)
    // 34.8 -> 40.8  Scene 7 (Memory 6): Cozy Moo Kratha dinner at home with family (ref/scene7)
    // 40.8 -> 43.8  Scene 8: Back to present, wake up POV, zoom out, tear flowing down cheek
    volatile double totalTime = 0;
    static final double CYCLE = 43.8;
    static final double SHOOT_START = 1.0;
    static final double SHOOT_DURATION = 1.5;
    static final double POV_ENTER_START = 2.4;
    static final double POV_START = 3.4;
    static final double POV_BLINK_START = 4.0;
    static final double POV_BLINK_END = 4.45;
    static final double POV_CLOSE_START = 5.6;
    static final double POV_CLOSE_END = 6.5;
    static final double WARP_INTO_MEMORY = 6.8;
    static final double WARP_INTO_SWORD = 11.8;
    static final double WARP_INTO_WATER = 17.8;
    static final double WARP_INTO_BIKE = 22.8;
    static final double WARP_INTO_TV = 28.8;
    static final double WARP_INTO_MOOKRATHA = 34.8;
    static final double WARP_BACK = 40.8;
    static final double POV_WAKE_START = 40.8;
    static final double POV_WAKE_END = 41.6;
    static final double POV_EXIT = 41.6;
    static final double TEAR_START = 41.4;
    static final double TEAR_END = 42.9;
    static final double WARP_RAMP = 0.45;
    static final int FRAME_MS = 16;

    private BufferedImage bicycleBackdrop = null;
    private BufferedImage livingRoomBackdrop = null;

    private static final int NUM_STARS = 260;
    private static final double[] starX = new double[NUM_STARS];
    private static final double[] starY = new double[NUM_STARS];
    private static final double[] starSize = new double[NUM_STARS];
    private static final double[] starTwinkleSpeed = new double[NUM_STARS];
    private static final double[] starPhase = new double[NUM_STARS];
    private static final int[] starType = new int[NUM_STARS];

    private static final int NUM_FLOWERS = 36;
    private static final double[] flowerX = new double[NUM_FLOWERS];
    private static final double[] flowerY = new double[NUM_FLOWERS];
    private static final double[] flowerScale = new double[NUM_FLOWERS];
    private static final double[] flowerRot = new double[NUM_FLOWERS];

    private static final int NUM_GRASS = 120;
    private static final double[] grassX = new double[NUM_GRASS];
    private static final double[] grassY = new double[NUM_GRASS];
    private static final double[] grassHeight = new double[NUM_GRASS];
    private static final double[] grassBend = new double[NUM_GRASS];

    static final int NUM_BLADES = 55;
    static final double[] bladeX = new double[NUM_BLADES];
    static final double[] bladeY = new double[NUM_BLADES];
    static final double[] bladeH = new double[NUM_BLADES];

    static {
        Random rand = new Random(2026);
        for (int i = 0; i < NUM_STARS; i++) {
            starX[i] = rand.nextDouble() * 900 - 150;
            starY[i] = rand.nextDouble() * 420;
            starSize[i] = 1.0 + rand.nextDouble() * 2.2;
            starTwinkleSpeed[i] = 1.8 + rand.nextDouble() * 3.8;
            starPhase[i] = rand.nextDouble() * Math.PI * 2;
            starType[i] = rand.nextInt(4);
        }

        for (int i = 0; i < NUM_FLOWERS; i++) {
            flowerX[i] = 15 + rand.nextDouble() * 570;
            flowerY[i] = 510 + rand.nextDouble() * 80;
            flowerScale[i] = 0.55 + rand.nextDouble() * 0.45;
            flowerRot[i] = (rand.nextDouble() - 0.5) * 0.8;
        }

        for (int i = 0; i < NUM_GRASS; i++) {
            grassX[i] = -20 + rand.nextDouble() * 640;
            grassY[i] = 490 + rand.nextDouble() * 110;
            grassHeight[i] = 20 + rand.nextDouble() * 40;
            grassBend[i] = (rand.nextDouble() - 0.5) * 24;
        }

        Random grand = new Random(7);
        for (int i = 0; i < NUM_BLADES; i++) {
            bladeX[i] = grand.nextDouble() * 600;
            bladeY[i] = 410 + grand.nextDouble() * 185;
            bladeH[i] = 10 + grand.nextDouble() * 10;
        }
    }

    public Assignment1_studentID_yourPairID() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.BLACK);
    }

    // Lab raster primitives. Graphics is used only to place the final raster spans.
    private static void paintSpan(Graphics g, int x, int y, int width) {
        if (width > 0) g.fillRect(x, y, width, 1);
    }

    private static void plotPixel(Graphics g, int x, int y) {
        paintSpan(g, x, y, 1);
    }

    private static void fillRectangle(Graphics g, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        for (int row = 0; row < height; row++) paintSpan(g, x, y + row, width);
    }

    private static void drawRectangle(Graphics g, int x, int y, int width, int height) {
        drawRasterLine(g, x, y, x + width, y);
        drawRasterLine(g, x + width, y, x + width, y + height);
        drawRasterLine(g, x + width, y + height, x, y + height);
        drawRasterLine(g, x, y + height, x, y);
    }

    private static void bresenhamLine(Graphics g, int x1, int y1, int x2, int y2) {
        bresenhamLine(g, x1, y1, x2, y2, 0);
    }

    private static void bresenhamLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int stepX = x1 < x2 ? 1 : -1;
        int stepY = y1 < y2 ? 1 : -1;
        int error = dx - dy;
        int size = thickness * 2 + 1;

        while (true) {
            fillRectangle(g, x1 - thickness, y1 - thickness, size, size);
            if (x1 == x2 && y1 == y2) return;
            int doubledError = error * 2;
            if (doubledError > -dy) {
                error -= dy;
                x1 += stepX;
            }
            if (doubledError < dx) {
                error += dx;
                y1 += stepY;
            }
        }
    }

    private static void drawRasterLine(Graphics g, int x1, int y1, int x2, int y2) {
        bresenhamLine(g, x1, y1, x2, y2);
    }

    private static void plotCirclePoints(Graphics g, int cx, int cy, int x, int y) {
        plotPixel(g, cx + x, cy + y);
        plotPixel(g, cx - x, cy + y);
        plotPixel(g, cx + x, cy - y);
        plotPixel(g, cx - x, cy - y);
        plotPixel(g, cx + y, cy + x);
        plotPixel(g, cx - y, cy + x);
        plotPixel(g, cx + y, cy - x);
        plotPixel(g, cx - y, cy - x);
    }

    private static void midpointCircle(Graphics g, int cx, int cy, int radius) {
        int x = 0;
        int y = radius;
        int decision = 1 - radius;
        while (x <= y) {
            plotCirclePoints(g, cx, cy, x, y);
            x++;
            if (decision < 0) {
                decision += 2 * x + 1;
            } else {
                y--;
                decision += 2 * (x - y) + 1;
            }
        }
    }

    public static void drawMidpointCircle(Graphics g, int cx, int cy, int radius, Color color) {
        g.setColor(color);
        midpointCircle(g, cx, cy, radius);
    }

    public static void fillMidpointCircle(Graphics g, int cx, int cy, int radius, Color color) {
        g.setColor(color);
        fillEllipseRaster(g, cx, cy, radius, radius);
    }

    private static void fillEllipseRaster(Graphics g, int cx, int cy, int radiusX, int radiusY) {
        if (radiusX <= 0 || radiusY <= 0) return;
        for (int y = -radiusY; y <= radiusY; y++) {
            double normalizedY = (double) y / radiusY;
            int halfWidth = (int) Math.round(radiusX * Math.sqrt(Math.max(0, 1 - normalizedY * normalizedY)));
            paintSpan(g, cx - halfWidth, cy + y, halfWidth * 2 + 1);
        }
    }

    public static void fillMidpointEllipse(Graphics g, int cx, int cy, int radiusX, int radiusY, Color color) {
        g.setColor(color);
        fillEllipseRaster(g, cx, cy, radiusX, radiusY);
    }

    private static void fillEllipse(Graphics g, int x, int y, int width, int height) {
        fillEllipseRaster(g, x + width / 2, y + height / 2, width / 2, height / 2);
    }

    private static void drawEllipse(Graphics g, int x, int y, int width, int height) {
        midpointEllipse(g, x + width / 2, y + height / 2, width / 2, height / 2);
    }

    public static void midpointEllipse(Graphics g, int cx, int cy, int radiusX, int radiusY) {
        long rx2 = (long) radiusX * radiusX;
        long ry2 = (long) radiusY * radiusY;
        long x = 0;
        long y = radiusY;
        long dx = 0;
        long dy = 2 * rx2 * y;
        double decision = ry2 - rx2 * radiusY + 0.25 * rx2;

        while (dx < dy) {
            plotEllipsePoints(g, cx, cy, (int) x, (int) y);
            x++;
            dx += 2 * ry2;
            if (decision < 0) {
                decision += dx + ry2;
            } else {
                y--;
                dy -= 2 * rx2;
                decision += dx - dy + ry2;
            }
        }

        decision = ry2 * Math.pow(x + 0.5, 2) + rx2 * Math.pow(y - 1, 2) - rx2 * ry2;
        while (y >= 0) {
            plotEllipsePoints(g, cx, cy, (int) x, (int) y);
            y--;
            dy -= 2 * rx2;
            if (decision > 0) {
                decision += rx2 - dy;
            } else {
                x++;
                dx += 2 * ry2;
                decision += dx - dy + rx2;
            }
        }
    }

    private static void plotEllipsePoints(Graphics g, int cx, int cy, int x, int y) {
        plotPixel(g, cx + x, cy + y);
        plotPixel(g, cx - x, cy + y);
        plotPixel(g, cx + x, cy - y);
        plotPixel(g, cx - x, cy - y);
    }

    private static void fillPolygonScanline(Graphics g, Polygon polygon) {
        fillPolygonScanline(g, polygon.xpoints, polygon.ypoints, polygon.npoints);
    }

    private static void fillPolygonScanline(Graphics g, int[] xPoints, int[] yPoints, int count) {
        if (count < 3) return;
        int minY = yPoints[0];
        int maxY = yPoints[0];
        for (int i = 1; i < count; i++) {
            minY = Math.min(minY, yPoints[i]);
            maxY = Math.max(maxY, yPoints[i]);
        }

        int[] intersections = new int[count];
        for (int y = minY; y <= maxY; y++) {
            int found = 0;
            for (int i = 0, previous = count - 1; i < count; previous = i++) {
                int y1 = yPoints[previous];
                int y2 = yPoints[i];
                if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y)) {
                    double ratio = (double) (y - y1) / (y2 - y1);
                    intersections[found++] = (int) Math.round(xPoints[previous] + ratio * (xPoints[i] - xPoints[previous]));
                }
            }
            java.util.Arrays.sort(intersections, 0, found);
            for (int i = 0; i + 1 < found; i += 2) {
                paintSpan(g, intersections[i], y, intersections[i + 1] - intersections[i] + 1);
            }
        }
    }

    private static void drawPolygonLines(Graphics g, Polygon polygon) {
        drawPolygonLines(g, polygon.xpoints, polygon.ypoints, polygon.npoints);
    }

    private static void drawPolygonLines(Graphics g, int[] xPoints, int[] yPoints, int count) {
        if (count < 2) return;
        for (int i = 0; i < count; i++) {
            int next = (i + 1) % count;
            drawRasterLine(g, xPoints[i], yPoints[i], xPoints[next], yPoints[next]);
        }
    }

    private static void fillRoundedRectangle(Graphics g, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        int radiusX = Math.max(1, Math.min(width / 2, arcWidth / 2));
        int radiusY = Math.max(1, Math.min(height / 2, arcHeight / 2));
        for (int row = 0; row < height; row++) {
            int inset = 0;
            if (row < radiusY || row >= height - radiusY) {
                double dy = row < radiusY ? radiusY - row - 0.5 : row - (height - radiusY) + 0.5;
                inset = radiusX - (int) Math.round(radiusX * Math.sqrt(Math.max(0, 1 - dy * dy / (radiusY * radiusY))));
            }
            paintSpan(g, x + inset, y + row, width - inset * 2);
        }
    }

    private static void drawRoundedRectangle(Graphics g, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        int radiusX = Math.max(1, Math.min(width / 2, arcWidth / 2));
        int radiusY = Math.max(1, Math.min(height / 2, arcHeight / 2));
        drawRasterLine(g, x + radiusX, y, x + width - radiusX, y);
        drawRasterLine(g, x + radiusX, y + height, x + width - radiusX, y + height);
        drawRasterLine(g, x, y + radiusY, x, y + height - radiusY);
        drawRasterLine(g, x + width, y + radiusY, x + width, y + height - radiusY);
        drawEllipseArc(g, x + radiusX, y + radiusY, radiusX, radiusY, 180, 270);
        drawEllipseArc(g, x + width - radiusX, y + radiusY, radiusX, radiusY, 270, 360);
        drawEllipseArc(g, x + width - radiusX, y + height - radiusY, radiusX, radiusY, 0, 90);
        drawEllipseArc(g, x + radiusX, y + height - radiusY, radiusX, radiusY, 90, 180);
    }

    private static void drawEllipseArc(Graphics g, int cx, int cy, int radiusX, int radiusY, int startDegree, int endDegree) {
        int previousX = cx + (int) Math.round(radiusX * Math.cos(Math.toRadians(startDegree)));
        int previousY = cy + (int) Math.round(radiusY * Math.sin(Math.toRadians(startDegree)));
        for (int degree = startDegree + 3; degree <= endDegree; degree += 3) {
            int currentX = cx + (int) Math.round(radiusX * Math.cos(Math.toRadians(degree)));
            int currentY = cy + (int) Math.round(radiusY * Math.sin(Math.toRadians(degree)));
            drawRasterLine(g, previousX, previousY, currentX, currentY);
            previousX = currentX;
            previousY = currentY;
        }
    }

    private static void fillShapeScanline(Graphics2D g, Shape shape) {
        java.util.List<Point> points = new java.util.ArrayList<>();
        PathIterator path = shape.getPathIterator(null, 0.75);
        double[] coordinates = new double[6];
        while (!path.isDone()) {
            int type = path.currentSegment(coordinates);
            if (type == PathIterator.SEG_MOVETO && !points.isEmpty()) {
                fillPointPolygon(g, points);
                points.clear();
            }
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                points.add(new Point((int) Math.round(coordinates[0]), (int) Math.round(coordinates[1])));
            } else if (type == PathIterator.SEG_CLOSE && !points.isEmpty()) {
                fillPointPolygon(g, points);
                points.clear();
            }
            path.next();
        }
        if (!points.isEmpty()) fillPointPolygon(g, points);
    }

    private static void fillPointPolygon(Graphics g, java.util.List<Point> points) {
        int[] xPoints = new int[points.size()];
        int[] yPoints = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            xPoints[i] = points.get(i).x;
            yPoints[i] = points.get(i).y;
        }
        fillPolygonScanline(g, xPoints, yPoints, points.size());
    }

    private static void drawShapeLines(Graphics2D g, Shape shape) {
        PathIterator path = shape.getPathIterator(null, 0.75);
        double[] coordinates = new double[6];
        int startX = 0;
        int startY = 0;
        int previousX = 0;
        int previousY = 0;
        while (!path.isDone()) {
            int type = path.currentSegment(coordinates);
            if (type == PathIterator.SEG_MOVETO) {
                startX = previousX = (int) Math.round(coordinates[0]);
                startY = previousY = (int) Math.round(coordinates[1]);
            } else if (type == PathIterator.SEG_LINETO) {
                int x = (int) Math.round(coordinates[0]);
                int y = (int) Math.round(coordinates[1]);
                drawRasterLine(g, previousX, previousY, x, y);
                previousX = x;
                previousY = y;
            } else if (type == PathIterator.SEG_CLOSE) {
                drawRasterLine(g, previousX, previousY, startX, startY);
            }
            path.next();
        }
    }

    public BufferedImage floodFill(BufferedImage image, int startX, int startY, Color target, Color replacement) {
        int targetRgb = target.getRGB();
        int replacementRgb = replacement.getRGB();
        if (targetRgb == replacementRgb || startX < 0 || startX >= image.getWidth()
                || startY < 0 || startY >= image.getHeight() || image.getRGB(startX, startY) != targetRgb) return image;

        Queue<Point> pixels = new LinkedList<>();
        pixels.add(new Point(startX, startY));
        image.setRGB(startX, startY, replacementRgb);
        int[] offsetX = {0, 0, 1, -1};
        int[] offsetY = {1, -1, 0, 0};
        while (!pixels.isEmpty()) {
            Point pixel = pixels.poll();
            for (int i = 0; i < 4; i++) {
                int x = pixel.x + offsetX[i];
                int y = pixel.y + offsetY[i];
                if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()
                        && image.getRGB(x, y) == targetRgb) {
                    image.setRGB(x, y, replacementRgb);
                    pixels.add(new Point(x, y));
                }
            }
        }
        return image;
    }

    private void bezierCurve(Graphics g, double x1, double y1, double x2, double y2,
                              double x3, double y3, double x4, double y4, int steps) {
        int prevX = (int) Math.round(x1);
        int prevY = (int) Math.round(y1);
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            double u = 1 - t;
            double b0 = u * u * u;
            double b1 = 3 * t * u * u;
            double b2 = 3 * t * t * u;
            double b3 = t * t * t;

            int currX = (int) Math.round(b0 * x1 + b1 * x2 + b2 * x3 + b3 * x4);
            int currY = (int) Math.round(b0 * y1 + b1 * y2 + b2 * y3 + b3 * y4);
            bresenhamLine(g, prevX, prevY, currX, currY);
            prevX = currX;
            prevY = currY;
        }
    }

    private void bezierCurve(Graphics g, double x1, double y1, double x2, double y2,
                              double x3, double y3, double x4, double y4) {
        bezierCurve(g, x1, y1, x2, y2, x3, y3, x4, y4, 24);
    }

    private static double smoothStep(double value) {
        double t = Math.max(0.0, Math.min(1.0, value));
        return t * t * (3.0 - 2.0 * t);
    }

    private double calculateStickmanBreathe(double t) {
        // Move the body gently up and down while breathing.
        double baseBreathe = Math.sin(t * 1.2) * 2.0;
        if (t >= POV_WAKE_START && t <= CYCLE) {
            double sobHitch = Math.sin(t * 10.0) * Math.exp(-(t - POV_WAKE_START) * 0.7) * 1.2;
            return baseBreathe + sobHitch;
        }
        return baseBreathe;
    }

    private Point2D.Double getStickmanEyePosition(double t) {
        double breathe = calculateStickmanBreathe(t);
        double headX = 150.0;
        double headY = 455.0 + breathe;
        double localEyeX = -4.0;
        double localEyeY = -8.0;
        double rad = Math.toRadians(-60);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double worldEyeX = headX + (localEyeX * cos - localEyeY * sin);
        double worldEyeY = headY + (localEyeX * sin + localEyeY * cos);
        return new Point2D.Double(worldEyeX, worldEyeY);
    }

    // 2. SCENE 1: NIGHT STARGAZING (3RD PERSON + FIRST-PERSON POV & BLINK)

    private void drawNightScene(Graphics2D g2d, double t) {
        boolean isPOV = (t >= POV_START && t < WARP_INTO_MEMORY) || (t >= WARP_BACK && t < POV_EXIT);

        if (isPOV) {
            drawPOVSkyScene(g2d, t);
        } else {
            drawThirdPersonNightScene(g2d, t);
        }

        drawVignette(g2d);
    }

    private void drawThirdPersonNightScene(Graphics2D g2d, double t) {
        AffineTransform screenTransform = g2d.getTransform();

        // Zoom into the eye, then zoom out after the memories.
        double zoom = 1.0;
        if (t >= POV_ENTER_START && t < POV_START) {
            double p = smoothStep((t - POV_ENTER_START) / (POV_START - POV_ENTER_START));
            zoom = 1.0 + 1.5 * p;
        } else if (t >= POV_EXIT && t < TEAR_END) {
            double p = smoothStep((t - POV_EXIT) / (TEAR_END - POV_EXIT));
            zoom = 2.5 - 1.5 * p;
        }

        if (zoom > 1.0) {
            Point2D.Double eyePos = getStickmanEyePosition(t);
            g2d.translate(eyePos.x, eyePos.y);
            g2d.scale(zoom, zoom);
            g2d.translate(-eyePos.x, -eyePos.y);
        }

        drawSkyBackground(g2d, t);
        drawMilkyWay(g2d, t);
        drawMoon(g2d, t);
        drawStars(g2d, t);
        drawShootingStar(g2d, t);
        drawDistantMountains(g2d);
        drawGrassyHill(g2d, t);
        drawStickman(g2d, t);
        drawForegroundFlowersAndGrass(g2d, t);

        g2d.setTransform(screenTransform);
    }

    private void drawPOVSkyScene(Graphics2D g2d, double t) {
        drawSkyBackground(g2d, t);
        drawMilkyWay(g2d, t);
        drawMoon(g2d, t);
        drawStars(g2d, t);
        drawShootingStar(g2d, t);

        drawPOVGrass(g2d, t);

        // Close and open both eyelids according to the scene time.
        double closure = 0.0;
        if (t >= POV_START && t < WARP_INTO_MEMORY) {
            if (t >= POV_BLINK_START && t < POV_BLINK_END) {
                double bp = (t - POV_BLINK_START) / (POV_BLINK_END - POV_BLINK_START);
                closure = Math.sin(bp * Math.PI) * 0.52;
            } else if (t >= POV_CLOSE_START && t < POV_CLOSE_END) {
                double cp = (t - POV_CLOSE_START) / (POV_CLOSE_END - POV_CLOSE_START);
                closure = smoothStep(cp);
            } else if (t >= POV_CLOSE_END) {
                closure = 1.0;
            }
        } else if (t >= POV_WAKE_START && t < POV_WAKE_END) {
            double wp = (t - POV_WAKE_START) / (POV_WAKE_END - POV_WAKE_START);
            closure = 1.0 - smoothStep(wp);
        }

        drawPOVEyelids(g2d, closure);
    }

    private void drawPOVGrass(Graphics2D g2d, double t) {
        double wind = Math.sin(t * 2.2) * 6.0;
        g2d.setColor(new Color(4, 14, 20, 210));

        for (int i = 0; i < 8; i++) {
            double bx = i * 28 - 20;
            double by = 620;
            double bh = 90 + (i % 3) * 35;
            double bend = -15 + (i * 4) + wind * 0.8;
            Path2D.Double blade = new Path2D.Double();
            blade.moveTo(bx - 3, by);
            blade.curveTo(bx - 1, by - bh * 0.5, bx + bend * 0.6, by - bh * 0.8, bx + bend, by - bh);
            blade.curveTo(bx + bend * 0.4, by - bh * 0.7, bx + 3, by - bh * 0.4, bx + 3, by);
            blade.closePath();
            fillShapeScanline(g2d, blade);
        }

        for (int i = 0; i < 8; i++) {
            double bx = 420 + i * 28;
            double by = 620;
            double bh = 85 + ((8 - i) % 3) * 35;
            double bend = 15 - (i * 4) + wind * 0.8;
            Path2D.Double blade = new Path2D.Double();
            blade.moveTo(bx - 3, by);
            blade.curveTo(bx - 1, by - bh * 0.5, bx + bend * 0.6, by - bh * 0.8, bx + bend, by - bh);
            blade.curveTo(bx + bend * 0.4, by - bh * 0.7, bx + 3, by - bh * 0.4, bx + 3, by);
            blade.closePath();
            fillShapeScanline(g2d, blade);
        }
    }

    private void drawPOVEyelids(Graphics2D g2d, double closure) {
        if (closure <= 0.002) return;
        if (closure >= 0.998) {
            g2d.setColor(Color.BLACK);
            fillRectangle(g2d, 0, 0, 600, 600);
            return;
        }

        double topH = 340.0 * closure;
        double botH = 280.0 * closure;
        double curveBow = 50.0 * (1.0 - closure * 0.3);

        Path2D.Double topEyelid = new Path2D.Double();
        topEyelid.moveTo(-20, -20);
        topEyelid.lineTo(620, -20);
        topEyelid.lineTo(620, topH - curveBow * 0.3);
        topEyelid.curveTo(420, topH + curveBow, 180, topH + curveBow, -20, topH - curveBow * 0.3);
        topEyelid.closePath();

        Path2D.Double botEyelid = new Path2D.Double();
        botEyelid.moveTo(-20, 620);
        botEyelid.lineTo(620, 620);
        botEyelid.lineTo(620, 620 - botH + curveBow * 0.3);
        botEyelid.curveTo(420, 620 - botH - curveBow, 180, 620 - botH - curveBow, -20, 620 - botH + curveBow * 0.3);
        botEyelid.closePath();

        g2d.setColor(Color.BLACK);
        fillShapeScanline(g2d, topEyelid);
        fillShapeScanline(g2d, botEyelid);

        g2d.setColor(new Color(0, 0, 0, (int) (140 * closure)));
        g2d.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawShapeLines(g2d, topEyelid);
        drawShapeLines(g2d, botEyelid);
    }

    public void drawSkyBackground(Graphics2D g2d, double time) {
        Point2D start = new Point2D.Float(300, 0);
        Point2D end = new Point2D.Float(300, 600);
        float[] dist = {0.0f, 0.4f, 0.75f, 1.0f};
        Color[] colors = {
            new Color(2, 6, 20),
            new Color(8, 22, 58),
            new Color(16, 45, 100),
            new Color(28, 70, 130)
        };
        LinearGradientPaint skyGrad = new LinearGradientPaint(start, end, dist, colors);
        g2d.setPaint(skyGrad);
        fillRectangle(g2d, -400, -400, 1400, 1400);
    }

    public void drawMilkyWay(Graphics2D g2d, double time) {
        AffineTransform oldTx = g2d.getTransform();

        g2d.translate(338, 265);
        g2d.rotate(Math.toRadians(-46.15));

        int[] bandWidths = {220, 150, 90, 45};
        Color[] bandColors = {
            new Color(40, 75, 170, 16),
            new Color(80, 130, 225, 26),
            new Color(140, 170, 255, 36),
            new Color(210, 230, 255, 48)
        };

        for (int i = 0; i < bandWidths.length; i++) {
            Path2D.Double band = new Path2D.Double();
            int w = bandWidths[i];
            band.moveTo(-600, -w / 2.0);
            band.curveTo(-200, -w * 0.7, 100, -w * 0.3, 600, -w / 2.0);
            band.curveTo(100, w * 0.3, -200, w * 0.7, -600, w / 2.0);
            band.closePath();

            g2d.setPaint(new RadialGradientPaint(
                new Point2D.Double(0, 0), 550f,
                new float[]{0f, 0.5f, 1f},
                new Color[]{bandColors[i], new Color(bandColors[i].getRed(), bandColors[i].getGreen(), bandColors[i].getBlue(), bandColors[i].getAlpha() / 2), new Color(0, 0, 0, 0)}
            ));
            fillShapeScanline(g2d, band);
        }

        Random gRand = new Random(777);
        for (int i = 0; i < 110; i++) {
            int gx = gRand.nextInt(1000) - 500;
            int gy = (int) (gRand.nextGaussian() * 30);
            int gr = 1 + gRand.nextInt(2);
            int alpha = 40 + gRand.nextInt(150);
            Color dustColor = (gRand.nextInt(3) == 0) ?
                    new Color(230, 200, 255, alpha) :
                    new Color(200, 230, 255, alpha);
            fillMidpointEllipse(g2d, gx, gy, gr, gr + (gRand.nextBoolean() ? 1 : 0), dustColor);
        }

        g2d.setTransform(oldTx);
    }

    public void drawMoon(Graphics2D g2d, double time) {
        int moonX = 525;
        int moonY = 70;
        int moonR = 26;

        for (int r = moonR + 40; r >= moonR; r -= 3) {
            int alpha = (int) (20 * (1.0 - (double) (r - moonR) / 40.0));
            fillMidpointCircle(g2d, moonX, moonY, r, new Color(165, 205, 255, alpha));
        }

        fillMidpointCircle(g2d, moonX, moonY, moonR + 4, new Color(220, 240, 255, 65));

        fillMidpointCircle(g2d, moonX, moonY, moonR, new Color(245, 250, 255));

        fillMidpointEllipse(g2d, moonX - 6, moonY - 4, 6, 8, new Color(205, 218, 235, 160));
        fillMidpointEllipse(g2d, moonX + 7, moonY - 7, 5, 4, new Color(210, 222, 238, 140));
        fillMidpointCircle(g2d, moonX - 3, moonY + 9, 5, new Color(200, 215, 232, 150));
        fillMidpointEllipse(g2d, moonX + 8, moonY + 6, 7, 5, new Color(195, 210, 230, 130));
        fillMidpointCircle(g2d, moonX + 1, moonY + 2, 4, new Color(212, 225, 240, 170));

        drawMidpointCircle(g2d, moonX, moonY, moonR, new Color(255, 255, 255, 210));
    }

    public void drawStars(Graphics2D g2d, double time) {
        for (int i = 0; i < NUM_STARS; i++) {
            double tw = Math.sin(time * starTwinkleSpeed[i] + starPhase[i]);
            double brightness = 0.35 + 0.65 * ((tw + 1.0) / 2.0);
            int alpha = (int) (Math.min(255, Math.max(30, brightness * 255)));

            Color c = (starType[i] == 0) ? new Color(175, 215, 255, alpha) :
                      (starType[i] == 1) ? new Color(255, 230, 200, alpha) :
                      (starType[i] == 2) ? new Color(220, 200, 255, alpha) :
                      new Color(255, 255, 255, alpha);

            int x = (int) starX[i];
            int y = (int) starY[i];
            int r = (int) Math.round(starSize[i]);

            if (r <= 1) {
                g2d.setColor(c);
                fillRectangle(g2d, x, y, 1, 1);
            } else {
                fillMidpointCircle(g2d, x, y, r, c);
                if (starSize[i] > 2.0 && brightness > 0.75) {
                    g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 0.6)));
                    int flareLen = (int) (r * 3.5);
                    drawRasterLine(g2d, x - flareLen, y, x + flareLen, y);
                    drawRasterLine(g2d, x, y - flareLen, x, y + flareLen);
                }
            }
        }
    }

    public void drawShootingStar(Graphics2D g2d, double time) {
        double local = time - SHOOT_START;
        if (local >= 0 && local <= SHOOT_DURATION) {
            double progress = local / SHOOT_DURATION;
            double startX = 460;
            double startY = 25;
            double dx = -260 * progress;
            double dy = 150 * progress;

            double headX = startX + dx;
            double headY = startY + dy;
            double tailLen = 75;
            double tailX = headX + tailLen * 0.86;
            double tailY = headY - tailLen * 0.5;

            float alpha = (float) Math.sin(progress * Math.PI);

            Point2D pHead = new Point2D.Double(headX, headY);
            Point2D pTail = new Point2D.Double(tailX, tailY);
            g2d.setPaint(new LinearGradientPaint(pHead, pTail, new float[]{0f, 1f},
                    new Color[]{new Color(255, 255, 255, (int) (alpha * 240)), new Color(160, 210, 255, 0)}));
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawShapeLines(g2d, new Line2D.Double(pHead, pTail));

            fillMidpointCircle(g2d, (int) headX, (int) headY, 3, new Color(255, 255, 255, (int) (alpha * 255)));
            fillMidpointCircle(g2d, (int) headX, (int) headY, 6, new Color(180, 220, 255, (int) (alpha * 120)));
        }
    }

    public void drawDistantMountains(Graphics2D g2d) {
        Path2D.Double mountains = new Path2D.Double();
        mountains.moveTo(-60, 470);
        mountains.curveTo(120, 425, 220, 455, 340, 430);
        mountains.curveTo(430, 410, 520, 445, 680, 420);
        mountains.lineTo(680, 600);
        mountains.lineTo(-60, 600);
        mountains.closePath();

        LinearGradientPaint mtnGrad = new LinearGradientPaint(
            new Point2D.Float(0, 410), new Point2D.Float(0, 520),
            new float[]{0f, 1f},
            new Color[]{new Color(14, 32, 68, 220), new Color(7, 18, 42, 245)}
        );
        g2d.setPaint(mtnGrad);
        fillShapeScanline(g2d, mountains);
    }

    public void drawGrassyHill(Graphics2D g2d, double time) {
        Path2D.Double hill = new Path2D.Double();
        hill.moveTo(-60, 515);
        hill.curveTo(150, 500, 380, 510, 680, 500);
        hill.lineTo(680, 650);
        hill.lineTo(-60, 650);
        hill.closePath();

        LinearGradientPaint hillGrad = new LinearGradientPaint(
            new Point2D.Float(0, 450), new Point2D.Float(0, 600),
            new float[]{0f, 0.4f, 1.0f},
            new Color[]{new Color(6, 24, 34), new Color(4, 16, 22), new Color(2, 8, 12)}
        );
        g2d.setPaint(hillGrad);
        fillShapeScanline(g2d, hill);
    }

    public void drawForegroundFlowersAndGrass(Graphics2D g2d, double time) {
        double wind = Math.sin(time * 2.2) * 5.0;

        for (int i = 0; i < NUM_GRASS; i++) {
            double gx = grassX[i];
            double gy = grassY[i];

            if (gx >= 110 && gx <= 195 && gy <= 515) continue;

            double gh = grassHeight[i];
            double gb = grassBend[i] + wind * (gy / 600.0);

            Path2D.Double blade = new Path2D.Double();
            blade.moveTo(gx - 2.5, gy);
            blade.curveTo(gx - 1, gy - gh * 0.5, gx + gb * 0.6, gy - gh * 0.8, gx + gb, gy - gh);
            blade.curveTo(gx + gb * 0.4, gy - gh * 0.7, gx + 2, gy - gh * 0.4, gx + 2.5, gy);
            blade.closePath();

            int alpha = (int) (180 + 75 * (gy - 490) / 110.0);
            g2d.setColor(new Color(8, 22, 28, Math.min(255, Math.max(0, alpha))));
            fillShapeScanline(g2d, blade);
        }

        for (int i = 0; i < NUM_FLOWERS; i++) {
            if (flowerX[i] >= 115 && flowerX[i] <= 190 && flowerY[i] <= 510) continue;
            drawChamomileFlower(g2d, flowerX[i], flowerY[i], flowerScale[i], flowerRot[i] + wind * 0.02);
        }
    }

    private void drawChamomileFlower(Graphics2D g2d, double x, double y, double scale, double rotation) {
        AffineTransform oldTx = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(rotation);
        g2d.scale(scale, scale);

        int numPetals = 8;
        Color petalColor = new Color(220, 235, 250, 210);
        for (int p = 0; p < numPetals; p++) {
            double angle = p * (2 * Math.PI / numPetals);
            int px = (int) (Math.cos(angle) * 7);
            int py = (int) (Math.sin(angle) * 7);
            fillMidpointEllipse(g2d, px, py, 3, 5, petalColor);
        }

        fillMidpointCircle(g2d, 0, 0, 4, new Color(255, 215, 80, 240));
        fillMidpointCircle(g2d, 0, 0, 2, new Color(255, 240, 150, 255));

        g2d.setTransform(oldTx);
    }

    public void drawVignette(Graphics2D g2d) {
        Point2D center = new Point2D.Float(300.0f, 300.0f);
        float radius = 420.0f;
        float[] dist = {0.0f, 0.65f, 1.0f};
        Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(0, 5, 15, 40),
            new Color(0, 3, 10, 180)
        };
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        g2d.setPaint(p);
        fillRectangle(g2d, 0, 0, 600, 600);
    }

    private static final Color GRASS_GREEN = new Color(55, 95, 48);

    private void drawGrassBlades(Graphics2D g2, double t) {
        g2.setColor(GRASS_GREEN);
        for (int i = 0; i < NUM_BLADES; i++) {
            double bx = bladeX[i], by = bladeY[i], h = bladeH[i];
            // Sway each grass blade from side to side.
            double sway = Math.sin(t * 1.5 + bx) * 3;
            bezierCurve(g2, bx, by, bx + sway * 0.5, by - h * 0.5,
                        bx + sway, by - h * 0.8, bx + sway * 1.2, by - h, 8);
        }
    }

    private void drawStickman(Graphics2D g2, double t) {
        double breathe = calculateStickmanBreathe(t);

        int headX = 150, headY = (int) (455 + breathe);
        int headR = 42;
        int bodyT = 2;

        g2.setColor(new Color(0, 4, 10, 110));
        fillShapeScanline(g2, new Ellipse2D.Double(headX - 30, 490, 390, 60));

        g2.setColor(new Color(20, 20, 20));

        int neckX = headX + 34, neckY = (int) (headY + 30 + breathe * 0.5);
        int hipX = 330, hipY = 505;
        bresenhamLine(g2, neckX, neckY, hipX, hipY, bodyT);

        g2.setColor(new Color(20, 20, 20));
        int farElbowX = headX + 72;
        int farElbowY = (int) (headY - 96 + breathe * 0.5);
        int farHandX = headX - 26;
        int farHandY = (int) (headY - 22 + breathe * 0.5);
        bresenhamLine(g2, neckX, neckY - 14, farElbowX, farElbowY, bodyT);
        bresenhamLine(g2, farElbowX, farElbowY, farHandX, farHandY, bodyT);

        g2.setColor(new Color(20, 20, 20));
        int nearElbowX = headX + 66;
        int nearElbowY = (int) (headY + 92 + breathe * 0.5);
        int nearHandX = headX - 26;
        int nearHandY = (int) (headY + 22 + breathe * 0.5);
        bresenhamLine(g2, neckX, neckY + 6, nearElbowX, nearElbowY, bodyT);
        bresenhamLine(g2, nearElbowX, nearElbowY, nearHandX, nearHandY, bodyT);

        int knee1X = 420, knee1Y = 512;
        int foot1X = 500, foot1Y = 500;
        bresenhamLine(g2, hipX, hipY, knee1X, knee1Y, bodyT);
        bresenhamLine(g2, knee1X, knee1Y, foot1X, foot1Y, bodyT);

        int knee2X = 400, knee2Y = 445;
        int foot2X = 448, foot2Y = 498;
        bresenhamLine(g2, hipX, hipY, knee2X, knee2Y, bodyT);
        bresenhamLine(g2, knee2X, knee2Y, foot2X, foot2Y, bodyT);

        fillEllipse(g2, foot1X - 12, foot1Y - 7, 26, 13);
        fillEllipse(g2, foot2X - 10, foot2Y - 7, 24, 13);

        AffineTransform noTilt = g2.getTransform();
        AffineTransform tilt = new AffineTransform(noTilt);
        tilt.rotate(Math.toRadians(-60), headX, headY);

        g2.setTransform(tilt);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(new Color(20, 20, 20));
        midpointCircle(g2, headX, headY, headR);
        g2.setTransform(noTilt);

        g2.setTransform(tilt);

        int hairT = 1;
        double[] hairAngles = {68, 84, 100, 116};
        double[] hairLen =    {12, 17, 17, 12};
        for (int i = 0; i < hairAngles.length; i++) {
            double rad = Math.toRadians(hairAngles[i]);
            int sx = (int) (headX + headR * 0.9 * Math.cos(rad));
            int sy = (int) (headY - headR * 0.9 * Math.sin(rad));
            int ex = (int) (headX + (headR + hairLen[i]) * Math.cos(rad) - 6);
            int ey = (int) (headY - (headR + hairLen[i]) * Math.sin(rad));
            bresenhamLine(g2, sx, sy, ex, ey, hairT);
        }

        int eyeY = headY - 8;
        int eyeLX = headX - 14, eyeRX = headX + 6;
        int h = 8;

        fillMidpointEllipse(g2, eyeLX, eyeY + 6, 7, 2, new Color(42, 48, 62, 45));
        fillMidpointEllipse(g2, eyeRX, eyeY + 6, 7, 2, new Color(42, 48, 62, 45));

        g2.setColor(new Color(48, 45, 58, 120));
        bezierCurve(g2, eyeLX - 6, eyeY + 5, eyeLX - 2, eyeY + 7, eyeLX + 2, eyeY + 7, eyeLX + 5, eyeY + 5);
        bezierCurve(g2, eyeRX - 5, eyeY + 5, eyeRX - 2, eyeY + 7, eyeRX + 2, eyeY + 7, eyeRX + 6, eyeY + 5);

        g2.setColor(new Color(60, 58, 72, 85));
        bezierCurve(g2, eyeLX - 5, eyeY - 6, eyeLX - 1, eyeY - 8, eyeLX + 2, eyeY - 8, eyeLX + 5, eyeY - 6);
        bezierCurve(g2, eyeRX - 5, eyeY - 6, eyeRX, eyeY - 8, eyeRX + 3, eyeY - 8, eyeRX + 6, eyeY - 6);

        boolean isScene8 = (t >= POV_WAKE_START && t <= CYCLE);
        if (isScene8) {
            double cryIntensity = Math.min(1.0, (t - POV_WAKE_START) / 0.8);
            int flushAlpha = (int) (45 * cryIntensity);
            fillMidpointEllipse(g2, eyeLX, eyeY + 3, 7, 4, new Color(230, 95, 105, flushAlpha));
            fillMidpointEllipse(g2, eyeRX, eyeY + 3, 7, 4, new Color(230, 95, 105, flushAlpha));
            fillMidpointEllipse(g2, headX + 1, headY + 3, 4, 3, new Color(230, 95, 105, (int) (40 * cryIntensity)));
            fillMidpointEllipse(g2, headX - 14, headY + 7, 6, 3, new Color(230, 95, 105, (int) (30 * cryIntensity)));
            fillMidpointEllipse(g2, headX + 14, headY + 7, 6, 3, new Color(230, 95, 105, (int) (30 * cryIntensity)));
        }

        g2.setColor(new Color(20, 20, 20));
        if (!isScene8) {
    // Scene 1: Tired, heavy, half-lidded eyes (อ่อนล้า อ่อนเพลีย)
            fillEllipse(g2, eyeLX - 4, eyeY - h / 2 + 1, 8, h - 1);
            fillEllipse(g2, eyeRX - 4, eyeY - h / 2 + 1, 8, h - 1);
            g2.setColor(new Color(20, 20, 20));
            bezierCurve(g2, eyeLX - 5, eyeY - 2, eyeLX, eyeY - 1, eyeLX + 3, eyeY - 1, eyeLX + 5, eyeY - 2);
            bezierCurve(g2, eyeRX - 5, eyeY - 2, eyeRX, eyeY - 1, eyeRX + 3, eyeY - 1, eyeRX + 5, eyeY - 2);
            fillMidpointCircle(g2, eyeLX - 1, eyeY - 1, 1, new Color(255, 255, 255, 160));
            fillMidpointCircle(g2, eyeRX - 1, eyeY - 1, 1, new Color(255, 255, 255, 160));
        } else {
    // Scene 8: Watery eyes filled with pooled tears (ตาฉ่ำวาวไปด้วยน้ำตา)
            fillEllipse(g2, eyeLX - 4, eyeY - h / 2, 8, h);
            fillEllipse(g2, eyeRX - 4, eyeY - h / 2, 8, h);

            fillMidpointEllipse(g2, eyeLX, eyeY + 2, 3, 2, new Color(195, 230, 255, 220));
            fillMidpointEllipse(g2, eyeRX, eyeY + 2, 3, 2, new Color(195, 230, 255, 220));
            fillMidpointCircle(g2, eyeLX - 1, eyeY - 2, 1, Color.WHITE);
            fillMidpointCircle(g2, eyeLX + 1, eyeY + 2, 1, new Color(255, 255, 255, 240));
            fillMidpointCircle(g2, eyeRX - 1, eyeY - 2, 1, Color.WHITE);
            fillMidpointCircle(g2, eyeRX + 1, eyeY + 2, 1, new Color(255, 255, 255, 240));
        }

        g2.setColor(new Color(20, 20, 20));
        if (!isScene8) {
    // Scene 1: Tired, flat/exhausted eyebrows
            bezierCurve(g2, eyeLX - 8, eyeY - 12, eyeLX - 4, eyeY - 15, eyeLX + 3, eyeY - 15, eyeLX + 7, eyeY - 13);
            bezierCurve(g2, eyeRX - 7, eyeY - 13, eyeRX - 3, eyeY - 15, eyeRX + 4, eyeY - 15, eyeRX + 8, eyeY - 12);
        } else {
    // Scene 8: Troubled, nostalgic, deeply moved eyebrows (คิ้วขมวดตกอย่างเศร้าสร้อยและซาบซึ้ง)
            bezierCurve(g2, eyeLX - 8, eyeY - 11, eyeLX - 4, eyeY - 14, eyeLX + 2, eyeY - 18, eyeLX + 7, eyeY - 16);
            bezierCurve(g2, eyeRX - 7, eyeY - 16, eyeRX - 2, eyeY - 18, eyeRX + 4, eyeY - 14, eyeRX + 8, eyeY - 11);
        }

    // Scene 1: Gentle tired faint sigh/smile of an exhausted adult
    // Scene 8: Trembling, poignant bittersweet mouth holding back a sob (ริมฝีปากสั่นเครือด้วยความสะเทือนใจ)
        g2.setColor(new Color(20, 20, 20));
        if (!isScene8) {
            bezierCurve(g2, headX - 12, headY + 13, headX - 6, headY + 21, headX + 6, headY + 21, headX + 12, headY + 13);
        } else {
            double tremble = (t >= 41.3 && t <= 43.5) ? Math.sin(t * 26.0) * (0.8 + 0.3 * Math.sin(t * 7.0)) : 0.0;
            int mx1 = headX - 14, my1 = (int) (headY + 14 + tremble * 0.5);
            int mc1x = headX - 7, mc1y = (int) (headY + 24 + tremble);
            int mc2x = headX + 7, mc2y = (int) (headY + 24 - tremble);
            int mx2 = headX + 14, my2 = (int) (headY + 14 - tremble * 0.5);
            bezierCurve(g2, mx1, my1, mc1x, mc1y, mc2x, mc2y, mx2, my2);

            g2.setColor(new Color(40, 30, 45, 100));
            bezierCurve(g2, headX - 5, (int)(headY + 26 + tremble), headX, (int)(headY + 28 + tremble), headX + 2, (int)(headY + 28 + tremble), headX + 5, (int)(headY + 26 + tremble));
        }

        if (t >= TEAR_START && t <= CYCLE) {
            drawEmotionalTears(g2, headX, headY, eyeLX, eyeRX, eyeY, t);
        }

        g2.setTransform(noTilt);
    }

    private void drawEmotionalTears(Graphics2D g2, int headX, int headY, int eyeLX, int eyeRX, int eyeY, double t) {
        double progress = (t < TEAR_END) ? smoothStep((t - TEAR_START) / (TEAR_END - TEAR_START)) : 1.0;
        double fade = (t <= TEAR_END) ? 1.0 : Math.max(0.0, (CYCLE - t) / (CYCLE - TEAR_END));

        int alphaTrail = (int) (170 * fade);
        int alphaDrop = (int) (245 * fade);
        if (alphaDrop <= 0) return;

        double rStartX = eyeRX + 2;
        double rStartY = eyeY + 4;
        double rCp1X = headX + 10, rCp1Y = headY + 3;
        double rCp2X = headX + 17, rCp2Y = headY + 14;
        double rEndX = headX + 24, rEndY = headY + 26;

        double u = progress;
        double u1 = 1.0 - u;
        double curRX = u1*u1*u1 * rStartX + 3*u1*u1*u * rCp1X + 3*u1*u*u * rCp2X + u*u*u * rEndX;
        double curRY = u1*u1*u1 * rStartY + 3*u1*u1*u * rCp1Y + 3*u1*u*u * rCp2Y + u*u*u * rEndY;

        if (progress > 0.03) {
            g2.setColor(new Color(180, 225, 255, alphaTrail));
            double midCp1X = (rStartX + rCp1X) / 2.0, midCp1Y = (rStartY + rCp1Y) / 2.0;
            double midCp2X = (rCp1X + rCp2X) / 2.0,   midCp2Y = (rCp1Y + rCp2Y) / 2.0;
            bezierCurve(g2, rStartX, rStartY, midCp1X, midCp1Y, midCp2X, midCp2Y, curRX, curRY);

            g2.setColor(new Color(255, 255, 255, (int) (alphaTrail * 0.7)));
            bezierCurve(g2, rStartX - 0.5, rStartY, midCp1X - 0.5, midCp1Y, midCp2X - 0.5, midCp2Y, curRX - 0.5, curRY);
        }

        int rix = (int) Math.round(curRX);
        int riy = (int) Math.round(curRY);

        fillMidpointCircle(g2, rix, riy, 5, new Color(150, 210, 255, alphaDrop / 4));
        fillMidpointEllipse(g2, rix, riy, 3, 4, new Color(200, 235, 255, alphaDrop));
        fillMidpointCircle(g2, rix - 1, riy - 1, 1, Color.WHITE);

        double leftProgress = Math.max(0.0, (t - (TEAR_START + 0.2)) / (TEAR_END - (TEAR_START + 0.2)));
        if (leftProgress > 0.0) {
            double lu = smoothStep(leftProgress);
            double lu1 = 1.0 - lu;
            double lStartX = eyeLX - 2;
            double lStartY = eyeY + 4;
            double lCp1X = headX - 18, lCp1Y = headY + 4;
            double lCp2X = headX - 22, lCp2Y = headY + 16;
            double lEndX = headX - 25, lEndY = headY + 25;

            double curLX = lu1*lu1*lu1 * lStartX + 3*lu1*lu1*lu * lCp1X + 3*lu1*lu*lu * lCp2X + lu*lu*lu * lEndX;
            double curLY = lu1*lu1*lu1 * lStartY + 3*lu1*lu1*lu * lCp1Y + 3*lu1*lu*lu * lCp2Y + lu*lu*lu * lEndY;

            g2.setColor(new Color(180, 225, 255, (int) (alphaTrail * 0.85)));
            bezierCurve(g2, lStartX, lStartY, (lStartX + lCp1X) / 2.0, (lStartY + lCp1Y) / 2.0,
                        (lCp1X + lCp2X) / 2.0, (lCp1Y + lCp2Y) / 2.0, curLX, curLY);

            int lix = (int) Math.round(curLX);
            int liy = (int) Math.round(curLY);
            fillMidpointCircle(g2, lix, liy, 4, new Color(150, 210, 255, alphaDrop / 5));
            fillMidpointEllipse(g2, lix, liy, 2, 3, new Color(200, 235, 255, (int) (alphaDrop * 0.9)));
            fillMidpointCircle(g2, lix - 1, liy - 1, 1, Color.WHITE);
        }

        if (t >= TEAR_START + 0.8 && t <= CYCLE) {
            double fallT = (t - (TEAR_START + 0.8)) / 1.4;
            if (fallT >= 0.0 && fallT <= 1.0) {
                double fallY = rEndY + fallT * 32.0 + 0.5 * 9.8 * fallT * fallT * 12.0;
                double fallX = rEndX + fallT * 4.0;
                int fallAlpha = (int) (220 * (1.0 - fallT) * fade);

                if (fallAlpha > 10) {
                    fillMidpointCircle(g2, (int) fallX, (int) fallY, 3, new Color(160, 220, 255, fallAlpha / 3));
                    fillMidpointEllipse(g2, (int) fallX, (int) fallY, 2, 3, new Color(210, 240, 255, fallAlpha));
                    fillMidpointCircle(g2, (int) fallX - 1, (int) fallY - 1, 1, new Color(255, 255, 255, fallAlpha));
                }
            }
        }

        if (t >= TEAR_START + 0.3 && t <= TEAR_END + 0.5) {
            double sparkle = Math.sin((t - (TEAR_START + 0.3)) / (TEAR_END + 0.5 - (TEAR_START + 0.3)) * Math.PI * 2);
            if (Math.abs(sparkle) > 0.2) {
                int sparkleAlpha = (int) (200 * Math.abs(sparkle) * fade);
                g2.setColor(new Color(255, 255, 255, sparkleAlpha));
                drawRasterLine(g2, rix - 4, riy, rix + 4, riy);
                drawRasterLine(g2, rix, riy - 4, rix, riy + 4);
            }
        }
    }

    private BufferedImage memoryBackdrop;

    private BufferedImage buildMemoryBackdrop() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = img.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint sky = new GradientPaint(0, 0, new Color(250, 200, 120),
                                              0, 400, new Color(255, 236, 190));
        bg.setPaint(sky);
        fillRectangle(bg, 0, 0, 600, 400);

        RadialGradientPaint halo = new RadialGradientPaint(
            new Point(470, 130), 130f,
            new float[]{0f, 0.45f, 1f},
            new Color[]{new Color(255, 245, 200, 200),
                        new Color(255, 225, 150, 90),
                        new Color(255, 220, 140, 0)});
        bg.setPaint(halo);
        fillEllipse(bg, 340, 0, 260, 260);
        bg.setColor(new Color(255, 250, 225));
        fillEllipse(bg, 470 - 34, 130 - 34, 68, 68);

        bg.setColor(new Color(150, 175, 110));
        fillEllipse(bg, -140, 336, 470, 160);
        fillEllipse(bg, 130, 352, 330, 150);
        fillEllipse(bg, 300, 342, 460, 160);

        GradientPaint field = new GradientPaint(0, 395, new Color(126, 176, 88),
                                                0, 600, new Color(86, 138, 62));
        bg.setPaint(field);
        fillRectangle(bg, 0, 395, 600, 205);

        bg.dispose();
        return img;
    }

    private void drawPlayer(Graphics2D g2, int x, int groundY, double kick, boolean faceRight, double bob) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;

        int headR = 16;
        int headY = (int) (groundY - 128 + bob);
        int shoulderY = (int) (groundY - 106 + bob);
        int hipY = (int) (groundY - 58 + bob);

        g2.setColor(new Color(25, 25, 25));

        bresenhamLine(g2, x, shoulderY, x, hipY, t2);

        // Swing both arms opposite the kicking leg for balance.
        double armSwing = kick * 0.9;
        int handAY = (int) (shoulderY + 40 - armSwing * 34);
        int handBY = (int) (shoulderY + 36 + armSwing * 12);
        bresenhamLine(g2, x, shoulderY + 4, x - dir * (int) (30 + armSwing * 16), handAY, t2);
        bresenhamLine(g2, x, shoulderY + 4, x + dir * (int) (24 + armSwing * 10), handBY, t2);

        int plantFootX = x - dir * 12;
        bresenhamLine(g2, x, hipY, plantFootX, groundY, t2);
        fillEllipse(g2, plantFootX - dir * 8 - 4, groundY - 4, 16, 8);

        int kneeX = x + dir * (int) (14 + kick * 26);
        int kneeY = (int) (hipY + 30 - kick * 22);
        int footX = x + dir * (int) (24 + kick * 54);
        int footY = (int) (groundY - kick * 40);
        bresenhamLine(g2, x, hipY, kneeX, kneeY, t2);
        bresenhamLine(g2, kneeX, kneeY, footX, footY, t2);
        fillEllipse(g2, footX - 6, footY - 4, 16, 8);

        g2.setColor(Color.WHITE);
        fillEllipse(g2, x - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(new Color(25, 25, 25));
        midpointCircle(g2, x, headY, headR);
        bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);

        int ex = x + dir * 3;
        fillEllipse(g2, ex - dir * 6 - 2, headY - 6, 4, 5);
        fillEllipse(g2, ex + dir * 2 - 2, headY - 6, 4, 5);
        bezierCurve(g2, ex - 7, headY + 4, ex - 3, headY + 10, ex + 3, headY + 10, ex + 7, headY + 4);
    }

    private void drawBall(Graphics2D g2, int cx, int cy, int r, double spin) {
        g2.setColor(Color.WHITE);
        fillEllipse(g2, cx - r, cy - r, r * 2, r * 2);
        g2.setColor(new Color(25, 25, 25));
        midpointCircle(g2, cx, cy, r);

        AffineTransform keep = g2.getTransform();
        AffineTransform spun = new AffineTransform(keep);
        spun.rotate(spin, cx, cy);
        g2.setTransform(spun);
        int[] px = new int[5];
        int[] py = new int[5];
        for (int i = 0; i < 5; i++) {
            double a = Math.toRadians(-90 + i * 72);
            px[i] = cx + (int) (r * 0.40 * Math.cos(a));
            py[i] = cy + (int) (r * 0.40 * Math.sin(a));
        }
        fillPolygonScanline(g2, px, py, 5);
        for (int i = 0; i < 5; i++) {
            double a = Math.toRadians(-90 + i * 72);
            int ox = cx + (int) (r * 0.95 * Math.cos(a));
            int oy = cy + (int) (r * 0.95 * Math.sin(a));
            bresenhamLine(g2, px[i], py[i], ox, oy, 1);
        }
        g2.setTransform(keep);
    }

    private static final Color INK = new Color(25, 25, 25);

    static final double RUN_END = 1.45;
    static final double CONTACT = 1.95;
    static final double FLIGHT_END = 3.8;
    static final double HIT_U = 0.26;
    static final double HIT_TIME = CONTACT + HIT_U * (FLIGHT_END - CONTACT);
    static final int KEEPER_X = 420;
    static final int HIT_X = 402, HIT_Y = 392;
    static final int BLAST_DX = 290;

    private void drawRunner(Graphics2D g2, int x, int groundY, double phase) {
        int t2 = 2;
        int headR = 15;
        // Bounce the body slightly on every running step.
        double bounce = Math.abs(Math.sin(phase * Math.PI)) * 6;
        int headY = (int) (groundY - 126 - bounce);
        int shoulderY = (int) (groundY - 104 - bounce);
        int hipY = (int) (groundY - 56 - bounce);

        g2.setColor(INK);
        bresenhamLine(g2, x + 6, shoulderY, x, hipY, t2);

        // Move the arms and legs in opposite directions.
        double swing = Math.sin(phase * Math.PI * 2);
        bresenhamLine(g2, x + 5, shoulderY + 5, x + 5 + (int) (26 * swing), shoulderY + 26 - (int) (14 * swing), t2);
        bresenhamLine(g2, x + 5, shoulderY + 5, x + 5 - (int) (26 * swing), shoulderY + 26 + (int) (14 * swing), t2);

        for (int leg = 0; leg < 2; leg++) {
            double s = (leg == 0) ? swing : -swing;
            int kneeX = x + (int) (16 * s);
            int kneeY = hipY + 28 - (int) (12 * Math.max(0, s));
            int footX = x + (int) (30 * s);
            int footY = groundY - (int) (26 * Math.max(0, s));
            bresenhamLine(g2, x, hipY, kneeX, kneeY, t2);
            bresenhamLine(g2, kneeX, kneeY, footX, footY, t2);
            fillEllipse(g2, footX - 6, footY - 4, 16, 8);
        }

        g2.setColor(Color.WHITE);
        fillEllipse(g2, x - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, x, headY, headR);
        bresenhamLine(g2, x + 3, headY + headR, x + 6, shoulderY, t2);
        fillEllipse(g2, x + 1, headY - 6, 4, 5);
        fillEllipse(g2, x + 9, headY - 6, 4, 5);
    }

    static final int FOOT_DX = 50, FOOT_DY = -58;

    private void drawBicycleKicker(Graphics2D g2, int cx, int cy, double windup, double tiltDeg) {
        AffineTransform keep = g2.getTransform();
        if (tiltDeg != 0) {
            AffineTransform tx = new AffineTransform(keep);
            tx.rotate(Math.toRadians(tiltDeg), cx, cy);
            g2.setTransform(tx);
        }

        int t2 = 2;
        int headR = 18;
        double w = Math.max(0, Math.min(1, windup));

        int shX = cx - (int) (30 * w);
        int shY = cy + (int) (16 * w) - (int) (34 * (1 - w));
        int hdX = cx - (int) (52 * w);
        int hdY = cy + (int) (30 * w) - (int) (56 * (1 - w));

        g2.setColor(INK);
        bresenhamLine(g2, shX, shY, cx, cy, t2);

        bresenhamLine(g2, shX, shY, shX - 26, shY - 26, t2);
        bresenhamLine(g2, shX, shY, shX - 16, shY + 40, t2);

        int kneeX = cx + (int) (22 * w);
        int kneeY = cy - (int) (26 * w) + (int) (28 * (1 - w));
        int footX = cx + (int) (FOOT_DX * w);
        int footY = cy + (int) (FOOT_DY * w) + (int) (56 * (1 - w));
        bresenhamLine(g2, cx, cy, kneeX, kneeY, t2);
        bresenhamLine(g2, kneeX, kneeY, footX, footY, t2);
        fillEllipse(g2, footX - 8, footY - 5, 18, 9);

        int k2x = cx + (int) (10 * w);
        int k2y = cy + 30;
        int f2x = cx + (int) (42 * w);
        int f2y = cy + 46;
        bresenhamLine(g2, cx, cy, k2x, k2y, t2);
        bresenhamLine(g2, k2x, k2y, f2x, f2y, t2);
        fillEllipse(g2, f2x - 8, f2y - 4, 18, 9);

        bresenhamLine(g2, hdX, hdY, shX, shY, t2);

        g2.setColor(Color.WHITE);
        fillEllipse(g2, hdX - headR, hdY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, hdX, hdY, headR);

        fillEllipse(g2, hdX - 8, hdY - 5, 4, 4);
        fillEllipse(g2, hdX + 4, hdY - 5, 4, 4);
        fillEllipse(g2, hdX - 4, hdY + 4, 8, 6);

        g2.setTransform(keep);
    }

    private void drawDiver(Graphics2D g2, int cx, int cy, double rotDeg, double reach) {
        AffineTransform keep = g2.getTransform();
        AffineTransform tx = new AffineTransform(keep);
        tx.rotate(Math.toRadians(rotDeg), cx, cy);
        g2.setTransform(tx);

        int t2 = 2;
        int headR = 16;
        g2.setColor(INK);
        bresenhamLine(g2, cx + 20, cy, cx - 40, cy + 6, t2);
        int r = (int) (reach * 20);
        bresenhamLine(g2, cx + 14, cy + 2, cx + 52 + r, cy - 20 - r, t2);
        bresenhamLine(g2, cx + 14, cy + 6, cx + 48 + r, cy + 14, t2);
        bresenhamLine(g2, cx - 40, cy + 6, cx - 70, cy - 12, t2);
        bresenhamLine(g2, cx - 70, cy - 12, cx - 96, cy + 4, t2);
        fillEllipse(g2, cx - 106, cy, 18, 9);
        bresenhamLine(g2, cx - 40, cy + 6, cx - 68, cy + 26, t2);
        bresenhamLine(g2, cx - 68, cy + 26, cx - 94, cy + 22, t2);
        fillEllipse(g2, cx - 104, cy + 18, 18, 9);

        g2.setColor(Color.WHITE);
        fillEllipse(g2, cx + 20 - headR, cy - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, cx + 20, cy, headR);
        fillEllipse(g2, cx + 18, cy - 6, 4, 4);
        fillEllipse(g2, cx + 27, cy - 6, 4, 4);

        g2.setTransform(keep);
    }

    private void drawSiuCelebrate(Graphics2D g2, int startX, int groundY, double ct) {
        int t2 = 2;
        int headR = 16;

        if (ct < 0.45) {
            // Jump upward while raising both arms.
            double u = ct / 0.45;
            int x = (int) (startX + 40 * u);
            double jumpH = Math.sin(u * Math.PI) * 75;
            int currentGroundY = (int) (groundY - jumpH);

            int hipY = currentGroundY - 56;
            int shoulderY = currentGroundY - 104;
            int headY = currentGroundY - 128;

            g2.setColor(INK);
            bresenhamLine(g2, x, shoulderY, x, hipY, t2);

            double armAngle = u * Math.PI;
            int leftHandX = x - (int) (25 * Math.cos(armAngle));
            int leftHandY = shoulderY - (int) (30 * Math.sin(armAngle));
            int rightHandX = x + (int) (25 * Math.cos(armAngle));
            int rightHandY = shoulderY - (int) (30 * Math.sin(armAngle));
            bresenhamLine(g2, x, shoulderY + 4, leftHandX, leftHandY, t2);
            bresenhamLine(g2, x, shoulderY + 4, rightHandX, rightHandY, t2);

            int k1x = x - 14, k1y = hipY + 22;
            int f1x = x - 20, f1y = hipY + 42;
            int k2x = x + 14, k2y = hipY + 20;
            int f2x = x + 22, f2y = hipY + 40;
            bresenhamLine(g2, x, hipY, k1x, k1y, t2);
            bresenhamLine(g2, k1x, k1y, f1x, f1y, t2);
            bresenhamLine(g2, x, hipY, k2x, k2y, t2);
            bresenhamLine(g2, k2x, k2y, f2x, f2y, t2);
            fillEllipse(g2, f1x - 6, f1y - 4, 14, 8);
            fillEllipse(g2, f2x - 6, f2y - 4, 14, 8);

            bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
            g2.setColor(Color.WHITE);
            fillEllipse(g2, x - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, x, headY, headR);

            fillEllipse(g2, x - 6, headY - 5, 4, 4);
            fillEllipse(g2, x + 2, headY - 5, 4, 4);
            fillMidpointEllipse(g2, x, headY + 5, 3, 4, INK);

        } else {
            // Land with both feet apart and arms pushed backward.
            double landTime = ct - 0.45;
            int x = startX + 40;

            if (landTime < 0.25) {
                int shake = (int) ((1.0 - landTime / 0.25) * 6 * Math.sin(landTime * 80));
                x += shake;
            }

            int hipY = groundY - 52;
            int shoulderY = groundY - 100;
            int headY = groundY - 124;

            if (landTime < 0.40) {
                double pu = landTime / 0.40;
                int ringR = (int) (10 + pu * 70);
                int ringAlpha = (int) (220 * (1.0 - pu));
                g2.setColor(new Color(255, 255, 255, ringAlpha));
                midpointCircle(g2, x, groundY - 2, ringR);
                fillMidpointEllipse(g2, x - 35 - (int)(pu * 25), groundY - 4, 18, 6, new Color(160, 200, 140, ringAlpha));
                fillMidpointEllipse(g2, x + 35 + (int)(pu * 25), groundY - 4, 18, 6, new Color(160, 200, 140, ringAlpha));
            }

            g2.setColor(new Color(0, 0, 0, 130));
            fillMidpointEllipse(g2, x, groundY - 2, 45, 8, new Color(0, 0, 0, 130));

            g2.setColor(INK);

            bresenhamLine(g2, x, shoulderY, x, hipY, t2 + 1);

            int leftArmElbowX = x - 26, leftArmElbowY = shoulderY + 22;
            int leftHandX = x - 42, leftHandY = shoulderY + 52;
            int rightArmElbowX = x + 26, rightArmElbowY = shoulderY + 22;
            int rightHandX = x + 42, rightHandY = shoulderY + 52;

            bresenhamLine(g2, x, shoulderY + 4, leftArmElbowX, leftArmElbowY, t2);
            bresenhamLine(g2, leftArmElbowX, leftArmElbowY, leftHandX, leftHandY, t2);
            bresenhamLine(g2, x, shoulderY + 4, rightArmElbowX, rightArmElbowY, t2);
            bresenhamLine(g2, rightArmElbowX, rightArmElbowY, rightHandX, rightHandY, t2);

            fillMidpointCircle(g2, leftHandX, leftHandY, 4, INK);
            fillMidpointCircle(g2, rightHandX, rightHandY, 4, INK);

            int knee1X = x - 22, knee1Y = groundY - 24;
            int foot1X = x - 34, foot1Y = groundY - 4;
            int knee2X = x + 22, knee2Y = groundY - 24;
            int foot2X = x + 34, foot2Y = groundY - 4;

            bresenhamLine(g2, x, hipY, knee1X, knee1Y, t2);
            bresenhamLine(g2, knee1X, knee1Y, foot1X, foot1Y, t2);
            bresenhamLine(g2, x, hipY, knee2X, knee2Y, t2);
            bresenhamLine(g2, knee2X, knee2Y, foot2X, foot2Y, t2);

            fillEllipse(g2, foot1X - 10, foot1Y - 4, 18, 9);
            fillEllipse(g2, foot2X - 8, foot2Y - 4, 18, 9);

            bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
            g2.setColor(Color.WHITE);
            fillEllipse(g2, x - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, x, headY, headR);

            fillEllipse(g2, x - 8, headY - 6, 4, 5);
            fillEllipse(g2, x + 4, headY - 6, 4, 5);

            bezierCurve(g2, x - 11, headY - 11, x - 7, headY - 13, x - 3, headY - 12, x, headY - 9);
            bezierCurve(g2, x + 11, headY - 11, x + 7, headY - 13, x + 3, headY - 12, x, headY - 9);

            fillMidpointEllipse(g2, x, headY + 5, 6, 8, INK);
            g2.setColor(new Color(210, 60, 70));
            fillMidpointEllipse(g2, x, headY + 8, 4, 3, new Color(210, 60, 70));

            if (landTime > 0.05 && landTime < 0.65) {
                drawSpeedLines(g2, x, shoulderY + 10, Math.min(1.0, (0.65 - landTime) / 0.4), 14, 3);
            }

            double textPop = Math.min(1.0, landTime / 0.12);
            double bounceScale = (landTime < 0.25) ? 1.0 + 0.35 * Math.sin((landTime / 0.25) * Math.PI) : 1.0;

            AffineTransform oldTxtTx = g2.getTransform();
            int textCenterX = x;
            int textCenterY = headY - 32;

            g2.translate(textCenterX, textCenterY);
            g2.scale(textPop * bounceScale, textPop * bounceScale);

            String siuText = "SIUUU!";
            Font font = new Font("Impact", Font.BOLD, 28);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(siuText);
            int th = fm.getAscent();

            int tx = -tw / 2;
            int ty = th / 2 - 4;

            g2.setColor(new Color(20, 20, 20));
            int[] ox = {-2, 0, 2, -2, 2, -2, 0, 2, -3, 3, 0, 0};
            int[] oy = {-2, -2, -2, 0, 0, 2, 2, 2, 0, 0, -3, 3};
            for (int i = 0; i < ox.length; i++) {
                g2.drawString(siuText, tx + ox[i], ty + oy[i]);
            }

            GradientPaint goldGrad = new GradientPaint(
                0, ty - th, new Color(255, 255, 140),
                0, ty, new Color(255, 195, 20)
            );
            g2.setPaint(goldGrad);
            g2.drawString(siuText, tx, ty);

            if (landTime < 0.6) {
                double spkAlpha = Math.max(0, 1.0 - landTime / 0.6);
                g2.setColor(new Color(255, 240, 100, (int) (240 * spkAlpha)));
                fillEllipse(g2, tx - 12, ty - th / 2, 5, 5);
                fillEllipse(g2, tx + tw + 6, ty - th / 2 - 4, 6, 6);
                fillEllipse(g2, tx + tw / 2 + 10, ty - th - 6, 4, 4);
            }

            g2.setTransform(oldTxtTx);
        }
    }

    private void drawFlungPlayer(Graphics2D g2, int cx, int cy, double spinDeg) {
        AffineTransform keep = g2.getTransform();
        AffineTransform tx = new AffineTransform(keep);
        tx.rotate(Math.toRadians(spinDeg), cx, cy);
        g2.setTransform(tx);

        int t2 = 2;
        int headR = 16;
        g2.setColor(INK);

        bresenhamLine(g2, cx, cy - 4, cx, cy + 32, t2);
        bresenhamLine(g2, cx, cy + 2, cx - 36, cy - 22, t2);
        bresenhamLine(g2, cx, cy + 2, cx + 34, cy - 16, t2);
        bresenhamLine(g2, cx, cy + 32, cx - 26, cy + 56, t2);
        bresenhamLine(g2, cx, cy + 32, cx + 28, cy + 54, t2);
        fillEllipse(g2, cx - 36, cy + 52, 18, 9);
        fillEllipse(g2, cx + 24, cy + 50, 18, 9);

        bresenhamLine(g2, cx, cy - 4, cx, cy - 24, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, cx - headR, cy - 26 - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, cx, cy - 26, headR);

        int ey = cy - 31;
        bresenhamLine(g2, cx - 11, ey - 4, cx - 4, ey + 3, 1);
        bresenhamLine(g2, cx - 4, ey - 4, cx - 11, ey + 3, 1);
        bresenhamLine(g2, cx + 4, ey - 4, cx + 11, ey + 3, 1);
        bresenhamLine(g2, cx + 11, ey - 4, cx + 4, ey + 3, 1);
        fillEllipse(g2, cx - 5, cy - 21, 10, 8);

        g2.setTransform(keep);
    }

    private void drawSpeedLines(Graphics2D g2, int cx, int cy, double strength, int count, int seed) {
        if (strength <= 0) return;
        g2.setColor(new Color(255, 255, 255, (int) (170 * strength)));
        for (int i = 0; i < count; i++) {
            double a = i * (2 * Math.PI / count) + seed * 0.7;
            double inner = 70 + ((i * 37 + seed * 13) % 60);
            double len = 60 + ((i * 53 + seed * 29) % 120) * strength;
            int x1 = cx + (int) (inner * Math.cos(a));
            int y1 = cy + (int) (inner * Math.sin(a));
            int x2 = cx + (int) ((inner + len) * Math.cos(a));
            int y2 = cy + (int) ((inner + len) * Math.sin(a));
            bresenhamLine(g2, x1, y1, x2, y2, (i % 3 == 0) ? 1 : 0);
        }
    }

    private void drawImpactBurst(Graphics2D g2, int cx, int cy, double p) {
        if (p < 0 || p > 1) return;
        int alpha = (int) (255 * (1 - p));

        g2.setColor(new Color(255, 255, 255, alpha));
        int ring = (int) (20 + p * 190);
        midpointCircle(g2, cx, cy, ring);
        midpointCircle(g2, cx, cy, Math.max(2, ring - 8));

        g2.setColor(new Color(255, 240, 190, alpha));
        for (int i = 0; i < 12; i++) {
            double a = i * (Math.PI / 6) + 0.2;
            int inner = (int) (18 + p * 90);
            int outer = inner + (int) (46 * (1 - p) + 16);
            bresenhamLine(g2,
                cx + (int) (inner * Math.cos(a)), cy + (int) (inner * Math.sin(a)),
                cx + (int) (outer * Math.cos(a)), cy + (int) (outer * Math.sin(a)), 1);
        }
    }

    private void drawBallTrail(Graphics2D g2, int bx, int by, int fromX, int fromY, int r, double u) {
        int steps = 9;
        for (int i = steps; i >= 1; i--) {
            double back = u - i * 0.035;
            if (back < 0) continue;
            int tx = (int) (fromX + (bx - fromX) * (back / u));
            int ty = (int) (fromY + (by - fromY) * (back / u));
            float f = (float) i / steps;
            int a = (int) (150 * (1 - f));
            int rr = (int) (r * (1 - f * 0.55));
            g2.setColor(new Color(255, (int) (200 - 90 * f), 90, a));
            fillEllipse(g2, tx - rr, ty - rr, rr * 2, rr * 2);
        }
    }

    private void drawMemoryScene(Graphics2D g2, double ft) {
        final int groundY = 500;
        final int ballR = 17;
        final int hipCX = 250, hipCY = 300;
        final int kickX = hipCX + FOOT_DX, kickY = hipCY + FOOT_DY;

        double shake = 0;
        double s1 = ft - CONTACT;
        if (s1 >= 0 && s1 < 0.4) shake = 1 - s1 / 0.4;
        double s2 = ft - HIT_TIME;
        if (s2 >= 0 && s2 < 0.45) shake = Math.max(shake, 1 - s2 / 0.45);
        int sx = (int) (Math.sin(ft * 90) * 9 * shake);
        int sy = (int) (Math.cos(ft * 78) * 7 * shake);

        AffineTransform steady = g2.getTransform();
        g2.translate(sx, sy);

        if (memoryBackdrop == null) memoryBackdrop = buildMemoryBackdrop();
        g2.drawImage(memoryBackdrop, 0, 0, null);
        drawGrassBlades(g2, ft);

        double keeperBob = Math.sin(ft * 2.4) * 2;

        if (ft < CONTACT) {
            double bu = Math.min(1, ft / CONTACT);
            int ballX = (int) (70 + (kickX - 70) * bu);
            int ballY = (int) (60 + (kickY - 60) * bu);

            drawPlayer(g2, KEEPER_X, groundY, 0, false, keeperBob);

            if (ft < RUN_END) {
                double u = ft / RUN_END;
                drawRunner(g2, (int) (90 + 150 * u), groundY, ft * 3.2);
            } else {
                double lu = (ft - RUN_END) / (CONTACT - RUN_END);
                int cx = (int) (hipCX - 10 * (1 - lu));
                int cy = (int) (groundY - 60 - (groundY - 60 - hipCY) * lu);
                drawSpeedLines(g2, cx, cy, lu * 0.7, 14, 1);
                drawBicycleKicker(g2, cx, cy, lu, 0);
            }
            drawBall(g2, ballX, ballY, ballR, ft * 6);

        } else if (ft < FLIGHT_END) {
            double u = (ft - CONTACT) / (FLIGHT_END - CONTACT);

            int cx = (int) (hipCX + 24 * u);
            int cy = (int) Math.min(groundY - 46, hipCY + 320 * u * u);
            drawSpeedLines(g2, kickX, kickY, Math.max(0, 1 - u * 3), 16, 2);
            drawBicycleKicker(g2, cx, cy, Math.max(0, 1 - u * 1.4), 26 * u);

            if (u < HIT_U) {
                double bu = u / HIT_U;
                int bx = (int) (kickX + (HIT_X - kickX) * bu);
                int by = (int) (kickY + (HIT_Y - kickY) * bu);

                drawPlayer(g2, KEEPER_X, groundY, 0, false, keeperBob);
                drawBallTrail(g2, bx, by, kickX, kickY, ballR, Math.max(bu, 0.001));
                drawBall(g2, bx, by, ballR, ft * 26);
                drawImpactBurst(g2, kickX, kickY, u * 4.5);

            } else {
                double fu = (u - HIT_U) / (1 - HIT_U);
                int bx = (int) (HIT_X + BLAST_DX * fu);
                int by = (int) (HIT_Y - 130 * fu + 60 * fu * fu);

                drawSpeedLines(g2, bx, by, Math.max(0, 1 - fu * 1.6), 14, 5);
                drawFlungPlayer(g2, bx + 34, by + 4, 300 * fu);
                drawBallTrail(g2, bx, by, HIT_X, HIT_Y, ballR, Math.max(fu, 0.001));
                drawBall(g2, bx, by, ballR, ft * 26);
                drawImpactBurst(g2, HIT_X, HIT_Y, fu * 2.6);
            }

        } else {
            drawSiuCelebrate(g2, 240, groundY, ft - FLIGHT_END);
        }

        g2.setTransform(steady);

        double flashT = Math.abs(ft - CONTACT);
        if (flashT < 0.14) {
            int a = (int) (200 * (1 - flashT / 0.14));
            g2.setColor(new Color(255, 255, 255, a));
            fillRectangle(g2, 0, 0, 600, 600);
        }
    }

    // 7. MEMORY SCENE 3: CHILDHOOD FRIENDS PLAYING IN A FOREST STREAM

    private BufferedImage streamBackdrop;

    private BufferedImage buildStreamBackdrop() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = img.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        LinearGradientPaint skyGrad = new LinearGradientPaint(
            new Point2D.Float(100, 0), new Point2D.Float(300, 360),
            new float[]{0.0f, 0.45f, 0.85f, 1.0f},
            new Color[]{
                new Color(225, 248, 185),
                new Color(175, 228, 140),
                new Color(105, 180, 90),
                new Color(60, 135, 68)
            }
        );
        bg.setPaint(skyGrad);
        fillRectangle(bg, 0, 0, 600, 600);

        bg.setColor(new Color(18, 56, 32, 230));
        fillEllipse(bg, -100, -60, 320, 220);
        fillEllipse(bg, 80, -90, 300, 230);
        fillEllipse(bg, 290, -80, 360, 240);
        fillEllipse(bg, 50, 40, 250, 180);
        fillEllipse(bg, 320, 30, 280, 190);

        bg.setColor(new Color(32, 92, 45, 235));
        fillEllipse(bg, -70, 20, 260, 190);
        fillEllipse(bg, 130, 10, 250, 180);
        fillEllipse(bg, 350, 15, 290, 200);
        fillEllipse(bg, -30, 110, 210, 160);
        fillEllipse(bg, 420, 95, 220, 170);

        bg.setColor(new Color(65, 48, 30));
        fillRectangle(bg, 155, 90, 18, 270);
        fillRectangle(bg, 435, 75, 20, 285);
        fillRectangle(bg, 230, 110, 12, 245);
        fillRectangle(bg, 365, 105, 14, 250);

        bg.setColor(new Color(55, 142, 60, 240));
        fillEllipse(bg, -50, -20, 200, 150);
        fillEllipse(bg, 90, 60, 190, 140);
        fillEllipse(bg, 270, 45, 210, 150);
        fillEllipse(bg, 450, 10, 220, 160);

        bg.setColor(new Color(95, 185, 72, 230));
        fillEllipse(bg, -20, 25, 140, 110);
        fillEllipse(bg, 140, 85, 130, 95);
        fillEllipse(bg, 310, 70, 145, 105);
        fillEllipse(bg, 480, 40, 150, 115);

        bg.setColor(new Color(58, 40, 24));
        fillRectangle(bg, 15, 40, 42, 330);
        bg.setColor(new Color(78, 54, 32));
        fillRectangle(bg, 22, 40, 26, 330);
        bg.setColor(new Color(42, 28, 16));
        for (int ly = 60; ly < 360; ly += 24) {
            bresenhamLine(bg, 18, ly, 48, ly + 14, 1);
            bresenhamLine(bg, 25, ly + 10, 52, ly + 22, 0);
        }
        bg.setColor(new Color(58, 40, 24));
        int[] lbx = {35, 140, 145, 35};
        int[] lby = {120, 80, 96, 138};
        fillPolygonScanline(bg, lbx, lby, 4);

        bg.setColor(new Color(54, 36, 22));
        fillRectangle(bg, 535, 25, 46, 345);
        bg.setColor(new Color(75, 52, 30));
        fillRectangle(bg, 542, 25, 30, 345);
        bg.setColor(new Color(38, 25, 14));
        for (int ry = 45; ry < 360; ry += 26) {
            bresenhamLine(bg, 538, ry, 574, ry + 16, 1);
            bresenhamLine(bg, 544, ry + 12, 576, ry + 26, 0);
        }
        int[] rbx = {550, 450, 445, 550};
        int[] rby = {105, 65, 80, 122};
        fillPolygonScanline(bg, rbx, rby, 4);

        bg.setColor(new Color(42, 120, 52, 245));
        fillEllipse(bg, -15, 80, 170, 110);
        fillEllipse(bg, 430, 50, 175, 120);
        bg.setColor(new Color(118, 205, 82, 240));
        fillEllipse(bg, 20, 100, 110, 75);
        fillEllipse(bg, 465, 75, 115, 80);

        LinearGradientPaint bankGrad = new LinearGradientPaint(
            new Point2D.Float(0, 310), new Point2D.Float(0, 380),
            new float[]{0.0f, 0.5f, 1.0f},
            new Color[]{new Color(50, 75, 48), new Color(72, 92, 62), new Color(42, 62, 52)}
        );
        bg.setPaint(bankGrad);
        fillRectangle(bg, 0, 315, 600, 85);

        fillMossyBoulder(bg, -25, 335, 140, 68, new Color(75, 85, 76), new Color(60, 130, 55));
        fillMossyBoulder(bg, 75, 345, 95, 48, new Color(85, 98, 88), new Color(72, 145, 65));
        fillMossyBoulder(bg, 445, 340, 125, 56, new Color(80, 92, 82), new Color(68, 138, 60));
        fillMossyBoulder(bg, 520, 330, 130, 65, new Color(72, 82, 74), new Color(55, 122, 50));
        fillMossyBoulder(bg, 190, 348, 55, 28, new Color(92, 105, 96), new Color(80, 150, 70));
        fillMossyBoulder(bg, 380, 344, 65, 32, new Color(88, 100, 92), new Color(75, 142, 65));

        bg.dispose();
        return img;
    }

    private static void fillMossyBoulder(Graphics2D g2, int x, int y, int w, int h, Color stoneColor, Color mossColor) {
        g2.setColor(new Color(25, 35, 30, 180));
        fillEllipse(g2, x + 2, y + 4, w, h);
        g2.setColor(stoneColor);
        fillEllipse(g2, x, y, w, h);
        g2.setColor(new Color(stoneColor.getRed() - 18, stoneColor.getGreen() - 18, stoneColor.getBlue() - 18));
        fillEllipse(g2, x + 4, y + h / 3, w - 8, h * 2 / 3);
        g2.setColor(mossColor);
        fillEllipse(g2, x + 3, y - 2, w - 6, h / 2 + 2);
        g2.setColor(new Color(Math.min(255, mossColor.getRed() + 45), Math.min(255, mossColor.getGreen() + 50), mossColor.getBlue()));
        fillEllipse(g2, x + w / 5, y, w * 3 / 5, h / 4 + 2);
    }

    private void drawSunbeams(Graphics2D g2, double st) {
        AffineTransform oldTx = g2.getTransform();

        int[][] rays = {
            {-30, -20, 220, 600, 110},
            {80, -20, 390, 600, 140},
            {220, -20, 520, 600, 100},
            {380, -20, 640, 600, 80}
        };

        for (int i = 0; i < rays.length; i++) {
            int x1 = rays[i][0];
            int y1 = rays[i][1];
            int x2 = rays[i][2];
            int y2 = rays[i][3];
            int w = rays[i][4];

            double pulse = Math.sin(st * 1.8 + i * 1.2) * 0.15 + 0.85;
            int alphaTop = (int) (55 * pulse);
            int alphaBottom = (int) (15 * pulse);

            Path2D.Double beam = new Path2D.Double();
            beam.moveTo(x1, y1);
            beam.lineTo(x1 + w, y1);
            beam.lineTo(x2 + w * 1.8, y2);
            beam.lineTo(x2, y2);
            beam.closePath();

            LinearGradientPaint beamGrad = new LinearGradientPaint(
                new Point2D.Float(x1, y1), new Point2D.Float(x2, y2),
                new float[]{0.0f, 0.6f, 1.0f},
                new Color[]{
                    new Color(255, 252, 200, alphaTop),
                    new Color(245, 240, 160, (alphaTop + alphaBottom) / 2),
                    new Color(220, 245, 140, alphaBottom)
                }
            );
            g2.setPaint(beamGrad);
            fillShapeScanline(g2, beam);
        }

        Random sparkRand = new Random(404);
        for (int i = 0; i < 28; i++) {
            double baseX = sparkRand.nextDouble() * 580 + 10;
            double baseY = sparkRand.nextDouble() * 320 + 30;
            double driftX = Math.sin(st * 1.4 + i * 0.7) * 16;
            double driftY = Math.cos(st * 1.1 + i * 0.9) * 12;
            int x = (int) (baseX + driftX);
            int y = (int) (baseY + driftY);
            int r = 1 + sparkRand.nextInt(3);
            int spkAlpha = (int) (90 + 90 * Math.sin(st * 3.0 + i * 1.5));
            fillMidpointCircle(g2, x, y, r, new Color(255, 255, 210, Math.max(20, Math.min(230, spkAlpha))));
        }

        g2.setTransform(oldTx);
    }

    private void drawRiverbed(Graphics2D g2, int waterY) {
        LinearGradientPaint waterBed = new LinearGradientPaint(
            new Point2D.Float(300, waterY), new Point2D.Float(300, 600),
            new float[]{0.0f, 0.35f, 0.75f, 1.0f},
            new Color[]{
                new Color(45, 130, 145),
                new Color(32, 110, 130),
                new Color(24, 88, 112),
                new Color(18, 65, 90)
            }
        );
        g2.setPaint(waterBed);
        fillRectangle(g2, 0, waterY, 600, 600 - waterY);

        Random pebRand = new Random(888);
        for (int i = 0; i < 45; i++) {
            int px = pebRand.nextInt(580) + 10;
            int py = waterY + 15 + pebRand.nextInt(215);
            int rx = 4 + pebRand.nextInt(9);
            int ry = 3 + pebRand.nextInt(6);
            int gray = 50 + pebRand.nextInt(40);
            int alpha = 130 + pebRand.nextInt(60);
            fillMidpointEllipse(g2, px, py, rx, ry, new Color(gray, gray + 15, gray + 25, alpha));
            fillMidpointEllipse(g2, px - 1, py - 1, Math.max(1, rx - 3), Math.max(1, ry - 2),
                    new Color(gray + 40, gray + 55, gray + 65, alpha / 2));
        }
    }

    private void drawWaterSurface(Graphics2D g2, int waterY, double st) {
        LinearGradientPaint waterLayer = new LinearGradientPaint(
            new Point2D.Float(300, waterY), new Point2D.Float(300, 600),
            new float[]{0.0f, 0.35f, 0.8f, 1.0f},
            new Color[]{
                new Color(90, 205, 230, 130),
                new Color(45, 165, 195, 160),
                new Color(28, 130, 165, 195),
                new Color(18, 85, 120, 215)
            }
        );
        g2.setPaint(waterLayer);
        fillRectangle(g2, 0, waterY, 600, 600 - waterY);

        for (int i = 0; i < 16; i++) {
            double cx = 30 + (i * 73) % 540;
            double cy = waterY + 22 + (i * 37) % 190;
            double wave = Math.sin(st * 2.8 + i * 0.9) * 5.0;
            int alpha = (int) (65 + 35 * Math.sin(st * 2.2 + i));

            g2.setColor(new Color(220, 252, 255, alpha));
            bezierCurve(g2,
                cx - 24, cy + wave,
                cx - 8, cy - 6 - wave,
                cx + 8, cy + 6 + wave,
                cx + 24, cy - wave, 2);
        }

        int[] friendX = {130, 270, 370, 480};
        int[] friendY = {waterY + 10, waterY + 6, waterY + 14, waterY + 4};
        for (int f = 0; f < friendX.length; f++) {
            int cx = friendX[f];
            int cy = friendY[f];
            for (int r = 0; r < 3; r++) {
                double prog = ((st * 1.6 + r * 0.33 + f * 0.25) % 1.0);
                int rx = (int) (10 + prog * 38);
                int ry = (int) (4 + prog * 13);
                int alpha = (int) (175 * (1.0 - prog));
                g2.setColor(new Color(215, 248, 255, alpha));
                fillMidpointEllipse(g2, cx, cy, rx, ry, new Color(215, 248, 255, alpha));
            }
        }
    }

    private void drawSplashingFriend1(Graphics2D g2, int x, int waterY, double st) {
        int t2 = 2;
        int headR = 15;

        // Scoop the water by moving both hands forward and backward.
        double splashCycle = (st * 4.2) % (Math.PI * 2);
        double scoop = Math.sin(splashCycle);
        double bob = Math.abs(Math.sin(st * 4.2)) * 3.0;

        int hipY = (int) (waterY - 6 + bob);
        int shoulderY = hipY - 32;
        int headY = shoulderY - 22;

        int torsoShX = x + 14;
        int torsoHipX = x - 6;

        g2.setColor(INK);
        int knee1X = torsoHipX + 12, knee1Y = waterY + 12;
        int foot1X = torsoHipX + 22, foot1Y = waterY + 24;
        int knee2X = torsoHipX - 8, knee2Y = waterY + 13;
        int foot2X = torsoHipX - 16, foot2Y = waterY + 26;
        bresenhamLine(g2, torsoHipX, hipY, knee1X, knee1Y, t2);
        bresenhamLine(g2, knee1X, knee1Y, foot1X, foot1Y, t2);
        bresenhamLine(g2, torsoHipX, hipY, knee2X, knee2Y, t2);
        bresenhamLine(g2, knee2X, knee2Y, foot2X, foot2Y, t2);
        fillEllipse(g2, foot1X - 5, foot1Y - 3, 11, 6);
        fillEllipse(g2, foot2X - 5, foot2Y - 3, 11, 6);

        g2.setColor(INK);
        bresenhamLine(g2, torsoShX, shoulderY, torsoHipX, hipY, t2);

        int armThrowX = (int) (torsoShX + 20 + scoop * 14);
        int armThrowY = (int) (shoulderY + 14 - scoop * 20);

        int hand2X = armThrowX - 4;
        int hand2Y = armThrowY + 6;
        bresenhamLine(g2, torsoShX - 4, shoulderY + 2, hand2X, hand2Y, t2);
        fillMidpointCircle(g2, hand2X, hand2Y, 3, INK);

        int hand1X = armThrowX + 6;
        int hand1Y = armThrowY - 2;
        bresenhamLine(g2, torsoShX + 4, shoulderY + 4, hand1X, hand1Y, t2);
        fillMidpointCircle(g2, hand1X, hand1Y, 3, INK);

        int headX = torsoShX + 4;
        bresenhamLine(g2, headX, headY + headR, torsoShX, shoulderY, t2);

        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 4, headY - headR, headX - 8, headY - headR - 6, 1);
        bresenhamLine(g2, headX + 2, headY - headR, headX + 2, headY - headR - 8, 1);
        bresenhamLine(g2, headX + 7, headY - headR + 2, headX + 12, headY - headR - 5, 1);

        int ex = headX + 3;
        fillEllipse(g2, ex + 2, headY - 4, 3, 4);
        bezierCurve(g2, ex - 8, headY - 6, ex - 4, headY - 3, ex - 4, headY - 3, ex - 8, headY);
        bezierCurve(g2, ex - 6, headY + 4, ex - 1, headY + 8, ex + 3, headY + 8, ex + 7, headY + 4);
    }

    private void drawBucketFriend2(Graphics2D g2, int x, int waterY, double st) {
        int t2 = 2;
        int headR = 15;

        // Sway the body while waving both arms above the water.
        double sway = Math.sin(st * 3.5) * 2.5;
        int hipY = (int) (waterY - 14 + sway);
        int shoulderY = hipY - 30;
        int headY = shoulderY - 20;

        g2.setColor(INK);
        bresenhamLine(g2, x, hipY, x - 8, waterY + 12, t2);
        bresenhamLine(g2, x, hipY, x + 9, waterY + 12, t2);

        g2.setColor(INK);
        bresenhamLine(g2, x, shoulderY, x, hipY, t2);

        double armWaveL = Math.sin(st * 6.0) * 4;
        double armWaveR = Math.cos(st * 6.0) * 4;

        int handLX = x - 18, handLY = (int) (shoulderY - 22 + armWaveL);
        int handRX = x + 18, handRY = (int) (shoulderY - 22 + armWaveR);

        bresenhamLine(g2, x, shoulderY + 3, handLX, handLY, t2);
        bresenhamLine(g2, x, shoulderY + 3, handRX, handRY, t2);
        fillMidpointCircle(g2, handLX, handLY, 3, INK);
        fillMidpointCircle(g2, handRX, handRY, 3, INK);

        bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, x - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, x, headY, headR);

        bresenhamLine(g2, x - 3, headY - headR, x - 5, headY - headR - 7, 1);
        bresenhamLine(g2, x + 3, headY - headR, x + 5, headY - headR - 7, 1);

        fillEllipse(g2, x - 5, headY - 4, 3, 4);
        fillEllipse(g2, x + 3, headY - 4, 3, 4);
        bezierCurve(g2, x - 6, headY + 3, x - 2, headY + 8, x + 2, headY + 8, x + 6, headY + 3);
    }

    private void drawLaughingFriend3(Graphics2D g2, int x, int waterY, double st) {
        int t2 = 2;
        int headR = 15;

        // Bounce while laughing and kick both legs alternately.
        double laughBounce = Math.abs(Math.sin(st * 6.5)) * 2.5;
        int hipY = (int) (waterY + 8 - laughBounce);
        int shoulderY = hipY - 28;
        int headY = shoulderY - 20;

        double kick1 = Math.sin(st * 8.0) * 6.0;
        double kick2 = Math.cos(st * 8.0) * 6.0;

        g2.setColor(INK);
        int knee1X = x - 18, knee1Y = (int) (waterY + 14 + kick1);
        int foot1X = x - 30, foot1Y = (int) (waterY + 8 - kick1);
        bresenhamLine(g2, x - 5, hipY, knee1X, knee1Y, t2);
        bresenhamLine(g2, knee1X, knee1Y, foot1X, foot1Y, t2);
        fillEllipse(g2, foot1X - 4, foot1Y - 3, 10, 6);

        int knee2X = x + 16, knee2Y = (int) (waterY + 15 + kick2);
        int foot2X = x + 28, foot2Y = (int) (waterY + 9 - kick2);
        bresenhamLine(g2, x + 5, hipY, knee2X, knee2Y, t2);
        bresenhamLine(g2, knee2X, knee2Y, foot2X, foot2Y, t2);
        fillEllipse(g2, foot2X - 4, foot2Y - 3, 10, 6);

        g2.setColor(INK);
        bresenhamLine(g2, x, shoulderY, x, hipY, t2);

        int armWaveL = (int) (Math.sin(st * 7.0) * 5);
        int armWaveR = (int) (Math.cos(st * 7.0) * 5);

        int handLX = x - 24, handLY = shoulderY - 14 + armWaveL;
        int handRX = x + 24, handRY = shoulderY - 16 + armWaveR;
        bresenhamLine(g2, x - 6, shoulderY + 3, handLX, handLY, t2);
        bresenhamLine(g2, x + 6, shoulderY + 3, handRX, handRY, t2);
        fillMidpointCircle(g2, handLX, handLY, 3, INK);
        fillMidpointCircle(g2, handRX, handRY, 3, INK);

        bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, x - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, x, headY, headR);

        bresenhamLine(g2, x - 4, headY - headR, x - 6, headY - headR - 6, 1);
        bresenhamLine(g2, x + 1, headY - headR, x + 1, headY - headR - 8, 1);
        bresenhamLine(g2, x + 6, headY - headR, x + 8, headY - headR - 6, 1);

        int ey = headY - 2;
        bezierCurve(g2, x - 8, ey, x - 5, ey - 5, x - 2, ey - 5, x, ey);
        bezierCurve(g2, x + 2, ey, x + 5, ey - 5, x + 8, ey - 5, x + 10, ey);
        bezierCurve(g2, x - 6, headY + 3, x - 1, headY + 8, x + 3, headY + 8, x + 7, headY + 3);
    }

    private void drawNetFriend4(Graphics2D g2, int x, int waterY, double st) {
        int t2 = 2;
        int headR = 15;

        // Step in place while lifting the fishing net.
        double step = Math.sin(st * 3.0) * 3.0;
        int hipY = (int) (waterY - 20 + step);
        int shoulderY = hipY - 30;
        int headY = shoulderY - 20;

        g2.setColor(INK);
        int plantX = x - 10, plantY = waterY + 4;
        bresenhamLine(g2, x, hipY, plantX + 2, waterY - 8, t2);
        bresenhamLine(g2, plantX + 2, waterY - 8, plantX, plantY, t2);
        fillEllipse(g2, plantX - 5, plantY - 3, 11, 6);

        int stepKneeX = x + 12, stepKneeY = hipY + 10;
        int stepFootX = x + 18, stepFootY = waterY - 6;
        bresenhamLine(g2, x, hipY, stepKneeX, stepKneeY, t2);
        bresenhamLine(g2, stepKneeX, stepKneeY, stepFootX, stepFootY, t2);
        fillEllipse(g2, stepFootX - 5, stepFootY - 3, 11, 6);

        g2.setColor(INK);
        bresenhamLine(g2, x + 2, shoulderY, x, hipY, t2);

        double armWaveL = Math.cos(st * 5.0) * 5;
        double armWaveR = Math.sin(st * 5.0) * 5;

        int hand1X = x - 18, hand1Y = (int) (shoulderY - 18 + armWaveL);
        int hand2X = x + 20, hand2Y = (int) (shoulderY - 22 + armWaveR);

        bresenhamLine(g2, x + 2, shoulderY + 3, hand1X, hand1Y, t2);
        bresenhamLine(g2, x + 2, shoulderY + 3, hand2X, hand2Y, t2);
        fillMidpointCircle(g2, hand1X, hand1Y, 3, INK);
        fillMidpointCircle(g2, hand2X, hand2Y, 3, INK);

        int headX = x + 3;
        bresenhamLine(g2, headX, headY + headR, x + 2, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 4, headY - headR, headX - 8, headY - headR - 6, 1);
        bresenhamLine(g2, headX + 3, headY - headR, headX + 5, headY - headR - 7, 1);

        fillEllipse(g2, headX - 4, headY - 4, 3, 4);
        fillEllipse(g2, headX + 4, headY - 4, 3, 4);
        bezierCurve(g2, headX - 5, headY + 3, headX, headY + 8, headX + 3, headY + 8, headX + 6, headY + 3);
    }

    private void drawWaterSplashesAndSpray(Graphics2D g2, double st) {
        for (int arc = 0; arc < 5; arc++) {
            double offset = (arc - 2) * 5.0;
            double waveY = Math.sin(st * 4.0 + arc * 1.3) * 5.0;
            int alpha = 170 - Math.abs(arc - 2) * 35;
            g2.setColor(new Color(230, 250, 255, Math.max(50, alpha)));
            bezierCurve(g2,
                150, 335 + offset * 0.5,
                205 + offset, 235 + waveY + offset,
                295 + offset, 250 - waveY + offset,
                368, 360 + offset * 0.5, 32);
        }

        g2.setColor(new Color(255, 255, 255, 230));
        bezierCurve(g2, 150, 333, 205, 238, 295, 252, 368, 358, 32);

        Random dropRand = new Random(777);
        for (int i = 0; i < 46; i++) {
            double prog = ((st * 2.8 + i * 0.065) % 1.0);
            double startX = 145 + dropRand.nextDouble() * 20;
            double startY = 325 + dropRand.nextDouble() * 20;
            double targetX = 350 + dropRand.nextDouble() * 45;
            double targetY = 365 + dropRand.nextDouble() * 30;

            double curX = startX + (targetX - startX) * prog;
            double arcHeight = Math.sin(prog * Math.PI) * (78 + (i % 6) * 10);
            double curY = startY + (targetY - startY) * prog - arcHeight;

            int r = 1 + (i % 3);
            int alpha = (int) (240 * Math.sin(prog * Math.PI));
            if (alpha > 10) {
                fillMidpointCircle(g2, (int) curX, (int) curY, r, new Color(240, 252, 255, alpha));
                if (r > 1) {
                    fillMidpointCircle(g2, (int) curX - 1, (int) curY - 1, 1, new Color(255, 255, 255, alpha));
                }
            }
        }

        int splashCX = 370;
        int splashCY = 372;
        for (int s = 0; s < 14; s++) {
            double sAngle = Math.PI * (0.05 + 0.9 * s / 13.0);
            double sDist = 20 + Math.sin(st * 8.0 + s * 1.1) * 16;
            int sx = splashCX + (int) (Math.cos(sAngle) * sDist * 1.5);
            int sy = splashCY - (int) (Math.sin(sAngle) * sDist);
            fillMidpointCircle(g2, sx, sy, 2 + (s % 3), new Color(230, 250, 255, 220));
            g2.setColor(new Color(210, 245, 255, 170));
            bresenhamLine(g2, splashCX + (int)(Math.cos(sAngle) * 10), splashCY - 4, sx, sy, 0);
        }

        int footSplashX = 510;
        int footSplashY = 370;
        for (int k = 0; k < 6; k++) {
            double kProg = ((st * 4.0 + k * 0.18) % 1.0);
            int kx = footSplashX + (int) ((k - 3) * 6 * kProg);
            int ky = footSplashY - (int) (Math.sin(kProg * Math.PI) * 14);
            fillMidpointCircle(g2, kx, ky, 2, new Color(230, 250, 255, (int)(200 * (1 - kProg))));
        }
    }

    private void drawForegroundProps(Graphics2D g2, double st) {
        fillMossyBoulder(g2, -65, 490, 220, 130, new Color(52, 65, 58), new Color(42, 118, 48));
        fillMossyBoulder(g2, 60, 525, 130, 85, new Color(62, 75, 68), new Color(55, 132, 58));

        fillMossyBoulder(g2, 430, 485, 210, 135, new Color(55, 68, 62), new Color(48, 125, 52));
        fillMossyBoulder(g2, 380, 535, 110, 75, new Color(65, 78, 72), new Color(58, 138, 62));

        int boxX = 475;
        int boxY = 478;
        int boxW = 54;
        int boxH = 38;

        g2.setColor(new Color(20, 35, 25, 160));
        fillMidpointEllipse(g2, boxX + boxW / 2, boxY + boxH + 2, boxW / 2 + 4, 6, new Color(20, 35, 25, 160));

        g2.setColor(new Color(185, 235, 245, 140));
        fillRoundedRectangle(g2, boxX, boxY + 10, boxW, boxH - 10, 6, 6);
        g2.setColor(new Color(120, 185, 205, 220));
        drawRoundedRectangle(g2, boxX, boxY + 10, boxW, boxH - 10, 6, 6);

        g2.setColor(new Color(85, 180, 210, 160));
        fillRectangle(g2, boxX + 2, boxY + 18, boxW - 4, boxH - 20);
        g2.setColor(new Color(75, 65, 55));
        fillMidpointCircle(g2, boxX + 12, boxY + boxH - 4, 3, new Color(75, 65, 55));
        fillMidpointCircle(g2, boxX + 22, boxY + boxH - 3, 2, new Color(85, 75, 65));
        fillMidpointCircle(g2, boxX + 38, boxY + boxH - 4, 3, new Color(65, 55, 45));

        double fishWiggle = Math.sin(st * 8.0) * 2;
        g2.setColor(new Color(225, 75, 45));
        int fx = boxX + 26;
        int fy = boxY + 25 + (int) fishWiggle;
        fillMidpointEllipse(g2, fx, fy, 4, 2, new Color(225, 75, 45));
        bresenhamLine(g2, fx - 4, fy, fx - 7, fy - 2, 1);
        bresenhamLine(g2, fx - 4, fy, fx - 7, fy + 2, 1);

        g2.setColor(new Color(25, 120, 235));
        fillRoundedRectangle(g2, boxX - 2, boxY + 6, boxW + 4, 8, 4, 4);
        g2.setColor(new Color(15, 90, 195));
        drawRoundedRectangle(g2, boxX - 2, boxY + 6, boxW + 4, 8, 4, 4);
        g2.setColor(new Color(10, 65, 140));
        for (int vx = boxX + 6; vx < boxX + boxW - 4; vx += 7) {
            bresenhamLine(g2, vx, boxY + 8, vx + 3, boxY + 8, 0);
        }

        g2.setColor(new Color(25, 120, 235));
        drawRoundedRectangle(g2, boxX + boxW / 2 - 9, boxY, 18, 8, 3, 3);

        g2.setColor(new Color(45, 135, 55, 230));
        for (int i = 0; i < 7; i++) {
            double lx = 10 + i * 16;
            double ly = 575 - i * 6;
            bezierCurve(g2, lx, ly, lx + 12, ly - 22, lx + 22, ly - 28, lx + 32, ly - 15, 2);
        }
        g2.setColor(new Color(38, 120, 48, 230));
        for (int i = 0; i < 6; i++) {
            double rx = 580 - i * 18;
            double ry = 580 - i * 7;
            bezierCurve(g2, rx, ry, rx - 14, ry - 20, rx - 24, ry - 26, rx - 34, ry - 14, 2);
        }

        Point2D center = new Point2D.Float(300.0f, 300.0f);
        float radius = 430.0f;
        float[] dist = {0.0f, 0.70f, 1.0f};
        Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(10, 30, 15, 25),
            new Color(5, 20, 10, 110)
        };
        RadialGradientPaint vig = new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(vig);
        fillRectangle(g2, 0, 0, 600, 600);
    }

    private void drawStreamScene(Graphics2D g2, double st) {
        final int waterY = 360;

        if (streamBackdrop == null) streamBackdrop = buildStreamBackdrop();
        g2.drawImage(streamBackdrop, 0, 0, null);

        drawSunbeams(g2, st);

        drawRiverbed(g2, waterY);

        drawSplashingFriend1(g2, 130, waterY, st);
        drawBucketFriend2(g2, 270, waterY, st);
        drawLaughingFriend3(g2, 370, waterY, st);
        drawNetFriend4(g2, 490, waterY, st);

        drawWaterSurface(g2, waterY, st);

        drawWaterSplashesAndSpray(g2, st);

        drawForegroundProps(g2, st);
    }

    // SCENE 5 (MEMORY 4): 4 FRIENDS RIDING BICYCLES AT SUNSET (REF/SCENE5/1.PNG)

    private BufferedImage buildBicycleBackdrop() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = img.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        LinearGradientPaint skyGrad = new LinearGradientPaint(
            new Point2D.Float(300, 0), new Point2D.Float(300, 320),
            new float[]{0.0f, 0.35f, 0.70f, 1.0f},
            new Color[]{
                new Color(195, 80, 48),
                new Color(245, 135, 42),
                new Color(255, 188, 72),
                new Color(255, 232, 142)
            }
        );
        bg.setPaint(skyGrad);
        fillRectangle(bg, 0, 0, 600, 320);

        int sunX = 410, sunY = 175, sunR = 38;
        for (int r = sunR + 80; r >= sunR; r -= 6) {
            int alpha = (int) (35 * (1.0 - (double)(r - sunR) / 80.0));
            fillMidpointCircle(bg, sunX, sunY, r, new Color(255, 200, 95, alpha));
        }
        fillMidpointCircle(bg, sunX, sunY, sunR + 12, new Color(255, 235, 150, 90));
        fillMidpointCircle(bg, sunX, sunY, sunR, new Color(255, 252, 235));

        Color cloudColor1 = new Color(255, 195, 140, 140);
        Color cloudColor2 = new Color(245, 160, 110, 110);
        fillMidpointEllipse(bg, 140, 95, 95, 24, cloudColor1);
        fillMidpointEllipse(bg, 180, 85, 65, 28, cloudColor2);
        fillMidpointEllipse(bg, 490, 110, 85, 20, cloudColor1);
        fillMidpointEllipse(bg, 520, 102, 55, 25, cloudColor2);

        int[] mtnFarX = {-20, 60, 150, 240, 330, 420, 510, 620, 620, -20};
        int[] mtnFarY = {280, 210, 245, 190, 235, 180, 225, 200, 320, 320};
        bg.setColor(new Color(165, 88, 72, 190));
        fillPolygonScanline(bg, new Polygon(mtnFarX, mtnFarY, mtnFarX.length));

        int[] mtnMidX = {-20, 80, 180, 270, 380, 470, 580, 620, 620, -20};
        int[] mtnMidY = {295, 245, 275, 230, 265, 220, 255, 240, 330, 330};
        bg.setColor(new Color(132, 68, 45, 220));
        fillPolygonScanline(bg, new Polygon(mtnMidX, mtnMidY, mtnMidX.length));

        int[] roadX = {195, 415, 660, -60};
        int[] roadY = {270, 270, 600, 600};
        LinearGradientPaint roadGrad = new LinearGradientPaint(
            new Point2D.Float(300, 270), new Point2D.Float(300, 600),
            new float[]{0.0f, 0.45f, 1.0f},
            new Color[]{
                new Color(232, 175, 110),
                new Color(205, 142, 82),
                new Color(168, 105, 52)
            }
        );
        bg.setPaint(roadGrad);
        fillPolygonScanline(bg, new Polygon(roadX, roadY, 4));

        int[] leftGrassX = {-20, 200, -20};
        int[] leftGrassY = {270, 270, 600};
        bg.setColor(new Color(88, 125, 45));
        fillPolygonScanline(bg, new Polygon(leftGrassX, leftGrassY, 3));
        int[] rightGrassX = {410, 620, 620};
        int[] rightGrassY = {270, 270, 600};
        bg.setColor(new Color(78, 115, 40));
        fillPolygonScanline(bg, new Polygon(rightGrassX, rightGrassY, 3));

        bg.setColor(new Color(145, 88, 42, 90));
        for (int r = 300; r < 590; r += 28) {
            double prog = (r - 270) / 330.0;
            int rw = (int) (120 + prog * 380);
            int rx = 300 - rw / 2;
            bresenhamLine(bg, rx + 15, r, rx + 45, r + 4, 1);
            bresenhamLine(bg, rx + rw - 50, r, rx + rw - 15, r + 5, 1);
        }
        Random roadRand = new Random(5555);
        for (int i = 0; i < 45; i++) {
            int px = roadRand.nextInt(560) + 20;
            int py = roadRand.nextInt(300) + 290;
            int pr = 1 + roadRand.nextInt(3);
            fillMidpointEllipse(bg, px, py, pr + 1, pr, new Color(125, 78, 42, 160));
        }

        int[] fencePostX = {18, 62, 112, 168};
        int[] fencePostY = {460, 410, 360, 310};
        int[] fencePostH = {130, 110, 90, 75};
        bg.setColor(new Color(112, 70, 40));
        for (int i = 0; i < fencePostX.length; i++) {
            int fx = fencePostX[i];
            int fy = fencePostY[i];
            int fh = fencePostH[i];
            fillRectangle(bg, fx - 4, fy - fh, 8, fh);
            bg.setColor(new Color(145, 92, 54));
            fillRectangle(bg, fx - 2, fy - fh + 2, 4, fh - 2);
            bg.setColor(new Color(85, 52, 28));
            bresenhamLine(bg, fx - 4, fy - fh, fx + 4, fy - fh, 1);
        }
        bg.setColor(new Color(118, 75, 42));
        for (int r = 0; r < 2; r++) {
            int off = r * 35;
            for (int i = 0; i < fencePostX.length - 1; i++) {
                int x1 = fencePostX[i], y1 = fencePostY[i] - fencePostH[i] + 25 + off;
                int x2 = fencePostX[i + 1], y2 = fencePostY[i + 1] - fencePostH[i + 1] + 20 + off;
                bresenhamLine(bg, x1, y1, x2, y2, 2);
            }
        }

        bg.setColor(new Color(110, 62, 38));
        fillRectangle(bg, 50, 310, 48, 30);
        int[] hutRoofX = {42, 74, 106};
        int[] hutRoofY = {310, 285, 310};
        bg.setColor(new Color(82, 48, 28));
        fillPolygonScanline(bg, new Polygon(hutRoofX, hutRoofY, 3));

        int hx = 475, hy = 165, hw = 145, hh = 160;
        bg.setColor(new Color(225, 208, 182));
        fillRectangle(bg, hx, hy, hw, hh);
        bg.setColor(new Color(85, 50, 26));
        fillRectangle(bg, hx, hy, 10, hh);
        fillRectangle(bg, hx + 65, hy + 35, 8, hh - 35);
        fillRectangle(bg, hx, hy + 35, hw, 8);
        fillRectangle(bg, hx, hy + hh - 12, hw, 12);
        bg.setColor(new Color(250, 245, 235));
        fillRectangle(bg, hx + 18, hy + 48, 42, 48);
        bg.setColor(new Color(85, 50, 26));
        drawRectangle(bg, hx + 18, hy + 48, 42, 48);
        bresenhamLine(bg, hx + 39, hy + 48, hx + 39, hy + 96, 1);
        bresenhamLine(bg, hx + 18, hy + 72, hx + 60, hy + 72, 1);

        int[] roofX = {440, 530, 620, 620, 450};
        int[] roofY = {175, 120, 155, 185, 192};
        bg.setColor(new Color(62, 70, 82));
        fillPolygonScanline(bg, new Polygon(roofX, roofY, 5));
        bg.setColor(new Color(45, 52, 62));
        bresenhamLine(bg, 440, 175, 530, 120, 3);
        bresenhamLine(bg, 530, 120, 620, 155, 3);
        bg.setColor(new Color(90, 102, 118));
        for (int rx = 455; rx < 600; rx += 20) {
            bresenhamLine(bg, rx, 160, rx - 10, 185, 1);
        }

        fillMossyBoulder(bg, 535, 390, 55, 38, new Color(92, 102, 95), new Color(75, 135, 65));
        bg.setColor(new Color(105, 65, 36));
        fillRectangle(bg, 550, 235, 10, 160);
        bg.setColor(new Color(175, 132, 88));
        fillRoundedRectangle(bg, 538, 205, 34, 105, 6, 6);
        bg.setColor(new Color(110, 72, 40));
        drawRoundedRectangle(bg, 538, 205, 34, 105, 6, 6);
        for (int sy = 215; sy < 305; sy += 16) {
            bresenhamLine(bg, 540, sy, 570, sy + 3, 0);
        }
        bg.setColor(new Color(45, 26, 12));
        bg.setFont(new Font("Serif", Font.BOLD, 18));
        bg.drawString("森", 547, 236);
        bg.setFont(new Font("SansSerif", Font.BOLD, 15));
        bg.drawString("の", 548, 264);
        bg.setFont(new Font("Serif", Font.BOLD, 18));
        bg.drawString("里", 547, 294);

        bg.setColor(new Color(55, 35, 20));
        int[] leftTrunkX = {-20, 30, 40, 5, -20};
        int[] leftTrunkY = {0, 0, 360, 375, 0};
        fillPolygonScanline(bg, new Polygon(leftTrunkX, leftTrunkY, 5));
        bg.setColor(new Color(38, 22, 12));
        for (int ty = 40; ty < 360; ty += 28) {
            bresenhamLine(bg, -5, ty, 25, ty + 14, 1);
        }
        bg.setColor(new Color(68, 42, 24));
        int[] lbrX = {25, 175, 170, 25};
        int[] lbrY = {115, 55, 75, 135};
        fillPolygonScanline(bg, new Polygon(lbrX, lbrY, 4));

        bg.setColor(new Color(52, 32, 18));
        int[] rightTrunkX = {565, 610, 620, 575, 565};
        int[] rightTrunkY = {0, 0, 340, 345, 0};
        fillPolygonScanline(bg, new Polygon(rightTrunkX, rightTrunkY, 5));
        bg.setColor(new Color(65, 40, 22));
        int[] rbrX = {575, 410, 415, 575};
        int[] rbrY = {95, 45, 62, 115};
        fillPolygonScanline(bg, new Polygon(rbrX, rbrY, 4));

        int[][] leafPuffs = {
            {-40, -40, 140, 110, 0}, {-10, 10, 130, 95, 1}, {60, -25, 150, 115, 0},
            {40, 35, 120, 85, 1}, {110, 15, 110, 75, 2}, {140, 45, 95, 65, 2},
            {-25, 65, 125, 90, 1}, {20, 80, 105, 70, 2}, {80, 70, 90, 60, 3},
            {340, -40, 150, 115, 0}, {380, 10, 140, 100, 1}, {460, -35, 160, 120, 0},
            {440, 30, 130, 90, 1}, {360, 40, 115, 80, 2}, {480, 25, 125, 85, 2},
            {410, 65, 100, 70, 2}, {460, 75, 110, 75, 3}, {520, 55, 110, 80, 1}
        };

        Color[] leafTones = {
            new Color(32, 68, 25, 250),
            new Color(48, 105, 34, 245),
            new Color(82, 148, 45, 235),
            new Color(165, 205, 58, 220)
        };

        for (int[] puff : leafPuffs) {
            bg.setColor(leafTones[puff[4]]);
            fillEllipse(bg, puff[0], puff[1], puff[2], puff[3]);
        }
        bg.setColor(new Color(245, 225, 95, 150));
        fillEllipse(bg, 125, 50, 55, 35);
        fillEllipse(bg, 435, 45, 65, 40);

        for (int i = 0; i < 18; i++) {
            int fx = 15 + i * 22;
            int fy = 540 + (i % 3) * 16;
            drawChamomileFlower(bg, fx, fy, 0.7 + (i % 4) * 0.1, 0.1 * i);
        }
        for (int i = 0; i < 14; i++) {
            int fx = 440 + i * 12;
            int fy = 550 + (i % 4) * 12;
            drawChamomileFlower(bg, fx, fy, 0.65 + (i % 3) * 0.1, -0.15 * i);
        }

        bg.dispose();
        return img;
    }

    private void drawFrontBicycleBase(Graphics2D g2, double cx, double cy, double scale, double wheelAngle,
                                      Color frameColor, double pedalAngle, double tiltAngle) {
        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);
        g2.scale(scale, scale);
        g2.rotate(tiltAngle, 0, 0);

        int wheelW = 11;
        int wheelH = 36;
        int hubY = -wheelH;
        int forkTopY = -wheelH * 2 + 8;

        fillMidpointEllipse(g2, 0, 2, 32, 8, new Color(45, 25, 12, 120));

        fillMidpointEllipse(g2, 0, hubY - 12, 8, 26, new Color(30, 32, 35, 180));

        int bbY = hubY + 14;
        int crankLen = 14;
        double leftCrankY = bbY + Math.sin(pedalAngle) * crankLen;
        double rightCrankY = bbY - Math.sin(pedalAngle) * crankLen;
        int crankW = 20;

        g2.setColor(new Color(175, 180, 188));
        bresenhamLine(g2, -crankW, bbY, crankW, bbY, 3);
        fillMidpointCircle(g2, 0, bbY, 6, new Color(55, 58, 62));

        bresenhamLine(g2, -crankW, bbY, -crankW, (int) leftCrankY, 3);
        bresenhamLine(g2, crankW, bbY, crankW, (int) rightCrankY, 3);
        fillMidpointEllipse(g2, -crankW - 4, (int) leftCrankY, 7, 3, new Color(45, 48, 52));
        fillMidpointEllipse(g2, crankW + 4, (int) rightCrankY, 7, 3, new Color(45, 48, 52));
        fillMidpointCircle(g2, -crankW - 4, (int) leftCrankY, 1, new Color(255, 175, 40));
        fillMidpointCircle(g2, crankW + 4, (int) rightCrankY, 1, new Color(255, 175, 40));

        g2.setColor(frameColor);
        bresenhamLine(g2, 0, forkTopY, 0, bbY, 5);
        bresenhamLine(g2, 0, bbY, 0, forkTopY - 12, 4);

        fillMidpointEllipse(g2, 0, hubY, wheelW, wheelH, new Color(32, 35, 38));
        fillMidpointEllipse(g2, 0, hubY, wheelW - 3, wheelH - 3, new Color(210, 215, 222));
        fillMidpointEllipse(g2, 0, hubY, wheelW - 6, wheelH - 7, new Color(195, 140, 85, 160));

        g2.setColor(new Color(240, 245, 252, 220));
        for (int i = 0; i < 8; i++) {
            double spA = wheelAngle + i * (Math.PI / 4.0);
            int spX = (int) (Math.cos(spA) * (wheelW - 3));
            int spY = hubY + (int) (Math.sin(spA) * (wheelH - 3));
            bresenhamLine(g2, 0, hubY, spX, spY, 0);
        }
        fillMidpointCircle(g2, 0, hubY, 4, new Color(120, 125, 132));

        g2.setColor(frameColor);
        bresenhamLine(g2, -8, hubY, -5, forkTopY, 3);
        bresenhamLine(g2, 8, hubY, 5, forkTopY, 3);
        bresenhamLine(g2, -6, forkTopY, 6, forkTopY, 4);

        g2.setColor(new Color(220, 225, 232));
        for (int fa = -wheelW - 1; fa <= wheelW + 1; fa++) {
            fillMidpointCircle(g2, fa, forkTopY + 2, 1, new Color(220, 225, 232));
        }

        g2.setTransform(old);
    }

    private void drawFrontBicycleCockpit(Graphics2D g2, double cx, double cy, double scale,
                                        Color frameColor, boolean hasFrontBasket, Color basketBagColor,
                                        double tiltAngle) {
        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);
        g2.scale(scale, scale);
        g2.rotate(tiltAngle, 0, 0);

        int wheelH = 36;
        int forkTopY = -wheelH * 2 + 8;
        int stemTopY = -wheelH * 2 - 4;

        g2.setColor(new Color(195, 200, 208));
        bresenhamLine(g2, 0, forkTopY, 0, stemTopY, 4);

        int barHalf = 36;
        int barY = stemTopY;
        g2.setColor(new Color(165, 170, 178));
        bezierCurve(g2, -barHalf, barY + 3, -barHalf / 2, barY - 3, barHalf / 2, barY - 3, barHalf, barY + 3, 3);
        fillMidpointEllipse(g2, -barHalf + 2, barY + 3, 6, 3, new Color(35, 38, 42));
        fillMidpointEllipse(g2, barHalf - 2, barY + 3, 6, 3, new Color(35, 38, 42));
        g2.setColor(new Color(220, 225, 232));
        bresenhamLine(g2, -barHalf + 6, barY + 4, -barHalf + 16, barY + 8, 1);
        bresenhamLine(g2, barHalf - 6, barY + 4, barHalf - 16, barY + 8, 1);

        if (hasFrontBasket) {
            int bskW = 32, bskH = 18;
            int bskX = -bskW / 2, bskY = stemTopY + 2;

            g2.setColor(new Color(20, 20, 20, 140));
            fillRectangle(g2, bskX + 1, bskY + 1, bskW - 2, bskH - 2);

            if (basketBagColor != null) {
                g2.setColor(basketBagColor);
                fillRoundedRectangle(g2, bskX + 2, bskY - 3, bskW - 4, bskH + 1, 5, 5);
                g2.setColor(new Color(255, 255, 255, 120));
                bresenhamLine(g2, bskX + 5, bskY + 2, bskX + bskW - 5, bskY + 2, 1);
            }

            g2.setColor(new Color(42, 46, 50));
            drawRoundedRectangle(g2, bskX, bskY, bskW, bskH, 3, 3);
            for (int bx = bskX + 5; bx < bskX + bskW; bx += 5) {
                bresenhamLine(g2, bx, bskY, bx, bskY + bskH, 0);
            }
            for (int by = bskY + 4; by < bskY + bskH; by += 4) {
                bresenhamLine(g2, bskX, by, bskX + bskW, by, 0);
            }

            fillMidpointCircle(g2, 0, bskY + bskH + 3, 5, new Color(255, 242, 160));
            drawMidpointCircle(g2, 0, bskY + bskH + 3, 5, new Color(180, 185, 192));
        } else {
            fillMidpointCircle(g2, 0, forkTopY - 6, 5, new Color(255, 242, 160));
            drawMidpointCircle(g2, 0, forkTopY - 6, 5, new Color(180, 185, 192));
        }

        g2.setTransform(old);
    }

    private void drawBikerFriend1_RedHoodie(Graphics2D g2, double x, double y, double st) {
        double scale = 1.05;
        double pedalAngle = st * 5.5;
        double bob = Math.sin(st * 11.0) * 2.2;
        double tilt = Math.sin(pedalAngle) * 0.04;
        int t2 = 2;

        int bbY = (int) (y - 24 * scale);
        int hipY = (int) (y - 56 * scale + bob);
        int shoulderY = (int) (y - 96 * scale + bob);
        int headY = (int) (y - 120 * scale + bob);
        int headX = (int) x;
        int headR = (int) (16 * scale);

        int crankW = (int) (20 * scale);
        int leftFootY = bbY + (int) (Math.sin(pedalAngle) * (14 * scale));
        int rightFootY = bbY - (int) (Math.sin(pedalAngle) * (14 * scale));

        int leftKneeX = (int) (x - 18 * scale);
        int leftKneeY = (hipY + leftFootY) / 2 - (int) (4 * scale);
        g2.setColor(INK);
        bresenhamLine(g2, (int)(x - 10 * scale), hipY, leftKneeX, leftKneeY, t2);
        bresenhamLine(g2, leftKneeX, leftKneeY, (int)(x - crankW - 4 * scale), leftFootY, t2);
        fillEllipse(g2, (int)(x - crankW - 10 * scale), leftFootY - (int)(4 * scale), (int)(16 * scale), (int)(8 * scale));

        int rightKneeX = (int) (x + 18 * scale);
        int rightKneeY = (hipY + rightFootY) / 2 - (int) (4 * scale);
        bresenhamLine(g2, (int)(x + 10 * scale), hipY, rightKneeX, rightKneeY, t2);
        bresenhamLine(g2, rightKneeX, rightKneeY, (int)(x + crankW + 4 * scale), rightFootY, t2);
        fillEllipse(g2, (int)(x + crankW + 2 * scale), rightFootY - (int)(4 * scale), (int)(16 * scale), (int)(8 * scale));

        bresenhamLine(g2, (int) x, shoulderY, (int) x, hipY, t2);

        bresenhamLine(g2, (int) x, shoulderY, headX, headY + headR, t2);

        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 6, headY - headR, headX - 10, headY - headR - 7, 1);
        bresenhamLine(g2, headX, headY - headR, headX + 1, headY - headR - 9, 1);
        bresenhamLine(g2, headX + 7, headY - headR + 1, headX + 11, headY - headR - 6, 1);

        fillEllipse(g2, headX - 2, headY - 4, (int)(4 * scale), (int)(5 * scale));
        fillEllipse(g2, headX + 5, headY - 4, (int)(4 * scale), (int)(5 * scale));

        bezierCurve(g2, headX - 4, headY + 3, headX + 1, headY + 8, headX + 5, headY + 8, headX + 8, headY + 3);

        drawFrontBicycleBase(g2, x, y, scale, st * 9.0, new Color(30, 95, 205), pedalAngle, tilt);

        int gripX1 = (int) (x - 34 * scale);
        int gripX2 = (int) (x + 34 * scale);
        int gripY = (int) (y - 80 * scale);
        g2.setColor(INK);
        bresenhamLine(g2, (int)(x - 12 * scale), shoulderY + (int)(2 * scale), gripX1, gripY, t2);
        bresenhamLine(g2, (int)(x + 12 * scale), shoulderY + (int)(2 * scale), gripX2, gripY, t2);
        fillMidpointCircle(g2, gripX1, gripY, (int)(3 * scale), INK);
        fillMidpointCircle(g2, gripX2, gripY, (int)(3 * scale), INK);

        drawFrontBicycleCockpit(g2, x, y, scale, new Color(30, 95, 205), false, null, tilt);
    }

    private void drawBikerFriend2_BlueJacket(Graphics2D g2, double x, double y, double st) {
        double scale = 0.72;
        double pedalAngle = st * 4.8;
        double bob = Math.sin(st * 9.6) * 1.5;
        double tilt = Math.sin(pedalAngle) * 0.035;
        int t2 = 2;

        int bbY = (int) (y - 24 * scale);
        int hipY = (int) (y - 56 * scale + bob);
        int shoulderY = (int) (y - 96 * scale + bob);
        int headY = (int) (y - 120 * scale + bob);
        int headX = (int) x;
        int headR = (int) (15 * scale);

        int crankW = (int) (20 * scale);
        int leftFootY = bbY + (int) (Math.sin(pedalAngle) * (14 * scale));
        int rightFootY = bbY - (int) (Math.sin(pedalAngle) * (14 * scale));

        g2.setColor(INK);
        bresenhamLine(g2, (int)(x - 10 * scale), hipY, (int)(x - 16 * scale), (hipY + leftFootY) / 2, t2);
        bresenhamLine(g2, (int)(x - 16 * scale), (hipY + leftFootY) / 2, (int)(x - crankW - 4 * scale), leftFootY, t2);
        fillEllipse(g2, (int)(x - crankW - 9 * scale), leftFootY - (int)(3 * scale), (int)(14 * scale), (int)(7 * scale));

        bresenhamLine(g2, (int)(x + 10 * scale), hipY, (int)(x + 16 * scale), (hipY + rightFootY) / 2, t2);
        bresenhamLine(g2, (int)(x + 16 * scale), (hipY + rightFootY) / 2, (int)(x + crankW + 4 * scale), rightFootY, t2);
        fillEllipse(g2, (int)(x + crankW + 1 * scale), rightFootY - (int)(3 * scale), (int)(14 * scale), (int)(7 * scale));

        bresenhamLine(g2, (int) x, shoulderY, (int) x, hipY, t2);

        bresenhamLine(g2, (int) x, shoulderY, headX, headY + headR, t2);

        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 5, headY - headR, headX - 8, headY - headR - 6, 1);
        bresenhamLine(g2, headX + 3, headY - headR, headX + 6, headY - headR - 6, 1);

        fillEllipse(g2, headX - 4, headY - 3, (int)(3 * scale), (int)(4 * scale));
        fillEllipse(g2, headX + 2, headY - 3, (int)(3 * scale), (int)(4 * scale));
        bezierCurve(g2, headX - 3, headY + 3, headX, headY + 6, headX + 2, headY + 6, headX + 4, headY + 3);

        drawFrontBicycleBase(g2, x, y, scale, st * 8.5, new Color(42, 142, 58), pedalAngle, tilt);

        int gripX1 = (int) (x - 34 * scale);
        int gripX2 = (int) (x + 34 * scale);
        int gripY = (int) (y - 80 * scale);
        g2.setColor(INK);
        bresenhamLine(g2, (int)(x - 10 * scale), shoulderY + (int)(2 * scale), gripX1, gripY, t2);
        bresenhamLine(g2, (int)(x + 10 * scale), shoulderY + (int)(2 * scale), gripX2, gripY, t2);
        fillMidpointCircle(g2, gripX1, gripY, (int)(2 * scale), INK);
        fillMidpointCircle(g2, gripX2, gripY, (int)(2 * scale), INK);

        drawFrontBicycleCockpit(g2, x, y, scale, new Color(42, 142, 58), true, null, tilt);
    }

    private void drawBikerFriend3_CenterHero23(Graphics2D g2, double x, double y, double st) {
        double scale = 1.10;
        double pedalAngle = st * 5.8;
        double bob = Math.sin(st * 11.6) * 2.4;
        double tilt = Math.sin(pedalAngle) * 0.04;
        int t2 = 2;

        int bbY = (int) (y - 24 * scale);
        int hipY = (int) (y - 56 * scale + bob);
        int shoulderY = (int) (y - 96 * scale + bob);
        int headY = (int) (y - 120 * scale + bob);
        int headX = (int) x;
        int headR = (int) (17 * scale);

        int crankW = (int) (20 * scale);
        int leftFootY = bbY + (int) (Math.sin(pedalAngle) * (14 * scale));
        int rightFootY = bbY - (int) (Math.sin(pedalAngle) * (14 * scale));

        int leftKneeX = (int) (x - 19 * scale);
        int leftKneeY = (hipY + leftFootY) / 2 - (int) (4 * scale);
        g2.setColor(INK);
        bresenhamLine(g2, (int)(x - 11 * scale), hipY, leftKneeX, leftKneeY, t2);
        bresenhamLine(g2, leftKneeX, leftKneeY, (int)(x - crankW - 4 * scale), leftFootY, t2);
        fillEllipse(g2, (int)(x - crankW - 10 * scale), leftFootY - (int)(4 * scale), (int)(16 * scale), (int)(8 * scale));

        int rightKneeX = (int) (x + 19 * scale);
        int rightKneeY = (hipY + rightFootY) / 2 - (int) (4 * scale);
        bresenhamLine(g2, (int)(x + 11 * scale), hipY, rightKneeX, rightKneeY, t2);
        bresenhamLine(g2, rightKneeX, rightKneeY, (int)(x + crankW + 4 * scale), rightFootY, t2);
        fillEllipse(g2, (int)(x + crankW + 2 * scale), rightFootY - (int)(4 * scale), (int)(16 * scale), (int)(8 * scale));

        bresenhamLine(g2, (int) x, shoulderY, (int) x, hipY, t2);

        bresenhamLine(g2, (int) x, shoulderY, headX, headY + headR, t2);

        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 6, headY - headR, headX - 10, headY - headR - 8, 1);
        bresenhamLine(g2, headX + 1, headY - headR, headX + 2, headY - headR - 10, 1);
        bresenhamLine(g2, headX + 8, headY - headR + 1, headX + 12, headY - headR - 7, 1);

        fillEllipse(g2, headX - 5, headY - 4, (int)(4 * scale), (int)(6 * scale));
        fillEllipse(g2, headX + 3, headY - 4, (int)(4 * scale), (int)(6 * scale));
        fillMidpointCircle(g2, headX - 4, headY - 3, 1, Color.WHITE);
        fillMidpointCircle(g2, headX + 4, headY - 3, 1, Color.WHITE);

        bezierCurve(g2, headX - 5, headY + 3, headX, headY + 8, headX + 4, headY + 8, headX + 7, headY + 3);

        drawFrontBicycleBase(g2, x, y, scale, st * 9.4, new Color(28, 80, 62), pedalAngle, tilt);

        int gripX1 = (int) (x - 34 * scale);
        int gripX2 = (int) (x + 34 * scale);
        int gripY = (int) (y - 80 * scale);
        g2.setColor(INK);
        bresenhamLine(g2, (int)(x - 12 * scale), shoulderY + (int)(2 * scale), gripX1, gripY, t2);
        bresenhamLine(g2, (int)(x + 12 * scale), shoulderY + (int)(2 * scale), gripX2, gripY, t2);
        fillMidpointCircle(g2, gripX1, gripY, (int)(3 * scale), INK);
        fillMidpointCircle(g2, gripX2, gripY, (int)(3 * scale), INK);

        drawFrontBicycleCockpit(g2, x, y, scale, new Color(28, 80, 62), true, new Color(65, 125, 55), tilt);
    }

    private void drawBikerFriend4_GreenHoodie(Graphics2D g2, double x, double y, double st) {
        double scale = 0.92;
        double pedalAngle = st * 5.4 + 0.8;
        double bob = Math.sin(st * 10.8 + 0.8) * 1.9;
        double tilt = Math.sin(pedalAngle) * 0.038;
        int t2 = 2;

        int bbY = (int) (y - 24 * scale);
        int hipY = (int) (y - 56 * scale + bob);
        int shoulderY = (int) (y - 96 * scale + bob);
        int headY = (int) (y - 120 * scale + bob);
        int headX = (int) x;
        int headR = (int) (15 * scale);

        int crankW = (int) (20 * scale);
        int leftFootY = bbY + (int) (Math.sin(pedalAngle) * (14 * scale));
        int rightFootY = bbY - (int) (Math.sin(pedalAngle) * (14 * scale));

        g2.setColor(INK);
        bresenhamLine(g2, (int)(x - 10 * scale), hipY, (int)(x - 16 * scale), (hipY + leftFootY) / 2, t2);
        bresenhamLine(g2, (int)(x - 16 * scale), (hipY + leftFootY) / 2, (int)(x - crankW - 4 * scale), leftFootY, t2);
        fillEllipse(g2, (int)(x - crankW - 9 * scale), leftFootY - (int)(3 * scale), (int)(14 * scale), (int)(7 * scale));

        bresenhamLine(g2, (int)(x + 10 * scale), hipY, (int)(x + 16 * scale), (hipY + rightFootY) / 2, t2);
        bresenhamLine(g2, (int)(x + 16 * scale), (hipY + rightFootY) / 2, (int)(x + crankW + 4 * scale), rightFootY, t2);
        fillEllipse(g2, (int)(x + crankW + 1 * scale), rightFootY - (int)(3 * scale), (int)(14 * scale), (int)(7 * scale));

        bresenhamLine(g2, (int) x, shoulderY, (int) x, hipY, t2);

        bresenhamLine(g2, (int) x, shoulderY, headX, headY + headR, t2);

        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 5, headY - headR, headX - 8, headY - headR - 6, 1);
        bresenhamLine(g2, headX + 3, headY - headR, headX + 6, headY - headR - 6, 1);

        fillEllipse(g2, headX - 5, headY - 4, (int)(4 * scale), (int)(5 * scale));
        fillEllipse(g2, headX + 1, headY - 4, (int)(4 * scale), (int)(5 * scale));
        bezierCurve(g2, headX - 5, headY + 3, headX - 2, headY + 7, headX + 2, headY + 7, headX + 4, headY + 4);

        drawFrontBicycleBase(g2, x, y, scale, st * 9.0, new Color(36, 92, 180), pedalAngle, tilt);

        int gripX1 = (int) (x - 34 * scale);
        int gripX2 = (int) (x + 34 * scale);
        int gripY = (int) (y - 80 * scale);
        g2.setColor(INK);
        bresenhamLine(g2, (int)(x - 11 * scale), shoulderY + (int)(2 * scale), gripX1, gripY, t2);
        bresenhamLine(g2, (int)(x + 11 * scale), shoulderY + (int)(2 * scale), gripX2, gripY, t2);
        fillMidpointCircle(g2, gripX1, gripY, (int)(3 * scale), INK);
        fillMidpointCircle(g2, gripX2, gripY, (int)(3 * scale), INK);

        drawFrontBicycleCockpit(g2, x, y, scale, new Color(36, 92, 180), true, null, tilt);
    }

    private void drawBicycleRoadMotion(Graphics2D g2, double st) {
        g2.setColor(new Color(135, 78, 38, 130));
        for (int i = 0; i < 14; i++) {
            double rutProg = ((i * 42.0 - st * 280.0) % 330.0);
            if (rutProg < 0) rutProg += 330.0;
            int ry = (int) (270 + rutProg);
            double p = rutProg / 330.0;
            int rw = (int) (120 + p * 380);
            int rx = 300 - rw / 2;
            bresenhamLine(g2, rx + (int)(15 + p * 10), ry, rx + (int)(45 + p * 15), ry + 3, 1);
            bresenhamLine(g2, rx + rw - (int)(55 + p * 15), ry, rx + rw - (int)(20 + p * 10), ry + 4, 1);
        }

        for (int i = 0; i < 28; i++) {
            double pxBase = (i * 79) % 460 + 70;
            double pyProg = ((i * 43.0 - st * 320.0) % 320.0);
            if (pyProg < 0) pyProg += 320.0;
            int py = (int) (280 + pyProg);
            double p = pyProg / 320.0;
            int px = (int) (300 + (pxBase - 300) * (0.35 + p * 0.85));
            fillMidpointEllipse(g2, px, py, (int)(2 + p * 2), (int)(1 + p), new Color(115, 68, 35, 160));
        }
    }

    private void drawBicycleAtmosphere(Graphics2D g2, double st) {
        Color rayColor1 = new Color(255, 235, 150, 24);
        Color rayColor2 = new Color(255, 215, 120, 14);

        int[][] rayPolys = {
            {-10, 120, 280, 0},
            {80, 250, 480, 120},
            {280, 440, 600, 360},
            {380, 520, 650, 480}
        };
        for (int i = 0; i < rayPolys.length; i++) {
            double pulse = Math.sin(st * 1.5 + i * 1.2) * 6;
            int[] rx = {rayPolys[i][0], rayPolys[i][1] + (int) pulse, rayPolys[i][2] + (int) pulse, rayPolys[i][3]};
            int[] ry = {0, 600, 600, 0};
            g2.setColor((i % 2 == 0) ? rayColor1 : rayColor2);
            fillPolygonScanline(g2, new Polygon(rx, ry, 4));
        }

        Random dustRand = new Random(7777);
        for (int i = 0; i < 35; i++) {
            double origX = dustRand.nextDouble() * 640 - 20;
            double origY = 80 + dustRand.nextDouble() * 480;
            double speed = 40 + dustRand.nextDouble() * 50;
            double wobble = Math.sin(st * 3.5 + i) * 14;

            double x = (origX + wobble - (st * speed)) % 660;
            if (x < -20) x += 660;
            double y = origY - ((st * speed * 0.2) % 400);
            if (y < 60) y += 400;

            int alpha = (int) (60 + 135 * Math.abs(Math.sin(st * 2.0 + i * 0.4)));
            Color dustColor = (i % 2 == 0) ?
                new Color(255, 240, 180, alpha) :
                new Color(255, 210, 120, alpha);
            fillMidpointCircle(g2, (int) x, (int) y, (i % 3 == 0) ? 2 : 1, dustColor);
        }

        Random leafRand = new Random(8888);
        for (int i = 0; i < 14; i++) {
            double lx = (leafRand.nextDouble() * 640 - (st * 140.0 + i * 45.0)) % 660;
            if (lx < -30) lx += 660;
            double ly = 60 + leafRand.nextDouble() * 460 + Math.sin(st * 3.0 + i) * 22;
            double rot = st * 4.0 + i;

            AffineTransform old = g2.getTransform();
            g2.translate(lx, ly);
            g2.rotate(rot);
            Color leafColor = (i % 2 == 0) ? new Color(115, 175, 52, 210) : new Color(225, 160, 48, 200);
            fillMidpointEllipse(g2, 0, 0, 5, 2, leafColor);
            g2.setTransform(old);
        }

        Point2D vigCenter = new Point2D.Float(300.0f, 300.0f);
        float vigRadius = 430.0f;
        float[] vigDist = {0.0f, 0.70f, 1.0f};
        Color[] vigColors = {
            new Color(0, 0, 0, 0),
            new Color(40, 18, 5, 20),
            new Color(25, 10, 2, 95)
        };
        RadialGradientPaint vig = new RadialGradientPaint(vigCenter, vigRadius, vigDist, vigColors);
        g2.setPaint(vig);
        fillRectangle(g2, 0, 0, 600, 600);
    }

    private void drawBicycleScene(Graphics2D g2, double st) {
        if (bicycleBackdrop == null) bicycleBackdrop = buildBicycleBackdrop();
        g2.drawImage(bicycleBackdrop, 0, 0, null);

        drawBicycleRoadMotion(g2, st);

        double x2 = 285 + Math.sin(st * 1.4) * 8.0;
        double y2 = 345 + Math.sin(st * 9.6) * 1.5;

        double x1 = 165 + Math.sin(st * 1.8) * 12.0;
        double y1 = 485 + Math.sin(st * 11.0) * 2.0;

        double x4 = 475 + Math.cos(st * 1.6) * 10.0;
        double y4 = 470 + Math.sin(st * 10.8) * 1.8;

        double x3 = 330 + Math.sin(st * 2.2) * 14.0;
        double y3 = 505 + Math.sin(st * 11.6) * 2.2;

        drawBikerFriend2_BlueJacket(g2, x2, y2, st);
        drawBikerFriend4_GreenHoodie(g2, x4, y4, st);
        drawBikerFriend1_RedHoodie(g2, x1, y1, st);
        drawBikerFriend3_CenterHero23(g2, x3, y3, st);

        drawBicycleAtmosphere(g2, st);
    }

    // 8. MEMORY SCENE 2: EPIC CHILDHOOD TOY SWORD FIGHT (ANIME BATTLE)

    private BufferedImage swordBackdrop;

    private BufferedImage buildSwordBackdrop() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = img.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        LinearGradientPaint sky = new LinearGradientPaint(
            new Point2D.Float(300, 0), new Point2D.Float(300, 480),
            new float[]{0.0f, 0.35f, 0.70f, 1.0f},
            new Color[]{
                new Color(52, 18, 68),
                new Color(185, 45, 62),
                new Color(238, 102, 35),
                new Color(255, 195, 85)
            }
        );
        bg.setPaint(sky);
        fillRectangle(bg, 0, 0, 600, 480);

        int sunX = 300, sunY = 240, sunR = 45;
        for (int r = sunR + 70; r >= sunR; r -= 5) {
            int alpha = (int) (35 * (1.0 - (double)(r - sunR) / 70.0));
            fillMidpointCircle(bg, sunX, sunY, r, new Color(255, 170, 70, alpha));
        }
        fillMidpointCircle(bg, sunX, sunY, sunR + 10, new Color(255, 220, 130, 80));
        fillMidpointCircle(bg, sunX, sunY, sunR, new Color(255, 248, 215));

        int[] mtnX = {-20, 70, 160, 260, 350, 440, 530, 620, 620, -20};
        int[] mtnY = {420, 350, 410, 330, 390, 320, 380, 340, 480, 480};
        Polygon distantMtn = new Polygon(mtnX, mtnY, mtnX.length);
        bg.setColor(new Color(90, 26, 60, 200));
        fillPolygonScanline(bg, distantMtn);

        int[] hillX = {-30, 40, 120, 200, 280, 350, 420, 500, 580, 630, 630, -30};
        int[] hillY = {440, 420, 400, 430, 450, 430, 410, 390, 430, 420, 500, 500};
        Polygon midHill = new Polygon(hillX, hillY, hillX.length);
        bg.setColor(new Color(60, 18, 42, 230));
        fillPolygonScanline(bg, midHill);

        LinearGradientPaint groundGrad = new LinearGradientPaint(
            new Point2D.Float(0, 460), new Point2D.Float(0, 600),
            new float[]{0f, 0.3f, 1f},
            new Color[]{new Color(42, 14, 25), new Color(30, 10, 18), new Color(18, 6, 12)}
        );
        bg.setPaint(groundGrad);
        fillRectangle(bg, 0, 460, 600, 140);

        bg.dispose();
        return img;
    }

    private void drawSwordEmbers(Graphics2D g2, double st) {
        Random rand = new Random(1337);
        for (int i = 0; i < 35; i++) {
            double startX = rand.nextDouble() * 660 - 30;
            double startY = 100 + rand.nextDouble() * 380;
            double speed = 25 + rand.nextDouble() * 40;
            double drift = Math.sin(st * 2.0 + i) * 15;
            double x = (startX + drift - (st * speed * 0.6)) % 660;
            if (x < -30) x += 660;
            double y = startY - ((st * speed) % 350);
            if (y < 60) y += 350;

            int sz = 1 + rand.nextInt(3);
            int alpha = (int) (80 + 130 * Math.sin((st + i * 0.3) * 3.0));
            alpha = Math.max(30, Math.min(240, alpha));

            Color emberColor = (i % 3 == 0) ?
                    new Color(255, 120, 50, alpha) :
                    (i % 3 == 1) ? new Color(255, 210, 80, alpha) :
                                   new Color(80, 200, 255, alpha);

            fillMidpointCircle(g2, (int) x, (int) y, sz, emberColor);
        }
    }

    private void drawGlowingSword(Graphics2D g2, int hx, int hy, int tx, int ty, Color auraColor, Color coreColor, int width) {
        double angle = Math.atan2(ty - hy, tx - hx);
        double perp = angle + Math.PI / 2;

        int gx1 = (int) (hx + Math.cos(perp) * 8);
        int gy1 = (int) (hy + Math.sin(perp) * 8);
        int gx2 = (int) (hx - Math.cos(perp) * 8);
        int gy2 = (int) (hy - Math.sin(perp) * 8);
        g2.setColor(new Color(30, 30, 30));
        bresenhamLine(g2, gx1, gy1, gx2, gy2, 2);

        int hbx = (int) (hx - Math.cos(angle) * 12);
        int hby = (int) (hy - Math.sin(angle) * 12);
        g2.setColor(new Color(50, 50, 50));
        bresenhamLine(g2, hx, hy, hbx, hby, 2);
        fillMidpointCircle(g2, hbx, hby, 3, new Color(20, 20, 20));

        g2.setColor(new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 60));
        bresenhamLine(g2, hx, hy, tx, ty, width + 4);

        g2.setColor(new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 170));
        bresenhamLine(g2, hx, hy, tx, ty, width + 2);

        g2.setColor(coreColor);
        bresenhamLine(g2, hx, hy, tx, ty, width);

        fillMidpointCircle(g2, tx, ty, width + 3, new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 120));
        fillMidpointCircle(g2, tx, ty, width + 1, coreColor);
    }

    private void drawScarf(Graphics2D g2, int neckX, int neckY, boolean faceRight, Color color, double time) {
        int dir = faceRight ? -1 : 1;
        double wave1 = Math.sin(time * 8.0) * 10;
        double wave2 = Math.cos(time * 8.0 + 1.2) * 14;

        g2.setColor(color);
        bezierCurve(g2, neckX, neckY,
                    neckX + dir * 20, neckY + 8 + wave1 * 0.5,
                    neckX + dir * 35, neckY - 6 + wave1,
                    neckX + dir * 50, neckY + 12 + wave2);
        bezierCurve(g2, neckX, neckY + 3,
                    neckX + dir * 18, neckY + 14 + wave2 * 0.4,
                    neckX + dir * 32, neckY + 2 + wave2,
                    neckX + dir * 46, neckY + 20 + wave1);
    }

    private void drawAnimeSlashRibbon(Graphics2D g2, int cx, int cy, double startAngleDeg, double arcSweepDeg, int radius, Color auraColor) {
        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);

        int steps = 20;
        int[] xPoints = new int[steps * 2 + 2];
        int[] yPoints = new int[steps * 2 + 2];

        for (int i = 0; i <= steps; i++) {
            double u = (double) i / steps;
            double a = Math.toRadians(startAngleDeg + arcSweepDeg * u);
            double thickness = Math.sin(u * Math.PI) * 16.0;
            double rOut = radius + thickness * 0.7;
            xPoints[i] = (int) Math.round(Math.cos(a) * rOut);
            yPoints[i] = (int) Math.round(Math.sin(a) * rOut);

            double rIn = radius - thickness * 0.3;
            xPoints[steps * 2 + 1 - i] = (int) Math.round(Math.cos(a) * rIn);
            yPoints[steps * 2 + 1 - i] = (int) Math.round(Math.sin(a) * rIn);
        }

        Polygon slashPoly = new Polygon(xPoints, yPoints, xPoints.length);

        g2.setColor(new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 90));
        fillPolygonScanline(g2, slashPoly);

        g2.setColor(new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 220));
        for (int i = 0; i < steps; i++) {
            bresenhamLine(g2, xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1], 2);
        }

        g2.setColor(new Color(255, 255, 255, 245));
        for (int i = 0; i < steps; i++) {
            bresenhamLine(g2, xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1], 0);
        }

        g2.setTransform(old);
    }

    private void drawSwordStance(Graphics2D g2, int x, int groundY, boolean faceRight,
                                 Color swordAura, Color scarfColor, double st) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;

        double breathe = Math.sin(st * 6.0) * 3;
        int hipY = (int) (groundY - 50 + breathe);
        int shoulderY = (int) (groundY - 90 + breathe);
        int headY = (int) (groundY - 114 + breathe);

        g2.setColor(INK);
        bresenhamLine(g2, x + dir * 6, shoulderY, x - dir * 6, hipY, t2);

        int f1x = x - dir * 26, f1y = groundY - 4;
        int f2x = x + dir * 22, f2y = groundY - 4;
        bresenhamLine(g2, x - dir * 6, hipY, x - dir * 16, groundY - 22, t2);
        bresenhamLine(g2, x - dir * 16, groundY - 22, f1x, f1y, t2);
        bresenhamLine(g2, x - dir * 6, hipY, x + dir * 12, groundY - 22, t2);
        bresenhamLine(g2, x + dir * 12, groundY - 22, f2x, f2y, t2);
        fillEllipse(g2, f1x - 6, f1y - 4, 16, 8);
        fillEllipse(g2, f2x - 6, f2y - 4, 16, 8);

        int handX = x + dir * 18;
        int handY = shoulderY + 8;
        bresenhamLine(g2, x + dir * 6, shoulderY + 4, handX, handY, t2);
        bresenhamLine(g2, x - dir * 6, shoulderY + 4, handX - dir * 4, handY + 4, t2);

        double swordAngle = faceRight ? -35 : -145;
        double rad = Math.toRadians(swordAngle);
        int tipX = handX + (int) (Math.cos(rad) * 46);
        int tipY = handY + (int) (Math.sin(rad) * 46);
        drawGlowingSword(g2, handX, handY, tipX, tipY, swordAura, Color.WHITE, 2);

        if (Math.sin(st * 20) > 0.2) {
            fillMidpointCircle(g2, tipX + (int)(Math.cos(st*30)*6), tipY + (int)(Math.sin(st*30)*6), 2, swordAura);
        }

        drawScarf(g2, x, shoulderY, faceRight, scarfColor, st * 2);

        bresenhamLine(g2, x + dir * 2, headY + headR, x + dir * 6, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, x + dir * 2 - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, x + dir * 2, headY, headR);

        int ex = x + dir * 5;
        fillEllipse(g2, ex - dir * 5 - 2, headY - 5, 4, 4);
        fillEllipse(g2, ex + dir * 3 - 2, headY - 5, 4, 4);
        bezierCurve(g2, ex - dir * 7, headY - 9, ex - dir * 3, headY - 13, ex + dir * 1, headY - 13, ex + dir * 5, headY - 9);
        bezierCurve(g2, ex - 4, headY + 5, ex, headY + 7, ex + 3, headY + 7, ex + 5, headY + 5);
    }

    private void drawSwordDashRunner(Graphics2D g2, int x, int groundY, boolean faceRight,
                                     Color swordAura, Color scarfColor, double phase, double st) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;

        double bounce = Math.abs(Math.sin(phase * Math.PI * 2)) * 8;
        int hipY = (int) (groundY - 50 - bounce);
        int shoulderY = (int) (groundY - 88 - bounce);
        int headY = (int) (groundY - 108 - bounce);
        int torsoShX = x + dir * 20;
        int torsoHipX = x - dir * 10;

        g2.setColor(INK);
        bresenhamLine(g2, torsoShX, shoulderY, torsoHipX, hipY, t2);

        double swing = Math.sin(phase * Math.PI * 2);
        for (int leg = 0; leg < 2; leg++) {
            double s = (leg == 0) ? swing : -swing;
            int kneeX = torsoHipX + dir * (int) (20 * s + 6);
            int kneeY = hipY + 24 - (int) (18 * Math.max(0, s));
            int footX = torsoHipX + dir * (int) (34 * s);
            int footY = groundY - (int) (24 * Math.max(0, s)) - 4;
            bresenhamLine(g2, torsoHipX, hipY, kneeX, kneeY, t2);
            bresenhamLine(g2, kneeX, kneeY, footX, footY, t2);
            fillEllipse(g2, footX - 6, footY - 4, 16, 8);
        }

        int frontHandX = torsoShX + dir * 28;
        int frontHandY = shoulderY + 18 - (int) (swing * 14);
        bresenhamLine(g2, torsoShX, shoulderY + 4, frontHandX, frontHandY, t2);

        int backHandX = torsoShX - dir * 24;
        int backHandY = shoulderY + 12;
        bresenhamLine(g2, torsoShX, shoulderY + 4, backHandX, backHandY, t2);

        int swordTipX = backHandX - dir * 42;
        int swordTipY = backHandY + 26;
        drawGlowingSword(g2, backHandX, backHandY, swordTipX, swordTipY, swordAura, Color.WHITE, 2);

        if (Math.sin(st * 40) > 0) {
            fillMidpointCircle(g2, swordTipX, groundY - 3, 2, new Color(255, 230, 100));
        }

        drawScarf(g2, torsoShX - dir * 4, shoulderY, faceRight, scarfColor, st * 2);

        int headX = torsoShX + dir * 8;
        bresenhamLine(g2, headX, headY + headR, torsoShX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        int ex = headX + dir * 4;
        fillEllipse(g2, ex - dir * 5 - 2, headY - 5, 4, 5);
        fillEllipse(g2, ex + dir * 3 - 2, headY - 5, 4, 5);
        bezierCurve(g2, ex - dir * 7, headY - 9, ex - dir * 3, headY - 13, ex + dir * 1, headY - 13, ex + dir * 5, headY - 9);
        bezierCurve(g2, ex - dir * 5, headY + 5, ex - dir * 1, headY + 8, ex + dir * 3, headY + 8, ex + dir * 6, headY + 5);
    }

    private void drawSwordSlashAttacker(Graphics2D g2, int x, int groundY, boolean faceRight,
                                         Color swordAura, Color scarfColor, double strikeProgress, int strikeType, double st) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;
        double sp = Math.max(0, Math.min(1, strikeProgress));

        if (strikeType % 3 == 0) {
            double hop = Math.sin(sp * Math.PI) * 28;
            int hipY = (int) (groundY - 56 - hop);
            int shoulderY = (int) (groundY - 96 - hop);
            int headY = (int) (groundY - 118 - hop);
            int torsoX = x + dir * (int) (22 * sp);

            g2.setColor(INK);
            bresenhamLine(g2, torsoX + dir * 10, shoulderY, torsoX - dir * 8, hipY, t2);

            int f1x = torsoX - dir * 26, f1y = groundY - (int) (hop * 0.4) - 4;
            int f2x = torsoX + dir * 26, f2y = groundY - 4;
            bresenhamLine(g2, torsoX - dir * 8, hipY, torsoX - dir * 14, groundY - 24, t2);
            bresenhamLine(g2, torsoX - dir * 14, groundY - 24, f1x, f1y, t2);
            bresenhamLine(g2, torsoX - dir * 8, hipY, torsoX + dir * 14, groundY - 24, t2);
            bresenhamLine(g2, torsoX + dir * 14, groundY - 24, f2x, f2y, t2);
            fillEllipse(g2, f1x - 6, f1y - 4, 16, 8);
            fillEllipse(g2, f2x - 6, f2y - 4, 16, 8);

            double swingAngle = (faceRight ? -130 + sp * 170 : -50 - sp * 170);
            double srad = Math.toRadians(swingAngle);
            int handX = torsoX + dir * 16;
            int handY = shoulderY + (int) (Math.sin(sp * Math.PI) * 16);
            bresenhamLine(g2, torsoX + dir * 10, shoulderY + 4, handX, handY, t2);

            int tipX = handX + (int) (Math.cos(srad) * 48);
            int tipY = handY + (int) (Math.sin(srad) * 48);
            drawGlowingSword(g2, handX, handY, tipX, tipY, swordAura, Color.WHITE, 2);

            drawAnimeSlashRibbon(g2, handX, handY, faceRight ? -130 : -50, faceRight ? 170 : -170, 48, swordAura);

            drawScarf(g2, torsoX, shoulderY, faceRight, scarfColor, st * 2);

            int headX = torsoX + dir * 8;
            bresenhamLine(g2, headX, headY + headR, torsoX + dir * 10, shoulderY, t2);
            g2.setColor(Color.WHITE);
            fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, headX, headY, headR);

            int ex = headX + dir * 4;
            fillEllipse(g2, ex - dir * 5 - 2, headY - 5, 4, 4);
            fillEllipse(g2, ex + dir * 3 - 2, headY - 5, 4, 4);
            fillEllipse(g2, ex - dir * 1 - 3, headY + 3, 7, 7);

        } else if (strikeType % 3 == 1) {
            int torsoX = x + dir * (int) (32 * Math.sin(sp * Math.PI));
            int hipY = groundY - 48;
            int shoulderY = groundY - 88;
            int headY = groundY - 110;

            g2.setColor(INK);
            bresenhamLine(g2, torsoX + dir * 14, shoulderY, torsoX - dir * 10, hipY, t2);

            int f1x = torsoX - dir * 34, f1y = groundY - 4;
            int f2x = torsoX + dir * 28, f2y = groundY - 4;
            bresenhamLine(g2, torsoX - dir * 10, hipY, torsoX - dir * 18, groundY - 18, t2);
            bresenhamLine(g2, torsoX - dir * 18, groundY - 18, f1x, f1y, t2);
            bresenhamLine(g2, torsoX - dir * 10, hipY, torsoX + dir * 18, groundY - 18, t2);
            bresenhamLine(g2, torsoX + dir * 18, groundY - 18, f2x, f2y, t2);
            fillEllipse(g2, f1x - 6, f1y - 4, 16, 8);
            fillEllipse(g2, f2x - 6, f2y - 4, 16, 8);

            double swingAngle = (faceRight ? 30 - sp * 180 : 150 + sp * 180);
            double srad = Math.toRadians(swingAngle);
            int handX = torsoX + dir * 22;
            int handY = shoulderY + 8;
            bresenhamLine(g2, torsoX + dir * 14, shoulderY + 4, handX, handY, t2);

            int tipX = handX + (int) (Math.cos(srad) * 50);
            int tipY = handY + (int) (Math.sin(srad) * 50);
            drawGlowingSword(g2, handX, handY, tipX, tipY, swordAura, Color.WHITE, 2);

            drawAnimeSlashRibbon(g2, handX, handY, faceRight ? 30 : 150, faceRight ? -180 : 180, 50, swordAura);

            drawScarf(g2, torsoX, shoulderY, faceRight, scarfColor, st * 2);

            int headX = torsoX + dir * 10;
            bresenhamLine(g2, headX, headY + headR, torsoX + dir * 14, shoulderY, t2);
            g2.setColor(Color.WHITE);
            fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, headX, headY, headR);

            int ex = headX + dir * 4;
            fillEllipse(g2, ex - dir * 5 - 2, headY - 5, 4, 4);
            fillEllipse(g2, ex + dir * 3 - 2, headY - 5, 4, 4);
            fillEllipse(g2, ex - dir * 1 - 3, headY + 3, 6, 6);

        } else {
            double rise = Math.sin(sp * Math.PI) * 22;
            int hipY = (int) (groundY - 54 - rise);
            int shoulderY = (int) (groundY - 94 - rise);
            int headY = (int) (groundY - 116 - rise);
            int torsoX = x + dir * (int) (14 * sp);

            g2.setColor(INK);
            bresenhamLine(g2, torsoX + dir * 8, shoulderY, torsoX - dir * 6, hipY, t2);

            int f1x = torsoX - dir * 20, f1y = groundY - 4;
            int f2x = torsoX + dir * 20, f2y = groundY - 4;
            bresenhamLine(g2, torsoX - dir * 6, hipY, torsoX - dir * 10, groundY - 20, t2);
            bresenhamLine(g2, torsoX - dir * 10, groundY - 20, f1x, f1y, t2);
            bresenhamLine(g2, torsoX - dir * 6, hipY, torsoX + dir * 10, groundY - 20, t2);
            bresenhamLine(g2, torsoX + dir * 10, groundY - 20, f2x, f2y, t2);
            fillEllipse(g2, f1x - 6, f1y - 4, 16, 8);
            fillEllipse(g2, f2x - 6, f2y - 4, 16, 8);

            double swingAngle = (faceRight ? 70 - sp * 160 : 110 + sp * 160);
            double srad = Math.toRadians(swingAngle);
            int handX = torsoX + dir * 18;
            int handY = shoulderY + 4;
            bresenhamLine(g2, torsoX + dir * 8, shoulderY + 4, handX, handY, t2);

            int tipX = handX + (int) (Math.cos(srad) * 48);
            int tipY = handY + (int) (Math.sin(srad) * 48);
            drawGlowingSword(g2, handX, handY, tipX, tipY, swordAura, Color.WHITE, 2);

            drawAnimeSlashRibbon(g2, handX, handY, faceRight ? 70 : 110, faceRight ? -160 : 160, 48, swordAura);

            drawScarf(g2, torsoX, shoulderY, faceRight, scarfColor, st * 2);

            int headX = torsoX + dir * 6;
            bresenhamLine(g2, headX, headY + headR, torsoX + dir * 8, shoulderY, t2);
            g2.setColor(Color.WHITE);
            fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, headX, headY, headR);

            int ex = headX + dir * 4;
            fillEllipse(g2, ex - dir * 5 - 2, headY - 5, 4, 4);
            fillEllipse(g2, ex + dir * 3 - 2, headY - 5, 4, 4);
            fillEllipse(g2, ex - dir * 1 - 3, headY + 3, 6, 6);
        }
    }

    private void drawHitAndFallKid(Graphics2D g2, int startX, int groundY, boolean faceRight,
                                   Color scarfColor, double fallProgress, double st) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;
        double fp = Math.max(0, Math.min(1, fallProgress));

        int x = (int) (startX + dir * 40 * fp);

        if (fp < 0.45) {
            double u = fp / 0.45;
            int hipY = (int) (groundY - 50 + u * 15);
            int shoulderY = (int) (groundY - 90 + u * 18);
            int headY = (int) (groundY - 114 + u * 20);

            g2.setColor(INK);
            bresenhamLine(g2, x - dir * 18, shoulderY, x - dir * 4, hipY, t2);

            bresenhamLine(g2, x - dir * 4, hipY, x - dir * 16, groundY - 18, t2);
            bresenhamLine(g2, x - dir * 16, groundY - 18, x - dir * 26, groundY - 4, t2);
            bresenhamLine(g2, x - dir * 4, hipY, x + dir * 14, groundY - 14, t2);
            bresenhamLine(g2, x + dir * 14, groundY - 14, x + dir * 22, groundY - 4, t2);
            fillEllipse(g2, x - dir * 26 - 6, groundY - 8, 16, 8);
            fillEllipse(g2, x + dir * 22 - 6, groundY - 8, 16, 8);

            int hand1X = x - dir * 28, hand1Y = shoulderY - 16;
            int hand2X = x + dir * 12, hand2Y = shoulderY - 10;
            bresenhamLine(g2, x - dir * 18, shoulderY + 4, hand1X, hand1Y, t2);
            bresenhamLine(g2, x - dir * 18, shoulderY + 4, hand2X, hand2Y, t2);

            int dropSwordX = x + dir * (int)(25 + u * 20);
            int dropSwordY = (int)(shoulderY + u * 70);
            bresenhamLine(g2, dropSwordX, dropSwordY, dropSwordX + dir * 30, dropSwordY + 10, 2);

            int headX = x - dir * 24;
            bresenhamLine(g2, headX, headY + headR, x - dir * 18, shoulderY, t2);
            g2.setColor(Color.WHITE);
            fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, headX, headY, headR);

            bezierCurve(g2, headX - 8, headY - 8, headX - 4, headY - 4, headX - 4, headY - 4, headX - 8, headY);
            bezierCurve(g2, headX + 8, headY - 8, headX + 4, headY - 4, headX + 4, headY - 4, headX + 8, headY);
            fillEllipse(g2, headX - 4, headY + 3, 8, 7);

        } else {
            double u = (fp - 0.45) / 0.55;
            int hipY = (int) (groundY - 35 + u * 5);
            int shoulderY = (int) (groundY - 72 + u * 4);
            int headY = (int) (groundY - 94 + u * 2);

            g2.setColor(INK);
            bresenhamLine(g2, x, shoulderY, x - dir * 6, hipY, t2);

            int f1x = x + dir * 28, f1y = groundY - 4;
            int f2x = x + dir * 18, f2y = groundY - 4;
            bresenhamLine(g2, x - dir * 6, hipY, x + dir * 12, groundY - 14, t2);
            bresenhamLine(g2, x + dir * 12, groundY - 14, f1x, f1y, t2);
            bresenhamLine(g2, x - dir * 6, hipY, x, groundY - 14, t2);
            bresenhamLine(g2, x, groundY - 14, f2x, f2y, t2);
            fillEllipse(g2, f1x - 6, f1y - 4, 16, 8);
            fillEllipse(g2, f2x - 6, f2y - 4, 16, 8);

            int handX = x + dir * 8;
            int handY = headY + 2;
            bresenhamLine(g2, x, shoulderY + 4, handX, handY, t2);
            bresenhamLine(g2, x, shoulderY + 4, x - dir * 18, groundY - 8, t2);

            g2.setColor(new Color(220, 60, 40));
            bresenhamLine(g2, x + dir * 36, groundY - 4, x + dir * 72, groundY - 4, 2);
            bresenhamLine(g2, x + dir * 44, groundY - 8, x + dir * 44, groundY, 2);

            drawScarf(g2, x, shoulderY, faceRight, scarfColor, st);

            bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
            g2.setColor(Color.WHITE);
            fillEllipse(g2, x - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, x, headY, headR);

            bezierCurve(g2, x - 8, headY - 6, x - 4, headY - 9, x - 2, headY - 6, x, headY - 6);
            bezierCurve(g2, x + 1, headY - 6, x + 3, headY - 9, x + 6, headY - 6, x + 8, headY - 6);
            bezierCurve(g2, x - 5, headY + 7, x, headY + 3, x + 3, headY + 3, x + 6, headY + 7);
        }
    }

    private void drawCryingKid(Graphics2D g2, int x, int groundY, boolean faceRight, Color scarfColor, double cryTime) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;

        double sob = Math.abs(Math.sin(cryTime * 14.0)) * 4;
        int hipY = (int) (groundY - 32 - sob * 0.5);
        int shoulderY = (int) (groundY - 68 - sob);
        int headY = (int) (groundY - 92 - sob);

        g2.setColor(INK);
        bresenhamLine(g2, x, shoulderY, x - dir * 6, hipY, t2);

        int f1x = x + dir * 30, f1y = groundY - 4;
        int f2x = x + dir * 18, f2y = groundY - 4;
        bresenhamLine(g2, x - dir * 6, hipY, x + dir * 14, groundY - 14, t2);
        bresenhamLine(g2, x + dir * 14, groundY - 14, f1x, f1y, t2);
        bresenhamLine(g2, x - dir * 6, hipY, x + dir * 2, groundY - 14, t2);
        bresenhamLine(g2, x + dir * 2, groundY - 14, f2x, f2y, t2);
        fillEllipse(g2, f1x - 6, f1y - 4, 16, 8);
        fillEllipse(g2, f2x - 6, f2y - 4, 16, 8);

        int handX = x + dir * 4;
        int handY = headY + 4;
        bresenhamLine(g2, x, shoulderY + 4, handX, handY, t2);
        bresenhamLine(g2, x, shoulderY + 4, x - dir * 18, groundY - 8, t2);

        g2.setColor(new Color(220, 50, 40));
        bresenhamLine(g2, x + dir * 38, groundY - 4, x + dir * 76, groundY - 4, 2);
        bresenhamLine(g2, x + dir * 46, groundY - 8, x + dir * 46, groundY, 2);
        fillMidpointCircle(g2, x + dir * 38, groundY - 4, 2, new Color(40, 40, 40));

        drawScarf(g2, x, shoulderY, faceRight, scarfColor, cryTime);

        bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, x - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, x, headY, headR);

        int eyeLeftX = x - 5, eyeRightX = x + 5, eyeY = headY - 4;
        bezierCurve(g2, eyeLeftX - 4, eyeY - 4, eyeLeftX, eyeY, eyeLeftX, eyeY, eyeLeftX - 4, eyeY + 4);
        bezierCurve(g2, eyeRightX + 4, eyeY - 4, eyeRightX, eyeY, eyeRightX, eyeY, eyeRightX + 4, eyeY + 4);

        fillMidpointEllipse(g2, x, headY + 7, 7, 8, INK);
        g2.setColor(new Color(230, 80, 100));
        fillMidpointEllipse(g2, x, headY + 9, 5, 4, new Color(230, 80, 100));

        g2.setColor(new Color(80, 190, 255));
        bezierCurve(g2, eyeLeftX, eyeY,
                    eyeLeftX - 22, eyeY - 26,
                    eyeLeftX - 38, groundY - 30,
                    eyeLeftX - 48, groundY - 4, 18);
        bezierCurve(g2, eyeLeftX, eyeY + 2,
                    eyeLeftX - 18, eyeY - 20,
                    eyeLeftX - 32, groundY - 24,
                    eyeLeftX - 42, groundY - 4, 18);

        bezierCurve(g2, eyeRightX, eyeY,
                    eyeRightX + 22, eyeY - 26,
                    eyeRightX + 38, groundY - 30,
                    eyeRightX + 48, groundY - 4, 18);
        bezierCurve(g2, eyeRightX, eyeY + 2,
                    eyeRightX + 18, eyeY - 20,
                    eyeRightX + 32, groundY - 24,
                    eyeRightX + 42, groundY - 4, 18);

        fillMidpointCircle(g2, eyeLeftX - 46, groundY - 3, 3 + (int)(sob * 0.5), new Color(80, 190, 255, 180));
        fillMidpointCircle(g2, eyeRightX + 46, groundY - 3, 3 + (int)(sob * 0.5), new Color(80, 190, 255, 180));
        fillMidpointCircle(g2, eyeLeftX - 28, eyeY - 14, 2, new Color(140, 220, 255));
        fillMidpointCircle(g2, eyeRightX + 28, eyeY - 14, 2, new Color(140, 220, 255));
    }

    private void drawPanickingFriend(Graphics2D g2, int x, int groundY, boolean faceRight,
                                     Color swordAura, Color scarfColor, double panicTime) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;

        double tremble = Math.sin(panicTime * 20.0) * 2;
        int hipY = groundY - 50;
        int shoulderY = groundY - 90;
        int headY = groundY - 114;
        int torsoShX = (int) (x + dir * 14 + tremble);

        g2.setColor(INK);
        bresenhamLine(g2, torsoShX, shoulderY, x - dir * 4, hipY, t2);

        int f1x = x - dir * 16, f1y = groundY - 4;
        int f2x = x + dir * 18, f2y = groundY - 4;
        bresenhamLine(g2, x - dir * 4, hipY, x - dir * 8, groundY - 22, t2);
        bresenhamLine(g2, x - dir * 8, groundY - 22, f1x, f1y, t2);
        bresenhamLine(g2, x - dir * 4, hipY, x + dir * 10, groundY - 22, t2);
        bresenhamLine(g2, x + dir * 10, groundY - 22, f2x, f2y, t2);
        fillEllipse(g2, f1x - 6, f1y - 4, 16, 8);
        fillEllipse(g2, f2x - 6, f2y - 4, 16, 8);

        double waveL = Math.sin(panicTime * 14.0) * 18;
        double waveR = Math.cos(panicTime * 14.0) * 18;

        int hand1X = torsoShX + dir * 26;
        int hand1Y = (int) (shoulderY + 6 + waveL);
        int hand2X = torsoShX + dir * 36;
        int hand2Y = (int) (shoulderY + 6 - waveR);

        bresenhamLine(g2, torsoShX, shoulderY + 4, hand1X, hand1Y, t2);
        bresenhamLine(g2, torsoShX, shoulderY + 4, hand2X, hand2Y, t2);
        fillMidpointCircle(g2, hand1X, hand1Y, 3, INK);
        fillMidpointCircle(g2, hand2X, hand2Y, 3, INK);

        g2.setColor(new Color(0, 180, 240));
        bresenhamLine(g2, x - dir * 18, groundY - 4, x - dir * 54, groundY - 4, 2);
        bresenhamLine(g2, x - dir * 26, groundY - 8, x - dir * 26, groundY, 2);
        fillMidpointCircle(g2, x - dir * 18, groundY - 4, 2, new Color(40, 40, 40));

        drawScarf(g2, torsoShX, shoulderY, faceRight, scarfColor, panicTime);

        int headX = torsoShX + dir * 6;
        bresenhamLine(g2, headX, headY + headR, torsoShX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        int ex = headX + dir * 3;
        midpointCircle(g2, ex - 6, headY - 4, 4);
        midpointCircle(g2, ex + 4, headY - 4, 4);
        fillMidpointCircle(g2, ex - 6, headY - 4, 2, INK);
        fillMidpointCircle(g2, ex + 4, headY - 4, 2, INK);

        bezierCurve(g2, ex - 7, headY + 7, ex - 3, headY + 3, ex + 2, headY + 9, ex + 6, headY + 5);

        int swx = headX - dir * 14;
        int swy = headY - 6 + (int)(Math.sin(panicTime * 8) * 3);
        g2.setColor(new Color(80, 200, 255));
        fillMidpointCircle(g2, swx, swy + 4, 4, new Color(80, 200, 255));
        bezierCurve(g2, swx - 3, swy + 2, swx - 1, swy - 6, swx + 1, swy - 6, swx + 3, swy + 2);
    }

    private void drawSwordFightScene(Graphics2D g2, double st) {
        final int groundY = 480;

        double shake = 0;
        if (st >= 1.4 && st < 3.0) {
            double beat = ((st - 1.4) * 4.0) % 1.0;
            if (beat < 0.35) shake = (1.0 - beat / 0.35) * 6.0;
        } else if (st >= 3.0 && st < 3.5) {
            double s = (st - 3.0) / 0.5;
            shake = (1.0 - s) * 12.0;
        }
        int sx = (int) (Math.sin(st * 110) * shake);
        int sy = (int) (Math.cos(st * 95) * shake);

        AffineTransform steady = g2.getTransform();
        g2.translate(sx, sy);

        if (swordBackdrop == null) swordBackdrop = buildSwordBackdrop();
        g2.drawImage(swordBackdrop, 0, 0, null);
        drawSwordEmbers(g2, st);

        Color cyanAura = new Color(0, 220, 255);
        Color redAura = new Color(255, 60, 40);
        Color blueScarf = new Color(0, 180, 255);
        Color redScarf = new Color(230, 40, 60);

        if (st < 1.4) {
            if (st < 0.6) {
                drawSwordStance(g2, 100, groundY, true, cyanAura, blueScarf, st);
                drawSwordStance(g2, 500, groundY, false, redAura, redScarf, st);

                int chargeAlpha = (int) (100 + 100 * Math.sin(st * 16));
                fillMidpointCircle(g2, 120, groundY - 90, 8 + (int)(Math.sin(st * 12)*3), new Color(0, 220, 255, chargeAlpha / 2));
                fillMidpointCircle(g2, 480, groundY - 90, 8 + (int)(Math.sin(st * 12)*3), new Color(255, 60, 40, chargeAlpha / 2));
            } else {
                double du = (st - 0.6) / 0.8;
                int p1x = (int) (100 + 140 * du);
                int p2x = (int) (500 - 140 * du);
                double runPhase = (st - 0.6) * 6.5;

                drawSpeedLines(g2, p1x, groundY - 60, 0.85, 12, 1);
                drawSpeedLines(g2, p2x, groundY - 60, 0.85, 12, 2);

                drawSwordDashRunner(g2, p1x, groundY, true, cyanAura, blueScarf, runPhase, st);
                drawSwordDashRunner(g2, p2x, groundY, false, redAura, redScarf, runPhase, st);
            }

        } else if (st < 3.0) {
            double clashTime = st - 1.4;
            int clashBeat = (int) (clashTime * 4.0);
            double strikeProgress = (clashTime * 4.0) % 1.0;

            int p1x = 230 + (int) (Math.sin(st * 20) * 14);
            int p2x = 370 - (int) (Math.sin(st * 20) * 14);

            drawSwordSlashAttacker(g2, p1x, groundY, true, cyanAura, blueScarf, strikeProgress, clashBeat, st);
            drawSwordSlashAttacker(g2, p2x, groundY, false, redAura, redScarf, strikeProgress, clashBeat + 1, st);

            int clashX = 300 + (clashBeat % 3 - 1) * 20;
            int clashY = 390 + (clashBeat % 2) * 26;

            drawImpactBurst(g2, clashX, clashY, strikeProgress);
            drawSpeedLines(g2, clashX, clashY, 0.8, 14, clashBeat);

            Random sparkRand = new Random(clashBeat * 997);
            for (int sp = 0; sp < 8; sp++) {
                double spAngle = sparkRand.nextDouble() * Math.PI * 2;
                double spDist = 15 + strikeProgress * 70;
                int spx = clashX + (int) (Math.cos(spAngle) * spDist);
                int spy = clashY + (int) (Math.sin(spAngle) * spDist);
                fillMidpointCircle(g2, spx, spy, 2, new Color(255, 230, 120, (int)(255 * (1 - strikeProgress))));
            }

        } else if (st < 4.2) {
            double fallTime = (st - 3.0) / 1.2;

            drawSwordSlashAttacker(g2, 240, groundY, true, cyanAura, blueScarf, Math.min(1.0, fallTime * 2.0), 0, st);

            drawHitAndFallKid(g2, 370, groundY, false, redScarf, fallTime, st);

            if (st < 3.6) {
                double bonkProg = (st - 3.0) / 0.6;
                drawImpactBurst(g2, 350, groundY - 110, bonkProg);

                g2.setFont(new Font("Impact", Font.BOLD, 26));
                g2.setColor(new Color(20, 20, 20));
                g2.drawString("BONK!", 333, groundY - 128);
                g2.drawString("BONK!", 337, groundY - 128);
                g2.setColor(new Color(255, 220, 50));
                g2.drawString("BONK!", 335, groundY - 128);
            }

        } else {
            double cryTime = st - 4.2;

            drawCryingKid(g2, 400, groundY, false, redScarf, cryTime);

            drawPanickingFriend(g2, 230, groundY, true, cyanAura, blueScarf, cryTime);
        }

        g2.setTransform(steady);

        double flashBonk = Math.abs(st - 3.02);
        if (flashBonk < 0.12) {
            int a = (int) (180 * (1 - flashBonk / 0.12));
            g2.setColor(new Color(255, 255, 255, a));
            fillRectangle(g2, 0, 0, 600, 600);
        }
    }

    // SCENE 6 (MEMORY 5): COZY MOO KRATHA (THAI BBQ) DINNER AT HOME WITH FAMILY

    private BufferedImage mooKrathaBackdrop;

    private BufferedImage buildMooKrathaBackdrop() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = img.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        LinearGradientPaint wallGrad = new LinearGradientPaint(
            new Point2D.Float(300, 0), new Point2D.Float(300, 390),
            new float[]{0.0f, 0.40f, 0.80f, 1.0f},
            new Color[]{
                new Color(255, 248, 232),
                new Color(250, 226, 185),
                new Color(232, 182, 126),
                new Color(180, 122, 72)
            }
        );
        bg.setPaint(wallGrad);
        fillRectangle(bg, 0, 0, 600, 390);

        LinearGradientPaint floorGrad = new LinearGradientPaint(
            new Point2D.Float(300, 385), new Point2D.Float(300, 600),
            new float[]{0.0f, 0.4f, 1.0f},
            new Color[]{
                new Color(92, 50, 26),
                new Color(66, 34, 16),
                new Color(38, 18, 8)
            }
        );
        bg.setPaint(floorGrad);
        fillRectangle(bg, 0, 385, 600, 215);

        bg.setColor(new Color(115, 65, 34));
        fillRectangle(bg, 0, 376, 600, 10);
        bg.setColor(new Color(160, 95, 52));
        bresenhamLine(bg, 0, 376, 600, 376, 0);

        int frX = -10, frY = 85, frW = 85, frH = 295;
        bg.setColor(new Color(215, 222, 230));
        fillRoundedRectangle(bg, frX, frY, frW, frH, 12, 12);
        bg.setColor(new Color(175, 185, 198));
        drawRoundedRectangle(bg, frX, frY, frW, frH, 12, 12);
        bresenhamLine(bg, frX, frY + 110, frX + frW, frY + 110, 1);
        bg.setColor(new Color(140, 150, 165));
        fillRoundedRectangle(bg, frX + frW - 14, frY + 65, 6, 32, 2, 2);
        fillRoundedRectangle(bg, frX + frW - 14, frY + 125, 6, 45, 2, 2);

        fillMidpointCircle(bg, frX + 50, frY + 38, 9, new Color(65, 140, 230));
        fillMidpointCircle(bg, frX + 43, frY + 30, 4, new Color(65, 140, 230));
        fillMidpointCircle(bg, frX + 57, frY + 30, 4, new Color(65, 140, 230));
        fillMidpointCircle(bg, frX + 50, frY + 40, 3, Color.WHITE);
        bg.setColor(new Color(255, 245, 120));
        fillRectangle(bg, frX + 22, frY + 68, 38, 38);
        bg.setColor(new Color(60, 45, 15));
        bg.setFont(new Font("Tahoma", Font.BOLD, 8));
        bg.drawString("GOOD", frX + 26, frY + 84);
        bg.drawString("LUCK!", frX + 26, frY + 98);
        fillMidpointCircle(bg, frX + 26, frY + 34, 6, new Color(230, 45, 45));

        bg.setColor(new Color(105, 58, 28));
        int[] stairBaseX = {220, 345, 345, 220};
        int[] stairBaseY = {0, 0, 320, 320};
        fillPolygonScanline(bg, stairBaseX, stairBaseY, 4);

        for (int step = 0; step < 9; step++) {
            int sy = 320 - step * 36;
            int sx1 = 220 + step * 12;
            int sx2 = 345;
            bg.setColor(new Color(152, 92, 48));
            fillRectangle(bg, sx1, sy, sx2 - sx1, 9);
            bg.setColor(new Color(85, 45, 20));
            fillRectangle(bg, sx1, sy + 9, sx2 - sx1, 27);
            bg.setColor(new Color(130, 75, 36));
            bresenhamLine(bg, sx1 + 6, sy, sx1 + 6, sy - 40, 1);
        }
        bg.setColor(new Color(175, 105, 55));
        bresenhamLine(bg, 226, 320 - 40, 332, -10, 2);

        drawWallPhotoFrame(bg, 82, 35, 42, 50, new Color(135, 78, 38), new Color(245, 230, 210), 1);
        drawWallPhotoFrame(bg, 132, 40, 35, 42, new Color(110, 60, 28), new Color(240, 225, 205), 2);
        drawWallPhotoFrame(bg, 370, 42, 46, 36, new Color(125, 70, 32), new Color(225, 240, 250), 3);
        drawWallPhotoFrame(bg, 510, 15, 62, 50, new Color(145, 85, 42), new Color(250, 235, 215), 4);

        int noteX = 175, noteY = 22, noteW = 72, noteH = 78;
        bg.setColor(new Color(255, 255, 252));
        fillRoundedRectangle(bg, noteX, noteY, noteW, noteH, 6, 6);
        bg.setColor(new Color(210, 205, 195));
        drawRoundedRectangle(bg, noteX, noteY, noteW, noteH, 6, 6);

        fillMidpointCircle(bg, noteX + noteW / 2, noteY + 6, 3, new Color(225, 45, 45));

        bg.setFont(new Font("Tahoma", Font.BOLD, 13));
        bg.setColor(new Color(25, 75, 150));
        bg.drawString("สวัสดีครับ", noteX + 16, noteY + 36);
        bg.setColor(new Color(205, 35, 35));
        bg.drawString("อาจารย์โม🙌♥", noteX + 10, noteY + 58);

        int shelfX = 460, shelfY = 0, shelfW = 140, shelfH = 345;
        bg.setColor(new Color(92, 50, 24));
        fillRectangle(bg, shelfX, shelfY, shelfW, shelfH);
        bg.setColor(new Color(125, 70, 34));
        fillRectangle(bg, shelfX + 8, shelfY, shelfW - 16, shelfH);

        for (int sh = 1; sh <= 3; sh++) {
            int shy = sh * 95;
            bg.setColor(new Color(75, 38, 18));
            fillRectangle(bg, shelfX + 8, shy, shelfW - 16, 12);
            Color[] bookColors = {new Color(165, 45, 40), new Color(42, 110, 75), new Color(35, 75, 140), new Color(210, 155, 45)};
            for (int b = 0; b < 6; b++) {
                int bx = shelfX + 22 + b * 16;
                int bh = 45 + (b % 3) * 12;
                bg.setColor(bookColors[b % 4]);
                fillRectangle(bg, bx, shy - bh, 13, bh);
                bg.setColor(new Color(245, 235, 215));
                bresenhamLine(bg, bx + 2, shy - bh + 6, bx + 11, shy - bh + 6, 0);
            }
        }
        int potX = shelfX + 45, potY = 95;
        bg.setColor(new Color(230, 225, 215));
        int[] potPx = {potX - 10, potX + 10, potX + 8, potX - 8};
        int[] potPy = {potY - 24, potY - 24, potY, potY};
        fillPolygonScanline(bg, potPx, potPy, 4);
        bg.setColor(new Color(55, 145, 65));
        for (int lv = 0; lv < 5; lv++) {
            bezierCurve(bg, potX, potY - 24, potX - 18 + lv * 9, potY - 38 + (lv % 2) * 8,
                        potX - 22 + lv * 11, potY - 14, potX - 15 + lv * 8, potY - 2, 1);
        }

        bg.setColor(new Color(40, 35, 30));
        bresenhamLine(bg, 300, 0, 300, 75, 1);
        bg.setColor(new Color(75, 68, 60));
        fillMidpointEllipse(bg, 300, 78, 38, 12, new Color(75, 68, 60));
        bg.setColor(new Color(245, 220, 130));
        fillMidpointEllipse(bg, 300, 84, 32, 8, new Color(255, 235, 150));
        fillMidpointCircle(bg, 300, 84, 9, new Color(255, 255, 220));

        LinearGradientPaint tableTop = new LinearGradientPaint(
            new Point2D.Float(300, 360), new Point2D.Float(300, 595),
            new float[]{0.0f, 0.35f, 0.75f, 1.0f},
            new Color[]{
                new Color(168, 98, 48),
                new Color(138, 78, 38),
                new Color(108, 55, 24),
                new Color(72, 34, 14)
            }
        );
        bg.setPaint(tableTop);
        fillRoundedRectangle(bg, 10, 360, 580, 235, 75, 75);

        bg.setColor(new Color(215, 140, 80));
        bg.setStroke(new BasicStroke(3.0f));
        drawRoundedRectangle(bg, 10, 360, 580, 235, 75, 75);

        bg.setColor(new Color(40, 18, 6, 120));
        fillMidpointEllipse(bg, 300, 480, 275, 88, new Color(40, 18, 6, 120));

        bg.dispose();
        return img;
    }

    private static void drawWallPhotoFrame(Graphics2D g, int x, int y, int w, int h, Color frameCol, Color photoBg, int type) {
        g.setColor(frameCol);
        fillRectangle(g, x, y, w, h);
        g.setColor(new Color(25, 15, 10, 180));
        drawRectangle(g, x, y, w, h);
        g.setColor(photoBg);
        fillRectangle(g, x + 3, y + 3, w - 6, h - 6);

        g.setColor(new Color(100, 85, 75));
        if (type == 1) {
            fillMidpointCircle(g, x + w / 3, y + h / 2 - 4, 4, new Color(100, 85, 75));
            fillMidpointCircle(g, x + 2 * w / 3, y + h / 2 - 2, 4, new Color(100, 85, 75));
            fillMidpointEllipse(g, x + w / 2, y + h - 6, w / 3, 6, new Color(100, 85, 75));
        } else if (type == 2) {
            fillMidpointCircle(g, x + 8, y + 16, 3, new Color(85, 70, 60));
            fillMidpointCircle(g, x + w / 2, y + 14, 4, new Color(85, 70, 60));
            fillMidpointCircle(g, x + w - 8, y + 16, 3, new Color(85, 70, 60));
        } else if (type == 3) {
            g.setColor(new Color(245, 160, 90));
            fillMidpointCircle(g, x + w / 2, y + h / 2, 6, new Color(245, 160, 90));
            g.setColor(new Color(80, 120, 70));
            fillEllipse(g, x + 3, y + h / 2, w - 6, h / 2);
        } else {
            for (int i = 0; i < 4; i++) {
                fillMidpointCircle(g, x + 10 + i * 14, y + 18, 4, new Color(90, 75, 65));
            }
        }
    }

    private void drawMooKrathaPot(Graphics2D g2, int potX, int potY, double st) {
        g2.setColor(new Color(45, 42, 40));
        fillMidpointEllipse(g2, potX, potY + 44, 110, 26, new Color(45, 42, 40));
        g2.setColor(new Color(65, 62, 58));
        fillRectangle(g2, potX - 105, potY + 22, 210, 22);
        fillMidpointEllipse(g2, potX, potY + 22, 105, 22, new Color(65, 62, 58));

        g2.setColor(new Color(30, 28, 26));
        fillRoundedRectangle(g2, potX - 128, potY + 12, 20, 14, 4, 4);
        fillRoundedRectangle(g2, potX + 108, potY + 12, 20, 14, 4, 4);

        int flameAlpha = (int) (170 + 60 * Math.sin(st * 12.0));
        fillMidpointEllipse(g2, potX, potY + 20, 65, 11, new Color(255, 120, 30, flameAlpha));

        int panW = 124;
        int panH = 60;

        g2.setColor(new Color(145, 155, 165));
        fillMidpointEllipse(g2, potX, potY + 6, panW + 4, panH + 4, new Color(145, 155, 165));
        g2.setColor(new Color(218, 228, 238));
        fillMidpointEllipse(g2, potX, potY, panW, panH, new Color(218, 228, 238));
        g2.setColor(new Color(170, 180, 190));
        midpointEllipse(g2, potX, potY, panW, panH);

        int moatW = 115;
        int moatH = 53;
        fillMidpointEllipse(g2, potX, potY, moatW, moatH, new Color(238, 185, 65, 235));

        g2.setColor(new Color(195, 235, 145));
        fillMidpointEllipse(g2, potX - 70, potY + 8, 24, 13, new Color(195, 235, 145));
        fillMidpointEllipse(g2, potX - 86, potY - 6, 20, 11, new Color(185, 228, 135));
        fillMidpointEllipse(g2, potX + 70, potY + 10, 26, 14, new Color(190, 232, 140));
        fillMidpointEllipse(g2, potX + 86, potY - 4, 20, 12, new Color(180, 225, 130));

        g2.setColor(new Color(52, 142, 58));
        for (int v = 0; v < 7; v++) {
            int vx = potX - 52 + v * 17;
            int vy = potY + 30 - (v % 3) * 4;
            bezierCurve(g2, vx, vy, vx + 6, vy - 6, vx + 12, vy - 4, vx + 16, vy + 2, 1);
        }

        g2.setColor(new Color(250, 248, 235));
        int[] enokiX = {potX - 82, potX - 76, potX - 70, potX - 64, potX + 58, potX + 64, potX + 70, potX + 78};
        for (int ex : enokiX) {
            int ey = potY + (ex < potX ? 12 : 14);
            bresenhamLine(g2, ex, ey, ex + 4, ey - 14, 1);
            fillMidpointCircle(g2, ex + 4, ey - 14, 2, new Color(255, 252, 240));
        }

        g2.setColor(new Color(255, 255, 255, 195));
        for (int n = 0; n < 4; n++) {
            int nx = potX + 25 + n * 12;
            int ny = potY + 20 + (n % 2) * 5;
            bezierCurve(g2, nx, ny, nx + 10, ny - 8, nx + 18, ny + 8, nx + 28, ny - 2, 2);
        }

        int carX = potX - 35, carY = potY + 26;
        fillMidpointCircle(g2, carX, carY, 8, new Color(245, 120, 25));
        fillMidpointCircle(g2, carX, carY, 3, new Color(255, 165, 60));

        for (int b = 0; b < 10; b++) {
            double bPhase = (st * 4.5 + b * 0.65) % (Math.PI * 2);
            int br = (int) (1 + Math.abs(Math.sin(bPhase)) * 3);
            double angle = b * (Math.PI * 2 / 10.0) + Math.sin(st * 2.0) * 0.2;
            int bx = potX + (int) (Math.cos(angle) * (86 + (b % 3) * 6));
            int by = potY + (int) (Math.sin(angle) * (38 + (b % 2) * 5));
            int bAlpha = (int) (180 * Math.sin(bPhase * 0.5));
            if (bAlpha > 20) {
                fillMidpointCircle(g2, bx, by, br, new Color(255, 250, 210, bAlpha));
                fillMidpointCircle(g2, bx - 1, by - 1, Math.max(1, br - 1), new Color(255, 255, 255, bAlpha));
            }
        }

        int domeW = 66;
        int domeH = 36;

        g2.setColor(new Color(165, 140, 100));
        fillMidpointEllipse(g2, potX, potY - 4, domeW + 2, domeH + 2, new Color(165, 140, 100));
        g2.setColor(new Color(208, 178, 128));
        fillMidpointEllipse(g2, potX, potY - 8, domeW, domeH, new Color(208, 178, 128));

        g2.setColor(new Color(110, 85, 55));
        for (int r = 0; r < 12; r++) {
            double rAngle = r * (Math.PI * 2 / 12.0);
            int rx1 = potX + (int) (Math.cos(rAngle) * 8);
            int ry1 = potY - 14 + (int) (Math.sin(rAngle) * 4);
            int rx2 = potX + (int) (Math.cos(rAngle) * domeW);
            int ry2 = potY - 8 + (int) (Math.sin(rAngle) * domeH);
            bresenhamLine(g2, rx1, ry1, rx2, ry2, 1);
        }

        int fatX = potX, fatY = potY - 18;
        g2.setColor(new Color(255, 245, 235));
        fillMidpointEllipse(g2, fatX, fatY, 9, 6, new Color(255, 245, 235));
        g2.setColor(new Color(230, 215, 195));
        drawEllipse(g2, fatX - 9, fatY - 6, 18, 12);
        g2.setColor(new Color(245, 205, 60, 210));
        fillMidpointCircle(g2, fatX - 3, fatY + 6, 2, new Color(245, 205, 60, 210));
        fillMidpointCircle(g2, fatX + 4, fatY + 8, 2, new Color(245, 205, 60, 210));

        double[][] meats = {
            {-34, -6, 24, 12, -25},
            {14, -10, 26, 12, 20},
            {-22, 6, 24, 12, 15},
            {20, 8, 24, 12, -18},
            {-40, -14, 20, 10, -40},
            {34, -14, 22, 11, 35},
            {0, 12, 26, 11, 0}
        };

        for (double[] m : meats) {
            int mx = potX + (int) m[0];
            int my = potY - 8 + (int) m[1];
            int mw = (int) m[2];
            int mh = (int) m[3];

            g2.setColor(new Color(228, 112, 112));
            fillMidpointEllipse(g2, mx, my, mw / 2, mh / 2, new Color(228, 112, 112));

            g2.setColor(new Color(252, 230, 225));
            fillMidpointEllipse(g2, mx, my - 1, mw / 2 - 2, mh / 2 - 3, new Color(252, 230, 225));

            g2.setColor(new Color(125, 48, 18));
            bresenhamLine(g2, mx - mw / 4, my - 2, mx + mw / 4, my - 2, 1);
            bresenhamLine(g2, mx - mw / 4 + 2, my + 2, mx + mw / 4 - 2, my + 2, 1);
        }

        Random sizzleRand = new Random(555);
        for (int s = 0; s < 8; s++) {
            double sProg = (st * 6.0 + s * 0.14) % 1.0;
            int sx = potX - 40 + sizzleRand.nextInt(80);
            int sy = potY - 20 + sizzleRand.nextInt(35) - (int)(sProg * 12);
            int sAlpha = (int) (230 * (1.0 - sProg));
            fillMidpointCircle(g2, sx, sy, 1, new Color(255, 230, 110, sAlpha));
        }
    }

    private void drawMooKrathaSteam(Graphics2D g2, int potX, int potY, double st) {
        for (int i = 0; i < 7; i++) {
            double cycle = (st * 1.6 + i * 0.18) % 1.0;
            double wave1 = Math.sin(st * 2.8 + i * 1.2) * 24;
            double wave2 = Math.cos(st * 2.2 + i * 0.9) * 30;

            int startX = potX - 50 + i * 16;
            int startY = (int) (potY - 12 - cycle * 20);

            int cp1X = (int) (startX + wave1);
            int cp1Y = (int) (startY - 45);
            int cp2X = (int) (startX + wave2);
            int cp2Y = (int) (startY - 100);
            int endX = (int) (startX + wave1 * 1.4);
            int endY = (int) (startY - 165);

            int alpha = (int) (120 * Math.sin(cycle * Math.PI));
            if (alpha > 5) {
                g2.setColor(new Color(255, 248, 235, alpha / 2));
                bezierCurve(g2, startX - 5, startY, cp1X - 9, cp1Y, cp2X - 11, cp2Y, endX - 7, endY, 28);
                bezierCurve(g2, startX + 5, startY, cp1X + 9, cp1Y, cp2X + 11, cp2Y, endX + 7, endY, 28);

                g2.setColor(new Color(255, 252, 245, alpha));
                bezierCurve(g2, startX, startY, cp1X, cp1Y, cp2X, cp2Y, endX, endY, 28);

                int puffX = (cp1X + cp2X) / 2;
                int puffY = (cp1Y + cp2Y) / 2;
                fillMidpointEllipse(g2, puffX, puffY, 9 + (i % 3) * 3, 6 + (i % 2) * 2, new Color(255, 250, 240, alpha / 3));
            }
        }
    }

    private void drawTableFoodDishes(Graphics2D g2, int tableX, int tableY, double st) {
        int tray1X = 110, tray1Y = 515, tray1W = 105, tray1H = 60;
        g2.setColor(new Color(180, 190, 200));
        fillMidpointEllipse(g2, tray1X, tray1Y, tray1W / 2 + 4, tray1H / 2 + 4, new Color(180, 190, 200));
        g2.setColor(new Color(240, 245, 250));
        fillMidpointEllipse(g2, tray1X, tray1Y, tray1W / 2, tray1H / 2, new Color(240, 245, 250));

        for (int p = 0; p < 5; p++) {
            int px = tray1X - 30 + p * 15;
            int py = tray1Y - 10 + (p % 2) * 8;
            fillMidpointEllipse(g2, px, py, 15, 9, new Color(225, 95, 95));
            fillMidpointEllipse(g2, px, py - 1, 11, 5, new Color(255, 220, 215));
            fillMidpointCircle(g2, px + 2, py, 1, Color.WHITE);
        }

        int tray2X = 270, tray2Y = 535, tray2W = 100, tray2H = 52;
        g2.setColor(new Color(170, 180, 190));
        fillMidpointEllipse(g2, tray2X, tray2Y, tray2W / 2 + 3, tray2H / 2 + 3, new Color(170, 180, 190));
        g2.setColor(new Color(210, 218, 225));
        fillMidpointEllipse(g2, tray2X, tray2Y, tray2W / 2, tray2H / 2, new Color(210, 218, 225));

        for (int m = 0; m < 5; m++) {
            int mx = tray2X - 28 + m * 14;
            int my = tray2Y - 8 + (m % 2) * 6;
            fillMidpointEllipse(g2, mx, my, 14, 8, new Color(155, 45, 30));
            fillMidpointCircle(g2, mx - 2, my - 1, 1, Color.WHITE);
            fillMidpointCircle(g2, mx + 3, my + 1, 1, Color.WHITE);
        }

        int tray3X = 425, tray3Y = 530, tray3W = 110, tray3H = 56;
        g2.setColor(new Color(180, 190, 200));
        fillMidpointEllipse(g2, tray3X, tray3Y, tray3W / 2 + 4, tray3H / 2 + 4, new Color(180, 190, 200));
        g2.setColor(new Color(240, 245, 250));
        fillMidpointEllipse(g2, tray3X, tray3Y, tray3W / 2, tray3H / 2, new Color(240, 245, 250));

        for (int b = 0; b < 4; b++) {
            int bx = tray3X - 26 + b * 17;
            int by = tray3Y - 8 + (b % 2) * 7;
            fillMidpointEllipse(g2, bx, by, 17, 9, new Color(220, 85, 85));
            fillMidpointEllipse(g2, bx, by - 2, 14, 4, new Color(255, 225, 220));
        }

        int plateX = 520, plateY = 465;
        g2.setColor(new Color(230, 235, 240));
        fillMidpointEllipse(g2, plateX, plateY, 38, 19, new Color(230, 235, 240));
        g2.setColor(new Color(255, 255, 255));
        fillMidpointEllipse(g2, plateX, plateY - 2, 34, 16, new Color(255, 255, 255));
        g2.setColor(new Color(225, 242, 248));
        for (int w = 0; w < 6; w++) {
            int wx = plateX - 18 + w * 6;
            bezierCurve(g2, wx, plateY + 4, wx + 6, plateY - 8, wx + 12, plateY + 6, wx + 18, plateY - 4, 1);
        }

        drawSauceBowl(g2, 148, 442);
        drawSauceBowl(g2, 388, 452);

        drawIcedDrinkGlass(g2, 60, 435, st);
        drawIcedDrinkGlass(g2, 545, 455, st + 0.5);
    }

    private void drawSauceBowl(Graphics2D g2, int bx, int by) {
        g2.setColor(new Color(220, 225, 230));
        fillMidpointEllipse(g2, bx, by + 2, 18, 11, new Color(220, 225, 230));
        g2.setColor(Color.WHITE);
        fillMidpointEllipse(g2, bx, by, 17, 10, Color.WHITE);

        g2.setColor(new Color(205, 38, 25));
        fillMidpointEllipse(g2, bx, by, 14, 8, new Color(205, 38, 25));

        fillMidpointCircle(g2, bx - 4, by - 1, 1, Color.WHITE);
        fillMidpointCircle(g2, bx + 2, by + 1, 1, Color.WHITE);
        fillMidpointCircle(g2, bx + 5, by - 2, 1, Color.WHITE);
        fillMidpointCircle(g2, bx - 1, by + 2, 1, new Color(55, 160, 60));
    }

    private void drawIcedDrinkGlass(Graphics2D g2, int gx, int gy, double st) {
        int gw = 30, gh = 48;

        g2.setColor(new Color(35, 15, 5, 130));
        fillMidpointEllipse(g2, gx + gw / 2, gy + gh + 1, gw / 2 + 2, 5, new Color(35, 15, 5, 130));

        g2.setColor(new Color(210, 235, 245, 90));
        fillRoundedRectangle(g2, gx, gy, gw, gh, 6, 6);
        g2.setColor(new Color(150, 195, 220, 180));
        drawRoundedRectangle(g2, gx, gy, gw, gh, 6, 6);

        g2.setColor(new Color(55, 22, 10, 230));
        fillRoundedRectangle(g2, gx + 2, gy + 8, gw - 4, gh - 10, 4, 4);

        g2.setColor(new Color(230, 245, 255, 180));
        fillRoundedRectangle(g2, gx + 4, gy + 10, 9, 9, 2, 2);
        fillRoundedRectangle(g2, gx + 16, gy + 12, 10, 9, 2, 2);
        fillRoundedRectangle(g2, gx + 10, gy + 22, 10, 9, 2, 2);

        for (int fb = 0; fb < 4; fb++) {
            int fbx = gx + 6 + fb * 5;
            int fby = gy + 38 - (int)(((st * 8.0 + fb * 10) % 28));
            fillMidpointCircle(g2, fbx, fby, 1, new Color(255, 245, 220, 190));
        }

        int bearX = gx + gw / 2;
        int bearY = gy + gh / 2 + 6;
        fillMidpointCircle(g2, bearX, bearY, 6, new Color(245, 190, 70, 220));
        fillMidpointCircle(g2, bearX - 5, bearY - 5, 2, new Color(245, 190, 70, 220));
        fillMidpointCircle(g2, bearX + 5, bearY - 5, 2, new Color(245, 190, 70, 220));
        fillMidpointCircle(g2, bearX - 2, bearY - 1, 1, Color.BLACK);
        fillMidpointCircle(g2, bearX + 2, bearY - 1, 1, Color.BLACK);
    }

    private void drawMooKrathaFriend1_Grilling(Graphics2D g2, int x, int tableY, double st) {
        int t2 = 2;
        int headR = 17;

        double reach = Math.sin(st * 3.5) * 6.0;
        int hipX = x - 12, hipY = tableY + 22;
        int shoulderX = (int) (x + 18 + reach * 0.4);
        int shoulderY = tableY - 22;
        int headX = shoulderX + 8;
        int headY = shoulderY - 26;

        g2.setColor(INK);
        bresenhamLine(g2, hipX, hipY, hipX - 14, hipY + 36, t2);
        bresenhamLine(g2, hipX - 14, hipY + 36, hipX - 14, hipY + 70, t2);
        fillEllipse(g2, hipX - 20, hipY + 66, 14, 7);

        bresenhamLine(g2, shoulderX, shoulderY, hipX, hipY, t2);

        int handLX = x + 10, handLY = tableY + 8;
        bresenhamLine(g2, shoulderX - 8, shoulderY + 4, handLX, handLY, t2);
        fillMidpointCircle(g2, handLX, handLY, 3, INK);

        int handRX = (int) (x + 72 + reach);
        int handRY = (int) (tableY - 14 + Math.sin(st * 3.5) * 4);
        int elbowRX = (shoulderX + handRX) / 2 + 4;
        int elbowRY = shoulderY + 14;
        bresenhamLine(g2, shoulderX + 6, shoulderY + 2, elbowRX, elbowRY, t2);
        bresenhamLine(g2, elbowRX, elbowRY, handRX, handRY, t2);
        fillMidpointCircle(g2, handRX, handRY, 3, INK);

        g2.setColor(new Color(175, 115, 60));
        int chopTipX = handRX + 36;
        int chopTipY = handRY + 16;
        bresenhamLine(g2, handRX - 10, handRY - 8, chopTipX, chopTipY, 1);
        bresenhamLine(g2, handRX - 8, handRY - 11, chopTipX - 2, chopTipY + 4, 1);

        bresenhamLine(g2, headX, headY + headR, shoulderX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 6, headY - headR, headX - 11, headY - headR - 7, 1);
        bresenhamLine(g2, headX, headY - headR, headX + 2, headY - headR - 9, 1);
        bresenhamLine(g2, headX + 7, headY - headR, headX + 11, headY - headR - 7, 1);

        int ex = headX + 2;
        bezierCurve(g2, ex - 10, headY - 5, ex - 5, headY - 2, ex - 5, headY - 2, ex - 10, headY + 1);
        fillEllipse(g2, ex + 2, headY - 4, 4, 5);
        bezierCurve(g2, headX - 7, headY + 4, headX, headY + 10, headX + 5, headY + 10, headX + 8, headY + 4);
    }

    private void drawMooKrathaFriend2_Eating(Graphics2D g2, int x, int tableY, double st) {
        int t2 = 2;
        int headR = 17;

        double chew = Math.sin(st * 5.0) * 1.8;
        int hipX = x - 2, hipY = tableY + 24;
        int shoulderX = x + 2, shoulderY = tableY - 24;
        int headX = x + 3, headY = (int) (shoulderY - 26 + chew * 0.4);

        g2.setColor(INK);
        bresenhamLine(g2, shoulderX, shoulderY, hipX, hipY, t2);

        int handX = headX + 24;
        int handY = (int) (headY + 6 + chew * 0.6);

        bresenhamLine(g2, shoulderX - 10, shoulderY + 4, handX - 10, handY + 12, t2);
        fillMidpointCircle(g2, handX - 10, handY + 12, 3, INK);

        bresenhamLine(g2, shoulderX + 10, shoulderY + 4, handX, handY, t2);
        fillMidpointCircle(g2, handX, handY, 3, INK);

        g2.setColor(new Color(185, 120, 65));
        int porkX = handX + 18, porkY = handY - 14;
        bresenhamLine(g2, handX - 8, handY + 8, porkX + 8, porkY - 6, 1);
        bresenhamLine(g2, handX - 6, handY + 12, porkX + 6, porkY + 2, 1);

        fillMidpointEllipse(g2, porkX, porkY, 8, 6, new Color(225, 105, 95));
        fillMidpointEllipse(g2, porkX, porkY - 1, 6, 3, new Color(255, 220, 215));
        g2.setColor(new Color(130, 48, 18));
        bresenhamLine(g2, porkX - 4, porkY, porkX + 4, porkY, 0);

        g2.setColor(new Color(255, 250, 235, 170));
        bezierCurve(g2, porkX, porkY - 6, porkX + 4, porkY - 16, porkX - 4, porkY - 24, porkX + 2, porkY - 32, 1);

        bresenhamLine(g2, headX, headY + headR, shoulderX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 6, headY - headR, headX - 9, headY - headR - 8, 1);
        bresenhamLine(g2, headX + 2, headY - headR, headX + 2, headY - headR - 10, 1);
        bresenhamLine(g2, headX + 9, headY - headR + 1, headX + 13, headY - headR - 7, 1);

        g2.setColor(new Color(255, 140, 140, 170));
        fillMidpointEllipse(g2, headX - 9, headY + 2, 4, 3, new Color(255, 140, 140, 170));
        fillMidpointEllipse(g2, headX + 9, headY + 2, 4, 3, new Color(255, 140, 140, 170));

        g2.setColor(INK);
        fillEllipse(g2, headX - 7, headY - 4, 4, 5);
        fillEllipse(g2, headX + 4, headY - 4, 4, 5);
        fillMidpointCircle(g2, headX - 6, headY - 5, 1, Color.WHITE);
        fillMidpointCircle(g2, headX + 5, headY - 5, 1, Color.WHITE);
        fillMidpointEllipse(g2, headX, headY + 6, 4, 5, INK);
    }

    private void drawMooKrathaFriend3_SauceLaugh(Graphics2D g2, int x, int tableY, double st) {
        int t2 = 2;
        int headR = 17;

        double laugh = Math.abs(Math.sin(st * 6.0)) * 2.0;
        int hipX = x + 2, hipY = tableY + 24;
        int shoulderX = x - 2, shoulderY = (int) (tableY - 24 - laugh * 0.5);
        int headX = x - 3, headY = (int) (shoulderY - 26 - laugh);

        g2.setColor(INK);
        bresenhamLine(g2, shoulderX, shoulderY, hipX, hipY, t2);

        int bowlHandX = x - 28, bowlHandY = tableY - 10;
        bresenhamLine(g2, shoulderX - 10, shoulderY + 4, bowlHandX, bowlHandY, t2);
        fillMidpointCircle(g2, bowlHandX, bowlHandY, 3, INK);
        g2.setColor(Color.WHITE);
        fillMidpointEllipse(g2, bowlHandX, bowlHandY - 4, 10, 6, Color.WHITE);
        g2.setColor(new Color(205, 38, 25));
        fillMidpointEllipse(g2, bowlHandX, bowlHandY - 4, 8, 4, new Color(205, 38, 25));

        int chopHandX = x + 26, chopHandY = tableY - 8;
        bresenhamLine(g2, shoulderX + 10, shoulderY + 4, chopHandX, chopHandY, t2);
        fillMidpointCircle(g2, chopHandX, chopHandY, 3, INK);
        g2.setColor(new Color(185, 120, 65));
        bresenhamLine(g2, chopHandX - 6, chopHandY - 6, chopHandX + 18, chopHandY + 12, 1);

        bresenhamLine(g2, headX, headY + headR, shoulderX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 7, headY - headR, headX - 10, headY - headR - 7, 1);
        bresenhamLine(g2, headX + 1, headY - headR, headX + 1, headY - headR - 9, 1);
        bresenhamLine(g2, headX + 8, headY - headR, headX + 12, headY - headR - 7, 1);

        int ey = headY - 3;
        bezierCurve(g2, headX - 9, ey, headX - 6, ey - 4, headX - 2, ey - 4, headX, ey);
        bezierCurve(g2, headX + 2, headY - 3, headX + 6, headY - 7, headX + 9, headY - 7, headX + 11, headY - 3);
        fillMidpointEllipse(g2, headX + 1, headY + 5, 6, 5, INK);
        g2.setColor(new Color(245, 120, 130));
        fillMidpointEllipse(g2, headX + 1, headY + 7, 4, 2, new Color(245, 120, 130));
    }

    private void drawMooKrathaFriend4_DrinkCheer(Graphics2D g2, int x, int tableY, double st) {
        int t2 = 2;
        int headR = 17;

        double toast = Math.sin(st * 4.0) * 5.0;
        int hipX = x + 10, hipY = tableY + 24;
        int shoulderX = x - 6, shoulderY = tableY - 22;
        int headX = shoulderX - 10, headY = tableY - 48;

        g2.setColor(INK);
        bresenhamLine(g2, shoulderX, shoulderY, hipX, hipY, t2);

        int toastHandX = (int) (shoulderX - 32);
        int toastHandY = (int) (shoulderY - 14 + toast);
        bresenhamLine(g2, shoulderX - 6, shoulderY + 2, toastHandX, toastHandY, t2);
        fillMidpointCircle(g2, toastHandX, toastHandY, 3, INK);

        drawIcedDrinkGlass(g2, toastHandX - 15, toastHandY - 24, st);

        int rightHandX = x + 18, rightHandY = tableY + 8;
        bresenhamLine(g2, shoulderX + 8, shoulderY + 4, rightHandX, rightHandY, t2);
        fillMidpointCircle(g2, rightHandX, rightHandY, 3, INK);

        bresenhamLine(g2, headX, headY + headR, shoulderX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 6, headY - headR, headX - 9, headY - headR - 7, 1);
        bresenhamLine(g2, headX + 3, headY - headR, headX + 5, headY - headR - 9, 1);

        fillEllipse(g2, headX - 7, headY - 4, 4, 5);
        fillEllipse(g2, headX + 2, headY - 4, 4, 5);
        bezierCurve(g2, headX - 7, headY + 4, headX, headY + 9, headX + 5, headY + 9, headX + 8, headY + 4);
    }

    private void drawMooKrathaWarmLighting(Graphics2D g2, double st) {
        Point2D lampCenter = new Point2D.Float(300.0f, 120.0f);
        float lampRadius = 460.0f;
        float[] lampDist = {0.0f, 0.45f, 0.85f, 1.0f};
        Color[] lampColors = {
            new Color(255, 235, 175, 75),
            new Color(255, 215, 140, 45),
            new Color(245, 185, 95, 18),
            new Color(240, 160, 70, 0)
        };
        RadialGradientPaint lampGlow = new RadialGradientPaint(lampCenter, lampRadius, lampDist, lampColors);
        g2.setPaint(lampGlow);
        fillRectangle(g2, 0, 0, 600, 600);

        Point2D vigCenter = new Point2D.Float(300.0f, 300.0f);
        float vigRadius = 440.0f;
        float[] vigDist = {0.0f, 0.70f, 1.0f};
        Color[] vigColors = {
            new Color(0, 0, 0, 0),
            new Color(45, 18, 5, 25),
            new Color(28, 10, 3, 110)
        };
        RadialGradientPaint vig = new RadialGradientPaint(vigCenter, vigRadius, vigDist, vigColors);
        g2.setPaint(vig);
        fillRectangle(g2, 0, 0, 600, 600);
    }

    private void drawMooKrathaScene(Graphics2D g2, double st) {
        final int tableY = 380;
        final int potX = 300;
        final int potY = 425;

        if (mooKrathaBackdrop == null) mooKrathaBackdrop = buildMooKrathaBackdrop();
        g2.drawImage(mooKrathaBackdrop, 0, 0, null);

        drawTableFoodDishes(g2, 300, tableY, st);

        drawMooKrathaFriend1_Grilling(g2, 115, tableY, st);
        drawMooKrathaFriend2_Eating(g2, 235, tableY, st);
        drawMooKrathaFriend3_SauceLaugh(g2, 365, tableY, st);
        drawMooKrathaFriend4_DrinkCheer(g2, 485, tableY, st);

        drawMooKrathaPot(g2, potX, potY, st);

        drawMooKrathaSteam(g2, potX, potY, st);

        drawMooKrathaWarmLighting(g2, st);
    }

    // SCENE 5 (MEMORY 4): WATCHING TV AT HOME WITH FRIENDS (ULTRAMAN VS GODZILLA)

    private BufferedImage buildLivingRoomBackdrop() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint wallPaint = new GradientPaint(
            300, 0, new Color(252, 243, 226),
            300, 390, new Color(230, 202, 168)
        );
        g2.setPaint(wallPaint);
        fillRectangle(g2, 0, 0, 600, 390);

        fillMidpointEllipse(g2, 530, 150, 160, 220, new Color(255, 235, 185, 45));

        g2.setColor(new Color(138, 92, 58));
        fillRectangle(g2, 320, 25, 100, 365);
        g2.setColor(new Color(105, 68, 42));
        drawRectangle(g2, 320, 25, 100, 365);
        fillRectangle(g2, 328, 35, 84, 355);
        g2.setColor(new Color(88, 55, 32));
        fillRectangle(g2, 336, 50, 68, 140);
        fillRectangle(g2, 336, 210, 68, 160);
        fillMidpointCircle(g2, 400, 205, 4, new Color(220, 185, 75));
        bresenhamLine(g2, 400, 205, 390, 205, 2);

        fillMidpointCircle(g2, 240, 52, 20, new Color(90, 35, 25));
        fillMidpointCircle(g2, 240, 52, 17, new Color(248, 246, 240));
        g2.setColor(INK);
        bresenhamLine(g2, 240, 52, 240, 42, 2);
        bresenhamLine(g2, 240, 52, 248, 56, 1);
        fillMidpointCircle(g2, 240, 52, 2, new Color(180, 40, 30));

        g2.setColor(new Color(115, 65, 35));
        fillRectangle(g2, 122, 28, 56, 74);
        g2.setColor(new Color(235, 242, 248));
        fillRectangle(g2, 125, 31, 50, 68);
        g2.setColor(new Color(90, 165, 215));
        fillRectangle(g2, 125, 31, 50, 38);
        fillMidpointCircle(g2, 150, 48, 8, new Color(255, 225, 110));
        g2.setColor(new Color(75, 115, 145));
        int[] mtnX = {125, 145, 160, 175, 175, 125};
        int[] mtnY = {69, 44, 56, 69, 69, 69};
        fillPolygonScanline(g2, mtnX, mtnY, 6);
        g2.setColor(new Color(55, 125, 65));
        fillRectangle(g2, 125, 65, 50, 34);
        g2.setColor(new Color(40, 65, 90));
        g2.setFont(new Font("SansSerif", Font.BOLD, 6));
        g2.drawString("ADVENTURE", 128, 38);

        g2.setColor(new Color(145, 95, 55));
        fillRectangle(g2, 470, 65, 80, 7);
        g2.setColor(new Color(110, 68, 35));
        fillRectangle(g2, 475, 72, 8, 8);
        fillRectangle(g2, 535, 72, 8, 8);
        g2.setColor(new Color(175, 45, 35));
        fillRectangle(g2, 480, 40, 26, 25);
        g2.setColor(new Color(248, 235, 220));
        fillRectangle(g2, 482, 42, 22, 21);
        fillMidpointCircle(g2, 490, 50, 4, new Color(130, 85, 55));
        fillMidpointCircle(g2, 498, 51, 3, new Color(130, 85, 55));
        g2.setColor(new Color(205, 115, 80));
        fillRectangle(g2, 525, 50, 14, 15);
        fillMidpointCircle(g2, 532, 46, 7, new Color(55, 145, 65));
        fillMidpointCircle(g2, 528, 42, 5, new Color(75, 175, 85));
        fillMidpointCircle(g2, 536, 43, 5, new Color(45, 125, 55));

        GradientPaint floorPaint = new GradientPaint(
            300, 385, new Color(145, 85, 45),
            300, 600, new Color(85, 45, 20)
        );
        g2.setPaint(floorPaint);
        fillRectangle(g2, 0, 385, 600, 215);

        g2.setColor(new Color(65, 32, 12, 100));
        for (int y = 415; y < 600; y += 32) {
            bresenhamLine(g2, 0, y, 600, y, 1);
        }

        g2.setColor(new Color(236, 225, 204));
        fillRoundedRectangle(g2, 20, 415, 560, 180, 24, 24);
        g2.setColor(new Color(208, 188, 158));
        drawRoundedRectangle(g2, 20, 415, 560, 180, 24, 24);
        g2.setColor(new Color(195, 165, 135, 45));
        drawRoundedRectangle(g2, 26, 421, 548, 168, 20, 20);

        g2.setColor(new Color(40, 20, 10, 110));
        fillRoundedRectangle(g2, 245, 400, 340, 20, 12, 12);
        g2.setColor(new Color(36, 75, 92));
        fillRoundedRectangle(g2, 250, 280, 332, 130, 22, 22);
        g2.setColor(new Color(45, 92, 112));
        fillRoundedRectangle(g2, 254, 238, 104, 100, 18, 18);
        fillRoundedRectangle(g2, 362, 238, 108, 100, 18, 18);
        fillRoundedRectangle(g2, 474, 238, 104, 100, 18, 18);
        g2.setColor(new Color(30, 65, 80));
        fillRoundedRectangle(g2, 258, 330, 102, 78, 14, 14);
        fillRoundedRectangle(g2, 364, 330, 106, 78, 14, 14);
        fillRoundedRectangle(g2, 474, 330, 102, 78, 14, 14);
        g2.setColor(new Color(42, 85, 104));
        fillRoundedRectangle(g2, 242, 288, 24, 110, 14, 14);
        fillRoundedRectangle(g2, 568, 288, 24, 110, 14, 14);
        g2.setColor(new Color(75, 42, 20));
        fillRectangle(g2, 260, 405, 12, 14);
        fillRectangle(g2, 555, 405, 12, 14);

        g2.setColor(new Color(175, 140, 65));
        bresenhamLine(g2, 575, 115, 575, 330, 3);
        fillMidpointEllipse(g2, 575, 330, 16, 5, new Color(135, 105, 45));
        g2.setColor(new Color(255, 248, 225));
        int[] shadeX = {558, 592, 584, 566};
        int[] shadeY = {115, 115, 68, 68};
        fillPolygonScanline(g2, shadeX, shadeY, 4);
        g2.setColor(new Color(215, 185, 125));
        drawPolygonLines(g2, shadeX, shadeY, 4);

        g2.setColor(new Color(40, 20, 10, 110));
        fillRoundedRectangle(g2, 2, 430, 192, 16, 8, 8);
        g2.setColor(new Color(110, 65, 35));
        fillRoundedRectangle(g2, 5, 330, 185, 104, 12, 12);
        g2.setColor(new Color(135, 82, 45));
        fillRectangle(g2, 8, 333, 179, 12);
        g2.setColor(new Color(55, 32, 18));
        fillRectangle(g2, 15, 352, 75, 32);
        fillRectangle(g2, 98, 352, 82, 32);
        fillRectangle(g2, 15, 390, 165, 36);
        g2.setColor(new Color(30, 32, 38));
        fillRoundedRectangle(g2, 20, 362, 65, 16, 4, 4);
        fillMidpointCircle(g2, 78, 370, 2, new Color(45, 225, 95));
        Color[] spineColors = {new Color(195, 45, 45), new Color(45, 125, 195), new Color(225, 175, 45), new Color(65, 165, 85)};
        for (int i = 0; i < 4; i++) {
            g2.setColor(spineColors[i]);
            fillRectangle(g2, 25 + i * 18, 396, 14, 26);
        }

        g2.dispose();
        return img;
    }

    private void drawTVScreenBattle(Graphics2D g2, int tvX, int tvY, int tvW, int tvH, double st) {
        g2.setColor(new Color(35, 38, 44));
        fillRoundedRectangle(g2, tvX + tvW / 2 - 28, tvY + tvH - 4, 56, 10, 4, 4);
        bresenhamLine(g2, tvX + tvW / 2, tvY + tvH - 12, tvX + tvW / 2, tvY + tvH - 2, 8);

        g2.setColor(new Color(22, 24, 28));
        fillRoundedRectangle(g2, tvX, tvY, tvW, tvH - 10, 10, 10);
        g2.setColor(new Color(55, 58, 66));
        drawRoundedRectangle(g2, tvX, tvY, tvW, tvH - 10, 10, 10);

        int scrX = tvX + 8;
        int scrY = tvY + 8;
        int scrW = tvW - 16;
        int scrH = tvH - 30;

        Shape oldClip = g2.getClip();
        g2.clipRect(scrX, scrY, scrW, scrH);

        GradientPaint tvSky = new GradientPaint(
            scrX, scrY, new Color(40, 120, 220),
            scrX, scrY + scrH, new Color(135, 195, 255)
        );
        g2.setPaint(tvSky);
        fillRectangle(g2, scrX, scrY, scrW, scrH);

        double cloudDrift = (st * 12.0) % (scrW + 40);
        g2.setColor(new Color(255, 255, 255, 160));
        fillMidpointEllipse(g2, (int) (scrX + scrW - cloudDrift), scrY + 22, 20, 9, new Color(255, 255, 255, 160));
        fillMidpointEllipse(g2, (int) (scrX + scrW - cloudDrift + 14), scrY + 24, 14, 7, new Color(255, 255, 255, 160));

        int groundY = scrY + scrH - 18;
        g2.setColor(new Color(52, 65, 88));
        int[] bldH = {38, 54, 42, 65, 48, 58, 36};
        int[] bldW = {18, 22, 16, 26, 20, 24, 20};
        int bx = scrX + 2;
        for (int i = 0; i < bldH.length && bx < scrX + scrW; i++) {
            fillRectangle(g2, bx, groundY - bldH[i], bldW[i], bldH[i]);
            g2.setColor(new Color(255, 235, 120, 180));
            for (int wy = groundY - bldH[i] + 4; wy < groundY - 6; wy += 8) {
                for (int wx = bx + 3; wx < bx + bldW[i] - 3; wx += 6) {
                    if (((i + wx + wy) % 3) != 0) fillRectangle(g2, wx, wy, 3, 4);
                }
            }
            g2.setColor(new Color(52, 65, 88));
            bx += bldW[i] + 2;
        }

        g2.setColor(new Color(60, 50, 42));
        fillRectangle(g2, scrX, groundY, scrW, 20);

        int gx = scrX + scrW - 38;
        int gy = groundY - 8;
        double stompHop = Math.sin(st * 8.0) * 2.0;

        g2.setColor(new Color(25, 42, 30));
        bezierCurve(g2, gx + 12, gy + 4, gx + 30, (int) (gy + stompHop), gx + 36, gy - 8, gx + 42, gy - 16, 5);

        fillEllipse(g2, gx - 4, gy + 4, 16, 12);
        fillEllipse(g2, gx + 8, gy + 2, 14, 14);

        fillEllipse(g2, gx - 12, (int) (gy - 38 + stompHop), 32, 42);
        g2.setColor(new Color(40, 68, 48));
        fillEllipse(g2, gx - 8, (int) (gy - 32 + stompHop), 18, 30);

        g2.setColor(new Color(110, 60, 240));
        int[] spineOffsetY = {-36, -26, -16, -6, 2};
        for (int spy : spineOffsetY) {
            int sx = gx + 14;
            int sy = (int) (gy + spy + stompHop);
            int[] px = {sx, sx + 7, sx + 2};
            int[] py = {sy - 4, sy, sy + 4};
            fillPolygonScanline(g2, px, py, 3);
        }

        int mHeadX = gx - 14;
        int mHeadY = (int) (gy - 44 + stompHop);
        g2.setColor(new Color(25, 42, 30));
        fillEllipse(g2, mHeadX, mHeadY, 20, 16);
        fillRectangle(g2, mHeadX - 8, mHeadY + 2, 12, 6);
        fillRectangle(g2, mHeadX - 6, mHeadY + 11, 10, 4);
        g2.setColor(Color.WHITE);
        fillRectangle(g2, mHeadX - 7, mHeadY + 7, 2, 2);
        fillRectangle(g2, mHeadX - 4, mHeadY + 7, 2, 2);
        fillMidpointCircle(g2, mHeadX + 5, mHeadY + 4, 2, new Color(255, 30, 30));

        int breathStartX = mHeadX - 8;
        int breathStartY = mHeadY + 8;
        int clashX = scrX + scrW / 2 - 4;
        int clashY = groundY - 26;

        g2.setColor(new Color(255, 60, 20, 210));
        bezierCurve(g2, breathStartX, breathStartY, clashX + 25, breathStartY - 6, clashX + 15, clashY + 6, clashX, clashY, 6);
        g2.setColor(new Color(255, 175, 30, 240));
        bezierCurve(g2, breathStartX, breathStartY, clashX + 20, breathStartY - 3, clashX + 10, clashY + 3, clashX, clashY, 3);
        g2.setColor(new Color(255, 255, 180));
        bresenhamLine(g2, breathStartX, breathStartY, clashX, clashY, 1);

        int ux = scrX + 32;
        int uy = groundY - 2;
        double ultraStance = Math.sin(st * 4.0) * 1.5;

        g2.setColor(new Color(220, 225, 235));
        bresenhamLine(g2, ux, uy - 18, ux - 14, uy, 2);
        bresenhamLine(g2, ux, uy - 18, ux + 10, uy, 2);
        g2.setColor(new Color(225, 35, 45));
        bresenhamLine(g2, ux - 14, uy - 4, ux - 8, uy, 2);
        bresenhamLine(g2, ux + 6, uy - 4, ux + 10, uy, 2);

        g2.setColor(new Color(220, 225, 235));
        bresenhamLine(g2, ux, uy - 18, ux, (int) (uy - 36 + ultraStance), 3);
        g2.setColor(new Color(225, 35, 45));
        fillMidpointEllipse(g2, ux, (int) (uy - 28 + ultraStance), 6, 8, new Color(225, 35, 45));

        Color timerColor = ((int) (st * 10) % 2 == 0) ? new Color(0, 235, 255) : new Color(255, 45, 55);
        fillMidpointCircle(g2, ux, (int) (uy - 30 + ultraStance), 3, timerColor);

        int uHeadX = ux;
        int uHeadY = (int) (uy - 46 + ultraStance);
        g2.setColor(new Color(230, 235, 245));
        fillMidpointCircle(g2, uHeadX, uHeadY, 7, new Color(230, 235, 245));
        g2.setColor(new Color(210, 218, 230));
        int[] finX = {uHeadX - 2, uHeadX + 5, uHeadX + 1};
        int[] finY = {uHeadY - 7, uHeadY - 14, uHeadY - 7};
        fillPolygonScanline(g2, finX, finY, 3);
        fillMidpointEllipse(g2, uHeadX + 3, uHeadY - 1, 3, 2, new Color(255, 235, 50));

        int handX = ux + 14;
        int handY = (int) (uy - 32 + ultraStance);
        g2.setColor(new Color(225, 35, 45));
        bresenhamLine(g2, ux - 4, (int) (uy - 34 + ultraStance), handX + 4, handY, 2);
        bresenhamLine(g2, handX, handY + 10, handX, handY - 12, 2);

        g2.setColor(new Color(0, 215, 255, 220));
        bezierCurve(g2, handX, handY - 2, clashX - 20, handY - 10, clashX - 10, clashY - 2, clashX, clashY, 6);
        g2.setColor(new Color(160, 245, 255, 250));
        bezierCurve(g2, handX, handY - 2, clashX - 15, handY - 6, clashX - 5, clashY - 1, clashX, clashY, 3);
        g2.setColor(Color.WHITE);
        bresenhamLine(g2, handX, handY - 2, clashX, clashY, 1);

        g2.setColor(new Color(0, 235, 255, 180));
        for (int rx = handX + 10; rx < clashX; rx += 14) {
            fillMidpointEllipse(g2, rx, handY - 2 + (clashY - handY + 2) * (rx - handX) / (clashX - handX + 1), 3, 7, new Color(0, 235, 255, 150));
        }

        double clashPulse = Math.sin(st * 25.0) * 3.0;
        fillMidpointCircle(g2, clashX, clashY, (int) (8 + clashPulse), new Color(255, 255, 255, 240));
        fillMidpointCircle(g2, clashX, clashY, (int) (14 + clashPulse), new Color(255, 230, 80, 160));
        g2.setColor(new Color(255, 255, 200));
        bresenhamLine(g2, clashX - 14, clashY, clashX + 14, clashY, 1);
        bresenhamLine(g2, clashX, clashY - 14, clashX, clashY + 14, 1);
        bresenhamLine(g2, clashX - 10, clashY - 10, clashX + 10, clashY + 10, 1);
        bresenhamLine(g2, clashX - 10, clashY + 10, clashX + 10, clashY - 10, 1);

        g2.setColor(new Color(255, 255, 255, 24));
        int[] glareX = {scrX, scrX + 35, scrX + 12, scrX};
        int[] glareY = {scrY, scrY, scrY + scrH, scrY + scrH};
        fillPolygonScanline(g2, glareX, glareY, 4);

        g2.setClip(oldClip);

        fillMidpointCircle(g2, tvX + tvW - 14, tvY + tvH - 18, 2, new Color(45, 220, 255));
    }

    private void drawCoffeeTableAndSnacks(Graphics2D g2, int tableX, int tableY, double st) {
        int tblW = 200;
        int tblH = 88;
        g2.setColor(new Color(40, 20, 10, 120));
        fillRoundedRectangle(g2, tableX - 8, tableY + tblH - 10, tblW + 16, 20, 16, 16);

        g2.setColor(new Color(135, 78, 38));
        fillRoundedRectangle(g2, tableX, tableY, tblW, 24, 10, 10);
        g2.setColor(new Color(165, 102, 54));
        fillRectangle(g2, tableX + 4, tableY + 2, tblW - 8, 8);

        g2.setColor(new Color(95, 52, 24));
        fillRectangle(g2, tableX + 8, tableY + 24, 14, tblH - 24);
        fillRectangle(g2, tableX + tblW - 22, tableY + 24, 14, tblH - 24);
        g2.setColor(new Color(115, 65, 32));
        fillRectangle(g2, tableX + 18, tableY + 58, tblW - 36, 10);
        g2.setColor(new Color(215, 55, 55));
        fillRectangle(g2, tableX + 28, tableY + 52, 38, 6);
        g2.setColor(new Color(45, 145, 215));
        fillRectangle(g2, tableX + 75, tableY + 54, 44, 4);

        int bowlX = tableX + 85;
        int bowlY = tableY - 14;
        g2.setColor(new Color(50, 25, 12, 100));
        fillMidpointEllipse(g2, bowlX, bowlY + 26, 34, 8, new Color(50, 25, 12, 100));

        g2.setColor(new Color(28, 75, 145));
        int[] bShapeX = {bowlX - 32, bowlX + 32, bowlX + 22, bowlX - 22};
        int[] bShapeY = {bowlY + 6, bowlY + 6, bowlY + 26, bowlY + 26};
        fillPolygonScanline(g2, bShapeX, bShapeY, 4);
        g2.setColor(new Color(45, 105, 195));
        drawPolygonLines(g2, bShapeX, bShapeY, 4);
        fillMidpointEllipse(g2, bowlX, bowlY + 26, 22, 6, new Color(22, 62, 125));

        g2.setColor(new Color(255, 220, 65));
        fillMidpointCircle(g2, bowlX - 16, bowlY + 16, 3, new Color(255, 220, 65));
        fillMidpointCircle(g2, bowlX, bowlY + 18, 4, new Color(255, 220, 65));
        fillMidpointCircle(g2, bowlX + 16, bowlY + 16, 3, new Color(255, 220, 65));

        int[] popX = {-24, -14, -4, 6, 16, 24, -18, -8, 2, 12, 20, -12, -2, 8, 0};
        int[] popY = {4, 2, 0, 2, 4, 6, -4, -6, -6, -4, -2, -12, -14, -10, -18};
        for (int i = 0; i < popX.length; i++) {
            fillMidpointCircle(g2, bowlX + popX[i], bowlY + popY[i], 6, new Color(255, 248, 215));
            fillMidpointCircle(g2, bowlX + popX[i], bowlY + popY[i] - 1, 4, new Color(255, 255, 245));
            fillMidpointCircle(g2, bowlX + popX[i] + 1, bowlY + popY[i] + 1, 2, new Color(245, 195, 75));
        }

        int snackX = tableX + 24;
        int snackY = tableY - 6;
        g2.setColor(new Color(245, 185, 30));
        int[] bagX = {snackX - 14, snackX + 16, snackX + 14, snackX - 16};
        int[] bagY = {snackY + 2, snackY - 4, snackY + 18, snackY + 16};
        fillPolygonScanline(g2, bagX, bagY, 4);
        g2.setColor(new Color(225, 45, 45));
        bresenhamLine(g2, snackX - 12, snackY + 8, snackX + 12, snackY + 5, 3);

        int cup1X = tableX + 148;
        int cup1Y = tableY - 10;
        g2.setColor(new Color(245, 125, 45));
        int[] c1X = {cup1X - 9, cup1X + 9, cup1X + 7, cup1X - 7};
        int[] c1Y = {cup1Y, cup1Y, cup1Y + 24, cup1Y + 24};
        fillPolygonScanline(g2, c1X, c1Y, 4);
        fillMidpointCircle(g2, cup1X - 3, cup1Y + 8, 2, Color.WHITE);
        fillMidpointCircle(g2, cup1X + 3, cup1Y + 14, 2, Color.WHITE);
        fillMidpointEllipse(g2, cup1X, cup1Y, 10, 3, Color.WHITE);
        g2.setColor(Color.WHITE);
        bresenhamLine(g2, cup1X, cup1Y, cup1X - 4, cup1Y - 14, 2);
        bresenhamLine(g2, cup1X - 4, cup1Y - 14, cup1X - 12, cup1Y - 16, 2);

        int cup2X = tableX + 172;
        int cup2Y = tableY - 14;
        g2.setColor(new Color(40, 165, 215));
        int[] c2X = {cup2X - 8, cup2X + 8, cup2X + 6, cup2X - 6};
        int[] c2Y = {cup2Y, cup2Y, cup2Y + 22, cup2Y + 22};
        fillPolygonScanline(g2, c2X, c2Y, 4);
        g2.setColor(Color.WHITE);
        bresenhamLine(g2, cup2X - 7, cup2Y + 10, cup2X + 7, cup2Y + 10, 2);
        fillMidpointEllipse(g2, cup2X, cup2Y, 9, 3, Color.WHITE);
        bresenhamLine(g2, cup2X, cup2Y, cup2X + 3, cup2Y - 12, 2);
        bresenhamLine(g2, cup2X + 3, cup2Y - 12, cup2X + 10, cup2Y - 14, 2);

        int remX = tableX + 12;
        int remY = tableY + 6;
        g2.setColor(new Color(24, 25, 28));
        fillRoundedRectangle(g2, remX, remY, 26, 9, 3, 3);
        fillMidpointCircle(g2, remX + 4, remY + 4, 1, new Color(235, 45, 45));
        fillMidpointCircle(g2, remX + 12, remY + 4, 2, new Color(160, 165, 175));
    }

    private void drawTVFriend1_PopcornBoy(Graphics2D g2, int x, int y, double st) {
        int t2 = 2;
        int headR = 19;

        double chew = Math.sin(st * 6.0) * 1.8;
        int hipX = x, hipY = y + 54;
        int shoulderX = x + 14, shoulderY = y;
        int headX = shoulderX + 8, headY = (int) (shoulderY - 28 + chew * 0.3);

        g2.setColor(INK);
        bresenhamLine(g2, hipX, hipY, hipX - 22, hipY + 16, t2);
        bresenhamLine(g2, hipX - 22, hipY + 16, hipX + 12, hipY + 28, t2);
        fillEllipse(g2, hipX + 8, hipY + 24, 14, 7);

        bresenhamLine(g2, shoulderX, shoulderY, hipX, hipY, t2);

        int bowlX = x + 18, bowlY = y + 34;
        bresenhamLine(g2, shoulderX - 8, shoulderY + 4, bowlX, bowlY, t2);
        fillMidpointCircle(g2, bowlX, bowlY, 3, INK);
        g2.setColor(new Color(35, 95, 175));
        fillMidpointEllipse(g2, bowlX + 6, bowlY, 14, 8, new Color(35, 95, 175));
        fillMidpointCircle(g2, bowlX + 6, bowlY - 3, 5, new Color(255, 245, 205));

        double eatArm = Math.sin(st * 4.0) * 4.0;
        int eatHandX = (int) (headX - 12 + eatArm * 0.4);
        int eatHandY = (int) (headY + 6 + chew);
        int elbowX = shoulderX + 20;
        int elbowY = shoulderY + 14;
        bresenhamLine(g2, shoulderX + 8, shoulderY + 4, elbowX, elbowY, t2);
        bresenhamLine(g2, elbowX, elbowY, eatHandX, eatHandY, t2);
        fillMidpointCircle(g2, eatHandX, eatHandY, 3, INK);
        fillMidpointCircle(g2, eatHandX - 3, eatHandY - 2, 4, new Color(255, 235, 120));

        bresenhamLine(g2, headX, headY + headR, shoulderX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 8, headY - headR, headX - 14, headY - headR - 9, 1);
        bresenhamLine(g2, headX - 1, headY - headR, headX - 1, headY - headR - 12, 1);
        bresenhamLine(g2, headX + 7, headY - headR, headX + 12, headY - headR - 9, 1);

        int ex = headX - 3;
        bezierCurve(g2, ex - 7, headY - 4, ex - 2, headY - 2, ex - 2, headY - 2, ex - 7, headY + 1);
        fillEllipse(g2, ex + 4, headY - 5, 5, 6);
        fillMidpointEllipse(g2, headX - 4, headY + 6, 6, 5, INK);
        fillMidpointEllipse(g2, headX - 4, headY + 7, 4, 3, new Color(245, 120, 130));
    }

    private void drawTVFriend2_CenterHero(Graphics2D g2, int x, int y, double st) {
        int t2 = 2;
        int headR = 19;

        double sway = Math.sin(st * 3.5) * 1.5;
        int hipX = x, hipY = y + 54;
        int shoulderX = x - 4, shoulderY = y;
        int headX = shoulderX - 4, headY = (int) (shoulderY - 28 + sway * 0.3);

        g2.setColor(INK);
        bresenhamLine(g2, hipX - 6, hipY, hipX - 26, hipY + 18, t2);
        bresenhamLine(g2, hipX - 26, hipY + 18, hipX - 6, hipY + 28, t2);
        bresenhamLine(g2, hipX + 6, hipY, hipX + 26, hipY + 18, t2);
        bresenhamLine(g2, hipX + 26, hipY + 18, hipX + 6, hipY + 28, t2);
        fillEllipse(g2, hipX - 24, hipY + 24, 12, 6);
        fillEllipse(g2, hipX + 12, hipY + 24, 12, 6);

        bresenhamLine(g2, shoulderX, shoulderY, hipX, hipY, t2);

        int pillowX = x - 2;
        int pillowY = y + 26;
        g2.setColor(new Color(110, 175, 105));
        fillMidpointEllipse(g2, pillowX, pillowY, 22, 16, new Color(110, 175, 105));
        g2.setColor(new Color(135, 198, 130));
        fillMidpointEllipse(g2, pillowX, pillowY - 2, 17, 12, new Color(135, 198, 130));

        g2.setColor(INK);
        bresenhamLine(g2, shoulderX - 12, shoulderY + 4, pillowX - 15, pillowY + 2, t2);
        bresenhamLine(g2, shoulderX + 12, shoulderY + 4, pillowX + 15, pillowY + 2, t2);
        fillMidpointCircle(g2, pillowX - 15, pillowY + 2, 3, INK);
        fillMidpointCircle(g2, pillowX + 15, pillowY + 2, 3, INK);

        bresenhamLine(g2, headX, headY + headR, shoulderX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 7, headY - headR, headX - 11, headY - headR - 9, 1);
        bresenhamLine(g2, headX + 2, headY - headR, headX + 2, headY - headR - 11, 1);
        bresenhamLine(g2, headX + 9, headY - headR, headX + 14, headY - headR - 9, 1);

        g2.setColor(new Color(255, 140, 140, 170));
        fillMidpointEllipse(g2, headX - 11, headY + 3, 5, 3, new Color(255, 140, 140, 170));
        fillMidpointEllipse(g2, headX + 9, headY + 3, 5, 3, new Color(255, 140, 140, 170));

        g2.setColor(INK);
        fillEllipse(g2, headX - 10, headY - 5, 5, 7);
        fillEllipse(g2, headX + 3, headY - 5, 5, 7);
        fillMidpointCircle(g2, headX - 9, headY - 6, 2, Color.WHITE);
        fillMidpointCircle(g2, headX + 4, headY - 6, 2, Color.WHITE);
        bezierCurve(g2, headX - 8, headY + 6, headX - 1, headY + 11, headX + 3, headY + 11, headX + 8, headY + 6);
    }

    private void drawTVFriend3_GreenPillowBoy(Graphics2D g2, int x, int y, double st) {
        int t2 = 2;
        int headR = 19;

        double lean = Math.sin(st * 4.5) * 2.0;
        int hipX = x + 10, hipY = y + 54;
        int shoulderX = (int) (x - 10 - lean * 0.5), shoulderY = y;
        int headX = shoulderX - 10, headY = y - 30;

        g2.setColor(INK);
        bresenhamLine(g2, hipX, hipY, hipX - 18, hipY + 16, t2);
        bresenhamLine(g2, hipX - 18, hipY + 16, hipX + 14, hipY + 28, t2);
        fillEllipse(g2, hipX + 10, hipY + 24, 14, 7);

        bresenhamLine(g2, shoulderX, shoulderY, hipX, hipY, t2);

        int pilX = shoulderX + 2;
        int pilY = shoulderY + 24;
        g2.setColor(new Color(235, 110, 42));
        fillMidpointEllipse(g2, pilX, pilY, 24, 18, new Color(235, 110, 42));
        g2.setColor(new Color(250, 145, 75));
        fillMidpointEllipse(g2, pilX, pilY - 2, 18, 13, new Color(250, 145, 75));

        g2.setColor(INK);
        bresenhamLine(g2, shoulderX - 10, shoulderY + 4, pilX - 16, pilY + 2, t2);
        bresenhamLine(g2, shoulderX + 10, shoulderY + 4, pilX + 16, pilY + 2, t2);
        fillMidpointCircle(g2, pilX - 16, pilY + 2, 3, INK);
        fillMidpointCircle(g2, pilX + 16, pilY + 2, 3, INK);

        bresenhamLine(g2, headX, headY + headR, shoulderX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 8, headY - headR, headX - 12, headY - headR - 9, 1);
        bresenhamLine(g2, headX + 1, headY - headR, headX + 1, headY - headR - 11, 1);
        bresenhamLine(g2, headX + 8, headY - headR, headX + 13, headY - headR - 8, 1);

        fillEllipse(g2, headX - 10, headY - 5, 5, 6);
        fillEllipse(g2, headX - 1, headY - 5, 5, 6);
        bezierCurve(g2, headX - 9, headY + 4, headX - 2, headY + 11, headX + 3, headY + 11, headX + 7, headY + 4);
    }

    private void drawTVFriend4_SofaCheerBoy(Graphics2D g2, int x, int y, double st) {
        int t2 = 2;
        int headR = 19;

        double cheerHop = Math.abs(Math.sin(st * 5.5)) * 4.0;
        int hipX = x + 8, hipY = y + 48;
        int shoulderX = x - 4, shoulderY = (int) (y - 2 - cheerHop);
        int headX = shoulderX - 6, headY = (int) (shoulderY - 28 - cheerHop * 0.5);

        g2.setColor(INK);
        bresenhamLine(g2, hipX, hipY, hipX + 16, hipY + 18, t2);
        fillEllipse(g2, hipX + 12, hipY + 15, 14, 7);

        bresenhamLine(g2, shoulderX, shoulderY, hipX, hipY, t2);

        int cushX = x - 22;
        int cushY = y + 18;
        g2.setColor(new Color(245, 205, 55));
        fillMidpointEllipse(g2, cushX, cushY, 22, 15, new Color(245, 205, 55));
        g2.setColor(new Color(255, 225, 95));
        fillMidpointEllipse(g2, cushX, cushY - 2, 17, 10, new Color(255, 225, 95));

        g2.setColor(INK);
        bresenhamLine(g2, shoulderX - 10, shoulderY + 4, cushX - 10, cushY - 2, t2);
        bresenhamLine(g2, shoulderX + 10, shoulderY + 4, cushX + 12, cushY - 2, t2);
        fillMidpointCircle(g2, cushX - 10, cushY - 2, 3, INK);
        fillMidpointCircle(g2, cushX + 12, cushY - 2, 3, INK);

        bresenhamLine(g2, headX, headY + headR, shoulderX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        fillEllipse(g2, headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        bresenhamLine(g2, headX - 7, headY - headR, headX - 11, headY - headR - 9, 1);
        bresenhamLine(g2, headX + 1, headY - headR, headX + 1, headY - headR - 11, 1);
        bresenhamLine(g2, headX + 9, headY - headR, headX + 14, headY - headR - 8, 1);

        int ey = headY - 3;
        bezierCurve(g2, headX - 11, ey, headX - 8, ey - 4, headX - 3, ey - 4, headX - 1, ey);
        bezierCurve(g2, headX + 2, ey, headX + 5, ey - 4, headX + 10, ey - 4, headX + 12, ey);
        fillMidpointEllipse(g2, headX, headY + 6, 7, 6, INK);
        g2.setColor(new Color(245, 120, 130));
        fillMidpointEllipse(g2, headX, headY + 8, 5, 3, new Color(245, 120, 130));
    }

    private void drawTVLivingRoomLighting(Graphics2D g2, double st) {
        float tvGlowPulse = (float) (0.6 + 0.4 * Math.sin(st * 12.0));
        Color glowColor = ((int) (st * 6) % 2 == 0)
            ? new Color(0, 175, 255, (int) (70 * tvGlowPulse))
            : new Color(255, 140, 30, (int) (65 * tvGlowPulse));

        Point2D tvCenter = new Point2D.Float(100.0f, 225.0f);
        float tvRadius = 420.0f;
        float[] tvDist = {0.0f, 0.45f, 0.85f, 1.0f};
        Color[] tvColors = {
            glowColor,
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), glowColor.getAlpha() / 2),
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), glowColor.getAlpha() / 6),
            new Color(0, 0, 0, 0)
        };
        RadialGradientPaint tvGlow = new RadialGradientPaint(tvCenter, tvRadius, tvDist, tvColors);
        g2.setPaint(tvGlow);
        fillRectangle(g2, 0, 0, 600, 600);

        Point2D lampCenter = new Point2D.Float(575.0f, 120.0f);
        float lampRadius = 380.0f;
        float[] lampDist = {0.0f, 0.50f, 1.0f};
        Color[] lampColors = {
            new Color(255, 230, 160, 60),
            new Color(245, 195, 110, 25),
            new Color(230, 150, 60, 0)
        };
        RadialGradientPaint lampGlow = new RadialGradientPaint(lampCenter, lampRadius, lampDist, lampColors);
        g2.setPaint(lampGlow);
        fillRectangle(g2, 0, 0, 600, 600);

        Point2D vigCenter = new Point2D.Float(300.0f, 300.0f);
        float vigRadius = 440.0f;
        float[] vigDist = {0.0f, 0.70f, 1.0f};
        Color[] vigColors = {
            new Color(0, 0, 0, 0),
            new Color(35, 18, 10, 25),
            new Color(20, 8, 4, 110)
        };
        RadialGradientPaint vig = new RadialGradientPaint(vigCenter, vigRadius, vigDist, vigColors);
        g2.setPaint(vig);
        fillRectangle(g2, 0, 0, 600, 600);
    }

    private void drawTVScene(Graphics2D g2, double st) {
        if (livingRoomBackdrop == null) livingRoomBackdrop = buildLivingRoomBackdrop();
        g2.drawImage(livingRoomBackdrop, 0, 0, null);

        drawTVScreenBattle(g2, 8, 140, 172, 195, st);

        drawTVFriend4_SofaCheerBoy(g2, 380, 260, st);

        drawTVFriend1_PopcornBoy(g2, 190, 395, st);
        drawTVFriend2_CenterHero(g2, 295, 390, st);
        drawTVFriend3_GreenPillowBoy(g2, 410, 400, st);

        drawCoffeeTableAndSnacks(g2, 8, 465, st);

        drawTVLivingRoomLighting(g2, st);
    }

    private double warpFlash(double t) {
        double a = 1 - Math.abs(t - WARP_INTO_MEMORY) / WARP_RAMP;
        double b = 1 - Math.abs(t - WARP_INTO_SWORD) / WARP_RAMP;
        double c = 1 - Math.abs(t - WARP_INTO_WATER) / WARP_RAMP;
        double d = 1 - Math.abs(t - WARP_INTO_BIKE) / WARP_RAMP;
        double e = 1 - Math.abs(t - WARP_INTO_TV) / WARP_RAMP;
        double f = 1 - Math.abs(t - WARP_INTO_MOOKRATHA) / WARP_RAMP;
        double g = 1 - Math.abs(t - WARP_BACK) / WARP_RAMP;
        return Math.max(0, Math.max(Math.max(Math.max(a, b), Math.max(c, d)), Math.max(Math.max(e, f), g)));
    }

    private void drawWarp(Graphics2D g2, double flash, double t) {
        boolean darkTransition = Math.abs(t - WARP_INTO_MEMORY) <= WARP_RAMP
                || Math.abs(t - WARP_BACK) <= WARP_RAMP;
        Color fadeColor = darkTransition ? Color.BLACK : Color.WHITE;

        g2.setColor(new Color(fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), (int) (255 * flash * flash)));
        fillRectangle(g2, 0, 0, 600, 600);
    }

    public void run() {
        long lastTime = System.nanoTime();
        while (true) {
            long frameStart = System.nanoTime();
            double elapsedTime = (frameStart - lastTime) / 1_000_000_000.0;
            lastTime = frameStart;

            totalTime += elapsedTime;
            if (totalTime >= CYCLE) totalTime -= CYCLE;

            repaint();

            long workMs = (System.nanoTime() - frameStart) / 1_000_000L;
            long sleepMs = FRAME_MS - workMs;
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        double t = totalTime;

        if (t >= WARP_INTO_MEMORY && t < WARP_INTO_SWORD) {
            drawMemoryScene(g2, t - WARP_INTO_MEMORY);
        } else if (t >= WARP_INTO_SWORD && t < WARP_INTO_WATER) {
            drawSwordFightScene(g2, t - WARP_INTO_SWORD);
        } else if (t >= WARP_INTO_WATER && t < WARP_INTO_BIKE) {
            drawStreamScene(g2, t - WARP_INTO_WATER);
        } else if (t >= WARP_INTO_BIKE && t < WARP_INTO_TV) {
            drawBicycleScene(g2, t - WARP_INTO_BIKE);
        } else if (t >= WARP_INTO_TV && t < WARP_INTO_MOOKRATHA) {
            drawTVScene(g2, t - WARP_INTO_TV);
        } else if (t >= WARP_INTO_MOOKRATHA && t < WARP_BACK) {
            drawMooKrathaScene(g2, t - WARP_INTO_MOOKRATHA);
        } else {
            drawNightScene(g2, t);
        }

        double flash = warpFlash(t);
        if (flash > 0) drawWarp(g2, flash, t);

        g2.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Assignment1_studentID_yourPairID panel = new Assignment1_studentID_yourPairID();
            JFrame frame = new JFrame("Assignment 1 - MY MEMORIES");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            new Thread(panel).start();
        });
    }
}
