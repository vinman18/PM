package it.vin.dev.menzione.frame;

import it.vin.dev.menzione.Msg;
import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.logica.ConfigurationManager;
import it.vin.dev.menzione.main_frame.CustomDateTextField;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Created by Vincenzo on 07/06/2016.
 */
public class ConfigFrame extends JFrame{
    private JPanel rootPanel;

    private ConfigurationManager configurationManager;
    private ResourceBundle strings;
    private Map<String, JComponent> keysComponents;

    public static void open(int closeOption) {
        JFrame configFrame = new ConfigFrame();
        configFrame.setDefaultCloseOperation(closeOption);
        configFrame.pack();
        configFrame.setSize(600, 500);
        configFrame.setVisible(true);
    }

    private ConfigFrame() {
        this.keysComponents = new HashMap<>();
        configurationManager = ConfigurationManager.getInstance();
        strings = ResourceBundle.getBundle("Localization/Strings");
        rootPanel = new JPanel(new BorderLayout());
        createUIComponents();
        setContentPane(rootPanel);

        setTitle(strings.getString("app.title") + " - " + strings.getString("app.version"));
        setIconImage(Toolkit.getDefaultToolkit().createImage(ViaggiUtils.getMainIcon()));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width / 2) -  (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
    }

    private void createUIComponents() {
        JPanel centerPanel = getConfigPanel();
        rootPanel.add(centerPanel, BorderLayout.CENTER);

        JButton cancelBtn = new JButton(strings.getString("generic.cancel"));
        cancelBtn.addActionListener(e -> {
            if(ConfigFrame.this.getDefaultCloseOperation() == WindowConstants.EXIT_ON_CLOSE) {
                System.exit(0);
            } else {
                dispose();
            }
        });

        JButton saveBtn = new JButton(strings.getString("generic.save"));
        saveBtn.addActionListener(e -> {
            try {
                save();
            } catch (Exception e1) {
                Msg.error(null, MessageFormat.format(strings.getString("configframe.save.error"), e1.getMessage()));
            }

            Msg.info(null, MessageFormat.format(strings.getString("configframe.save.success"), ViaggiUtils.getConfigPath()));
        });
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(cancelBtn);
        bottomPanel.add(saveBtn);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel getConfigPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("CenterPanel"));
        Iterator<String> keys = configurationManager.getConfiguration().getKeys();
        int currentRow = 0;
        while (keys.hasNext()) {
            String key = keys.next();
            String labelText = key;
            try { labelText = strings.getString("configuration." + key); } catch (MissingResourceException ignored) {}
            JLabel prefLabel = ViaggiFrameUtils.multiStyledLabel(
                    new ViaggiFrameUtils.StyledText(labelText, null, 4),
                    new ViaggiFrameUtils.StyledText("\n " + key, Color.gray, 2)
            );
            prefLabel.setFont(prefLabel.getFont().deriveFont(25f));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 0.1; gbc.weighty = 1; gbc.insets = new Insets(1, 1, 1, 1);
            gbc.gridx = 0; gbc.gridy = currentRow; gbc.fill = GridBagConstraints.HORIZONTAL;
            centerPanel.add(prefLabel, gbc);

            JComponent prefField = getComponentPerKey(key);
            this.keysComponents.put(key, prefField);
            gbc = new GridBagConstraints();
            gbc.weightx = 0.9; gbc.weighty = 1; gbc.insets = new Insets(1, 1, 1, 1);
            gbc.gridx = 1; gbc.gridy = currentRow; gbc.fill = GridBagConstraints.BOTH; gbc.anchor = GridBagConstraints.LAST_LINE_START;
            centerPanel.add(prefField, gbc);
            currentRow++;
        }

        return centerPanel;
    }

    private JComponent getComponentPerKey(String key) {
        ConfigurationManager.FIELD_TYPES fieldType = ConfigurationManager.keyTypesMap.get(key);
        JComponent toReturn;
        switch (fieldType) {
            case INTEGER:
                int integer = configurationManager.getConfiguration().getInt(key);
                JSpinner spinner = new JSpinner(new SpinnerNumberModel());
                JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(spinner, "#");
                numberEditor.getTextField().setHorizontalAlignment(JTextField.LEADING);
                spinner.setEditor(numberEditor);
                spinner.setValue(integer);
                toReturn = spinner;
                break;
            case SECRET:
                String decodedValue = configurationManager.getConfiguration().getEncodedString(key);
                toReturn = new JPasswordField(decodedValue);
                break;
            case TEXT:
            default:
                String value = configurationManager.getConfiguration().getString(key);
                toReturn = new JTextField(value);
                break;
        }
        toReturn.setFont(toReturn.getFont().deriveFont(15f));
        return toReturn;
    }

    private void save() throws Exception {
        Set<String> keys = keysComponents.keySet();
        for (String key : keys) {
            JComponent component = keysComponents.get(key);
            ConfigurationManager.FIELD_TYPES fieldType = ConfigurationManager.keyTypesMap.get(key);
            Object value;
            if(fieldType != null) {
                switch (fieldType) {
                    case INTEGER:
                        JSpinner spinner;
                        if(component instanceof JSpinner) {
                            spinner = ((JSpinner) component);
                        } else {
                            continue;
                        }
                        spinner.commitEdit();
                        value = spinner.getValue();
                        break;
                    case TEXT:
                        JTextField textField;
                        if(component instanceof JTextField) {
                            textField = ((JTextField) component);
                        } else {
                            continue;
                        }
                        value = textField.getText();
                        break;
                    case SECRET:
                        JPasswordField passwordField;
                        if(component instanceof JPasswordField) {
                            passwordField = ((JPasswordField) component);
                        } else {
                            continue;
                        }
                        value = ViaggiUtils.encrypt(new String(passwordField.getPassword()));
                        break;
                    default:
                        continue;
                }

                configurationManager.getConfiguration().setProperty(key, value);
            }
        }

        configurationManager.save();
    }
}