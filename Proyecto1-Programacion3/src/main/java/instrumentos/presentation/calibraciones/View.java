package instrumentos.presentation.calibraciones;

import instrumentos.logic.Calibraciones;
import instrumentos.Application;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

public class View implements Observer{
    private JPanel panel;
    private JPanel panelInstrumento;
    private JFormattedTextField formattedTextField1;
    private JPanel panelCalibracion;
    private JLabel labelNumero;
    private JTextField numeroTextField;
    private JLabel labelFecha;
    private JTextField fechaTextField;
    private JLabel labelMediciones;
    private JTextField medicionesTextField;
    private JButton guardarBotonCal;
    private JButton limpiarBotonCal;
    private JButton borrarBotonCal;
    private JPanel busquedaPanel;
    private JLabel labelNumeroBus;
    private JTextField numeroTextFieldBus;
    private JButton reporteBoton;
    private JButton buscarButton;
    private JPanel listadoPanel;
    private JTable tabla;
//----------------------------------------------------------------------------------------------------------------------
    public JPanel getPanel(){return panel;}
    public JTextField getNumero(){ return numeroTextField; }
    public JTextField getFecha(){ return fechaTextField; }
    public JTextField getMediciones(){ return medicionesTextField; }

    public void setController(Controller controller) {
        this.controller = controller;
    }
    public void setModel(Model model) {
        this.model = model;
        model.addObserver(this);
    }
//----------------------------------------------------------------------------------------------------------------------
    Controller controller;
    Model model;
//----------------------------------------------------------------------------------------------------------------------
    public View(){

    }
    private boolean isValid(){
        boolean valid = true;
        if(numeroTextField.getText().isEmpty()){
            labelNumero.setBackground(Color.red);
            numeroTextField.setToolTipText("Numero requerido");
            valid = false;
        } else {
            labelNumero.setBackground(panel.getBackground());
            numeroTextField.setToolTipText(null);
        }
        if(fechaTextField.getText().isEmpty()){
            labelFecha.setBackground(Color.red);
            fechaTextField.setToolTipText("Fecha requerido");
            valid = false;
        } else {
            labelFecha.setBackground(panel.getBackground());
            fechaTextField.setToolTipText(null);
        }
        if(medicionesTextField.getText().isEmpty()){
            labelMediciones.setBackground(Color.red);
            medicionesTextField.setToolTipText("Medicion requerida");
            valid = false;
        } else {
            labelMediciones.setBackground(panel.getBackground());
            medicionesTextField.setToolTipText(null);
        }
        return valid;
    }
    @Override
    public void update(Observable updatedModel, Object properties){
        int changedProps = (int) properties;

        if((changedProps & Model.LIST) == Model.LIST){
            int[] cols = {TableModel.NUMERO, TableModel.FECHA, TableModel.MEDICIONES};
            tabla.setModel(new TableModel(cols, model.getList()));
            tabla.setRowHeight(30);
            TableColumnModel columnModel = tabla.getColumnModel();
            columnModel.getColumn(2).setPreferredWidth(200);
        }
        if ((changedProps & Model.CURRENT) == Model.CURRENT) {
            numeroTextField.setText(model.getCurrent().getNumero());
            fechaTextField.setText(model.getCurrent().getFecha());
            medicionesTextField.setText(String.valueOf(model.getCurrent().getMediciones()));
        }
        if(model.getMode() == Application.MODE_EDIT){
            numeroTextField.setEnabled(false);
            borrarBotonCal.setEnabled(true);
        } else {
            numeroTextField.setEnabled(true);
            borrarBotonCal.setEnabled(false);
        }
        this.panel.revalidate();
    }
}
