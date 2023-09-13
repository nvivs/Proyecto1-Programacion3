package instrumentos.presentation.calibraciones;

import instrumentos.logic.Calibraciones;
import instrumentos.logic.Instrumento;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TableModelMediciones extends AbstractTableModel implements javax.swing.table.TableModel {
    List<Calibraciones> rowsCal;
    List<Instrumento> rowsIns;
    int[] cols;
//----------------------------------------------------------------------------------------------------------------------
    public static final int MEDIDA = 0;
    public static final int REFERENCIA = 1;
    public static final int LECTURA = 2;
    String[] colNames = new String[6];
//----------------------------------------------------------------------------------------------------------------------
    public TableModelMediciones(int[] cols, List<Calibraciones> rowsCal, List<Instrumento> rowsIns){
        this.rowsCal = rowsCal;
        this.rowsIns = rowsIns;
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
    public int getRowCount() {
        return rowsCal.size();
    }
    public Class<?> getColumnClass(int col){
        switch (cols[col]){
            case LECTURA: return Integer.class;
            default: return super.getColumnClass(col);
        }
    }
    /*public void setValueAt(Object aValue, int rowIndex, int columnIndex){
        Medida e = rowsCal.get(rowIndex);
        switch (cols[columnIndex]){
            case LECTURA: e.setLectura((Integer) aValue);
        }
    }*/
    public Object getValueAt(int row, int col) {
        Calibraciones sucursalCal = rowsCal.get(row);
        Instrumento sucursalIns = rowsIns.get(row);
        switch (cols[col]){
            case MEDIDA: return sucursalCal.getNumero();
            case REFERENCIA: return sucursalIns.getMaximo();
            //case LECTURA: return sucursal.getFecha();
            default: return "";
        }
    }
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
        if(columnIndex==LECTURA) return true;
        else return false;
        // return columnIndex==LECTURA;
    }
    private void initColNames(){
        colNames[MEDIDA]= "Medida";
        colNames[REFERENCIA]= "Referencia";
        colNames[LECTURA]= "Lectura";
    }
}