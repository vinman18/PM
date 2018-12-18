package it.vin.dev.menzione.frame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ViaggiFrameUtils {
    private static Logger logger = LogManager.getLogger(ViaggiFrameUtils.class);
    public static final boolean DEBUG_FRAME = false;

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

    public static JButton newButton(String text, ActionListener action, @Nullable String actionCommand) {
        JButton button = new JButton(text);
        button.addActionListener(action);

        if(actionCommand != null) {
            button.setActionCommand(actionCommand);
        }

        return button;
    }

    public static JButton newIconButton(String resourcePath, String alternateText, ActionListener action, @Nullable String actionCommand) {
        JButton button = new JButton();
        try {
            Image reloadIcon = ImageIO.read(ViaggiFrameUtils.class.getResource(resourcePath));
            button.setIcon(new ImageIcon(reloadIcon));
        } catch (IOException e) {
            logger.warn("Failed to load button icon", e);
            button.setText(alternateText);
        }

        button.addActionListener(action);

        if(actionCommand != null) {
            button.setActionCommand(actionCommand);
        }

        return button;
    }

}
