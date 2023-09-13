package instrumentos.presentation.calibraciones;

import instrumentos.logic.Calibraciones;
import instrumentos.Application;
import instrumentos.logic.Service;

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
    private JLabel textoRojo;

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
        guardarBotonCal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Calibraciones filter = new Calibraciones();
                filter.setNumero(numeroTextFieldBus.getText());
                try{
                    if(isValid()){
                        controller.save(filter);
                    }
                } catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tabla.getSelectedRow();
                controller.edit(row);
            }
        });
        borrarBotonCal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Calibraciones filter = new Calibraciones();
                try{
                    controller.delete(filter);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        limpiarBotonCal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { controller.clear(); }
        });
        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Calibraciones filter = new Calibraciones();
                    filter.setNumero(numeroTextFieldBus.getText());
                    controller.search(filter);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }
//----------------------------------------------------------------------------------------------------------------------
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
        if(medicionesTextField.getText().isEmpty()){
            labelMediciones.setBackground(Color.red);
            medicionesTextField.setToolTipText("Medicion requerida");
            valid = false;
        } else {
            labelMediciones.setBackground(panel.getBackground());
            medicionesTextField.setToolTipText(null);
        }
        if(fechaTextField.getText().isEmpty()){
            labelFecha.setBackground(Color.red);
            fechaTextField.setToolTipText("Fecha requerido");
            valid = false;
        } else {
            labelFecha.setBackground(panel.getBackground());
            fechaTextField.setToolTipText(null);
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
            medicionesTextField.setText(model.getCurrent().getMediciones());
            fechaTextField.setText(model.getCurrent().getFecha());
        }
        if(model.getMode() == Application.MODE_EDIT){
            //String var = model.getCurrent().getNumero();
            //char car = var.charAt(2);

            numeroTextField.setEnabled(false);
            borrarBotonCal.setEnabled(true);
            textoRojo.setText("155668");
            //textoRojo.setText(Service.instance().getTipos().get(Character.getNumericValue(car) - 1).getUnidad());
            //textoRojo.setText(Service.instance().getInstrumentos().get(Character.getNumericValue(car) - 1).getSerie());
            // La linea de arriba solo funciona si la calibracion ya esta guardada y los imprime en orden contrario.
            textoRojo.setForeground(Color.red);
        } else {
            numeroTextField.setEnabled(true);
            borrarBotonCal.setEnabled(false);
            textoRojo.setText(null);
        }
        this.panel.revalidate();
    }
}
