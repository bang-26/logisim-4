package com.cburch.logisim.gui.main;

import com.cburch.draw.toolbar.ToolbarClickableItem;
import com.cburch.logisim.gui.icons.AIIcon;

import javax.swing.*;
import java.awt.*;

public class AiToolbarItem implements ToolbarClickableItem {
  private final AIIcon icon;
  private final String toolTip;
  private final Frame frame;
  private boolean visible;
  
  public AiToolbarItem(Frame frame) {
    this.icon = new AIIcon();
    this.toolTip = "AI Assistant";
    this.frame = frame;
    this.visible = false;
  }
  
  @Override
  public Icon getIcon() {
    return icon;
  }
  
  @Override
  public String getToolTip() {
    return toolTip;
  }
  
  @Override
  public void clicked() {
    System.out.println("AiToolbarItem clicked! Current visible: " + visible);
    toggle();
    if (frame != null) {
      System.out.println("Calling frame.toggleAiPanel()");
      frame.toggleAiPanel();
    } else {
      System.err.println("Frame is null!");
    }
  }
  
  @Override
  public void doAction() {
    clicked();
  }
  
  public void toggle() {
    visible = !visible;
  }
  
  public boolean isVisible() {
    return visible;
  }
  
  @Override
  public boolean isSelectable() {
    return false;
  }
  
  @Override
  public Dimension getDimension(Object orientation) {
    return new Dimension(32, 32);
  }
  
  @Override
  public void paintIcon(Component destination, Graphics gfx) {
    icon.paintIcon(destination, gfx, 0, 0);
  }
  
  @Override
  public void paintPressedIcon(Component destination, Graphics gfx) {
    Graphics2D g2d = (Graphics2D) gfx.create();
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
    icon.paintIcon(destination, g2d, 0, 0);
    g2d.dispose();
  }
}
