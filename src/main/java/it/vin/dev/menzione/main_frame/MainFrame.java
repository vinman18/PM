package it.vin.dev.menzione.main_frame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.TableColumnModel;

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
import java.text.MessageFormat;
import java.util.*;


@SuppressWarnings("FieldCanBeLocal")
public class MainFrame extends JFrame implements TableModelListener, ReloadCallback, DatabaseHelperListener{
    private final boolean DEBUG_FRAME = false;

    private static final int RELOAD_STANDARD = 0;
    public static final int RELOAD_RESETCONNECTION = 1;

    public static final Font TAHOMA_DEFAULT_FONT = new Font("Tahoma", Font.PLAIN, 15);

    private static final String SUD_PIN_COMMAND = "sud_pin";
    private static final String NORD_PIN_COMMAND = "nord_pin";
    private static final String SUD_UNPIN_COMMAND = "sud_unpin";
    private static final String NORD_UNPIN_COMMAND = "nord_unpin";

    private static final String ORDINI_DISCESA_COMMAND = "ordine_discesa";
    private static final String ORDINI_SALITA_COMMAND = "ordine_salita";

    private static final String NORD_ADD_ROW_COMMAND = "nord_add_row";
    private static final String SUD_ADD_ROW_COMMAND = "sud_add_row";
    private static final String NORD_REMOVE_ROW_COMMAND = "nord_remove_row";
    private static final String SUD_REMOVE_ROW_COMMAND = "sud_remove_row";

    private ResourceBundle strings = ResourceBundle.getBundle("Localization/Strings");

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
//    private ViaggiTableModel viaggiSudPinTableModel;
    //    private ViaggiTableModel sudTableModel;
//    private OrdiniTableModel ordiniSalitaTableModel;
//    private OrdiniTableModel ordiniDiscesaTableModel;
    private OrdiniTable ordiniSalitaTable;
    private ViaggiJTable viaggiNordTable;
    private ViaggiJTable viaggiNordPinTable;
    private JSplitPane viaggiNordSplitPane;
    private JFormattedTextField formattedTextField = new CustomDateTextField();
    private JPanel northPanel = new JPanel();
    private JLabel lblDateSelection;
    private JPanel tablePanel;
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
    //    private NoteTableModel noteModel;
    private JPanel noteButtonPanel;
    private JLabel lblNote;
    private JButton noteRemoveButton;
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
            if(type == TableModelEvent.UPDATE && col == TableModelEvent.ALL_COLUMNS) {
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
                                infoTextField.setInfoMessage(strings.getString("mainframe.msg.move.viaggio.ok"));
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
//                JOptionPane.showMessageDialog(root, "Esportazione completata", "", JOptionPane.INFORMATION_MESSAGE);
                Msg.info(root, strings.getString("mainframe.msg.export.ok"));
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
                Msg.error(root, strings.getString("mainframe.msg.select.date"));
            }
        }
    };

    private final ActionListener removeOrderAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            OrdiniTable table = arg0.getActionCommand().equals(ORDINI_DISCESA_COMMAND)
                    ? ordiniDiscesaTable
                    : ordiniSalitaTable;

            int rowSel = table.getSelectedRow();
            int colSel = table.getSelectedColumn();

            OrdiniTableModel tm = (OrdiniTableModel) table.getModel();
            Ordine o = tm.removeRow(rowSel);
            try {
                dbs.rimuoviOrdine(o);
            } catch (SQLException e) {
                tm.addRow(o);
                logDatabaseError(e);
            }

            selectTableCell(table, rowSel, colSel);
        }
    };

    private final ActionListener aggiungiNotaAction = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            Component source = (Component) arg0.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(source);

            if (currentDate != null) {

                Nota nuovo = new Nota(currentDate, "", Nota.NOTA);

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

                                int rows = noteTable.getRowCount();

                                selectTableCell(noteTable, rows-1, 0);
                                notifyRowInserted("Note", ""+newId, inserted.getData().toString());
                            }
                        })
                        .execute();
            } else {
                Msg.error(root, strings.getString("mainframe.msg.select.date"));
            }

        }
    };

    private final ActionListener deleteNoteAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selRow = noteTable.getSelectedRow();
            int selCol = noteTable.getSelectedColumn();

            NoteTableModel tm = (NoteTableModel) noteTable.getModel();
            Nota n = tm.removeRow(selRow);
            try {
                dbs.rimuoviNota(n);
            } catch (SQLException e1) {
                tm.addRow(n);
                logDatabaseError(e1);
            }

            selectTableCell(noteTable, selRow, selCol);
        }
    };

    private final ActionListener salvaFermiENonAssAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            UpdateWorkerListener<Nota> resultListener = new UpdateWorkerListener<Nota>() {
                @SuppressWarnings("Duplicates")
                @Override
                public void onInsert(Nota inserted, long newId) {
                    String message = null;
                    if(Nota.FERMI.equals(inserted.getTipo())) {
                        message = strings.getString("mainframe.msg.fermi.nonass.save.ok");
                        fermiNota = inserted;
                        fermiTxt.setText(fermiNota.getTesto());
                    } else if(Nota.NONASS.equals(inserted.getTipo())) {
                        message = strings.getString("mainframe.msg.nonass.fermi.save.ok");
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
                        message = strings.getString("mainframe.msg.fermi.nonass.save.ok");
                        fermiNota = updated;
                        fermiTxt.setText(fermiNota.getTesto());
                    } else if(Nota.NONASS.equals(updated.getTipo())) {
                        message = strings.getString("mainframe.msg.nonass.fermi.save.ok");
                        nonAssNota = updated;
                        nonAssicuratiTxt.setText(nonAssNota.getTesto());
                    }

                    notifyRowUpdated("Fermi e Non Ass", updated.getData().toString(), message);
                }

                @Override
                public void onError(Exception error) {
                    MainFrame.this.onError(error);
                }
            };

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

            int resp1 = Msg.yesno(root, MessageFormat.format(strings.getString("mainframe.msg.date.delete.confirm.1"), currentDate.toString()));

            if(resp1 == JOptionPane.YES_OPTION) {
                int resp2 = Msg.yesno(root, MessageFormat.format(strings.getString("mainframe.msg.date.delete.confirm.2"), currentDate.toString()));

                if(resp2 == JOptionPane.YES_OPTION) {
                    try {
                        dbs.deleteDate(currentDate);
                        DatabaseHelperChannel.getInstance().notifyDateRemoved(currentDate.toString());
                        loadDate(dbs.getDataAggiornamento(), RELOAD_RESETCONNECTION);
                    } catch (SQLException e1) {
                        logDatabaseError(e1);
                    } catch (RemoteException e1) {
                        infoTextField.setWarnMessage(strings.getString("database.helper.communication.fail"));
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
                    pinTableModel = (ViaggiTableModel) viaggiNordPinTable.getModel();
                    break;
                case SUD_PIN_COMMAND:
                    table = viaggiSudTable;
                    tableModel = (ViaggiTableModel) viaggiSudTable.getModel();
                    pinTableModel = (ViaggiTableModel) viaggiSudPinTable.getModel();
                    break;
                default:
                    return;
            }

            int selectedRow = table.getSelectedRow();
            int selectedColumn = table.getSelectedColumn();

            if(selectedRow < 0) {
                return;
            }
            if(table.isEditing()) {
                table.getCellEditor().cancelCellEditing();
            }

            Viaggio v = tableModel.removeRow(selectedRow);
            pinTableModel.addRow(v);

            selectTableCell(table, selectedRow, selectedColumn);
        }
    };

    private ActionListener viaggiUnPinAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTable pinTable;
            ViaggiTableModel tableModel, pinTableModel;

            switch (e.getActionCommand()) {
                case NORD_UNPIN_COMMAND:
                    pinTable = viaggiNordPinTable;
                    tableModel = ((ViaggiTableModel) viaggiNordTable.getModel());
                    pinTableModel = (ViaggiTableModel) viaggiNordPinTable.getModel();
                    break;
                case SUD_UNPIN_COMMAND:
                    pinTable = viaggiSudPinTable;
                    tableModel = (ViaggiTableModel) viaggiSudTable.getModel();
                    pinTableModel = (ViaggiTableModel) viaggiSudPinTable.getModel();
                    break;
                default:
                    return;
            }

            int selectedRow = pinTable.getSelectedRow();
            int selectedColumn = pinTable.getSelectedColumn();

            if(selectedRow < 0) {
                return;
            }

            if(pinTable.isEditing()) {
                pinTable.getCellEditor().cancelCellEditing();
            }

            Viaggio v = pinTableModel.removeRow(selectedRow);
            tableModel.addRow(v);

            selectTableCell(pinTable, selectedRow, selectedColumn);
        }
    };

/*    private final ActionListener sudAddAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sudTableModel.addRow(null);
        }
    };*/

    private final ActionListener viaggiRowsAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            String actionCommand = arg0.getActionCommand();
            ViaggiJTable table;

            switch (actionCommand) {
                case NORD_REMOVE_ROW_COMMAND:
                case NORD_ADD_ROW_COMMAND:
                    table = viaggiNordTable; break;
                case SUD_REMOVE_ROW_COMMAND:
                case SUD_ADD_ROW_COMMAND:
                    table = viaggiSudTable; break;
                default:
                    return;
            }

            switch (actionCommand) {
                case NORD_ADD_ROW_COMMAND:
                case SUD_ADD_ROW_COMMAND:
                    ((ViaggiTableModel) table.getModel()).addRow(null);
                    break;
                case NORD_REMOVE_ROW_COMMAND:
                case SUD_REMOVE_ROW_COMMAND:
                    int selectedRow = table.getSelectedRow();
                    int selectedColumn = table.getSelectedColumn();

                    if(selectedRow < 0) {
                        return;
                    }

                    if(table.isEditing()){
                        table.getCellEditor().cancelCellEditing();
                    }

                    Viaggio removed = ((ViaggiTableModel) table.getModel()).removeRow(selectedRow);
                    try {
                        dbs.rimuoviViaggio(removed);
                    } catch (SQLException e) {
                        ((ViaggiTableModel) table.getModel()).addRow(removed);
                        logDatabaseError(e);
                    }

                    selectTableCell(table, selectedRow, selectedColumn);
                    break;
            }

        }
    };

    /*private final ActionListener viaggiAddRowAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            String actionCommand = arg0.getActionCommand();
            ViaggiTableModel tm;

            switch (actionCommand) {
                case NORD_ADD_ROW_COMMAND:
                    tm = (ViaggiTableModel) viaggiNordTable.getModel(); break;
                case SUD_ADD_ROW_COMMAND:
                    tm = (ViaggiTableModel) viaggiSudTable.getModel(); break;
                default:
                    return;
            }

            tm.addRow(null);
        }
    };*/

    /*private final ActionListener nordRemoveAction = new ActionListener() {
        @SuppressWarnings("Duplicates")
        @Override
        public void actionPerformed(ActionEvent arg0) {
            int selected = viaggiNordTable.getSelectedRow();
            if(selected < 0) {
                return;
            }

            if(viaggiNordTable.isEditing()){
                viaggiNordTable.getCellEditor().cancelCellEditing();
            }
            Viaggio rimosso = ((ViaggiTableModel) viaggiNordTable.getModel()).removeRow(selected);
            try {
                dbs.rimuoviViaggio(rimosso);
            } catch (SQLException e) {
                logDatabaseError(e);
            }
        }
    };*/

    private AbstractAction openConfigAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(
                    () -> ConfigFrame.open(WindowConstants.DO_NOTHING_ON_CLOSE)
            );
        }
    };

    private void init() throws SQLException {
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
            //String[] options = new String[] {"OK", "Impostazioni"};

            String[] options = strings.getString("mainframe.msg.options.ok.settings").split(",");

            int ans = Msg.options(
                    rootPane,
                    strings.getString("generic.server.connection.fail"),
                    strings.getString("generic.error"),
                    JOptionPane.ERROR_MESSAGE,
                    options
            );

            if(ans == 1) {
                ConfigFrame.open(WindowConstants.EXIT_ON_CLOSE);
                throw new SQLException(e);
            } else {
                System.exit(1);
            }
        }

        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F12"), "openConfig");
        rootPane.getActionMap().put("openConfig", openConfigAction);

        setTitle(strings.getString("app.title") + " - " + strings.getString("app.version"));
        setBounds(100, 100, 450, 300);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public MainFrame() {
        try {
            init();
        } catch (SQLException e) {
            return;
        }

        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
//        JScrollPane pp = new JScrollPane(contentPane);
        setContentPane(contentPane);

        //nordTableModel = new ViaggiTableModel(Consts.VIAGGI_TM_TYPE_NORD, camions);
//        sudTableModel = new ViaggiTableModel(Consts.VIAGGI_TM_TYPE_SUD);

        contentPane.add(northPanel, BorderLayout.NORTH);
        lblDateSelection = ViaggiFrameUtils.newJLabel(strings.getString("mainframe.label.date.selection"));
        northPanel.add(lblDateSelection);

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
        northPanel.add(formattedTextField);

        JButton btnFind = ViaggiFrameUtils.newIconButton(
                "/Icons/search16.png",
                strings.getString("mainframe.button.find"),
                e -> loadDateFromInsertedFormattedTextField(),
                null
        );
        northPanel.add(btnFind);

        JButton reloadButton = ViaggiFrameUtils.newIconButton(
                "/Icons/reload16.png",
                strings.getString("mainframe.button.reload"),
                reloadAction,
                null
        );

        northPanel.add(reloadButton);

        JButton btnGoToLastDate = ViaggiFrameUtils.newButton(
                strings.getString("mainframe.button.last.date"),
                goToLastDateAction,
                null
        );
        northPanel.add(btnGoToLastDate);

        titlePanel = new JPanel();
        northPanel.add(titlePanel);

        lblDataSelezionata = ViaggiFrameUtils.newJLabel(
                strings.getString("mainframe.label.date.selected"),
                TAHOMA_DEFAULT_FONT
        );
        titlePanel.add(lblDataSelezionata);

        selectedDateLbl = ViaggiFrameUtils.newJLabel(
                "",
                TAHOMA_DEFAULT_FONT
        );
        titlePanel.add(selectedDateLbl);

        giornoSettLabel = ViaggiFrameUtils.newJLabel(
                "",
                TAHOMA_DEFAULT_FONT
        );
        titlePanel.add(giornoSettLabel);

        JButton btnDelete = ViaggiFrameUtils.newIconButton(
                "/Icons/delete16.png",
                strings.getString("mainframe.button.date.delete"),
                deleteThisDayAction,
                null
        );
        northPanel.add(btnDelete);

        JButton btnGestisciCamion = ViaggiFrameUtils.newButton(
                strings.getString("mainframe.button.camion.managment"),
                openCamionFrameAction,
                null
        );
        northPanel.add(btnGestisciCamion);

        JButton btnAggiungiGiornata = ViaggiFrameUtils.newButton(
                strings.getString("mainframe.button.date.add"),
                openNewDateFrameAction,
                null
        );
        northPanel.add(btnAggiungiGiornata);

        btnEsportaQuestaData = ViaggiFrameUtils.newButton(
                strings.getString("mainframe.button.date.export"),
                dateExportAction,
                null
        );
        northPanel.add(btnEsportaQuestaData);


        tablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        tablePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        if(DEBUG_FRAME) {
            contentPane.add(new JScrollPane(tablePanel), BorderLayout.CENTER);
        } else {
            contentPane.add(tablePanel, BorderLayout.CENTER);
        }

        salitaPanel = new JPanel(new BorderLayout(0, 0));
        ordiniSalitaTableButtonPanel = new JPanel(new BorderLayout(0, 0));
        salitaPanel.add(ordiniSalitaTableButtonPanel, BorderLayout.NORTH);

        lblOrdini = ViaggiFrameUtils.newJLabel(strings.getString("mainframe.label.order.up"), TAHOMA_DEFAULT_FONT);
        lblOrdini.setHorizontalAlignment(SwingConstants.CENTER);
        ordiniSalitaTableButtonPanel.add(lblOrdini, BorderLayout.CENTER);

        panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        ordiniSalitaTableButtonPanel.add(panel, BorderLayout.EAST);

        ordiniSalitaRimuoviButton = ViaggiFrameUtils.newButton(
                "-",
                removeOrderAction,
                ORDINI_SALITA_COMMAND
        );
        panel.add(ordiniSalitaRimuoviButton);

        ordiniSalitaAggiungiButton = ViaggiFrameUtils.newButton(
                "+",
                addOrderAction,
                ORDINI_SALITA_COMMAND
        );
        panel.add(ordiniSalitaAggiungiButton);

        ordiniSalitaTable = new OrdiniTable();
        ordiniSalitaTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ordiniSalitaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordiniSalitaTable.setCellSelectionEnabled(true);
        ordiniSalitaTable.setModel(new OrdiniTableModel());
//        tablePanel.add(ordiniSalitaTable);

        clientiTableScrollPane = new JScrollPane(ordiniSalitaTable);
//        clientiTableScrollPane.setPreferredSize(new Dimension(452, 200));
        salitaPanel.add(clientiTableScrollPane);

        discesaPanel = new JPanel(new BorderLayout(0, 0));
//        discesaPanel.setPreferredSize(new Dimension(452, 200));

        ordiniDiscesaButtonPanel = new JPanel(new BorderLayout(0, 0));
        discesaPanel.add(ordiniDiscesaButtonPanel, BorderLayout.NORTH);

        lblNewLabel = ViaggiFrameUtils.newJLabel(strings.getString("mainframe.label.order.down"), TAHOMA_DEFAULT_FONT);
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ordiniDiscesaButtonPanel.add(lblNewLabel);

        panel_4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        ordiniDiscesaButtonPanel.add(panel_4, BorderLayout.EAST);

        ordiniDiscesaRimuoviButton = ViaggiFrameUtils.newButton(
                "-",
                removeOrderAction,
                ORDINI_DISCESA_COMMAND
        );
        panel_4.add(ordiniDiscesaRimuoviButton);

        ordiniDiscesaAggiungiButton = ViaggiFrameUtils.newButton(
                "+",
                addOrderAction,
                ORDINI_DISCESA_COMMAND
        );
        panel_4.add(ordiniDiscesaAggiungiButton);

        scrollPane = new JScrollPane();
        discesaPanel.add(scrollPane, BorderLayout.CENTER);

        ordiniDiscesaTable = new OrdiniTable();
        ordiniDiscesaTable.setCellSelectionEnabled(true);
        ordiniDiscesaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordiniDiscesaTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ordiniDiscesaTable.setModel(new OrdiniTableModel());

        lis = new OrdiniTableListener(dbs);
        lis.setUpdateWorkerListener(ordiniUpdateResultListener);
        ordiniSalitaTable.getModel().addTableModelListener(lis);
        ordiniDiscesaTable.getModel().addTableModelListener(lis);

        scrollPane.setViewportView(ordiniDiscesaTable);

        clientiTableSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, salitaPanel, discesaPanel);
        clientiTableSplitPane.setVerifyInputWhenFocusTarget(false);
        clientiTableSplitPane.setDividerSize(1);
        clientiTableSplitPane.setBorder(null);
        clientiTableSplitPane.setLayout(new BoxLayout(clientiTableSplitPane, BoxLayout.Y_AXIS));

        panel_5 = new JPanel(new BorderLayout(0, 0));
        tablePanel.add(panel_5);
        panel_5.add(clientiTableSplitPane);

        notePanel = new JPanel(new BorderLayout(0, 0));
        panel_5.add(notePanel, BorderLayout.SOUTH);

        noteScrollPane = new JScrollPane();
        notePanel.add(noteScrollPane);

        noteTable = new JTable();
        noteTable.setModel(new NoteTableModel());
        noteListener = new NoteTableListener(dbs);
        noteListener.setResultListener(noteUpdateResultListener);
        noteTable.getModel().addTableModelListener(noteListener);

        noteScrollPane.setViewportView(noteTable);

        noteButtonPanel = new JPanel(new BorderLayout(0, 0));
        notePanel.add(noteButtonPanel, BorderLayout.NORTH);

        lblNote = ViaggiFrameUtils.newJLabel(strings.getString("mainframe.label.note"));
        lblNote.setHorizontalAlignment(SwingConstants.CENTER);
        noteButtonPanel.add(lblNote, BorderLayout.CENTER);

        panel_8 = new JPanel();
        noteButtonPanel.add(panel_8, BorderLayout.EAST);

        noteRemoveButton = ViaggiFrameUtils.newButton(
                "-",
                deleteNoteAction,
                null
        );
        panel_8.add(noteRemoveButton);

        noteAddButton = ViaggiFrameUtils.newButton(
                "+",
                aggiungiNotaAction,
                null
        );
        panel_8.add(noteAddButton);

        sudTablePanel = new JPanel(new BorderLayout(0, 0));
//        tablePanel.add(sudTablePanel);

        viaggiSudTable = new ViaggiJTable(Consts.VIAGGI_TM_TYPE_SUD);
//        tablePanel.add(viaggiSudTable);
        viaggiSudTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); //TODO: add this to all tables in frame

        //TODO: viaggi sud table model definition goes here
        sudTableButtonPanel = new JPanel(new BorderLayout(0, 0));
        sudTablePanel.add(sudTableButtonPanel, BorderLayout.NORTH);

        panel_3 = new JPanel(new BorderLayout(0, 0));
        tablePanel.add(panel_3);

        lblSud = ViaggiFrameUtils.newJLabel(strings.getString("mainframe.label.viaggi.sud"), TAHOMA_DEFAULT_FONT);
        lblSud.setHorizontalAlignment(SwingConstants.CENTER);
        sudTableButtonPanel.add(lblSud, BorderLayout.CENTER);

        panel_2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sudTableButtonPanel.add(panel_2, BorderLayout.EAST);

        sudRemoveButton = ViaggiFrameUtils.newButton(
                "-",
                viaggiRowsAction,
                SUD_REMOVE_ROW_COMMAND
        );


        sudAddButton = ViaggiFrameUtils.newButton(
                "+",
                viaggiRowsAction,
                SUD_ADD_ROW_COMMAND
        );

        sudPinButton = ViaggiFrameUtils.newButton(
                "\u2193",
                viaggiPinAction,
                SUD_PIN_COMMAND
        );

        sudUnpinButton = ViaggiFrameUtils.newButton(
                "\u2191",
                viaggiUnPinAction,
                SUD_UNPIN_COMMAND
        );

        panel_2.add(sudPinButton);
        panel_2.add(sudUnpinButton);
        panel_2.add(sudRemoveButton);
        panel_2.add(sudAddButton);

        sudTableScrollPane = new JScrollPane(viaggiSudTable);
        sudTableScrollPane.setBorder(null);

        viaggiSudPinTable = new ViaggiJTable(Consts.VIAGGI_TM_TYPE_SUD);
        viaggiSudPinTable.setTableHeader(null);
        //TODO: viaggi sud pin table model definition goes here
        sudPinScrollPane = new JScrollPane(viaggiSudPinTable);

        viaggiSudSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sudTableScrollPane, sudPinScrollPane);
        viaggiSudSplitPane.setDividerSize(3);
        sudTablePanel.add(viaggiSudSplitPane, BorderLayout.CENTER);

        nordTablePanel = new JPanel(new BorderLayout(0, 0));
//        tablePanel.add(nordTablePanel);

        viaggiNordTable = new ViaggiJTable(Consts.VIAGGI_TM_TYPE_NORD);
//        tablePanel.add(viaggiNordTable);

        //TODO: viaggi nord table model definition goes here

        nordTableButtonPanel = new JPanel(new BorderLayout(0, 0));
        nordTablePanel.add(nordTableButtonPanel, BorderLayout.NORTH);

        lblNord = ViaggiFrameUtils.newJLabel(strings.getString("mainframe.label.viaggi.nord"), TAHOMA_DEFAULT_FONT);
        lblNord.setHorizontalAlignment(SwingConstants.CENTER);
        nordTableButtonPanel.add(lblNord);

        panel_1 = new JPanel();

        nordRemoveButton = ViaggiFrameUtils.newButton("-", viaggiRowsAction, NORD_REMOVE_ROW_COMMAND);
        nordAddButton = ViaggiFrameUtils.newButton("+", viaggiRowsAction, NORD_ADD_ROW_COMMAND);
        nordPinButton = ViaggiFrameUtils.newButton("\u2193", viaggiPinAction, NORD_PIN_COMMAND);
        panel_2.add(nordPinButton);

        nordUnpinButton = ViaggiFrameUtils.newButton("\u2191", viaggiUnPinAction, NORD_UNPIN_COMMAND);
        panel_2.add(nordUnpinButton);

        panel_1.add(nordPinButton);
        panel_1.add(nordUnpinButton);
        panel_1.add(nordRemoveButton);
        panel_1.add(nordAddButton);
        nordTableButtonPanel.add(panel_1, BorderLayout.EAST);

        nordTableScrollPane = new JScrollPane(viaggiNordTable);
        //nordTablePanel.add(nordTableScrollPane, BorderLayout.CENTER);

        viaggiNordPinTable = new ViaggiJTable(Consts.VIAGGI_TM_TYPE_NORD);
        viaggiNordPinTable.setTableHeader(null);
//TODO: viaggi nord pin table model declaration goes here
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

        panel_6 = new JPanel(new BorderLayout(0, 0));
        otherPanel.add(panel_6);

        fermiTxt = new JTextArea();
        fermiTxt.setColumns(40);
        fermiTxt.setRows(4);
        fermiTxt.setLineWrap(true);
        fermiTxt.setWrapStyleWord(true);
        panel_6.add(fermiTxt);
        fermiTxt.setText("");

        fermiScrollPane = new JScrollPane(fermiTxt);
        panel_6.add(fermiScrollPane, BorderLayout.SOUTH);

        lblFermi = new JLabel(strings.getString("mainframe.label.fermi.assicurati"));
        panel_6.add(lblFermi, BorderLayout.NORTH);

        assNonAssicuratiPanel = new JPanel(new BorderLayout(0, 0));
        otherPanel.add(assNonAssicuratiPanel);

        nonAssicuratiTxt = new JTextArea();
        nonAssicuratiTxt.setTabSize(2);
        nonAssicuratiTxt.setRows(4);
        nonAssicuratiTxt.setColumns(40);
        nonAssicuratiTxt.setLineWrap(true);
        nonAssicuratiTxt.setWrapStyleWord(true);
        nonAssicuratiTxt.setText("");

        nonAssicuratiScrollPane = new JScrollPane(nonAssicuratiTxt);
        assNonAssicuratiPanel.add(nonAssicuratiScrollPane);

        lblNonAssicurati = new JLabel(strings.getString("mainframe.label.non.assicurati"));
        assNonAssicuratiPanel.add(lblNonAssicurati, BorderLayout.NORTH);

        btnSalvaFermiE = new JButton(strings.getString("mainframe.button.fermi.nonass.save"));
        btnSalvaFermiE.addActionListener(salvaFermiENonAssAction);
        otherPanel.add(btnSalvaFermiE);

        southPanel = new JPanel(new BorderLayout());
        infoTextField = new MessageJLabel();
        southPanel.add(infoTextField, BorderLayout.CENTER);
        contentPane.add(southPanel, BorderLayout.SOUTH);


        infoTextField.setInfoMessage(strings.getString("mainframe.infofield.welcome"));
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


        if(DEBUG_FRAME) {
            panel.setBorder(new TitledBorder("panel"));
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
            northPanel.setBorder(new TitledBorder("northPanel"));
            tablePanel.setBorder(new TitledBorder("tablePanel"));

            southPanel.setBorder(new TitledBorder("southPanel"));
            sudTableScrollPane.setBorder(new TitledBorder("sudTableScrollPane"));
            nordTableScrollPane.setBorder(new TitledBorder("nordTableScrollPane"));
            clientiTableScrollPane.setBorder(new TitledBorder("clientiTableScrollPane"));
            scrollPane.setBorder(new TitledBorder("scrollPane"));
            fermiScrollPane.setBorder(new TitledBorder("fermiScrollPane"));
            nonAssicuratiScrollPane.setBorder(new TitledBorder("nonAssicuratiScrollPane"));
            noteScrollPane.setBorder(new TitledBorder("noteScrollPane"));
            viaggiSplitPane.setBorder(new TitledBorder("viaggiSplitPane"));
            clientiTableSplitPane.setBorder(new TitledBorder("clientiTableSplitPane"));
        }
    }

/*    @Subscribe
    public void updateCamionList(CamionEvent e) {
//        camions = dbs.getCamion();
//        ((ViaggiTableModel) viaggiNordTable.getModel()).setCamions(camions);
//        sudTableModel.setCamions(camions);
        formattaTabelle();
    }*/


    @Override
    public void reloadTableModel(Date d, int option) throws SQLException {
        viaggiNordTable.setModel(new ViaggiTableModel(Consts.VIAGGI_TM_TYPE_NORD));
        viaggiNordTable.getModel().addTableModelListener(this);
        viaggiNordTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());

        viaggiSudTable.setModel(new ViaggiTableModel(Consts.VIAGGI_TM_TYPE_SUD));
        viaggiSudTable.getModel().addTableModelListener(this);
        viaggiSudTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());

        viaggiNordPinTable.setModel(new ViaggiTableModel(Consts.VIAGGI_TM_TYPE_NORD));
        viaggiNordPinTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());
        ViaggiTableModel viaggiNordPinTableModel = (ViaggiTableModel) viaggiNordPinTable.getModel();
        viaggiNordPinTableModel.addTableModelListener(viaggiPinnedTableListener);

        viaggiSudPinTable.setModel(new ViaggiTableModel(Consts.VIAGGI_TM_TYPE_SUD));
        viaggiSudPinTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());
        ViaggiTableModel viaggiSudPinTableModel = (ViaggiTableModel) viaggiSudPinTable.getModel();
        viaggiSudPinTableModel.addTableModelListener(viaggiPinnedTableListener);

        ViaggiTableModel nordTableModel = ((ViaggiTableModel) viaggiNordTable.getModel());
        ViaggiTableModel sudTableModel = (ViaggiTableModel) viaggiSudTable.getModel();

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
        int dow = c.get(Calendar.DAY_OF_WEEK) - 1; //-1 because  c.get(Calendar.DAY_OF_WEEK)  starts from 1
        String[] weekDays = strings.getString("generic.week.days").split(",");
        giornoSettLabel.setText(weekDays[dow]);

        try {
            reloadTableModel(d, mode);
            reloadOrdiniModel(d);
            reloadNote(d);
            currentDate = d;
            selectedDateLbl.setText(ViaggiUtils.createStringFromDate(currentDate));

            formattaTabelle();
            //updateCamionList();
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
                                } else {
                                    onError(new IllegalArgumentException("Invalid id returned from database"));
                                }

                                JTable t = tm.getType() == Consts.VIAGGI_TM_TYPE_NORD ? viaggiNordTable : viaggiSudTable;
                                selectTableCell(t, row-1, 0);
                                logger.info("ADD ROW!");

                                notifyRowInserted("Viaggi " + inserted.getPosizione(), ""+newId, inserted.getData().toString());
                            }
                        })
                        .execute();
            }else if(e.getType() == TableModelEvent.DELETE){
                logger.info("ROW DELETED!");
            }
        }
    }

    private void selectTableCell(JTable table, int row, int col) {
        int rowCount = table.getRowCount();
        if(row >= rowCount) { //must be 0 <= row <= rowCount-1
            row = rowCount - 1; //if not we select the last row
        }

        table.changeSelection(row, col, false, false);
        //table.editCellAt(row, col);
        table.requestFocus();
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

        int maxRowCount = Math.max(viaggiSudPinTable.getRowCount(), viaggiNordPinTable.getRowCount());
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
                //int newHeight = tablePanel.getHeight()-10;
                //int newHeight = source.getHeight();
                int newHeight = tablePanel.getHeight();
                tablePanel.setSize(new Dimension(newWidth, newHeight));

                //contentPane.setMaximumSize(source.getSize());

                clientiTableSplitPane.setPreferredSize(new Dimension((newWidth/3)-20, (newHeight/2) + (int)(newHeight/3.5)));

                nordTablePanel.setPreferredSize(new Dimension((newWidth/3)-10, (newHeight/2) + (int)(newHeight/3)));
                sudTablePanel.setPreferredSize(new Dimension((newWidth/3)-10, (newHeight/2) + (int)(newHeight/3)));
                //viaggiSplitPane.setPreferredSize(new Dimension((newWidth/2), (newHeight/2) + (int)(newHeight/3)));

                notePanel.setPreferredSize(new Dimension((newWidth/3) - 20, tablePanel.getHeight()-((newHeight/2) + ((int)(newHeight/3.5) + 20))));
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
        Msg.error(this, MessageFormat.format(strings.getString("database.error"), e.getErrorCode(), e.getMessage()));
    }

    private void notifyRowUpdated(String table, String columnName, String newValue, String date) {
        logger.debug("Updated row on table: " + table + " Col: " + columnName + " Value: " + newValue);
        String updateMessage = MessageFormat.format(strings.getString("mainframe.infofield.table.row.update"), table, newValue, columnName);
        try {
            DatabaseHelperChannel.getInstance().notifyRowUpdated(table, date);
        } catch (RemoteException e) {
            logger.error("DBH" , e);
            updateMessage += " " + strings.getString("mainframe.infofield.update.communication.fail");
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
            updateMessage += " " + strings.getString("mainframe.infofield.update.communication.fail");
        }

        infoTextField.setInfoMessage(updateMessage);
    }

    private void notifyRowInserted(String table, String newId, String date) {
        logger.info("Inserted new row in table: " + table + " With id: " + newId);
        String insertMessage = MessageFormat.format(strings.getString("mainframe.infofield.table.row.insert"), table, newId);
        try {
            DatabaseHelperChannel.getInstance().notifyRowInserted(table, date);
        } catch (RemoteException e) {
            logger.error("DBH", e);
            insertMessage += " " + strings.getString("mainframe.infofield.update.communication.fail");
        }

        infoTextField.setInfoMessage(insertMessage);
    }

    private void onError(Exception error) {
        logger.error(error.getMessage(), error);

        if (error instanceof SQLException) {
            infoTextField.setErrorMessage(strings.getString("mainframe.infofield.database.error"));
        } else if (error instanceof RemoteException) {
            infoTextField.setWarnMessage(strings.getString("database.helper.communication.fail"));
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
        infoTextField.setUploadMessage(MessageFormat.format(strings.getString("database.helper.table.row.insert"), tableName, who, elapsedSeconds));
        infoTextField.addMouseListener(aggiornaOnClickAdapter);
    }

    @Override
    public void onRowUpdated(String tableName, String date, String who, long timestamp) {
        long elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000;
        infoTextField.setUploadMessage(MessageFormat.format(strings.getString("database.helper.table.row.update"), tableName, who, elapsedSeconds));
        infoTextField.addMouseListener(aggiornaOnClickAdapter);    }

    @Override
    public void onDateAdded(String date, String who, long timestamp) {
        Date date1 = ViaggiUtils.checkAndCreateDate(date, "-", true);
        long elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000;
        infoTextField.setUploadMessage(MessageFormat.format(strings.getString("database.helper.day.add"), who, elapsedSeconds));
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
                Msg.warn(MainFrame.this, MessageFormat.format(strings.getString("database.helper.day.deleted"), who));
                try {
                    lastDateFromDb = dbs.getDataAggiornamento();
                    loadDate(lastDateFromDb, RELOAD_RESETCONNECTION);
                } catch (SQLException e) {
                    logDatabaseError(e);
                }
            });
        }
    }

    /*public MessageJLabel getMessageField() {
        return infoTextField;
    }
*/
    class ColumnChangeListener implements TableColumnModelListener {
        JTable sourceTable;
        JTable targetTable;

        public ColumnChangeListener(JTable source, JTable target) {
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

            for (int i = 0; i < sourceModel.getColumnCount(); i++) {
                targetModel.getColumn(i).setPreferredWidth(sourceModel.getColumn(i).getWidth());
            }

            targetModel.addColumnModelListener(listener);
        }
    }
}