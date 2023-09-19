package instrumentos.presentation.calibraciones;

import instrumentos.logic.Calibraciones;
import instrumentos.Application;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
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
    private JLabel labelFechaBus;
    private JTextField FechaTextFieldBus;
    private JButton reporteBoton;
    private JButton buscarButton;
    private JPanel listadoPanel;
    private JTable tabla;
    private JPanel panelMediciones;
    private JTable tablaMedidas;
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
                filter.setNumero(FechaTextFieldBus.getText());
                try{
                    if(isValid()){
                        controller.save(filter);
                        controller.CreateMeasure();
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
                    textoRojo.setText(controller.getSelectedInstrumento().toString());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        limpiarBotonCal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.clear();
                textoRojo.setText(controller.getSelectedInstrumento().toString());
            }
        });
        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Calibraciones filter = new Calibraciones();
                    filter.setFecha(FechaTextFieldBus.getText());
                    controller.search(filter);
                    textoRojo.setText(controller.getSelectedInstrumento().toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                try {
                    controller.setSelectedInstrumento();
                    tablaMedidas.setVisible(false);
                    controller.shown();
                    Calibraciones filter = new Calibraciones();
                    if(!controller.getSelectedInstrumento().getListCalibracion().isEmpty()) {
                        filter.setFecha("");
                        controller.search(filter);
                    }
                    textoRojo.setText(controller.getSelectedInstrumento().toString());
                    textoRojo.setForeground(Color.red);
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                controller.clear();
                controller.setCurrent(new Calibraciones());
                super.componentHidden(e);
            }
        });
        panelMediciones.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
            }
        });
        tablaMedidas.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = tablaMedidas.getSelectedRow();
                controller.editarMedidas(row);
            }
        });
        reporteBoton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    controller.createDocument();
                    if(Desktop.isDesktopSupported()){
                        File archivo = new File("Calibraciones.pdf");
                        Desktop.getDesktop().open(archivo);
                    }
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "ERROR", JOptionPane.INFORMATION_MESSAGE);
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
            fechaTextField.setToolTipText("Fecha requerida");
            valid = false;
        } else {
            labelFecha.setBackground(panel.getBackground());
            fechaTextField.setToolTipText(null);
        }
        return valid;
    }
    @Override
    public void update(Observable updatedModel, Object properties){
        if(controller != null) {
            int changedProps = (int) properties;
            if ((changedProps & Model.LIST) == Model.LIST) {
                int[] cols = {TableModel.NUMERO, TableModel.FECHA, TableModel.MEDICIONES};
                tabla.setModel(new TableModel(cols, controller.obtenerListaInstrumentos()));
                tabla.setRowHeight(30);

                TableColumnModel columnModel = tabla.getColumnModel();
                columnModel.getColumn(2).setPreferredWidth(200);
            }
            if ((changedProps & Model.CURRENT) == Model.CURRENT) {
                controller.crearNum();
                numeroTextField.setText(model.getCurrent().getNumero());
                medicionesTextField.setText(String.valueOf(model.getCurrent().getMediciones()));
                fechaTextField.setText(model.getCurrent().getFecha());

                int[] colsMed = {TableModelMediciones.MEDIDA, TableModelMediciones.REFERENCIA, TableModelMediciones.LECTURA};
                tablaMedidas.setModel(new TableModelMediciones(colsMed, controller.obtenerListaMedidas()));
                tablaMedidas.setRowHeight(10);
                TableColumnModel columnModelMed = tablaMedidas.getColumnModel();
                columnModelMed.getColumn(2).setPreferredWidth(100);
            }
            if (model.getMode() == Application.MODE_EDIT) {
                numeroTextField.setEnabled(false);
                borrarBotonCal.setEnabled(true);
                panelMediciones.setVisible(true);
                tablaMedidas.setVisible(true);
            } else {
                numeroTextField.setEnabled(false);
                borrarBotonCal.setEnabled(false);
                panelMediciones.setVisible(false);
                tablaMedidas.setVisible(false);
                textoRojo.setText(null);
            }
            this.panel.revalidate();
        }
    }
}
