package it.vin.dev.menzione.main_frame;

import com.github.lgooddatepicker.optionalusertools.DateHighlightPolicy;
import com.github.lgooddatepicker.zinternaltools.HighlightInformation;
import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.ViaggiUtils;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DatePickerExistingDatesHighlightPolicy implements DateHighlightPolicy {
    private HashMap<Long, LocalDate> existingDates;

    private HighlightInformation highlightInformation = new HighlightInformation(
            Consts.Colors.ACCENT,
            Color.WHITE,
            null
    );

    public DatePickerExistingDatesHighlightPolicy(List<LocalDate> dates) {
        this.existingDates = new HashMap<>(dates.size());
        for (LocalDate date : dates) {
            addDate(date);
        }
    }

    @Override
    public HighlightInformation getHighlightInformationOrNull(LocalDate date) {
        if(existingDates.containsKey(date.toEpochDay())) {
            return highlightInformation;
        }

        return null;
    }

    public void addDate(LocalDate date) {
        long key = date.toEpochDay();
        this.existingDates.put(key, date);
    }

    public void removeDate(LocalDate date) {
        long key = date.toEpochDay();
        this.existingDates.remove(key);
    }

    public void replaceDates(List<LocalDate> existingDates) {
        this.existingDates.clear();
        for (LocalDate existingDate : existingDates) {
            addDate(existingDate);
        }
    }
}
