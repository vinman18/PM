package it.vin.dev.menzione.main_frame;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.net.URL;

public class MessageJLabel extends JLabel {

    private ImageIcon errorIcon;
    private ImageIcon infoIcon;
    private ImageIcon warnIcon;
    private ImageIcon uploadIcon;

    public MessageJLabel() {
        super();
        Border padding = new EmptyBorder(3,3,3,3);
        Border border = new SoftBevelBorder(SoftBevelBorder.LOWERED);
        this.setBorder(new CompoundBorder(border, padding));
        this.setFont(getFont().deriveFont(12f));
        errorIcon = new ImageIcon(this.getClass().getResource("/Icons/cancel16.png"));
        infoIcon = new ImageIcon(this.getClass().getResource("/Icons/info16.png"));
        warnIcon = new ImageIcon(this.getClass().getResource("/Icons/warning16.png"));
        uploadIcon = new ImageIcon(this.getClass().getResource("/Icons/upload16.png"));
    }

    public void setErrorMessage(String message) {
        this.setForeground(new Color(255,64,64));
        this.setText(message);
        this.setIcon(errorIcon);
        clearMouseListeners();
    }

    public void setInfoMessage(String message) {
        this.setForeground(new Color(33,101,255));
        this.setText(message);
        this.setIcon(infoIcon);
        clearMouseListeners();
    }

    public void setWarnMessage(String message) {
        this.setForeground(new Color(255,159,0));
        this.setText(message);
        this.setIcon(warnIcon);
        clearMouseListeners();
    }

    public void setUploadMessage(String message) {
        this.setForeground(new Color(0,166,0));
        this.setText(message);
        this.setIcon(uploadIcon);
        clearMouseListeners();
    }

    public void clearMessage() {
        this.setText("");
        this.setIcon(null);
        clearMouseListeners();
    }

    public void clearMouseListeners() {
        MouseListener[] mouseListeners = this.getMouseListeners();
        for (MouseListener mouseListener : mouseListeners) {
            this.removeMouseListener(mouseListener);
        }
    }
}
