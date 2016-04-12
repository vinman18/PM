package it.vin.dev.menzione.logica;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

public class DbTest {

	public DbTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		//Vector<Viaggio> v = new Vector<>();
		Vector<Camion> c = new Vector<>();
		Camion c1 = new Camion("AAAAA", "BBB");
		Camion c2 = new Camion("CCCCC", "DDD");
		Camion c3 = new Camion("EEEEE", "FFF");
		Camion c4 = new Camion("GGGGG", "HHH");
		Camion c5 = new Camion("IIIII", "JJJ");
		Camion c6 = new Camion("KKKKK", "LLL");
		Camion c7 = new Camion("MMMMM", "NNN");
		Camion c8 = new Camion("OOOOO", "PPP");
		
		/*Viaggio v1 = new Viaggio(c1, "Pippo", "Nord", Date.valueOf("2015-11-21"));
		Viaggio v2 = new Viaggio(c2, "Pluto", "Nord", Date.valueOf("2015-10-15"));
		Viaggio v3 = new Viaggio(c3, "Pippo", "Nord", Date.valueOf("2015-10-15"));
		Viaggio v4 = new Viaggio(c4, "Topolino", "Sud", Date.valueOf("2015-10-15"));
		Viaggio v5 = new Viaggio(c5, "Pippo", "Nord", Date.valueOf("2015-10-15"));
		Viaggio v6 = new Viaggio(c6, "Pippo", "Sud", Date.valueOf("2015-10-14"));
		Viaggio v8 = new Viaggio(c8, "Pippo", "Sud", Date.valueOf("2015-10-14"));
		Viaggio v7 = new Viaggio(c7, "Pippo", "Nord", Date.valueOf("2015-10-14"));
		
		v.addElement(v1);
		v.addElement(v2);
		v.addElement(v3);
		v.addElement(v4);
		v.addElement(v5);
		v.addElement(v6);
		v.addElement(v7);
		v.addElement(v8);*/
		
		c.addElement(c1);
		c.addElement(c2);
		c.addElement(c3);
		c.addElement(c4);
		c.addElement(c5);
		c.addElement(c6);
		c.addElement(c7);
		c.addElement(c8);
		
		Connection conn = null;
		try {
			//Vector<Viaggio> vv;
			conn = DbUtil.createConnection();
			
			DbUtil dbu = DbUtil.dbUtilFactory(conn);
			//for(Camion cc : c){
				//dbu.aggiungiCamion(cc);
			//}
			//dbu.aggiungiViaggio(v);
			//vv = dbu.getViaggiBy("Nord", Date.valueOf("2015-10-15"));
			//System.out.println(vv.toString());
			//for(Viaggio vvv : vv) {
				//System.out.println(vvv.toString());
			//}
			//dbu.aggiungiCamion(cacca);
			
			//Ordine o = new Ordine("09-01-2016", "Topolino", "");
			//dbu.aggiungiOrdine(o);
			Vector<Ordine> oo = dbu.getOrdini();
			for(Ordine ooo : oo){
				System.out.println(ooo.toString());

			}
			//o.setNote("Pollo");
			//dbu.modificaOrdine(o, 3);
			
			oo = dbu.getOrdini();
			/*for(Ordine ooo : oo){
				System.out.println(ooo.toString());
			}*/
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}
