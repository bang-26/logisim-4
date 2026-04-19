/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.draw.toolbar;

import javax.swing.*;
import java.awt.Component;
import java.awt.Graphics;

public interface ToolbarClickableItem extends ToolbarItem {
    
    Icon getIcon();
    
    void clicked();
    
    void doAction();
    
    void paintPressedIcon(Component destination, Graphics gfx);
}
