package it.vin.dev.menzione.logica;

import java.sql.Statement;
import java.util.Vector;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbUtil{

	private Connection conn;
	
	public DbUtil(Connection conn) {
		this.conn = conn;
		
	}

	public static Connection createConnection() throws SQLException{
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String ip = Configuration.getIp();
		String url = "jdbc:mysql://"+ip+"/GestioneViaggi";
		String user = Configuration.getUser();
		String password = Configuration.getPassword();
		Connection conn = DriverManager.getConnection(url, user, password);
		
		return conn;
	}
	
	public void closeConnection() throws SQLException{
		conn.close();
	}
	
	public void setConnection(Connection conn){
		this.conn = conn;
	}
	
	public static DbUtil dbUtilFactory(Connection conn) {
		return new DbUtil(conn);
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
	
	public void aggiungiOrdine(Ordine o) throws SQLException{
		String data = o.getData();
		String cliente = o.getCliente();
		String note = o.getNote();
		Date date = o.getDate();
		String tipo = o.getType();
		
		String query = "INSERT INTO Ordini(Data, Cliente, Note, Date, Tipo) VALUES (?,?,?,?,?)";
		
		PreparedStatement ps = conn.prepareStatement(query);
		
		ps.setString(1, data);
		ps.setString(2, cliente);
		ps.setString(3, note);
		ps.setDate(4, date);
		ps.setString(5, tipo);
		
		ps.executeUpdate();
		
		ps.close();
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
	
	public Vector<Camion> getCamion() throws SQLException{
		String query = "SELECT * FROM Camion";
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
			if(rs.getBoolean("Selezionato") != false){
				v.setSelezionato(true);
			}
			litri = rs.getInt("Litri");
			v.setLitriB(litri);
			viaggi.addElement(v);
		}
		
		rs.close();
		st.close();
		return viaggi;
		
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
			}
		}else if(v.getPosizione().compareTo(Viaggio.SUD) == 0){
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
		String viaggiQuery = "INSERT INTO Viaggio(Targa,Caratt,Autista,Posizione,Note,Data,Selezionato,Litri) VALUES (?,?,?,?,?,?,?,?)";
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

			viaggiSt.setString(1, targa);
			viaggiSt.setString(2, caratt);
			viaggiSt.setString(3, autista);
			viaggiSt.setString(4, posizione);
			viaggiSt.setString(5, note);
			viaggiSt.setDate(6, data);
			viaggiSt.setBoolean(7, selezionato);
			viaggiSt.setInt(8, litri);
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

}