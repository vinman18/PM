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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.Msg;
import it.vin.dev.menzione.database_helper.DatabaseHelperChannel;
import it.vin.dev.menzione.logica.*;
import it.vin.dev.menzione.main_frame.CustomDateTextField;
import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.logica.DatabaseService;
import it.vin.dev.menzione.main_frame.MainFrame;
import it.vin.dev.menzione.main_frame.ReloadCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;


public class AggiungiDataFrame extends JFrame implements TableModelListener {

    private static final long serialVersionUID = 7870598458313964074L;
    private JPanel contentPane;
    private JTable viaggiNordTable;
    private JTable viaggiSudTable;
    private ViaggiNuoviTableModel nordTM;
    private ViaggiNuoviTableModel sudTM;
    private Date lastDate;
    private DatabaseService dbu;
    private JFormattedTextField frmtdtxtfldData;
    private JComboBox<String> camionComboBox;
    private Logger logger;
    private ReloadCallback source;
    private Nota fermiToDB;
    private Nota nonAssToDB;
    private Vector<Ordine> ordiniToDB;
    private Vector<Viaggio> toDB;


    private List<Camion> camions;
    /**
     * Create the frame.
     */
    public AggiungiDataFrame(ReloadCallback source) {
        try {
            dbu = DatabaseService.create();
        } catch (SQLException e3) {
            e3.printStackTrace();
        }

        this.source = source;
        this.toDB = new Vector<>();

        logger = LogManager.getLogger(AggiungiDataFrame.class);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(Toolkit.getDefaultToolkit().createImage(ViaggiUtils.getMainIcon()));

        setBounds(100, 100, 1200, 487);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel headerPanel = new JPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);


        JLabel lblInserisciData = new JLabel("Inserisci data:");
        headerPanel.add(lblInserisciData);

        frmtdtxtfldData = new CustomDateTextField();

        frmtdtxtfldData.setFocusTraversalKeysEnabled(false);

        frmtdtxtfldData.addKeyListener(new KeyListener() {
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
        });

        headerPanel.add(frmtdtxtfldData);

        JPanel centerPanel = new JPanel();
        contentPane.add(centerPanel, BorderLayout.CENTER);

        try {
            lastDate = dbu.getDataAggiornamento();
            createDatiToDB(lastDate);
        } catch (SQLException e1) {
            databaseError(e1);
        }


        JPanel panel_4 = new JPanel();
        centerPanel.add(panel_4);
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
        centerPanel.add(viaggiNordTable);

        JScrollPane scrollPane = new JScrollPane(viaggiNordTable);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        panel_4.add(scrollPane);

        JPanel panel_1 = new JPanel();
        centerPanel.add(panel_1);
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
        centerPanel.add(panel_5);
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
                Component root = SwingUtilities.getRoot((Component) arg0.getSource());
                int resp = Msg.yesno(root, "Stai per aggiungere una nuova giornata in data " + frmtdtxtfldData.getText() + ".\nSei sicuro?");

                if (resp == JOptionPane.YES_OPTION) {
                    salva();
                }
            }

        });
        panel_8.add(SalvaBtn);

        viaggiNordTable.getModel().addTableModelListener(this);
        viaggiSudTable.getModel().addTableModelListener(this);

        formattaTabelle();
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
                        v.setData(newLastDate);
                        v.setPosizione(Viaggio.NORD);
                        toDB.add(v);
                        i++;
                    }

                    if(i == nordViaggi.size()){
                        nordOk = true;
                        i = 0;
                    }

                    if(nordOk){
                        for(Viaggio v : sudViaggi){

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
                            Msg.warn(this, "Impossibile creare la data. Controllare i dati");
                        }
                    }
                }else{
                    Msg.warn(this, "Data inserita precedente all'ultima presente nel database");
                }
            }catch(Exception e){
                Msg.warn(this, "Impossibile creare la data.\nCausa: " + e.getMessage());
            }

        }
    }

    @SuppressWarnings("Duplicates")
    private void createDatiToDB(Date lastDate) throws SQLException{
        //Creo i camion
        camions = dbu.getCamion();

        nordTM = new ViaggiNuoviTableModel(Consts.VIAGGI_TM_TYPE_NORD, camions);
        sudTM = new ViaggiNuoviTableModel(Consts.VIAGGI_TYPE_SUD, camions);

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
                vv.setLitriB(v.getLitriB());
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
                vv.setLitriB(v.getLitriB());
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
                        if(sudTM.existsCamion(c) > 0){
                            sudTM.replaceCaratt(c.getTarga(), c.getCaratteristiche());
                        }
                    }else if(tm.getType() == Consts.VIAGGI_TYPE_SUD){
                        if(nordTM.existsCamion(c) > 0){
                            nordTM.replaceCaratt(c.getTarga(), c.getCaratteristiche());
                        }
                    }
                }
            }
        }else if(e.getType() == TableModelEvent.INSERT){
            if(tm.getType() == Consts.VIAGGI_TM_TYPE_NORD){
                viaggiNordTable.requestFocus();
                viaggiNordTable.changeSelection(row-1, 0, false, false);
                String t = tm.getElementAt(viaggiNordTable.getSelectedRow()).getCamion().getTarga();
                if(t.trim().isEmpty()){
                    viaggiNordTable.editCellAt(row-1, 0);
                }
            }else {
                viaggiSudTable.requestFocus();
                viaggiSudTable.changeSelection(row-1, 0, false, false);
                String t = tm.getElementAt(viaggiSudTable.getSelectedRow()).getCamion().getTarga();
                if(t.trim().isEmpty()){
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
                Msg.info(this, "Data aggiunta correttamente!");
                DatabaseHelperChannel.getInstance().notifyDateAdded(newLastDate.toString());
                source.loadDate(newLastDate, MainFrame.RELOAD_RESETCONNECTION);
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
