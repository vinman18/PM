package it.vin.dev.menzione.frame;

import it.vin.dev.menzione.Msg;
import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.logica.ConfigurationManager;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Created by Vincenzo on 07/06/2016.
 */
public class ConfigFrameOld extends JFrame{
    private JTable table1;
    private JButton annullaButton;
    private JButton salvaButton;
    private JPanel rootPanel;

    private ConfigurationManager c;

    public static void open(int closeOption) {
        JFrame configFrame = new ConfigFrameOld();
        configFrame.setDefaultCloseOperation(closeOption);
        configFrame.setVisible(true);
    }

    public ConfigFrameOld() {
        rootPanel = new JPanel(new BorderLayout());
        createUIComponents();

        setContentPane(rootPanel);

        ResourceBundle strings = ResourceBundle.getBundle("Localization/Strings");
        setTitle(strings.getString("app.title") + " - " + strings.getString("app.version"));
        setIconImage(Toolkit.getDefaultToolkit().createImage(ViaggiUtils.getMainIcon()));

        c = ConfigurationManager.getInstance();
        popolaTabella();

        annullaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(ConfigFrameOld.this.getDefaultCloseOperation() == EXIT_ON_CLOSE) {
                    System.exit(0);
                } else {
                    dispose();
                }
            }
        });

        salvaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vector<Vector> elements = ((DefaultTableModel) table1.getModel()).getDataVector();

                for (Vector<String> vector : elements){
                    c.getConfiguration().setProperty(vector.get(0), vector.get(1));
                }

                try {
                    c.save();
                } catch (ConfigurationException e1) {
                    Msg.error(null, "Impossibile salvare\n" + e1.getMessage());
                }

                Msg.info(null, "Salvataggio completato in " + ViaggiUtils.getConfigPath());
            }
        });

        setSize(500, 300);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width / 2) -  (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
    }

    private void createUIComponents() {
        table1 = new JTable(new DefaultTableModel(new Object[]{"NOME", "VALORE"}, 0));
        annullaButton = new JButton("Annulla");
        salvaButton = new JButton("Salva");

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(annullaButton);
        bottomPanel.add(salvaButton);

        rootPanel.add(table1, BorderLayout.NORTH);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void popolaTabella() {
        DefaultTableModel tm = (DefaultTableModel) table1.getModel();

        Configuration props = c.getConfiguration();

        Iterator<String> enumeration = props.getKeys();
        while(enumeration.hasNext()){
            String nome = enumeration.next();
            String valore = props.getString(nome, "");

            Vector<String> newRow = new Vector<>();
            newRow.add(nome);
            newRow.add(valore);

            tm.addRow(newRow);
        }
    }
}