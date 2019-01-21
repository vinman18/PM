package it.vin.dev.menzione.frame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.Msg;
import it.vin.dev.menzione.database_helper.DatabaseHelperChannel;
import it.vin.dev.menzione.events.NewDateAddedEvent;
import it.vin.dev.menzione.events.ViaggiEventBus;
import it.vin.dev.menzione.logica.*;
import it.vin.dev.menzione.main_frame.CustomDateTextField;
import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.logica.DatabaseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;


public class AggiungiDataFrame extends JFrame implements TableModelListener {
    private final static boolean DEBUG_FRAME = ViaggiFrameUtils.DEBUG_FRAME;

    private static final String CMD_MOVE_NORD_TO_SUD = "cmd_move_nord_to_sud";
    private static final String CMD_MOVE_SUD_TO_NORD = "cmd_move_sud_to_nord";
    private static final String CMD_NORD_TABLE = "cmd_nord_table";
    private static final String CMD_SUD_TABLE = "cmd_sud_table";

    private JPanel contentPane;
    private JTable viaggiNordTable;
    private JTable viaggiSudTable;
    //    private ViaggiNuoviTableModel nordTM;
//    private ViaggiNuoviTableModel sudTM;
    private Date lastDate;
    private DatabaseService dbu;
    private JFormattedTextField frmtdtxtfldData;
    private JComboBox<String> camionComboBox;
    private Logger logger;
    private Nota fermiToDB;
    private Nota nonAssToDB;
    private List<Camion> camions;

    private Vector<Ordine> ordiniToDB;
    private Vector<Viaggio> toDB;

    private final KeyListener dateFieldKeyListener = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent e) {
            String text;
            int code = e.getKeyCode();
            Date d = null;

            if(code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB){
                text = frmtdtxtfldData.getText();
                try{
                    d = ViaggiUtils.checkAndCreateDate(text, "/", false);
                    viaggiNordTable.requestFocusInWindow();
                    viaggiNordTable.editCellAt(0, 0);
                }catch(NumberFormatException ex){
                    e.getComponent().setBackground(Color.RED);
                }

                if(d == null) {
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
    };

    private ActionListener moveRowActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            JTable srcTable;
            JTable dstTable;

            if(arg0.getActionCommand().equals(CMD_MOVE_NORD_TO_SUD)) {
                srcTable = viaggiNordTable;
                dstTable = viaggiSudTable;
            } else {
                srcTable = viaggiSudTable;
                dstTable = viaggiNordTable;
            }

            int selectedRow = srcTable.getSelectedRow();
            if(srcTable.isEditing()){
                srcTable.getCellEditor().cancelCellEditing();
            }

            if(selectedRow>=0 && selectedRow < srcTable.getRowCount()){
                Viaggio tmp = ((ViaggiNuoviTableModel) srcTable.getModel()).removeRow(selectedRow);
                switch (tmp.getPosizione()) {
                    case Viaggio.SUD:
                        tmp.setPosizione(Viaggio.NORD);
                        break;
                    case Viaggio.NORD:
                        tmp.setPosizione(Viaggio.SUD);
                        break;
                }
                ((ViaggiNuoviTableModel) dstTable.getModel()).addRow(tmp);
                ViaggiFrameUtils.selectTableCell(srcTable, selectedRow, srcTable.getSelectedColumn());
            }
        }
    };

    private final ActionListener addRowAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            JTable table;

            if(e.getActionCommand().equals(CMD_NORD_TABLE)) {
                table = viaggiNordTable;
            } else {
                table = viaggiSudTable;
            }

            ((ViaggiNuoviTableModel) table.getModel()).addRow(null);
        }
    };

    private final ActionListener removeRowAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            JTable table;

            if(e.getActionCommand().equals(CMD_NORD_TABLE)) {
                table = viaggiNordTable;
            } else {
                table = viaggiSudTable;
            }

            int row = table.getSelectedRow();
            if(row >= 0) {
                ((ViaggiNuoviTableModel) table.getModel()).removeRow(row);
                ViaggiFrameUtils.selectTableCell(table, row, table.getSelectedRow());
            }
        }
    };

    /**
     * Create the frame.
     */
    public AggiungiDataFrame() {
        try {
            dbu = DatabaseService.create();
        } catch (SQLException e3) {
            e3.printStackTrace();
        }

//        this.source = source;
        this.toDB = new Vector<>();

        logger = LogManager.getLogger(AggiungiDataFrame.class);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(Toolkit.getDefaultToolkit().createImage(ViaggiUtils.getMainIcon()));

        setBounds(100, 100, 1200, 600);
        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JPanel headerPanel = new JPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);


        JLabel lblInserisciData = new JLabel("Inserisci data:");
        headerPanel.add(lblInserisciData);

        frmtdtxtfldData = new CustomDateTextField();
        frmtdtxtfldData.setFocusTraversalKeysEnabled(false);
        frmtdtxtfldData.addKeyListener(dateFieldKeyListener);

        headerPanel.add(frmtdtxtfldData);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        contentPane.add(centerPanel, BorderLayout.CENTER);
        JPanel panel_4 = new JPanel(new BorderLayout(0, 0));

        JPanel panel_6 = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));

        JButton nordAddButton = ViaggiFrameUtils.newButton("+", addRowAction, CMD_NORD_TABLE);
        JButton NordRemoveButton = ViaggiFrameUtils.newButton("-", removeRowAction, CMD_NORD_TABLE);

        panel_6.add(NordRemoveButton);
        panel_6.add(nordAddButton);
        panel_4.add(panel_6, BorderLayout.NORTH);

        viaggiNordTable = new JTable();
        viaggiNordTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        viaggiNordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        viaggiNordTable.setCellSelectionEnabled(true);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(viaggiNordTable);
        panel_4.add(scrollPane);

        centerPanel.add(panel_4, new GridBagConstraints(
                0, 0, 1, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(10, 10, 10, 10), 1, 1
        ));

        JPanel panel_1 = new JPanel(new GridLayout(2, 1, 10, 60));

        JButton moveLeftButton = ViaggiFrameUtils.newButton(">", moveRowActionListener, CMD_MOVE_NORD_TO_SUD);
        panel_1.add(moveLeftButton);

        JButton moveRightButton = ViaggiFrameUtils.newButton("<", moveRowActionListener, CMD_MOVE_SUD_TO_NORD);
        panel_1.add(moveRightButton);

        centerPanel.add(panel_1, new GridBagConstraints(
                1, 0, 1, 1, 0.1, 0.1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        JPanel panel_5 = new JPanel(new BorderLayout(0, 0));

        JPanel panel_7 = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0,0));

        JButton SudAddButton = ViaggiFrameUtils.newButton("+", addRowAction, CMD_SUD_TABLE);
        JButton SudRemoveButton = ViaggiFrameUtils.newButton("-", removeRowAction, CMD_SUD_TABLE);

        panel_7.add(SudRemoveButton);
        panel_7.add(SudAddButton);
        panel_5.add(panel_7, BorderLayout.NORTH);

        viaggiSudTable = new JTable();
        viaggiSudTable.setCellSelectionEnabled(true);
        viaggiSudTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane_1 = new JScrollPane(viaggiSudTable);
        //scrollPane_1.setPreferredSize(new Dimension(500, 300));
        panel_5.add(scrollPane_1);

        centerPanel.add(panel_5, new GridBagConstraints(
                2, 0, 1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
                new Insets(10, 10, 10, 10), 1, 1));

        JPanel panel_8 = new JPanel(new FlowLayout(FlowLayout.TRAILING));

        JButton SalvaBtn = new JButton("Salva");
        SalvaBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Component root = SwingUtilities.getRoot((Component) arg0.getSource());
                int resp = Msg.yesno(root, "Stai per aggiungere una nuova giornata in data " + frmtdtxtfldData.getText() + ".\nSei sicuro?");

                if (resp == JOptionPane.YES_OPTION) {
                    salva();
                }
            }
        });

        panel_8.add(SalvaBtn);
        contentPane.add(panel_8, BorderLayout.SOUTH);

        try {
            lastDate = dbu.getDataAggiornamento();
            createDatiToDB(lastDate);
        } catch (SQLException e1) {
            databaseError(e1);
        }

        formattaTabelle();

        if(DEBUG_FRAME) {
            panel_1.setBorder(new TitledBorder("panel_1"));
            panel_4.setBorder(new TitledBorder("panel_4"));
            panel_5.setBorder(new TitledBorder("panel_5"));
            panel_6.setBorder(new TitledBorder("panel_6"));
            panel_7.setBorder(new TitledBorder("panel_7"));
            panel_8.setBorder(new TitledBorder("panel_8"));
            centerPanel.setBorder(new TitledBorder("centerPanel"));
            headerPanel.setBorder(new TitledBorder("headerPanel"));
        }

        pack();
    }

    private void salva(){
        ViaggiNuoviTableModel tmSud = (ViaggiNuoviTableModel) viaggiSudTable.getModel();
        ViaggiNuoviTableModel tmNord = (ViaggiNuoviTableModel) viaggiNordTable.getModel();
        boolean nordOk = false;
        boolean sudOK = false;
        Date newLastDate = null;
        try{
            newLastDate = ViaggiUtils.checkAndCreateDate(frmtdtxtfldData.getText(), "/", false);
        }catch(NumberFormatException e){
            Msg.warn(this, "Data inserita non corretta");
        }

        if(newLastDate != null){
            try{
                int giaInToDB = toDB.size();
                boolean giaInToDBOK = false;
                if(newLastDate.after(lastDate)) {
                    Vector<Viaggio> nordViaggi = tmNord.getData();
                    Vector<Viaggio> sudViaggi = tmSud.getData();
                    //Vector<Viaggio> toDB = new Vector<>();
                    int i = 0;
                    //int occorrenzeInNord = 0;
                    //int occorrenzeInSud = 0;
                    for(Viaggio v : toDB) {
                        if(v.getData() == null) {
                            v.setData(newLastDate);
                            i++;
                        }
                    }

                    if(i == giaInToDB) {
                        giaInToDBOK = true;
                        i = 0;
                    }

                    for(Viaggio v : nordViaggi) {
                        v.setData(newLastDate);
                        v.setPosizione(Viaggio.NORD);
                        toDB.add(v);
                        i++;
                    }

                    if(i == nordViaggi.size()) {
                        nordOk = true;
                        i = 0;
                    }

                    if(nordOk){
                        for(Viaggio v : sudViaggi) {

                            v.setData(newLastDate);
                            v.setPosizione(Viaggio.SUD);
                            toDB.addElement(v);
                            i++;

                        }

                        if(i == sudViaggi.size()){
                            sudOK = true;
                        }

                        int sum = tmNord.getData().size() + tmSud.getData().size();

                        //Aggiungo la data agli ordini
                        for(Ordine o : ordiniToDB) {
                            o.setDate(newLastDate);
                        }

                        boolean ordiniOK = false;
                        int ordOK = 0;
                        for(Ordine o : ordiniToDB) {
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

                        if(giaInToDBOK && noteOK && ordiniOK && sudOK && nordOk && (toDB.size() - giaInToDB) == sum) {
                            creaData(toDB, newLastDate);
                        } else {
                            Msg.warn(this, "Impossibile creare la data. Controllare i dati");
                        }
                    }
                } else {
                    Msg.warn(this, "Data inserita precedente all'ultima presente nel database");
                }
            } catch(Exception e) {
                Msg.warn(this, "Impossibile creare la data.\nCausa: " + e.getMessage());
            }

        }
    }


    @SuppressWarnings("Duplicates")
    private void createDatiToDB(Date lastDate) throws SQLException {
        //Creo i camion
        camions = dbu.getCamion();

        ViaggiNuoviTableModel nordTM = new ViaggiNuoviTableModel(Consts.VIAGGI_TM_TYPE_NORD);
        ViaggiNuoviTableModel sudTM = new ViaggiNuoviTableModel(Consts.VIAGGI_TM_TYPE_SUD);
        viaggiNordTable.setModel(nordTM);
        viaggiSudTable.setModel(sudTM);
        viaggiNordTable.getModel().addTableModelListener(this);
        viaggiSudTable.getModel().addTableModelListener(this);

        //Creo i viaggi
        Vector<Viaggio> nordViaggiTMP = dbu.getViaggiBy(Viaggio.NORD, lastDate);
        Vector<Viaggio> sudViaggiTMP = dbu.getViaggiBy(Viaggio.SUD, lastDate);

        Vector<Viaggio> nordViaggi = new Vector<>();
        Vector<Viaggio> sudViaggi = new Vector<>();


        for(Viaggio v : nordViaggiTMP){
            Viaggio vv = new Viaggio();

            vv.setPinned(v.isPinned());
            vv.setCamion(v.getCamion());
            vv.setAutista(v.getAutista());

            if(vv.isPinned()) {
                vv.setPosizione(v.getPosizione());
                vv.setNote(v.getNote());
                //vv.setLitriB(v.getLitriB());
                vv.setSelezionato(v.isSelezionato());
                toDB.add(vv);
                continue;
            }

            if(v.isSelezionato()){
                //vv.setCamion(MainFrame.findCamionByTarga(v.getCamion().getTarga()));
                vv.setPosizione(Viaggio.SUD);
                sudViaggi.addElement(vv);
            }else{
                //vv.setCamion(MainFrame.findCamionByTarga(v.getCamion().getTarga()));
                vv.setPosizione(Viaggio.NORD);
                vv.setData(null);
                toDB.addElement(vv);
            }
        }

        for(Viaggio v : sudViaggiTMP){
            Viaggio vv = new Viaggio();
            vv.setCamion(v.getCamion());
            vv.setAutista(v.getAutista());
            vv.setPinned(v.isPinned());

            if(vv.isPinned()) {
                vv.setPosizione(v.getPosizione());
                vv.setNote(v.getNote());
                //vv.setLitriB(v.getLitriB());
                vv.setSelezionato(v.isSelezionato());
                toDB.add(vv);
                continue;
            }

            if(v.isSelezionato()){
                //vv.setCamion(MainFrame.findCamionByTarga(v.getCamion().getTarga()));
                vv.setPosizione(Viaggio.NORD);
                nordViaggi.addElement(vv);
            }else{
                //vv.setCamion(MainFrame.findCamionByTarga(v.getCamion().getTarga()));
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
                        e1.printStackTrace();
                        logger.error(e1.getMessage(), e1);
                    }
                }
            }
        });


        for(Camion c : camions){
            camionComboBox.addItem(c.getTarga());
        }

        //for(int i=0;i<3;i++){
        //if(i==0){
        DefaultCellEditor cellEdt = new DefaultCellEditor(camionComboBox);
        cellEdt.setClickCountToStart(1);
        viaggiNordTable.getColumnModel().getColumn(0).setCellEditor(cellEdt);
        viaggiSudTable.getColumnModel().getColumn(0).setCellEditor(cellEdt);
        //}
        //}


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
            if(tm.getType() == Consts.VIAGGI_TM_TYPE_NORD){  //TODO: perchè questo if?
                if(col==1){
                    Camion c = tm.getElementAt(row).getCamion();
                    if(tm.getType() == Consts.VIAGGI_TM_TYPE_NORD){
                        if(((ViaggiNuoviTableModel) viaggiSudTable.getModel()).existsCamion(c) > 0) {
                            ((ViaggiNuoviTableModel) viaggiSudTable.getModel()).replaceCaratt(c.getTarga(), c.getCaratteristiche());
                        }
                    }else if(tm.getType() == Consts.VIAGGI_TM_TYPE_SUD){
                        if(((ViaggiNuoviTableModel) viaggiNordTable.getModel()).existsCamion(c) > 0) {
                            ((ViaggiNuoviTableModel) viaggiNordTable.getModel()).replaceCaratt(c.getTarga(), c.getCaratteristiche());
                        }
                    }
                }
            }
        } else if(e.getType() == TableModelEvent.INSERT) {
            if(tm.getType() == Consts.VIAGGI_TM_TYPE_NORD){
                viaggiNordTable.requestFocus();
                viaggiNordTable.changeSelection(row, 0, false, false);
                String t = tm.getElementAt(viaggiNordTable.getSelectedRow()).getCamion().getTarga();
                if(t.trim().isEmpty()) {
                    viaggiNordTable.editCellAt(row, 0);
                }
            } else {
                viaggiSudTable.requestFocus();
                viaggiSudTable.changeSelection(row, 0, false, false);
                String t = tm.getElementAt(viaggiSudTable.getSelectedRow()).getCamion().getTarga();
                if(t.trim().isEmpty()) {
                    viaggiSudTable.editCellAt(row, 0);
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
                Msg.info(this, "Data aggiunta correttamente!");
                DatabaseHelperChannel.getInstance().notifyDateAdded(newLastDate.toString());
                //source.loadDate(newLastDate, MainFrame.RELOAD_RESETCONNECTION);
                ViaggiEventBus.getInstance().post(new NewDateAddedEvent(newLastDate, NewDateAddedEvent.NewDateEventSource.ADD_DATE_FRAME));
                dbu.closeConnection();
                dispose();
            } else {
                Msg.error(this, "Errore nella query al database. I dati non sono stati modificati");
                dbu.closeConnection();
            }
        }catch(SQLException e){
            databaseError(e);
        } catch (RemoteException e) {
            logger.debug(e);
            logger.warn(e.getMessage());
        }

    }

    private void databaseError(SQLException e) {
        e.printStackTrace();
        logger.error(e.getMessage(), e);
        JOptionPane.showMessageDialog(this,
                "Errore di connessione al database" +
                        "Codice errore:"+e.getErrorCode()+"\n"+e.getMessage(),
                "Attenzione",
                JOptionPane.ERROR_MESSAGE);
    }
}
