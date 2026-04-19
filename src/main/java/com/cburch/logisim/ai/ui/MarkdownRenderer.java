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
            emptyText.setFont(new Font("Microsoft YaHei", Font.ITALIC, 12));
            emptyText.setForeground(Color.GRAY);
            return new JScrollPane(emptyText);
        }
        
        try
        {
            JTextPane textPane = new JTextPane();
            textPane.setEditable(false);
            textPane.setContentType("text/html");
            textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            textPane.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
            
            String html = convertMarkdownToHtml(markdown);
            textPane.setText(html);
            
            textPane.setCaretPosition(0);
            
            JScrollPane scrollPane = new JScrollPane(textPane);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            scrollPane.setPreferredSize(new Dimension(
                scrollPane.getPreferredSize().width,
                Math.min(scrollPane.getPreferredSize().height, 400)
            ));
            
            return scrollPane;
        } catch (Exception e)
        {
            System.err.println("Markdown渲染失败，使用纯文本: " + e.getMessage());
            
            JTextArea fallbackArea = new JTextArea(markdown);
            fallbackArea.setEditable(false);
            fallbackArea.setWrapStyleWord(true);
            fallbackArea.setLineWrap(true);
            fallbackArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
            fallbackArea.setOpaque(false);
            
            JScrollPane scrollPane = new JScrollPane(fallbackArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            return scrollPane;
        }
    }
    
    private String convertMarkdownToHtml(String markdown)
    {
        Node document = parser.parse(markdown);
        String html = htmlRenderer.render(document);
        
        return "<html><head><style>" +
                   "body { font-family: 'Segoe UI', Arial, sans-serif; padding: 10px; line-height: 1.6; }" +
                   "pre { background-color: #f5f5f5; padding: 10px; border-radius: 5px; overflow-x: auto; }" +
                   "code { background-color: #f5f5f5; padding: 2px 5px; border-radius: 3px; font-family: 'Consolas', monospace; }" +
                   "pre code { background-color: transparent; padding: 0; }" +
                   "table { border-collapse: collapse; width: 100%; margin: 10px 0; }" +
                   "th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }" +
                   "th { background-color: #f0f0f0; }" +
                   "blockquote { border-left: 4px solid #ddd; padding-left: 10px; margin-left: 0; color: #666; }" +
                   "img { max-width: 100%; height: auto; }" +
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
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        JTextArea codeArea = new JTextArea(code);
        codeArea.setEditable(false);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        codeArea.setBackground(new Color(245, 245, 245));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel langLabel = new JLabel(language != null ? language : "code");
        langLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e ->
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(code), null);
            copyButton.setText("Copied!");
            Timer timer = new Timer(2000, evt -> copyButton.setText("Copy"));
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