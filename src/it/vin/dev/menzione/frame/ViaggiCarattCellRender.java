package it.vin.dev.menzione.frame;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

public class ViaggiCarattCellRender extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final String RA = "R/A";
	private static final String RF = "R/F";
	private static final String C = "C";
	private static final String C_DOT = "C.";
	JTextField f = new JTextField();

    @Override
    public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
        if(arg1 != null){
            f.setText(arg1.toString());
            String string = arg1.toString();
            if(string.contains(" " + RA + " ")){
                int indexOf = string.indexOf(RA);
                try {
                    f.getHighlighter().addHighlight(indexOf,indexOf+RA.length(),
                    		new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 0, 150)));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }else if(string.contains(" " + RF + " ")){
                int indexOf = string.indexOf(RF);
                try {
                    f.getHighlighter().addHighlight(indexOf,indexOf+RF.length(),
                    		new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }else if(string.contains(" " + C + " ")){
                int indexOf = string.indexOf(C);
                try {
                    f.getHighlighter().addHighlight(indexOf,indexOf+C.length(),
                    		new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }else if(string.contains(" " + C_DOT + " ")){
                int indexOf = string.indexOf(C_DOT);
                try {
                    f.getHighlighter().addHighlight(indexOf,indexOf+C_DOT.length(),
                    		new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        } else {
            f.setText("");
            f.getHighlighter().removeAllHighlights();
        }
        f.setBorder(null);
        return f;
    }

}
