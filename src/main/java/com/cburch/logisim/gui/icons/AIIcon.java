package com.cburch.logisim.gui.icons;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class AIIcon implements Icon
{
    private static final int SIZE = 24;
    
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        int iconSize = getIconWidth();
        int padding = (iconSize - SIZE) / 2;
        
        g2d.translate(x + padding, y + padding);
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(66, 133, 244),
            SIZE, SIZE, new Color(52, 168, 83));
        g2d.setPaint(gradient);
        
        RoundRectangle2D roundedRect = new RoundRectangle2D.Double(1, 1, SIZE - 2, SIZE - 2, 8, 8);
        g2d.fill(roundedRect);
        
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int centerX = SIZE / 2;
        int centerY = SIZE / 2 - 2;
        int headRadius = 7;
        
        g2d.drawOval(centerX - headRadius, centerY - headRadius, headRadius * 2, headRadius * 2);
        
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int eyeOffset = 3;
        int eyeY = centerY - 1;
        int eyeSize = 2;
        g2d.fillOval(centerX - eyeOffset - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);
        g2d.fillOval(centerX + eyeOffset - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);
        
        g2d.drawArc(centerX - 3, centerY + 1, 6, 4, 0, -180);
        
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int antennaHeight = 3;
        g2d.drawLine(centerX, centerY - headRadius, centerX, centerY - headRadius - antennaHeight);
        g2d.fillOval(centerX - 1, centerY - headRadius - antennaHeight - 2, 3, 3);
        
        int bodyY = centerY + headRadius - 2;
        int bodyWidth = 12;
        int bodyHeight = 6;
        RoundRectangle2D body = new RoundRectangle2D.Double(
            centerX - bodyWidth/2, bodyY, bodyWidth, bodyHeight, 3, 3);
        g2d.fill(body);
        
        g2d.dispose();
    }
    
    @Override
    public int getIconWidth()
    {
        return 32;
    }
    
    @Override
    public int getIconHeight()
    {
        return 32;
    }
}