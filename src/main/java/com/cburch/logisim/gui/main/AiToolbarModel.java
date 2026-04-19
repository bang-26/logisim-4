package com.cburch.logisim.gui.main;


import com.cburch.draw.toolbar.AbstractToolbarModel;
import com.cburch.draw.toolbar.ToolbarItem;

import java.util.Collections;
import java.util.List;

public class AiToolbarModel extends AbstractToolbarModel
{
    private static final long serialVersionUID = 1L;
    
    private final AiToolbarItem aiItem;
    private final List<ToolbarItem> items;
    
    public AiToolbarModel(Frame frame)
    {
        aiItem = new AiToolbarItem(frame);
        items = Collections.singletonList(aiItem);
    }
    
    @Override
    public List<ToolbarItem> getItems()
    {
        return items;
    }
    
    @Override
    public boolean isSelected(ToolbarItem item)
    {
        return false;
    }
    
    @Override
    public void itemSelected(ToolbarItem item)
    {
        
    }
    
    
    public ToolbarItem getSelectedItem()
    {
        return null;
    }
    
    public void setSelectedItem(ToolbarItem item)
    {
    }
    
    public void toggleAiPanel()
    {
        aiItem.toggle();
    }
    
    public boolean isAiPanelVisible()
    {
        return aiItem.isVisible();
    }
}