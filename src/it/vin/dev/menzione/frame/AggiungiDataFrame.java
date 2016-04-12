package it.vin.dev.menzione.frame;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.text.MaskFormatter;

import it.vin.dev.menzione.logica.Camion;
import it.vin.dev.menzione.logica.Configuration;
import it.vin.dev.menzione.logica.DbUtil;
import it.vin.dev.menzione.logica.DbUtilFactory;
import it.vin.dev.menzione.logica.Nota;
import it.vin.dev.menzione.logica.Ordine;
import it.vin.dev.menzione.logica.Viaggio;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import java.awt.Robot;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import javax.swing.ListSelectionModel;

public class AggiungiDataFrame extends JFrame implements TableModelListener {

	private static final long serialVersionUID = 7870598458313964074L;
	private JPanel contentPane;
	private JTable viaggiNordTable;
	private JTable viaggiSudTable;
	private ViaggiNuoviTableModel nordTM;
	private ViaggiNuoviTableModel sudTM;
	private Date lastDate;
	private DbUtil dbu;
	private JComboBox<String> camionComboBox;
	private Logger logger;
	private MainFrame source;
	private Nota fermiToDB;
	private Nota nonAssToDB;
	private Vector<Ordine> ordiniToDB;
	private Vector<Viaggio> toDB;

	/**
	 * Create the frame.
	 */
	public AggiungiDataFrame(MainFrame source) {

		try {
			dbu = DbUtilFactory.createDbUtil();
		} catch (SQLException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}

		this.source = source;
		this.toDB = new Vector<>();
		logger = Logger.getGlobal();
		try {
			FileHandler fh = new FileHandler(Configuration.logfile+"-AggiungiDataFrame.log", true);
			fh.setLevel(Level.SEVERE);
			logger.addHandler(fh);

		} catch (SecurityException | IOException e2) {
			e2.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1200, 487);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);


		JLabel lblInserisciData = new JLabel("Inserisci data:");
		panel.add(lblInserisciData);

		JFormattedTextField frmtdtxtfldData = new JFormattedTextField();
		frmtdtxtfldData.setColumns(10);
		frmtdtxtfldData.setFocusTraversalKeysEnabled(false);

		try {
			MaskFormatter dateMask = new MaskFormatter("##/##/####");
			dateMask.install(frmtdtxtfldData);
		} catch (ParseException e1) {
			logger.log(Level.SEVERE, e1.getMessage(), e1);
			e1.printStackTrace();
		}
		frmtdtxtfldData.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				String text;
				int code = e.getKeyCode();
				Date d = null;

				if(code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB){
					text = frmtdtxtfldData.getText();
					try{
						d = checkAndCreateDate(text);
						viaggiNordTable.requestFocusInWindow();
						viaggiNordTable.editCellAt(0, 0);
					}catch(NumberFormatException ex){
						e.getComponent().setBackground(Color.RED);
					}

					if(d != null){

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

		panel.add(frmtdtxtfldData);

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.CENTER);

		nordTM = new ViaggiNuoviTableModel(Viaggio.NORD);
		sudTM = new ViaggiNuoviTableModel(Viaggio.SUD);

		JPanel panel_4 = new JPanel();
		panel_2.add(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_6.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		flowLayout.setAlignment(FlowLayout.TRAILING);
		panel_4.add(panel_6, BorderLayout.NORTH);

		JButton nordAddButton = new JButton("+");
		nordAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViaggiNuoviTableModel tm = (ViaggiNuoviTableModel) viaggiNordTable.getModel();
				tm.addRow(null);
			}
		});

		JButton NordRemoveButton = new JButton("-");
		NordRemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViaggiNuoviTableModel tm = (ViaggiNuoviTableModel) viaggiNordTable.getModel();
				int row = viaggiNordTable.getSelectedRow();
				tm.removeRow(row);
			}
		});
		panel_6.add(NordRemoveButton);
		panel_6.add(nordAddButton);

		viaggiNordTable = new JTable(nordTM);
		viaggiNordTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		viaggiNordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		viaggiNordTable.setCellSelectionEnabled(true);
		panel_2.add(viaggiNordTable);

		JScrollPane scrollPane = new JScrollPane(viaggiNordTable);
		scrollPane.setPreferredSize(new Dimension(500, 300));
		panel_4.add(scrollPane);

		JPanel panel_1 = new JPanel();
		panel_2.add(panel_1);
		panel_1.setLayout(new GridLayout(5, 1, 10, 10));

		JButton button = new JButton(">");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = viaggiNordTable.getSelectedRow();
				if(viaggiNordTable.isEditing()){
					viaggiNordTable.getCellEditor().cancelCellEditing();
				}
				if(selectedRow>=0){
					Viaggio tmp = nordTM.removeRow(selectedRow);
					tmp.setPosizione(Viaggio.SUD);
					sudTM.addRow(tmp);
				}
			}
		});
		panel_1.add(button);

		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3);

		JButton button_1 = new JButton("<");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = viaggiSudTable.getSelectedRow();
				if(viaggiSudTable.isEditing()){
					viaggiSudTable.getCellEditor().cancelCellEditing();
				}
				if(selectedRow>=0){
					Viaggio tmp = sudTM.removeRow(selectedRow);
					tmp.setPosizione(Viaggio.NORD);
					nordTM.addRow(tmp);
				}
			}
		});
		panel_1.add(button_1);

		JPanel panel_5 = new JPanel();
		panel_2.add(panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));

		JPanel panel_7 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_7.getLayout();
		flowLayout_1.setVgap(0);
		flowLayout_1.setAlignment(FlowLayout.TRAILING);
		flowLayout_1.setHgap(0);
		panel_5.add(panel_7, BorderLayout.NORTH);

		JButton SudAddButton = new JButton("+");
		SudAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViaggiNuoviTableModel tm = (ViaggiNuoviTableModel) viaggiSudTable.getModel();
				tm.addRow(null);
			}
		});

		JButton SudRemoveButton = new JButton("-");
		SudRemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViaggiNuoviTableModel tm = (ViaggiNuoviTableModel) viaggiSudTable.getModel();
				int row = viaggiSudTable.getSelectedRow();
				tm.removeRow(row);
			}
		});
		panel_7.add(SudRemoveButton);
		panel_7.add(SudAddButton);
		viaggiSudTable = new JTable(sudTM);
		viaggiSudTable.setCellSelectionEnabled(true);
		viaggiSudTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane_1 = new JScrollPane(viaggiSudTable);
		scrollPane_1.setPreferredSize(new Dimension(500, 300));
		panel_5.add(scrollPane_1);

		JPanel panel_8 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_8.getLayout();
		flowLayout_2.setAlignment(FlowLayout.TRAILING);
		contentPane.add(panel_8, BorderLayout.SOUTH);

		JButton SalvaBtn = new JButton("Salva");
		SalvaBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				ViaggiNuoviTableModel tmSud = (ViaggiNuoviTableModel) viaggiSudTable.getModel();
				ViaggiNuoviTableModel tmNord = (ViaggiNuoviTableModel) viaggiNordTable.getModel();
				boolean nordOk = false;
				boolean sudOK = false;
				JButton source = (JButton)arg0.getSource();
				JFrame buttonRoot = (JFrame) SwingUtilities.getRoot(source);
				Date newLastDate = null;
				try{
					newLastDate = checkAndCreateDate(frmtdtxtfldData.getText());
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(buttonRoot, 
							"Data inserita non corretta", 
							"Attenzione", 
							JOptionPane.WARNING_MESSAGE);
				}

				if(newLastDate != null){
					try{
						int giaInToDB = toDB.size();
						boolean giaInToDBOK = false;
						if(newLastDate.after(lastDate)){
							Vector<Viaggio> nordViaggi = tmNord.getData();
							Vector<Viaggio> sudViaggi = tmSud.getData();
							//Vector<Viaggio> toDB = new Vector<>();
							int i = 0;
							//int occorrenzeInNord = 0;
							//int occorrenzeInSud = 0;
							for(Viaggio v : toDB){
								if(v.getData() == null){
									v.setData(newLastDate);
									i++;
								}
							}
							if(i == giaInToDB){
								giaInToDBOK = true;
								i = 0;
							}
							for(Viaggio v : nordViaggi){
								/*Camion c = v.getCamion();
							occorrenzeInNord = tmNord.existsCamion(c);
							occorrenzeInSud = tmSud.existsCamion(c);
							boolean hasAutista = false;
							if(v.getAutista().compareTo("") == 0){
								JOptionPane.showMessageDialog(buttonRoot, 
										"Non è specificato autista per viaggio con targa " + c.getTarga()+".",  
										"Attenzione", 
										JOptionPane.WARNING_MESSAGE);
								break;
							}else{
								hasAutista = true;
							}*/

								v.setData(newLastDate);
								v.setPosizione(Viaggio.NORD);
								toDB.add(v);
								i++;
							}

							if(i == nordViaggi.size()){
								nordOk = true;
								i = 0;
							}

							if(nordOk == true){
								for(Viaggio v : sudViaggi){
									/*Camion c = v.getCamion();
								occorrenzeInNord = tmNord.existsCamion(c);
								occorrenzeInSud = tmSud.existsCamion(c);
								boolean hasAutista = false;
								if(v.getAutista().compareTo("") == 0){
									JOptionPane.showMessageDialog(buttonRoot, 
											"Non è specificato autista per viaggio con targa " + c.getTarga()+".",  
											"Attenzione", 
											JOptionPane.WARNING_MESSAGE);
									break;
								}else{
									hasAutista = true;
								}*/

									v.setData(newLastDate);
									v.setPosizione(Viaggio.SUD);
									toDB.addElement(v);
									i++;

								}

								if(i == sudViaggi.size()){
									sudOK = true;
								}

								int sum = nordTM.getData().size() + sudTM.getData().size();

								//Aggiungo la data agli ordini
								for(Ordine o : ordiniToDB){
									o.setDate(newLastDate);
								}
								boolean ordiniOK = false;
								int ordOK = 0;
								for(Ordine o : ordiniToDB){
									System.out.println(o.getDate());
									if(o.getDate() != null)
										ordOK++;
								}
								if(ordOK == ordiniToDB.size()) ordiniOK = true;


								//Aggiungo la data alla nota fermi
								fermiToDB.setData(newLastDate);

								//Aggiungo la data alla nota nonAss
								nonAssToDB.setData(newLastDate);

								boolean noteOK = false;
								if(fermiToDB.getData() != null && nonAssToDB.getData() != null) noteOK = true;

								if(giaInToDBOK && noteOK && ordiniOK && sudOK && nordOk && (toDB.size() - giaInToDB) == sum){
									creaData(toDB, newLastDate);
								}else{
									JOptionPane.showMessageDialog(buttonRoot, 
											"Impossibile creare la data. Controllare i dati",  
											"Attenzione", 
											JOptionPane.WARNING_MESSAGE);
								}
							}


						}else{
							JOptionPane.showMessageDialog(buttonRoot, 
									"Data inserita precedente all'ultima presente nel database", 
									"Attenzione", 
									JOptionPane.WARNING_MESSAGE);
						}
					}catch(Exception e){
						JOptionPane.showMessageDialog(buttonRoot, 
								"Impossibile creare la data.\nCausa: " + e.getMessage(),  
								"Attenzione", 
								JOptionPane.WARNING_MESSAGE);
					}

				}

			}

		});
		panel_8.add(SalvaBtn);

		viaggiNordTable.getModel().addTableModelListener(this);
		viaggiSudTable.getModel().addTableModelListener(this);

		try {
			lastDate = dbu.getDataAggiornamento();
			createDatiToDB(lastDate);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.log(Level.SEVERE, e1.getMessage(), e1);
			JOptionPane.showMessageDialog(this, 
					"Errore di connessione al database" +
							"Codice errore:"+e1.getErrorCode()+"\n"+e1.getMessage(),  
							"Attenzione", 
							JOptionPane.ERROR_MESSAGE);

		}

		formattaTabelle();
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

	private void createDatiToDB(Date lastDate) throws SQLException{
		//Creo i viaggi
		Vector<Viaggio> nordViaggiTMP = dbu.getViaggiBy(Viaggio.NORD, lastDate);
		Vector<Viaggio> sudViaggiTMP = dbu.getViaggiBy(Viaggio.SUD, lastDate);

		Vector<Viaggio> nordViaggi = new Vector<>();
		Vector<Viaggio> sudViaggi = new Vector<>();


		for(Viaggio v : nordViaggiTMP){
			if(v.isSelezionato()){
				Viaggio vv = new Viaggio();
				vv.setCamion(MainFrame.findCamionByTarga(v.getCamion().getTarga()));
				vv.setPosizione(Viaggio.SUD);
				vv.setAutista(v.getAutista());
				sudViaggi.addElement(vv);
			}else{
				Viaggio vv = new Viaggio();
				vv.setCamion(MainFrame.findCamionByTarga(v.getCamion().getTarga()));
				vv.setAutista(v.getAutista());
				vv.setPosizione(Viaggio.NORD);
				vv.setData(null);
				toDB.addElement(vv);
			}
		}

		for(Viaggio v : sudViaggiTMP){
			if(v.isSelezionato()){
				Viaggio vv = new Viaggio();
				vv.setCamion(MainFrame.findCamionByTarga(v.getCamion().getTarga()));
				vv.setPosizione(Viaggio.NORD);
				vv.setAutista(v.getAutista());
				nordViaggi.addElement(vv);
			}else{
				Viaggio vv = new Viaggio();
				vv.setCamion(MainFrame.findCamionByTarga(v.getCamion().getTarga()));
				vv.setAutista(v.getAutista());
				vv.setPosizione(Viaggio.SUD);
				vv.setData(null);
				toDB.addElement(vv);
			}
		}

		sudTM.setData(sudViaggi);
		nordTM.setData(nordViaggi);

		//Creo gli ordini
		Vector<Ordine> ordiniTMP = dbu.getOrdiniByDate(lastDate);
		ordiniToDB = new Vector<>();

		for(Ordine o : ordiniTMP){
			Ordine oo;
			if(o.getSelezionato()){
				oo = new Ordine(o.getCliente(), null, o.getType());
			}else {
				oo = new Ordine(o.getData(), o.getCliente(), o.getNote(), null);
				oo.setType(o.getType());
			}
			ordiniToDB.add(oo);
		}

		//creo la nota fermi
		Nota fermiTMP = dbu.getFermiByDate(lastDate);
		fermiTMP.setData(null);
		fermiToDB = new Nota(null, fermiTMP.getTesto(), fermiTMP.getTipo());

		//creo la nota nonAss
		Nota nonAssTMP = dbu.getNonAssByDate(lastDate);
		nonAssTMP.setData(null);
		nonAssToDB = new Nota(null, nonAssTMP.getTesto(), nonAssTMP.getTipo());
	}



	private void formattaTabelle(){

		camionComboBox = new JComboBox<>();
		camionComboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					try {

						Robot r = new Robot();
						for(int i = 0; i<2; i++){
							r.keyPress(KeyEvent.VK_TAB);
						}
					} catch (AWTException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						logger.log(Level.SEVERE, e1.getMessage(), e1);
					}
				}
			}
		});


		for(Camion c : MainFrame.camions){
			camionComboBox.addItem(c.getTarga());
		}

		for(int i=0;i<3;i++){
			if(i==0){
				DefaultCellEditor cellEdt = new DefaultCellEditor(camionComboBox);
				cellEdt.setClickCountToStart(1);
				viaggiNordTable.getColumnModel().getColumn(i).setCellEditor(cellEdt);
				viaggiSudTable.getColumnModel().getColumn(i).setCellEditor(cellEdt);
			}
		}


		TableColumn col;
		for(int i = 0; i < 4; i++){
			if(i==0){
				col = viaggiNordTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(70);
				col.setMinWidth(70);
				col.setMaxWidth(70);
			}else if(i == 1){
				col = viaggiNordTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(90);
				col.setMinWidth(90);
				col.setMaxWidth(100);
			}else if(i == 2){
				col = viaggiNordTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(100);
				col.setMinWidth(110);
				col.setMaxWidth(120);
			}
		}

		for(int i = 0; i < 5; i++){
			if(i==0){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(70);
				col.setMinWidth(70);
				col.setMaxWidth(70);
			}else if(i == 1){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(90);
				col.setMinWidth(90);
				col.setMaxWidth(100);
			}else if(i == 2){
				col = viaggiNordTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(100);
				col.setMinWidth(110);
				col.setMaxWidth(120);
			}else if(i == 4){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(25);
				col.setMinWidth(30);
				col.setMaxWidth(30);
			}
		}


	}

	@Override
	public void tableChanged(TableModelEvent e) {
		int col = e.getColumn();
		int row = e.getFirstRow();

		ViaggiNuoviTableModel tm = (ViaggiNuoviTableModel) e.getSource();
		if(e.getType() == TableModelEvent.UPDATE){
			//Viaggio v = tm.getElementAt(row);
			if(tm.getType() == Viaggio.NORD){
				if(col==1){
					if(tm.getType().compareTo(Viaggio.NORD) == 0){
						Camion c = tm.getElementAt(row).getCamion();
						int i = 0;
						if((i = sudTM.existsCamion(c)) > 0){
							sudTM.replaceCaratt(c.getTarga(), c.getCaratteristiche());
						}
					}else if(tm.getType().compareTo(Viaggio.SUD) == 0){
						Camion c = tm.getElementAt(row).getCamion();
						int i = 0;
						if((i = nordTM.existsCamion(c)) > 0){
							nordTM.replaceCaratt(c.getTarga(), c.getCaratteristiche());
						}
					}
				}
			}
		}else if(e.getType() == TableModelEvent.INSERT){
			if(tm.getType() == Viaggio.NORD){
				viaggiNordTable.requestFocus();
				viaggiNordTable.changeSelection(row-1, 0, false, false);
				String t = tm.getElementAt(viaggiNordTable.getSelectedRow()).getCamion().getTarga();
				if(t.compareTo("") == 0){
					viaggiNordTable.editCellAt(row-1, 0);
				}
			}else {
				viaggiSudTable.requestFocus();
				viaggiSudTable.changeSelection(row-1, 0, false, false);
				String t = tm.getElementAt(viaggiSudTable.getSelectedRow()).getCamion().getTarga();
				if(t.compareTo("") == 0){
					viaggiSudTable.editCellAt(row-1, 0);
				}
			}
		}
	}

	private void creaData(Vector<Viaggio> toDB, Date newLastDate) {
		try{
			/*dbu.setDataAggiornamento(newLastDate);
			dbu.aggiungiNota(fermiToDB);
			dbu.aggiungiNota(nonAssToDB);
			dbu.aggiungiViaggio(toDB);
			dbu.aggiungiOrdine(ordiniToDB);*/

			boolean result = dbu.aggiungiGiornata(newLastDate, toDB, ordiniToDB, fermiToDB, nonAssToDB);

			if(result){
				JOptionPane.showMessageDialog(this, 
						"Data aggiunta correttamente!",  
						"Successo", 
						JOptionPane.INFORMATION_MESSAGE);
				source.reloadTableModel(newLastDate, MainFrame.RELOAD_RESETCONNECTION);
				source.reloadOrdiniModel(newLastDate);
				source.reloadNote(newLastDate);
				dbu.closeConnection();
				dispose();
			}else if(!result){


				JOptionPane.showMessageDialog(this, 
						"Errore nella query al database. I dati non sono stati modificati",  
						"ATTENZIONE", 
						JOptionPane.ERROR_MESSAGE);
				dbu.closeConnection();

			}
			
		}catch(SQLException e){
			logger.log(Level.SEVERE, e.getMessage(), e);
			JOptionPane.showMessageDialog(this, 
					"Errore di connessione al database\n"
							+ "Codice errore:"+e.getErrorCode()+"\n"+e.getMessage(),
							"Attenzione", JOptionPane.ERROR_MESSAGE);

		}

	}

}
