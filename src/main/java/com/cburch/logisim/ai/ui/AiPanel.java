package com.cburch.logisim.ai.ui;

import com.cburch.logisim.ai.config.AiConfig;
import com.cburch.logisim.ai.model.ChatMessage;
import com.cburch.logisim.ai.service.AiService;
import com.cburch.logisim.ai.service.AiServiceImpl;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AiPanel extends JPanel
{
    private static final long serialVersionUID = 1L;
    
    private final AiConfig config;
    private final AiService aiService;
    private final List<ChatMessage> chatHistory;
    
    private DefaultListModel<ChatMessage> messageListModel;
    private JList<ChatMessage> messageList;
    private JTextArea inputArea;
    private JButton sendButton;
    private JButton attachFileButton;
    private JButton attachImageButton;
    private JButton clearChatButton;
    private JButton settingsButton;
    
    private List<File> pendingFiles;
    private List<Image> pendingImages;
    
    public AiPanel()
    {
        this.config = new AiConfig();
        this.config.loadFromPreferences();
        this.aiService = new AiServiceImpl();
        this.chatHistory = new ArrayList<>();
        this.pendingFiles = new ArrayList<>();
        this.pendingImages = new ArrayList<>();
        
        initializeUI();
        addWelcomeMessage();
    }
    
    private void initializeUI()
    {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        messageListModel = new DefaultListModel<>();
        messageList = new JList<>(messageListModel);
        messageList.setCellRenderer(new MessageCellRenderer());
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(messageList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        JLabel titleLabel = new JLabel("AI 助手");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        
        settingsButton = new JButton("⚙");
        settingsButton.setToolTipText("AI 设置");
        settingsButton.addActionListener(e -> openSettingsDialog());
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(settingsButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createInputPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        inputArea.enableInputMethods(true);
        
        inputArea.addInputMethodListener(new InputMethodListener()
        {
            @Override
            public void caretPositionChanged(InputMethodEvent e)
            {
            }
            
            @Override
            public void inputMethodTextChanged(InputMethodEvent e)
            {
                if (e.getCommittedCharacterCount() > 0)
                {
                    inputArea.repaint();
                }
            }
        });
        
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        attachFileButton = createIconButton("📎", "附加文件");
        attachFileButton.addActionListener(e -> attachFiles());
        
        attachImageButton = createIconButton("🖼", "附加图片");
        attachImageButton.addActionListener(e -> attachImages());
        
        clearChatButton = createIconButton("🗑", "清空对话");
        clearChatButton.addActionListener(e -> clearChat());
        
        sendButton = createIconButton("➤", "发送给 AI");
        sendButton.addActionListener(e -> sendMessage());
        
        buttonPanel.add(attachFileButton);
        buttonPanel.add(attachImageButton);
        buttonPanel.add(clearChatButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(sendButton);
        
        panel.add(inputScroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JButton createIconButton(String icon, String tooltip)
    {
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(35, 35));
        button.setFocusPainted(false);
        return button;
    }
    
    private void addWelcomeMessage()
    {
        ChatMessage welcomeMsg = new ChatMessage(ChatMessage.Role.ASSISTANT,
            "你好！我是 Logisim 的 AI 助手。今天我能帮你什么？");
        chatHistory.add(welcomeMsg);
        messageListModel.addElement(welcomeMsg);
    }
    
    private void sendMessage()
    {
        String text = inputArea.getText().trim();
        if (text.isEmpty() && pendingFiles.isEmpty() && pendingImages.isEmpty())
        {
            return;
        }
        
        if (config.getApiKey().isEmpty())
        {
            JOptionPane.showMessageDialog(this,
                "请在设置中配置 API 密钥",
                "警告",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        StringBuilder content = new StringBuilder(text);
        ChatMessage userMessage = new ChatMessage(ChatMessage.Role.USER, content.toString());
        
        for (File file : pendingFiles)
        {
            userMessage.addAttachedFile(file);
            content.append("\n[附加文件: ").append(file.getName()).append("]");
        }
        
        for (Image image : pendingImages)
        {
            userMessage.addAttachedImage(image);
            content.append("\n[附加图片]");
        }
        
        userMessage.setContent(content.toString());
        chatHistory.add(userMessage);
        messageListModel.addElement(userMessage);
        
        inputArea.setText("");
        pendingFiles.clear();
        pendingImages.clear();
        
        sendButton.setEnabled(false);
        sendButton.setText("⏳");
        
        ChatMessage loadingMsg = new ChatMessage(ChatMessage.Role.ASSISTANT, "思考中...");
        loadingMsg.setRendering(true);
        chatHistory.add(loadingMsg);
        messageListModel.addElement(loadingMsg);
        
        final int loadingMsgIndex = messageListModel.getSize() - 1;
        
        aiService.sendMessage(chatHistory, config)
            .thenAccept(response ->
            {
                SwingUtilities.invokeLater(() ->
                {
                    try
                    {
                        System.out.println("\n✅ 收到AI响应,长度: " + (response != null ? response.length() : 0));
                        
                        if (response == null || response.trim().isEmpty())
                        {
                            loadingMsg.setContent("(空响应)");
                        } else
                        {
                            loadingMsg.setContent(response);
                        }
                        
                        loadingMsg.setRendering(false);
                        
                        int modelIndex = messageListModel.indexOf(loadingMsg);
                        if (modelIndex >= 0)
                        {
                            messageListModel.set(modelIndex, loadingMsg);
                        }
                        
                        messageList.repaint();
                        messageList.revalidate();
                        
                        sendButton.setEnabled(true);
                        sendButton.setText("➤");
                        
                        scrollToBottom();
                        
                        System.out.println("✅ UI已更新\n");
                    } catch (Exception e)
                    {
                        System.err.println("❌ 更新UI时出错: " + e.getMessage());
                        e.printStackTrace();
                        loadingMsg.setContent("显示错误: " + e.getMessage());
                        loadingMsg.setRendering(false);
                        int modelIndex = messageListModel.indexOf(loadingMsg);
                        if (modelIndex >= 0)
                        {
                            messageListModel.set(modelIndex, loadingMsg);
                        }
                        messageList.repaint();
                    }
                });
            })
            .exceptionally(error ->
            {
                SwingUtilities.invokeLater(() ->
                {
                    System.err.println("❌ AI请求失败: " + error.getMessage());
                    error.printStackTrace();
                    
                    String errorMsg = "请求失败";
                    if (error.getCause() != null)
                    {
                        errorMsg += ": " + error.getCause().getMessage();
                    } else if (error.getMessage() != null)
                    {
                        errorMsg += ": " + error.getMessage();
                    }
                    
                    loadingMsg.setContent(errorMsg);
                    loadingMsg.setRendering(false);
                    
                    int modelIndex = messageListModel.indexOf(loadingMsg);
                    if (modelIndex >= 0)
                    {
                        messageListModel.set(modelIndex, loadingMsg);
                    }
                    
                    messageList.repaint();
                    messageList.revalidate();
                    
                    sendButton.setEnabled(true);
                    sendButton.setText("➤");
                    
                    scrollToBottom();
                });
                return null;
            });
        
        scrollToBottom();
    }
    
    private void attachFiles()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "所有文件", "*"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files)
            {
                pendingFiles.add(file);
            }
            updateInputPlaceholder();
        }
    }
    
    private void attachImages()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "图片文件", "png", "jpg", "jpeg", "gif", "bmp"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files)
            {
                try
                {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null)
                    {
                        pendingImages.add(image);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            updateInputPlaceholder();
        }
    }
    
    private void updateInputPlaceholder()
    {
        int fileCount = pendingFiles.size() + pendingImages.size();
        if (fileCount > 0)
        {
            inputArea.setText("[已附加 " + fileCount + " 个文件]\n" + inputArea.getText());
        }
    }
    
    private void clearChat()
    {
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要清空对话吗？",
            "清空对话",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION)
        {
            chatHistory.clear();
            messageListModel.clear();
            addWelcomeMessage();
        }
    }
    
    private void openSettingsDialog()
    {
        AiConfigDialog dialog = new AiConfigDialog((Frame) SwingUtilities.getWindowAncestor(this), config);
        dialog.setVisible(true);
        
        if (dialog.isSaved())
        {
            config.saveToPreferences();
        }
    }
    
    private void scrollToBottom()
    {
        SwingUtilities.invokeLater(() ->
        {
            messageList.ensureIndexIsVisible(messageListModel.getSize() - 1);
        });
    }
    
    private class MessageCellRenderer implements ListCellRenderer<ChatMessage>
    {
        private final MarkdownRenderer markdownRenderer;
        
        public MessageCellRenderer()
        {
            this.markdownRenderer = new MarkdownRenderer();
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends ChatMessage> list,
                                                      ChatMessage message,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            if (message.getRole() == ChatMessage.Role.USER)
            {
                panel.setBackground(new Color(220, 240, 255));
            } else
            {
                panel.setBackground(Color.WHITE);
            }
            
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            
            JLabel roleLabel = new JLabel(message.getRole() == ChatMessage.Role.USER ? "你" : "AI");
            roleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
            
            JLabel timeLabel = new JLabel(message.getFormattedTime());
            timeLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            timeLabel.setForeground(Color.GRAY);
            
            headerPanel.add(roleLabel, BorderLayout.WEST);
            headerPanel.add(timeLabel, BorderLayout.EAST);
            
            panel.add(headerPanel, BorderLayout.NORTH);
            
            if (message.isRendering())
            {
                JLabel loadingLabel = new JLabel(message.getContent());
                loadingLabel.setFont(new Font("Microsoft YaHei", Font.ITALIC, 12));
                loadingLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                panel.add(loadingLabel, BorderLayout.CENTER);
            } else
            {
                String content = message.getContent();
                if (content != null && !content.trim().isEmpty())
                {
                    try
                    {
                        JComponent contentComponent = markdownRenderer.renderMarkdown(content);
                        contentComponent.setOpaque(false);
                        if (contentComponent.getParent() != null)
                        {
                            ((Container)contentComponent.getParent()).remove(contentComponent);
                        }
                        panel.add(contentComponent, BorderLayout.CENTER);
                    } catch (Exception e)
                    {
                        System.err.println("渲染Markdown失败: " + e.getMessage());
                        JTextArea fallbackText = new JTextArea(content);
                        fallbackText.setEditable(false);
                        fallbackText.setWrapStyleWord(true);
                        fallbackText.setLineWrap(true);
                        fallbackText.setOpaque(false);
                        fallbackText.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
                        fallbackText.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                        panel.add(fallbackText, BorderLayout.CENTER);
                    }
                } else
                {
                    JLabel emptyLabel = new JLabel("(无内容)");
                    emptyLabel.setFont(new Font("Microsoft YaHei", Font.ITALIC, 12));
                    emptyLabel.setForeground(Color.GRAY);
                    panel.add(emptyLabel, BorderLayout.CENTER);
                }
            }
            
            if (message.hasAttachments())
            {
                JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                attachmentPanel.setOpaque(false);
                
                for (File file : message.getAttachedFiles())
                {
                    JLabel fileLabel = new JLabel("📄 " + file.getName());
                    fileLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
                    attachmentPanel.add(fileLabel);
                }
                
                panel.add(attachmentPanel, BorderLayout.SOUTH);
            }
            
            return panel;
        }
    }
}