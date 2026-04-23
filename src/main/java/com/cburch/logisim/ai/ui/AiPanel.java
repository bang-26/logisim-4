package com.cburch.logisim.ai.ui;

import com.cburch.logisim.ai.config.AiConfig;
import com.cburch.logisim.ai.model.ChatMessage;
import com.cburch.logisim.ai.service.AiService;
import com.cburch.logisim.ai.service.AiServiceImpl;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
        setBackground(Color.WHITE);
        
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        messageListModel = new DefaultListModel<>();
        messageList = new JList<>(messageListModel);
        messageList.setCellRenderer(new MessageCellRenderer());
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setBackground(new Color(250, 250, 250));
        messageList.setFixedCellWidth(-1);
        messageList.setFixedCellHeight(-1);
        
        enableGlobalCopyForMessageList();
        
        JScrollPane scrollPane = new JScrollPane(messageList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(66, 133, 244));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        JLabel titleLabel = new JLabel(createRobotIcon(), SwingConstants.LEFT);
        titleLabel.setText(" AI 助手");
        titleLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        titleLabel.setFont(createMixedFont(Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        settingsButton = createStyledIconButton(getSettingsIconString(), "AI 设置", new Color(255, 255, 255, 50));
        settingsButton.addActionListener(e -> openSettingsDialog());
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(settingsButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private Icon createRobotIcon()
    {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int centerX = x + 12;
                int centerY = y + 12;
                
                int headRadius = 6;
                g2d.drawOval(centerX - headRadius, centerY - headRadius - 2, headRadius * 2, headRadius * 2);
                
                int eyeOffset = 3;
                int eyeY = centerY - 2;
                int eyeSize = 2;
                g2d.fillOval(centerX - eyeOffset - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);
                g2d.fillOval(centerX + eyeOffset - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);
                
                g2d.drawArc(centerX - 2, centerY, 4, 3, 0, -180);
                
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawLine(centerX, centerY - headRadius - 2, centerX, centerY - headRadius - 4);
                g2d.fillOval(centerX - 1, centerY - headRadius - 6, 2, 2);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 24;
            }
            
            @Override
            public int getIconHeight() {
                return 24;
            }
        };
    }
    
    private String getSettingsIconString()
    {
        return "\u2699";
    }
    
    private JPanel createInputPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        
        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(createMixedFont(Font.PLAIN, 14));
        inputArea.enableInputMethods(true);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        inputArea.setBackground(new Color(250, 250, 250));
        
        inputArea.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown())
                {
                    e.consume();
                    sendMessage();
                }
            }
        });
        
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
        inputScroll.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonPanel.setBackground(Color.WHITE);
        
        attachFileButton = createStyledIconButton("📎", "附加文件", new Color(66, 133, 244, 20));
        attachFileButton.addActionListener(e -> attachFiles());
        
        attachImageButton = createStyledIconButton("🖼", "附加图片", new Color(52, 168, 83, 20));
        attachImageButton.addActionListener(e -> attachImages());
        
        clearChatButton = createStyledIconButton("🗑", "清空对话", new Color(234, 67, 53, 20));
        clearChatButton.addActionListener(e -> clearChat());
        
        sendButton = createStyledIconButton("➤", "发送给 AI (Enter)", new Color(66, 133, 244));
        sendButton.setForeground(Color.WHITE);
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
    
    private Font createMixedFont(int style, int size)
    {
        String chineseFont = "Microsoft YaHei";
        String englishFont = "Monaco";
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        boolean hasChinese = false;
        boolean hasMonaco = false;
        
        for (String font : availableFonts)
        {
            if (font.equals(chineseFont))
            {
                hasChinese = true;
            }
            if (font.equals(englishFont))
            {
                hasMonaco = true;
            }
        }
        
        if (hasMonaco && hasChinese)
        {
            return new Font(chineseFont, style, size);
        } else if (hasChinese)
        {
            return new Font(chineseFont, style, size);
        } else if (hasMonaco)
        {
            return new Font(englishFont, style, size);
        } else
        {
            return new Font(Font.SANS_SERIF, style, size);
        }
    }
    
    private JButton createStyledIconButton(String icon, String tooltip, Color backgroundColor)
    {
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(40, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setBackground(backgroundColor);
        button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseEntered(java.awt.event.MouseEvent evt)
            {
                button.setBackground(darkenColor(backgroundColor, 0.9f));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt)
            {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    private Color darkenColor(Color color, float factor)
    {
        int r = Math.max(0, (int)(color.getRed() * factor));
        int g = Math.max(0, (int)(color.getGreen() * factor));
        int b = Math.max(0, (int)(color.getBlue() * factor));
        int a = color.getAlpha();
        return new Color(r, g, b, a);
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
        sendButton.setText("\u23F3");
        
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
                        sendButton.setText("\u27A4");
                        
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
                    sendButton.setText("\u27A4");
                    
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
    
    private void enableGlobalCopyForMessageList()
    {
        messageList.setFocusable(true);
        
        messageList.getInputMap().put(KeyStroke.getKeyStroke("control C"), "copyAction");
        messageList.getActionMap().put("copyAction", new AbstractAction()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                copySelectedText();
            }
        });
    }
    
    private void copySelectedText()
    {
        Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        
        if (focusedComponent instanceof JTextComponent)
        {
            JTextComponent textComponent = (JTextComponent) focusedComponent;
            String selectedText = textComponent.getSelectedText();
            
            if (selectedText != null && !selectedText.isEmpty())
            {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(selectedText), null);
                return;
            }
        }
        
        int selectedIndex = messageList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < chatHistory.size())
        {
            ChatMessage selectedMessage = chatHistory.get(selectedIndex);
            String content = selectedMessage.getContent();
            
            if (content != null && !content.isEmpty())
            {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(content), null);
            }
        }
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
            panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
            
            if (message.getRole() == ChatMessage.Role.USER)
            {
                panel.setBackground(new Color(232, 245, 255));
            } else
            {
                panel.setBackground(Color.WHITE);
            }
            
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            
            JLabel roleLabel = new JLabel(message.getRole() == ChatMessage.Role.USER ? 
                createUserIcon() : createAssistantIcon(), SwingConstants.LEFT);
            roleLabel.setText(message.getRole() == ChatMessage.Role.USER ? " 你" : " AI");
            roleLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            roleLabel.setFont(createMixedFont(Font.BOLD, 13));
            roleLabel.setForeground(message.getRole() == ChatMessage.Role.USER ? 
                new Color(66, 133, 244) : new Color(52, 168, 83));
            
            JLabel timeLabel = new JLabel(message.getFormattedTime());
            timeLabel.setFont(createMixedFont(Font.PLAIN, 11));
            timeLabel.setForeground(new Color(140, 140, 140));
            
            headerPanel.add(roleLabel, BorderLayout.WEST);
            headerPanel.add(timeLabel, BorderLayout.EAST);
            
            panel.add(headerPanel, BorderLayout.NORTH);
            
            if (message.isRendering())
            {
                JPanel loadingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                loadingPanel.setOpaque(false);
                
                JLabel loadingLabel = new JLabel(createThinkingIcon(), SwingConstants.LEFT);
                loadingLabel.setText(" " + message.getContent());
                loadingLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
                loadingLabel.setFont(createMixedFont(Font.ITALIC, 13));
                loadingLabel.setForeground(new Color(100, 100, 100));
                loadingPanel.add(loadingLabel);
                
                panel.add(loadingPanel, BorderLayout.CENTER);
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
                            Container parent = (Container)contentComponent.getParent();
                            if (parent != null) {
                                parent.remove(contentComponent);
                            }
                        }
                        panel.add(contentComponent, BorderLayout.CENTER);
                    } catch (Exception e)
                    {
                        System.err.println("渲染Markdown失败: " + e.getMessage());
                        e.printStackTrace();
                        JTextArea fallbackText = new JTextArea(content);
                        fallbackText.setEditable(false);
                        fallbackText.setWrapStyleWord(true);
                        fallbackText.setLineWrap(true);
                        fallbackText.setOpaque(false);
                        fallbackText.setFont(createMixedFont(Font.PLAIN, 13));
                        fallbackText.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                        fallbackText.setBackground(panel.getBackground());
                        
                        JScrollPane scrollPane = new JScrollPane(fallbackText);
                        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        
                        panel.add(scrollPane, BorderLayout.CENTER);
                    }
                } else
                {
                    JLabel emptyLabel = new JLabel("(无内容)");
                    emptyLabel.setFont(createMixedFont(Font.ITALIC, 12));
                    emptyLabel.setForeground(Color.GRAY);
                    panel.add(emptyLabel, BorderLayout.CENTER);
                }
            }
            
            if (message.hasAttachments())
            {
                JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
                attachmentPanel.setOpaque(false);
                attachmentPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                
                for (File file : message.getAttachedFiles())
                {
                    JLabel fileLabel = new JLabel(createFileIcon(), SwingConstants.LEFT);
                    fileLabel.setText(" " + file.getName());
                    fileLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
                    fileLabel.setFont(createMixedFont(Font.PLAIN, 12));
                    fileLabel.setForeground(new Color(66, 133, 244));
                    attachmentPanel.add(fileLabel);
                }
                
                panel.add(attachmentPanel, BorderLayout.SOUTH);
            }
            
            return panel;
        }
        
        private Icon createUserIcon()
        {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(66, 133, 244));
                    
                    int centerX = x + 8;
                    int centerY = y + 8;
                    
                    g2d.fillOval(centerX - 4, centerY - 6, 8, 8);
                    
                    int[] xPoints = {centerX - 6, centerX + 6, centerX + 6, centerX - 6};
                    int[] yPoints = {centerY + 6, centerY + 6, centerY + 2, centerY + 2};
                    g2d.fillPolygon(xPoints, yPoints, 4);
                    
                    g2d.dispose();
                }
                
                @Override
                public int getIconWidth() { return 16; }
                
                @Override
                public int getIconHeight() { return 16; }
            };
        }
        
        private Icon createAssistantIcon()
        {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(52, 168, 83));
                    g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    int centerX = x + 8;
                    int centerY = y + 8;
                    
                    g2d.drawOval(centerX - 5, centerY - 5, 10, 10);
                    
                    int eyeOffset = 2;
                    int eyeY = centerY - 1;
                    g2d.fillOval(centerX - eyeOffset - 1, eyeY - 1, 2, 2);
                    g2d.fillOval(centerX + eyeOffset - 1, eyeY - 1, 2, 2);
                    
                    g2d.drawArc(centerX - 2, centerY + 1, 4, 3, 0, -180);
                    
                    g2d.dispose();
                }
                
                @Override
                public int getIconWidth() { return 16; }
                
                @Override
                public int getIconHeight() { return 16; }
            };
        }
        
        private Icon createThinkingIcon()
        {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(100, 100, 100));
                    
                    int centerX = x + 8;
                    int centerY = y + 8;
                    
                    g2d.setFont(new Font("Microsoft YaHei", Font.ITALIC, 12));
                    g2d.drawString("\u2601", centerX - 6, centerY + 4);
                    
                    g2d.dispose();
                }
                
                @Override
                public int getIconWidth() { return 16; }
                
                @Override
                public int getIconHeight() { return 16; }
            };
        }
        
        private Icon createFileIcon()
        {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(66, 133, 244));
                    g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    int[] xPoints = {x + 3, x + 9, x + 13, x + 13, x + 3};
                    int[] yPoints = {y + 2, y + 2, y + 6, y + 14, y + 14};
                    g2d.drawPolygon(xPoints, yPoints, 5);
                    g2d.drawLine(x + 9, y + 2, x + 9, y + 6);
                    g2d.drawLine(x + 9, y + 6, x + 13, y + 6);
                    
                    g2d.drawLine(x + 6, y + 9, x + 10, y + 9);
                    g2d.drawLine(x + 6, y + 11, x + 10, y + 11);
                    
                    g2d.dispose();
                }
                
                @Override
                public int getIconWidth() { return 16; }
                
                @Override
                public int getIconHeight() { return 16; }
            };
        }
    }
}
