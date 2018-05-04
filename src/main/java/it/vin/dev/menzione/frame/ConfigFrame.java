package it.vin.dev.menzione.frame;

import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.Msg;
import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.logica.Configuration;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by Vincenzo on 07/06/2016.
 */
public class ConfigFrame extends JFrame{
    private JTable table1;
    private JButton annullaButton;
    private JButton salvaButton;
    private JPanel rootPanel;

    private Configuration c;

    public static void open(int closeOption) {
        JFrame configFrame = new ConfigFrame();
        configFrame.setDefaultCloseOperation(closeOption);
        configFrame.setVisible(true);
    }

    public ConfigFrame() {
        rootPanel = new JPanel(new BorderLayout());
        createUIComponents();

        setContentPane(rootPanel);

        setTitle(Consts.PROG_TITLE);
        setIconImage(Toolkit.getDefaultToolkit().createImage(ViaggiUtils.getMainIcon()));

        c = Configuration.getInstance();
        popolaTabella();

        annullaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(ConfigFrame.this.getDefaultCloseOperation() == EXIT_ON_CLOSE) {
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
                    c.setConfiguration(vector.get(0), vector.get(1));
                }

                try {
                    c.salvaProps();
                } catch (IOException e1) {
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

        Properties props = c.getProps();

        Enumeration<?> enumeration = props.propertyNames();
        while(enumeration.hasMoreElements()){
            String nome = enumeration.nextElement().toString();
            String valore = props.getProperty(nome);

            Vector<String> newRow = new Vector<>();
            newRow.add(nome);
            newRow.add(valore);

            tm.addRow(newRow);
        }
    }
}