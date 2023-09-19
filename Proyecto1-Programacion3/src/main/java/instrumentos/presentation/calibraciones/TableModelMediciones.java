package instrumentos.presentation.calibraciones;

import instrumentos.logic.Medida;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TableModelMediciones extends AbstractTableModel implements javax.swing.table.TableModel {
    List<Medida> rows;
    int[] cols;
//----------------------------------------------------------------------------------------------------------------------
    public static final int MEDIDA = 0;
    public static final int REFERENCIA = 1;
    public static final int LECTURA = 2;
    String[] colNames = new String[6];
//----------------------------------------------------------------------------------------------------------------------
    public TableModelMediciones(int[] cols, List<Medida> rows){
        this.rows = rows;
        this.cols = cols;
        initColNames();
    }
//----------------------------------------------------------------------------------------------------------------------
    public int getColumnCount() {
    return cols.length;
}
    public String getColumnName(int col){
        return colNames[cols[col]];
    }
    public int getRowCount() { return rows.size(); }
    public Class<?> getColumnClass(int col){
        switch (cols[col]){
            case LECTURA: return Integer.class;
            default: return super.getColumnClass(col);
        }
    }
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
        Medida e = rows.get(rowIndex);
        switch (cols[columnIndex]){
            case LECTURA: e.setLectura((Integer) aValue);
        }
    }
    public Object getValueAt(int row, int col) {
        Medida e = rows.get(row);
        switch (cols[col]){
            case MEDIDA: return e.getMedida();
            case REFERENCIA: return e.getReferencia();
            case LECTURA: return e.getLectura();
            default: return 0;
        }
    }
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
        if(columnIndex==LECTURA) return true;
        else return false;
    }
    private void initColNames(){
        colNames[MEDIDA]= "Medida";
        colNames[REFERENCIA]= "Referencia";
        colNames[LECTURA]= "Lectura";
    }
}