package it.vin.dev.menzione.main_frame;

import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FormattedTextFieldDateChangeListener implements DateChangeListener {
    JFormattedTextField txtField;

    public FormattedTextFieldDateChangeListener(JFormattedTextField txtField) {
        this.txtField = txtField;
    }

    @Override
    public void dateChanged(DateChangeEvent event) {
        LocalDate newDate = event.getNewDate();
        if(newDate == null) {
            return;
        }
        txtField.setValue(newDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtField.setBackground(Color.WHITE);
    }
}
