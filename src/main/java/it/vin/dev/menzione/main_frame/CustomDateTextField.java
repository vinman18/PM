package it.vin.dev.menzione.main_frame;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;

/**
 * Created by Vincenzo on 17/10/2016.
 */
public class CustomDateTextField extends JFormattedTextField{

    private MaskFormatter dateMask;

    public CustomDateTextField() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent arg0) {

                JTextField source = (JTextField) arg0.getSource();
                source.setCaretPosition(0);

            }
        });
        setColumns(10);
        try {
            dateMask = new MaskFormatter("##/##/####");
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        dateMask.install(this);
    }
}
