package it.vin.dev.menzione.frame;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import it.vin.dev.menzione.Msg;
import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.events.CamionInsertEvent;
import it.vin.dev.menzione.events.CamionRemoveEvent;
import it.vin.dev.menzione.events.CamionUpdateEvent;
import it.vin.dev.menzione.events.ViaggiEventsBus;
import it.vin.dev.menzione.logica.Camion;
import it.vin.dev.menzione.logica.DatabaseService;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.border.TitledBorder;

import com.mysql.jdbc.MysqlDataTruncation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class AggiungiCamionFrame extends JFrame implements WindowListener {

    private JPanel contentPane;
    private JTextField txtFieldTarga;
    private JTextField txtFieldCaratt;
    private DatabaseService dbu;
    private Vector<Camion> camions;
    private JComboBox<Camion> camionComboBox;
    private JTextArea resultLabel;
    private JTextField txtCaratt;
    private Logger logger;

    private final ActionListener addCamionListener = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            resultLabel.setText("");
            String targa = txtFieldTarga.getText();
            String caratt = txtFieldCaratt.getText();
            targa = targa.toUpperCase();
            caratt = caratt.toUpperCase();

            if(!targa.trim().isEmpty() && !caratt.trim().isEmpty()){
                Camion c = new Camion(targa, caratt);

                if(!findCamionByTarga(targa)){
                    try {
                        dbu.aggiungiCamion(c);
                        updateCamionList();
                        resultLabel.setText("Aggiunta effettuata con successo");
                        //callback.updateCamionList();
                        ViaggiEventsBus.getInstance().post(new CamionInsertEvent(c));
                    } catch(MysqlDataTruncation e){
                        resultLabel.setText("ATTENZIONE: la targa deve avere 7 caratteri"
                                + " (spazi inclusi)");
                    } catch (SQLException e) {
                        databaseError(e);
                    }
                }else{
                    resultLabel.setText("ATTENZIONE: Esiste già un camion con la stessa targa");
                }
            }else{
                resultLabel.setText("ATTENZIONE: i campi non sono compliati");
            }
        }
    };

    private final ActionListener removeCamionAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Camion camion = (Camion) camionComboBox.getSelectedItem();
            int result = Msg.yesno(null, "Sei sicuro di voler eliminare il camion con targa: " + camion.getTarga()+
                    "?\nQuesta operazione non può essere annullata");
            if (result == JOptionPane.YES_OPTION){
                try {
                    dbu.rimuoviCamion(camion);
                    updateCamionList();
                    //callback.updateCamionList();
                    ViaggiEventsBus.getInstance().post(new CamionRemoveEvent(camion));
                } catch (SQLException e1) {
                    databaseError(e1);
                }
            }
        }
    };

    private final ActionListener saveAction = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            Component b = (Component) arg0.getSource();
            Component root = SwingUtilities.getRoot(b);
            Camion c = (Camion) camionComboBox.getSelectedItem();
            String car = txtCaratt.getText();

            if(c == null) {
                Msg.warn(root, "Selezionare una targa");
                return;
            }

            if(car.trim().isEmpty()) {
                Msg.warn(root, "Il campo caratteristiche non può essere vuoto");
                txtCaratt.setText(c.getCaratteristiche());
                return;
            } else {
                c.setCaratteristiche(txtCaratt.getText());
            }

            try {
                dbu.modificaCamion(c);

                ViaggiEventsBus.getInstance().post(new CamionUpdateEvent(c));

                Msg.info(root, "Modifica salvata con successo");
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
                resultLabel.setText("Errore di collegamento al database.");
            }
        }
    };

    /**
     * Create the frame.
     */
    public AggiungiCamionFrame() {
        logger = LogManager.getLogger(AggiungiCamionFrame.class);
        setIconImage(Toolkit.getDefaultToolkit().createImage(ViaggiUtils.getMainIcon()));

        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 300, 360);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel rootPanel = new JPanel();
        contentPane.add(rootPanel, BorderLayout.CENTER);
        rootPanel.setLayout(new GridLayout(0, 1, 0, 0));

        JPanel aggiungiCamionPanel = new JPanel();
        rootPanel.add(aggiungiCamionPanel);
        aggiungiCamionPanel.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Aggiungi Camion", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        aggiungiCamionPanel.setLayout(new BorderLayout(0, 0));

        JLabel lblInserisciIDati = new JLabel("Inserisci i dati del camion e clicca su Aggiungi");
        lblInserisciIDati.setHorizontalAlignment(SwingConstants.CENTER);
        aggiungiCamionPanel.add(lblInserisciIDati, BorderLayout.NORTH);

        JPanel camionDataPanel = new JPanel();
        aggiungiCamionPanel.add(camionDataPanel, BorderLayout.CENTER);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {117};
        gbl_panel.rowHeights = new int[] {20};
        gbl_panel.columnWeights = new double[]{1.0, 1.0};
        gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0};
        camionDataPanel.setLayout(gbl_panel);

        JLabel lblTarga = new JLabel("Targa");
        GridBagConstraints gbc_lblTarga = new GridBagConstraints();
        gbc_lblTarga.anchor = GridBagConstraints.WEST;
        gbc_lblTarga.insets = new Insets(0, 0, 5, 5);
        gbc_lblTarga.gridx = 0;
        gbc_lblTarga.gridy = 0;
        camionDataPanel.add(lblTarga, gbc_lblTarga);

        txtFieldTarga = new JTextField();
        txtFieldTarga.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                resultLabel.setText("");
            }
        });
        GridBagConstraints gbc_txtFieldTarga = new GridBagConstraints();
        gbc_txtFieldTarga.anchor = GridBagConstraints.WEST;
        gbc_txtFieldTarga.insets = new Insets(0, 0, 5, 0);
        gbc_txtFieldTarga.gridx = 1;
        gbc_txtFieldTarga.gridy = 0;
        camionDataPanel.add(txtFieldTarga, gbc_txtFieldTarga);
        txtFieldTarga.setColumns(20);

        JLabel lblCaratteristiche = new JLabel("Caratteristiche");
        GridBagConstraints gbc_lblCaratteristiche = new GridBagConstraints();
        gbc_lblCaratteristiche.anchor = GridBagConstraints.WEST;
        gbc_lblCaratteristiche.insets = new Insets(0, 0, 5, 5);
        gbc_lblCaratteristiche.gridx = 0;
        gbc_lblCaratteristiche.gridy = 1;
        camionDataPanel.add(lblCaratteristiche, gbc_lblCaratteristiche);

        txtFieldCaratt = new JTextField();
        GridBagConstraints gbc_txtFieldCaratt = new GridBagConstraints();
        gbc_txtFieldCaratt.anchor = GridBagConstraints.WEST;
        gbc_txtFieldCaratt.insets = new Insets(0, 0, 5, 0);
        gbc_txtFieldCaratt.gridx = 1;
        gbc_txtFieldCaratt.gridy = 1;
        camionDataPanel.add(txtFieldCaratt, gbc_txtFieldCaratt);
        txtFieldCaratt.setColumns(20);

        txtFieldCaratt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                resultLabel.setText("");
            }
        });

        JButton btnAggiungi = new JButton("Aggiungi");
        btnAggiungi.addActionListener(addCamionListener);
        GridBagConstraints gbc_btnAggiungi = new GridBagConstraints();
        gbc_btnAggiungi.insets = new Insets(0, 0, 5, 0);
        gbc_btnAggiungi.gridx = 1;
        gbc_btnAggiungi.gridy = 2;
        camionDataPanel.add(btnAggiungi, gbc_btnAggiungi);

        resultLabel = new JTextArea("");
        resultLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        resultLabel.setPreferredSize(new Dimension(4, 50));
        resultLabel.setEditable(false);
        resultLabel.setBackground(UIManager.getColor("Label.background"));
        resultLabel.setAutoscrolls(true);
        aggiungiCamionPanel.add(resultLabel, BorderLayout.SOUTH);
        camionComboBox = new JComboBox<>();
        camionComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                if(arg0.getStateChange() == ItemEvent.SELECTED){
                    Camion c = (Camion) camionComboBox.getSelectedItem();
                    txtCaratt.setText(c.getCaratteristiche());
                }
            }
        });

        JPanel modificaCamionPanel = new JPanel();
        rootPanel.add(modificaCamionPanel);
        modificaCamionPanel.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Modifica Camion", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
//		modificaCamionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        modificaCamionPanel.setLayout(new BoxLayout(modificaCamionPanel, BoxLayout.Y_AXIS));

        JPanel modifyCamionDataPanel = new JPanel();
        modifyCamionDataPanel.setLayout(new GridBagLayout());
        modificaCamionPanel.add(modifyCamionDataPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblSelezionareUnCamion = new JLabel("Targa");
        modifyCamionDataPanel.add(lblSelezionareUnCamion, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 55);
        modifyCamionDataPanel.add(camionComboBox, gbc);

        JLabel lblCaratteristiche_1 = new JLabel("Caratteristiche");
        gbc.gridx = 0;
        gbc.gridy = 1;
        modifyCamionDataPanel.add(lblCaratteristiche_1, gbc);

        txtCaratt = new JTextField();
        txtCaratt.setColumns(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        modifyCamionDataPanel.add(txtCaratt, gbc);

        JButton btnSalva = new JButton("Salva");
        btnSalva.addActionListener(saveAction);
        modificaCamionPanel.add(btnSalva);


        JButton btnRimuovi = new JButton("Rimuovi");
        btnRimuovi.setVisible(true);
        btnRimuovi.setEnabled(true);
        btnRimuovi.addActionListener(removeCamionAction);
        modificaCamionPanel.add(btnRimuovi);

        try {
            dbu = DatabaseService.create();
            updateCamionList();
        } catch (SQLException e) {
            databaseError(e);
        }

		/*camionDataPanel.setBorder(new TitledBorder("camionDataPanel"));
		aggiungiCamionPanel.setBorder(new TitledBorder("aggiungiCamionPanel"));
        modifyCamionDataPanel.setBorder(new TitledBorder("modifyCamionDataPanel"));
        modificaCamionPanel.setBorder(new TitledBorder("modificaCamionPanel"));
        rootPanel.setBorder(new TitledBorder("rootPanel"));*/

        addWindowListener(this);
        pack();
    }

    private void databaseError(SQLException e) {
        logger.error(e.getMessage(), e);
        e.printStackTrace();
        Msg.error(this, "Errore di collegamento col"
                + "database.\nInformazioni sull'errore:\n" +
                "Codice errore: "+e.getErrorCode()+"\n"+e.getMessage());
    }

    private void updateCamionList() throws SQLException{
        camions = dbu.getCamion();
        camionComboBox.removeAllItems();
        for(Camion c : camions){
            camionComboBox.addItem(c);
        }
    }

    private boolean findCamionByTarga(String targa){
        if(camions.size() != 0){
            for(Camion camion : camions){
                if(camion.getTarga().compareToIgnoreCase(targa) == 0){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {
/*		try {
			//TODO: callback.updateCamionList();
		} catch (SQLException e1) {
			e1.printStackTrace();
			logger.error(e1.getMessage(), e1);
		}*/

    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            dbu.closeConnection();
        } catch (SQLException ex) {
            databaseError(ex);
        }
    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

}
