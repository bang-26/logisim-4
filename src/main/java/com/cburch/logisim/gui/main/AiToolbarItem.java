package com.cburch.logisim.gui.main;

import com.cburch.draw.toolbar.ToolbarClickableItem;
import com.cburch.logisim.gui.icons.AIIcon;

import javax.swing.*;
import java.awt.*;

public class AiToolbarItem implements ToolbarClickableItem
{
    private final AIIcon icon;
    private final String toolTip;
    private final Frame frame;
    private boolean visible;
    
    public AiToolbarItem(Frame frame)
    {
        this.icon = new AIIcon();
        this.toolTip = "AI 助手";
        this.frame = frame;
        this.visible = false;
    }
    
    @Override
    public Icon getIcon()
    {
        return icon;
    }
    
    @Override
    public String getToolTip()
    {
        return toolTip;
    }
    
    @Override
    public void clicked()
    {
        toggle();
        if (frame != null)
        {
            frame.toggleAiPanel();
        }
    }
    
    @Override
    public void doAction()
    {
        clicked();
    }
    
    public void toggle()
    {
        visible = !visible;
    }
    
    public boolean isVisible()
    {
        return visible;
    }
    
    @Override
    public boolean isSelectable()
    {
        return false;
    }
    
    @Override
    public Dimension getDimension(Object orientation)
    {
        return new Dimension(60, 40);
    }
    
    @Override
    public void paintIcon(Component destination, Graphics gfx)
    {
        Graphics2D g2d = (Graphics2D) gfx.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Dimension dim = getDimension(null);
        int width = dim.width;
        int height = dim.height;
        
        icon.paintIcon(destination, g2d, 8, 4);
        
        g2d.setColor(destination.getForeground());
        g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "AI";
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = height - 6;
        g2d.drawString(text, textX, textY);
        
        g2d.dispose();
    }
    
    @Override
    public void paintPressedIcon(Component destination, Graphics gfx)
    {
        Graphics2D g2d = (Graphics2D) gfx.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        paintIcon(destination, g2d);
        g2d.dispose();
    }
}
