package it.vin.dev.menzione.frame;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.text.MaskFormatter;
import it.vin.dev.menzione.logica.*;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.awt.Color;
import java.awt.Frame;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JFormattedTextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class OLDMainFrame2 extends JFrame implements TableModelListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private ViaggiJTable viaggiSudTable;
	private static DbUtil dbu;
	private static ViaggiTableModel nordTableModel;
	private static ViaggiTableModel sudTableModel;
	private JTable clientiTable;
	private ViaggiJTable viaggiNordTable;
	private JScrollPane sudTableScrollPane;
	private JScrollPane nordTableScrollPane;
	private JScrollPane clientiTableScrollPane;
	private JFormattedTextField formattedTextField;
	private JComboBox<String> camionCombo;
	private Logger logger;
	public static Vector<Camion> camions;

	/**
	 * Create the frame.
	 * @throws PropertyVetoException 
	 */
	public OLDMainFrame2() throws PropertyVetoException {

		setTitle("ms");
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		logger = Logger.getGlobal();

		/*JButton salvaButton = new JButton("Salva");
		salvaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				(new UpdateWorker(modifiche, dbu)).run();
			}
		});
		salvaButton.setBounds(1255, 671, 89, 23);
		contentPane.add(salvaButton);
		 */
		
		clientiTableScrollPane = new JScrollPane();
		clientiTableScrollPane.setBounds(24, 109, 400, 280);
		contentPane.add(clientiTableScrollPane);

		clientiTable = new JTable();
		clientiTableScrollPane.setViewportView(clientiTable);

		viaggiNordTable = new ViaggiJTable();
		viaggiNordTable.setCellSelectionEnabled(true);
		viaggiNordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nordTableModel = new ViaggiTableModel(Viaggio.NORD);
		//viaggiNordTable.setBounds(469, 103, 400, 280);
		viaggiNordTable.setModel(nordTableModel);
		viaggiNordTable.getModel().addTableModelListener(this);

		viaggiSudTable = new ViaggiJTable();
		sudTableModel = new ViaggiTableModel(Viaggio.SUD);
		//viaggiSudTable.setBounds(944, 103, 400, 280);
		viaggiSudTable.setModel(sudTableModel);
		viaggiSudTable.getModel().addTableModelListener(this);

		nordTableScrollPane = new JScrollPane();
		nordTableScrollPane.setViewportView(viaggiNordTable);
		nordTableScrollPane.setBounds(469, 103, 400, 280);
		contentPane.add(nordTableScrollPane);

		sudTableScrollPane = new JScrollPane();
		sudTableScrollPane.setViewportView(viaggiSudTable);
		sudTableScrollPane.setBounds(944, 103, 400, 280);
		contentPane.add(sudTableScrollPane);


		formattedTextField = new JFormattedTextField();

		try {
			MaskFormatter dateMask = new MaskFormatter("##/##/####");
			dateMask.install(formattedTextField);
		} catch (ParseException e1) {
			logger.log(Level.SEVERE, e1.getMessage(), e1);
			e1.printStackTrace();
		}
		formattedTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
 				String text;
				int code = e.getKeyCode();
				Date d = null;

				if(code == KeyEvent.VK_ENTER){
					text = formattedTextField.getText();
					try{
						d = checkAndCreateDate(text);
					}catch(NumberFormatException ex){
						e.getComponent().setBackground(Color.RED);
					}

					if(d != null){
						//System.out.println(checkAndCreateDate(text).toString());
						reloadTableModel(d);
					}else{
						e.getComponent().setBackground(Color.RED);
					}
				}else{
					if(e.getComponent().getBackground() == Color.RED){
						e.getComponent().setBackground(Color.WHITE);
					}
				}
			}
			public void keyReleased (KeyEvent arg0) {}
			public void keyTyped (KeyEvent arg0) {}
		});
		formattedTextField.setBounds(10, 11, 122, 20);
		contentPane.add(formattedTextField);

		JButton cercaButton = new JButton("Cerca");
		cercaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = formattedTextField.getText();
				Date d = null;
				try{
					d = checkAndCreateDate(text);
				}catch(NumberFormatException ex){
					formattedTextField.setBackground(Color.RED);
				}

				if(d != null){
					//System.out.println(checkAndCreateDate(text).toString());
					reloadTableModel(d);
				}else{
					formattedTextField.setBackground(Color.RED);
				}
			}
		});
		cercaButton.setBounds(142, 10, 89, 23);
		contentPane.add(cercaButton);

		JButton nordAddButton = new JButton("+");
		nordAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				nordTableModel.addRow(null);
			}
		});
		nordAddButton.setBounds(820, 78, 50, 25);
		contentPane.add(nordAddButton);

		JButton sudAddButton = new JButton("+");
		sudAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sudTableModel.addRow(null);
			}
		});
		sudAddButton.setBounds(1294, 78, 50, 25);
		contentPane.add(sudAddButton);
		
		JButton nordRemoveButton = new JButton("-");
		nordRemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int selected = viaggiNordTable.getSelectedRow();
				if(viaggiNordTable.isEditing()){
					viaggiNordTable.getCellEditor().cancelCellEditing();
				}
				Viaggio rimosso = nordTableModel.removeRow(selected);
				try {
					dbu.rimuoviViaggio(rimosso);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		nordRemoveButton.setBounds(760, 78, 50, 25);
		contentPane.add(nordRemoveButton);
		
		JButton sudRemoveButton = new JButton("-");
		sudRemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int selected = viaggiSudTable.getSelectedRow();
				if(viaggiSudTable.isEditing()){
					viaggiSudTable.getCellEditor().cancelCellEditing();
				}
				Viaggio rimosso = sudTableModel.removeRow(selected);
				try {
					dbu.rimuoviViaggio(rimosso);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		sudRemoveButton.setBounds(1236, 78, 50, 25);
		contentPane.add(sudRemoveButton);
		viaggiNordTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		JButton btnGestisciCamion = new JButton("Gestisci Camion");
		btnGestisciCamion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//JFrame aggiungiCamionFrame = new AggiungiCamionFrame();
				//aggiungiCamionFrame.setVisible(true);
			}
		});
		btnGestisciCamion.setBounds(469, 10, 129, 23);
		contentPane.add(btnGestisciCamion);
		
		JButton btnAggiungiGiornata = new JButton("Aggiungi Giornata");
		btnAggiungiGiornata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFrame inserisciGiornataFrame = new AggiungiDataFrame( null);
				inserisciGiornataFrame.setVisible(true);
			}
		});
		btnAggiungiGiornata.setBounds(624, 10, 129, 23);
		contentPane.add(btnAggiungiGiornata);

		Connection conn;
		Date lastDate = Date.valueOf("1999-10-10");
		try {
			conn = DbUtil.createConnection();
			dbu = DbUtil.dbUtilFactory(conn);
			updateCamionList();
			lastDate = dbu.getDataAggiornamento();

		} catch (SQLException e) {
			e.printStackTrace();
			
			JOptionPane.showMessageDialog(this, "Errore di connessione al server, "
					+ "\n contattare l'amministratore",
					"ERRORE", JOptionPane.ERROR_MESSAGE, null);
			
			System.exit(1);
		}

		formattaTabelleViaggi();

		reloadTableModel(lastDate);
	}


	public static void updateCamionList() throws SQLException {
		
		camions = dbu.getCamion();
		
	}


	private Date checkAndCreateDate(String s) throws NumberFormatException{
		String[] tmp;
		boolean giornoOK = false, meseOK = false, annoOK = false; 
		int giornoTMP = -1, meseTMP = -1, annoTMP = -1;
		Date result = null;

		tmp = s.split("/");
		giornoTMP = Integer.parseInt(tmp[0]);
		meseTMP = Integer.parseInt(tmp[1]);
		annoTMP = Integer.parseInt(tmp[2]);

		if(giornoTMP > 0 && giornoTMP <= 31) giornoOK = true;
		if(meseTMP > 0 && meseTMP <=12) meseOK = true;
		if(annoTMP > 1990) annoOK = true;

		if(giornoOK && meseOK && annoOK){
			result = Date.valueOf(""+annoTMP+"-"+meseTMP+"-"+giornoTMP);
		} else throw new NumberFormatException();

		return result;
	}

	public static void reloadTableModel(Date d) {
		Vector<Viaggio> nord = null;
		Vector<Viaggio> sud = null;

		try {
			nord = dbu.getViaggiBy(Viaggio.NORD, d);
			sud = dbu.getViaggiBy(Viaggio.SUD, d);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		sudTableModel.setData(sud);
		sudTableModel.setCurrentDate(d);
		nordTableModel.setData(nord);
		nordTableModel.setCurrentDate(d);

	}
	


	@Override
	public void tableChanged(TableModelEvent e) {

		int col = e.getColumn();
		int row = e.getFirstRow();

		if(col>=0 || e.getType() > 0){
			ViaggiTableModel tm = (ViaggiTableModel) e.getSource();
			Viaggio v;
			if(e.getType() == TableModelEvent.UPDATE){
				logger.info("RIGA MODIFICATA: "+row+"   COLONNA MODIFICATA: "+col);
				v = tm.getElementAt(row);
				if(col == 0){
					/*if(tm.getType() == Viaggio.NORD && sudTableModel.existsCamion(v.getCamion()) > 0)
						throw new IllegalArgumentException("Esiste gia questo camion in questa data");
					else if(tm.getType() == Viaggio.SUD && nordTableModel.existsCamion(v.getCamion()) > 0)
						throw new IllegalArgumentException("Esiste gia questo camion in questa data");*/
				}
				logger.info(v.toString());
				(new UpdateWorker2(v, col, dbu)).execute();
			}else if(e.getType() == TableModelEvent.INSERT){
				if(tm.getType() == Viaggio.NORD){
					viaggiNordTable.requestFocus();
					viaggiNordTable.changeSelection(row-1, 0, false, false);
					viaggiNordTable.editCellAt(row-1, 0);
				}else {
					viaggiSudTable.changeSelection(row-1, 0, false, false);
					viaggiSudTable.editCellAt(row-1, 0);
					viaggiSudTable.requestFocus();
				}
				logger.info("ADD ROW!");
				v = tm.getElementAt(row-1);
				//logger.info(modifiche.toString());
			}else if(e.getType() == TableModelEvent.DELETE){
				logger.info("ROW DELETED!");
			}
		}
	}

	public static Camion findCamionByTarga(String targa) throws IllegalArgumentException{
		for(Camion c : camions){
			if(c.getTarga().compareTo(targa) == 0)
				return c;
		}
		throw new IllegalArgumentException("Impossibile trovare un camion con targa " + targa + ".");
	}
	
	private void formattaTabelleViaggi(){
		
		camionCombo = new JComboBox<>();
		for(Camion c : camions){
			camionCombo.addItem(c.getTarga());
		}
		
		TableColumn col;
		for(int i = 0; i < 5; i++){
		if(i==0){
			col = viaggiNordTable.getColumnModel().getColumn(i);
			col.setCellEditor(new DefaultCellEditor(camionCombo));
			col.setPreferredWidth(100);
		}else if(i == 3){
				col = viaggiNordTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(200);
			}else if(i == 4){
				col = viaggiNordTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(20);
			}
		}
		for(int i = 0; i < 5; i++){
			if(i==0){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setCellEditor(new DefaultCellEditor(camionCombo));
				col.setPreferredWidth(100);
			}else if(i == 3){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(200);
			}else if(i == 4){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(20);
			}
		}

	}
}