import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Assignment1_studentID_yourPairID extends JPanel implements Runnable {

    // ===== animation timing =====
    // 0.0 -> 4.6    Scene 1: Night stargazing, zoom into eye, eyes close
    // 4.6 -> 9.6    Scene 2 (Memory 1): Football match & bicycle kick
    // 9.6 -> 15.6   Scene 3 (Memory 2): Epic childhood toy sword fight
    // 15.6 -> 19.6  Scene 4 (Memory 3): Playing in a cheerful stream
    // 19.6 -> 22.0  Scene 1: Back to present, wake up, zoom out
    volatile double totalTime = 0;
    static final double CYCLE = 22.0;     // seconds, whole animation loops after this
    static final double SHOOT_START = 1.2;
    static final double SHOOT_DURATION = 1.4;
    static final double EYES_CLOSE_START = 3.2;
    static final double EYES_CLOSE_END = 3.9;
    static final double WARP_INTO_MEMORY = 4.6;
    static final double WARP_INTO_SWORD = 9.6;
    static final double WARP_INTO_WATER = 15.6;
    static final double WARP_BACK = 19.6;
    static final double EYES_REOPEN_START = 20.8;  // wakes up again so the loop is seamless
    static final double EYES_REOPEN_END = 21.4;
    static final double WARP_RAMP = 0.45; // how long the scene transition takes each side
    static final int FRAME_MS = 16;       // ~60 fps

    // ===== Seeded background parameters (from test.java) =====
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

    // ===== memory scene grass =====
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

    // =========================================================================
    // COMPUTER GRAPHICS LAB 1 - LAB 6 ALGORITHMS REPOSITORY
    // =========================================================================

    // Lab_01: Basic plot pixel
    private void plotPixel(Graphics g, int x, int y) {
        g.fillRect(x - 1, y - 1, 3, 3);
    }

    // Lab_02: Bresenham's Line Algorithm
    private void bresenhamLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        boolean isSwap = false;
        if (dy > dx) {
            int temp = dx;
            dx = dy;
            dy = temp;
            isSwap = true;
        }
        int D = 2 * dy - dx;
        int x = x1;
        int y = y1;
        int size = thickness * 2 + 1;
        for (int i = 1; i <= dx; i++) {
            g.fillRect(x - thickness, y - thickness, size, size);
            if (D >= 0) {
                if (isSwap) x += sx;
                else y += sy;
                D -= 2 * dx;
            }
            if (isSwap) y += sy;
            else x += sx;
            D += 2 * dy;
        }
    }

    private void bresenhamLine(Graphics g, int x1, int y1, int x2, int y2) {
        bresenhamLine(g, x1, y1, x2, y2, 0);
    }

    // Lab_02: DDA Line Algorithm
    public void DDALine(Graphics g, int x1, int y1, int x2, int y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            plotPixel(g, x1, y1);
            return;
        }
        float m = (dx != 0) ? dy / dx : 999999.0f;

        if (m <= 1 && m >= 0) {
            float y = (x1 <= x2) ? y1 : y2;
            int startX = Math.min(x1, x2);
            int endX = Math.max(x1, x2);
            for (int x = startX; x <= endX; x++) {
                plotPixel(g, x, Math.round(y));
                y = y + m;
            }
        } else if (m <= -1) {
            float y = (x1 >= x2) ? y1 : y2;
            int startX = Math.max(x1, x2);
            int endX = Math.min(x1, x2);
            for (int x = startX; x >= endX; x--) {
                plotPixel(g, x, Math.round(y));
                y = y + m;
            }
        } else if (m > 1) {
            float x = (y1 <= y2) ? x1 : x2;
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                plotPixel(g, Math.round(x), y);
                x = x + 1 / m;
            }
        } else {
            float x = (y2 <= y1) ? x2 : x1;
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = endY; y >= startY; y--) {
                plotPixel(g, Math.round(x), y);
                x = x + 1 / m;
            }
        }
    }

    private void plot8Octants(Graphics g, int xc, int yc, int x, int y) {
        plotPixel(g, xc + x, yc - y);
        plotPixel(g, xc + y, yc - x);
        plotPixel(g, xc + y, yc + x);
        plotPixel(g, xc + x, yc + y);
        plotPixel(g, xc - x, yc + y);
        plotPixel(g, xc - y, yc + x);
        plotPixel(g, xc - y, yc - x);
        plotPixel(g, xc - x, yc - y);
    }

    private void midpointCircle(Graphics g, int xc, int yc, int r) {
        int x = 0;
        int y = r;
        int Dx = 2 * x;
        int Dy = 2 * y;
        int D = 1 - r;
        while (x <= y) {
            plot8Octants(g, xc, yc, x, y);
            x = x + 1;
            Dx = Dx + 2;
            D = D + Dx + 1;
            if (D >= 0) {
                y = y - 1;
                Dy = Dy - 2;
                D = D - Dy;
            }
        }
    }

    public static void drawMidpointCircle(Graphics g, int xc, int yc, int r, Color color) {
        if (r <= 0) return;
        g.setColor(color);
        int x = 0;
        int y = r;
        int p = 1 - r;

        plot8CirclePoints(g, xc, yc, x, y);

        while (x < y) {
            x++;
            if (p < 0) {
                p += 2 * x + 1;
            } else {
                y--;
                p += 2 * (x - y) + 1;
            }
            plot8CirclePoints(g, xc, yc, x, y);
        }
    }

    public static void fillMidpointCircle(Graphics g, int xc, int yc, int r, Color color) {
        if (r <= 0) return;
        g.setColor(color);
        int x = 0;
        int y = r;
        int p = 1 - r;

        drawCircleScanlines(g, xc, yc, x, y);

        while (x < y) {
            x++;
            if (p < 0) {
                p += 2 * x + 1;
            } else {
                y--;
                p += 2 * (x - y) + 1;
            }
            drawCircleScanlines(g, xc, yc, x, y);
        }
    }

    private static void plot8CirclePoints(Graphics g, int xc, int yc, int x, int y) {
        g.fillRect(xc + x, yc + y, 1, 1);
        g.fillRect(xc - x, yc + y, 1, 1);
        g.fillRect(xc + x, yc - y, 1, 1);
        g.fillRect(xc - x, yc - y, 1, 1);
        g.fillRect(xc + y, yc + x, 1, 1);
        g.fillRect(xc - y, yc + x, 1, 1);
        g.fillRect(xc + y, yc - x, 1, 1);
        g.fillRect(xc - y, yc - x, 1, 1);
    }

    private static void drawCircleScanlines(Graphics g, int xc, int yc, int x, int y) {
        g.drawLine(xc - x, yc + y, xc + x, yc + y);
        g.drawLine(xc - x, yc - y, xc + x, yc - y);
        g.drawLine(xc - y, yc + x, xc + y, yc + x);
        g.drawLine(xc - y, yc - x, xc + y, yc - x);
    }

    public static void fillMidpointEllipse(Graphics g, int xc, int yc, int rx, int ry, Color color) {
        if (rx <= 0 || ry <= 0) return;
        g.setColor(color);

        long rxSq = (long) rx * rx;
        long rySq = (long) ry * ry;
        long twoRxSq = 2 * rxSq;
        long twoRySq = 2 * rySq;

        int x = 0;
        int y = ry;
        long px = 0;
        long py = twoRxSq * y;

        // Region 1
        long p1 = Math.round(rySq - (rxSq * ry) + (0.25 * rxSq));
        while (px < py) {
            g.drawLine(xc - x, yc + y, xc + x, yc + y);
            g.drawLine(xc - x, yc - y, xc + x, yc - y);
            x++;
            px += twoRySq;
            if (p1 < 0) {
                p1 += rySq + px;
            } else {
                y--;
                py -= twoRxSq;
                p1 += rySq + px - py;
            }
        }

        // Region 2
        long p2 = Math.round(rySq * (x + 0.5) * (x + 0.5) + rxSq * (y - 1) * (y - 1) - rxSq * rySq);
        while (y >= 0) {
            g.drawLine(xc - x, yc + y, xc + x, yc + y);
            g.drawLine(xc - x, yc - y, xc + x, yc - y);
            y--;
            py -= twoRxSq;
            if (p2 > 0) {
                p2 += rxSq - py;
            } else {
                x++;
                px += twoRySq;
                p2 += rxSq - py + px;
            }
        }
    }

    // Lab_04: Midpoint Ellipse Algorithm (Outline)
    public void midpointEllipse(Graphics g, int xc, int yc, int a, int b) {
        int a2 = a * a;
        int b2 = b * b;
        int twoA2 = 2 * a2;
        int twoB2 = 2 * b2;

        // REGION 1
        int x = 0;
        int y = b;
        int D = (int) Math.round(b2 - (a2 * b) + (a2 / 4.0));
        int Dx = 0;
        int Dy = twoA2 * y;

        while (Dx <= Dy) {
            plot4Quadrants(g, xc, yc, x, y);
            x = x + 1;
            Dx = Dx + twoB2;
            D = D + Dx + b2;

            if (D >= 0) {
                y = y - 1;
                Dy = Dy - twoA2;
                D = D - Dy;
            }
        }

        // REGION 2
        x = a;
        y = 0;
        D = (int) Math.round(a2 - (b2 * a) + (b2 / 4.0));
        Dx = twoB2 * x;
        Dy = 0;

        while (Dx >= Dy) {
            plot4Quadrants(g, xc, yc, x, y);
            y = y + 1;
            Dy = Dy + twoA2;
            D = D + Dy + a2;

            if (D >= 0) {
                x = x - 1;
                Dx = Dx - twoB2;
                D = D - Dx;
            }
        }
    }

    private void plot4Quadrants(Graphics g, int xc, int yc, int x, int y) {
        plotPixel(g, xc + x, yc - y);
        plotPixel(g, xc - x, yc - y);
        plotPixel(g, xc - x, yc + y);
        plotPixel(g, xc + x, yc + y);
    }

    // Lab_03: Flood Fill Algorithm (Queue-based 4-connected)
    public BufferedImage floodFill(BufferedImage m, int x, int y, Color target_colour, Color replacement_colour) {
        int targetRGB = target_colour.getRGB();
        int replacementRGB = replacement_colour.getRGB();
        if (targetRGB == replacementRGB) return m;
        if (x < 0 || x >= m.getWidth() || y < 0 || y >= m.getHeight()) return m;
        if (m.getRGB(x, y) != targetRGB) return m;

        Queue<Point> Q = new LinkedList<>();
        m.setRGB(x, y, replacementRGB);
        Q.add(new Point(x, y));
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while (!Q.isEmpty()) {
            Point current = Q.poll();
            for (int i = 0; i < 4; i++) {
                int next_x = current.x + dx[i];
                int next_y = current.y + dy[i];
                if (next_x >= 0 && next_x < m.getWidth() && next_y >= 0 && next_y < m.getHeight()) {
                    if (m.getRGB(next_x, next_y) == targetRGB) {
                        m.setRGB(next_x, next_y, replacementRGB);
                        Q.add(new Point(next_x, next_y));
                    }
                }
            }
        }
        return m;
    }

    // Lab_03: cubic Bezier
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
            g.drawLine(prevX, prevY, currX, currY);
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

    // Calculate stickman's eye position in world space for zoom camera targeting
    private Point2D.Double getStickmanEyePosition(double t) {
        double breathe = Math.sin(t * 1.2) * 2.0;
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

    // =========================================================================
    // 2. SCENE 1: NIGHT STARGAZING (MERGED SCENERY + STICKMAN + EYE ZOOM)
    // =========================================================================

    private void drawNightScene(Graphics2D g2d, double t) {
        AffineTransform screenTransform = g2d.getTransform();

        // Camera smoothly zooms into the stickman's eye before eyes close and transition
        double zoom = 1.0;
        if (t >= 1.8 && t < 4.3) {
            double p = smoothStep((t - 1.8) / 2.5);
            zoom = 1.0 + 1.25 * p;
        } else if (t >= 4.3 && t < WARP_INTO_MEMORY) {
            zoom = 2.25;
        } else if (t >= WARP_BACK && t < 20.2) {
            zoom = 2.25;
        } else if (t >= 20.2 && t < 21.6) {
            double p = smoothStep((t - 20.2) / 1.4);
            zoom = 2.25 - 1.25 * p;
        }

        if (zoom > 1.0) {
            Point2D.Double eyePos = getStickmanEyePosition(t);
            g2d.translate(eyePos.x, eyePos.y);
            g2d.scale(zoom, zoom);
            g2d.translate(-eyePos.x, -eyePos.y);
        }

        // --- DRAW WORLD SCENE LAYERS (from test.java) ---
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

        // Screen vignette framing
        drawVignette(g2d);
    }

    public void drawSkyBackground(Graphics2D g2d, double time) {
        Point2D start = new Point2D.Float(300, 0);
        Point2D end = new Point2D.Float(300, 600);
        float[] dist = {0.0f, 0.4f, 0.75f, 1.0f};
        Color[] colors = {
            new Color(2, 6, 20),     // Deep midnight zenith
            new Color(8, 22, 58),    // Indigo upper sky
            new Color(16, 45, 100),  // Sapphire celestial blue
            new Color(28, 70, 130)   // Twilight horizon glow
        };
        LinearGradientPaint skyGrad = new LinearGradientPaint(start, end, dist, colors);
        g2d.setPaint(skyGrad);
        g2d.fillRect(-400, -400, 1400, 1400);
    }

    public void drawMilkyWay(Graphics2D g2d, double time) {
        AffineTransform oldTx = g2d.getTransform();

        // Diagonal galactic arch across the sky
        g2d.translate(340, 175);
        g2d.rotate(Math.toRadians(-40));

        // Soft layered nebulas with translucent glow
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
            g2d.fill(band);
        }

        // Stardust clusters along galactic spine (Midpoint Ellipse Algorithm)
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

        // Outer soft glow halos (Midpoint Circle Algorithm)
        for (int r = moonR + 40; r >= moonR; r -= 3) {
            int alpha = (int) (20 * (1.0 - (double) (r - moonR) / 40.0));
            fillMidpointCircle(g2d, moonX, moonY, r, new Color(165, 205, 255, alpha));
        }

        // Inner bright halo
        fillMidpointCircle(g2d, moonX, moonY, moonR + 4, new Color(220, 240, 255, 65));

        // Moon Base Disc (Midpoint Circle Algorithm)
        fillMidpointCircle(g2d, moonX, moonY, moonR, new Color(245, 250, 255));

        // Lunar Maria & Craters (Midpoint Ellipse & Circle Algorithms)
        fillMidpointEllipse(g2d, moonX - 6, moonY - 4, 6, 8, new Color(205, 218, 235, 160));
        fillMidpointEllipse(g2d, moonX + 7, moonY - 7, 5, 4, new Color(210, 222, 238, 140));
        fillMidpointCircle(g2d, moonX - 3, moonY + 9, 5, new Color(200, 215, 232, 150));
        fillMidpointEllipse(g2d, moonX + 8, moonY + 6, 7, 5, new Color(195, 210, 230, 130));
        fillMidpointCircle(g2d, moonX + 1, moonY + 2, 4, new Color(212, 225, 240, 170));

        // Crisp rim outline
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
                g2d.fillRect(x, y, 1, 1);
            } else {
                fillMidpointCircle(g2d, x, y, r, c);
                // Sparkling 4-point cross diffraction spike on bright stars
                if (starSize[i] > 2.0 && brightness > 0.75) {
                    g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 0.6)));
                    int flareLen = (int) (r * 3.5);
                    g2d.drawLine(x - flareLen, y, x + flareLen, y);
                    g2d.drawLine(x, y - flareLen, x, y + flareLen);
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
            g2d.draw(new Line2D.Double(pHead, pTail));

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
        g2d.fill(mountains);
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
        g2d.fill(hill);
    }

    // =========================================================================
    // 3. FOREGROUND FLOWERS & SWAYING GRASS (from test.java)
    // =========================================================================

    public void drawForegroundFlowersAndGrass(Graphics2D g2d, double time) {
        double wind = Math.sin(time * 2.2) * 5.0;

        // Foreground grass blades across meadow
        for (int i = 0; i < NUM_GRASS; i++) {
            double gx = grassX[i];
            double gy = grassY[i];
            double gh = grassHeight[i];
            double gb = grassBend[i] + wind * (gy / 600.0);

            Path2D.Double blade = new Path2D.Double();
            blade.moveTo(gx - 2.5, gy);
            blade.curveTo(gx - 1, gy - gh * 0.5, gx + gb * 0.6, gy - gh * 0.8, gx + gb, gy - gh);
            blade.curveTo(gx + gb * 0.4, gy - gh * 0.7, gx + 2, gy - gh * 0.4, gx + 2.5, gy);
            blade.closePath();

            int alpha = (int) (180 + 75 * (gy - 490) / 110.0);
            g2d.setColor(new Color(8, 22, 28, Math.min(255, Math.max(0, alpha))));
            g2d.fill(blade);
        }

        // Chamomile / Daisy flowers (Midpoint Ellipse petals & Midpoint Circle center)
        for (int i = 0; i < NUM_FLOWERS; i++) {
            drawChamomileFlower(g2d, flowerX[i], flowerY[i], flowerScale[i], flowerRot[i] + wind * 0.02);
        }
    }

    private void drawChamomileFlower(Graphics2D g2d, double x, double y, double scale, double rotation) {
        AffineTransform oldTx = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(rotation);
        g2d.scale(scale, scale);

        // 8 Moonlit Petals (Midpoint Ellipse Algorithm)
        int numPetals = 8;
        Color petalColor = new Color(220, 235, 250, 210);
        for (int p = 0; p < numPetals; p++) {
            double angle = p * (2 * Math.PI / numPetals);
            int px = (int) (Math.cos(angle) * 7);
            int py = (int) (Math.sin(angle) * 7);
            fillMidpointEllipse(g2d, px, py, 3, 5, petalColor);
        }

        // Flower Center Disk (Midpoint Circle Algorithm)
        fillMidpointCircle(g2d, 0, 0, 4, new Color(255, 215, 80, 240)); // Golden yellow
        fillMidpointCircle(g2d, 0, 0, 2, new Color(255, 240, 150, 255)); // Bright core

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
        g2d.fillRect(0, 0, 600, 600);
    }

    private static final Color GRASS_GREEN = new Color(55, 95, 48);

    private void drawGrassBlades(Graphics2D g2, double t) {
        g2.setColor(GRASS_GREEN);
        for (int i = 0; i < NUM_BLADES; i++) {
            double bx = bladeX[i], by = bladeY[i], h = bladeH[i];
            double sway = Math.sin(t * 1.5 + bx) * 3;
            bezierCurve(g2, bx, by, bx + sway * 0.5, by - h * 0.5,
                        bx + sway, by - h * 0.8, bx + sway * 1.2, by - h, 8);
        }
    }

    // ================= stickman lying down, hands behind the head =================
    // classic stickman proportions: small head, long thin limbs, rounded hand/foot caps
    private void drawStickman(Graphics2D g2, double t) {
        double breathe = Math.sin(t * 1.2) * 2.0;

        int headX = 150, headY = (int) (455 + breathe);
        int headR = 42;
        int bodyT = 2;

        // Contact shadow on the grass
        g2.setColor(new Color(0, 4, 10, 110));
        g2.fill(new Ellipse2D.Double(headX - 30, 490, 390, 60));

        g2.setColor(new Color(20, 20, 20));

        // ---- torso, running from under the head down to the hip ----
        int neckX = headX + 34, neckY = (int) (headY + 30 + breathe * 0.5);
        int hipX = 330, hipY = 505;
        bresenhamLine(g2, neckX, neckY, hipX, hipY, bodyT);

        // ---- both arms cradle the head: elbows splay to either side, hands tuck in
        //      behind it. Drawn before the head so the head hides the hands. ----

        // upper arm, elbow pointing up toward the sky
        g2.setColor(new Color(20, 20, 20));
        int farElbowX = headX + 72;
        int farElbowY = (int) (headY - 96 + breathe * 0.5);
        int farHandX = headX - 26;
        int farHandY = (int) (headY - 22 + breathe * 0.5);
        bresenhamLine(g2, neckX, neckY - 14, farElbowX, farElbowY, bodyT);
        bresenhamLine(g2, farElbowX, farElbowY, farHandX, farHandY, bodyT);

        // lower arm, elbow resting down on the grass
        g2.setColor(new Color(20, 20, 20));
        int nearElbowX = headX + 66;
        int nearElbowY = (int) (headY + 92 + breathe * 0.5);
        int nearHandX = headX - 26;
        int nearHandY = (int) (headY + 22 + breathe * 0.5);
        bresenhamLine(g2, neckX, neckY + 6, nearElbowX, nearElbowY, bodyT);
        bresenhamLine(g2, nearElbowX, nearElbowY, nearHandX, nearHandY, bodyT);

        // ---- legs: one flat on the grass, one knee raised ----
        int knee1X = 420, knee1Y = 512;
        int foot1X = 500, foot1Y = 500;
        bresenhamLine(g2, hipX, hipY, knee1X, knee1Y, bodyT);
        bresenhamLine(g2, knee1X, knee1Y, foot1X, foot1Y, bodyT);

        int knee2X = 400, knee2Y = 445;
        int foot2X = 448, foot2Y = 498;
        bresenhamLine(g2, hipX, hipY, knee2X, knee2Y, bodyT);
        bresenhamLine(g2, knee2X, knee2Y, foot2X, foot2Y, bodyT);

        // rounded shoe caps, like the reference
        g2.fillOval(foot1X - 12, foot1Y - 7, 26, 13);
        g2.fillOval(foot2X - 10, foot2Y - 7, 24, 13);

        // ---- head, tipped back so the face turns up to the sky
        //      (Lab_06 rotate-about-a-point pattern) ----
        // he is lying with his feet to the right, so the crown of his head points left
        // and his face points up: a negative (counter-clockwise) tilt does exactly that.
        // the head and every facial feature share this transform so they tilt as one piece
        AffineTransform noTilt = g2.getTransform();
        AffineTransform tilt = new AffineTransform(noTilt);
        tilt.rotate(Math.toRadians(-60), headX, headY);

        g2.setTransform(tilt);
        g2.setColor(Color.WHITE);
        g2.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(new Color(20, 20, 20));
        midpointCircle(g2, headX, headY, headR);
        g2.setTransform(noTilt);

        // hair and face ride the same tilt as the head
        g2.setTransform(tilt);

        // small tuft of hair on the crown
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

        // ---- face ----
        double eyeOpen;
        if (t < EYES_CLOSE_START) eyeOpen = 1.0;
        else if (t < EYES_CLOSE_END) eyeOpen = 1.0 - (t - EYES_CLOSE_START) / (EYES_CLOSE_END - EYES_CLOSE_START);
        else if (t < EYES_REOPEN_START) eyeOpen = 0.0;
        else eyeOpen = Math.min(1.0, (t - EYES_REOPEN_START) / (EYES_REOPEN_END - EYES_REOPEN_START));

        int eyeY = headY - 8;
        int eyeLX = headX - 14, eyeRX = headX + 6;
        if (eyeOpen > 0.05) {
            // round open eyes
            int h = (int) (9 * eyeOpen);
            g2.fillOval(eyeLX - 4, eyeY - h / 2, 8, h);
            g2.fillOval(eyeRX - 4, eyeY - h / 2, 8, h);
            // eyebrows
            bezierCurve(g2, eyeLX - 8, eyeY - 14, eyeLX - 4, eyeY - 18, eyeLX + 3, eyeY - 18, eyeLX + 7, eyeY - 14);
            bezierCurve(g2, eyeRX - 7, eyeY - 14, eyeRX - 3, eyeY - 18, eyeRX + 4, eyeY - 18, eyeRX + 8, eyeY - 14);
        } else {
            // closed eyes = calm little curves
            bezierCurve(g2, eyeLX - 7, eyeY, eyeLX - 4, eyeY + 5, eyeLX + 3, eyeY + 5, eyeLX + 7, eyeY);
            bezierCurve(g2, eyeRX - 7, eyeY, eyeRX - 4, eyeY + 5, eyeRX + 3, eyeY + 5, eyeRX + 7, eyeY);
        }

        // mouth: a wide, calm smile
        bezierCurve(g2, headX - 15, headY + 12, headX - 7, headY + 24, headX + 7, headY + 24, headX + 15, headY + 12);

        g2.setTransform(noTilt);
    }

    // ==================================================================
    //  MEMORY SCENE: two stickmen kicking a ball on a sunny field
    // ==================================================================

    private BufferedImage memoryBackdrop;

    private BufferedImage buildMemoryBackdrop() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = img.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // warm late-afternoon sky, so the memory reads golden next to the blue night
        GradientPaint sky = new GradientPaint(0, 0, new Color(250, 200, 120),
                                              0, 400, new Color(255, 236, 190));
        bg.setPaint(sky);
        bg.fillRect(0, 0, 600, 400);

        // low sun with a soft halo
        RadialGradientPaint halo = new RadialGradientPaint(
            new Point(470, 130), 130f,
            new float[]{0f, 0.45f, 1f},
            new Color[]{new Color(255, 245, 200, 200),
                        new Color(255, 225, 150, 90),
                        new Color(255, 220, 140, 0)});
        bg.setPaint(halo);
        bg.fillOval(340, 0, 260, 260);
        bg.setColor(new Color(255, 250, 225));
        bg.fillOval(470 - 34, 130 - 34, 68, 68);

        // distant hills: three overlapping mounds, the middle one filling the dip
        // that two mounds alone would leave between them
        bg.setColor(new Color(150, 175, 110));
        bg.fillOval(-140, 336, 470, 160);
        bg.fillOval(130, 352, 330, 150);
        bg.fillOval(300, 342, 460, 160);

        // field
        GradientPaint field = new GradientPaint(0, 395, new Color(126, 176, 88),
                                                0, 600, new Color(86, 138, 62));
        bg.setPaint(field);
        bg.fillRect(0, 395, 600, 205);

        bg.dispose();
        return img;
    }

    // one standing stickman. `kick` is 0..1: how far the near leg has swung through.
    // faceRight flips which way he is turned.
    private void drawPlayer(Graphics2D g2, int x, int groundY, double kick, boolean faceRight, double bob) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;

        int headR = 16;
        int headY = (int) (groundY - 128 + bob);
        int shoulderY = (int) (groundY - 106 + bob);
        int hipY = (int) (groundY - 58 + bob);

        g2.setColor(new Color(25, 25, 25));

        // torso
        bresenhamLine(g2, x, shoulderY, x, hipY, t2);

        // arms swing out opposite the kicking leg, which keeps him balanced
        double armSwing = kick * 0.9;
        int handAY = (int) (shoulderY + 40 - armSwing * 34);
        int handBY = (int) (shoulderY + 36 + armSwing * 12);
        bresenhamLine(g2, x, shoulderY + 4, x - dir * (int) (30 + armSwing * 16), handAY, t2);
        bresenhamLine(g2, x, shoulderY + 4, x + dir * (int) (24 + armSwing * 10), handBY, t2);

        // planted leg
        int plantFootX = x - dir * 12;
        bresenhamLine(g2, x, hipY, plantFootX, groundY, t2);
        g2.fillOval(plantFootX - dir * 8 - 4, groundY - 4, 16, 8);

        // kicking leg: knee lifts and the shin snaps forward as kick goes 0 -> 1
        int kneeX = x + dir * (int) (14 + kick * 26);
        int kneeY = (int) (hipY + 30 - kick * 22);
        int footX = x + dir * (int) (24 + kick * 54);
        int footY = (int) (groundY - kick * 40);
        bresenhamLine(g2, x, hipY, kneeX, kneeY, t2);
        bresenhamLine(g2, kneeX, kneeY, footX, footY, t2);
        g2.fillOval(footX - 6, footY - 4, 16, 8);

        // head, filled so the field never shows through
        g2.setColor(Color.WHITE);
        g2.fillOval(x - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(new Color(25, 25, 25));
        midpointCircle(g2, x, headY, headR);
        // neck
        bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);

        // happy little face turned the way he is playing
        int ex = x + dir * 3;
        g2.fillOval(ex - dir * 6 - 2, headY - 6, 4, 5);
        g2.fillOval(ex + dir * 2 - 2, headY - 6, 4, 5);
        bezierCurve(g2, ex - 7, headY + 4, ex - 3, headY + 10, ex + 3, headY + 10, ex + 7, headY + 4);
    }

    // the football itself: midpoint circle for the rim, plus a few panels
    private void drawBall(Graphics2D g2, int cx, int cy, int r, double spin) {
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(new Color(25, 25, 25));
        midpointCircle(g2, cx, cy, r);

        // pentagon panels, rotated by how far the ball has spun
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
        g2.fillPolygon(px, py, 5);
        // spokes from each corner of the centre panel out to the rim
        for (int i = 0; i < 5; i++) {
            double a = Math.toRadians(-90 + i * 72);
            int ox = cx + (int) (r * 0.95 * Math.cos(a));
            int oy = cy + (int) (r * 0.95 * Math.sin(a));
            bresenhamLine(g2, px[i], py[i], ox, oy, 1);
        }
        g2.setTransform(keep);
    }

    private static final Color INK = new Color(25, 25, 25);

    // ===== beats of the bicycle-kick sequence, in seconds from the memory start =====
    static final double RUN_END = 1.45;    // he has arrived under the dropping ball
    static final double CONTACT = 1.95;    // boot meets ball
    static final double FLIGHT_END = 3.8;  // ball has screamed off the pitch
    static final double HIT_U = 0.26;      // fraction of the flight when it reaches the keeper
    static final double HIT_TIME = CONTACT + HIT_U * (FLIGHT_END - CONTACT);
    static final int KEEPER_X = 420;       // where he is standing when it arrives
    static final int HIT_X = 402, HIT_Y = 392;   // where the ball buries itself in him
    static final int BLAST_DX = 290;       // how far the pair travel before leaving the frame

    // a running stickman, legs cycling
    private void drawRunner(Graphics2D g2, int x, int groundY, double phase) {
        int t2 = 2;
        int headR = 15;
        double bounce = Math.abs(Math.sin(phase * Math.PI)) * 6;
        int headY = (int) (groundY - 126 - bounce);
        int shoulderY = (int) (groundY - 104 - bounce);
        int hipY = (int) (groundY - 56 - bounce);

        g2.setColor(INK);
        // leaning forward into the run
        bresenhamLine(g2, x + 6, shoulderY, x, hipY, t2);

        double swing = Math.sin(phase * Math.PI * 2);
        // arms pumping
        bresenhamLine(g2, x + 5, shoulderY + 5, x + 5 + (int) (26 * swing), shoulderY + 26 - (int) (14 * swing), t2);
        bresenhamLine(g2, x + 5, shoulderY + 5, x + 5 - (int) (26 * swing), shoulderY + 26 + (int) (14 * swing), t2);

        // legs cycling in opposition
        for (int leg = 0; leg < 2; leg++) {
            double s = (leg == 0) ? swing : -swing;
            int kneeX = x + (int) (16 * s);
            int kneeY = hipY + 28 - (int) (12 * Math.max(0, s));
            int footX = x + (int) (30 * s);
            int footY = groundY - (int) (26 * Math.max(0, s));
            bresenhamLine(g2, x, hipY, kneeX, kneeY, t2);
            bresenhamLine(g2, kneeX, kneeY, footX, footY, t2);
            g2.fillOval(footX - 6, footY - 4, 16, 8);
        }

        g2.setColor(Color.WHITE);
        g2.fillOval(x - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, x, headY, headR);
        bresenhamLine(g2, x + 3, headY + headR, x + 6, shoulderY, t2);
        g2.fillOval(x + 1, headY - 6, 4, 5);
        g2.fillOval(x + 9, headY - 6, 4, 5);
    }

    // where the striking boot sits, relative to the kicker's hip, at full extension.
    // the ball is placed here at contact so foot and ball actually meet.
    static final int FOOT_DX = 50, FOOT_DY = -58;

    // the hero shot: inverted in mid-air, striking leg whipping up through the ball.
    // drawn directly in its final orientation - rotating a standing figure put the
    // kicking foot on the wrong side entirely.
    // `windup` 0..1 swings him from upright into the full inverted pose.
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

        // torso rolls back: shoulders drop behind and below the hips
        int shX = cx - (int) (30 * w);
        int shY = cy + (int) (16 * w) - (int) (34 * (1 - w));
        int hdX = cx - (int) (52 * w);
        int hdY = cy + (int) (30 * w) - (int) (56 * (1 - w));

        g2.setColor(INK);
        bresenhamLine(g2, shX, shY, cx, cy, t2);

        // arms flung wide for balance
        bresenhamLine(g2, shX, shY, shX - 26, shY - 26, t2);
        bresenhamLine(g2, shX, shY, shX - 16, shY + 40, t2);

        // striking leg: snaps up and forward through the ball
        int kneeX = cx + (int) (22 * w);
        int kneeY = cy - (int) (26 * w) + (int) (28 * (1 - w));
        int footX = cx + (int) (FOOT_DX * w);
        int footY = cy + (int) (FOOT_DY * w) + (int) (56 * (1 - w));
        bresenhamLine(g2, cx, cy, kneeX, kneeY, t2);
        bresenhamLine(g2, kneeX, kneeY, footX, footY, t2);
        g2.fillOval(footX - 8, footY - 5, 18, 9);

        // trailing leg drives the other way
        int k2x = cx + (int) (10 * w);
        int k2y = cy + 30;
        int f2x = cx + (int) (42 * w);
        int f2y = cy + 46;
        bresenhamLine(g2, cx, cy, k2x, k2y, t2);
        bresenhamLine(g2, k2x, k2y, f2x, f2y, t2);
        g2.fillOval(f2x - 8, f2y - 4, 18, 9);

        // neck first, so the filled head covers where it would cross the face
        bresenhamLine(g2, hdX, hdY, shX, shY, t2);

        g2.setColor(Color.WHITE);
        g2.fillOval(hdX - headR, hdY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, hdX, hdY, headR);

        // fierce face, kept small so the head still reads as a head
        g2.fillOval(hdX - 8, hdY - 5, 4, 4);
        g2.fillOval(hdX + 4, hdY - 5, 4, 4);
        g2.fillOval(hdX - 4, hdY + 4, 8, 6);

        g2.setTransform(keep);
    }

    // keeper hurling himself sideways
    private void drawDiver(Graphics2D g2, int cx, int cy, double rotDeg, double reach) {
        AffineTransform keep = g2.getTransform();
        AffineTransform tx = new AffineTransform(keep);
        tx.rotate(Math.toRadians(rotDeg), cx, cy);
        g2.setTransform(tx);

        int t2 = 2;
        int headR = 16;
        g2.setColor(INK);
        // stretched out flat, head and hands leading to the right, chasing the ball
        bresenhamLine(g2, cx + 20, cy, cx - 40, cy + 6, t2);
        // arms thrown out after the ball
        int r = (int) (reach * 20);
        bresenhamLine(g2, cx + 14, cy + 2, cx + 52 + r, cy - 20 - r, t2);
        bresenhamLine(g2, cx + 14, cy + 6, cx + 48 + r, cy + 14, t2);
        // legs trailing behind
        bresenhamLine(g2, cx - 40, cy + 6, cx - 70, cy - 12, t2);
        bresenhamLine(g2, cx - 70, cy - 12, cx - 96, cy + 4, t2);
        g2.fillOval(cx - 106, cy, 18, 9);
        bresenhamLine(g2, cx - 40, cy + 6, cx - 68, cy + 26, t2);
        bresenhamLine(g2, cx - 68, cy + 26, cx - 94, cy + 22, t2);
        g2.fillOval(cx - 104, cy + 18, 18, 9);

        g2.setColor(Color.WHITE);
        g2.fillOval(cx + 20 - headR, cy - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, cx + 20, cy, headR);
        g2.fillOval(cx + 18, cy - 6, 4, 4);
        g2.fillOval(cx + 27, cy - 6, 4, 4);

        g2.setTransform(keep);
    }

    // Cristiano Ronaldo's iconic "SIUUU" Celebration with mid-air jump, 180 spin, wide-legged power slam, and shouting mouth ("ปากหว่อ")
    private void drawSiuCelebrate(Graphics2D g2, int startX, int groundY, double ct) {
        int t2 = 2;
        int headR = 16;

        if (ct < 0.45) {
            // ---- STEP 1: SPRINT & HIGH LEAP WITH ARMS RAISING ----
            double u = ct / 0.45;
            int x = (int) (startX + 40 * u);
            // Parabolic leap height
            double jumpH = Math.sin(u * Math.PI) * 75;
            int currentGroundY = (int) (groundY - jumpH);

            int hipY = currentGroundY - 56;
            int shoulderY = currentGroundY - 104;
            int headY = currentGroundY - 128;

            g2.setColor(INK);
            // Torso
            bresenhamLine(g2, x, shoulderY, x, hipY, t2);

            // Arms pumping up preparing for the spin slam
            double armAngle = u * Math.PI;
            int leftHandX = x - (int) (25 * Math.cos(armAngle));
            int leftHandY = shoulderY - (int) (30 * Math.sin(armAngle));
            int rightHandX = x + (int) (25 * Math.cos(armAngle));
            int rightHandY = shoulderY - (int) (30 * Math.sin(armAngle));
            bresenhamLine(g2, x, shoulderY + 4, leftHandX, leftHandY, t2);
            bresenhamLine(g2, x, shoulderY + 4, rightHandX, rightHandY, t2);

            // Legs tucked during jump
            int k1x = x - 14, k1y = hipY + 22;
            int f1x = x - 20, f1y = hipY + 42;
            int k2x = x + 14, k2y = hipY + 20;
            int f2x = x + 22, f2y = hipY + 40;
            bresenhamLine(g2, x, hipY, k1x, k1y, t2);
            bresenhamLine(g2, k1x, k1y, f1x, f1y, t2);
            bresenhamLine(g2, x, hipY, k2x, k2y, t2);
            bresenhamLine(g2, k2x, k2y, f2x, f2y, t2);
            g2.fillOval(f1x - 6, f1y - 4, 14, 8);
            g2.fillOval(f2x - 6, f2y - 4, 14, 8);

            // Head & face looking forward/up
            bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
            g2.setColor(Color.WHITE);
            g2.fillOval(x - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, x, headY, headR);

            // Focused eyes & open mouth preparing
            g2.fillOval(x - 6, headY - 5, 4, 4);
            g2.fillOval(x + 2, headY - 5, 4, 4);
            fillMidpointEllipse(g2, x, headY + 5, 3, 4, INK);

        } else {
            // ---- STEP 2: THE ICONIC "SIUUU" IMPACT LANDING POSE ----
            double landTime = ct - 0.45;
            int x = startX + 40;

            // Screen shake right on landing impact
            if (landTime < 0.25) {
                int shake = (int) ((1.0 - landTime / 0.25) * 6 * Math.sin(landTime * 80));
                x += shake;
            }

            int hipY = groundY - 52;
            int shoulderY = groundY - 100;
            int headY = groundY - 124;

            // Ground impact shockwave on touch-down
            if (landTime < 0.40) {
                double pu = landTime / 0.40;
                int ringR = (int) (10 + pu * 70);
                int ringAlpha = (int) (220 * (1.0 - pu));
                g2.setColor(new Color(255, 255, 255, ringAlpha));
                midpointCircle(g2, x, groundY - 2, ringR);
                // Grass dust puffs to both sides
                fillMidpointEllipse(g2, x - 35 - (int)(pu * 25), groundY - 4, 18, 6, new Color(160, 200, 140, ringAlpha));
                fillMidpointEllipse(g2, x + 35 + (int)(pu * 25), groundY - 4, 18, 6, new Color(160, 200, 140, ringAlpha));
            }

            // Contact shadow under both feet
            g2.setColor(new Color(0, 0, 0, 130));
            fillMidpointEllipse(g2, x, groundY - 2, 45, 8, new Color(0, 0, 0, 130));

            g2.setColor(INK);

            // Torso: Puffed chest, confident stance
            bresenhamLine(g2, x, shoulderY, x, hipY, t2 + 1);

            // Arms: Iconic SIU downward-backward thrust (arms flung down and back with power)
            int leftArmElbowX = x - 26, leftArmElbowY = shoulderY + 22;
            int leftHandX = x - 42, leftHandY = shoulderY + 52;
            int rightArmElbowX = x + 26, rightArmElbowY = shoulderY + 22;
            int rightHandX = x + 42, rightHandY = shoulderY + 52;

            bresenhamLine(g2, x, shoulderY + 4, leftArmElbowX, leftArmElbowY, t2);
            bresenhamLine(g2, leftArmElbowX, leftArmElbowY, leftHandX, leftHandY, t2);
            bresenhamLine(g2, x, shoulderY + 4, rightArmElbowX, rightArmElbowY, t2);
            bresenhamLine(g2, rightArmElbowX, rightArmElbowY, rightHandX, rightHandY, t2);

            // Clenched fists at hand positions
            fillMidpointCircle(g2, leftHandX, leftHandY, 4, INK);
            fillMidpointCircle(g2, rightHandX, rightHandY, 4, INK);

            // Legs: Wide power stance (feet planted wide apart)
            int knee1X = x - 22, knee1Y = groundY - 24;
            int foot1X = x - 34, foot1Y = groundY - 4;
            int knee2X = x + 22, knee2Y = groundY - 24;
            int foot2X = x + 34, foot2Y = groundY - 4;

            bresenhamLine(g2, x, hipY, knee1X, knee1Y, t2);
            bresenhamLine(g2, knee1X, knee1Y, foot1X, foot1Y, t2);
            bresenhamLine(g2, x, hipY, knee2X, knee2Y, t2);
            bresenhamLine(g2, knee2X, knee2Y, foot2X, foot2Y, t2);

            // Solid planted shoe soles
            g2.fillOval(foot1X - 10, foot1Y - 4, 18, 9);
            g2.fillOval(foot2X - 8, foot2Y - 4, 18, 9);

            // Head
            bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
            g2.setColor(Color.WHITE);
            g2.fillOval(x - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, x, headY, headR);

            // Intense eyes looking straight forward
            g2.fillOval(x - 8, headY - 6, 4, 5);
            g2.fillOval(x + 4, headY - 6, 4, 5);

            // Slanted confident eyebrows
            bezierCurve(g2, x - 11, headY - 11, x - 7, headY - 13, x - 3, headY - 12, x, headY - 9);
            bezierCurve(g2, x + 11, headY - 11, x + 7, headY - 13, x + 3, headY - 12, x, headY - 9);

            // "ปากหว่อ" (The iconic wide round shouting "O / WOOO / SIUUU" mouth)
            // Big open oval mouth
            fillMidpointEllipse(g2, x, headY + 5, 6, 8, INK);
            // Tongue / depth highlight inside mouth
            g2.setColor(new Color(210, 60, 70));
            fillMidpointEllipse(g2, x, headY + 8, 4, 3, new Color(210, 60, 70));

            // Anime hype speed lines around the SIUUU power pose
            if (landTime > 0.05 && landTime < 0.65) {
                drawSpeedLines(g2, x, shoulderY + 10, Math.min(1.0, (0.65 - landTime) / 0.4), 14, 3);
            }

            // "SIUUU!" Comic / Anime Action Text Floating Above Head
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

            // Thick comic black outline
            g2.setColor(new Color(20, 20, 20));
            int[] ox = {-2, 0, 2, -2, 2, -2, 0, 2, -3, 3, 0, 0};
            int[] oy = {-2, -2, -2, 0, 0, 2, 2, 2, 0, 0, -3, 3};
            for (int i = 0; i < ox.length; i++) {
                g2.drawString(siuText, tx + ox[i], ty + oy[i]);
            }

            // Vibrant golden yellow gradient fill
            GradientPaint goldGrad = new GradientPaint(
                0, ty - th, new Color(255, 255, 140),
                0, ty, new Color(255, 195, 20)
            );
            g2.setPaint(goldGrad);
            g2.drawString(siuText, tx, ty);

            // Pop sparkle stars around text
            if (landTime < 0.6) {
                double spkAlpha = Math.max(0, 1.0 - landTime / 0.6);
                g2.setColor(new Color(255, 240, 100, (int) (240 * spkAlpha)));
                g2.fillOval(tx - 12, ty - th / 2, 5, 5);
                g2.fillOval(tx + tw + 6, ty - th / 2 - 4, 6, 6);
                g2.fillOval(tx + tw / 2 + 10, ty - th - 6, 4, 4);
            }

            g2.setTransform(oldTxtTx);
        }
    }

    // flattened by the shot: limbs flailing, eyes crossed out, tumbling through the air
    private void drawFlungPlayer(Graphics2D g2, int cx, int cy, double spinDeg) {
        AffineTransform keep = g2.getTransform();
        AffineTransform tx = new AffineTransform(keep);
        tx.rotate(Math.toRadians(spinDeg), cx, cy);
        g2.setTransform(tx);

        int t2 = 2;
        int headR = 16;
        g2.setColor(INK);

        bresenhamLine(g2, cx, cy - 4, cx, cy + 32, t2);
        // arms and legs thrown out every which way
        bresenhamLine(g2, cx, cy + 2, cx - 36, cy - 22, t2);
        bresenhamLine(g2, cx, cy + 2, cx + 34, cy - 16, t2);
        bresenhamLine(g2, cx, cy + 32, cx - 26, cy + 56, t2);
        bresenhamLine(g2, cx, cy + 32, cx + 28, cy + 54, t2);
        g2.fillOval(cx - 36, cy + 52, 18, 9);
        g2.fillOval(cx + 24, cy + 50, 18, 9);

        // neck before the fill so it never crosses the face
        bresenhamLine(g2, cx, cy - 4, cx, cy - 24, t2);
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - headR, cy - 26 - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, cx, cy - 26, headR);

        // crossed-out eyes and a shocked open mouth
        int ey = cy - 31;
        bresenhamLine(g2, cx - 11, ey - 4, cx - 4, ey + 3, 1);
        bresenhamLine(g2, cx - 4, ey - 4, cx - 11, ey + 3, 1);
        bresenhamLine(g2, cx + 4, ey - 4, cx + 11, ey + 3, 1);
        bresenhamLine(g2, cx + 11, ey - 4, cx + 4, ey + 3, 1);
        g2.fillOval(cx - 5, cy - 21, 10, 8);

        g2.setTransform(keep);
    }

    // anime radial speed lines bursting out of a point
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

    // the moment of contact: expanding shockwave ring plus a spike burst
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

    // blazing comet tail behind the struck ball
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
            g2.fillOval(tx - rr, ty - rr, rr * 2, rr * 2);
        }
    }

    // ft = seconds since the memory started
    private void drawMemoryScene(Graphics2D g2, double ft) {
        final int groundY = 500;
        final int ballR = 17;
        // the kicker's hip at the moment of contact, and the point his boot reaches
        final int hipCX = 250, hipCY = 300;
        final int kickX = hipCX + FOOT_DX, kickY = hipCY + FOOT_DY;

        // ---- screen shake: once when the boot connects, again when it hits him ----
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

        // his friend is out there waiting the whole time, not conjured up at the last second
        double keeperBob = Math.sin(ft * 2.4) * 2;

        if (ft < CONTACT) {
            // ---------- run-up: ball drops in, he sprints under it ----------
            double bu = Math.min(1, ft / CONTACT);
            int ballX = (int) (70 + (kickX - 70) * bu);
            int ballY = (int) (60 + (kickY - 60) * bu);

            drawPlayer(g2, KEEPER_X, groundY, 0, false, keeperBob);

            if (ft < RUN_END) {
                double u = ft / RUN_END;
                drawRunner(g2, (int) (90 + 150 * u), groundY, ft * 3.2);
            } else {
                // ---------- the leap: he launches and rolls upside down ----------
                double lu = (ft - RUN_END) / (CONTACT - RUN_END);
                int cx = (int) (hipCX - 10 * (1 - lu));
                int cy = (int) (groundY - 60 - (groundY - 60 - hipCY) * lu);
                drawSpeedLines(g2, cx, cy, lu * 0.7, 14, 1);
                drawBicycleKicker(g2, cx, cy, lu, 0);
            }
            drawBall(g2, ballX, ballY, ballR, ft * 6);

        } else if (ft < FLIGHT_END) {
            // ---------- the strike, the hit, and both of them leaving the pitch ----------
            double u = (ft - CONTACT) / (FLIGHT_END - CONTACT);

            // he hangs inverted for a beat, then drops back to earth
            int cx = (int) (hipCX + 24 * u);
            int cy = (int) Math.min(groundY - 46, hipCY + 320 * u * u);
            drawSpeedLines(g2, kickX, kickY, Math.max(0, 1 - u * 3), 16, 2);
            drawBicycleKicker(g2, cx, cy, Math.max(0, 1 - u * 1.4), 26 * u);

            if (u < HIT_U) {
                // ball screams straight at the keeper, who has no time to move
                double bu = u / HIT_U;
                int bx = (int) (kickX + (HIT_X - kickX) * bu);
                int by = (int) (kickY + (HIT_Y - kickY) * bu);

                drawPlayer(g2, KEEPER_X, groundY, 0, false, keeperBob);
                drawBallTrail(g2, bx, by, kickX, kickY, ballR, Math.max(bu, 0.001));
                drawBall(g2, bx, by, ballR, ft * 26);
                drawImpactBurst(g2, kickX, kickY, u * 4.5);

            } else {
                // smashed: he is carried off the right edge stuck to the ball
                double fu = (u - HIT_U) / (1 - HIT_U);
                int bx = (int) (HIT_X + BLAST_DX * fu);
                int by = (int) (HIT_Y - 130 * fu + 60 * fu * fu);

                drawSpeedLines(g2, bx, by, Math.max(0, 1 - fu * 1.6), 14, 5);
                // he tumbles along, pinned just ahead of the ball
                drawFlungPlayer(g2, bx + 34, by + 4, 300 * fu);
                drawBallTrail(g2, bx, by, HIT_X, HIT_Y, ballR, Math.max(fu, 0.001));
                drawBall(g2, bx, by, ballR, ft * 26);
                drawImpactBurst(g2, HIT_X, HIT_Y, fu * 2.6);
            }

        } else {
            // ---------- aftermath: he hits Cristiano Ronaldo's iconic SIUUU celebration ----------
            drawSiuCelebrate(g2, 240, groundY, ft - FLIGHT_END);
        }

        g2.setTransform(steady);

        // a white pop right on contact, painted over everything
        double flashT = Math.abs(ft - CONTACT);
        if (flashT < 0.14) {
            int a = (int) (200 * (1 - flashT / 0.14));
            g2.setColor(new Color(255, 255, 255, a));
            g2.fillRect(0, 0, 600, 600);
        }
    }

    // ==================================================================
    // 7. MEMORY SCENE 3: CHILDHOOD FRIENDS PLAYING IN A STREAM
    // ==================================================================

    private void drawStreamScene(Graphics2D g2, double st) {
        // Put the waterline around the friends' waists instead of below their feet.
        final int waterY = 365;

        // Bright outdoor memory backdrop: trees, a bank, and a flowing stream.
        GradientPaint sky = new GradientPaint(0, 0, new Color(115, 205, 245),
                                              0, 430, new Color(225, 248, 255));
        g2.setPaint(sky);
        g2.fillRect(0, 0, 600, 600);

        g2.setColor(new Color(90, 175, 105));
        g2.fillOval(-80, 205, 330, 250);
        g2.fillOval(360, 185, 350, 270);
        g2.setColor(new Color(55, 145, 78));
        g2.fillOval(30, 280, 180, 145);
        g2.fillOval(420, 265, 180, 160);

        // Tree trunks frame the stream and establish a clear outdoor setting.
        g2.setColor(new Color(105, 70, 42));
        g2.fillRect(35, 130, 34, 300);
        g2.fillRect(525, 115, 38, 315);
        g2.setColor(new Color(45, 125, 68));
        g2.fillOval(0, 75, 125, 110);
        g2.fillOval(475, 55, 145, 120);

        // Near bank, stream body, and soft current lines.
        g2.setColor(new Color(185, 145, 78));
        g2.fillRect(0, waterY - 14, 600, 30);
        g2.setColor(new Color(45, 170, 215));
        g2.fillRect(0, waterY + 2, 600, 230);
        g2.setColor(new Color(95, 215, 238, 180));
        for (int i = 0; i < 9; i++) {
            double wave = Math.sin(st * 2.5 + i * 0.8) * 8;
            bezierCurve(g2, 20 + i * 72, waterY + 45 + i % 3 * 45,
                    45 + i * 72, waterY + 32 + wave + i % 3 * 45,
                    75 + i * 72, waterY + 58 - wave + i % 3 * 45,
                    105 + i * 72, waterY + 43 + i % 3 * 45, 2);
        }

        // Draw the friends already standing in the stream first. The water surface
        // is drawn over their lower bodies so they read as being waist-deep.
        drawStreamFriend(g2, 105, waterY, 0, new Color(245, 100, 85), st);
        drawStreamFriend(g2, 235, waterY, 1, new Color(255, 205, 65), st);
        drawStreamFriend(g2, 365, waterY, 2, new Color(105, 115, 240), st);

        drawStreamSurface(g2, waterY, st);

        // The fourth friend is in front of the water while jumping into it.
        drawStreamFriend(g2, 505, waterY, 3, new Color(235, 105, 175), st);
    }

    private void drawStreamSurface(Graphics2D g2, int waterY, double st) {
        // Translucent foreground water hides the submerged legs and creates a
        // clear waterline across the friends' waists.
        g2.setColor(new Color(35, 155, 205, 225));
        g2.fillRect(0, waterY - 4, 600, 230);

        g2.setColor(new Color(185, 245, 255, 210));
        for (int i = 0; i < 12; i++) {
            double x = 18 + i * 54;
            double y = waterY + 4 + (i % 3) * 10;
            double wave = Math.sin(st * 3.0 + i) * 5;
            bezierCurve(g2, x - 16, y, x - 7, y - 3 + wave,
                    x + 7, y + 3 - wave, x + 16, y, 2);
        }

        // Ripples around the three friends who are standing in the stream.
        for (int x : new int[]{105, 235, 365}) {
            bezierCurve(g2, x - 28, waterY + 10, x - 14, waterY + 4,
                    x + 14, waterY + 4, x + 28, waterY + 10, 2);
        }
    }

    private void drawStreamFriend(Graphics2D g2, int x, int waterY, int action,
                                  Color shirtColor, double st) {
        final Color ink = new Color(35, 45, 55);
        double actionTime = st + action * 0.55;
        double bob = Math.sin(actionTime * 4.0) * 3.0;
        boolean jumping = action == 3;
        double jump = jumping ? Math.max(0, Math.sin(st * 2.2 + 0.4)) * 70 : 0;
        int hipY = (int) (waterY - 5 - bob - jump);
        int shoulderY = hipY - 62;
        int headY = shoulderY - 24;

        g2.setColor(new Color(35, 125, 175, 120));
        if (action == 0) {
            // Water thrown toward the next friend.
            for (int i = 0; i < 7; i++) {
                double p = i / 6.0;
                int dx = (int) (18 + p * 72);
                int dy = (int) (shoulderY + 12 - 42 * Math.sin(p * Math.PI) + bob);
                fillMidpointCircle(g2, x + dx, dy, 2 + (i % 2), new Color(225, 250, 255, 220));
            }
        }

        g2.setColor(ink);
        // Body and legs remain in the same simple stickman style as the memories.
        bresenhamLine(g2, x, shoulderY, x, hipY, 2);
        int leftFootY = (int) (waterY + 2 - (jumping ? jump * 0.85 : 0));
        int rightFootY = (int) (waterY + 2 - (jumping ? jump * 0.75 : 0));
        int leftFootX = x - 18 - (jumping ? 8 : 0);
        int rightFootX = x + 20 + (jumping ? 7 : 0);
        bresenhamLine(g2, x, hipY, leftFootX, leftFootY, 2);
        bresenhamLine(g2, x, hipY, rightFootX, rightFootY, 2);
        fillMidpointCircle(g2, leftFootX, leftFootY, 4, ink);
        fillMidpointCircle(g2, rightFootX, rightFootY, 4, ink);

        int leftHandY = shoulderY + 12;
        int rightHandY = shoulderY + 12;
        if (action == 0) { // throwing water
            rightHandY = shoulderY - 8;
        } else if (action == 2) { // celebrating with both hands raised
            leftHandY = shoulderY - 38;
            rightHandY = shoulderY - 45;
        } else if (action == 3) { // arms up during the jump
            leftHandY = shoulderY - 28;
            rightHandY = shoulderY - 35;
        }
        bresenhamLine(g2, x, shoulderY + 5, x - 28, leftHandY, 2);
        bresenhamLine(g2, x, shoulderY + 5, x + 28, rightHandY, 2);
        fillMidpointCircle(g2, x - 28, leftHandY, 4, ink);
        fillMidpointCircle(g2, x + 28, rightHandY, 4, ink);

        // Shirt and head are drawn over the stick lines for a readable silhouette.
        g2.setColor(shirtColor);
        g2.fillOval(x - 13, shoulderY - 2, 26, 38);
        g2.setColor(new Color(255, 224, 185));
        g2.fillOval(x - 18, headY - 18, 36, 36);
        g2.setColor(ink);
        midpointCircle(g2, x, headY, 18);

        // Smiling face: eyes and a curved laughing mouth.
        g2.fillOval(x - 8, headY - 4, 3, 4);
        g2.fillOval(x + 5, headY - 4, 3, 4);
        bezierCurve(g2, x - 8, headY + 7, x - 3, headY + 14,
                    x + 4, headY + 14, x + 9, headY + 7, 2);

        if (jumping) {
            // The jumper lands into the stream at the bottom of the looped motion.
            double splash = Math.max(0, Math.sin(st * 2.2 + Math.PI / 2));
            for (int i = 0; i < 10; i++) {
                double angle = Math.PI * (0.1 + 0.8 * i / 9.0);
                int sx = x + (int) (Math.cos(angle) * (18 + splash * 24));
                int sy = waterY + 5 - (int) (Math.sin(angle) * (18 + splash * 24));
                fillMidpointCircle(g2, sx, sy, 2 + i % 2, new Color(235, 252, 255, 220));
            }
        }
    }

    // ==================================================================
    // 8. MEMORY SCENE 2: EPIC CHILDHOOD TOY SWORD FIGHT (ANIME BATTLE)
    // ==================================================================

    private BufferedImage swordBackdrop;

    private BufferedImage buildSwordBackdrop() {
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = img.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dramatic sunset gradient (Crimson-Purple -> Fiery Orange -> Golden Amber)
        LinearGradientPaint sky = new LinearGradientPaint(
            new Point2D.Float(300, 0), new Point2D.Float(300, 480),
            new float[]{0.0f, 0.35f, 0.70f, 1.0f},
            new Color[]{
                new Color(52, 18, 68),   // Dark twilight purple
                new Color(185, 45, 62),  // Crimson red
                new Color(238, 102, 35), // Fiery sunset orange
                new Color(255, 195, 85)  // Golden horizon glow
            }
        );
        bg.setPaint(sky);
        bg.fillRect(0, 0, 600, 480);

        // Giant setting sun with multi-layer glowing halos
        int sunX = 300, sunY = 240, sunR = 45;
        for (int r = sunR + 70; r >= sunR; r -= 5) {
            int alpha = (int) (35 * (1.0 - (double)(r - sunR) / 70.0));
            fillMidpointCircle(bg, sunX, sunY, r, new Color(255, 170, 70, alpha));
        }
        fillMidpointCircle(bg, sunX, sunY, sunR + 10, new Color(255, 220, 130, 80));
        fillMidpointCircle(bg, sunX, sunY, sunR, new Color(255, 248, 215));

        // Lab_03: Distant jagged mountains using Polygon
        int[] mtnX = {-20, 70, 160, 260, 350, 440, 530, 620, 620, -20};
        int[] mtnY = {420, 350, 410, 330, 390, 320, 380, 340, 480, 480};
        Polygon distantMtn = new Polygon(mtnX, mtnY, mtnX.length);
        bg.setColor(new Color(90, 26, 60, 200));
        bg.fillPolygon(distantMtn);

        // Lab_03: Mid-ground battlefield hills using Polygon & Bezier points
        int[] hillX = {-30, 40, 120, 200, 280, 350, 420, 500, 580, 630, 630, -30};
        int[] hillY = {440, 420, 400, 430, 450, 430, 410, 390, 430, 420, 500, 500};
        Polygon midHill = new Polygon(hillX, hillY, hillX.length);
        bg.setColor(new Color(60, 18, 42, 230));
        bg.fillPolygon(midHill);

        // Foreground battle arena ground
        LinearGradientPaint groundGrad = new LinearGradientPaint(
            new Point2D.Float(0, 460), new Point2D.Float(0, 600),
            new float[]{0f, 0.3f, 1f},
            new Color[]{new Color(42, 14, 25), new Color(30, 10, 18), new Color(18, 6, 12)}
        );
        bg.setPaint(groundGrad);
        bg.fillRect(0, 460, 600, 140);

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

        // Crossguard
        int gx1 = (int) (hx + Math.cos(perp) * 8);
        int gy1 = (int) (hy + Math.sin(perp) * 8);
        int gx2 = (int) (hx - Math.cos(perp) * 8);
        int gy2 = (int) (hy - Math.sin(perp) * 8);
        g2.setColor(new Color(30, 30, 30));
        bresenhamLine(g2, gx1, gy1, gx2, gy2, 2);

        // Handle / Pommel
        int hbx = (int) (hx - Math.cos(angle) * 12);
        int hby = (int) (hy - Math.sin(angle) * 12);
        g2.setColor(new Color(50, 50, 50));
        bresenhamLine(g2, hx, hy, hbx, hby, 2);
        fillMidpointCircle(g2, hbx, hby, 3, new Color(20, 20, 20));

        // Outer aura
        g2.setColor(new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 60));
        bresenhamLine(g2, hx, hy, tx, ty, width + 4);

        // Mid glow
        g2.setColor(new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 170));
        bresenhamLine(g2, hx, hy, tx, ty, width + 2);

        // Core line
        g2.setColor(coreColor);
        bresenhamLine(g2, hx, hy, tx, ty, width);

        // Glowing sword tip
        fillMidpointCircle(g2, tx, ty, width + 3, new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 120));
        fillMidpointCircle(g2, tx, ty, width + 1, coreColor);
    }

    private void drawScarf(Graphics2D g2, int neckX, int neckY, boolean faceRight, Color color, double time) {
        int dir = faceRight ? -1 : 1;
        double wave1 = Math.sin(time * 8.0) * 10;
        double wave2 = Math.cos(time * 8.0 + 1.2) * 14;

        g2.setColor(color);
        // Scarf tail 1
        bezierCurve(g2, neckX, neckY,
                    neckX + dir * 20, neckY + 8 + wave1 * 0.5,
                    neckX + dir * 35, neckY - 6 + wave1,
                    neckX + dir * 50, neckY + 12 + wave2);
        // Scarf tail 2
        bezierCurve(g2, neckX, neckY + 3,
                    neckX + dir * 18, neckY + 14 + wave2 * 0.4,
                    neckX + dir * 32, neckY + 2 + wave2,
                    neckX + dir * 46, neckY + 20 + wave1);
    }

    // =========================================================================
    // DYNAMIC SWORD FIGHT PROCEDURAL ANIMATION RIG (FLUID ANIME COMBAT)
    // =========================================================================

    // Lab_03 Polygon + Lab_02 Bresenham: Multi-layer luminous anime crescent slash wave (Demon Slayer style)
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

        // Broad translucent aura ribbon (Lab 3 Polygon)
        g2.setColor(new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 90));
        g2.fillPolygon(slashPoly);

        // Core bright energy arc with Bresenham's line algorithm (Lab 2)
        g2.setColor(new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 220));
        for (int i = 0; i < steps; i++) {
            bresenhamLine(g2, xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1], 2);
        }

        // White-hot inner slicing core (Lab 2)
        g2.setColor(new Color(255, 255, 255, 245));
        for (int i = 0; i < steps; i++) {
            bresenhamLine(g2, xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1], 0);
        }

        g2.setTransform(old);
    }

    // Battle stance: crouched low, blade charged with crackling sparks, scarf fluttering
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
        // Torso crouched low
        bresenhamLine(g2, x + dir * 6, shoulderY, x - dir * 6, hipY, t2);

        // Alert stance legs
        int f1x = x - dir * 26, f1y = groundY - 4;
        int f2x = x + dir * 22, f2y = groundY - 4;
        bresenhamLine(g2, x - dir * 6, hipY, x - dir * 16, groundY - 22, t2);
        bresenhamLine(g2, x - dir * 16, groundY - 22, f1x, f1y, t2);
        bresenhamLine(g2, x - dir * 6, hipY, x + dir * 12, groundY - 22, t2);
        bresenhamLine(g2, x + dir * 12, groundY - 22, f2x, f2y, t2);
        g2.fillOval(f1x - 6, f1y - 4, 16, 8);
        g2.fillOval(f2x - 6, f2y - 4, 16, 8);

        // Two-handed sword guard pointing forward-up
        int handX = x + dir * 18;
        int handY = shoulderY + 8;
        bresenhamLine(g2, x + dir * 6, shoulderY + 4, handX, handY, t2);
        bresenhamLine(g2, x - dir * 6, shoulderY + 4, handX - dir * 4, handY + 4, t2);

        double swordAngle = faceRight ? -35 : -145;
        double rad = Math.toRadians(swordAngle);
        int tipX = handX + (int) (Math.cos(rad) * 46);
        int tipY = handY + (int) (Math.sin(rad) * 46);
        drawGlowingSword(g2, handX, handY, tipX, tipY, swordAura, Color.WHITE, 2);

        // Sparks vibrating off blade tip
        if (Math.sin(st * 20) > 0.2) {
            fillMidpointCircle(g2, tipX + (int)(Math.cos(st*30)*6), tipY + (int)(Math.sin(st*30)*6), 2, swordAura);
        }

        drawScarf(g2, x, shoulderY, faceRight, scarfColor, st * 2);

        // Head
        bresenhamLine(g2, x + dir * 2, headY + headR, x + dir * 6, shoulderY, t2);
        g2.setColor(Color.WHITE);
        g2.fillOval(x + dir * 2 - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, x + dir * 2, headY, headR);

        int ex = x + dir * 5;
        g2.fillOval(ex - dir * 5 - 2, headY - 5, 4, 4);
        g2.fillOval(ex + dir * 3 - 2, headY - 5, 4, 4);
        bezierCurve(g2, ex - dir * 7, headY - 9, ex - dir * 3, headY - 13, ex + dir * 1, headY - 13, ex + dir * 5, headY - 9);
        bezierCurve(g2, ex - 4, headY + 5, ex, headY + 7, ex + 3, headY + 7, ex + 5, headY + 5);
    }

    // Dynamic anime ninja sprint: heavy forward tilt, full running leg strides, sparks dragging on ground
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
        // Torso leaning heavily forward into the sprint
        bresenhamLine(g2, torsoShX, shoulderY, torsoHipX, hipY, t2);

        // Fluid athletic running stride cycle
        double swing = Math.sin(phase * Math.PI * 2);
        for (int leg = 0; leg < 2; leg++) {
            double s = (leg == 0) ? swing : -swing;
            int kneeX = torsoHipX + dir * (int) (20 * s + 6);
            int kneeY = hipY + 24 - (int) (18 * Math.max(0, s));
            int footX = torsoHipX + dir * (int) (34 * s);
            int footY = groundY - (int) (24 * Math.max(0, s)) - 4;
            bresenhamLine(g2, torsoHipX, hipY, kneeX, kneeY, t2);
            bresenhamLine(g2, kneeX, kneeY, footX, footY, t2);
            g2.fillOval(footX - 6, footY - 4, 16, 8);
        }

        // Front arm pumping forward
        int frontHandX = torsoShX + dir * 28;
        int frontHandY = shoulderY + 18 - (int) (swing * 14);
        bresenhamLine(g2, torsoShX, shoulderY + 4, frontHandX, frontHandY, t2);

        // Back sword arm: holding glowing sword trailing behind
        int backHandX = torsoShX - dir * 24;
        int backHandY = shoulderY + 12;
        bresenhamLine(g2, torsoShX, shoulderY + 4, backHandX, backHandY, t2);

        // Sword blade trailing back-down with sparks on ground
        int swordTipX = backHandX - dir * 42;
        int swordTipY = backHandY + 26;
        drawGlowingSword(g2, backHandX, backHandY, swordTipX, swordTipY, swordAura, Color.WHITE, 2);

        // Sparks scraping along ground
        if (Math.sin(st * 40) > 0) {
            fillMidpointCircle(g2, swordTipX, groundY - 3, 2, new Color(255, 230, 100));
        }

        // Scarf streaming back horizontally
        drawScarf(g2, torsoShX - dir * 4, shoulderY, faceRight, scarfColor, st * 2);

        // Head thrust forward
        int headX = torsoShX + dir * 8;
        bresenhamLine(g2, headX, headY + headR, torsoShX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        g2.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        // Fierce battle expression
        int ex = headX + dir * 4;
        g2.fillOval(ex - dir * 5 - 2, headY - 5, 4, 5);
        g2.fillOval(ex + dir * 3 - 2, headY - 5, 4, 5);
        bezierCurve(g2, ex - dir * 7, headY - 9, ex - dir * 3, headY - 13, ex + dir * 1, headY - 13, ex + dir * 5, headY - 9);
        bezierCurve(g2, ex - dir * 5, headY + 5, ex - dir * 1, headY + 8, ex + dir * 3, headY + 8, ex + dir * 6, headY + 5);
    }

    // Dynamic combat strikes: jumping overhead smash, deep forward horizontal sweep, rising dragon slice
    private void drawSwordSlashAttacker(Graphics2D g2, int x, int groundY, boolean faceRight,
                                         Color swordAura, Color scarfColor, double strikeProgress, int strikeType, double st) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;
        double sp = Math.max(0, Math.min(1, strikeProgress));

        if (strikeType % 3 == 0) {
            // STYLE 1: Over-the-top jumping overhead downward smash!
            double hop = Math.sin(sp * Math.PI) * 28;
            int hipY = (int) (groundY - 56 - hop);
            int shoulderY = (int) (groundY - 96 - hop);
            int headY = (int) (groundY - 118 - hop);
            int torsoX = x + dir * (int) (22 * sp);

            g2.setColor(INK);
            // Torso lunging into smash
            bresenhamLine(g2, torsoX + dir * 10, shoulderY, torsoX - dir * 8, hipY, t2);

            // Wide spread legs
            int f1x = torsoX - dir * 26, f1y = groundY - (int) (hop * 0.4) - 4;
            int f2x = torsoX + dir * 26, f2y = groundY - 4;
            bresenhamLine(g2, torsoX - dir * 8, hipY, torsoX - dir * 14, groundY - 24, t2);
            bresenhamLine(g2, torsoX - dir * 14, groundY - 24, f1x, f1y, t2);
            bresenhamLine(g2, torsoX - dir * 8, hipY, torsoX + dir * 14, groundY - 24, t2);
            bresenhamLine(g2, torsoX + dir * 14, groundY - 24, f2x, f2y, t2);
            g2.fillOval(f1x - 6, f1y - 4, 16, 8);
            g2.fillOval(f2x - 6, f2y - 4, 16, 8);

            // Two hands holding sword swinging from overhead down
            double swingAngle = (faceRight ? -130 + sp * 170 : -50 - sp * 170);
            double srad = Math.toRadians(swingAngle);
            int handX = torsoX + dir * 16;
            int handY = shoulderY + (int) (Math.sin(sp * Math.PI) * 16);
            bresenhamLine(g2, torsoX + dir * 10, shoulderY + 4, handX, handY, t2);

            int tipX = handX + (int) (Math.cos(srad) * 48);
            int tipY = handY + (int) (Math.sin(srad) * 48);
            drawGlowingSword(g2, handX, handY, tipX, tipY, swordAura, Color.WHITE, 2);

            // Giant crescent slash ribbon
            drawAnimeSlashRibbon(g2, handX, handY, faceRight ? -130 : -50, faceRight ? 170 : -170, 48, swordAura);

            drawScarf(g2, torsoX, shoulderY, faceRight, scarfColor, st * 2);

            int headX = torsoX + dir * 8;
            bresenhamLine(g2, headX, headY + headR, torsoX + dir * 10, shoulderY, t2);
            g2.setColor(Color.WHITE);
            g2.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, headX, headY, headR);

            int ex = headX + dir * 4;
            g2.fillOval(ex - dir * 5 - 2, headY - 5, 4, 4);
            g2.fillOval(ex + dir * 3 - 2, headY - 5, 4, 4);
            g2.fillOval(ex - dir * 1 - 3, headY + 3, 7, 7);

        } else if (strikeType % 3 == 1) {
            // STYLE 2: Deep forward lunge horizontal roundhouse sweep!
            int torsoX = x + dir * (int) (32 * Math.sin(sp * Math.PI));
            int hipY = groundY - 48;
            int shoulderY = groundY - 88;
            int headY = groundY - 110;

            g2.setColor(INK);
            bresenhamLine(g2, torsoX + dir * 14, shoulderY, torsoX - dir * 10, hipY, t2);

            // Deep lunging legs
            int f1x = torsoX - dir * 34, f1y = groundY - 4;
            int f2x = torsoX + dir * 28, f2y = groundY - 4;
            bresenhamLine(g2, torsoX - dir * 10, hipY, torsoX - dir * 18, groundY - 18, t2);
            bresenhamLine(g2, torsoX - dir * 18, groundY - 18, f1x, f1y, t2);
            bresenhamLine(g2, torsoX - dir * 10, hipY, torsoX + dir * 18, groundY - 18, t2);
            bresenhamLine(g2, torsoX + dir * 18, groundY - 18, f2x, f2y, t2);
            g2.fillOval(f1x - 6, f1y - 4, 16, 8);
            g2.fillOval(f2x - 6, f2y - 4, 16, 8);

            // Wide horizontal sweep slash
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
            g2.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, headX, headY, headR);

            int ex = headX + dir * 4;
            g2.fillOval(ex - dir * 5 - 2, headY - 5, 4, 4);
            g2.fillOval(ex + dir * 3 - 2, headY - 5, 4, 4);
            g2.fillOval(ex - dir * 1 - 3, headY + 3, 6, 6);

        } else {
            // STYLE 3: Upward rising dragon blade slice!
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
            g2.fillOval(f1x - 6, f1y - 4, 16, 8);
            g2.fillOval(f2x - 6, f2y - 4, 16, 8);

            // Upward rising slash
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
            g2.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, headX, headY, headR);

            int ex = headX + dir * 4;
            g2.fillOval(ex - dir * 5 - 2, headY - 5, 4, 4);
            g2.fillOval(ex + dir * 3 - 2, headY - 5, 4, 4);
            g2.fillOval(ex - dir * 1 - 3, headY + 3, 6, 6);
        }
    }
    // =========================================================================
    // CHILDHOOD ACCIDENTAL HIT & CRYING ANIMATIONS (NOSTALGIC PLAYGROUND)
    // =========================================================================

    // The rival kid getting hit with a bonk, stumbling back, and falling on the grass
    private void drawHitAndFallKid(Graphics2D g2, int startX, int groundY, boolean faceRight,
                                   Color scarfColor, double fallProgress, double st) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;
        double fp = Math.max(0, Math.min(1, fallProgress));

        int x = (int) (startX + dir * 40 * fp);

        if (fp < 0.45) {
            // STEP 1: Reeling back from the toy sword bonk!
            double u = fp / 0.45;
            int hipY = (int) (groundY - 50 + u * 15);
            int shoulderY = (int) (groundY - 90 + u * 18);
            int headY = (int) (groundY - 114 + u * 20);

            g2.setColor(INK);
            // Torso reeling backwards
            bresenhamLine(g2, x - dir * 18, shoulderY, x - dir * 4, hipY, t2);

            // Legs buckling
            bresenhamLine(g2, x - dir * 4, hipY, x - dir * 16, groundY - 18, t2);
            bresenhamLine(g2, x - dir * 16, groundY - 18, x - dir * 26, groundY - 4, t2);
            bresenhamLine(g2, x - dir * 4, hipY, x + dir * 14, groundY - 14, t2);
            bresenhamLine(g2, x + dir * 14, groundY - 14, x + dir * 22, groundY - 4, t2);
            g2.fillOval(x - dir * 26 - 6, groundY - 8, 16, 8);
            g2.fillOval(x + dir * 22 - 6, groundY - 8, 16, 8);

            // Arms thrown up, dropped sword tumbling down
            int hand1X = x - dir * 28, hand1Y = shoulderY - 16;
            int hand2X = x + dir * 12, hand2Y = shoulderY - 10;
            bresenhamLine(g2, x - dir * 18, shoulderY + 4, hand1X, hand1Y, t2);
            bresenhamLine(g2, x - dir * 18, shoulderY + 4, hand2X, hand2Y, t2);

            // Dropped plastic sword falling down
            int dropSwordX = x + dir * (int)(25 + u * 20);
            int dropSwordY = (int)(shoulderY + u * 70);
            bresenhamLine(g2, dropSwordX, dropSwordY, dropSwordX + dir * 30, dropSwordY + 10, 2);

            // Head thrown back
            int headX = x - dir * 24;
            bresenhamLine(g2, headX, headY + headR, x - dir * 18, shoulderY, t2);
            g2.setColor(Color.WHITE);
            g2.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, headX, headY, headR);

            // Dazed star / shock eyes (> <)
            bezierCurve(g2, headX - 8, headY - 8, headX - 4, headY - 4, headX - 4, headY - 4, headX - 8, headY);
            bezierCurve(g2, headX + 8, headY - 8, headX + 4, headY - 4, headX + 4, headY - 4, headX + 8, headY);
            // Open shocked mouth
            g2.fillOval(headX - 4, headY + 3, 8, 7);

        } else {
            // STEP 2: Falling down and sitting on the grass
            double u = (fp - 0.45) / 0.55;
            int hipY = (int) (groundY - 35 + u * 5);
            int shoulderY = (int) (groundY - 72 + u * 4);
            int headY = (int) (groundY - 94 + u * 2);

            g2.setColor(INK);
            // Torso sitting
            bresenhamLine(g2, x, shoulderY, x - dir * 6, hipY, t2);

            // Legs splayed forward on grass
            int f1x = x + dir * 28, f1y = groundY - 4;
            int f2x = x + dir * 18, f2y = groundY - 4;
            bresenhamLine(g2, x - dir * 6, hipY, x + dir * 12, groundY - 14, t2);
            bresenhamLine(g2, x + dir * 12, groundY - 14, f1x, f1y, t2);
            bresenhamLine(g2, x - dir * 6, hipY, x, groundY - 14, t2);
            bresenhamLine(g2, x, groundY - 14, f2x, f2y, t2);
            g2.fillOval(f1x - 6, f1y - 4, 16, 8);
            g2.fillOval(f2x - 6, f2y - 4, 16, 8);

            // Hand holding head
            int handX = x + dir * 8;
            int handY = headY + 2;
            bresenhamLine(g2, x, shoulderY + 4, handX, handY, t2);
            bresenhamLine(g2, x, shoulderY + 4, x - dir * 18, groundY - 8, t2);

            // Dropped toy sword lying on the grass
            g2.setColor(new Color(220, 60, 40));
            bresenhamLine(g2, x + dir * 36, groundY - 4, x + dir * 72, groundY - 4, 2);
            bresenhamLine(g2, x + dir * 44, groundY - 8, x + dir * 44, groundY, 2);

            // Scarf
            drawScarf(g2, x, shoulderY, faceRight, scarfColor, st);

            // Head
            bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
            g2.setColor(Color.WHITE);
            g2.fillOval(x - headR, headY - headR, headR * 2, headR * 2);
            g2.setColor(INK);
            midpointCircle(g2, x, headY, headR);

            // Crying eyes starting
            bezierCurve(g2, x - 8, headY - 6, x - 4, headY - 9, x - 2, headY - 6, x, headY - 6);
            bezierCurve(g2, x + 1, headY - 6, x + 3, headY - 9, x + 6, headY - 6, x + 8, headY - 6);
            // Starting to pout/cry mouth
            bezierCurve(g2, x - 5, headY + 7, x, headY + 3, x + 3, headY + 3, x + 6, headY + 7);
        }
    }

    // The kid sitting on the ground wailing with hilarious anime waterfall tears
    private void drawCryingKid(Graphics2D g2, int x, int groundY, boolean faceRight, Color scarfColor, double cryTime) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;

        // Sobbing shake / bob
        double sob = Math.abs(Math.sin(cryTime * 14.0)) * 4;
        int hipY = (int) (groundY - 32 - sob * 0.5);
        int shoulderY = (int) (groundY - 68 - sob);
        int headY = (int) (groundY - 92 - sob);

        g2.setColor(INK);
        // Torso sitting
        bresenhamLine(g2, x, shoulderY, x - dir * 6, hipY, t2);

        // Legs splayed forward on grass
        int f1x = x + dir * 30, f1y = groundY - 4;
        int f2x = x + dir * 18, f2y = groundY - 4;
        bresenhamLine(g2, x - dir * 6, hipY, x + dir * 14, groundY - 14, t2);
        bresenhamLine(g2, x + dir * 14, groundY - 14, f1x, f1y, t2);
        bresenhamLine(g2, x - dir * 6, hipY, x + dir * 2, groundY - 14, t2);
        bresenhamLine(g2, x + dir * 2, groundY - 14, f2x, f2y, t2);
        g2.fillOval(f1x - 6, f1y - 4, 16, 8);
        g2.fillOval(f2x - 6, f2y - 4, 16, 8);

        // One hand rubbing head/eye, other hand on grass
        int handX = x + dir * 4;
        int handY = headY + 4;
        bresenhamLine(g2, x, shoulderY + 4, handX, handY, t2);
        bresenhamLine(g2, x, shoulderY + 4, x - dir * 18, groundY - 8, t2);

        // Dropped red plastic toy sword lying on the grass
        g2.setColor(new Color(220, 50, 40));
        bresenhamLine(g2, x + dir * 38, groundY - 4, x + dir * 76, groundY - 4, 2);
        bresenhamLine(g2, x + dir * 46, groundY - 8, x + dir * 46, groundY, 2);
        fillMidpointCircle(g2, x + dir * 38, groundY - 4, 2, new Color(40, 40, 40));

        // Scarf
        drawScarf(g2, x, shoulderY, faceRight, scarfColor, cryTime);

        // Head
        bresenhamLine(g2, x, headY + headR, x, shoulderY, t2);
        g2.setColor(Color.WHITE);
        g2.fillOval(x - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, x, headY, headR);

        // Crying squinting closed eyes (> <)
        int eyeLeftX = x - 5, eyeRightX = x + 5, eyeY = headY - 4;
        bezierCurve(g2, eyeLeftX - 4, eyeY - 4, eyeLeftX, eyeY, eyeLeftX, eyeY, eyeLeftX - 4, eyeY + 4);
        bezierCurve(g2, eyeRightX + 4, eyeY - 4, eyeRightX, eyeY, eyeRightX, eyeY, eyeRightX + 4, eyeY + 4);

        // Big wide open wailing mouth
        fillMidpointEllipse(g2, x, headY + 7, 7, 8, INK);
        g2.setColor(new Color(230, 80, 100));
        fillMidpointEllipse(g2, x, headY + 9, 5, 4, new Color(230, 80, 100));

        // Hilarious anime waterfall fountain tears (Lab 3 Bezier Curve + Lab 4 Midpoint Circle)
        g2.setColor(new Color(80, 190, 255));
        // Left eye tear stream
        bezierCurve(g2, eyeLeftX, eyeY,
                    eyeLeftX - 22, eyeY - 26,
                    eyeLeftX - 38, groundY - 30,
                    eyeLeftX - 48, groundY - 4, 18);
        bezierCurve(g2, eyeLeftX, eyeY + 2,
                    eyeLeftX - 18, eyeY - 20,
                    eyeLeftX - 32, groundY - 24,
                    eyeLeftX - 42, groundY - 4, 18);

        // Right eye tear stream
        bezierCurve(g2, eyeRightX, eyeY,
                    eyeRightX + 22, eyeY - 26,
                    eyeRightX + 38, groundY - 30,
                    eyeRightX + 48, groundY - 4, 18);
        bezierCurve(g2, eyeRightX, eyeY + 2,
                    eyeRightX + 18, eyeY - 20,
                    eyeRightX + 32, groundY - 24,
                    eyeRightX + 42, groundY - 4, 18);

        // Splashing tear puddles on ground
        fillMidpointCircle(g2, eyeLeftX - 46, groundY - 3, 3 + (int)(sob * 0.5), new Color(80, 190, 255, 180));
        fillMidpointCircle(g2, eyeRightX + 46, groundY - 3, 3 + (int)(sob * 0.5), new Color(80, 190, 255, 180));
        fillMidpointCircle(g2, eyeLeftX - 28, eyeY - 14, 2, new Color(140, 220, 255));
        fillMidpointCircle(g2, eyeRightX + 28, eyeY - 14, 2, new Color(140, 220, 255));
    }

    // The hero kid standing in utter panic, frantically waving arms ("อย่าร้องนะๆ เดี๋ยวแม่ด่า!")
    private void drawPanickingFriend(Graphics2D g2, int x, int groundY, boolean faceRight,
                                     Color swordAura, Color scarfColor, double panicTime) {
        int dir = faceRight ? 1 : -1;
        int t2 = 2;
        int headR = 16;

        // Anxious trembling
        double tremble = Math.sin(panicTime * 20.0) * 2;
        int hipY = groundY - 50;
        int shoulderY = groundY - 90;
        int headY = groundY - 114;
        int torsoShX = (int) (x + dir * 14 + tremble);

        g2.setColor(INK);
        // Torso leaning forward anxiously
        bresenhamLine(g2, torsoShX, shoulderY, x - dir * 4, hipY, t2);

        // Legs bent / nervous knees
        int f1x = x - dir * 16, f1y = groundY - 4;
        int f2x = x + dir * 18, f2y = groundY - 4;
        bresenhamLine(g2, x - dir * 4, hipY, x - dir * 8, groundY - 22, t2);
        bresenhamLine(g2, x - dir * 8, groundY - 22, f1x, f1y, t2);
        bresenhamLine(g2, x - dir * 4, hipY, x + dir * 10, groundY - 22, t2);
        bresenhamLine(g2, x + dir * 10, groundY - 22, f2x, f2y, t2);
        g2.fillOval(f1x - 6, f1y - 4, 16, 8);
        g2.fillOval(f2x - 6, f2y - 4, 16, 8);

        // Frantic waving arms up and down ("โอ๋ๆ อย่าร้องๆ!")
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

        // Toy sword dropped behind him on the grass
        g2.setColor(new Color(0, 180, 240));
        bresenhamLine(g2, x - dir * 18, groundY - 4, x - dir * 54, groundY - 4, 2);
        bresenhamLine(g2, x - dir * 26, groundY - 8, x - dir * 26, groundY, 2);
        fillMidpointCircle(g2, x - dir * 18, groundY - 4, 2, new Color(40, 40, 40));

        // Scarf
        drawScarf(g2, torsoShX, shoulderY, faceRight, scarfColor, panicTime);

        // Head
        int headX = torsoShX + dir * 6;
        bresenhamLine(g2, headX, headY + headR, torsoShX, shoulderY, t2);
        g2.setColor(Color.WHITE);
        g2.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
        g2.setColor(INK);
        midpointCircle(g2, headX, headY, headR);

        // Wide shocked eyes (O O) with tiny dot pupils
        int ex = headX + dir * 3;
        midpointCircle(g2, ex - 6, headY - 4, 4);
        midpointCircle(g2, ex + 4, headY - 4, 4);
        fillMidpointCircle(g2, ex - 6, headY - 4, 2, INK);
        fillMidpointCircle(g2, ex + 4, headY - 4, 2, INK);

        // Wavy trembling nervous mouth
        bezierCurve(g2, ex - 7, headY + 7, ex - 3, headY + 3, ex + 2, headY + 9, ex + 6, headY + 5);

        // Giant anime blue sweat drop dripping off head (Lab 3 Bezier + Lab 4 Midpoint Circle)
        int swx = headX - dir * 14;
        int swy = headY - 6 + (int)(Math.sin(panicTime * 8) * 3);
        g2.setColor(new Color(80, 200, 255));
        fillMidpointCircle(g2, swx, swy + 4, 4, new Color(80, 200, 255));
        bezierCurve(g2, swx - 3, swy + 2, swx - 1, swy - 6, swx + 1, swy - 6, swx + 3, swy + 2);
    }

    private void drawSwordFightScene(Graphics2D g2, double st) {
        final int groundY = 480;

        // Screen Shake calculation on clash and on the "BONK!" impact
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
            // =========================================================
            // PHASE 1: STANCE & FLUID ANIME DASH
            // =========================================================
            if (st < 0.6) {
                // Battle ready crouch stance
                drawSwordStance(g2, 100, groundY, true, cyanAura, blueScarf, st);
                drawSwordStance(g2, 500, groundY, false, redAura, redScarf, st);

                // Energy charging auras
                int chargeAlpha = (int) (100 + 100 * Math.sin(st * 16));
                fillMidpointCircle(g2, 120, groundY - 90, 8 + (int)(Math.sin(st * 12)*3), new Color(0, 220, 255, chargeAlpha / 2));
                fillMidpointCircle(g2, 480, groundY - 90, 8 + (int)(Math.sin(st * 12)*3), new Color(255, 60, 40, chargeAlpha / 2));
            } else {
                // Fluid Ninja Dash towards each other
                double du = (st - 0.6) / 0.8;
                int p1x = (int) (100 + 140 * du);
                int p2x = (int) (500 - 140 * du);
                double runPhase = (st - 0.6) * 6.5;

                // Speed lines (Lab 2 Bresenham)
                drawSpeedLines(g2, p1x, groundY - 60, 0.85, 12, 1);
                drawSpeedLines(g2, p2x, groundY - 60, 0.85, 12, 2);

                drawSwordDashRunner(g2, p1x, groundY, true, cyanAura, blueScarf, runPhase, st);
                drawSwordDashRunner(g2, p2x, groundY, false, redAura, redScarf, runPhase, st);
            }

        } else if (st < 3.0) {
            // =========================================================
            // PHASE 2: FAST TOY SWORD CLASHING
            // =========================================================
            double clashTime = st - 1.4;
            int clashBeat = (int) (clashTime * 4.0);
            double strikeProgress = (clashTime * 4.0) % 1.0;

            int p1x = 230 + (int) (Math.sin(st * 20) * 14);
            int p2x = 370 - (int) (Math.sin(st * 20) * 14);

            drawSwordSlashAttacker(g2, p1x, groundY, true, cyanAura, blueScarf, strikeProgress, clashBeat, st);
            drawSwordSlashAttacker(g2, p2x, groundY, false, redAura, redScarf, strikeProgress, clashBeat + 1, st);

            // Clash point at center
            int clashX = 300 + (clashBeat % 3 - 1) * 20;
            int clashY = 390 + (clashBeat % 2) * 26;

            drawImpactBurst(g2, clashX, clashY, strikeProgress);
            drawSpeedLines(g2, clashX, clashY, 0.8, 14, clashBeat);

            // Flying sparks
            Random sparkRand = new Random(clashBeat * 997);
            for (int sp = 0; sp < 8; sp++) {
                double spAngle = sparkRand.nextDouble() * Math.PI * 2;
                double spDist = 15 + strikeProgress * 70;
                int spx = clashX + (int) (Math.cos(spAngle) * spDist);
                int spy = clashY + (int) (Math.sin(spAngle) * spDist);
                fillMidpointCircle(g2, spx, spy, 2, new Color(255, 230, 120, (int)(255 * (1 - strikeProgress))));
            }

        } else if (st < 4.2) {
            // =========================================================
            // PHASE 3: THE "BONK!" HIT & FALLING DOWN ON GRASS
            // =========================================================
            double fallTime = (st - 3.0) / 1.2;

            // Hero follows through with downward tap swing
            drawSwordSlashAttacker(g2, 240, groundY, true, cyanAura, blueScarf, Math.min(1.0, fallTime * 2.0), 0, st);

            // Friend gets hit and falls on the grass
            drawHitAndFallKid(g2, 370, groundY, false, redScarf, fallTime, st);

            // "BONK!" Comic Impact FX right on strike ($st = 3.0 \rightarrow 3.5$)
            if (st < 3.6) {
                double bonkProg = (st - 3.0) / 0.6;
                drawImpactBurst(g2, 350, groundY - 110, bonkProg);

                // Action text "BONK!"
                g2.setFont(new Font("Impact", Font.BOLD, 26));
                g2.setColor(new Color(20, 20, 20));
                g2.drawString("BONK!", 333, groundY - 128);
                g2.drawString("BONK!", 337, groundY - 128);
                g2.setColor(new Color(255, 220, 50));
                g2.drawString("BONK!", 335, groundY - 128);
            }

        } else {
            // =========================================================
            // PHASE 4: WATERFALL TEARS & PANICKING APOLOGETIC FRIEND!
            // =========================================================
            double cryTime = st - 4.2;

            // Friend wailing with fountain tears
            drawCryingKid(g2, 400, groundY, false, redScarf, cryTime);

            // Hero panicking frantically ("อย่าร้องนะๆ เดี๋ยวแม่ตี!")
            drawPanickingFriend(g2, 230, groundY, true, cyanAura, blueScarf, cryTime);
        }

        g2.setTransform(steady);

        // Flash pop on bonk hit (st ≈ 3.02)
        double flashBonk = Math.abs(st - 3.02);
        if (flashBonk < 0.12) {
            int a = (int) (180 * (1 - flashBonk / 0.12));
            g2.setColor(new Color(255, 255, 255, a));
            g2.fillRect(0, 0, 600, 600);
        }
    }

    // ==================================================================
    // 9. TRANSITIONS & RENDERING PIPELINE
    // ==================================================================

    private double warpFlash(double t) {
        double a = 1 - Math.abs(t - WARP_INTO_MEMORY) / WARP_RAMP;
        double b = 1 - Math.abs(t - WARP_INTO_SWORD) / WARP_RAMP;
        double c = 1 - Math.abs(t - WARP_INTO_WATER) / WARP_RAMP;
        double d = 1 - Math.abs(t - WARP_BACK) / WARP_RAMP;
        return Math.max(0, Math.max(Math.max(a, b), Math.max(c, d)));
    }

    private void drawWarp(Graphics2D g2, double flash, double t) {
        // The transitions into and out of the memory sequence close into darkness.
        // The transition between memories keeps the original white flash.
        boolean darkTransition = Math.abs(t - WARP_INTO_MEMORY) <= WARP_RAMP
                || Math.abs(t - WARP_BACK) <= WARP_RAMP;
        Color fadeColor = darkTransition ? Color.BLACK : Color.WHITE;
        Color ringColor = darkTransition ? new Color(18, 18, 18) : Color.WHITE;

        // expanding rings, drawn with the midpoint circle, read as "a memory surfacing"
        int cx = 150, cy = 455;
        g2.setColor(new Color(ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(), (int) (200 * flash)));
        for (int i = 0; i < 4; i++) {
            double ringT = flash + i * 0.22;
            int r = (int) (ringT * 520);
            if (r > 4 && r < 700) midpointCircle(g2, cx, cy, r);
        }

        g2.setColor(new Color(fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), (int) (255 * flash * flash)));
        g2.fillRect(0, 0, 600, 600);
    }

    // ==================================================================
    // 10. ANIMATION LOOP (Lab_05 pattern) & MAIN
    // ==================================================================

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
        } else if (t >= WARP_INTO_WATER && t < WARP_BACK) {
            drawStreamScene(g2, t - WARP_INTO_WATER);
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
