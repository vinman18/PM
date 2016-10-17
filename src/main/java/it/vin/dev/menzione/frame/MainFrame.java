package it.vin.dev.menzione.frame;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.text.MaskFormatter;

import it.vin.dev.menzione.MainFrame.CustomDateTextField;
import it.vin.dev.menzione.logica.*;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JFormattedTextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.event.WindowStateListener;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class MainFrame extends JFrame implements TableModelListener {

	private static final long serialVersionUID = 6785301407122344285L;
	private Date lastDate;
	private JPanel contentPane;
	private ViaggiJTable viaggiSudTable;
	private static DbUtil dbu;
	private static ViaggiTableModel nordTableModel;
	private static ViaggiTableModel sudTableModel;
	private OrdiniTableModel ordiniSalitaTableModel;
	private OrdiniTableModel ordiniDiscesaTableModel;
	private static OrdiniTable ordiniSalitaTable;
	private ViaggiJTable viaggiNordTable;
	private JScrollPane sudTableScrollPane;
	private JScrollPane nordTableScrollPane;
	private JScrollPane clientiTableScrollPane;
	private JFormattedTextField formattedTextField;
	private JComboBox<String> camionCombo;
	private static Logger logger;
	public static Vector<Camion> camions;
	private JPanel NorthPanel;
	private JLabel lblSelezionaUnaData;
	private MaskFormatter dateMask;
	private JPanel CenterPanel;
	private JPanel TablePanel;
	private JPanel titlePanel;
	private static JLabel lblDataSelezionata;
	private static JLabel selectedDateLbl;
	private JPanel nordTablePanel;
	private JPanel nordTableButtonPanel;
	private JPanel sudTablePanel;
	private JPanel sudTableButtonPanel;
	private JButton sudAddButton;
	private JButton sudRemoveButton;
	private static JLabel giornoSettLabel;
	private JPanel ordiniSalitaTableButtonPanel;
	private JButton ordiniAddButton;
	private JButton ordiniRemoveButton;
	private JLabel lblOrdini;
	private JPanel panel;
	private JLabel lblNord;
	private JPanel panel_1;
	private JLabel lblSud;
	private JPanel panel_2;
	private OrdiniTableListener lis;
	public static final int RELOAD_STANDARD = 0;
	public static final int RELOAD_RESETCONNECTION = 1;
	private JButton btnEsportaQuestaData;
	private JPanel salitaPanel;
	private JPanel discesaPanel;
	private OrdiniTable ordiniDiscesaTable;
	private JScrollPane scrollPane;
	private JSplitPane clientiTableSplitPanel;
	private JPanel ordiniDiscesaButtonPanel;
	private JPanel panel_4;
	private JLabel lblNewLabel;
	private JButton button;
	private JButton button_1;
	private JSplitPane viaggiSplitPane;
	private JPanel panel_3;
	private JPanel panel_5;
	private JTable noteTable;
	private JScrollPane noteScrollPane;
	private JPanel otherPanel;
	private JPanel notePanel;
	private JTextArea fermiTxt;
	private JTextArea nonAssicuratiTxt;
	private JPanel panel_6;
	private JPanel panel_7;
	private JLabel lblFermi;
	private JLabel lblNonAssicurati;
	private JScrollPane fermiScrollPane;
	private JScrollPane nonAssicuratiScrollPane;
	private NoteTableModel noteModel;
	private JPanel noteButtonPanel;
	private JLabel lblNote;
	private JButton NoteRemoveButton;
	private JButton noteAddButton;
	private JPanel panel_8;
	private NoteTableListener noteListener;
	private JButton btnSalvaFermiE;
	private Nota fermiNota;
	private Nota nonAssNota;

	private void inizializzazione(){
		logger = Logger.getGlobal();
		FileHandler fh;
		try {
			fh = new FileHandler(Configuration.getLogfile()+"-MainFrame.log", true);
			fh.setLevel(Level.SEVERE);
			logger.addHandler(fh);

		} catch (SecurityException | IOException e2) {
			e2.printStackTrace();
		}
		lastDate = Date.valueOf("1999-10-10");
		try {
			dbu = DbUtilFactory.createDbUtil();
			lastDate = dbu.getDataAggiornamento();
		}catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Errore di connessione al database"
					+ "\nCodice errore :"+e.getErrorCode()+"\n"+e.getMessage(),
					"ERRORE", JOptionPane.ERROR_MESSAGE, null);

			System.exit(1);
		}


		setTitle("GestioneViaggi - VER: " + Configuration.PROG_VERSION);
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public MainFrame() throws PropertyVetoException {

		inizializzazione();

		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);


		contentPane.setLayout(new BorderLayout(0, 0));

		nordTableModel = new ViaggiTableModel(Viaggio.NORD);
		sudTableModel = new ViaggiTableModel(Viaggio.SUD);

		NorthPanel = new JPanel();
		contentPane.add(NorthPanel, BorderLayout.NORTH);

		lblSelezionaUnaData = new JLabel("Seleziona una data:");
		NorthPanel.add(lblSelezionaUnaData);


		formattedTextField = new CustomDateTextField();


		NorthPanel.add(formattedTextField);

		JButton cercaButton = new JButton("Cerca");
		NorthPanel.add(cercaButton);

		titlePanel = new JPanel();
		NorthPanel.add(titlePanel);

		lblDataSelezionata = new JLabel("Data selezionata:");
		lblDataSelezionata.setFont(new Font("Tahoma", Font.PLAIN, 15));
		titlePanel.add(lblDataSelezionata);

		selectedDateLbl = new JLabel("");
		selectedDateLbl.setFont(new Font("Tahoma", Font.PLAIN, 15));
		titlePanel.add(selectedDateLbl);

		giornoSettLabel = new JLabel("");
		giornoSettLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		titlePanel.add(giornoSettLabel);

		JButton btnGestisciCamion = new JButton("Gestisci Camion");
		NorthPanel.add(btnGestisciCamion);

		JButton btnAggiungiGiornata = new JButton("Aggiungi Giornata");
		NorthPanel.add(btnAggiungiGiornata);

		btnEsportaQuestaData = new JButton("Esporta questa data");
		NorthPanel.add(btnEsportaQuestaData);
		btnEsportaQuestaData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JButton b = (JButton) arg0.getSource();
				MainFrame root = (MainFrame) SwingUtilities.getRoot(b);
				try {
					new PdfReportBuilder(viaggiNordTable, viaggiSudTable, ordiniSalitaTable, ordiniDiscesaTable, noteTable, fermiTxt.getText(), nonAssicuratiTxt.getText(), selectedDateLbl.getText());
					JOptionPane.showMessageDialog(root, "Esportazione completata", "", JOptionPane.INFORMATION_MESSAGE);
					if (Desktop.isDesktopSupported()) {
						try {
							File myFile = new File("Exports\\" +selectedDateLbl.getText()+".pdf");
							Desktop.getDesktop().open(myFile);
						} catch (IOException ex) {
							// no application registered for PDFs
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.log(Level.SEVERE, e.getMessage(), e);
					JOptionPane.showMessageDialog(root, e.getMessage(),
							"ERRORE", JOptionPane.ERROR_MESSAGE, null);
				}

			}
		});

		btnAggiungiGiornata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JButton b = (JButton) arg0.getSource();
				MainFrame root = (MainFrame) SwingUtilities.getRoot(b);
				JFrame inserisciGiornataFrame = new AggiungiDataFrame(root);
				inserisciGiornataFrame.setVisible(true);
			}
		});

		btnGestisciCamion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JButton b = (JButton) arg0.getSource();
				MainFrame root = (MainFrame) SwingUtilities.getRoot(b);
				JFrame aggiungiCamionFrame = new AggiungiCamionFrame(root);
				aggiungiCamionFrame.setVisible(true);
			}
		});

		cercaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = formattedTextField.getText();
				Date d = null;
				try{
					d = checkAndCreateDate(text);
				}catch(NumberFormatException ex){
					logger.log(Level.SEVERE, ex.getMessage(), ex);
					formattedTextField.setBackground(Color.RED);
				}

				if(d != null){
					//System.out.println(checkAndCreateDate(text).toString());
					reloadTableModel(d, MainFrame.RELOAD_STANDARD);
					reloadOrdiniModel(d);
					reloadNote(d);
				}else{
					formattedTextField.setBackground(Color.RED);
				}
			}
		});

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
						logger.log(Level.SEVERE, ex.getMessage(), ex);
					}

					if(d != null){
						//System.out.println(checkAndCreateDate(text).toString());
						reloadTableModel(d, MainFrame.RELOAD_STANDARD);
						reloadOrdiniModel(d);
						reloadNote(d);
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

		CenterPanel = new JPanel();
		contentPane.add(CenterPanel, BorderLayout.CENTER);
		CenterPanel.setLayout(new BorderLayout(0, 0));

		TablePanel = new JPanel();
		TablePanel.setAlignmentY(Component.TOP_ALIGNMENT);
		TablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		CenterPanel.add(TablePanel);
		TablePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		ordiniSalitaTableModel = new OrdiniTableModel();

		panel_5 = new JPanel();
		TablePanel.add(panel_5);



		salitaPanel = new JPanel();
		salitaPanel.setLayout(new BorderLayout(0, 0));
		ordiniSalitaTableButtonPanel = new JPanel();
		salitaPanel.add(ordiniSalitaTableButtonPanel, BorderLayout.NORTH);
		ordiniSalitaTableButtonPanel.setLayout(new BorderLayout(0, 0));

		lblOrdini = new JLabel("ORDINI SALITA");
		lblOrdini.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblOrdini.setHorizontalAlignment(SwingConstants.CENTER);
		ordiniSalitaTableButtonPanel.add(lblOrdini, BorderLayout.CENTER);

		panel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.TRAILING);
		ordiniSalitaTableButtonPanel.add(panel, BorderLayout.EAST);

		ordiniRemoveButton = new JButton("-");
		panel.add(ordiniRemoveButton);

		ordiniAddButton = new JButton("+");
		panel.add(ordiniAddButton);

		ordiniSalitaTable = new OrdiniTable();
		ordiniSalitaTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		ordiniSalitaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ordiniSalitaTable.setCellSelectionEnabled(true);
		ordiniSalitaTable.setModel(ordiniSalitaTableModel);
		TablePanel.add(ordiniSalitaTable);

		clientiTableScrollPane = new JScrollPane(ordiniSalitaTable);
		clientiTableScrollPane.setPreferredSize(new Dimension(452, 200));
		salitaPanel.add(clientiTableScrollPane);

		discesaPanel = new JPanel();
		discesaPanel.setPreferredSize(new Dimension(452, 200));
		discesaPanel.setLayout(new BorderLayout(0, 0));
		panel_5.setLayout(new BorderLayout(0, 0));


		clientiTableSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, salitaPanel, discesaPanel);
		clientiTableSplitPanel.setVerifyInputWhenFocusTarget(false);
		clientiTableSplitPanel.setDividerSize(1);
		panel_5.add(clientiTableSplitPanel);
		clientiTableSplitPanel.setBorder(null);
		clientiTableSplitPanel.setLayout(new BoxLayout(clientiTableSplitPanel, BoxLayout.Y_AXIS));

		notePanel = new JPanel();
		panel_5.add(notePanel, BorderLayout.SOUTH);
		notePanel.setLayout(new BorderLayout(0, 0));

		noteScrollPane = new JScrollPane();
		notePanel.add(noteScrollPane);

		noteTable = new JTable();
		noteScrollPane.setViewportView(noteTable);

		noteModel = new NoteTableModel();

		noteTable.setModel(noteModel);

		noteButtonPanel = new JPanel();
		notePanel.add(noteButtonPanel, BorderLayout.NORTH);
		noteButtonPanel.setLayout(new BorderLayout(0, 0));

		lblNote = new JLabel("Note");
		lblNote.setHorizontalAlignment(SwingConstants.CENTER);
		noteButtonPanel.add(lblNote, BorderLayout.CENTER);

		panel_8 = new JPanel();
		noteButtonPanel.add(panel_8, BorderLayout.EAST);

		NoteRemoveButton = new JButton("-");
		NoteRemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component source = (Component) e.getSource();
				MainFrame root = (MainFrame) SwingUtilities.getRoot(source);
				int sel = noteTable.getSelectedRow();
				NoteTableModel tm = (NoteTableModel) noteTable.getModel();
				Nota n = tm.removeRow(sel);
				try {
					dbu.rimuoviNota(n);
				} catch (SQLException e1) {
					tm.addRow(n);
					e1.printStackTrace();
					logger.log(Level.SEVERE, e1.getMessage(), e1);
					JOptionPane.showMessageDialog(root, "Errore di connessione al server"
							+ "\nCodice errore  :"+e1.getErrorCode()+"\n"+e1.getMessage(),
							"ERRORE", JOptionPane.ERROR_MESSAGE, null);
				}
			}
		});
		panel_8.add(NoteRemoveButton);

		noteAddButton = new JButton("+");
		noteAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Component source = (Component) arg0.getSource();
				MainFrame root = (MainFrame) SwingUtilities.getRoot(source);
				String s = selectedDateLbl.getText();
				s = s.replace('-', '/');
				Date d = null;
				try{
					d = checkAndCreateDate(s);
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(root, "Deve essere selezionata una \ndata corretta per poter inserire\n"
							+ "un ordine.\nControllare la data inserita.", "ERRORE", JOptionPane.ERROR_MESSAGE);
				}
				if(d!=null){
					try {
						Nota nuovo = new Nota(d, "", Nota.NOTA);
						long newId = dbu.aggiungiNota(nuovo);
						logger.info("New id = " + newId);
						nuovo.setId(newId);
						NoteTableModel tm = (NoteTableModel) noteTable.getModel();
						tm.addRow(nuovo);
					} catch (SQLException e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
						e.printStackTrace();
						JOptionPane.showMessageDialog(root, "Errore di connessione al server"
								+ "\nCodice errore :"+e.getErrorCode()+"\n"+e.getMessage(),
								"ERRORE", JOptionPane.ERROR_MESSAGE, null);
					}

				}else{
					JOptionPane.showMessageDialog(root, "Deve essere selezionata una \ndata corretta per poter inserire "
							+ "un ordine.\nControllare la data inserita.", "ERRORE", JOptionPane.ERROR_MESSAGE);
				}

			}

		});
		panel_8.add(noteAddButton);

		ordiniDiscesaButtonPanel = new JPanel();
		discesaPanel.add(ordiniDiscesaButtonPanel, BorderLayout.NORTH);
		ordiniDiscesaButtonPanel.setLayout(new BorderLayout(0, 0));

		lblNewLabel = new JLabel("DISCESA");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ordiniDiscesaButtonPanel.add(lblNewLabel);

		panel_4 = new JPanel();
		ordiniDiscesaButtonPanel.add(panel_4, BorderLayout.EAST);
		panel_4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		button_1 = new JButton("-");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component source = (Component) e.getSource();
				MainFrame root = (MainFrame) SwingUtilities.getRoot(source);
				int sel = ordiniDiscesaTable.getSelectedRow();
				OrdiniTableModel tm = (OrdiniTableModel) ordiniDiscesaTable.getModel();
				Ordine o = tm.removeRow(sel);
				try {
					dbu.rimuoviOrdine(o);
				} catch (SQLException e1) {
					tm.addRow(o);
					e1.printStackTrace();
					logger.log(Level.SEVERE, e1.getMessage(), e1);
					JOptionPane.showMessageDialog(root, "Errore di connessione al server"
							+ "\nCodice errore :"+e1.getErrorCode()+"\n"+e1.getMessage(),
							"ERRORE", JOptionPane.ERROR_MESSAGE, null);
				}
			}
		});
		panel_4.add(button_1);

		button = new JButton("+");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component source = (Component) e.getSource();
				MainFrame root = (MainFrame) SwingUtilities.getRoot(source);
				String s = selectedDateLbl.getText();
				s = s.replace('-', '/');
				Date d = null;
				try{
					d = checkAndCreateDate(s);
				}catch(NumberFormatException e1){
					JOptionPane.showMessageDialog(root, "Deve essere selezionata una \ndata corretta per poter inserire\n"
							+ "un ordine.\nControllare la data inserita.", "ERRORE", JOptionPane.ERROR_MESSAGE);
				}
				if(d!=null){
					AggiungiOrdineFrame fr;
					try {
						fr = new AggiungiOrdineFrame(root, Ordine.DISCESA, d);
						fr.setVisible(true);
					} catch (SQLException e1) {
						logger.log(Level.SEVERE, e1.getMessage(), e1);
						e1.printStackTrace();
						JOptionPane.showMessageDialog(root, "Errore di connessione al server"
								+ "\nCodice errore :"+e1.getErrorCode()+"\n"+e1.getMessage(),
								"ERRORE", JOptionPane.ERROR_MESSAGE, null);
					}

				}else{
					JOptionPane.showMessageDialog(root, "Deve essere selezionata una \ndata corretta per poter inserire "
							+ "un ordine.\nControllare la data inserita.", "ERRORE", JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		panel_4.add(button);

		scrollPane = new JScrollPane();
		discesaPanel.add(scrollPane, BorderLayout.CENTER);

		ordiniDiscesaTable = new OrdiniTable();
		ordiniDiscesaTable.setCellSelectionEnabled(true);
		ordiniDiscesaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ordiniDiscesaTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		ordiniDiscesaTableModel = new OrdiniTableModel();
		ordiniDiscesaTable.setModel(ordiniDiscesaTableModel);
		scrollPane.setViewportView(ordiniDiscesaTable);
		ordiniAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Component source = (Component) arg0.getSource();
				MainFrame root = (MainFrame) SwingUtilities.getRoot(source);
				String s = selectedDateLbl.getText();
				s = s.replace('-', '/');
				Date d = null;
				try{
					d = checkAndCreateDate(s);
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(root, "Deve essere selezionata una \ndata corretta per poter inserire\n"
							+ "un ordine.\nControllare la data inserita.", "ERRORE", JOptionPane.ERROR_MESSAGE);
				}
				if(d!=null){
					AggiungiOrdineFrame fr;
					try {
						fr = new AggiungiOrdineFrame(root, Ordine.SALITA, d);
						fr.setVisible(true);
					} catch (SQLException e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
						e.printStackTrace();
						JOptionPane.showMessageDialog(root, "Errore di connessione al server"
								+ "\nCodice errore :"+e.getErrorCode()+"\n"+e.getMessage(),
								"ERRORE", JOptionPane.ERROR_MESSAGE, null);
					}

				}else{
					JOptionPane.showMessageDialog(root, "Deve essere selezionata una \ndata corretta per poter inserire "
							+ "un ordine.\nControllare la data inserita.", "ERRORE", JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		ordiniRemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Component source = (Component) arg0.getSource();
				MainFrame root = (MainFrame) SwingUtilities.getRoot(source);
				int sel = ordiniSalitaTable.getSelectedRow();
				OrdiniTableModel tm = (OrdiniTableModel) ordiniSalitaTable.getModel();
				Ordine o = tm.removeRow(sel);
				try {
					dbu.rimuoviOrdine(o);
				} catch (SQLException e) {
					tm.addRow(o);
					e.printStackTrace();
					logger.log(Level.SEVERE, e.getMessage(), e);
					JOptionPane.showMessageDialog(root, "Errore di connessione al server"
							+ "\nCodice errore :"+e.getErrorCode()+"\n"+e.getMessage(),
							"ERRORE", JOptionPane.ERROR_MESSAGE, null);
				}

			}
		});

		try {
			lis = new OrdiniTableListener();
			ordiniSalitaTableModel.addTableModelListener(lis);
			ordiniDiscesaTableModel.addTableModelListener(lis);
			noteListener = new NoteTableListener();
			noteModel.addTableModelListener(noteListener);
		} catch (SQLException e1) {
			logger.log(Level.SEVERE, e1.getMessage(), e1);
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this, "Errore di connessione al server"
					+ "\nCodice errore :"+e1.getErrorCode()+"\n"+e1.getMessage(),
					"ERRORE", JOptionPane.ERROR_MESSAGE, null);
		}

		sudTablePanel = new JPanel();
		TablePanel.add(sudTablePanel);

		viaggiSudTable = new ViaggiJTable();
		viaggiSudTable.setType(Viaggio.SUD);
		viaggiSudTable.setCellSelectionEnabled(true);
		viaggiSudTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TablePanel.add(viaggiSudTable);
		//viaggiSudTable.setBounds(944, 103, 400, 280);
		viaggiSudTable.setModel(sudTableModel);
		viaggiSudTable.getModel().addTableModelListener(this);
		viaggiSudTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());
		sudTablePanel.setLayout(new BorderLayout(0, 0));

		sudTableButtonPanel = new JPanel();
		sudTablePanel.add(sudTableButtonPanel, BorderLayout.NORTH);
		sudTableButtonPanel.setLayout(new BorderLayout(0, 0));

		panel_3 = new JPanel();
		TablePanel.add(panel_3);



		nordTablePanel = new JPanel();
		TablePanel.add(nordTablePanel);

		viaggiNordTable = new ViaggiJTable();
		viaggiNordTable.setType(Viaggio.NORD);
		TablePanel.add(viaggiNordTable);
		viaggiNordTable.setCellSelectionEnabled(true);
		viaggiNordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//viaggiNordTable.setBounds(469, 103, 400, 280);
		viaggiNordTable.setModel(nordTableModel);
		viaggiNordTable.getModel().addTableModelListener(this);
		viaggiNordTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());
		nordTablePanel.setLayout(new BorderLayout(0, 0));

		nordTableButtonPanel = new JPanel();
		nordTablePanel.add(nordTableButtonPanel, BorderLayout.NORTH);
		nordTableButtonPanel.setLayout(new BorderLayout(0, 0));

		lblNord = new JLabel("NORD");
		lblNord.setHorizontalAlignment(SwingConstants.CENTER);
		lblNord.setFont(new Font("Tahoma", Font.PLAIN, 15));
		nordTableButtonPanel.add(lblNord);

		panel_1 = new JPanel();
		nordTableButtonPanel.add(panel_1, BorderLayout.EAST);

		JButton nordRemoveButton = new JButton("-");
		panel_1.add(nordRemoveButton);
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
					logger.log(Level.SEVERE, e.getMessage(), e);
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Errore di connessione al server"
							+ "\nCodice errore :"+e.getErrorCode()+"\n"+e.getMessage(),
							"ERRORE", JOptionPane.ERROR_MESSAGE, null);
				}
			}
		});



		JButton nordAddButton = new JButton("+");
		panel_1.add(nordAddButton);
		nordAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				nordTableModel.addRow(null);
			}
		});
		panel_3.setLayout(new BorderLayout(0, 0));
		viaggiNordTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

		nordTableScrollPane = new JScrollPane(viaggiNordTable);
		nordTablePanel.add(nordTableScrollPane);
		viaggiSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, nordTablePanel, sudTablePanel);
		viaggiSplitPane.setDividerSize(0);
		viaggiSplitPane.setBorder(null);
		viaggiSplitPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		viaggiSplitPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_3.add(viaggiSplitPane);

		otherPanel = new JPanel();
		panel_3.add(otherPanel, BorderLayout.SOUTH);

		panel_6 = new JPanel();
		otherPanel.add(panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));

		fermiTxt = new JTextArea();
		fermiTxt.setColumns(40);
		fermiTxt.setRows(4);
		fermiTxt.setLineWrap(true);
		fermiTxt.setWrapStyleWord(true);
		panel_6.add(fermiTxt);
		fermiTxt.setText("");

		fermiScrollPane = new JScrollPane(fermiTxt);
		panel_6.add(fermiScrollPane, BorderLayout.SOUTH);

		lblFermi = new JLabel("Fermi assicurati");
		panel_6.add(lblFermi, BorderLayout.NORTH);

		panel_7 = new JPanel();
		otherPanel.add(panel_7);
		panel_7.setLayout(new BorderLayout(0, 0));

		nonAssicuratiTxt = new JTextArea();
		nonAssicuratiTxt.setTabSize(2);
		nonAssicuratiTxt.setRows(4);
		nonAssicuratiTxt.setColumns(40);
		nonAssicuratiTxt.setLineWrap(true);
		nonAssicuratiTxt.setWrapStyleWord(true);
		nonAssicuratiTxt.setText("");

		nonAssicuratiScrollPane = new JScrollPane(nonAssicuratiTxt);
		panel_7.add(nonAssicuratiScrollPane);

		lblNonAssicurati = new JLabel("Non assicurati");
		panel_7.add(lblNonAssicurati, BorderLayout.NORTH);

		btnSalvaFermiE = new JButton("Salva fermi e non ass.");
		btnSalvaFermiE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					String s = selectedDateLbl.getText();
					s = s.replace('-', '/');
					Date d = checkAndCreateDate(s);

					if(fermiNota == null){
						fermiNota = new Nota(d, fermiTxt.getText(), Nota.FERMI);
						dbu.aggiungiNota(fermiNota);
						fermiNota = dbu.getFermiByDate(d);
					}else{
						fermiNota.setTesto(fermiTxt.getText());
						dbu.modificaNota(fermiNota);
					}

					if(nonAssNota == null){
						nonAssNota = new Nota(d, fermiTxt.getText(), Nota.NONASS);
						dbu.aggiungiNota(nonAssNota);
						nonAssNota = dbu.getNonAssByDate(d);
					}else{
						nonAssNota.setTesto(nonAssicuratiTxt.getText());
						dbu.modificaNota(nonAssNota);
					}

					reloadNote(d);


				} catch (SQLException e1) {
					logger.log(Level.SEVERE, e1.getMessage(), e1);
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "Errore di connessione al server"
							+ "\nCodice errore :"+e1.getErrorCode()+"\n"+e1.getMessage(),
							"ERRORE", JOptionPane.ERROR_MESSAGE, null);
				}


			}
		});

		otherPanel.add(btnSalvaFermiE);
		lblSud = new JLabel("SUD");
		lblSud.setHorizontalAlignment(SwingConstants.CENTER);
		lblSud.setFont(new Font("Tahoma", Font.PLAIN, 15));
		sudTableButtonPanel.add(lblSud, BorderLayout.CENTER);

		panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		sudTableButtonPanel.add(panel_2, BorderLayout.EAST);

		sudRemoveButton = new JButton("-");
		panel_2.add(sudRemoveButton);
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
					logger.log(Level.SEVERE, e.getMessage(), e);
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Errore di connessione al server"
							+ "\nCodice errore :"+e.getErrorCode()+"\n"+e.getMessage(),
							"ERRORE", JOptionPane.ERROR_MESSAGE, null);
				}
			}
		});

		sudAddButton = new JButton("+");
		panel_2.add(sudAddButton);
		sudAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sudTableModel.addRow(null);
			}
		});

		sudTableScrollPane = new JScrollPane(viaggiSudTable);
		sudTableScrollPane.setBorder(null);
		sudTablePanel.add(sudTableScrollPane);

		try {
			updateCamionList();
		}catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Errore di connessione al server"
					+ "\nCodice errore :"+e.getErrorCode()+"\n"+e.getMessage(),
					"ERRORE", JOptionPane.ERROR_MESSAGE, null);
		}

		addListeners();
		setLocationByPlatform(true);
		formattaTabelleViaggi();

		reloadTableModel(lastDate, MainFrame.RELOAD_STANDARD);
		reloadOrdiniModel(lastDate);
		reloadNote(lastDate);

	}


	public void updateCamionList() throws SQLException {

		camions = dbu.getCamion();
		formattaTabelleViaggi();
		if(selectedDateLbl.getText().compareTo("") == 0){

		}else{
			Date d = checkAndCreateDate(selectedDateLbl.getText().replace('-', '/'));
			reloadTableModel(d, MainFrame.RELOAD_STANDARD);
		}
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

	public void reloadTableModel(Date d, int option) {
		Vector<Viaggio> nord = null;
		Vector<Viaggio> sud = null;
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		int dow = c.get(Calendar.DAY_OF_WEEK);
		switch(dow){
		case 1: giornoSettLabel.setText("DOMENICA"); break;
		case 2: giornoSettLabel.setText("LUNEDI"); break;
		case 3: giornoSettLabel.setText("MARTEDI"); break;
		case 4: giornoSettLabel.setText("MERCOLEDI"); break;
		case 5: giornoSettLabel.setText("GIOVEDI"); break;
		case 6: giornoSettLabel.setText("VENERDI"); break;
		case 7: giornoSettLabel.setText("SABATO"); break;
		}
		String anno, mese, giorno;

		if(option == MainFrame.RELOAD_STANDARD){
			try {
				nord = dbu.getViaggiBy(Viaggio.NORD, d);
				sud = dbu.getViaggiBy(Viaggio.SUD, d);
				String[] data = d.toString().split("-");
				anno = data[0];
				mese = data[1];
				giorno = data[2];
				String dat = giorno + "-" + mese +"-"+anno;
				selectedDateLbl.setText(dat);
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Errore di connessione al server"
						+ "\nCodice errore :"+e.getErrorCode()+"\n"+e.getMessage(),
						"ERRORE", JOptionPane.ERROR_MESSAGE, null);
			}
		}else if(option == MainFrame.RELOAD_RESETCONNECTION){
			try {
				dbu.closeConnection();
				dbu.setConnection(DbUtil.createConnection());
				nord = dbu.getViaggiBy(Viaggio.NORD, d);
				sud = dbu.getViaggiBy(Viaggio.SUD, d);
				String[] data = d.toString().split("-");
				anno = data[0];
				mese = data[1];
				giorno = data[2];
				String dat = giorno + "-" + mese +"-"+anno;
				selectedDateLbl.setText(dat);
			} catch (SQLException e1) {
				logger.log(Level.SEVERE, e1.getMessage(), e1);
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Errore di connessione al server"
						+ "\nCodice errore :"+e1.getErrorCode()+"\n"+e1.getMessage(),
						"ERRORE", JOptionPane.ERROR_MESSAGE, null);
			}
		}


		sudTableModel.setData(sud);
		sudTableModel.setCurrentDate(d);
		nordTableModel.setData(nord);
		nordTableModel.setCurrentDate(d);

	}

	public void reloadOrdiniModel(Date d){

		OrdiniTableModel tmSalita = (OrdiniTableModel) ordiniSalitaTable.getModel();
		OrdiniTableModel tmDiscesa = (OrdiniTableModel) ordiniDiscesaTable.getModel();

		Vector<Ordine> salite = new Vector<Ordine>();
		Vector<Ordine> discese = new Vector<Ordine>();

		try {
			Vector<Ordine> fromDB = dbu.getOrdiniByDate(d);
			for(Ordine o : fromDB){
				if(o.getType().compareTo(Ordine.SALITA) == 0){
					salite.addElement(o);
				}else if(o.getType().compareTo(Ordine.DISCESA) == 0){
					discese.addElement(o);
				}
			}
			tmSalita.setData(salite);
			tmDiscesa.setData(discese);

		} catch (SQLException e1) {
			logger.log(Level.SEVERE, e1.getMessage(), e1);
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "Errore di connessione al server"
					+ "\nCodice errore :"+e1.getErrorCode()+"\n"+e1.getMessage(),
					"ERRORE", JOptionPane.ERROR_MESSAGE, null);
		}

	}

	public void reloadNote(Date d){

		Vector<Nota> fromDB = new Vector<>();
		Vector<Nota> toNoteTable = new Vector<>();
		NoteTableModel tm = (NoteTableModel) noteTable.getModel();

		try{
			fromDB = dbu.getNoteByDate(d);
			for(Nota n : fromDB){
				if(n.getTipo().compareTo(Nota.NOTA) == 0){
					toNoteTable.addElement(n);
				}else if(n.getTipo().compareTo(Nota.FERMI) == 0){
					fermiNota = n;
					if(fermiNota != null)
					fermiTxt.setText(fermiNota.getTesto());
				}else if(n.getTipo().compareTo(Nota.NONASS) == 0){
					nonAssNota = n;
					nonAssicuratiTxt.setText(nonAssNota.getTesto());
				}
			}
			tm.setData(toNoteTable);
		}catch(SQLException e1){
			logger.log(Level.SEVERE, e1.getMessage(), e1);
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "Errore di connessione al server"
					+ "\nCodice errore :"+e1.getErrorCode()+"\n"+e1.getMessage(),
					"ERRORE", JOptionPane.ERROR_MESSAGE, null);
		}

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
				/*if(col == 0){
					if(tm.getType() == Viaggio.NORD && sudTableModel.existsCamion(v.getCamion()) > 0)
						throw new IllegalArgumentException("Esiste gia questo camion in questa data");
					else if(tm.getType() == Viaggio.SUD && nordTableModel.existsCamion(v.getCamion()) > 0)
						throw new IllegalArgumentException("Esiste gia questo camion in questa data");
				}*/
				//logger.info(v.toString());
				(new UpdateWorker2(v, col, dbu)).execute();
			}else if(e.getType() == TableModelEvent.INSERT){
				Viaggio nuovo = tm.getElementAt(row-1);
				long newId = -1;
				logger.info(nuovo.toString());
				try {
					newId = dbu.aggiungiViaggio(nuovo);
				} catch (SQLException e1) {
					logger.log(Level.SEVERE, e1.getMessage(), e1);
					JOptionPane.showMessageDialog(null, "Errore di collegamento ad database"+
							"\nCodice errore :"+e1.getErrorCode()+"\n"+e1.getMessage(),
							"ERRORE", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				logger.info("LAST ID INSERTED: "+newId);

				if(newId > 0){
					tm.getElementAt(row-1).setId(newId);
					logger.info(tm.getElementAt(row-1).toString());
				}else throw new IllegalArgumentException("Problemi nell'id del nuovo viaggio");


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
				col.setPreferredWidth(75);
			}else if(i == 1){
				col = viaggiNordTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(100);
			}else if(i == 3){
				col = viaggiNordTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(200);
			}else if(i == 4){
				col = viaggiNordTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(16);
			}
		}

		for(int i = 0; i < 6; i++){
			if(i==0){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setCellEditor(new DefaultCellEditor(camionCombo));
				col.setPreferredWidth(75);
			}else if(i == 1){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(100);
			}else if(i == 3){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(200);
			}else if(i == 4){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(30);
			}else if(i == 5){
				col = viaggiSudTable.getColumnModel().getColumn(i);
				col.setPreferredWidth(16);
			}
		}

		for(int i=0; i<3; i++){
			if(i == 1) {
				col = ordiniSalitaTable.getColumnModel().getColumn(i);
				col.setMaxWidth(75);
				col.setMaxWidth(100);
				col.setPreferredWidth(75);
				col = ordiniDiscesaTable.getColumnModel().getColumn(i);
				col.setMaxWidth(75);
				col.setMaxWidth(100);
				col.setPreferredWidth(75);
			}else if(i == 2){
				col = ordiniSalitaTable.getColumnModel().getColumn(i);
				col.setMaxWidth(125);
				col.setMinWidth(80);
				col.setPreferredWidth(80);
				col = ordiniDiscesaTable.getColumnModel().getColumn(i);
				col.setMaxWidth(125);
				col.setMinWidth(80);
				col.setPreferredWidth(80);
			}else if(i == 0){
				col = ordiniSalitaTable.getColumnModel().getColumn(i);
				col.setMaxWidth(16);
				col.setMinWidth(16);
				col.setPreferredWidth(16);
				col = ordiniDiscesaTable.getColumnModel().getColumn(i);
				col.setMaxWidth(16);
				col.setMinWidth(16);
				col.setPreferredWidth(16);
			}
		}

	}

	private void addListeners(){
		this.addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent arg0) {
				Component source = (Component) arg0.getComponent();

				
				/*int newWidth = source.getWidth();
				int newHeight = CenterPanel.getHeight()-10;

				//clientiNoteTablePanel.getWidth();
				//clientiNoteTablePanel.getHeight();

				if(arg0.getNewState() == Frame.MAXIMIZED_BOTH){
					clientiTableSplitPanel.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight/3.5)));
					nordTablePanel.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight)));
					sudTablePanel.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight)));
					notePanel.setPreferredSize(new Dimension((newWidth/3)-20, CenterPanel.getHeight()-((newHeight/2) + ((int)(newHeight/3.5)))));
					noteScrollPane.setPreferredSize(new Dimension((newWidth/3)-20, CenterPanel.getHeight()-((newHeight/2) + ((int)(newHeight/3.5)))));
				}
				*/
			}
		});


		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				Component source = (Component) arg0.getComponent();
				int newWidth = source.getWidth();
				int newHeight = CenterPanel.getHeight()-10;

				//clientiNoteTablePanel.getWidth();
				//clientiNoteTablePanel.getHeight();

				contentPane.setMaximumSize(source.getSize());

				clientiTableSplitPanel.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight/3.5)));

				nordTablePanel.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight/3)));
				sudTablePanel.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight/3)));
				//viaggiSplitPane.setPreferredSize(new Dimension((newWidth/2), (newHeight/2) + (int)(newHeight/3)));

				notePanel.setPreferredSize(new Dimension((newWidth/3)-20, CenterPanel.getHeight()-((newHeight/2) + ((int)(newHeight/3.5) + 20))));
				//noteScrollPane.setPreferredSize(new Dimension((newWidth/3)-20, CenterPanel.getHeight()-((newHeight/2) + ((int)(newHeight/3.5) +20 ))));

			}

		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				try {
					dbu.closeConnection();
					lis.closeConnection();
					noteListener.closeConnection();
				} catch (SQLException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					e.printStackTrace();
				}
			}
		});
	}
}