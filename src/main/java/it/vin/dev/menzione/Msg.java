package it.vin.dev.menzione;

import javax.swing.*;
import java.awt.*;

public class Msg {

    public static void error(Component parent, String message){
        JOptionPane.showMessageDialog(parent, message, "ERRORE", JOptionPane.ERROR_MESSAGE);
    }

    public static void warn(Component parent, String message){
        JOptionPane.showMessageDialog(parent, message, "ATTENZIONE", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Informazione", JOptionPane.INFORMATION_MESSAGE);
    }

    public static int yesno(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Scegli un'opzione", JOptionPane.YES_NO_OPTION);
    }

    public static int options(Component parent, String message, Object[] options) {
        return JOptionPane.showOptionDialog(parent,
                message,
                "Scegli un'opzione",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
    }

    public static int options(Component parent, String message, String title, int messageType, Object[] options) {
        return JOptionPane.showOptionDialog(parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                messageType,
                null,
                options,
                options[0]
        );
    }
}
