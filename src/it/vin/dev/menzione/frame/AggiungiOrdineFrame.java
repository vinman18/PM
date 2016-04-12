package it.vin.dev.menzione.frame;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import it.vin.dev.menzione.logica.Configuration;
import it.vin.dev.menzione.logica.DbUtil;
import it.vin.dev.menzione.logica.DbUtilFactory;
import it.vin.dev.menzione.logica.Ordine;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;

public class AggiungiOrdineFrame extends JFrame {

	private static final long serialVersionUID = -6622068677087303721L;
	private JPanel contentPane;
	private JTextField txtData;
	private JTextField txtCliente;
	private DbUtil dbu;
	private Logger logger;
	/**
	 * Create the frame.
	 * @throws SQLException 
	 */
	public AggiungiOrdineFrame(MainFrame source, String type, Date dataOrdine) throws SQLException {
		logger = Logger.getGlobal();
		try {
			FileHandler fh = new FileHandler(Configuration.logfile+"-AggiungiOrdineFrame.log", true);
			fh.setLevel(Level.SEVERE);
			logger.addHandler(fh);
			
		} catch (SecurityException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		setResizable(false);
		setTitle("Inserisci ordine");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 288, 265);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		this.dbu = DbUtilFactory.createDbUtil();
		JPanel northPanel = new JPanel();
		contentPane.add(northPanel, BorderLayout.NORTH);
		
		JLabel lblInserisciIDati = new JLabel("Inserisci i dati dell'ordine");
		northPanel.add(lblInserisciIDati);
		
		JPanel centerPanel = new JPanel();
		contentPane.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(null);
		
		JLabel lblData = new JLabel("Data:");
		lblData.setBounds(10, 45, 27, 14);
		centerPanel.add(lblData);
		
		txtData = new JTextField();
		txtData.setBounds(70, 42, 191, 20);
		centerPanel.add(txtData);
		txtData.setColumns(10);
		
		JLabel lblCliente = new JLabel("Cliente:");
		lblCliente.setBounds(10, 93, 37, 14);
		centerPanel.add(lblCliente);
		
		txtCliente = new JTextField();
		txtCliente.setBounds(70, 90, 191, 20);
		centerPanel.add(txtCliente);
		txtCliente.setColumns(10);
		
		JLabel lblNote = new JLabel("Note:");
		lblNote.setBounds(10, 138, 50, 20);
		centerPanel.add(lblNote);
		
		JTextField txtrNote = new JTextField();
		txtrNote.setBounds(70, 138, 191, 20);
		centerPanel.add(txtrNote);
		txtrNote.setColumns(10);
		
		JLabel lblLoridneSarInserito = new JLabel("L'ordine sar\u00E0 inserito in data: ");
		lblLoridneSarInserito.setBounds(29, 11, 147, 14);
		centerPanel.add(lblLoridneSarInserito);
		
		JLabel lblDate = new JLabel("");
		String[] s = dataOrdine.toString().split("-");
		
		lblDate.setText(""+s[2]+"-"+s[1]+"-"+s[0]);
		
		lblDate.setBounds(170, 11, 91, 14);
		centerPanel.add(lblDate);
		
		JPanel southPanel = new JPanel();
		contentPane.add(southPanel, BorderLayout.SOUTH);
		
		JButton btnSalva = new JButton("Salva");
		btnSalva.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JButton b = (JButton) arg0.getSource();
				Component root = SwingUtilities.getRoot(b);
				String data = txtData.getText();
				String cliente = txtCliente.getText();
				String note = txtrNote.getText();
				Ordine o = new Ordine(data, cliente, note, dataOrdine);
				o.setType(type);

				//OrdiniTableModel tm = (OrdiniTableModel) ordineTable.getModel();
				//tm.addRow(o);
				try {
					dbu.aggiungiOrdine(o);
				} catch (SQLException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					JOptionPane.showMessageDialog(root, "Errore di connessione al database"
							+ "\nCodice errore:"+e.getErrorCode()+"\n"+e.getMessage(),
							"ERRORE", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				source.reloadOrdiniModel(dataOrdine);
				dispose();
			}
		});
		southPanel.add(btnSalva);
	}
}
