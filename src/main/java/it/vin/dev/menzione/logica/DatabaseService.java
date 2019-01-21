package it.vin.dev.menzione.logica;

import it.vin.dev.menzione.Consts;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("Duplicates")
public class DatabaseService {

	private Connection conn;

	public static DatabaseService create() throws SQLException{
		DatabaseService dbs;
		Connection conn = DatabaseService.createConnection();
		dbs = new DatabaseService(conn);

		if(dbs.getDbVersion() != Consts.DBVERSION) {
            throw new SQLException("Versione del database non compatibile con questa versione del programma");
        }

		return dbs;
	}

	private DatabaseService(Connection conn) {
		this.conn = conn;
	}

	private static Connection createConnection() throws SQLException{
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

        Configuration conf = Configuration.getInstance();
        String ip = conf.getLocation();
        String port = conf.getDbPort();
        String dbName = conf.getDbName();

		String url = "jdbc:mysql://"+ ip + ":" + port + "/" + dbName;
		String user = conf.getUser();
		String password = conf.getPassword();

		return DriverManager.getConnection(url, user, password);
	}

	public void closeConnection() throws SQLException{
		conn.close();
	}

	public void setConnection(Connection conn){
		this.conn = conn;
	}

	public void openConnection() throws SQLException {
	    if(!conn.isClosed()) {
	        throw new SQLException("Connection already opened");
        }

        this.conn = createConnection();
    }

	@Deprecated
	public static DatabaseService dbUtilFactory(Connection conn) {
		return new DatabaseService(conn);
	}

	public void eseguiUpdateQuery(String query) throws SQLException{

		Statement st = conn.createStatement();
		st.executeUpdate(query);
		st.close();
	}

	public ResultSet eseguiQuery(String query) throws SQLException{
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		st.close();
		return rs;
	}

	public long aggiungiOrdine(Ordine o) throws SQLException{
		String data = o.getData();
		String cliente = o.getCliente();
		String note = o.getNote();
		Date date = o.getDate();
		String tipo = o.getType();

		String query = "INSERT INTO Ordini(Data, Cliente, Note, Date, Tipo) VALUES (?,?,?,?,?)";

		PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

		ps.setString(1, data);
		ps.setString(2, cliente);
		ps.setString(3, note);
		ps.setDate(4, date);
		ps.setString(5, tipo);

		ps.executeUpdate();

		long generatedKey;
		try(ResultSet rs = ps.getGeneratedKeys()){
			if(rs.next()){
				generatedKey = rs.getLong(1);
			}else {
				throw new SQLException("Creating Ordine failed, no ID obtained.");
			}
		}
		ps.close();

		return generatedKey;

	}

	public void aggiungiCamion(Camion c) throws SQLException{
		String targa = c.getTarga();
		String caratt = c.getCaratteristiche();

		String query = "INSERT INTO Camion(Targa, Caratteristiche) VALUES (?,?)";
		PreparedStatement st = conn.prepareStatement(query);

		st.setString(1, targa);
		st.setString(2, caratt);

		st.executeUpdate();

		st.close();
	}


	public void rimuoviCamion(Camion c) throws SQLException{
		String query = "DELETE FROM Camion WHERE Targa=?";
		PreparedStatement ps = conn.prepareStatement(query);

		String targa = c.getTarga();

		ps.setString(1, targa);

		ps.executeUpdate();
		ps.close();
	}

	public void rimuoviNota(Nota n) throws SQLException{
		String query = "DELETE FROM Nota WHERE Id=? AND Data=?";

		PreparedStatement ps = conn.prepareStatement(query);
		long id = n.getId();
		Date data = n.getData();
		ps.setLong(1, id);
		ps.setDate(2, data);

		ps.executeUpdate();
		ps.close();
	}

	public void rimuoviOrdine(Ordine o) throws SQLException{

		String query = "DELETE FROM Ordini WHERE Id=? AND Tipo=?";
		PreparedStatement ps = conn.prepareStatement(query);

		long id = o.getId();
		String tipo = o.getType();

		ps.setLong(1, id);
		ps.setString(2, tipo);

		ps.executeUpdate();
		ps.close();
	}

	public long aggiungiViaggio(Viaggio v) throws SQLException{
		String targa = v.getCamion().getTarga();
		String caratt = v.getCamion().getCaratteristiche();
		String autista = v.getAutista();
		String posizione = v.getPosizione();
		String note = v.getNote();
		Date data = v.getData();
		int litri = v.getLitriB();
		boolean selezionato = v.isSelezionato();
		String query = "INSERT INTO Viaggio(Targa,Caratt,Autista,Posizione,Note,Data,Selezionato,Litri) VALUES (?,?,?,?,?,?,?,?)";

		PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

		st.setString(1, targa);
		st.setString(2, caratt);
		st.setString(3, autista);
		st.setString(4, posizione);
		st.setString(5, note);
		st.setDate(6, data);
		st.setBoolean(7, selezionato);
		st.setInt(8, litri);

		st.executeUpdate();
		long generatedKey;
		try(ResultSet rs = st.getGeneratedKeys()){
			if(rs.next()){
				generatedKey = rs.getLong(1);
			}else {
				throw new SQLException("Creating user failed, no ID obtained.");
			}
		}

		st.close();

		return generatedKey;
	}


	public long aggiungiNota(Nota n) throws SQLException{
		String testo = n.getTesto();
		String tipo = n.getTipo();
		Date data = n.getData();

		String query = "INSERT INTO Nota(Data, Testo, Tipo) VALUES (?,?,?)";

		PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

		st.setDate(1, data);
		st.setString(2, testo);
		st.setString(3, tipo);

		st.executeUpdate();

		long generatedKey;

		try(ResultSet rs = st.getGeneratedKeys()){
			if(rs.next()){
				generatedKey = rs.getLong(1);
			}else {
				throw new SQLException("Creating user failed, no ID obtained.");
			}
		}
		System.out.println(generatedKey);
		st.close();
		return generatedKey;
	}



	public void setDataAggiornamento(Date d) throws SQLException{
		String query = "UPDATE DataAggiornamento SET Data = ?";
		PreparedStatement st = conn.prepareStatement(query);

		st.setDate(1, d);
		st.executeUpdate();
		st.close();
	}

	public Date getDataAggiornamento() throws SQLException{
		String query = "SELECT * FROM DataAggiornamento";
		PreparedStatement st = conn.prepareStatement(query);

		ResultSet rs = st.executeQuery();
		Date result = null;
		while(rs.next()){
			result = rs.getDate("Data");
		}

		return result;
	}

	public int getDbVersion() throws SQLException{
		String query = "SELECT * FROM dbversion";
		PreparedStatement st = conn.prepareStatement(query);

		ResultSet rs = st.executeQuery();
		int result = 0;
		while(rs.next()){
			result = rs.getInt("Version");
		}

		st.close();
		return result;
	}
	public void aggiungiViaggio(Vector<Viaggio> viaggi) throws SQLException{
		String query = "INSERT INTO Viaggio(Targa,Caratt,Autista,Posizione,Note,Data,Selezionato,Litri) VALUES (?,?,?,?,?,?,?,?)";
		PreparedStatement st = conn.prepareStatement(query);
		conn.setAutoCommit(false);

		for(Viaggio v : viaggi){
			String targa = v.getCamion().getTarga();
			String caratt = v.getCamion().getCaratteristiche();
			String autista = v.getAutista();
			String posizione = v.getPosizione();
			String note = v.getNote();
			Date data = v.getData();
			boolean selezionato = v.isSelezionato();
			int litri = v.getLitriB();

			st.setString(1, targa);
			st.setString(2, caratt);
			st.setString(3, autista);
			st.setString(4, posizione);
			st.setString(5, note);
			st.setDate(6, data);
			st.setBoolean(7, selezionato);
			st.setInt(8, litri);
			st.addBatch();
		}

		st.executeBatch();
		conn.commit();
		st.close();
		conn.setAutoCommit(true);
	}

	public Vector<Camion> getCamion() throws SQLException {
		String query = "SELECT * FROM Camion c ORDER BY c.Targa";
		Statement st = conn.createStatement();
		Vector<Camion> camion = new Vector<>();

		ResultSet rs = st.executeQuery(query);

		while (rs.next()){
			String targa = rs.getString(1);
			String caratt = rs.getString(2);
			Camion c = new Camion(targa, caratt);
			camion.addElement(c);
		}

		rs.close();
		st.close();
		return camion;
	}

	public Vector<Nota> getNoteByDate(Date d) throws SQLException{
		String query = "SELECT * FROM Nota WHERE Data=?";
		PreparedStatement ps = conn.prepareStatement(query);
		Vector<Nota> note = new Vector<>();
		ps.setDate(1, d);
		ResultSet rs = ps.executeQuery();

		while(rs.next()){
			long id = rs.getLong("Id");
			String testo = rs.getString("Testo");
			Date date = rs.getDate("Data");
			String tipo = rs.getString("Tipo");

			Nota n = new Nota(id, date, testo, tipo);
			note.addElement(n);
		}

		rs.close();
		ps.close();
		return note;
	}

	public Nota getNotaById(long id) throws SQLException{
		String query = "SELECT * FROM Nota WHERE id=?";
		PreparedStatement ps = conn.prepareStatement(query);
		Vector<Nota> note = new Vector<>();
		ps.setLong(1, id);
		ResultSet rs = ps.executeQuery();

		while(rs.next()){
			long idNota = rs.getLong("Id");
			String testo = rs.getString("Testo");
			Date date = rs.getDate("Data");
			String tipo = rs.getString("Tipo");

			Nota n = new Nota(idNota, date, testo, tipo);
			note.addElement(n);
		}

		rs.close();
		ps.close();

		if(note.size() == 1) {
			return note.elementAt(0);
		} else {
			throw new SQLException("Failed to load nota with id=" + id);
		}
	}

	public Nota getFermiByDate(Date d) throws SQLException{
		String query = "SELECT * FROM Nota WHERE Data=? AND Tipo=?";
		PreparedStatement ps = conn.prepareStatement(query);
		Vector<Nota> note = new Vector<>();
		ps.setDate(1, d);
		ps.setString(2, Nota.FERMI);
		ResultSet rs = ps.executeQuery();

		while(rs.next()){
			long id = rs.getLong("Id");
			String testo = rs.getString("Testo");
			Date date = rs.getDate("Data");
			String tipo = rs.getString("Tipo");

			Nota n = new Nota(id, date, testo, tipo);
			note.addElement(n);
		}
		Nota result = null;
		if(note.size() == 1) result = note.elementAt(0);
		else {
			throw new SQLException("Creating user failed, to many Fermi for this date");
		}

		rs.close();
		ps.close();
		return result;
	}

	public Nota getNonAssByDate(Date d) throws SQLException{
		String query = "SELECT * FROM Nota WHERE Data=? AND Tipo=?";
		PreparedStatement ps = conn.prepareStatement(query);
		Vector<Nota> note = new Vector<>();
		ps.setDate(1, d);
		ps.setString(2, Nota.NONASS);
		ResultSet rs = ps.executeQuery();

		while(rs.next()){
			long id = rs.getLong("Id");
			String testo = rs.getString("Testo");
			Date date = rs.getDate("Data");
			String tipo = rs.getString("Tipo");

			Nota n = new Nota(id, date, testo, tipo);
			note.addElement(n);
		}

		Nota result = null;
		if(note.size() == 1) result = note.elementAt(0);
		else {
			throw new SQLException("Select failed, to many NonAss for this date");
		}

		rs.close();
		ps.close();
		return result;
	}


	public Vector<Ordine> getOrdiniByDate(Date d) throws SQLException {
		String query = "SELECT * FROM Ordini WHERE Date=? ORDER BY Cliente";
		PreparedStatement ps = conn.prepareStatement(query);
		Vector<Ordine> ordini = new Vector<>();
		ps.setDate(1, d);
		ResultSet rs = ps.executeQuery();

		while(rs.next()){
			long id = rs.getLong("Id");
			String data = rs.getString("Data");
			String cliente = rs.getString("Cliente");
			String note = rs.getString("Note");
			boolean selezionato = rs.getBoolean("Selezionato");
			Date date = rs.getDate("Date");
			String type = rs.getString("Tipo");

			Ordine o = new Ordine(data, cliente, note, date);
			o.setId(id);
			o.setSelezionato(selezionato);
			o.setType(type);

			ordini.addElement(o);
		}

		rs.close();
		ps.close();
		return ordini;
	}

	public Vector<Ordine> getOrdini() throws SQLException{
		String query = "SELECT * FROM Ordini";
		Statement st = conn.createStatement();
		Vector<Ordine> ordini = new Vector<>();

		ResultSet rs = st.executeQuery(query);

		while(rs.next()){
			long id = rs.getLong("Id");
			String data = rs.getString("Data");
			String cliente = rs.getString("Cliente");
			String note = rs.getString("Note");
			boolean selezionato = rs.getBoolean("Selezionato");
			Date date = rs.getDate("Date");
			String type = rs.getString("Tipo");


			Ordine o = new Ordine(data, cliente, note, date);
			o.setId(id);
			o.setType(type);
			o.setSelezionato(selezionato);
			ordini.addElement(o);
		}

		rs.close();
		st.close();
		return ordini;
	}

	public Ordine getOrdineById(long id) throws SQLException {
		String query = "SELECT * FROM Ordini WHERE Id=?";
		PreparedStatement ps = conn.prepareStatement(query);
		Vector<Ordine> ordini = new Vector<>();
		ps.setLong(1, id);
		ResultSet rs = ps.executeQuery();

		while(rs.next()){
			long idOrdine = rs.getLong("Id");
			String data = rs.getString("Data");
			String cliente = rs.getString("Cliente");
			String note = rs.getString("Note");
			boolean selezionato = rs.getBoolean("Selezionato");
			Date date = rs.getDate("Date");
			String type = rs.getString("Tipo");

			Ordine o = new Ordine(data, cliente, note, date);
			o.setId(idOrdine);
			o.setSelezionato(selezionato);
			o.setType(type);

			ordini.addElement(o);
		}

		rs.close();
		ps.close();

		if(ordini.size() == 1) {
			return ordini.elementAt(0);
		} else {
			throw new SQLException("Ordine with id=" + id + " not found");
		}
	}

    /*public Ordine getOrdineById(long id) throws SQLException{
        String query = "SELECT * FROM Ordini o WHERE o.id=?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setLong(1, id);

        Vector<Ordine> ordini = new Vector<>();
        ResultSet rs = ps.executeQuery(query);

        while(rs.next()){
            long idOrdine = rs.getLong("Id");
            String data = rs.getString("Data");
            String cliente = rs.getString("Cliente");
            String note = rs.getString("Note");
            boolean selezionato = rs.getBoolean("Selezionato");
            Date date = rs.getDate("Date");
            String type = rs.getString("Tipo");


            Ordine o = new Ordine(data, cliente, note, date);
            o.setId(idOrdine);
            o.setType(type);
            o.setSelezionato(selezionato);
            ordini.addElement(o);
        }

        rs.close();
        ps.close();

        if(ordini.size() == 1) {
            return ordini.elementAt(0);
        } else {
            throw new SQLException("Ordine with id=" + id + " not found");
        }
    }*/

	public Vector<Viaggio> getViaggiBy(String posizione, Date data) throws SQLException{
		String query = "SELECT * FROM Viaggio v" +
				" WHERE Posizione = ? AND Data = ?";
		PreparedStatement st = conn.prepareStatement(query);
		Vector<Viaggio> viaggi = new Vector<>();
		st.setString(1, posizione);
		st.setDate(2, data);

		ResultSet rs = st.executeQuery();
		String targa, caratt, autista, pos;
		String note;
		Date d;
		int litri;
		long id;

		while (rs.next()){
			targa = rs.getString("Targa");
			caratt = rs.getString("Caratt");
			autista = rs.getString("Autista");
			pos = rs.getString("Posizione");
			d = rs.getDate("Data");
			id = rs.getLong("Id");

			Viaggio v = new Viaggio(targa, caratt, autista, pos, d, id);
			if((note = rs.getString("Note")) != null){
				v.setNote(note);
			}
			if(rs.getBoolean("Selezionato")){
				v.setSelezionato(true);
			}

			boolean pinned = rs.getBoolean("Pinned");
			v.setPinned(pinned);

			litri = rs.getInt("Litri");
			v.setLitriB(litri);
			viaggi.addElement(v);
		}

		rs.close();
		st.close();
		return viaggi;

	}

	public Viaggio getViaggioById(long id) throws SQLException{
		String query = "SELECT * FROM Viaggio v" +
				" WHERE v.Id=?";
		PreparedStatement st = conn.prepareStatement(query);
		Vector<Viaggio> viaggi = new Vector<>();
		st.setLong(1, id);

		ResultSet rs = st.executeQuery();
		String targa, caratt, autista, pos;
		String note;
		Date d;
		int litri;
		long idViaggio;
		boolean pinned;

		while (rs.next()){
			targa = rs.getString("Targa");
			caratt = rs.getString("Caratt");
			autista = rs.getString("Autista");
			pos = rs.getString("Posizione");
			d = rs.getDate("Data");
			idViaggio = rs.getLong("Id");

			Viaggio v = new Viaggio(targa, caratt, autista, pos, d, idViaggio);
			if((note = rs.getString("Note")) != null){
				v.setNote(note);
			}
			if(rs.getBoolean("Selezionato")){
				v.setSelezionato(true);
			}

			pinned = rs.getBoolean("Pinned");
			v.setPinned(pinned);

			litri = rs.getInt("Litri");
			v.setLitriB(litri);
			viaggi.addElement(v);
		}

		rs.close();
		st.close();
		if(viaggi.size() == 1) {
			return viaggi.elementAt(0);
		} else {
			throw new SQLException("Viaggio with id=" + id + " not found");
		}
	}

	public void rimuoviViaggio(Viaggio v) throws SQLException{
		String query = "DELETE FROM Viaggio WHERE Targa=? AND Data=? AND Id=?";
		PreparedStatement ps = conn.prepareStatement(query);

		String targa = v.getCamion().getTarga();
		Date data = v.getData();
		long id = v.getId();

		ps.setString(1, targa);
		ps.setDate(2, data);
		ps.setLong(3, id);

		ps.executeUpdate();
		ps.close();
	}

	public void modificaViaggio(Viaggio v) throws SQLException{

		rimuoviViaggio(v);
		aggiungiViaggio(v);


	}

	public void modificaCamion(Camion c) throws SQLException{
		String caratt = c.getCaratteristiche();
		String targa = c.getTarga();
		String query = "UPDATE Camion SET Caratteristiche=? WHERE Targa=?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, caratt);
		ps.setString(2, targa);

		ps.executeUpdate();
		ps.close();
	}


	public void modificaNota(Nota n) throws SQLException{
		long id = n.getId();
		Date d = n.getData();
		String tipo = n.getTipo();
		String testo = n.getTesto();

		String query = "UPDATE Nota SET Testo=? WHERE Id=? AND Data=? AND Tipo=?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, testo);
		ps.setLong(2, id);
		ps.setDate(3, d);
		ps.setString(4, tipo);

		ps.executeUpdate();
		ps.close();
	}


	public void modificaOrdine(Ordine o, int col) throws SQLException{
		long id = o.getId();

		if(col == 1){
			String data = o.getData();
			String query = "UPDATE Ordini SET Data=? WHERE Id=?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, data);
			ps.setLong(2, id);
			ps.executeUpdate();
			ps.close();
		}else if(col == 2){
			String cliente = o.getCliente();
			String query = "UPDATE Ordini SET Cliente=? WHERE Id=?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, cliente);
			ps.setLong(2, id);
			ps.executeUpdate();
			ps.close();
		}else if(col == 3){
			String note = o.getNote();
			String query = "UPDATE Ordini SET Note=? WHERE Id=?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, note);
			ps.setLong(2, id);
			ps.executeUpdate();
			ps.close();
		}else if(col == 0){
			boolean sel = o.getSelezionato();
			String query = "UPDATE Ordini SET Selezionato=? WHERE Id=?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setBoolean(1, sel);
			ps.setLong(2, id);
			ps.executeUpdate();
			ps.close();
		}
	}

	public void modificaViaggio(Viaggio v, int col) throws SQLException{
		Date data = v.getData();
		String targa = v.getCamion().getTarga();
		long id = v.getId();

		if(v.getPosizione().compareTo(Viaggio.NORD) == 0){
			if(col == 3){
				String note = v.getNote();
				String query = "UPDATE Viaggio SET Note=? WHERE Targa=? AND Data=? AND Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setString(2, targa);
				ps.setDate(3, data);
				ps.setString(1, note);
				ps.setLong(4, id);
				ps.executeUpdate();
				ps.close();
			}else if(col == 4){
				boolean sel = v.isSelezionato();
				String query = "UPDATE Viaggio SET Selezionato=? WHERE Targa=? AND Data=? AND Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setString(2, targa);
				ps.setDate(3, data);
				ps.setBoolean(1, sel);
				ps.setLong(4, id);
				ps.executeUpdate();
				ps.close();
			}else if(col == 2){
				String query = "UPDATE Viaggio SET Autista=? WHERE Targa=? AND Data=? AND Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				String autista = v.getAutista();
				ps.setString(1, autista);
				ps.setString(2, targa);
				ps.setDate(3, data);
				ps.setLong(4, id);
				ps.executeUpdate();
				ps.close();
			}else if(col == 0){
				String query = "UPDATE Viaggio SET Targa=?, Caratt=? WHERE Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				String caratt = v.getCamion().getCaratteristiche();
				ps.setString(1, targa);
				ps.setString(2, caratt);
				ps.setLong(3, id);
				ps.executeUpdate();
				ps.close();
			} else if(col == Viaggio.COL_PINNED) {
				boolean pinned = v.isPinned();
				String query = "UPDATE Viaggio SET Pinned=? WHERE Targa=? AND Data=? AND Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setString(2, targa);
				ps.setDate(3, data);
				ps.setBoolean(1, pinned);
				ps.setLong(4, id);
				ps.executeUpdate();
				ps.close();
			}
		} else if(v.getPosizione().compareTo(Viaggio.SUD) == 0) {
			if(col == 3){
				String note = v.getNote();
				String query = "UPDATE Viaggio SET Note=? WHERE Targa=? AND Data=? AND Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setString(2, targa);
				ps.setDate(3, data);
				ps.setString(1, note);
				ps.setLong(4, id);
				ps.executeUpdate();
				ps.close();
			}else if(col == 5){
				boolean sel = v.isSelezionato();
				String query = "UPDATE Viaggio SET Selezionato=? WHERE Targa=? AND Data=? AND Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setString(2, targa);
				ps.setDate(3, data);
				ps.setBoolean(1, sel);
				ps.setLong(4, id);
				ps.executeUpdate();
				ps.close();
			}else if(col == 2){
				String query = "UPDATE Viaggio SET Autista=? WHERE Targa=? AND Data=? AND Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				String autista = v.getAutista();
				ps.setString(1, autista);
				ps.setString(2, targa);
				ps.setDate(3, data);
				ps.setLong(4, id);
				ps.executeUpdate();
				ps.close();
			}else if(col == 4){
				String query = "UPDATE Viaggio SET Litri=? WHERE Targa=? AND Data=? AND Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				int litri = v.getLitriB();
				ps.setInt(1, litri);
				ps.setString(2, targa);
				ps.setDate(3, data);
				ps.setLong(4, id);
				ps.executeUpdate();
				ps.close();
			}else if(col == 0){
				String query = "UPDATE Viaggio SET Targa=?, Caratt=? WHERE Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				String caratt = v.getCamion().getCaratteristiche();
				ps.setString(1, targa);
				ps.setString(2, caratt);
				ps.setLong(3, id);
				ps.executeUpdate();
				ps.close();
			} else if(col == Viaggio.COL_PINNED) {
				boolean pinned = v.isPinned();
				String query = "UPDATE Viaggio SET Pinned=? WHERE Targa=? AND Data=? AND Id=?";
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setString(2, targa);
				ps.setDate(3, data);
				ps.setBoolean(1, pinned);
				ps.setLong(4, id);
				ps.executeUpdate();
				ps.close();
			}
		}

	}

	public void spostaViaggio(Viaggio v) throws SQLException{
		String curPos = v.getPosizione();
		String targa = v.getCamion().getTarga();
		Date data = v.getData();
		long id = v.getId();
		String query = "UPDATE Viaggio SET Posizione=? WHERE Targa=? AND Data=? AND Id=?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(2, targa);
		ps.setDate(3, data);
		ps.setLong(4, id);
		if(curPos.compareTo(Viaggio.NORD) == 0){
			ps.setString(1, Viaggio.SUD);
		}else if(curPos.compareTo(Viaggio.SUD) == 0){
			ps.setString(1, Viaggio.NORD);
		}

		ps.executeUpdate();

		ps.close();
	}



	public void aggiungiOrdine(Vector<Ordine> ordini) throws SQLException {
		String query = "INSERT INTO Ordini(Data, Cliente, Note, Date, Tipo) VALUES (?,?,?,?,?)";
		PreparedStatement st = conn.prepareStatement(query);
		conn.setAutoCommit(false);

		for(Ordine o : ordini){
			String data = o.getData();
			String cliente = o.getCliente();
			String note = o.getNote();
			Date date = o.getDate();
			String tipo = o.getType();

			st.setString(1, data);
			st.setString(2, cliente);
			st.setString(3, note);
			st.setDate(4, date);
			st.setString(5, tipo);
			st.addBatch();
		}

		st.executeBatch();
		conn.commit();
		st.close();
		conn.setAutoCommit(true);

	}

	public ArrayList<Date> getDateEsistenti() throws SQLException {
		String query = "SELECT * FROM vw_existing_dates ORDER BY Data desc";

		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(query);
		ArrayList<Date> toReturn = new ArrayList<>();
		while(rs.next()){
			Date data = rs.getDate("Data");
			toReturn.add(data);
		}

		rs.close();
		s.close();

		return toReturn;
	}

    public ArrayList<Date> getDateEsistenti(int limit) throws SQLException {
        String query = "SELECT * FROM vw_existing_dates ORDER BY Data desc LIMIT ?";

        PreparedStatement s = conn.prepareStatement(query);
        s.setInt(1, limit);
        ResultSet rs = s.executeQuery();

        ArrayList<Date> toReturn = new ArrayList<>();
        while(rs.next()){
            Date data = rs.getDate("Data");
            toReturn.add(data);
        }

        rs.close();
        s.close();

        return toReturn;
    }

	public boolean aggiungiGiornata(Date newDate, Vector<Viaggio> newViaggi, Vector<Ordine> newOrdini, Nota newFermi, Nota newNonAss) throws SQLException{
		boolean result = false;
		PreparedStatement viaggiSt = null;
		PreparedStatement stOrdini = null;
		PreparedStatement stFermi = null;
		PreparedStatement stNonAss = null;
		PreparedStatement stData= null;

		try{
			conn.setAutoCommit(false);
			//Aggiungo i viaggi
			String viaggiQuery = "INSERT INTO Viaggio(Targa,Caratt,Autista,Posizione,Note,Data,Selezionato,Litri,Pinned) VALUES (?,?,?,?,?,?,?,?,?)";
			viaggiSt = conn.prepareStatement(viaggiQuery);


			for(Viaggio v : newViaggi){
				String targa = v.getCamion().getTarga();
				String caratt = v.getCamion().getCaratteristiche();
				String autista = v.getAutista();
				String posizione = v.getPosizione();
				String note = v.getNote();
				Date data = v.getData();
				boolean selezionato = v.isSelezionato();
				int litri = v.getLitriB();
				boolean pinned = v.isPinned();

				viaggiSt.setString(1, targa);
				viaggiSt.setString(2, caratt);
				viaggiSt.setString(3, autista);
				viaggiSt.setString(4, posizione);
				viaggiSt.setString(5, note);
				viaggiSt.setDate(6, data);
				viaggiSt.setBoolean(7, selezionato);
				viaggiSt.setInt(8, litri);
				viaggiSt.setBoolean(9, pinned);
				viaggiSt.executeUpdate();
			}

			//Aggiungo gli ordini
			String queryOrdini = "INSERT INTO Ordini(Data, Cliente, Note, Date, Tipo) VALUES (?,?,?,?,?)";
			stOrdini = conn.prepareStatement(queryOrdini);

			for(Ordine o : newOrdini){
				String data = o.getData();
				String cliente = o.getCliente();
				String note = o.getNote();
				Date date = o.getDate();
				String tipo = o.getType();

				stOrdini.setString(1, data);
				stOrdini.setString(2, cliente);
				stOrdini.setString(3, note);
				stOrdini.setDate(4, date);
				stOrdini.setString(5, tipo);
				stOrdini.executeUpdate();
			}


			//Aggiungo la nota per i fermi
			String testo = newFermi.getTesto();
			String tipo = newFermi.getTipo();
			Date data = newFermi.getData();

			String queryFermi = "INSERT INTO Nota(Data, Testo, Tipo) VALUES (?,?,?)";

			stFermi = conn.prepareStatement(queryFermi);

			stFermi.setDate(1, data);
			stFermi.setString(2, testo);
			stFermi.setString(3, tipo);

			stFermi.executeUpdate();

			//Aggiungo la nota per i nonAss
			String ntesto = newNonAss.getTesto();
			String ntipo = newNonAss.getTipo();
			Date ndata = newNonAss.getData();

			String queryNonAss = "INSERT INTO Nota(Data, Testo, Tipo) VALUES (?,?,?)";

			stNonAss = conn.prepareStatement(queryNonAss);

			stNonAss.setDate(1, ndata);
			stNonAss.setString(2, ntesto);
			stNonAss.setString(3, ntipo);

			stNonAss.executeUpdate();

			//Aggiorno la DataAggiornamento
			String queryData = "UPDATE DataAggiornamento SET Data = ?";
			stData = conn.prepareStatement(queryData);

			stData.setDate(1, newDate);
			stData.executeUpdate();

			conn.commit();
			conn.setAutoCommit(true);
			result=true;
		}catch(SQLException e){
			conn.rollback();
			result=false;
		}
		finally{
			if(viaggiSt != null){
				viaggiSt.close();
			}
			if(stOrdini != null){
				stOrdini.close();
			}
			if(stFermi != null){
				stFermi.close();
			}
			if(stNonAss != null){
				stNonAss.close();
			}
			if(stData != null){
				stData.close();
			}

		}
		return result;
	}

	public boolean dateExists(Date date) throws SQLException {
	    String query = "SELECT COUNT(*) FROM Viaggio v WHERE v.Data=?";

	    PreparedStatement statement = conn.prepareStatement(query);
	    statement.setDate(1, date);
        ResultSet resultSet = statement.executeQuery();

        int rowCount = resultSet.getInt(1);

        resultSet.close();
        statement.close();

        if(rowCount > 0){
            //return true;
        }

        query = "SELECT  COUNT(*) FROM nota n WHERE  n.data=?";
        statement = conn.prepareStatement(query);
        statement.setDate(1, date);
        resultSet = statement.executeQuery();

        rowCount = resultSet.getInt(1);

        resultSet.close();
        statement.close();

        if(rowCount > 0){
            //return true;
        }

        query = "SELECT  COUNT(*) FROM ordini n WHERE  n.Date=?";
        statement = conn.prepareStatement(query);
        statement.setDate(1, date);
        resultSet = statement.executeQuery();

        rowCount = resultSet.getInt(1);

        resultSet.close();
        statement.close();

        if(rowCount > 0){
            return true;
        }

        return false;
    }


    public void deleteDate(Date data) throws SQLException {
        PreparedStatement viaggiStatement = null;
	    PreparedStatement ordiniStatement = null;
	    PreparedStatement noteStatement = null;

	    String viaggiQuery = "DELETE FROM Viaggio WHERE Data=?";
	    String ordiniQuery = "DELETE FROM Ordini WHERE Date=?";
	    String noteQuery = "DELETE FROM Nota WHERE Data=?";

	    try {
            conn.setAutoCommit(false);

            Vector<Viaggio> viaggiNord = getViaggiBy(Viaggio.NORD, data);
            Vector<Viaggio> viaggiSud = getViaggiBy(Viaggio.SUD, data);
            Vector<Ordine> ordini = getOrdiniByDate(data);
            Vector<Nota> note = getNoteByDate(data);

            int nViaggi = viaggiNord.size() + viaggiSud.size();
            int nOrdini = ordini.size();
            int nNote = note.size();
            int queryRes;

            viaggiStatement = conn.prepareStatement(viaggiQuery);
            viaggiStatement.setDate(1, data);

            queryRes = viaggiStatement.executeUpdate();

            if(queryRes != nViaggi) {
                throw new SQLException("Query error");
            }

            ordiniStatement = conn.prepareStatement(ordiniQuery);
            ordiniStatement.setDate(1, data);

            queryRes = ordiniStatement.executeUpdate();

            if(queryRes != nOrdini) {
                throw new SQLException("Query error");
            }

            noteStatement = conn.prepareStatement(noteQuery);
            noteStatement.setDate(1, data);

            queryRes = noteStatement.executeUpdate();

            if(queryRes != nNote) {
                throw new SQLException("Query error");
            }

            setDataAggiornamento(getLastDate());

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
	        conn.rollback();
	        conn.setAutoCommit(true);
	        throw new SQLException(e);
        } finally {
	        if(viaggiStatement != null) {
	            viaggiStatement.close();
            }

            if(noteStatement != null) {
	            noteStatement.close();
            }

            if(ordiniStatement != null) {
                ordiniStatement.close();
            }
        }
    }

    public Date getLastDate() throws SQLException {
        ArrayList<Date> dateEsistenti = getDateEsistenti(1);

        if(dateEsistenti.size() == 1) {
            return dateEsistenti.get(0);
        }

        return null;
    }
}