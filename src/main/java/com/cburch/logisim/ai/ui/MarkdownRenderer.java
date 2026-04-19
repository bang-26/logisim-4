package com.cburch.logisim.ai.ui;

/**
 * **************************************************************************************************
 * ProjectName   ：  logisim-4
 * Package       ：  com.cburch.logisim.ai.ui
 * ClassName     ：  MarkdownRenderer
 * CreateTime    ：  2026-04-19 16:07
 * Author        ：  Issac_Al
 * Email         ：  IssacAl@qq.com
 * IDE           ：  IntelliJ IDEA 2020.3.4
 * Version       ：  1.0
 * CodedFormat   ：  utf-8
 * Description   ：  Java Class
 * **************************************************************************************************
 */


import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;

public class MarkdownRenderer
{
    private final Parser parser;
    private final HtmlRenderer htmlRenderer;
    
    public MarkdownRenderer()
    {
        this.parser = Parser.builder()
                          .extensions(Arrays.asList(TablesExtension.create()))
                          .build();
        
        this.htmlRenderer = HtmlRenderer.builder()
                                .extensions(Arrays.asList(TablesExtension.create()))
                                .build();
    }
    
    public JComponent renderMarkdown(String markdown)
    {
        if (markdown == null || markdown.trim().isEmpty())
        {
            JTextArea emptyText = new JTextArea("(空内容)");
            emptyText.setEditable(false);
            emptyText.setOpaque(false);
            emptyText.setFont(createMixedFont(Font.ITALIC, 12));
            emptyText.setForeground(Color.GRAY);
            return new JScrollPane(emptyText);
        }
        
        try
        {
            JTextPane textPane = new JTextPane();
            textPane.setEditable(false);
            textPane.setContentType("text/html");
            textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            textPane.setFont(createMixedFont(Font.PLAIN, 14));
            textPane.setBackground(Color.WHITE);
            
            String html = convertMarkdownToHtml(markdown);
            textPane.setText(html);
            
            textPane.setCaretPosition(0);
            
            JScrollPane scrollPane = new JScrollPane(textPane);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            int preferredHeight = Math.min(textPane.getPreferredSize().height + 20, 2000);
            scrollPane.setPreferredSize(new Dimension(
                scrollPane.getPreferredSize().width,
                preferredHeight
            ));
            
            return scrollPane;
        } catch (Exception e)
        {
            System.err.println("Markdown渲染失败，使用纯文本: " + e.getMessage());
            
            JTextArea fallbackArea = new JTextArea(markdown);
            fallbackArea.setEditable(false);
            fallbackArea.setWrapStyleWord(true);
            fallbackArea.setLineWrap(true);
            fallbackArea.setFont(createMixedFont(Font.PLAIN, 14));
            fallbackArea.setOpaque(false);
            fallbackArea.setBackground(Color.WHITE);
            
            JScrollPane scrollPane = new JScrollPane(fallbackArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            int preferredHeight = Math.min(fallbackArea.getPreferredSize().height + 20, 2000);
            scrollPane.setPreferredSize(new Dimension(
                scrollPane.getPreferredSize().width,
                preferredHeight
            ));
            
            return scrollPane;
        }
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
        
        if (hasChinese)
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
    
    private String convertMarkdownToHtml(String markdown)
    {
        Node document = parser.parse(markdown);
        String html = htmlRenderer.render(document);
        
        return "<html><head><style>" +
                   "body { font-family: 'Microsoft YaHei', 'Segoe UI', Arial, sans-serif; padding: 10px; line-height: 1.8; color: #333; }" +
                   "h1, h2, h3, h4, h5, h6 { color: #2c3e50; margin-top: 15px; margin-bottom: 10px; }" +
                   "h1 { font-size: 24px; border-bottom: 2px solid #eee; padding-bottom: 5px; }" +
                   "h2 { font-size: 20px; border-bottom: 1px solid #eee; padding-bottom: 5px; }" +
                   "h3 { font-size: 18px; }" +
                   "p { margin: 8px 0; }" +
                   "pre { background-color: #f6f8fa; padding: 12px; border-radius: 6px; overflow-x: auto; border: 1px solid #e1e4e8; }" +
                   "code { background-color: #f6f8fa; padding: 2px 6px; border-radius: 3px; font-family: 'Monaco', 'Consolas', 'Courier New', monospace; font-size: 13px; color: #e83e8c; }" +
                   "pre code { background-color: transparent; padding: 0; color: #333; }" +
                   "table { border-collapse: collapse; width: 100%; margin: 10px 0; }" +
                   "th, td { border: 1px solid #dfe2e5; padding: 8px 12px; text-align: left; }" +
                   "th { background-color: #f6f8fa; font-weight: bold; color: #24292e; }" +
                   "tr:nth-child(even) { background-color: #f9f9f9; }" +
                   "blockquote { border-left: 4px solid #3b82f6; padding: 8px 12px; margin: 10px 0; background-color: #f0f7ff; color: #555; border-radius: 0 4px 4px 0; }" +
                   "ul, ol { padding-left: 25px; margin: 8px 0; }" +
                   "li { margin: 4px 0; }" +
                   "a { color: #3b82f6; text-decoration: none; }" +
                   "a:hover { text-decoration: underline; }" +
                   "strong { color: #1a1a1a; font-weight: bold; }" +
                   "em { color: #555; font-style: italic; }" +
                   "hr { border: none; border-top: 2px solid #e1e4e8; margin: 15px 0; }" +
                   "img { max-width: 100%; height: auto; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                   "</style></head><body>" + html + "</body></html>";
    }
    
    private void applyStyles(StyledDocument doc)
    {
        StyleContext context = StyleContext.getDefaultStyleContext();
        
        Style defaultStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defaultStyle, "Segoe UI");
        StyleConstants.setFontSize(defaultStyle, 13);
        
        Style headingStyle = context.addStyle("heading", defaultStyle);
        StyleConstants.setBold(headingStyle, true);
        StyleConstants.setFontSize(headingStyle, 16);
        
        Style codeStyle = context.addStyle("code", defaultStyle);
        StyleConstants.setFontFamily(codeStyle, "Consolas");
        StyleConstants.setBackground(codeStyle, new Color(245, 245, 245));
    }
    
    public static JPanel createCodeBlockWithCopy(String code, String language)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        panel.setBackground(new Color(246, 248, 250));
        
        JTextArea codeArea = new JTextArea(code);
        codeArea.setEditable(false);
        codeArea.setFont(new Font("Monaco", Font.PLAIN, 13));
        codeArea.setBackground(new Color(246, 248, 250));
        codeArea.setForeground(new Color(36, 41, 47));
        codeArea.setCaretColor(Color.BLACK);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        
        JLabel langLabel = new JLabel(language != null && !language.isEmpty() ? language.toUpperCase() : "CODE");
        langLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        langLabel.setForeground(new Color(88, 88, 88));
        
        JButton copyButton = new JButton("\uD83D\uDCCB 复制");
        copyButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        copyButton.setBackground(new Color(66, 133, 244));
        copyButton.setForeground(Color.WHITE);
        copyButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        copyButton.setFocusPainted(false);
        copyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyButton.addActionListener(e ->
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(code), null);
            copyButton.setText("\u2705 已复制");
            copyButton.setBackground(new Color(52, 168, 83));
            Timer timer = new Timer(2000, evt -> {
                copyButton.setText("\uD83D\uDCCB 复制");
                copyButton.setBackground(new Color(66, 133, 244));
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        headerPanel.add(langLabel, BorderLayout.WEST);
        headerPanel.add(copyButton, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(codeArea), BorderLayout.CENTER);
        
        return panel;
    }
}