package instrumentos.presentation.tipos;

import instrumentos.Application;
import instrumentos.logic.TipoInstrumento;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

public class View implements Observer {
    private JPanel panel;
    private JTextField searchNombre;
    private JButton search;
    private JButton save;
    private JTable list;
    private JButton delete;
    private JLabel searchNombreLbl;
    private JButton report;
    private JTextField codigo;
    private JTextField nombre;
    private JTextField unidad;
    private JLabel codigoLbl;
    private JLabel nombreLbl;
    private JLabel unidadLbl;
    private JButton clear;

    public View() {
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    TipoInstrumento filter= new TipoInstrumento();
                    filter.setNombre(searchNombre.getText());
                    controller.search(filter);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = list.getSelectedRow();
                controller.edit(row);
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TipoInstrumento filter= new TipoInstrumento();
                try {
                    controller.delete(filter);

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.clear();
            }
        });
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)  {
                TipoInstrumento filter = new TipoInstrumento();
                filter.setNombre(searchNombre.getText());
                try {
                    if (isValid()) {
                        controller.save(filter);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }
    public JTextField getCodigo(){return codigo;}
    public JTextField getNombre(){return nombre;}
    public JTextField getUnidad(){return unidad;}

    Controller controller;
    Model model;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setModel(Model model) {
        this.model = model;
        model.addObserver(this);
    }
    private boolean isValid(){
        boolean valid = true;
        if(codigo.getText().isEmpty()){
            codigoLbl.setBackground(Color.red);
            codigo.setToolTipText("Codigo requerido");
            valid = false;
        }else{
            codigoLbl.setBackground(panel.getBackground());
            codigo.setToolTipText(null);
        }
        if(nombre.getText().isEmpty()){
            nombreLbl.setBackground(Color.red);
            nombre.setToolTipText("Nombre requerido");
            valid = false;
        }else{
            nombreLbl.setBackground(panel.getBackground());
            nombre.setToolTipText(null);
        }
        if(unidad.getText().isEmpty()){
            unidadLbl.setBackground(Color.red);
            unidad.setToolTipText("Unidad requerida");
            valid = false;
        }else{
            unidadLbl.setBackground(panel.getBackground());
            unidad.setToolTipText(null);
        }
        return valid;
    }

    @Override
    public void update(Observable updatedModel, Object properties) {
        int changedProps = (int) properties;
        if ((changedProps & Model.LIST) == Model.LIST) {
            int[] cols = {TableModel.CODIGO, TableModel.NOMBRE, TableModel.UNIDAD};
            list.setModel(new TableModel(cols, model.getList()));
            list.setRowHeight(30);
            TableColumnModel columnModel = list.getColumnModel();
            columnModel.getColumn(2).setPreferredWidth(200);
        }
        if ((changedProps & Model.CURRENT) == Model.CURRENT) {
            codigo.setText(model.getCurrent().getCodigo());
            nombre.setText(model.getCurrent().getNombre());
            unidad.setText(model.getCurrent().getUnidad());
        }
        if(model.getMode() == Application.MODE_EDIT){
            codigo.setEnabled(false);
            delete.setEnabled(true);
        } else {
            codigo.setEnabled(true);
            delete.setEnabled(false);
        }
        this.panel.revalidate();
    }
}
