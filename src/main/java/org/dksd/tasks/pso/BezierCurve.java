package org.dksd.tasks.pso;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.GeneralPath;

public class BezierCurve extends JPanel {

    private int[] xPoints = {50, 200, 450, 500};
    private int[] yPoints = {300, 100, 100, 300};

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLUE);
        for (int i = 0; i < 4; i++) {
            g2d.fillOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
        }
        g2d.setColor(Color.BLACK);
        GeneralPath path = new GeneralPath();
        path.moveTo(xPoints[0], yPoints[0]);
        path.curveTo(xPoints[1], yPoints[1], xPoints[2], yPoints[2], xPoints[3], yPoints[3]);
        g2d.draw(path);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bezier Curve");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.add(new BezierCurve());
        frame.setVisible(true);
    }
}