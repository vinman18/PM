package it.vin.dev.menzione.main_frame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.TableColumnModel;
import javax.swing.text.MaskFormatter;

import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.Msg;
import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.database_helper.DatabaseHelperChannel;
import it.vin.dev.menzione.database_helper.DatabaseHelperListener;
import it.vin.dev.menzione.frame.*;
import it.vin.dev.menzione.logica.*;
import it.vin.dev.menzione.workers.NoteUpdateWorker;
import it.vin.dev.menzione.workers.UpdateWorkerAdapter;
import it.vin.dev.menzione.workers.UpdateWorkerListener;
import it.vin.dev.menzione.workers.ViaggiUpdateWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


@SuppressWarnings("FieldCanBeLocal")
public class MainFrame extends JFrame implements TableModelListener, ReloadCallback, DatabaseHelperListener{

    private static final long serialVersionUID = 6785301407122344285L;
    public static final int RELOAD_STANDARD = 0;
    public static final int RELOAD_RESETCONNECTION = 1;

    public static final String SUD_PIN_COMMAND = "sud_pin";
    public static final String NORD_PIN_COMMAND = "nord_pin";
    public static final String SUD_UNPIN_COMMAND = "sud_unpin";
    public static final String NORD_UNPIN_COMMAND = "nord_unpin";

    public static final String ORDINI_DISCESA_COMMAND = "ordine_discesa";
    public static final String ORDINI_SALITA_COMMAND = "ordine_salita";

    private Date lastDateFromDb;
    private DatabaseService dbs;

    private Logger logger;

    private Date currentDate;
    public  Vector<Camion> camions;

    private JPanel contentPane;
    private JScrollPane sudTableScrollPane;
    private JScrollPane nordTableScrollPane;
    private JScrollPane nordPinScrollPane;
    private JScrollPane clientiTableScrollPane;
    private JScrollPane scrollPane;
    private JScrollPane fermiScrollPane;
    private JScrollPane nonAssicuratiScrollPane;
    private JScrollPane noteScrollPane;
    private JSplitPane viaggiSplitPane;
    private JSplitPane clientiTableSplitPane;
    private ViaggiJTable viaggiSudTable;
    private JScrollPane sudPinScrollPane;
    private ViaggiJTable viaggiSudPinTable;
    private JSplitPane viaggiSudSplitPane;
//    private ViaggiTableModel nordTableModel;
    private ViaggiTableModel viaggiNordPinTableModel;
    private ViaggiTableModel viaggiSudPinTableModel;
    private ViaggiTableModel sudTableModel;
    private OrdiniTableModel ordiniSalitaTableModel;
    private OrdiniTableModel ordiniDiscesaTableModel;
    private OrdiniTable ordiniSalitaTable;
    private ViaggiJTable viaggiNordTable;
    private ViaggiJTable viaggiNordPinTable;
    private JSplitPane viaggiNordSplitPane;
    private JFormattedTextField formattedTextField;
    private JComboBox<String> camionCombo;
    private JPanel NorthPanel;
    private JLabel lblDateSelection;
    private MaskFormatter dateMask;
    private JPanel TablePanel;
    private JPanel titlePanel;
    private JLabel lblDataSelezionata;
    private JLabel selectedDateLbl;
    private JPanel nordTablePanel;
    private JPanel nordTableButtonPanel;
    private JPanel sudTablePanel;
    private JPanel sudTableButtonPanel;
    private JButton sudAddButton;
    private JButton sudRemoveButton;
    private JButton sudPinButton;
    private JButton sudUnpinButton;
    private JButton nordRemoveButton;
    private JButton nordAddButton;
    private JButton nordPinButton;
    private JButton nordUnpinButton;
    private JLabel giornoSettLabel;
    private JPanel ordiniSalitaTableButtonPanel;
    private JButton ordiniSalitaAggiungiButton;
    private JButton ordiniSalitaRimuoviButton;
    private JLabel lblOrdini;
    private JPanel panel;
    private JLabel lblNord;
    private JPanel panel_1;
    private JLabel lblSud;
    private JPanel panel_2;
    private OrdiniTableListener lis;
    private JButton btnEsportaQuestaData;
    private JPanel salitaPanel;
    private JPanel discesaPanel;
    private OrdiniTable ordiniDiscesaTable;
    private JPanel ordiniDiscesaButtonPanel;
    private JPanel panel_4;
    private JLabel lblNewLabel;
    private JButton ordiniDiscesaAggiungiButton;
    private JButton ordiniDiscesaRimuoviButton;
    private JPanel panel_3;
    private JPanel panel_5;
    private JTable noteTable;
    private JPanel otherPanel;
    private JPanel notePanel;
    private JTextArea fermiTxt;
    private JTextArea nonAssicuratiTxt;
    private JPanel panel_6;
    private JPanel assNonAssicuratiPanel;
    private JLabel lblFermi;
    private JLabel lblNonAssicurati;
    private NoteTableModel noteModel;
    private JPanel noteButtonPanel;
    private JLabel lblNote;
    private JButton NoteRemoveButton;
    private JButton noteAddButton;
    private JPanel panel_8;
    private NoteTableListener noteListener;
    private JButton btnSalvaFermiE;
    private JPanel southPanel;
    private MessageJLabel infoTextField;

    private Nota fermiNota;
    private Nota nonAssNota;


    private Map<JTable, TableColumnModelListener> tableColumnModelListenerMap;

    private TableModelListener viaggiPinnedTableListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            ViaggiTableModel model = (ViaggiTableModel) e.getSource();
            int row = e.getFirstRow();
            int lastRow = e.getLastRow();
            int col = e.getColumn();
            int type = e.getType();

            if(type == TableModelEvent.INSERT) {
                row = row - 1;
            }

            logger.debug("viaggiPinTableListener: type=" + type + " row="+ row + " col="+ col);
            if(type == TableModelEvent.UPDATE && col < 0) {
                return;
            }

            Viaggio v = model.getElementAt(row);

            switch (type) {
                case TableModelEvent.INSERT:
                    v.setPinned(true);
                    col = Viaggio.COL_PINNED;
                    break;
                case TableModelEvent.DELETE:
                    v.setPinned(false);
                    col = Viaggio.COL_PINNED;
                    break;
            }

            ViaggiUpdateWorker.connect(dbs)
                    .update(v, col)
                    .onResult(new UpdateWorkerAdapter<Viaggio>() {
                        @Override
                        public void onUpdate(Viaggio updated, int col) {
                            if(col == Viaggio.COL_PINNED) {
                                infoTextField.setInfoMessage("Viaggio spostato con successo");
                            } else {
                                String value = ViaggiUtils.getViaggioValueByColumnIndex(updated, col);
                                String columnName = Viaggio.NORD.equals(updated.getPosizione())
                                        ? viaggiNordTable.getColumnName(col)
                                        : viaggiSudTable.getColumnName(col);

                                notifyRowUpdated("Viaggi " + updated.getPosizione(), columnName, value, updated.getData().toString());
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            MainFrame.this.onError(error);
                        }
                    })
                    .execute();
        }
    };

    private UpdateWorkerListener<Ordine> ordiniUpdateResultListener = new UpdateWorkerListener<Ordine>() {
        @Override
        public void onUpdate(Ordine updated, int col) {
            String columnName = ordiniDiscesaTable.getColumnName(col);
            String value = ViaggiUtils.getOrdineValueFromColumnIndex(updated, col);
            //infoTextField.setInfoMessage("Ordini: valore " + value  + " per la colonna " + columnName + " salvato con successo");
            notifyRowUpdated("Ordini", columnName, value, updated.getDate().toString());
        }

        @Override
        public void onError(Exception error) {
            MainFrame.this.onError(error);
        }

        @Override
        public void onInsert(Ordine inserted, long newId) {
            notifyRowInserted("Ordini", ""+newId, inserted.getDate().toString());
        }
    };

    private final UpdateWorkerListener<Nota> noteUpdateResultListener = new UpdateWorkerListener<Nota>() {
        @Override
        public void onUpdate(Nota updated, int col) {
            //infoTextField.setInfoMessage("Note: valore " + updated.getTesto() + " salvato con successo");
            notifyRowUpdated("Note", updated.getTipo(), updated.getTesto(), updated.getData().toString());
        }

        @Override
        public void onError(Exception error) {
            MainFrame.this.onError(error);
        }

        @Override
        public void onInsert(Nota inserted, long newId) {
            notifyRowInserted("Note", ""+newId, inserted.getData().toString());
        }
    };

    private MouseAdapter aggiornaOnClickAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            loadDate(currentDate, RELOAD_STANDARD);
            infoTextField.clearMessage();
        }
    };

    private final ActionListener openCamionFrameAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            JButton b = (JButton) arg0.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(b);
            JFrame aggiungiCamionFrame = new AggiungiCamionFrame(root);
            aggiungiCamionFrame.setVisible(true);
        }
    };

    private final ActionListener openNewDateFrameAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            JButton b = (JButton) arg0.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(b);
            JFrame inserisciGiornataFrame = new AggiungiDataFrame(root);
            inserisciGiornataFrame.setVisible(true);
        }
    };

    private final ActionListener goToLastDateAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                Date lastDate = dbs.getLastDate();
                if(!currentDate.equals(lastDate)) {
                    loadDate(lastDate, RELOAD_STANDARD);
                }
            } catch (SQLException e1) {
                logDatabaseError(e1);
            }
        }
    };

    private final ActionListener dateExportAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            JButton b = (JButton) actionEvent.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(b);
            try {
                new PdfReportBuilder(viaggiNordTable, viaggiSudTable, viaggiNordPinTable, viaggiSudPinTable, ordiniSalitaTable, ordiniDiscesaTable, noteTable, fermiTxt.getText(), nonAssicuratiTxt.getText(), selectedDateLbl.getText());
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
                logger.error(e.getMessage(), e);
                Msg.error(root, e.getMessage());
            }
        }
    };

    private final ActionListener addOrderAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            Component source = (Component) arg0.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(source);
            String type = arg0.getActionCommand().equals(ORDINI_DISCESA_COMMAND)
                    ? Ordine.DISCESA
                    : Ordine.SALITA;

            if(currentDate!=null){
                AggiungiOrdineFrame fr;
                try {
                    fr = new AggiungiOrdineFrame(root, type, currentDate);
                    fr.setResultListener(ordiniUpdateResultListener);
                    fr.setVisible(true);
                } catch (SQLException e) {
                    logDatabaseError(e);
                }
            }else{
                Msg.error(root, "Deve essere selezionata una \ndata corretta per poter inserire\n"
                        + "un ordine.\nControllare la data inserita.");
            }
        }
    };

    private final ActionListener removeOrderAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            OrdiniTable table = arg0.getActionCommand().equals(ORDINI_DISCESA_COMMAND)
                    ? ordiniDiscesaTable
                    : ordiniSalitaTable;

            int sel = table.getSelectedRow();
            OrdiniTableModel tm = (OrdiniTableModel) table.getModel();
            Ordine o = tm.removeRow(sel);
            try {
                dbs.rimuoviOrdine(o);
            } catch (SQLException e) {
                tm.addRow(o);
                logDatabaseError(e);
            }
        }
    };

    private final ActionListener aggiungiNotaAction = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            Component source = (Component) arg0.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(source);

            if (currentDate != null) {

                Nota nuovo = new Nota(currentDate, "", Nota.NOTA);
                //long newId = dbs.aggiungiNota(nuovo);

                NoteUpdateWorker.connect(dbs)
                        .insert(nuovo)
                        .onResult(new UpdateWorkerAdapter<Nota>() {
                            @Override
                            public void onError(Exception error) {
                                MainFrame.this.onError(error);
                            }

                            @Override
                            public void onInsert(Nota inserted, long newId) {
                                logger.info("New id = " + newId);
                                nuovo.setId(newId);
                                NoteTableModel tm = (NoteTableModel) noteTable.getModel();
                                tm.addRow(nuovo);
                                notifyRowInserted("Note", ""+newId, inserted.getData().toString());
                            }
                        })
                        .execute();
            } else {
                Msg.error(root, "Deve essere selezionata una \ndata corretta per poter inserire "
                        + "una nota.\nControllare la data inserita.");
            }

        }
    };

    private final ActionListener deleteNoteAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int sel = noteTable.getSelectedRow();
            NoteTableModel tm = (NoteTableModel) noteTable.getModel();
            Nota n = tm.removeRow(sel);
            try {
                dbs.rimuoviNota(n);
            } catch (SQLException e1) {
                tm.addRow(n);
                logDatabaseError(e1);
            }
        }
    };

    private final ActionListener salvaFermiENonAssAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
                    /*String s = selectedDateLbl.getText();
                    s = s.replace('-', '/');
                    Date d = ViaggiUtils.checkAndCreateDate(s);*/

            UpdateWorkerListener<Nota> resultListener = new UpdateWorkerListener<Nota>() {
                @SuppressWarnings("Duplicates")
                @Override
                public void onInsert(Nota inserted, long newId) {
                    String message = null;
                    if(Nota.FERMI.equals(inserted.getTipo())) {
                        message = "Fermi (e Non Ass) salvati con successo";
                        fermiNota = inserted;
                        fermiTxt.setText(fermiNota.getTesto());
                    } else if(Nota.NONASS.equals(inserted.getTipo())) {
                        message = "Non Ass (e Fermi) salvati con successo";
                        nonAssNota = inserted;
                        nonAssicuratiTxt.setText(nonAssNota.getTesto());
                    }
                    notifyRowUpdated("Fermi e Non Ass", inserted.getData().toString(), message);
                }

                @SuppressWarnings("Duplicates")
                @Override
                public void onUpdate(Nota updated, int col) {
                    String message = null;
                    if(Nota.FERMI.equals(updated.getTipo())) {
                        message = "Fermi (e Non Ass) salvati con successo";
                        fermiNota = updated;
                        fermiTxt.setText(fermiNota.getTesto());
                    } else if(Nota.NONASS.equals(updated.getTipo())) {
                        message = "Non Ass (e Fermi) salvati con successo";
                        nonAssNota = updated;
                        nonAssicuratiTxt.setText(nonAssNota.getTesto());
                    }

                    //infoTextField.setInfoMessage(message);
                    notifyRowUpdated("Fermi e Non Ass", updated.getData().toString(), message);
                }

                @Override
                public void onError(Exception error) {
                    MainFrame.this.onError(error);
                }
            };

            /*if(fermiNota == null){
                fermiNota = new Nota(currentDate, fermiTxt.getText(), Nota.FERMI);
                dbs.aggiungiNota(fermiNota);
                fermiNota = dbs.getFermiByDate(currentDate);
            }else{
                fermiNota.setTesto(fermiTxt.getText());
                dbs.modificaNota(fermiNota);
            }

            if(nonAssNota == null){
                nonAssNota = new Nota(currentDate, fermiTxt.getText(), Nota.NONASS);
                dbs.aggiungiNota(nonAssNota);
                nonAssNota = dbs.getNonAssByDate(currentDate);
            }else{
                nonAssNota.setTesto(nonAssicuratiTxt.getText());
                dbs.modificaNota(nonAssNota);
            }*/
            if(fermiNota == null) {
                fermiNota = new Nota(currentDate, fermiTxt.getText(), Nota.FERMI);
                NoteUpdateWorker.connect(dbs)
                        .insert(fermiNota)
                        .onResult(resultListener)
                        .execute();
            } else {
                fermiNota.setTesto(fermiTxt.getText());
                NoteUpdateWorker.connect(dbs)
                        .update(fermiNota)
                        .onResult(resultListener)
                        .execute();
            }

            if(nonAssNota == null) {
                nonAssNota = new Nota(currentDate, fermiTxt.getText(), Nota.NONASS);
                NoteUpdateWorker.connect(dbs)
                        .insert(nonAssNota)
                        .onResult(resultListener)
                        .execute();
            } else {
                nonAssNota.setTesto(nonAssicuratiTxt.getText());
                NoteUpdateWorker.connect(dbs)
                        .update(nonAssNota)
                        .onResult(resultListener)
                        .execute();
            }

            //reloadNote(currentDate);
        }
    };

    private ActionListener reloadAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadDate(currentDate, MainFrame.RELOAD_RESETCONNECTION);
        }
    };

    private ActionListener deleteThisDayAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Component source = (Component) e.getSource();
            Component root = SwingUtilities.getRoot(source);

            int resp1 = Msg.yesno(root, "Stai per cancellare la data " + currentDate.toString() + "." +
                    " Sei sicuro? \n\nATTENZIONE: QUESTA OPERAZIONE NON PUO' ESSERE ANNULLATA.");

            if(resp1 == JOptionPane.YES_OPTION) {
                int resp2 = Msg.yesno(root, "Confermi di eliminare la data " + currentDate.toString() + "?" +
                        "\n\n Questa operazione NON può essere annullata.");

                if(resp2 == JOptionPane.YES_OPTION) {
                    try {
                        dbs.deleteDate(currentDate);
                        DatabaseHelperChannel.getInstance().notifyDateRemoved(currentDate.toString());
                        loadDate(dbs.getDataAggiornamento(), RELOAD_RESETCONNECTION);
                    } catch (SQLException e1) {
                        logDatabaseError(e1);
                    } catch (RemoteException e1) {
                        infoTextField.setWarnMessage("Impossibile comunicare l'azione agli altri clients");
                    }
                }
            }
        }
    };

    private ActionListener viaggiPinAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTable table;
            ViaggiTableModel tableModel, pinTableModel;

            switch (e.getActionCommand()) {
                case NORD_PIN_COMMAND:
                    table = viaggiNordTable;
                    tableModel = ((ViaggiTableModel) viaggiNordTable.getModel());
                    pinTableModel = viaggiNordPinTableModel;
                    break;
                case SUD_PIN_COMMAND:
                    table = viaggiSudTable;
                    tableModel = sudTableModel;
                    pinTableModel = viaggiSudPinTableModel;
                    break;
                default:
                    return;
            }

            int selectedRow = table.getSelectedRow();
            if(selectedRow < 0) {
                return;
            }
            if(table.isEditing()) {
                table.getCellEditor().cancelCellEditing();
            }

            Viaggio v = tableModel.removeRow(selectedRow);
            pinTableModel.addRow(v);
        }
    };

    private ActionListener viaggiUnPinAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTable table;
            ViaggiTableModel tableModel, pinTableModel;

            switch (e.getActionCommand()) {
                case NORD_UNPIN_COMMAND:
                    table = viaggiNordPinTable;
                    tableModel = ((ViaggiTableModel) viaggiNordTable.getModel());
                    pinTableModel = viaggiNordPinTableModel;
                    break;
                case SUD_UNPIN_COMMAND:
                    table = viaggiSudPinTable;
                    tableModel = sudTableModel;
                    pinTableModel = viaggiSudPinTableModel;
                    break;
                default:
                    return;
            }

            int selectedRow = table.getSelectedRow();

            if(selectedRow < 0) {
                return;
            }

            if(table.isEditing()) {
                table.getCellEditor().cancelCellEditing();
            }

            Viaggio v = pinTableModel.removeRow(selectedRow);
            tableModel.addRow(v);
        }
    };

    private final ActionListener sudAddAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sudTableModel.addRow(null);
        }
    };

    private final ActionListener sudRemoveAction = new ActionListener() {
        @SuppressWarnings("Duplicates")
        @Override
        public void actionPerformed(ActionEvent arg0) {
            int selected = viaggiSudTable.getSelectedRow();
            if(selected < 0) {
                return;
            }

            if(viaggiSudTable.isEditing()){
                viaggiSudTable.getCellEditor().cancelCellEditing();
            }
            Viaggio rimosso = sudTableModel.removeRow(selected);
            try {
                dbs.rimuoviViaggio(rimosso);
            } catch (SQLException e) {
                logDatabaseError(e);
            }
        }
    };

    private final ActionListener nordAddAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            ((ViaggiTableModel) viaggiNordTable.getModel()).addRow(null);
        }
    };

    private final ActionListener nordRemoveAction = new ActionListener() {
        @SuppressWarnings("Duplicates")
        @Override
        public void actionPerformed(ActionEvent arg0) {
            ViaggiTableModel nordTableModel = ((ViaggiTableModel) viaggiNordTable.getModel());

            int selected = viaggiNordTable.getSelectedRow();
            if(selected < 0) {
                return;
            }

            if(viaggiNordTable.isEditing()){
                viaggiNordTable.getCellEditor().cancelCellEditing();
            }
            Viaggio rimosso = nordTableModel.removeRow(selected);
            try {
                dbs.rimuoviViaggio(rimosso);
            } catch (SQLException e) {
                logDatabaseError(e);
            }
        }
    };

    private AbstractAction openConfigAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(
                    () -> ConfigFrame.open(WindowConstants.DO_NOTHING_ON_CLOSE)
            );
        }
    };

    private void init(){
        this.setIconImage(Toolkit.getDefaultToolkit().createImage(ViaggiUtils.getMainIcon()));
        tableColumnModelListenerMap = new HashMap<>();

        logger = LogManager.getLogger(MainFrame.class);
        lastDateFromDb = null;
        try {
            logger.debug("Creating database connection...");
            dbs = DatabaseService.create();
            lastDateFromDb = dbs.getDataAggiornamento();
            camions = dbs.getCamion();
        }catch (SQLException e) {
            logger.fatal(e);
            String[] options = new String[] {"OK", "Impostazioni"};

            int ans = JOptionPane.showOptionDialog(rootPane,
                    "Connessione al server fallita",
                    "Errore",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if(ans == 1) {
                ConfigFrame.open(WindowConstants.EXIT_ON_CLOSE);
                return;
            } else {
                System.exit(1);
            }
        }

        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F12"), "openConfig");
        rootPane.getActionMap().put("openConfig", openConfigAction);


        setTitle(Consts.PROG_TITLE);
        setBounds(100, 100, 450, 300);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public MainFrame() {
        init();

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane pp = new JScrollPane(contentPane);
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        //nordTableModel = new ViaggiTableModel(Consts.VIAGGI_TM_TYPE_NORD, camions);
        sudTableModel = new ViaggiTableModel(Consts.VIAGGI_TYPE_SUD, camions);

        NorthPanel = new JPanel();
        contentPane.add(NorthPanel, BorderLayout.NORTH);

        lblDateSelection = new JLabel("Seleziona una data:");
        NorthPanel.add(lblDateSelection);

        formattedTextField = new CustomDateTextField();
        formattedTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if(code == KeyEvent.VK_ENTER){
                    loadDateFromInsertedFormattedTextField();
                } else {
                    if(e.getComponent().getBackground() == Color.RED){
                        e.getComponent().setBackground(Color.WHITE);
                    }
                }
            }
        });
        NorthPanel.add(formattedTextField);

        JButton btnFind = new JButton();
        try {
            Image searchIcon = ImageIO.read(getClass().getResource("/Icons/search16.png"));
            btnFind.setIcon(new ImageIcon(searchIcon));
        } catch (IOException e) {
            logger.warn("Failed to load button icon", e);
        }
        btnFind.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadDateFromInsertedFormattedTextField();
            }
        });
        NorthPanel.add(btnFind);

        JButton reloadButton = new JButton();
        try {
            Image reloadIcon = ImageIO.read(getClass().getResource("/Icons/reload16.png"));
            reloadButton.setIcon(new ImageIcon(reloadIcon));
        } catch (IOException e) {
            logger.warn("Failed to load button icon", e);
        }
        reloadButton.addActionListener(reloadAction);
        NorthPanel.add(reloadButton);

        JButton btnGoToLastDate = new JButton("Ultima data");
        btnGoToLastDate.addActionListener(goToLastDateAction);
        NorthPanel.add(btnGoToLastDate);

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

        JButton btnDelete = new JButton();
        try {
            Image deleteIcon = ImageIO.read(getClass().getResource("/Icons/delete16.png"));
            btnDelete.setIcon(new ImageIcon(deleteIcon));
        } catch (IOException e) {
            logger.warn("Failed to load button icon", e);
        }

        btnDelete.addActionListener(deleteThisDayAction);
        NorthPanel.add(btnDelete);

        JButton btnGestisciCamion = new JButton("Gestisci Camion");
        btnGestisciCamion.addActionListener(openCamionFrameAction);
        NorthPanel.add(btnGestisciCamion);

        JButton btnAggiungiGiornata = new JButton("Aggiungi Giornata");
        btnAggiungiGiornata.addActionListener(openNewDateFrameAction);
        NorthPanel.add(btnAggiungiGiornata);

        btnEsportaQuestaData = new JButton("Esporta questa data");
        btnEsportaQuestaData.addActionListener(dateExportAction);
        NorthPanel.add(btnEsportaQuestaData);


        TablePanel = new JPanel();
        TablePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        TablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        TablePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        contentPane.add(TablePanel, BorderLayout.CENTER);

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

        ordiniSalitaRimuoviButton = new JButton("-");
        panel.add(ordiniSalitaRimuoviButton);

        ordiniSalitaAggiungiButton = new JButton("+");
        panel.add(ordiniSalitaAggiungiButton);

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


        clientiTableSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, salitaPanel, discesaPanel);
        clientiTableSplitPane.setVerifyInputWhenFocusTarget(false);
        clientiTableSplitPane.setDividerSize(1);
        panel_5.add(clientiTableSplitPane);
        clientiTableSplitPane.setBorder(null);
        clientiTableSplitPane.setLayout(new BoxLayout(clientiTableSplitPane, BoxLayout.Y_AXIS));

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
        NoteRemoveButton.addActionListener(deleteNoteAction);
        panel_8.add(NoteRemoveButton);

        noteAddButton = new JButton("+");
        noteAddButton.addActionListener(aggiungiNotaAction);
        panel_8.add(noteAddButton);

        ordiniDiscesaButtonPanel = new JPanel();
        discesaPanel.add(ordiniDiscesaButtonPanel, BorderLayout.NORTH);
        ordiniDiscesaButtonPanel.setLayout(new BorderLayout(0, 0));

        lblNewLabel = new JLabel("DISCESA");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ordiniDiscesaButtonPanel.add(lblNewLabel);

        panel_4 = new JPanel();
        ordiniDiscesaButtonPanel.add(panel_4, BorderLayout.EAST);
        panel_4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        ordiniDiscesaRimuoviButton = new JButton("-");
        ordiniDiscesaRimuoviButton.addActionListener(removeOrderAction);
        ordiniDiscesaRimuoviButton.setActionCommand(ORDINI_DISCESA_COMMAND);

        panel_4.add(ordiniDiscesaRimuoviButton);

        ordiniDiscesaAggiungiButton = new JButton("+");
        ordiniDiscesaAggiungiButton.addActionListener(addOrderAction);
        ordiniDiscesaAggiungiButton.setActionCommand(ORDINI_DISCESA_COMMAND);

        panel_4.add(ordiniDiscesaAggiungiButton);

        scrollPane = new JScrollPane();
        discesaPanel.add(scrollPane, BorderLayout.CENTER);

        ordiniDiscesaTable = new OrdiniTable();
        ordiniDiscesaTable.setCellSelectionEnabled(true);
        ordiniDiscesaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordiniDiscesaTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ordiniDiscesaTableModel = new OrdiniTableModel();
        ordiniDiscesaTable.setModel(ordiniDiscesaTableModel);
        scrollPane.setViewportView(ordiniDiscesaTable);

        ordiniSalitaAggiungiButton.addActionListener(addOrderAction);
        ordiniSalitaAggiungiButton.setActionCommand(ORDINI_SALITA_COMMAND);

        ordiniSalitaRimuoviButton.addActionListener(removeOrderAction);
        ordiniSalitaRimuoviButton.setActionCommand(ORDINI_SALITA_COMMAND);

        try {
            lis = new OrdiniTableListener(dbs);
            lis.setUpdateWorkerListener(ordiniUpdateResultListener);
            ordiniSalitaTableModel.addTableModelListener(lis);
            ordiniDiscesaTableModel.addTableModelListener(lis);
            noteListener = new NoteTableListener(dbs);
            noteListener.setResultListener(noteUpdateResultListener);
            noteModel.addTableModelListener(noteListener);
        } catch (SQLException e1) {
            logDatabaseError(e1);
        }

        sudTablePanel = new JPanel();
        TablePanel.add(sudTablePanel);

        viaggiSudTable = new ViaggiJTable(Consts.VIAGGI_TYPE_SUD);
        TablePanel.add(viaggiSudTable);
        viaggiSudTable.setModel(sudTableModel);
        viaggiSudTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());
        viaggiSudTable.getModel().addTableModelListener(this);
        sudTablePanel.setLayout(new BorderLayout(0, 0));

        sudTableButtonPanel = new JPanel();
        sudTablePanel.add(sudTableButtonPanel, BorderLayout.NORTH);
        sudTableButtonPanel.setLayout(new BorderLayout(0, 0));

        panel_3 = new JPanel();
        TablePanel.add(panel_3);

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
        sudRemoveButton.addActionListener(sudRemoveAction);

        sudAddButton = new JButton("+");
        panel_2.add(sudAddButton);
        sudAddButton.addActionListener(sudAddAction);

        sudPinButton = new JButton("\u2193");
        sudPinButton.addActionListener(viaggiPinAction);
        sudPinButton.setActionCommand(SUD_PIN_COMMAND);
        panel_2.add(sudPinButton);

        sudUnpinButton = new JButton("\u2191");
        sudUnpinButton.addActionListener(viaggiUnPinAction);
        sudUnpinButton.setActionCommand(SUD_UNPIN_COMMAND);
        panel_2.add(sudUnpinButton);

        sudTableScrollPane = new JScrollPane(viaggiSudTable);
        sudTableScrollPane.setBorder(null);

        viaggiSudPinTable = new ViaggiJTable(Consts.VIAGGI_TYPE_SUD);
        viaggiSudPinTable.setTableHeader(null);
        viaggiSudPinTableModel = new ViaggiTableModel(Consts.VIAGGI_TYPE_SUD, camions);
        viaggiSudPinTableModel.addTableModelListener(viaggiPinnedTableListener);
        viaggiSudPinTable.setModel(viaggiSudPinTableModel);
        viaggiSudPinTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());
        sudPinScrollPane = new JScrollPane(viaggiSudPinTable);

        viaggiSudSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sudTableScrollPane, sudPinScrollPane);
        viaggiSudSplitPane.setDividerSize(3);
        sudTablePanel.add(viaggiSudSplitPane, BorderLayout.CENTER);

        nordTablePanel = new JPanel();
        TablePanel.add(nordTablePanel);

        viaggiNordTable = new ViaggiJTable(Consts.VIAGGI_TM_TYPE_NORD);
        TablePanel.add(viaggiNordTable);

        //TODO: viaggi nord table model definition goes here
        nordTablePanel.setLayout(new BorderLayout(0, 0));

        nordTableButtonPanel = new JPanel();
        nordTablePanel.add(nordTableButtonPanel, BorderLayout.NORTH);
        nordTableButtonPanel.setLayout(new BorderLayout(0, 0));

        lblNord = new JLabel("NORD");
        lblNord.setHorizontalAlignment(SwingConstants.CENTER);
        lblNord.setFont(new Font("Tahoma", Font.PLAIN, 15));
        nordTableButtonPanel.add(lblNord);

        panel_1 = new JPanel();

        nordRemoveButton = new JButton("-");
        nordRemoveButton.addActionListener(nordRemoveAction);

        nordAddButton = new JButton("+");
        nordAddButton.addActionListener(nordAddAction);

        nordPinButton = new JButton("\u2193");
        nordPinButton.addActionListener(viaggiPinAction);
        nordPinButton.setActionCommand(NORD_PIN_COMMAND);
        panel_2.add(nordPinButton);

        nordUnpinButton = new JButton("\u2191");
        nordUnpinButton.addActionListener(viaggiUnPinAction);
        nordUnpinButton.setActionCommand(NORD_UNPIN_COMMAND);
        panel_2.add(nordUnpinButton);

        panel_1.add(nordPinButton);
        panel_1.add(nordUnpinButton);
        panel_1.add(nordRemoveButton);
        panel_1.add(nordAddButton);
        nordTableButtonPanel.add(panel_1, BorderLayout.EAST);


        panel_3.setLayout(new BorderLayout(0, 0));

        nordTableScrollPane = new JScrollPane(viaggiNordTable);
        //nordTablePanel.add(nordTableScrollPane, BorderLayout.CENTER);

        viaggiNordPinTable = new ViaggiJTable(Consts.VIAGGI_TM_TYPE_NORD);
        viaggiNordPinTable.setTableHeader(null);
        viaggiNordPinTableModel = new ViaggiTableModel(Consts.VIAGGI_TM_TYPE_NORD, camions);
        viaggiNordPinTableModel.addTableModelListener(viaggiPinnedTableListener);
        viaggiNordPinTable.setModel(viaggiNordPinTableModel);
        viaggiNordPinTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());
        nordPinScrollPane = new JScrollPane(viaggiNordPinTable);
        viaggiNordSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, nordTableScrollPane, nordPinScrollPane);
        viaggiNordSplitPane.setDividerSize(3);

        nordTablePanel.add(viaggiNordSplitPane, BorderLayout.CENTER);

        TableColumnModelListener nordTableColumnListener = new ColumnChangeListener(viaggiNordTable, viaggiNordPinTable);
        TableColumnModelListener sudTableColumnListener = new ColumnChangeListener(viaggiSudTable, viaggiSudPinTable);

        viaggiNordTable.getColumnModel().addColumnModelListener(nordTableColumnListener);
        viaggiSudTable.getColumnModel().addColumnModelListener(sudTableColumnListener);

        tableColumnModelListenerMap.put(viaggiNordTable, nordTableColumnListener);
        tableColumnModelListenerMap.put(viaggiSudTable, sudTableColumnListener);

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

        assNonAssicuratiPanel = new JPanel();
        otherPanel.add(assNonAssicuratiPanel);
        assNonAssicuratiPanel.setLayout(new BorderLayout(0, 0));

        nonAssicuratiTxt = new JTextArea();
        nonAssicuratiTxt.setTabSize(2);
        nonAssicuratiTxt.setRows(4);
        nonAssicuratiTxt.setColumns(40);
        nonAssicuratiTxt.setLineWrap(true);
        nonAssicuratiTxt.setWrapStyleWord(true);
        nonAssicuratiTxt.setText("");

        nonAssicuratiScrollPane = new JScrollPane(nonAssicuratiTxt);
        assNonAssicuratiPanel.add(nonAssicuratiScrollPane);

        lblNonAssicurati = new JLabel("Non assicurati");
        assNonAssicuratiPanel.add(lblNonAssicurati, BorderLayout.NORTH);

        btnSalvaFermiE = new JButton("Salva fermi e non ass.");
        btnSalvaFermiE.addActionListener(salvaFermiENonAssAction);
        otherPanel.add(btnSalvaFermiE);

        southPanel = new JPanel(new BorderLayout());
        infoTextField = new MessageJLabel();
        infoTextField.setInfoMessage("Benvenuto");
        southPanel.add(infoTextField, BorderLayout.CENTER);
        contentPane.add(southPanel, BorderLayout.SOUTH);


        addListeners();
        setLocationByPlatform(true);

        loadDate(lastDateFromDb, MainFrame.RELOAD_STANDARD);
/*        try {
            updateCamionList();
        }catch (SQLException e) {
            logDatabaseError(e);
        }
        formattaTabelle();*/
//        try {
            /*reloadTableModel(lastDateFromDb, MainFrame.RELOAD_STANDARD);
            reloadOrdiniModel(lastDateFromDb);
            reloadNote(lastDateFromDb);*/
//        }catch (SQLException e){
//            logDatabaseError(e);
//        }


        /*panel.setBorder(new TitledBorder("panel"));
        panel_1.setBorder(new TitledBorder("panel_1"));
        panel_2.setBorder(new TitledBorder("panel_2"));
        panel_3.setBorder(new TitledBorder("panel_3"));
        panel_4.setBorder(new TitledBorder("panel_4"));
        panel_5.setBorder(new TitledBorder("panel_5"));
        panel_6.setBorder(new TitledBorder("panel_6"));
        panel_8.setBorder(new TitledBorder("panel_8"));
        assNonAssicuratiPanel.setBorder(new TitledBorder("assNonAssicuratiPanel"));
        clientiTableSplitPane.setBorder(new TitledBorder("clientiTableSplitPane"));
        discesaPanel.setBorder(new TitledBorder("discesaPanel"));
        nordTableButtonPanel.setBorder(new TitledBorder("nordTableButtonPanel"));
        nordTablePanel.setBorder(new TitledBorder("nordTablePanel"));
        noteButtonPanel.setBorder(new TitledBorder("noteButtonPanel"));
        notePanel.setBorder(new TitledBorder("notePanel"));
        ordiniDiscesaButtonPanel.setBorder(new TitledBorder("ordiniDiscesaButtonPanel"));
        ordiniSalitaTableButtonPanel.setBorder(new TitledBorder("ordiniSalitaTableButtonPanel"));
        otherPanel.setBorder(new TitledBorder("otherPanel"));
        salitaPanel.setBorder(new TitledBorder("salitaPanel"));
        sudTablePanel.setBorder(new TitledBorder("sudTablePanel"));
        sudTableButtonPanel.setBorder(new TitledBorder("sudTableButtonPanel"));
        assNonAssicuratiPanel.setBorder(new TitledBorder("assNonAssicuratiPanel"));
        titlePanel.setBorder(new TitledBorder("titlePanel"));
        contentPane.setBorder(new TitledBorder("contentPane"));
        NorthPanel.setBorder(new TitledBorder("NorthPanel"));
        TablePanel.setBorder(new TitledBorder("TablePanel"));*/

        //southPanel.setBorder(new TitledBorder("southPanel"));
        //sudTableScrollPane.setBorder(new TitledBorder("sudTableScrollPane"));
        //nordTableScrollPane.setBorder(new TitledBorder("nordTableScrollPane"));
        //clientiTableScrollPane.setBorder(new TitledBorder("clientiTableScrollPane"));
        //scrollPane.setBorder(new TitledBorder("scrollPane"));
        //fermiScrollPane.setBorder(new TitledBorder("fermiScrollPane"));
        //nonAssicuratiScrollPane.setBorder(new TitledBorder("nonAssicuratiScrollPane"));
        //noteScrollPane.setBorder(new TitledBorder("noteScrollPane"));
        //viaggiSplitPane.setBorder(new TitledBorder("viaggiSplitPane"));
        //clientiTableSplitPane.setBorder(new TitledBorder("clientiTableSplitPane"));
    }


    @Override
    public void updateCamionList() throws SQLException {
        camions = dbs.getCamion();
        ((ViaggiTableModel) viaggiNordTable.getModel()).setCamions(camions);
        sudTableModel.setCamions(camions);
        formattaTabelle();
    }


    @Override
    public void reloadTableModel(Date d, int option) throws SQLException {
        viaggiNordTable.setModel(new ViaggiTableModel(Consts.VIAGGI_TM_TYPE_NORD, camions));
        viaggiNordTable.getModel().addTableModelListener(this);
        viaggiNordTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());

        ViaggiTableModel nordTableModel = ((ViaggiTableModel) viaggiNordTable.getModel());

        if (option == RELOAD_RESETCONNECTION) {
            dbs.closeConnection();
            dbs.openConnection();
        }

        Vector<Viaggio> nord = dbs.getViaggiBy(Viaggio.NORD, d);
        Vector<Viaggio> sud = dbs.getViaggiBy(Viaggio.SUD, d);
        Vector<Viaggio> sudNormal = new Vector<>();
        Vector<Viaggio> nordNormal = new Vector<>();
        Vector<Viaggio> nordPinned = new Vector<>();
        Vector<Viaggio> sudPinned = new Vector<>();

        for (Viaggio viaggio : nord) {
            if(viaggio.isPinned()) {
                nordPinned.add(viaggio);
            } else {
                nordNormal.add(viaggio);
            }
        }

        for (Viaggio viaggio : sud) {
            if(viaggio.isPinned()) {
                sudPinned.add(viaggio);
            } else {
                sudNormal.add(viaggio);
            }
        }

        sudTableModel.setData(sudNormal);
        sudTableModel.setCurrentDate(d);
        viaggiSudPinTableModel.setData(sudPinned);
        viaggiSudPinTableModel.setCurrentDate(d);

        nordTableModel.setData(nordNormal);
        nordTableModel.setCurrentDate(d);
        viaggiNordPinTableModel.setData(nordPinned);
        viaggiNordPinTableModel.setCurrentDate(d);
    }

    @Override
    public void reloadOrdiniModel(Date d) throws SQLException {
        OrdiniTableModel tmSalita = (OrdiniTableModel) ordiniSalitaTable.getModel();
        OrdiniTableModel tmDiscesa = (OrdiniTableModel) ordiniDiscesaTable.getModel();

        Vector<Ordine> salite = new Vector<>();
        Vector<Ordine> discese = new Vector<>();

        Vector<Ordine> fromDB = dbs.getOrdiniByDate(d);
        for(Ordine o : fromDB){
            if(o.getType().toLowerCase().compareTo(Ordine.SALITA.toLowerCase()) == 0){
                salite.addElement(o);
            }else if(o.getType().toLowerCase().compareTo(Ordine.DISCESA.toLowerCase()) == 0){
                discese.addElement(o);
            }
        }
        tmSalita.setData(salite);
        tmDiscesa.setData(discese);

    }

    @Override
    public void reloadNote(Date d) throws SQLException {
        fermiTxt.setText("");
        nonAssicuratiTxt.setText("");
        Vector<Nota> fromDB;
        Vector<Nota> toNoteTable = new Vector<>();
        NoteTableModel tm = (NoteTableModel) noteTable.getModel();

        fromDB = dbs.getNoteByDate(d);
        for(Nota n : fromDB){
            if(n.getTipo().compareTo(Nota.NOTA) == 0){
                toNoteTable.addElement(n);
            }else if(n.getTipo().compareTo(Nota.FERMI) == 0){
                fermiNota = n;
                fermiTxt.setText(fermiNota.getTesto());
            }else if(n.getTipo().compareTo(Nota.NONASS) == 0){
                nonAssNota = n;
                nonAssicuratiTxt.setText(nonAssNota.getTesto());
            }
        }
        tm.setData(toNoteTable);
    }

    @Override
    public void loadDate(Date d, int mode) {
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

        try {
            reloadTableModel(d, mode);
            reloadOrdiniModel(d);
            reloadNote(d);
            currentDate = d;
            selectedDateLbl.setText(ViaggiUtils.createStringFromDate(currentDate));

            formattaTabelle();
            updateCamionList();
            resizePinTables();
        } catch (SQLException e) {
            logDatabaseError(e);
        }
    }

    private void loadDateFromInsertedFormattedTextField() {
        String text = formattedTextField.getText();
        Date d = null;
        try{
            d = ViaggiUtils.checkAndCreateDate(text, "/", false);
        }catch(NumberFormatException ex){
            logger.info(ex.getMessage(), ex);
            formattedTextField.setBackground(Color.RED);
        }
        if(d != null){
            loadDate(d, MainFrame.RELOAD_STANDARD);

        }else{
            formattedTextField.setBackground(Color.RED);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int col = e.getColumn();
        int row = e.getFirstRow();

        if(col>=0 || e.getType() > 0) {
            ViaggiTableModel tm = (ViaggiTableModel) e.getSource();
            Viaggio v;
            if(e.getType() == TableModelEvent.UPDATE){
                logger.info("RIGA MODIFICATA: "+ row +"   COLONNA MODIFICATA: " + col);
                v = tm.getElementAt(row);
                ViaggiUpdateWorker.connect(dbs)
                        .update(v, col)
                        .onResult(new UpdateWorkerAdapter<Viaggio>() {
                            @Override
                            public void onUpdate(Viaggio updated, int col) {
                                String value = ViaggiUtils.getViaggioValueByColumnIndex(updated, col);
                                String columnName = Viaggio.NORD.equals(updated.getPosizione())
                                        ? viaggiNordTable.getColumnName(col)
                                        : viaggiSudTable.getColumnName(col);

                                notifyRowUpdated("Viaggi " + updated.getPosizione(), columnName, value, updated.getData().toString());
                            }

                            @Override
                            public void onError(Exception error) {
                                MainFrame.this.onError(error);
                            }
                        })
                        .execute();
            }else if(e.getType() == TableModelEvent.INSERT){
                Viaggio nuovo = tm.getElementAt(row-1);
                if(nuovo.getId() >= 0) { //Inserted a row yet in database, so we haven't to insert into database
                    return;
                }

                logger.info(nuovo.toString());

                ViaggiUpdateWorker.connect(dbs)
                        .insert(nuovo)
                        .onResult(new UpdateWorkerAdapter<Viaggio>() {
                            @Override
                            public void onError(Exception error) {
                                MainFrame.this.onError(error);
                            }
                            @Override
                            public void onInsert(Viaggio inserted, long newId) {
                                logger.info("LAST ID INSERTED: "+newId);

                                if(newId > 0){
                                    tm.getElementAt(row-1).setId(newId);
                                    logger.info(tm.getElementAt(row-1).toString());
                                }else {
                                    onError(new IllegalArgumentException("Problemi nell'id del nuovo viaggio"));
                                }
                                JTable t = tm.getType() == Consts.VIAGGI_TM_TYPE_NORD ? viaggiNordTable : viaggiSudTable;
                                /*t.requestFocus();
                                t.changeSelection(row-1, 0, false, false);
                                t.editCellAt(row-1, 0);*/
                                if(tm.getType() == Consts.VIAGGI_TM_TYPE_NORD) {
                                    viaggiNordTable.requestFocus();
                                    viaggiNordTable.changeSelection(row-1, 0, false, false);
                                    viaggiNordTable.editCellAt(row-1, 0);
                                } else {
                                    viaggiSudTable.changeSelection(row-1, 0, false, false);
                                    viaggiSudTable.editCellAt(row-1, 0);
                                    viaggiSudTable.requestFocus();
                                }
                                logger.info("ADD ROW!");
                                //logger.info(modifiche.toString());

                                notifyRowInserted("Viaggi " + inserted.getPosizione(), ""+newId, inserted.getData().toString());
                            }
                        })
                        .execute();
            }else if(e.getType() == TableModelEvent.DELETE){
                logger.info("ROW DELETED!");
            }
        }
    }


    private void formattaTabelle(){
        //viaggiSplitPane.setDividerLocation(0.5);
        viaggiSplitPane.setResizeWeight(0.5);

        viaggiNordTable.doTableLayout();
        viaggiSudTable.doTableLayout();
        viaggiNordPinTable.doTableLayout();
        viaggiSudPinTable.doTableLayout();

        ordiniDiscesaTable.doTableLayout();
        ordiniSalitaTable.doTableLayout();
    }

    private void resizePinTables() {
        int h = viaggiNordSplitPane.getHeight();
        //logger.debug("SIZE" + h);

        int maxRowCount = Math.max(viaggiSudPinTableModel.getRowCount(), viaggiNordPinTableModel.getRowCount());
        maxRowCount = maxRowCount + 2;
        viaggiNordSplitPane.setDividerLocation(h - (20 * maxRowCount));
        viaggiSudSplitPane.setDividerLocation(h - (20 * maxRowCount));
        //viaggiSudSplitPane.setResizeWeight(0);
    }

    private void addListeners(){
/*        this.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent arg0) {
                Component source = (Component) arg0.getComponent();


				*//*int newWidth = source.getWidth();
				int newHeight = CenterPanel.getHeight()-10;

				//clientiNoteTablePanel.getWidth();
				//clientiNoteTablePanel.getHeight();

				if(arg0.getNewState() == Frame.MAXIMIZED_BOTH){
					clientiTableSplitPane.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight/3.5)));
					nordTablePanel.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight)));
					sudTablePanel.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight)));
					notePanel.setPreferredSize(new Dimension((newWidth/3)-20, CenterPanel.getHeight()-((newHeight/2) + ((int)(newHeight/3.5)))));
					noteScrollPane.setPreferredSize(new Dimension((newWidth/3)-20, CenterPanel.getHeight()-((newHeight/2) + ((int)(newHeight/3.5)))));
				}
				*//*
            }
        });*/


        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                Component source = arg0.getComponent();
                int newWidth = source.getWidth();
                //int newHeight = TablePanel.getHeight()-10;
                //int newHeight = source.getHeight();
                int newHeight = TablePanel.getHeight();
                TablePanel.setSize(new Dimension(newWidth, newHeight));

                //contentPane.setMaximumSize(source.getSize());

                clientiTableSplitPane.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight/3.5)));

                nordTablePanel.setPreferredSize(new Dimension((newWidth/3)-10, (newHeight/2) + (int)(newHeight/3)));
                sudTablePanel.setPreferredSize(new Dimension((newWidth/3)-10, (newHeight/2) + (int)(newHeight/3)));
                //viaggiSplitPane.setPreferredSize(new Dimension((newWidth/2), (newHeight/2) + (int)(newHeight/3)));

                notePanel.setPreferredSize(new Dimension((newWidth/3) - 20, TablePanel.getHeight()-((newHeight/2) + ((int)(newHeight/3.5) + 20))));
                //noteScrollPane.setPreferredSize(new Dimension((newWidth/3)-20, CenterPanel.getHeight()-((newHeight/2) + ((int)(newHeight/3.5) +20 ))));
                resizePinTables();
            }

        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                try {
                    dbs.closeConnection();
                    DatabaseHelperChannel.getInstance().disconnect();
                    //lis.closeConnection();
                    //noteListener.closeConnection();
                } catch (SQLException | RemoteException e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }

        });

    }

    private void logDatabaseError(SQLException e){
        logger.error(e.getMessage(), e);
        e.printStackTrace();
        Msg.error(this, "Errore di connessione al database"
                + "\nCodice errore: "+e.getErrorCode()+"\n"+e.getMessage());
    }

    private void notifyRowUpdated(String table, String columnName, String newValue, String date) {
        logger.debug("Updated row on table: " + table + " Col: " + columnName + " Value: " + newValue);
        String updateMessage = table + ": valore " + newValue  + " per la colonna " + columnName + " salvato con successo.";
        try {
            DatabaseHelperChannel.getInstance().notifyRowUpdated(table, date);
        } catch (RemoteException e) {
            logger.error("DBH" , e);
            updateMessage += " Tuttavia la modifica non sarà notificata agli altri clients a causa di un errore.";
        }

        infoTextField.setInfoMessage(updateMessage);
    }

    private void notifyRowUpdated(String table, String date, String messageCustomText) {
        logger.debug("Updated row on table: " + table + "Update message: " + messageCustomText);
        String updateMessage = table + ": " + messageCustomText;
        try {
            DatabaseHelperChannel.getInstance().notifyRowUpdated(table, date);
        } catch (RemoteException e) {
            logger.error("DBH" , e);
            updateMessage += " Tuttavia la modifica non sarà notificata agli altri clients a causa di un errore.";
        }

        infoTextField.setInfoMessage(updateMessage);
    }

    private void notifyRowInserted(String table, String newId, String date) {
        logger.info("Inserted new row in table: " + table + " With id: " + newId);
        String insertMessage = table + ": nuova riga con id " + newId + " salvata con successo.";
        try {
            DatabaseHelperChannel.getInstance().notifyRowInserted(table, date);
        } catch (RemoteException e) {
            logger.error("DBH", e);
            insertMessage += " Tuttavia la modifica non sarà notificata agli altri clients a causa di un errore.";
        }

        infoTextField.setInfoMessage(insertMessage);
    }

    private void onError(Exception error) {
        logger.error(error.getMessage(), error);

        if (error instanceof SQLException) {
            infoTextField.setErrorMessage("Errore nel salvataggio dell'ultimo valore. Clicca qui per informazioni");
        } else if (error instanceof RemoteException) {
            infoTextField.setWarnMessage("Impossibile notificare le modifiche agli altri clients");
        }

        infoTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Msg.error(getParent(), error.getMessage());
            }
        });
    }

    //LISTENER FOR MESSAGES FROM DatabaseHelper
    @Override
    public void onRowInserted(String tableName, String date, String who, long timestamp) {
        long elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000;
        infoTextField.setUploadMessage("Nuova riga inserita nella tabella " + tableName + " da " + who +
                " " + elapsedSeconds + " secondi fa. Clicca qui per aggiornare");
        infoTextField.addMouseListener(aggiornaOnClickAdapter);
    }

    @Override
    public void onRowUpdated(String tableName, String date, String who, long timestamp) {
        long elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000;
        infoTextField.setUploadMessage("Valore modificato nella tabella " + tableName + " da " + who +
                " " + elapsedSeconds + " secondi fa. Clicca qui per aggiornare");
        infoTextField.addMouseListener(aggiornaOnClickAdapter);    }

    @Override
    public void onDateAdded(String date, String who, long timestamp) {
        Date date1 = ViaggiUtils.checkAndCreateDate(date, "-", true);
        long elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000;
        infoTextField.setUploadMessage("Nuova data aggiunta da " + who + " " + elapsedSeconds
                + " secondi fa. Clicca qui per aggiornare");
        infoTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadDate(date1, RELOAD_STANDARD);
                infoTextField.clearMessage();
            }
        });
    }

    @Override
    public void onDateRemoved(String date, String who, long timestamp) {
        Date date1 = ViaggiUtils.checkAndCreateDate(date, "-", true);
        if(date1.equals(currentDate)) {
            EventQueue.invokeLater(() -> {
                Msg.warn(MainFrame.this, "Questa data è stata cancellata da " + who);
                try {
                    lastDateFromDb = dbs.getDataAggiornamento();
                    loadDate(lastDateFromDb, RELOAD_RESETCONNECTION);
                } catch (SQLException e) {
                    logDatabaseError(e);
                }
            });
        }
    }

    public MessageJLabel getMessageField() {
        return infoTextField;
    }

    class ColumnChangeListener implements TableColumnModelListener
    {
        JTable sourceTable;
        JTable targetTable;

        public ColumnChangeListener(JTable source, JTable target)
        {
            this.sourceTable = source;
            this.targetTable = target;
        }

        public void columnAdded(TableColumnModelEvent e) {}
        public void columnSelectionChanged(ListSelectionEvent e) {}
        public void columnRemoved(TableColumnModelEvent e) {}
        public void columnMoved(TableColumnModelEvent e) {}

        public void columnMarginChanged(ChangeEvent e) {
            TableColumnModel sourceModel = sourceTable.getColumnModel();
            TableColumnModel targetModel = targetTable.getColumnModel();
            TableColumnModelListener listener = tableColumnModelListenerMap.get(targetTable);

            targetModel.removeColumnModelListener(listener);

            for (int i = 0; i < sourceModel.getColumnCount(); i++)
            {
                targetModel.getColumn(i).setPreferredWidth(sourceModel.getColumn(i).getWidth());
            }

            targetModel.addColumnModelListener(listener);
        }
    }
}