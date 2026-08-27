import java.awt.*;
import java.awt.geom.*;
import java.util.Random;
import javax.swing.*;

public class Assignment1_studentID_yourPairID extends JPanel {
    public static final int CANVAS_WIDTH = 600;
    public static final int CANVAS_HEIGHT = 600;
    public static final double TOTAL_DURATION = 5.8; // Seconds per cycle
    public static final int FPS = 60;

    private double currentTime = 0.0;
    private Timer animationTimer;

    // Seeded background parameters for consistent rendering
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

    /** Explicit anatomical anchors keep the lying pose connected during zoom. */
    private static class BodyPose {
        Point2D.Double headCenter;
        Point2D.Double chin;
        Point2D.Double neckBase;
        Point2D.Double shoulderNear;
        Point2D.Double shoulderFar;
        Point2D.Double chest;
        Point2D.Double waist;
        Point2D.Double hip;
        Point2D.Double elbowSupport;
        Point2D.Double wristSupport;
        Point2D.Double elbowFront;
        Point2D.Double wristFront;
    }

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
    }

    public Assignment1_studentID_yourPairID() {
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        setBackground(Color.BLACK);

        animationTimer = new Timer(1000 / FPS, e -> {
            currentTime += 1.0 / FPS;
            if (currentTime > TOTAL_DURATION) {
                currentTime = 0.0; // Smooth continuous loop
            }
            repaint();
        });
    }

    public void startAnimation() {
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Configure rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Execute background scenery renderer
        renderScene1_StarGazingAndMemory(g2d, currentTime);

        g2d.dispose();
    }

    // =========================================================================
    // 1. COMPUTER GRAPHICS ALGORITHMS (MIDPOINT CIRCLE & ELLIPSE)
    // =========================================================================

    /**
     * Midpoint Circle Algorithm implementation (draws outline pixel-by-pixel)
     */
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

    /**
     * Filled Midpoint Circle Algorithm using horizontal scanlines between 8-way symmetric points
     */
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

    /**
     * Filled Midpoint Ellipse Algorithm using Region 1 and Region 2 scanlines
     */
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

    // =========================================================================
    // 2. MASTER SCENE MANAGER
    // =========================================================================

    public void renderScene1_StarGazingAndMemory(Graphics2D g2d, double time) {
        // Scene timing follows Scenario 1 exactly.
        double fadeToBlack = 0.0;
        if (time >= 5.2) {
            fadeToBlack = Math.min(1.0, (time - 5.2) / 0.6);
            fadeToBlack = fadeToBlack * fadeToBlack; // Ease in
        }

        AffineTransform screenTransform = g2d.getTransform();

        // Camera zooms around the character's face, so the entire world and
        // character obey the same perspective during the close-up.
        double zoom = 1.0;
        if (time >= 2.0 && time < 4.2) {
            double p = smoothStep((time - 2.0) / 2.2);
            zoom = 1.0 + 0.72 * p;
        } else if (time >= 4.2) {
            zoom = 1.72;
        }
        if (zoom > 1.0) {
            double faceX = 205.0;
            double faceY = 393.0;
            g2d.translate(faceX, faceY);
            g2d.scale(zoom, zoom);
            g2d.translate(-faceX, -faceY);
        }

        // --- DRAW WORLD SCENE LAYERS ---
        drawSkyBackground(g2d, time);
        drawMilkyWay(g2d, time);
        drawMoon(g2d, time);
        drawStars(g2d, time);
        drawShootingStar(g2d, time);
        drawDistantMountains(g2d);
        drawGrassyHill(g2d, time);
        drawLyingCharacter(g2d, time);
        drawForegroundFlowersAndGrass(g2d, time);
        g2d.setTransform(screenTransform);

        // Nostalgic Memory Glow overlay
        drawMemoryGlow(g2d, time);

        // Vignette framing
        drawVignette(g2d);

        // Final Fade to Black
        drawFadeToBlack(g2d, fadeToBlack);
    }

    private static double smoothStep(double value) {
        double t = Math.max(0.0, Math.min(1.0, value));
        return t * t * (3.0 - 2.0 * t);
    }

    // =========================================================================
    // 3. SCENE METHOD: SKY BACKGROUND
    // =========================================================================
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

    // =========================================================================
    // 4. SCENE METHOD: MILKY WAY GALAXY
    // =========================================================================
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

    // =========================================================================
    // 5. SCENE METHOD: MOON & LUNAR GLOW (MIDPOINT CIRCLE ALGORITHM)
    // =========================================================================
    public void drawMoon(Graphics2D g2d, double time) {
        int moonX = 530;
        int moonY = 65;
        int moonR = 25;

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

    // =========================================================================
    // 6. SCENE METHOD: TWINKLING STARS
    // =========================================================================
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

    // =========================================================================
    // 7. SCENE METHOD: SHOOTING STAR
    // =========================================================================
    public void drawShootingStar(Graphics2D g2d, double time) {
        double cycle = time % 4.0;
        if (cycle >= 1.0 && cycle <= 2.0) {
            double progress = (cycle - 1.0) / 1.0;
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

    // =========================================================================
    // 8. SCENE METHOD: DISTANT MOUNTAINS & HORIZON
    // =========================================================================
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

    // =========================================================================
    // 9. SCENE METHOD: GRASSY MEADOW HILL
    // =========================================================================
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
    // 10. SCENE METHOD: LYING ANIME CHARACTER
    // =========================================================================
    private BodyPose createLyingPose() {
        BodyPose pose = new BodyPose();
        // The body axis follows the ground from the supported head to the hips.
        pose.headCenter = new Point2D.Double(205, 393);
        pose.chin = new Point2D.Double(226, 417);
        pose.neckBase = new Point2D.Double(250, 435);
        pose.shoulderNear = new Point2D.Double(270, 447);
        pose.shoulderFar = new Point2D.Double(255, 430);
        pose.chest = new Point2D.Double(315, 465);
        pose.waist = new Point2D.Double(380, 492);
        pose.hip = new Point2D.Double(447, 516);

        // Support arm: shoulder -> elbow -> wrist. The head rests on this arm.
        pose.elbowSupport = new Point2D.Double(132, 428);
        pose.wristSupport = new Point2D.Double(176, 416);

        // Near arm lies relaxed across the lower torso.
        pose.elbowFront = new Point2D.Double(348, 505);
        pose.wristFront = new Point2D.Double(405, 529);
        return pose;
    }

    private void drawLyingCharacter(Graphics2D g2d, double time) {
        BodyPose p = createLyingPose();

        // A simple contact shadow anchors the stickman to the grass.
        g2d.setColor(new Color(0, 4, 10, 120));
        g2d.fill(new Ellipse2D.Double(120, 405, 390, 125));

        // Stickman body is drawn with black lines.
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Support arm: shoulder -> elbow -> wrist. The head rests on this arm.
        drawStickLine(g2d, p.shoulderFar, p.elbowSupport);
        drawStickLine(g2d, p.elbowSupport, p.wristSupport);
        g2d.draw(new Ellipse2D.Double(p.wristSupport.x - 5, p.wristSupport.y - 5, 10, 10));

        // One continuous line is used for the neck and the lying torso axis.
        drawStickLine(g2d, new Point2D.Double(205, 410), p.shoulderNear);
        drawStickLine(g2d, p.shoulderNear, p.chest);
        drawStickLine(g2d, p.chest, p.waist);
        drawStickLine(g2d, p.waist, p.hip);

        // Front arm is bent naturally at the elbow and rests across the body.
        drawStickLine(g2d, p.shoulderNear, p.elbowFront);
        drawStickLine(g2d, p.elbowFront, p.wristFront);
        g2d.draw(new Ellipse2D.Double(p.wristFront.x - 5, p.wristFront.y - 5, 10, 10));

        // Relaxed legs continue from the hip, with readable knee and ankle bends.
        Point2D.Double kneeNear = new Point2D.Double(472, 544);
        Point2D.Double ankleNear = new Point2D.Double(508, 560);
        Point2D.Double kneeFar = new Point2D.Double(430, 548);
        Point2D.Double ankleFar = new Point2D.Double(397, 560);
        drawStickLine(g2d, p.hip, kneeNear);
        drawStickLine(g2d, kneeNear, ankleNear);
        drawStickLine(g2d, p.hip, kneeFar);
        drawStickLine(g2d, kneeFar, ankleFar);
        drawFoot(g2d, ankleNear, 1.0);
        drawFoot(g2d, ankleFar, -1.0);

        // The head has a white interior with a black outline and black features.
        g2d.setColor(Color.WHITE);
        g2d.fill(new Ellipse2D.Double(p.headCenter.x - 30, p.headCenter.y - 30, 60, 60));
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.draw(new Ellipse2D.Double(p.headCenter.x - 30, p.headCenter.y - 30, 60, 60));
        drawAnimeFace(g2d, p.headCenter, time);
    }

    private void drawStickLine(Graphics2D g2d, Point2D.Double a, Point2D.Double b) {
        g2d.draw(new Line2D.Double(a, b));
    }

    private void drawFoot(Graphics2D g2d, Point2D.Double ankle, double direction) {
        g2d.draw(new QuadCurve2D.Double(ankle.x, ankle.y,
                ankle.x + direction * 11, ankle.y + 4,
                ankle.x + direction * 20, ankle.y));
    }

    private void drawAnimeFace(Graphics2D g2d, Point2D.Double headCenter, double time) {
        double blink = 0.0;
        if (time >= 4.2) {
            blink = smoothStep((time - 4.2) / 1.0);
        }
        double eyeHeight = 7.0 * (1.0 - blink);
        g2d.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Eye positions are head-relative, so they remain attached during zoom.
        double leftEyeX = headCenter.x - 17;
        double rightEyeX = headCenter.x + 4;
        double eyeY = headCenter.y - 3;
        if (eyeHeight > 0.25) {
            g2d.setColor(new Color(15, 22, 58));
            g2d.fill(new Ellipse2D.Double(leftEyeX - 6, eyeY - eyeHeight / 2, 12, eyeHeight));
            g2d.fill(new Ellipse2D.Double(rightEyeX - 5, eyeY - eyeHeight / 2, 10, eyeHeight));
            g2d.setColor(new Color(124, 172, 235));
            g2d.fill(new Ellipse2D.Double(leftEyeX - 2, eyeY - 1, 3, 3));
            g2d.fill(new Ellipse2D.Double(rightEyeX - 1, eyeY - 1, 3, 3));
        } else {
            g2d.setColor(new Color(24, 27, 65));
            g2d.draw(new QuadCurve2D.Double(leftEyeX - 7, eyeY, leftEyeX, eyeY + 3,
                    leftEyeX + 6, eyeY));
            g2d.draw(new QuadCurve2D.Double(rightEyeX - 6, eyeY, rightEyeX, eyeY + 3,
                    rightEyeX + 5, eyeY));
        }

        g2d.setColor(new Color(36, 37, 81));
        g2d.draw(new QuadCurve2D.Double(leftEyeX - 7, eyeY - 8, leftEyeX, eyeY - 11,
                leftEyeX + 6, eyeY - 8));
        g2d.draw(new QuadCurve2D.Double(rightEyeX - 6, eyeY - 8, rightEyeX, eyeY - 10,
                rightEyeX + 5, eyeY - 8));
        // Two small nostrils and a curved smile follow the reference face.
        g2d.fill(new Ellipse2D.Double(231, 402, 3, 3));
        g2d.fill(new Ellipse2D.Double(237, 402, 3, 3));
        g2d.draw(new QuadCurve2D.Double(220, 416, 228, 421, 237, 416));
    }

    // 11. SCENE METHOD: FOREGROUND FLOWERS & SWAYING GRASS
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
            g2d.setColor(new Color(8, 22, 28, Math.min(255, alpha)));
            g2d.fill(blade);
        }

        // Chamomile / Daisy flowers (Midpoint Ellipse petals & Midpoint Circle center)
        for (int i = 0; i < NUM_FLOWERS; i++) {
            drawChamomileFlower(g2d, flowerX[i], flowerY[i], flowerScale[i], flowerRot[i] + wind * 0.02);
        }
    }

    /**
     * Chamomile Flower using Midpoint Ellipse Algorithm for petals & Midpoint Circle for center
     */
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

    // =========================================================================
    // 11. SCENE METHOD: MEMORY GLOW
    // =========================================================================
    public void drawMemoryGlow(Graphics2D g2d, double time) {
        if (time >= 5.5 && time < 7.5) {
            double glowAlpha = Math.sin((time - 5.5) / 2.0 * Math.PI) * 0.18;
            g2d.setColor(new Color(160, 205, 255, (int) (glowAlpha * 255)));
            g2d.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }
    }

    // =========================================================================
    // 12. SCENE METHOD: VIGNETTE
    // =========================================================================
    public void drawVignette(Graphics2D g2d) {
        Point2D center = new Point2D.Float(CANVAS_WIDTH / 2.0f, CANVAS_HEIGHT / 2.0f);
        float radius = 420.0f;
        float[] dist = {0.0f, 0.65f, 1.0f};
        Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(0, 5, 15, 40),
            new Color(0, 3, 10, 180)
        };
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        g2d.setPaint(p);
        g2d.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    // =========================================================================
    // 13. SCENE METHOD: FADE TO BLACK
    // =========================================================================
    public void drawFadeToBlack(Graphics2D g2d, double fadeToBlack) {
        if (fadeToBlack > 0.0) {
            int alpha = (int) Math.min(255, Math.max(0, fadeToBlack * 255));
            g2d.setColor(new Color(0, 0, 0, alpha));
            g2d.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }
    }

    // =========================================================================
    // 14. MAIN METHOD
    // =========================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Assignment 1 - MY MEMORIES (Star Gazing)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            Assignment1_studentID_yourPairID panel = new Assignment1_studentID_yourPairID();
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.startAnimation();
        });
    }
}
