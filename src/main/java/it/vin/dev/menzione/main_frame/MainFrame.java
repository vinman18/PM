package it.vin.dev.menzione.main_frame;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.Msg;
import it.vin.dev.menzione.VerboseLogger;
import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.database_helper.DatabaseHelperChannel;
import it.vin.dev.menzione.database_helper.DatabaseHelperException;
import it.vin.dev.menzione.events.DateAddEvent;
import it.vin.dev.menzione.events.ViaggiEventsBus;
import it.vin.dev.menzione.events.dbh.*;
import it.vin.dev.menzione.frame.*;
import it.vin.dev.menzione.logica.*;
import it.vin.dev.menzione.workers.*;
import org.apache.logging.log4j.ThreadContext;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;


@SuppressWarnings({"FieldCanBeLocal", "Duplicates"})
public class MainFrame extends JFrame implements TableModelListener {
    private static final boolean DEBUG_FRAME = ViaggiFrameUtils.DEBUG_FRAME;

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
    private static final String NORD_PIN_REMOVE_COMMAND = "nord_pin_remove_row";
    private static final String SUD_PIN_REMOVE_COMMAND = "nord_pin_remove_command";

    private ResourceBundle strings = ResourceBundle.getBundle("Localization/Strings");
//    private MainFrameColumnsSize mainFrameColumnsSize;

    //    private Date lastDateFromDb;
    private DatabaseService dbs;

    private VerboseLogger logger = VerboseLogger.create(MainFrame.class);

    private Date currentDate;

    private boolean dbhConnectionErrorShowed = false;

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
    //    private JSplitPane clientiTableSplitPane;
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
    private JPanel northPanel;
    private JLabel lblDateSelection;
    private JPanel tablePanel;
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
    private JPanel centerLeftPanel;
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

//    private Map<JTable, TableColumnModelListener> tableColumnModelListenerMap;

    private TableModelListener viaggiPinnedTableListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            ViaggiTableModel model = (ViaggiTableModel) e.getSource();
            int row = e.getFirstRow();
            int col = e.getColumn();
            int type = e.getType();


            logger.debug("viaggiPinTableListener: type={} row={}  col={}", type, row, col);

            if(type == TableModelEvent.UPDATE && col == TableModelEvent.ALL_COLUMNS) {
                return;
            }

            Viaggio v = null;

            switch (type) {
                case TableModelEvent.INSERT:
                    v = model.getElementAt(row);
                    if(v.isPinned()) { // if isPinned is always true we have to add it to database (if id < 0)
                        if(v.getId() < 0) {
                            ViaggiUpdateWorker.connect(dbs)
                                    .insert(v)
                                    .onResult(new UpdateWorkerAdapter<Viaggio>() {
                                        @Override
                                        public void onError(Exception error) {
                                            logger.warn("viaggiPinTableListener: viaggio insert database error logged in next line");
                                            MainFrame.this.onError(error);
                                        }
                                        @Override
                                        public void onInsert(Viaggio inserted, long newId) {
                                            logger.info("viaggiPinTableListener: new viaggio sent successfully! New viaggio id = {}", newId);
                                            logger.verbose("viaggiPinTableListener: viaggio returned from database: {}", inserted.toString());

                                            if(newId > 0){
                                                model.getElementAt(row).setId(newId);
                                            } else {
                                                onError(new IllegalArgumentException("Invalid id returned from database"));
                                            }

                                            JTable t = model.getType() == Consts.TABLE_TYPES.VIAGGI_NORD ? viaggiNordPinTable : viaggiSudPinTable;
                                            ViaggiFrameUtils.selectTableCell(t, row, 0);
                                        }
                                    })
                                    .execute();
                            return;
                        }
                    }
                    logger.info("ViaggiPinnedTableListener: inserted new row in table Pinned {}", ViaggiTableModel.getTableModelName(model));
                    logger.verbose("Element before insert: {}", v.toString());
                    v.setPinned(true);
                    col = Viaggio.COL_PINNED;
                    break;
                case TableModelEvent.DELETE:
                    v = ((ViaggiPinTableModel) model).getRemovedRow(row);
                    if(v.isPinned()) { // if pinned is true so the row isn't unpinned, so it was removed from database
                        return;  // and we have nothing to do
                    }

                    // if pinned was set to false, so unpin was requested and we have to update the database
                    logger.info("ViaggiPinnedTableListener: removed row in table Pinned {}", ViaggiTableModel.getTableModelName(model));
                    logger.verbose("Element before delete: {}", v.toString());
//                    v.setPinned(false);
                    col = Viaggio.COL_PINNED;
                    break;
                case TableModelEvent.UPDATE:
                    v = model.getElementAt(row);
                    logger.info("ViaggiPinnedTableListener: updated col '{}' of row {} in table Pinned {}",
                            ViaggiTableModel.getViaggioColumnNameByIndex(model, col),
                            row,
                            ViaggiTableModel.getTableModelName(model)
                    );
                    logger.verbose("ViaggiPinnedTableListener: new value: '{}'", ViaggiTableModel.getViaggioValueByColumnIndex(v, col));
                    break;
            }

            logger.verbose("ViaggiPinnedTableListener: element after operations: {}", v.toString());
            logger.info("ViaggiPinnedTableListener: sending update to database...");

            ViaggiUpdateWorker.connect(dbs)
                    .update(v, col)
                    .onResult(new UpdateWorkerAdapter<Viaggio>() {
                        @Override
                        public void onUpdate(Viaggio updated, int col) {
                            logger.info("ViaggiPinnedTableListener: update sent to database successfully!");
                            logger.verbose("ViaggiPinnedTableListener: element returned from database: {}", updated.toString());
                            if(col == Viaggio.COL_PINNED) {
                                infoTextField.setInfoMessage(strings.getString("mainframe.msg.move.viaggio.ok"));
                            } else {
                                String value = String.valueOf(ViaggiTableModel.getViaggioValueByColumnIndex(updated, col));
                                String columnName = Viaggio.NORD.equals(updated.getPosizione())
                                        ? viaggiNordTable.getColumnName(col)
                                        : viaggiSudTable.getColumnName(col);

                                notifyRowUpdated("Viaggi " + updated.getPosizione(), columnName, value, updated.getData().toString());
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            logger.warn("ViaggiPinnedTableListener: update sent error logged in next line");
                            MainFrame.this.onError(error);
                        }
                    })
                    .execute();
        }
    };

    private UpdateWorkerListener<Ordine> ordiniUpdateResultListener = new UpdateWorkerListener<Ordine>() {
        @Override
        public void onUpdate(Ordine updated, int col) {
            logger.info("ordiniUpdateResultListener: update sent to database successfully!");
            logger.verbose("ordiniUpdateResultListener: element returned form database: {}", updated.toString());
            String columnName = ordiniDiscesaTable.getColumnName(col);
            String value = OrdiniTableModel.getOrdineValueFromColumnIndex(updated, col);
            notifyRowUpdated("Ordini", columnName, value, updated.getDate().toString());
        }

        @Override
        public void onError(Exception error) {
            logger.warn("ordiniUpdateResultListener: error logged in next line");
            MainFrame.this.onError(error);
        }

        @Override
        public void onInsert(Ordine inserted, long newId) {
            logger.info("ordiniUpdateResultListener: new row inserted into database successfully with id {}!", newId);
            logger.verbose("ordiniUpdateResultListener: element returned from database: {}", inserted.toString());
            notifyRowInserted("Ordini", ""+newId, inserted.getDate().toString());

            logger.verbose("ordiniUpdateResultListener: reloading ordini tables...");
            try {
                //TODO: change reloading from database entire data set with simply adding new row to table
                reloadOrdiniModel(currentDate);
            } catch (SQLException e) {
                logger.warn("ordiniUpdateResultListener: ordini tables reload error logged in next line");
                logDatabaseError(e);
            }
        }
    };

    private final UpdateWorkerListener<Nota> noteUpdateResultListener = new UpdateWorkerListener<Nota>() {
        @Override
        public void onUpdate(Nota updated, int col) {
            logger.info("noteUpdateResultListener: update sent to database successfully!");
            logger.verbose("noteUpdateResultListener: element returned form database: ", updated.toString());
            notifyRowUpdated("Note", updated.getTipo(), updated.getTesto(), updated.getData().toString());
        }

        @Override
        public void onError(Exception error) {
            logger.warn("noteUpdateResultListener: error logged in next line");
            MainFrame.this.onError(error);
        }

        @Override
        public void onInsert(Nota inserted, long newId) {
            logger.info("noteUpdateResultListener: new row inserted into database successfully with id {}!", newId);
            logger.verbose("noteUpdateResultListener: element returned form database: ", inserted.toString());

            notifyRowInserted("Note", ""+newId, inserted.getData().toString());
        }
    };

    private MouseAdapter aggiornaOnClickAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            logger.info("aggiornaOnClickAdapter: reloading current date...");
            loadDate(currentDate, RELOAD_STANDARD);
//            infoTextField.setInfoMessage(strings.getString("mainframe.infofield.welcome"));
        }
    };

    private final ActionListener openCamionFrameAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            logger.info("openCamionFrameAction: opening frame...");
            JButton b = (JButton) arg0.getSource();
            JFrame aggiungiCamionFrame = new AggiungiCamionFrame();
            aggiungiCamionFrame.setVisible(true);
        }
    };

    private final ActionListener openNewDateFrameAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            logger.info("openNewDateFrameAction: opening frame...");
            JButton b = (JButton) arg0.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(b);
            JFrame inserisciGiornataFrame = new AggiungiDataFrame();
            inserisciGiornataFrame.setVisible(true);
        }
    };

    private final ActionListener goToLastDateAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("goToLastDateAction: going to last date...");
            try {
                Date lastDate = dbs.getLastDate();
                logger.verbose("goToLastDateAction: date returned from database: {}", lastDate.toString());
                if(!currentDate.equals(lastDate)) {
                    loadDate(lastDate, RELOAD_RESETCONNECTION);
                }
            } catch (SQLException e1) {
                logger.warn("goToLastDateAction: date retreiving error logged in next line");
                logDatabaseError(e1);
            }
        }
    };

    private final ActionListener dateExportAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            logger.info("dateExportAction: exporting date...");
            JButton b = (JButton) actionEvent.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(b);
            try {
                //new PdfReportBuilder(viaggiNordTable, viaggiSudTable, viaggiNordPinTable, viaggiSudPinTable, ordiniSalitaTable, ordiniDiscesaTable, noteTable, fermiTxt.getText(), nonAssicuratiTxt.getText(), ViaggiUtils.createStringFromDate(currentDate, true));
                PdfReportBuilderRemote pdfReportBuilder = new PdfReportBuilderRemote(currentDate);
                File reportFile = pdfReportBuilder.startExport();
                logger.verbose("dateExportAction: date export completed!");
                Msg.info(root, strings.getString("mainframe.msg.export.ok"));
                if (Desktop.isDesktopSupported()) {
                    try {
                        logger.verbose("dateExportAction: opening file...");
                        Desktop.getDesktop().open(reportFile);
                    } catch (IOException ex) {
                        logger.warn("dateExportAction: no application registered fro PDFs");
                        infoTextField.setErrorMessage(strings.getString("mainframe.msg.export.pdf.application.error"));
                        // no application registered for PDFs
                    }
                }
            } catch (Exception e) {
                logger.warn("dateExportAction: date export error logged in next line");
                logger.error(e.getMessage(), e);
                Msg.error(root, e.getMessage());
            }
        }
    };

    private final ActionListener addOrderAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            logger.info("addOrderAction: opening add order frame...");
            Component source = (Component) arg0.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(source);
            String type = arg0.getActionCommand().equals(ORDINI_DISCESA_COMMAND)
                    ? Ordine.DISCESA
                    : Ordine.SALITA;

            logger.verbose("addOrderAction: command='{}', table={}", arg0.getActionCommand(), type);
            if(currentDate != null) {
                AggiungiOrdineFrame fr;
                try {
                    fr = new AggiungiOrdineFrame(type, currentDate);
                    fr.setResultListener(ordiniUpdateResultListener);
                    fr.setVisible(true);
                } catch (SQLException e) {
                    logger.warn("addOrderAction: opening frame error logged in next line");
                    logDatabaseError(e);
                }
            }else{
                logger.warn("addOrderAction: currentDate is null");
                Msg.error(root, strings.getString("mainframe.msg.select.date"));
            }
        }
    };

    private final ActionListener removeOrderAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            int secondsToWait = Integer.parseInt(Configuration.getInstance().getProperty(Configuration.UNDO_WAIT_SECONDS));
            String command = arg0.getActionCommand();
            String deleteSuccessMessage = strings.getString("mainframe.infofield.table.row.delete");
            String clickToCancelMessage = MessageFormat.format(
                    strings.getString("mainframe.infofield.click.here.to.cancel"), secondsToWait
            );
            logger.verbose("removeOrderAction: requested row deleting with command '{}'", arg0.getActionCommand());
            OrdiniTable table = command.equals(ORDINI_DISCESA_COMMAND)
                    ? ordiniDiscesaTable
                    : ordiniSalitaTable;

            String tableType = command.equals(ORDINI_DISCESA_COMMAND) ? "DISCESA" : "SALITA";
            int rowSel = table.getSelectedRow();
            int colSel = table.getSelectedColumn();

            if(rowSel < 0) {
                logger.verbose("removeOrderAction: invalid row number ({}). Action ignored", rowSel);
                return;
            }

            OrdiniTableModel tm = (OrdiniTableModel) table.getModel();

            logger.info("removeOrderAction: removing order in row {} from table Ordini {}", rowSel, tableType);

            Ordine removed = tm.removeRow(rowSel);

            logger.verbose("removeOrderAction: order to remove: {}", removed.toString());

            try {
                dbs.rimuoviOrdine(removed);
                logger.info("removeOrderAction: order removed from database successfully!");
                infoTextField.setInfoMessage(deleteSuccessMessage + ". " + clickToCancelMessage);
                Timer t = ViaggiUtils.executeAfter(secondsToWait * 1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        infoTextField.setInfoMessage(deleteSuccessMessage);
                    }
                });
                infoTextField.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        t.stop();
                        logger.info("removeOrderAction: row deletion cancelled by user. Reinserting deleted row");
                        OrdiniUpdateWorker.connect(dbs)
                                .insert(removed)
                                .onResult(ordiniUpdateResultListener)
                                .execute();
                        infoTextField.setInfoMessage(strings.getString("mainframe.infofield.table.row.delete.cancelled"));
                    }
                });
            } catch (SQLException e) {
                tm.addRow(removed);
                logger.warn("removeOrderAction: order removing error logged in next line");
                logDatabaseError(e);
            }

            ViaggiFrameUtils.selectTableCell(table, rowSel, colSel);
        }
    };

    private final ActionListener aggiungiNotaAction = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            Component source = (Component) arg0.getSource();
            MainFrame root = (MainFrame) SwingUtilities.getRoot(source);
            logger.info("aggiungiNotaAction: adding new nota...");

            if (currentDate != null) {
                Nota nuovo = new Nota(currentDate, "", Nota.NOTA);

                logger.info("aggiungiNotaAction: sending new nota to database...");
                NoteUpdateWorker.connect(dbs)
                        .insert(nuovo)
                        .onResult(new UpdateWorkerAdapter<Nota>() {
                            @Override
                            public void onError(Exception error) {
                                logger.info("aggiungiNotaAction: database error logged in next line");
                                MainFrame.this.onError(error);
                            }

                            @Override
                            public void onInsert(Nota inserted, long newId) {
                                logger.info("aggiungiNotaAction: new nota sent successfully with id {}", newId);
                                nuovo.setId(newId);
                                NoteTableModel tm = (NoteTableModel) noteTable.getModel();
                                tm.addRow(nuovo);

                                int rows = tm.getRowCount();

                                ViaggiFrameUtils.selectTableCell(noteTable, rows-1, 0);
                                notifyRowInserted("Note", ""+newId, inserted.getData().toString());
                            }
                        })
                        .execute();
            } else {
                logger.info("aggiungiNotaAction: currentDate is null");
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

            logger.info("deleteNoteAction: removing nota in row {}", selRow);
            if(selRow < 0) {
                logger.verbose("deleteNoteAction: invalid row number ({}). Action ignored", selRow);
                return;
            }

            Nota n = tm.removeRow(selRow);

            logger.verbose("deleteNoteAction: nota to remove: {}", n.toString());
            try {
                dbs.rimuoviNota(n);
                logger.info("deleteNoteAction: nota deleted from database successfully!");
            } catch (SQLException e1) {
                tm.addRow(n);
                logger.warn("deleteNoteAction: nota delete error logged in next line");
                logDatabaseError(e1);
            }

            ViaggiFrameUtils.selectTableCell(noteTable, selRow, selCol);
        }
    };

    private final ActionListener salvaFermiENonAssAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            logger.info("salvaFermiENonAssAction: saving fermi e non assicurati note...");

            UpdateWorkerListener<Nota> resultListener = new UpdateWorkerListener<Nota>() {
                @SuppressWarnings("Duplicates")
                @Override
                public void onInsert(Nota inserted, long newId) {
                    String message = null;
                    logger.info("salvaFermiENonAssAction: nota '{}' inserted into database successfully!", inserted.getTipo());
                    logger.verbose("salvaFermiENonAssAction: element returned from database: {}", inserted.toString());
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
                    logger.info("salvaFermiENonAssAction: nota '{}' updated successfully!", updated.getTipo());
                    logger.verbose("salvaFermiENonAssAction: element returned from database: {}", updated.toString());

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
                    logger.warn("salvaFermiENonAssAction: fermi e non ass save error logged in next line");
                    MainFrame.this.onError(error);
                }
            };

            NoteUpdateWorker fermiNotaUpdateWorker;

            if(fermiNota == null) {
                logger.info("salvaFermiENonAssAction: 'fermi' nota is null, saving new one into database...");
                fermiNota = new Nota(currentDate, fermiTxt.getText(), Nota.FERMI);
                fermiNotaUpdateWorker = NoteUpdateWorker.connect(dbs)
                        .insert(fermiNota)
                        .onResult(resultListener);
            } else {
                logger.info("salvaFermiENonAssAction: 'fermi' nota already exists, updating...");
                logger.verbose("salvaFermiENonAssAction: element before update: {}", fermiNota.toString());
                fermiNota.setTesto(fermiTxt.getText());
                fermiNotaUpdateWorker = NoteUpdateWorker.connect(dbs)
                        .update(fermiNota)
                        .onResult(resultListener);
            }

            logger.verbose("salvaFermiENonAssAction: element to save into database: {}", fermiNota.toString());
            fermiNotaUpdateWorker.execute();


            NoteUpdateWorker nonAssNotaUpdateWorker;
            if(nonAssNota == null) {
                logger.info("salvaFermiENonAssAction: 'nonAss' nota is null, saving new one into database...");
                nonAssNota = new Nota(currentDate, fermiTxt.getText(), Nota.NONASS);
                nonAssNotaUpdateWorker = NoteUpdateWorker.connect(dbs)
                        .insert(nonAssNota)
                        .onResult(resultListener);
            } else {
                logger.info("salvaFermiENonAssAction: 'nonAss' nota already exists, updating...");
                logger.verbose("salvaFermiENonAssAction: element before update: {}", nonAssNota.toString());
                nonAssNota.setTesto(nonAssicuratiTxt.getText());
                nonAssNotaUpdateWorker = NoteUpdateWorker.connect(dbs)
                        .update(nonAssNota)
                        .onResult(resultListener);
            }
            logger.verbose("salvaFermiENonAssAction: element to save into database: {}", nonAssNota.toString());
            nonAssNotaUpdateWorker.execute();
        }
    };

    private ActionListener reloadAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("reloadAction: reloading currentDate {} with RELOAD_STANDARD flag", ViaggiUtils.createStringFromDate(currentDate, false));
            loadDate(currentDate, MainFrame.RELOAD_STANDARD);
        }
    };

    private ActionListener deleteThisDayAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Component source = (Component) e.getSource();
            Component root = SwingUtilities.getRoot(source);

            String currentDateString = ViaggiUtils.createStringFromDate(currentDate, true);
            logger.info("deleteThisDayAction: deleting date {}", ViaggiUtils.createStringFromDate(currentDate, false));
            logger.verbose("deleteThisDayAction: prompting first confirmation check...");
            int resp1 = Msg.yesno(root, MessageFormat.format(strings.getString("mainframe.msg.date.delete.confirm.1"), currentDateString));

            if(resp1 == JOptionPane.YES_OPTION) {
                logger.verbose("deleteThisDayAction: first confirmation check passed, prompting second confirmation check...");
                int resp2 = Msg.yesno(root, MessageFormat.format(strings.getString("mainframe.msg.date.delete.confirm.2"), currentDateString));

                if(resp2 == JOptionPane.YES_OPTION) {
                    logger.verbose("deleteThisDayAction: second confirmation check passed, proceeding with database deletion...");
                    try {
                        dbs.deleteDate(currentDate);
                        logger.info("deleteThisDayAction: date {} deleted successfully!", ViaggiUtils.createStringFromDate(currentDate, false));
                        logger.info("deleteThisDayAction: sending remote notification to other clients...");
                        logger.info("deleteThisDayAction: loading new last date...");
                        loadDate(dbs.getDataAggiornamento(), RELOAD_RESETCONNECTION);
                        logger.info("deleteThisDayAction: notification sended successfully!");
                        DatabaseHelperChannel.getInstance().notifyDateRemoved(currentDate.toString());
                    } catch (SQLException e1) {
                        logger.warn("deleteThisDayAction: database deletion error logged in next line");
                        logDatabaseError(e1);
                    } catch (DatabaseHelperException e1) {
                        logger.warn("deleteThisDayAction: remote notification send error logged in next line");
                        logger.error(e1.getMessage(), e1);
                        infoTextField.setWarnMessage(strings.getString("database.helper.communication.fail"));
                    }
                } else {
                    logger.verbose("deleteThisDayAction: operation cancelled by user");
                }
            } else {
                logger.verbose("deleteThisDayAction: operation cancelled by user");
            }
        }
    };

    private ActionListener viaggiPinAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTable table;
            ViaggiTableModel tableModel, pinTableModel;

            logger.verbose("viaggiPinAction: requested pin action with command '{}'", e.getActionCommand());
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

            logger.info("viaggiPinAction: moving row {} from table {} to table Pin {}", selectedRow, ViaggiTableModel.getTableModelName(tableModel), ViaggiTableModel.getTableModelName(pinTableModel));

            if(selectedRow < 0) {
                logger.info("viaggiPinAction: no row selected");
                return;
            }

            if(table.isEditing()) {
                table.getCellEditor().cancelCellEditing();
            }

            Viaggio v = tableModel.removeRow(selectedRow);
            logger.verbose("viaggiPinAction: element removed from table {}: {}", ViaggiTableModel.getTableModelName(tableModel), v.toString());
            pinTableModel.addRow(v);

            ViaggiFrameUtils.selectTableCell(table, selectedRow, selectedColumn);
        }
    };

    private ActionListener viaggiUnPinAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTable pinTable;
            ViaggiTableModel tableModel;
            ViaggiPinTableModel pinTableModel;

            logger.verbose("viaggiUnPinAction: requested unpin action with command '{}'", e.getActionCommand());

            switch (e.getActionCommand()) {
                case NORD_UNPIN_COMMAND:
                    pinTable = viaggiNordPinTable;
                    tableModel = ((ViaggiTableModel) viaggiNordTable.getModel());
                    pinTableModel = (ViaggiPinTableModel) viaggiNordPinTable.getModel();
                    break;
                case SUD_UNPIN_COMMAND:
                    pinTable = viaggiSudPinTable;
                    tableModel = (ViaggiTableModel) viaggiSudTable.getModel();
                    pinTableModel = (ViaggiPinTableModel) viaggiSudPinTable.getModel();
                    break;
                default:
                    return;
            }

            int viewSelectedRow = pinTable.getSelectedRow();
            int modelSelectedRow = pinTable.convertRowIndexToModel(viewSelectedRow);
            int selectedColumn = pinTable.getSelectedColumn();

            logger.info("viaggiUnPinAction: moving row {} from table Pin {} to table {}", modelSelectedRow, ViaggiTableModel.getTableModelName(pinTableModel), ViaggiTableModel.getTableModelName(tableModel));

            if(modelSelectedRow < 0) {
                logger.info("viaggiUnPinAction: no row selected");
                return;
            }

            if(pinTable.isEditing()) {
                pinTable.getCellEditor().cancelCellEditing();
            }

            Viaggio v = pinTableModel.unpinRow(modelSelectedRow);
            logger.verbose("viaggiUnPinAction: element removed from table {}: {}", ViaggiTableModel.getTableModelName(pinTableModel), v.toString());
            tableModel.addRow(v);

            ViaggiFrameUtils.selectTableCell(pinTable, viewSelectedRow, selectedColumn);
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

            logger.verbose("viaggiRowsAction: requested action on Viaggi table with command '{}'", actionCommand);

            switch (actionCommand) {
                case NORD_REMOVE_ROW_COMMAND:
                case NORD_ADD_ROW_COMMAND:
                    table = viaggiNordTable; break;
                case SUD_REMOVE_ROW_COMMAND:
                case SUD_ADD_ROW_COMMAND:
                    table = viaggiSudTable; break;
                case NORD_PIN_REMOVE_COMMAND:
                    table = viaggiNordPinTable; break;
                case SUD_PIN_REMOVE_COMMAND:
                    table = viaggiSudPinTable; break;
                default:
                    return;
            }

            switch (actionCommand) {
                case NORD_ADD_ROW_COMMAND:
                case SUD_ADD_ROW_COMMAND:
                    logger.info("viaggiRowsAction: adding new row to table {}...",
                            ViaggiTableModel.getTableModelName(((ViaggiTableModel) table.getModel())));

                    ((ViaggiTableModel) table.getModel()).addRow(null);
                    break;
                case NORD_REMOVE_ROW_COMMAND:
                case SUD_REMOVE_ROW_COMMAND:
                case NORD_PIN_REMOVE_COMMAND:
                case SUD_PIN_REMOVE_COMMAND:
                    int secondsToWait = Integer.parseInt(Configuration.getInstance().getProperty(Configuration.UNDO_WAIT_SECONDS));
                    int selectedRow = table.getSelectedRow();
                    int selectedColumn = table.getSelectedColumn();
                    String deleteSuccessMessage = strings.getString("mainframe.infofield.table.row.delete");
                    String clickToCancelMessage = MessageFormat.format(
                            strings.getString("mainframe.infofield.click.here.to.cancel"), secondsToWait
                    );

                    logger.info("viaggiRowsAction: removing row {} from table {}...", selectedRow,
                            ViaggiTableModel.getTableModelName((ViaggiTableModel) table.getModel()));

                    if(selectedRow < 0) {
                        logger.verbose("viaggiRowsAction: invalid row number ({}). Action ignored", selectedRow);
                        return;
                    }

                    if(table.isEditing()){
                        table.getCellEditor().cancelCellEditing();
                    }


                    Viaggio removed = ((ViaggiTableModel) table.getModel()).removeRow(selectedRow);
                    logger.verbose("viaggiRowsAction: element to remove from database: {}", removed.toString());

                    try {
                        logger.verbose("viaggiRowsAction: sending remove request to database...");
                        dbs.rimuoviViaggio(removed);
                        logger.info("viaggiRowsAction: element removed from database successfully!");
                        infoTextField.setInfoMessage(deleteSuccessMessage + " " + clickToCancelMessage);
                        Timer t = ViaggiUtils.executeAfter(secondsToWait * 1000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                infoTextField.setInfoMessage(deleteSuccessMessage);
                            }
                        });
                        infoTextField.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                t.stop();
                                logger.info("viaggiRowsAction: row deletion cancelled by user. Reinserting deleted row");
                                removed.setId(-1); // row will be inserted into database only if id < 0
                                ((ViaggiTableModel) table.getModel()).addRow(removed);
                                infoTextField.setInfoMessage(strings.getString("mainframe.infofield.table.row.delete.cancelled"));
                            }
                        });
                    } catch (SQLException e) {
                        ((ViaggiTableModel) table.getModel()).addRow(removed);
                        logger.warn("viaggiRowsAction: database remove error logged in next line");
                        logDatabaseError(e);
                    }

                    ViaggiFrameUtils.selectTableCell(table, selectedRow, selectedColumn);
                    break;
            }

        }
    };

    private final ActionListener resetDefaultColumnsSize = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            formattaTabelle(null);
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
            logger.info("openConfigAction: opening frame...");
            SwingUtilities.invokeLater(
                    () -> ConfigFrame.open(WindowConstants.DISPOSE_ON_CLOSE)
            );
        }
    };

    private void init() throws SQLException {
        logger.verbose("init: creating frame icon...");
        this.setIconImage(Toolkit.getDefaultToolkit().createImage(ViaggiUtils.getMainIcon()));
//        tableColumnModelListenerMap = new HashMap<>();

        try {
            logger.info("init: connecting to database...");
            dbs = DatabaseService.create();
            logger.info("inti: database connection success!");
//            lastDateFromDb = dbs.getDataAggiornamento();
        }catch (SQLException e) {
            logger.warn("int: database connection error logged in next line");
            logger.fatal(e.getMessage(), e);
            //String[] options = new String[] {"OK", "Impostazioni"};

            String[] options = strings.getString("mainframe.msg.options.ok.settings").split(",");

            logger.info("init: prompting connection error message...");
            int ans = Msg.options(
                    rootPane,
                    strings.getString("generic.server.connection.fail"),
                    strings.getString("generic.error"),
                    JOptionPane.ERROR_MESSAGE,
                    options
            );

            if(ans == 1) {
                logger.info("init: user selected to open config frame");
                ConfigFrame.open(WindowConstants.EXIT_ON_CLOSE);
                throw new SQLException(e);
            } else {
                logger.info("init: user selected to close application. Exiting...");
                System.exit(1);
            }
        }

        ViaggiEventsBus.getInstance().register(this);

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
        setContentPane(contentPane);

        //nordTableModel = new ViaggiTableModel(Consts.VIAGGI_NORD, camions);
//        sudTableModel = new ViaggiTableModel(Consts.VIAGGI_SUD);

        northPanel = new JPanel(new BorderLayout());
        northPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel northWestPanel = new JPanel();
        JPanel northCenterPanel = new JPanel(new GridBagLayout());
        JPanel northEastPanel = new JPanel();
        GridBagConstraints gbc = new GridBagConstraints();

        lblDateSelection = ViaggiFrameUtils.newJLabel(strings.getString("mainframe.label.date.selection"));
        northWestPanel.add(lblDateSelection);

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
        northWestPanel.add(formattedTextField);

        JButton btnFind = ViaggiFrameUtils.newIconButton(
                "/Icons/search16.png",
                strings.getString("mainframe.button.find"),
                e -> loadDateFromInsertedFormattedTextField(),
                null
        );
        northWestPanel.add(btnFind);

        JButton reloadButton = ViaggiFrameUtils.newIconButton(
                "/Icons/reload16.png",
                strings.getString("mainframe.button.reload"),
                reloadAction,
                null
        );
        northWestPanel.add(reloadButton);

        JButton btnGoToLastDate = ViaggiFrameUtils.newButton(
                strings.getString("mainframe.button.last.date"),
                goToLastDateAction,
                null
        );
        northWestPanel.add(btnGoToLastDate);

        JButton btnResetColumnsSize = ViaggiFrameUtils.newButton(
                strings.getString("mainframe.button.reset.columns.size"),
                resetDefaultColumnsSize,
                null
        );
        northWestPanel.add(btnResetColumnsSize);

        lblDataSelezionata = ViaggiFrameUtils.newJLabel(
                strings.getString("mainframe.label.date.selected"),
                TAHOMA_DEFAULT_FONT.deriveFont(15f)
        );
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(1, 1,1, 1);
        northCenterPanel.add(lblDataSelezionata, gbc);

        giornoSettLabel = ViaggiFrameUtils.newJLabel(
                "",
                TAHOMA_DEFAULT_FONT.deriveFont(Font.BOLD, 20)
        );
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        northCenterPanel.add(giornoSettLabel, gbc);

        selectedDateLbl = ViaggiFrameUtils.newJLabel(
                "",
                TAHOMA_DEFAULT_FONT.deriveFont(Font.BOLD, 20)
        );
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; gbc.insets = new Insets(1, 3,1, 3);
        northCenterPanel.add(selectedDateLbl, gbc);

        JButton btnDelete = ViaggiFrameUtils.newIconButton(
                "/Icons/delete16.png",
                strings.getString("mainframe.button.date.delete"),
                deleteThisDayAction,
                null
        );
        northEastPanel.add(btnDelete);

        JButton btnGestisciCamion = ViaggiFrameUtils.newButton(
                strings.getString("mainframe.button.camion.managment"),
                openCamionFrameAction,
                null
        );
        northEastPanel.add(btnGestisciCamion);

        JButton btnAggiungiGiornata = ViaggiFrameUtils.newButton(
                strings.getString("mainframe.button.date.add"),
                openNewDateFrameAction,
                null
        );
        northEastPanel.add(btnAggiungiGiornata);

        btnEsportaQuestaData = ViaggiFrameUtils.newButton(
                strings.getString("mainframe.button.date.export"),
                dateExportAction,
                null
        );
        northEastPanel.add(btnEsportaQuestaData);

        northPanel.add(northWestPanel, BorderLayout.WEST);
        northPanel.add(northCenterPanel, BorderLayout.CENTER);
        northPanel.add(northEastPanel, BorderLayout.EAST);
        contentPane.add(northPanel, BorderLayout.NORTH);

        tablePanel = new JPanel(new GridBagLayout());/*
        tablePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);*/
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
//        ordiniSalitaTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//        ordiniSalitaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordiniSalitaTable.setCellSelectionEnabled(true);
        ordiniSalitaTable.setModel(new OrdiniTableModel());
        ordiniSalitaTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
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
//        ordiniDiscesaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        ordiniDiscesaTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ordiniDiscesaTable.setModel(new OrdiniTableModel());
        ordiniDiscesaTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        lis = new OrdiniTableListener(dbs);
        lis.setUpdateWorkerListener(ordiniUpdateResultListener);
        ordiniSalitaTable.getModel().addTableModelListener(lis);
        ordiniDiscesaTable.getModel().addTableModelListener(lis);

        scrollPane.setViewportView(ordiniDiscesaTable);

/*        clientiTableSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, salitaPanel, discesaPanel);
        clientiTableSplitPane.setVerifyInputWhenFocusTarget(false);
        clientiTableSplitPane.setDividerSize(1);
        clientiTableSplitPane.setBorder(null);
        clientiTableSplitPane.setLayout(new BoxLayout(clientiTableSplitPane, BoxLayout.Y_AXIS));*/


        centerLeftPanel = new JPanel(new GridBagLayout());
        tablePanel.add(centerLeftPanel, new GridBagConstraints(0, 0, 1, 1, 0.5, 0.5, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));
//        centerLeftPanel.add(clientiTableSplitPane);
        notePanel = new JPanel(new BorderLayout(0, 0));
//        centerLeftPanel.add(notePanel, BorderLayout.SOUTH);

        noteScrollPane = new JScrollPane();

        noteTable = new JTable();
        noteTable.setModel(new NoteTableModel());
        noteTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        noteListener = new NoteTableListener(dbs);
        noteListener.setResultListener(noteUpdateResultListener);
        noteTable.getModel().addTableModelListener(noteListener);
        noteScrollPane.setViewportView(noteTable);

        noteButtonPanel = new JPanel(new BorderLayout(0, 0));
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
        notePanel.add(noteButtonPanel, BorderLayout.NORTH);
        notePanel.add(noteScrollPane);


        centerLeftPanel.add(salitaPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
                new Insets(1,1 ,1, 1), 1, 1));
        centerLeftPanel.add(discesaPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(1,1 ,1, 1), 1, 1));
        centerLeftPanel.add(notePanel, new GridBagConstraints(0, 2, 1, 1, 0.5, 0.5,
                GridBagConstraints.SOUTH, GridBagConstraints.BOTH,
                new Insets(1,1 ,1, 1), 1, 1));


        sudTablePanel = new JPanel(new BorderLayout(0, 0));
//        tablePanel.add(sudTablePanel);

        viaggiSudTable = new ViaggiJTable(Consts.TABLE_TYPES.VIAGGI_SUD);
//        tablePanel.add(viaggiSudTable);
        viaggiSudTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        //TODO: viaggi sud table model definition goes here
        sudTableButtonPanel = new JPanel(new BorderLayout(0, 0));
        sudTablePanel.add(sudTableButtonPanel, BorderLayout.NORTH);

        panel_3 = new JPanel(new BorderLayout(0, 0));
        tablePanel.add(panel_3, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));

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
        sudTableScrollPane.setBorder(BorderFactory.createEmptyBorder());

        viaggiSudPinTable = new ViaggiJTable(Consts.TABLE_TYPES.VIAGGI_SUD);
        viaggiSudPinTable.setTableHeader(null);
        viaggiSudPinTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        //TODO: viaggi sud pin table model definition goes here
        sudPinScrollPane = new JScrollPane(viaggiSudPinTable);
        sudPinScrollPane.setBorder(BorderFactory.createEmptyBorder());

        viaggiSudSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sudTableScrollPane, sudPinScrollPane);
        viaggiSudSplitPane.setDividerSize(3);
        sudTablePanel.add(viaggiSudSplitPane, BorderLayout.CENTER);

        nordTablePanel = new JPanel(new BorderLayout(0, 0));
//        tablePanel.add(nordTablePanel);

        viaggiNordTable = new ViaggiJTable(Consts.TABLE_TYPES.VIAGGI_NORD);
//        tablePanel.add(viaggiNordTable);
        viaggiNordTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
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
        nordTableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        //nordTablePanel.add(nordTableScrollPane, BorderLayout.CENTER);

        viaggiNordPinTable = new ViaggiJTable(Consts.TABLE_TYPES.VIAGGI_NORD);


        viaggiNordPinTable.setTableHeader(null);
        viaggiNordPinTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        //TODO: viaggi nord pin table model declaration goes here
        nordPinScrollPane = new JScrollPane(viaggiNordPinTable);
        nordPinScrollPane.setBorder(BorderFactory.createEmptyBorder());
        viaggiNordSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, nordTableScrollPane, nordPinScrollPane);
        viaggiNordSplitPane.setDividerSize(3);

        nordTablePanel.add(viaggiNordSplitPane, BorderLayout.CENTER);

        TableColumnModelListener nordTableColumnListener = new ColumnChangeListener(viaggiNordTable, viaggiNordPinTable);
        TableColumnModelListener sudTableColumnListener = new ColumnChangeListener(viaggiSudTable, viaggiSudPinTable);

        viaggiNordTable.getColumnModel().addColumnModelListener(nordTableColumnListener);
        viaggiSudTable.getColumnModel().addColumnModelListener(sudTableColumnListener);
//        tableColumnModelListenerMap.put(viaggiNordTable, nordTableColumnListener);
//        tableColumnModelListenerMap.put(viaggiSudTable, sudTableColumnListener);

        viaggiSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, nordTablePanel, sudTablePanel);
        viaggiSplitPane.setDividerSize(0);
        viaggiSplitPane.setBorder(BorderFactory.createEmptyBorder());
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

        logger.info("mainframe: retrieving last date from database...");

        Date lastDateFromDb = null;
        try {
            lastDateFromDb = dbs.getDataAggiornamento();
            logger.verbose("mainframe: date returned from databse: {}", lastDateFromDb.toString());
            loadDate(lastDateFromDb, MainFrame.RELOAD_STANDARD);
        } catch (SQLException e) {
            logger.warn("mainframe: database last date retrieving error logged in next line");
            logDatabaseError(e);
        }

        logger.verbose("Loading columns preferences...");
        MainFrameColumnsSize mainFrameColumnsSize;
        try {
            CharSource columnsFileSource = Files.asCharSource(new File(ViaggiUtils.getColumnsPreferencesPath()), Charsets.UTF_8);
            String columnsPreferencesJSON = columnsFileSource.read();
            mainFrameColumnsSize = MainFrameColumnsSize.fromJson(columnsPreferencesJSON);
        } catch (IOException exception) {
            logger.warn("Columns preferences file error. ({})", exception.getMessage());
            logger.warn("Will be loaded default configuration");
            mainFrameColumnsSize = null;
        }

        formattaTabelle(mainFrameColumnsSize);
        createTablesPopupMenu();
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
            panel.setBorder(BorderFactory.createTitledBorder("panel"));
            panel_1.setBorder(BorderFactory.createTitledBorder("panel_1"));
            panel_2.setBorder(BorderFactory.createTitledBorder("panel_2"));
            panel_3.setBorder(BorderFactory.createTitledBorder("panel_3"));
            panel_4.setBorder(BorderFactory.createTitledBorder("panel_4"));
            centerLeftPanel.setBorder(BorderFactory.createTitledBorder("centerLeftPanel"));
            panel_6.setBorder(BorderFactory.createTitledBorder("panel_6"));
            panel_8.setBorder(BorderFactory.createTitledBorder("panel_8"));
            assNonAssicuratiPanel.setBorder(BorderFactory.createTitledBorder("assNonAssicuratiPanel"));
//            clientiTableSplitPane.setBorder(BorderFactory.createTitledBorder("clientiTableSplitPane"));
            discesaPanel.setBorder(BorderFactory.createTitledBorder("discesaPanel"));
            nordTableButtonPanel.setBorder(BorderFactory.createTitledBorder("nordTableButtonPanel"));
            nordTablePanel.setBorder(BorderFactory.createTitledBorder("nordTablePanel"));
            noteButtonPanel.setBorder(BorderFactory.createTitledBorder("noteButtonPanel"));
            notePanel.setBorder(BorderFactory.createTitledBorder("notePanel"));
            ordiniDiscesaButtonPanel.setBorder(BorderFactory.createTitledBorder("ordiniDiscesaButtonPanel"));
            ordiniSalitaTableButtonPanel.setBorder(BorderFactory.createTitledBorder("ordiniSalitaTableButtonPanel"));
            otherPanel.setBorder(BorderFactory.createTitledBorder("otherPanel"));
            salitaPanel.setBorder(BorderFactory.createTitledBorder("salitaPanel"));
            sudTablePanel.setBorder(BorderFactory.createTitledBorder("sudTablePanel"));
            sudTableButtonPanel.setBorder(BorderFactory.createTitledBorder("sudTableButtonPanel"));
            assNonAssicuratiPanel.setBorder(BorderFactory.createTitledBorder("assNonAssicuratiPanel"));
            northEastPanel.setBorder(BorderFactory.createTitledBorder("northEastPanel"));
            northCenterPanel.setBorder(BorderFactory.createTitledBorder("northCenterPanel"));
            northWestPanel.setBorder(BorderFactory.createTitledBorder("northWestPanel"));
            contentPane.setBorder(BorderFactory.createTitledBorder("contentPane"));
            northPanel.setBorder(BorderFactory.createTitledBorder("northPanel"));
            tablePanel.setBorder(BorderFactory.createTitledBorder("tablePanel"));

            southPanel.setBorder(BorderFactory.createTitledBorder("southPanel"));
            sudTableScrollPane.setBorder(BorderFactory.createTitledBorder("sudTableScrollPane"));
            nordTableScrollPane.setBorder(BorderFactory.createTitledBorder("nordTableScrollPane"));
            clientiTableScrollPane.setBorder(BorderFactory.createTitledBorder("clientiTableScrollPane"));
            scrollPane.setBorder(BorderFactory.createTitledBorder("scrollPane"));
            fermiScrollPane.setBorder(BorderFactory.createTitledBorder("fermiScrollPane"));
            nonAssicuratiScrollPane.setBorder(BorderFactory.createTitledBorder("nonAssicuratiScrollPane"));
            noteScrollPane.setBorder(BorderFactory.createTitledBorder("noteScrollPane"));
            viaggiSplitPane.setBorder(BorderFactory.createTitledBorder("viaggiSplitPane"));
//            clientiTableSplitPane.setBorder(BorderFactory.createTitledBorder("clientiTableSplitPane"));
        }
    }

/*    @Subscribe
    public void updateCamionList(CamionEvent e) {
//        camions = dbs.getCamion();
//        ((ViaggiTableModel) viaggiNordTable.getModel()).setCamions(camions);
//        sudTableModel.setCamions(camions);
        formattaTabelle();
    }*/


    public void reloadTableModel(Date d) throws SQLException {
        logger.info("reloadTableModel: reloading viaggi table models...");

        logger.verbose("reloadTableModel: creating Viaggi NORD table model...");
        viaggiNordTable.setModel(new ViaggiTableModel(Consts.TABLE_TYPES.VIAGGI_NORD));
        viaggiNordTable.getModel().addTableModelListener(this);
        viaggiNordTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());

        logger.verbose("reloadTableModel: creating Viaggi SUD table model...");
        viaggiSudTable.setModel(new ViaggiTableModel(Consts.TABLE_TYPES.VIAGGI_SUD));
        viaggiSudTable.getModel().addTableModelListener(this);
        viaggiSudTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());

        logger.verbose("reloadTableModel: creating Viaggi NORD Pin table model...");
        viaggiNordPinTable.setModel(new ViaggiPinTableModel(Consts.TABLE_TYPES.VIAGGI_NORD));
        viaggiNordPinTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());
        ViaggiTableModel viaggiNordPinTableModel = (ViaggiTableModel) viaggiNordPinTable.getModel();
        viaggiNordPinTableModel.addTableModelListener(viaggiPinnedTableListener);

        logger.verbose("reloadTableModel: creating Viaggi SUD Pin table model...");
        viaggiSudPinTable.setModel(new ViaggiPinTableModel(Consts.TABLE_TYPES.VIAGGI_SUD));
        viaggiSudPinTable.getColumnModel().getColumn(1).setCellRenderer(new ViaggiCarattCellRender());
        ViaggiTableModel viaggiSudPinTableModel = (ViaggiTableModel) viaggiSudPinTable.getModel();
        viaggiSudPinTableModel.addTableModelListener(viaggiPinnedTableListener);

        ViaggiTableModel nordTableModel = ((ViaggiTableModel) viaggiNordTable.getModel());
        ViaggiTableModel sudTableModel = (ViaggiTableModel) viaggiSudTable.getModel();

        logger.info("reloadTableModel: retrieving NORD viaggi from database...");
        Vector<Viaggio> nord = dbs.getViaggiBy(Viaggio.NORD, d);

        logger.info("reloadTableModel: retrieving NORD viaggi success! Retrieving SUD viaggi from database...");
        Vector<Viaggio> sud = dbs.getViaggiBy(Viaggio.SUD, d);
        logger.info("reloadTableModel: retrieving SUD viaggi success! Moving retrieved data into tables...");

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

        // with setAutoCreateColumnsFromModel flag setted every time we create a new table model,
        // columns width would be reset
        // so after the first time Column Model is created automatically, we stop this behavior
        // (we can -hopefully- think that the Column Model never change when a new table model is created)
        viaggiNordTable.setAutoCreateColumnsFromModel(false);
        viaggiSudTable.setAutoCreateColumnsFromModel(false);
        viaggiNordPinTable.setAutoCreateColumnsFromModel(false);
        viaggiSudPinTable.setAutoCreateColumnsFromModel(false);

        logger.info("reloadTableModel: viaggi table models loaded successfully!");
    }


    public void reloadOrdiniModel(Date d) throws SQLException {
        logger.info("reloadOrdiniModel: reloading ordini table models...");

        OrdiniTableModel tmSalita = (OrdiniTableModel) ordiniSalitaTable.getModel();
        OrdiniTableModel tmDiscesa = (OrdiniTableModel) ordiniDiscesaTable.getModel();

        Vector<Ordine> salite = new Vector<>();
        Vector<Ordine> discese = new Vector<>();

        logger.info("reloadOrdiniModel: retrieving ordini from database...");
        Vector<Ordine> fromDB = dbs.getOrdiniByDate(d);
        logger.info("reloadOrdiniModel: ordini retrieved from database successfully! Moving ordini into tables...");

        for(Ordine o : fromDB) {
            if(Ordine.SALITA.toLowerCase().equals(o.getType().toLowerCase())) {
                salite.addElement(o);
            } else if(Ordine.DISCESA.toLowerCase().equals(o.getType().toLowerCase())) {
                discese.addElement(o);
            }
        }

        tmSalita.setData(salite);
        tmDiscesa.setData(discese);

        logger.info("reloadOrdiniModel: ordini table models loaded successfully!");
    }


    public void reloadNote(Date d) throws SQLException {
        logger.info("reloadNote: reloading note...");

        fermiNota = null;
        nonAssNota = null;

        fermiTxt.setText("");
        nonAssicuratiTxt.setText("");
        Vector<Nota> fromDB;
        Vector<Nota> toNoteTable = new Vector<>();
        NoteTableModel tm = (NoteTableModel) noteTable.getModel();

        logger.info("reloadNote: retrieving note from database...");
        fromDB = dbs.getNoteByDate(d);
        logger.info("reloadNote: note retrieved from database successfully!");

        for(Nota n : fromDB){
            if(Nota.NOTA.equals(n.getTipo())){
                toNoteTable.addElement(n);
            }else if(Nota.FERMI.equals(n.getTipo())){
                fermiNota = n;
                fermiTxt.setText(fermiNota.getTesto());
            }else if(Nota.NONASS.equals(n.getTipo())){
                nonAssNota = n;
                nonAssicuratiTxt.setText(nonAssNota.getTesto());
            }
        }
        tm.setData(toNoteTable);

        logger.info("reloadNote: node loaded successfully!");
    }


    public void loadDate(Date d, int mode) {
        String modeName = mode == RELOAD_STANDARD ? "RELOAD_STANDARD" : "RELOAD_RESETCONNECTION";
        logger.info("loadDate: loading date '{}' with mode {}", d.toString(), modeName);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int dow = c.get(Calendar.DAY_OF_WEEK) - 1; //-1 because  c.get(Calendar.DAY_OF_WEEK)  starts from 1
        String[] weekDays = strings.getString("generic.week.days").split(",");
        giornoSettLabel.setText(weekDays[dow]);

        try {
            if (mode == RELOAD_RESETCONNECTION) {
                logger.info("loadDate: closing connection...");
                dbs.closeConnection();
                logger.info("loadDate: connection closed");
                logger.info("loadDate: opening new connection...");
                dbs.openConnection();
                logger.info("loadDate: connection opened");
            }

            reloadTableModel(d);
            reloadOrdiniModel(d);
            reloadNote(d);
            currentDate = d;
            ThreadContext.put("currentDate", ViaggiUtils.createStringFromDate(currentDate, true));
            selectedDateLbl.setText(ViaggiUtils.createStringFromDate(currentDate, false));

//            formattaTabelle();
            //updateCamionList();
            resizePinTables();
            logger.info("loadDate: date loaded");
            infoTextField.setInfoMessage(strings.getString("mainframe.infofield.welcome"));
        } catch (SQLException e) {
            logger.warn("loadDate: load date database error logged in next line");
            logDatabaseError(e);
        }
    }

    private void loadDateFromInsertedFormattedTextField() {
        String text = formattedTextField.getText();
        Date d = null;
        try{
            d = ViaggiUtils.checkAndCreateDate(text, "/", false);
        }catch(NumberFormatException ex){
            //logger.info(ex.getMessage(), ex);
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
                v = tm.getElementAt(row);
                logger.info("tableChanged: updated column {} ({}) in row {} of table {}",
                        col,
                        ViaggiTableModel.getViaggioColumnNameByIndex(tm, col),
                        row,
                        ViaggiTableModel.getTableModelName(tm)
                );

                logger.info("tableChanged: sending update to database...");
                logger.verbose("tableChanged: element to update: {}", v.toString());
                ViaggiUpdateWorker.connect(dbs)
                        .update(v, col)
                        .onResult(new UpdateWorkerAdapter<Viaggio>() {
                            @Override
                            public void onUpdate(Viaggio updated, int col) {
                                logger.info("tableChanged: viaggio update sent to database successfully!");
                                logger.verbose("tableChanged: element returned from database: {}", updated.toString());
                                String value = String.valueOf(ViaggiTableModel.getViaggioValueByColumnIndex(updated, col));
                                String columnName = Viaggio.NORD.equals(updated.getPosizione())
                                        ? viaggiNordTable.getColumnName(col)
                                        : viaggiSudTable.getColumnName(col);

                                notifyRowUpdated("Viaggi " + updated.getPosizione(), columnName, value, updated.getData().toString());
                            }

                            @Override
                            public void onError(Exception error) {
                                logger.warn("tableChanged: viaggio update database error logged in next line");
                                MainFrame.this.onError(error);
                            }
                        })
                        .execute();
            }else if(e.getType() == TableModelEvent.INSERT){
                logger.info("tableChanged: inserted new row into table {}", ViaggiTableModel.getTableModelName(tm));
                Viaggio nuovo = tm.getElementAt(row);
                logger.verbose("tableChanged: viaggio inserted: {}", nuovo.toString());
                if(nuovo.getId() >= 0) { //Inserted a row yet in database, so we haven't to insert into database
                    logger.verbose("tableChanged: new row id >= 0. No further actions required");
                    return;
                }

                logger.info("tableChanged: sending new viaggio to database...");
                ViaggiUpdateWorker.connect(dbs)
                        .insert(nuovo)
                        .onResult(new UpdateWorkerAdapter<Viaggio>() {
                            @Override
                            public void onError(Exception error) {
                                logger.warn("tableChanged: viaggio insert database error logged in next line");
                                MainFrame.this.onError(error);
                            }
                            @Override
                            public void onInsert(Viaggio inserted, long newId) {
                                logger.info("tableChanged: new viaggio sent successfully! New viaggio id = {}", newId);
                                logger.verbose("tableChanged: viaggio returned from database: {}", inserted.toString());

                                if(newId > 0){
                                    tm.getElementAt(row).setId(newId);
                                } else {
                                    onError(new IllegalArgumentException("Invalid id returned from database"));
                                }

                                JTable t = tm.getType() == Consts.TABLE_TYPES.VIAGGI_NORD ? viaggiNordTable : viaggiSudTable;
                                ViaggiFrameUtils.selectTableCell(t, row, 0);

                                notifyRowInserted("Viaggi " + inserted.getPosizione(), ""+newId, inserted.getData().toString());
                            }
                        })
                        .execute();
            }else if(e.getType() == TableModelEvent.DELETE){
                logger.info("tableChanged: viaggio deleted from table {}", ViaggiTableModel.getTableModelName(tm));
                Viaggio removed = tm.getElementAt(row);
                logger.verbose("tableChanged: element removed from table: {}", removed.toString());
            }
        }
    }

    private void createTablesPopupMenu(){
        // https://stackoverflow.com/questions/16743427/jtable-right-click-popup-menu

        // Nord Table
        JPopupMenu nordPopupMenu = new JPopupMenu();
        nordPopupMenu.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.move.down"),
                viaggiPinAction,
                NORD_PIN_COMMAND
        ));
        nordPopupMenu.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.remove"),
                viaggiRowsAction,
                NORD_REMOVE_ROW_COMMAND
        ));
        nordPopupMenu.addPopupMenuListener(new TablePopupMenuListener(viaggiNordTable));
        viaggiNordTable.setComponentPopupMenu(nordPopupMenu);

        // Sud Table
        JPopupMenu sudPopupMenu = new JPopupMenu();
        sudPopupMenu.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.move.down"),
                viaggiPinAction,
                SUD_PIN_COMMAND
        ));
        sudPopupMenu.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.remove"),
                viaggiRowsAction,
                SUD_REMOVE_ROW_COMMAND
        ));
        sudPopupMenu.addPopupMenuListener(new TablePopupMenuListener(viaggiSudTable));
        viaggiSudTable.setComponentPopupMenu(sudPopupMenu);

        //Nord Pin Table
        JPopupMenu nordPinMenu = new JPopupMenu();
        nordPinMenu.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.move.up"),
                viaggiUnPinAction,
                NORD_UNPIN_COMMAND
        ));
        nordPinMenu.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.remove"),
                viaggiRowsAction,
                NORD_PIN_REMOVE_COMMAND
        ));
        nordPinMenu.addPopupMenuListener(new TablePopupMenuListener(viaggiNordPinTable));
        viaggiNordPinTable.setComponentPopupMenu(nordPinMenu);

        //Sud Pin Table
        JPopupMenu sudPinMenu = new JPopupMenu();
        sudPinMenu.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.move.up"),
                viaggiUnPinAction,
                SUD_UNPIN_COMMAND
        ));
        sudPinMenu.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.remove"),
                viaggiRowsAction,
                SUD_PIN_REMOVE_COMMAND
        ));
        sudPinMenu.addPopupMenuListener(new TablePopupMenuListener(viaggiSudPinTable));
        viaggiSudPinTable.setComponentPopupMenu(sudPinMenu);

        // Oridini salita
        JPopupMenu ordiniPopup = new JPopupMenu();
        ordiniPopup.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.remove"),
                removeOrderAction,
                ORDINI_SALITA_COMMAND
        ));
        ordiniPopup.addPopupMenuListener(new TablePopupMenuListener(ordiniSalitaTable));
        ordiniSalitaTable.setComponentPopupMenu(ordiniPopup);

        // Oridini discesa
        ordiniPopup = new JPopupMenu();
        ordiniPopup.add(ViaggiFrameUtils.newMenuItemButton(
                strings.getString("mainframe.menu.button.remove"),
                removeOrderAction,
                ORDINI_DISCESA_COMMAND
        ));
        ordiniPopup.addPopupMenuListener(new TablePopupMenuListener(ordiniDiscesaTable));
        ordiniDiscesaTable.setComponentPopupMenu(ordiniPopup);
    }


    private void formattaTabelle(@Nullable MainFrameColumnsSize mainFrameColumnsSize){
        if(mainFrameColumnsSize == null) {
            try {
                String defaultColumnsPreferencesJSON = Files.asCharSource(
                        new File(ViaggiUtils.getAppPath("DefaultColumnsPreferences.json")),
                        Charsets.UTF_8
                ).read();
                mainFrameColumnsSize = MainFrameColumnsSize.fromJson(defaultColumnsPreferencesJSON);
            } catch (IOException e) {
                logger.warn("formattaTabelle: loading DefaultColumnsPreferences.json failed ({})", e.getMessage());
                mainFrameColumnsSize = new MainFrameColumnsSize();
            }
        }

        //viaggiSplitPane.setDividerLocation(0.5);
        //TODO: persist user columns width preferences (maybe per date?)
        viaggiSplitPane.setResizeWeight(0.5);

        viaggiNordTable.doTableLayout(mainFrameColumnsSize.viaggiNord);
        viaggiSudTable.doTableLayout(mainFrameColumnsSize.viaggiSud);
        viaggiNordPinTable.doTableLayout(mainFrameColumnsSize.viaggiNord);
        viaggiSudPinTable.doTableLayout(mainFrameColumnsSize.viaggiSud);

        ordiniDiscesaTable.doTableLayout(mainFrameColumnsSize.ordiniDiscesa);
        ordiniSalitaTable.doTableLayout(mainFrameColumnsSize.ordiniSalita);
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
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                resizePinTables();
            }

        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                logger.info("windowClosing: exiting...");
                try {
                    logger.info("windowClosing: disconnecting from database helper server...");
                    DatabaseHelperChannel.getInstance().disconnect();
                    logger.info("windowClosing: closing database connection...");
                    dbs.closeConnection();
                    logger.verbose("windowClosing: closing database connection success!");

                    logger.verbose("windowsClosing: Saving columns preferences...");
                    MainFrameColumnsSize mainFrameColumnsSizePreferred = computeMainframeColumnsSize();
                    String JSONEncode = MainFrameColumnsSize.toJson(mainFrameColumnsSizePreferred);
                    Files.asCharSink(new File(ViaggiUtils.getColumnsPreferencesPath()), Charsets.UTF_8)
                            .write(JSONEncode);
                } catch (SQLException e) {
                    logger.warn("windowsClosing: disconnection error logged in next line");
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
                } catch (IOException e) {
                    logger.warn("windowsClosing: column preferences write error logged in next line");
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    private MainFrameColumnsSize computeMainframeColumnsSize() {
        return new MainFrameColumnsSize(
                viaggiNordTable.getColumnsSize(),
                viaggiSudTable.getColumnsSize(),
                ordiniSalitaTable.getColumnsSize(),
                ordiniDiscesaTable.getColumnsSize()
        );
    }

    private void logDatabaseError(SQLException e){
        logger.error("DATABASE", e);
        e.printStackTrace();
        Msg.error(this, MessageFormat.format(strings.getString("database.error"), e.getErrorCode(), e.getMessage()));
    }

    private void notifyRowUpdated(String table, String columnName, String newValue, String date) {
        logger.verbose("notifyRowUpdated: updated row on table '{}'. Col {}. Value '{}", table, columnName, newValue);
        String updateMessage = MessageFormat.format(strings.getString("mainframe.infofield.table.row.update"), table, newValue, columnName);
        updateMessage = dbhNotifyUpdateToClients(table, date, updateMessage);

        infoTextField.setInfoMessage(updateMessage);
    }

    private void notifyRowUpdated(String table, String date, String messageCustomText) {
        logger.verbose("notifyRowUpdated: updated row on table '{}'. Update message: '{}'", table, messageCustomText);
        String updateMessage = table + ": " + messageCustomText;
        updateMessage = dbhNotifyUpdateToClients(table, date, updateMessage);

        infoTextField.setInfoMessage(updateMessage);
    }

    private String dbhNotifyUpdateToClients(String table, String date, String updateMessage) {
        try {
            logger.verbose("notifyRowUpdated: sending update info to other clients...");
            DatabaseHelperChannel.getInstance().notifyRowUpdated(table, date);
            logger.verbose("notifyRowUpdated: sending update info to other clients success!");
        } catch (DatabaseHelperException e) {
            logger.warn("notifyRowUpdated: sending update info to other clients error logged in next line");
            logger.error("DBH" , e);
            updateMessage += ". " + strings.getString("mainframe.infofield.update.communication.fail");
        }
        return updateMessage;
    }

    private void notifyRowInserted(String table, String newId, String date) {
        logger.verbose("notifyRowInserted: inserted new row in table {} with id {}",  table, newId);
        String insertMessage = MessageFormat.format(strings.getString("mainframe.infofield.table.row.insert"), table, newId);
        try {
            logger.verbose("notifyRowInserted: sending update info to other clients...");
            DatabaseHelperChannel.getInstance().notifyRowInserted(table, date);
            logger.verbose("notifyRowInserted: sending update info to other clients success!");
        } catch (DatabaseHelperException e) {
            logger.warn("notifyRowInserted: sending update info to other clients error logged in next line");
            logger.error("DBH", e);
            insertMessage += " " + strings.getString("mainframe.infofield.update.communication.fail");
        }

        infoTextField.setInfoMessage(insertMessage);
    }

    private void onError(Exception error) {
        logger.error(error.getMessage(), error);

        if (error instanceof SQLException) {
            infoTextField.setErrorMessage(strings.getString("mainframe.infofield.database.error"));
        } else if (error instanceof DatabaseHelperException) {
            infoTextField.setWarnMessage(strings.getString("database.helper.communication.fail"));
        }

        infoTextField.setText(infoTextField.getText() + ". " + strings.getString("mainframe.infofield.click.more.info"));
        infoTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Msg.error(getParent(), error.getMessage());
            }
        });
    }

    //LISTENERS FOR EventBus EVENTS
    @Subscribe
    public void onDateEvent(DateEvent event) {
        String dateString = ViaggiUtils.createStringFromDate(event.date, false);
        if(event instanceof DateDeleteEvent) {
            logger.verbose("onDateEvent: received new DateDeleteEvent. Removed date: {}", dateString);
            onDateRemoved(event.date, event.whoName, event.timestamp);
        } else if(event instanceof DateAddEvent) {
            DateAddEvent dateAddEvent = ((DateAddEvent) event);

            switch (dateAddEvent.getSource()) {
                case ADD_DATE_FRAME:
                    logger.verbose("onDateEvent: received new DateAddEvent from ADD_DATE_FRAME. New last date: {}", dateString);
                    loadDate(dateAddEvent.getDate(), RELOAD_RESETCONNECTION);
                    break;
                case DATABASE_HELPER:
                    logger.verbose("onDateEvent: received new DateAddEvent from DATABASE_HELPER. New last date: {}", dateString);
                    onDateAdded(dateAddEvent.getDate(), dateAddEvent.getWhoName(), dateAddEvent.getTimestamp());
                    break;
            }
        }
    }

    @Subscribe
    public void onDbhRowEvent(RowEvent event) {
        String dateString = ViaggiUtils.createStringFromDate(event.date, true);
        String logString = "", textFieldString = "";
        long elapsedSeconds = (System.currentTimeMillis() - event.timestamp) / 1000;

        if(event instanceof RowInsertEvent) {
            logString = "inserted";
            textFieldString = MessageFormat.format(strings.getString("database.helper.table.row.insert"), event.tableName, event.whoName, elapsedSeconds);
        } else if(event instanceof RowUpdateEvent) {
            logString = "updated";
            textFieldString = MessageFormat.format(strings.getString("database.helper.table.row.update"), event.tableName, event.whoName, elapsedSeconds);
        } else if(event instanceof RowDeleteEvent) {
            //TODO: not implemented yet
        }

        logger.info("onRowUpdated: received database helper message: row {} into table '{}' and date '{}'", logString, event.tableName, dateString);

        if(currentDate.equals(event.date)) {
            infoTextField.setUploadMessage(textFieldString);
            infoTextField.addMouseListener(aggiornaOnClickAdapter);
        }
    }

    @Subscribe
    public void onSocketEvent(SocketEvent event) {
        if(event instanceof SocketConnectEvent) {
            switch (((SocketConnectEvent) event).type) {
                case CONNECT:
                    infoTextField.setUploadMessage(strings.getString("database.helper.connection.success"));
                    break;
                case RECONNECT:
                    infoTextField.setUploadMessage(strings.getString("database.helper.reconnect.success"));
                    break;
            }

            dbhConnectionErrorShowed = false;
        } else if(event instanceof SocketConnectionErrorEvent) {
            if(!dbhConnectionErrorShowed) {
                infoTextField.setWarnMessage(strings.getString("database.helper.connection.error"));
                dbhConnectionErrorShowed = true;
            }
        } else if(event instanceof SocketErrorEvent) {
            SocketErrorEvent socketErrorEvent = ((SocketErrorEvent) event);
            if(socketErrorEvent.hasException()) {
                onError(socketErrorEvent.getException());
            } else {
                infoTextField.setWarnMessage(infoTextField.getText() + ". " + strings.getString("database.helper.communication.fail"));
            }
            dbhConnectionErrorShowed = true;
        }
    }

    private void onDateAdded(Date date, String who, long timestamp) {
        logger.info("onDateAdded: received database helper message: new date added '{}'", date);
        long elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000;
        infoTextField.setUploadMessage(MessageFormat.format(strings.getString("database.helper.day.add"), who, elapsedSeconds));
        infoTextField.addMouseListener(aggiornaOnClickAdapter);
    }


    private void onDateRemoved(Date date, String who, long timestamp) {
        logger.info("onDateRemoved: received database helper message: date removed '{}'", ViaggiUtils.createStringFromDate(date, true));
        if(date.equals(currentDate)) {
            EventQueue.invokeLater(() -> {
                Msg.warn(MainFrame.this, MessageFormat.format(strings.getString("database.helper.day.deleted"), who));
                try {
                    logger.info("onDateRemoved: retrieving new last date from database...");
                    Date lastDateFromDb = dbs.getDataAggiornamento();
                    loadDate(lastDateFromDb, RELOAD_RESETCONNECTION);
                } catch (SQLException e) {
                    logger.warn("onDateRemoved: retrieving new last date from database error logged in next line");
                    logDatabaseError(e);
                }
            });
        }
    }


    class ColumnChangeListener implements TableColumnModelListener {
        JTable sourceTable;
        JTable targetTable;

        ColumnChangeListener(JTable source, JTable target) {
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

            // NB: uncomment next lines (and all other tableColumnModelListenerMap uses in code if you want also
            // the inverse trigger (if pin table is resized then resize normal table)
//            TableColumnModelListener listener = tableColumnModelListenerMap.get(targetTable);

//            targetModel.removeColumnModelListener(listener);

            for (int i = 0; i < sourceModel.getColumnCount(); i++) {
                targetModel.getColumn(i).setPreferredWidth(sourceModel.getColumn(i).getWidth());
            }

//            targetModel.addColumnModelListener(listener);

        }
    }

    class TablePopupMenuListener implements PopupMenuListener {
        JTable targetTable;

        public TablePopupMenuListener(JTable targetTable) {
            this.targetTable = targetTable;
        }


        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int rowAtPoint = targetTable.rowAtPoint(SwingUtilities.convertPoint(
                            (JPopupMenu) e.getSource(), new Point(0, 0), targetTable)
                    );
                    int columnAtPoint = targetTable.columnAtPoint(SwingUtilities.convertPoint(
                            (JPopupMenu) e.getSource(), new Point(0, 0), targetTable)
                    );

                    if (rowAtPoint > -1 && columnAtPoint > -1) {
//                        targetTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
                        ViaggiFrameUtils.selectTableCell(targetTable, rowAtPoint, columnAtPoint);
                    }
                }
            });
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {

        }
    }
}