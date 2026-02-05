package com.enkelagon.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Animated background panel with moving dots and waves in dragon theme colors.
 */
public class AnimatedBackground extends JPanel {

    private static final Color RED = new Color(220, 20, 20);
    private static final Color RED_BRIGHT = new Color(255, 60, 60);
    private static final Color RED_DARK = new Color(140, 0, 0);
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color WHITE_DIM = new Color(200, 200, 200);
    private static final Color GREY = new Color(150, 150, 150);
    private static final Color GREY_DARK = new Color(80, 80, 80);

    private final List<Particle> particles;
    private final List<Wave> waves;
    private final Random random;
    private final Timer animationTimer;

    private double time = 0;
    private boolean running = true;

    public AnimatedBackground() {
        this.particles = new ArrayList<>();
        this.waves = new ArrayList<>();
        this.random = new Random();

        setBackground(Color.BLACK);
        setOpaque(true);

        // Initialize particles - more of them for presence
        initParticles(150);

        // Initialize waves - more layers
        initWaves(6);

        // Animation timer - 30 FPS
        animationTimer = new Timer(33, e -> {
            if (running) {
                time += 0.02;
                updateParticles();
                updateWaves();
                repaint();
            }
        });
        animationTimer.start();
    }

    private void initParticles(int count) {
        particles.clear();
        for (int i = 0; i < count; i++) {
            particles.add(createParticle());
        }
    }

    private Particle createParticle() {
        Particle p = new Particle();
        p.x = random.nextDouble() * 2500;
        p.y = random.nextDouble() * 1500;
        p.size = 3 + random.nextDouble() * 8;  // Larger particles
        p.speedX = (random.nextDouble() - 0.5) * 2.0;
        p.speedY = (random.nextDouble() - 0.5) * 1.5;
        p.alpha = 0.4f + random.nextFloat() * 0.5f;  // More visible
        p.pulseSpeed = 0.03 + random.nextDouble() * 0.04;
        p.pulseOffset = random.nextDouble() * Math.PI * 2;

        // Color distribution: 45% red, 30% white, 25% grey
        double colorRoll = random.nextDouble();
        if (colorRoll < 0.25) {
            p.color = RED_BRIGHT;
        } else if (colorRoll < 0.45) {
            p.color = random.nextBoolean() ? RED : RED_DARK;
        } else if (colorRoll < 0.75) {
            p.color = random.nextBoolean() ? WHITE : WHITE_DIM;
        } else {
            p.color = random.nextBoolean() ? GREY : GREY_DARK;
        }

        return p;
    }

    private void initWaves(int count) {
        waves.clear();
        for (int i = 0; i < count; i++) {
            Wave w = new Wave();
            w.amplitude = 40 + random.nextDouble() * 80;  // Bigger waves
            w.frequency = 0.0015 + random.nextDouble() * 0.003;
            w.speed = 0.4 + random.nextDouble() * 0.6;
            w.yOffset = 100 + i * 150 + random.nextDouble() * 80;
            w.alpha = 0.15f + random.nextFloat() * 0.2f;  // More visible

            // Color: mostly red tones, some grey and white
            double colorRoll = random.nextDouble();
            if (colorRoll < 0.5) {
                w.color = new Color(180, 0, 0);
            } else if (colorRoll < 0.75) {
                w.color = new Color(120, 0, 0);
            } else if (colorRoll < 0.9) {
                w.color = GREY_DARK;
            } else {
                w.color = new Color(60, 60, 60);
            }

            waves.add(w);
        }
    }

    private void updateParticles() {
        int width = Math.max(getWidth(), 100);
        int height = Math.max(getHeight(), 100);

        for (Particle p : particles) {
            p.x += p.speedX;
            p.y += p.speedY;

            // Pulse effect
            p.currentAlpha = p.alpha * (0.5f + 0.5f * (float) Math.sin(time * p.pulseSpeed * 50 + p.pulseOffset));

            // Wrap around screen
            if (p.x < -10) p.x = width + 10;
            if (p.x > width + 10) p.x = -10;
            if (p.y < -10) p.y = height + 10;
            if (p.y > height + 10) p.y = -10;
        }
    }

    private void updateWaves() {
        for (Wave w : waves) {
            w.phase += w.speed * 0.02;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        int width = getWidth();
        int height = getHeight();

        // Draw subtle gradient background
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(5, 0, 0),
                width, height, new Color(0, 0, 0)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        // Draw waves
        drawWaves(g2d, width, height);

        // Draw particles
        drawParticles(g2d);

        // Draw connecting lines between nearby particles
        drawConnections(g2d);

        g2d.dispose();
    }

    private void drawWaves(Graphics2D g2d, int width, int height) {
        for (Wave w : waves) {
            Path2D path = new Path2D.Double();
            path.moveTo(0, height);

            for (int x = 0; x <= width; x += 5) {
                double y = w.yOffset + w.amplitude * Math.sin(x * w.frequency + w.phase);
                if (x == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }

            // Complete the path to fill below the wave
            path.lineTo(width, height);
            path.lineTo(0, height);
            path.closePath();

            // Fill with gradient
            Color waveColor = new Color(
                    w.color.getRed(),
                    w.color.getGreen(),
                    w.color.getBlue(),
                    (int) (w.alpha * 255)
            );
            g2d.setColor(waveColor);
            g2d.fill(path);

            // Draw wave line
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.setColor(new Color(
                    Math.min(255, w.color.getRed() + 50),
                    w.color.getGreen(),
                    w.color.getBlue(),
                    (int) (w.alpha * 255 * 1.5)
            ));

            Path2D linePath = new Path2D.Double();
            for (int x = 0; x <= width; x += 3) {
                double y = w.yOffset + w.amplitude * Math.sin(x * w.frequency + w.phase);
                if (x == 0) {
                    linePath.moveTo(x, y);
                } else {
                    linePath.lineTo(x, y);
                }
            }
            g2d.draw(linePath);
        }
    }

    private void drawParticles(Graphics2D g2d) {
        for (Particle p : particles) {
            Color c = new Color(
                    p.color.getRed(),
                    p.color.getGreen(),
                    p.color.getBlue(),
                    (int) (p.currentAlpha * 255)
            );
            g2d.setColor(c);

            // Draw glow
            float glowSize = (float) (p.size * 3);
            RadialGradientPaint glow = new RadialGradientPaint(
                    (float) p.x, (float) p.y, glowSize,
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int) (p.currentAlpha * 100)),
                            new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), 0)
                    }
            );
            g2d.setPaint(glow);
            g2d.fill(new Ellipse2D.Double(p.x - glowSize, p.y - glowSize, glowSize * 2, glowSize * 2));

            // Draw core
            g2d.setColor(c);
            g2d.fill(new Ellipse2D.Double(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size));
        }
    }

    private void drawConnections(Graphics2D g2d) {
        double maxDist = 180;  // Longer connections

        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);
            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);

                double dx = p1.x - p2.x;
                double dy = p1.y - p2.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < maxDist) {
                    float alpha = (float) ((1 - dist / maxDist) * 0.35 * Math.min(p1.currentAlpha, p2.currentAlpha));

                    // Blend colors
                    int r = (p1.color.getRed() + p2.color.getRed()) / 2;
                    int gr = (p1.color.getGreen() + p2.color.getGreen()) / 2;
                    int b = (p1.color.getBlue() + p2.color.getBlue()) / 2;

                    g2d.setColor(new Color(r, gr, b, (int) (alpha * 255)));
                    g2d.setStroke(new BasicStroke(1.0f));  // Thicker lines
                    g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
                }
            }
        }
    }

    public void start() {
        running = true;
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    public void stop() {
        running = false;
        animationTimer.stop();
    }

    public void setParticleCount(int count) {
        initParticles(count);
    }

    private static class Particle {
        double x, y;
        double size;
        double speedX, speedY;
        float alpha;
        float currentAlpha;
        double pulseSpeed;
        double pulseOffset;
        Color color;
    }

    private static class Wave {
        double amplitude;
        double frequency;
        double speed;
        double phase;
        double yOffset;
        float alpha;
        Color color;
    }
}
