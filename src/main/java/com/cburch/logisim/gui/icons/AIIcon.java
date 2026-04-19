package com.cburch.logisim.gui.icons;

/**
 * **************************************************************************************************
 * ProjectName   ：  logisim-4
 * Package       ：  com.cburch.logisim.gui.icons
 * ClassName     ：  AIIcon
 * CreateTime    ：  2026-04-19 16:11
 * Author        ：  Issac_Al
 * Email         ：  IssacAl@qq.com
 * IDE           ：  IntelliJ IDEA 2020.3.4
 * Version       ：  1.0
 * CodedFormat   ：  utf-8
 * Description   ：  Java Class
 * **************************************************************************************************
 */

import javax.swing.*;
import java.awt.*;

public class AIIcon implements Icon
{
    private static final int SIZE = 24;
    
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int size = Math.min(getIconWidth(), getIconHeight());
        int offset = (size - SIZE) / 2;
        
        g2d.translate(x + offset, y + offset);
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(100, 150, 255),
            SIZE, SIZE, new Color(150, 100, 255));
        g2d.setPaint(gradient);
        
        g2d.fillRoundRect(2, 2, SIZE - 4, SIZE - 4, 8, 8);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "AI";
        int textX = (SIZE - fm.stringWidth(text)) / 2;
        int textY = (SIZE + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(text, textX, textY);
        
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