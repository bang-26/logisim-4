package com.cburch.logisim.ai.model;

/**
 * **************************************************************************************************
 * ProjectName   ：  logisim-4
 * Package       ：  com.cburch.logisim.ai.model
 * ClassName     ：  ChatMessage
 * CreateTime    ：  2026-04-19 16:04
 * Author        ：  Issac_Al
 * Email         ：  IssacAl@qq.com
 * IDE           ：  IntelliJ IDEA 2020.3.4
 * Version       ：  1.0
 * CodedFormat   ：  utf-8
 * Description   ：  Java Class
 * **************************************************************************************************
 */


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage
{
    public enum Role
    {
        USER, ASSISTANT, SYSTEM
    }
    
    private Role role;
    private String content;
    private LocalDateTime timestamp;
    private List<File> attachedFiles;
    private List<Image> attachedImages;
    private boolean isRendering;
    
    public ChatMessage(Role role, String content)
    {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.attachedFiles = new ArrayList<>();
        this.attachedImages = new ArrayList<>();
        this.isRendering = false;
    }
    
    
    public String getFormattedTime()
    {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    public void addAttachedFile(File file)
    {
        attachedFiles.add(file);
    }
    
    public void addAttachedImage(Image image)
    {
        attachedImages.add(image);
    }
    
    public boolean hasAttachments()
    {
        return !attachedFiles.isEmpty() || !attachedImages.isEmpty();
    }
    
    public boolean isRendering()
    {
        return isRendering;
    }
    
    public void setRendering(boolean rendering)
    {
        isRendering = rendering;
    }
}