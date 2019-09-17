package it.vin.dev.menzione.frame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ViaggiFrameUtils {
    public static class StyledText {
        String text;
        Color color;
        Integer size;

        public StyledText(String text, Color color, Integer size) {
            this.text = text;
            this.color = color;
            this.size = size;
        }
    }

    private static Logger logger = LogManager.getLogger(ViaggiFrameUtils.class);
    public static final boolean DEBUG_FRAME = false;

    public static String colorHexString(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static JLabel newJLabel(String text, @Nullable Font font) {
        JLabel label = new JLabel(text);
        if(font != null) {
            label.setFont(font);
        }
        return label;
    }

    public static JLabel newJLabel(String text) {
        return newJLabel(text, null);
    }

    public static JLabel multiStyledLabel(StyledText... textParts) {
        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<html>");
        for (StyledText textPart : textParts) {
            String text = textPart.text.replace("\n", "<br>");
            htmlString.append("<font");
            if (textPart.color != null) {
                htmlString.append(" color='")
                        .append(colorHexString(textPart.color))
                        .append("'");
            }
            if (textPart.size != null) {
                htmlString.append(" size=")
                        .append(textPart.size);
            }
            htmlString.append(">")
                    .append(text)
                    .append("</font>");
        }
        htmlString.append("</html>");
        return newJLabel(htmlString.toString());
    }

    public static JButton newButton(String text, ActionListener action, @Nullable String actionCommand) {
        JButton button = new JButton(text);
        button.addActionListener(action);

        if(actionCommand != null) {
            button.setActionCommand(actionCommand);
        }

        return button;
    }

    public static JMenuItem newMenuItemButton(String text, ActionListener action, @Nullable String actionCommand) {
        JMenuItem button = new JMenuItem(text);
        button.addActionListener(action);

        if(actionCommand != null) {
            button.setActionCommand(actionCommand);
        }

        return button;
    }

    public static JButton newIconButton(String resourcePath, ActionListener action, @Nullable String actionCommand) {
        JButton button = new JButton();
        Image reloadIcon = Toolkit.getDefaultToolkit().getImage(ViaggiFrameUtils.class.getResource(resourcePath));
        button.setIcon(new ImageIcon(reloadIcon));

        button.addActionListener(action);

        if(actionCommand != null) {
            button.setActionCommand(actionCommand);
        }

        return button;
    }

    public static void selectTableCell(JTable table, int row, int col) {
        int rowCount = table.getRowCount();
        if(row >= rowCount) { //must be 0 <= row <= rowCount-1
            row = rowCount - 1; //if not we select the last row
        }

        table.changeSelection(row, col, false, false);
        //table.editCellAt(row, col);
        table.requestFocus();
    }

    public static Icon getIcon(String resourcePath) {
        Image image = Toolkit.getDefaultToolkit().getImage(ViaggiFrameUtils.class.getResource(resourcePath));
        return new ImageIcon(image);
    }
}
