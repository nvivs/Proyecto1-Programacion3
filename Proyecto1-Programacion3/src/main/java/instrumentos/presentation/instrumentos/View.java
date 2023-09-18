package instrumentos.presentation.instrumentos;

import instrumentos.Application;
import instrumentos.logic.Instrumento;
import instrumentos.logic.TipoInstrumento;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.List;


public class View implements Observer {
    private JPanel panel;
    private JLabel searchDescripcionLbl;
    private JTextField searchDescripcion;
    private JButton search;
    private JButton report;
    private JTable list;
    private JButton save;
    private JButton delete;
    private JButton clear;
    private JLabel SerieLbl;
    private JTextField serie;
    private JLabel MinimoLbl;
    private JTextField minimo;
    private JLabel DescripcionLbl;
    private JTextField descripcion;
    private JLabel MaximoLbl;
    private JTextField maximo;
    private JLabel ToleranciaLbl;
    private JTextField tolerancia;
    private JComboBox tipo;
    private JLabel TipoLbl;

    public View() {
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Instrumento filter= new Instrumento();
                    filter.setDescripcion(searchDescripcion.getText());
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
                tipo.setSelectedItem(controller.getSelected());
                isValid();
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Instrumento filter= new Instrumento();
                try {
                    controller.delete(filter);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Información", JOptionPane.INFORMATION_MESSAGE);
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
                Instrumento filter = new Instrumento();
                filter.setDescripcion(searchDescripcion.getText());
                try {
                    if (isValid()) {
                        controller.save(filter);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        report.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(Desktop.isDesktopSupported()){
                        controller.createDocument();
                        File archivo = new File("Instrumentos.pdf");
                        Desktop.getDesktop().open(archivo);
                    }
                }catch(Exception ex){
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "ERROR", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                try {
                    controller.shown();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                actualizarComboBox();

            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }
    public JTextField getSerie(){return serie;}
    public JTextField getDescripcion(){return descripcion;}
    public JTextField getMinimo(){return minimo;}
    public JTextField getMaximo(){return maximo;}
    public JTextField getTolerancia(){return tolerancia;}
    public TipoInstrumento getTipo(){return (TipoInstrumento) tipo.getSelectedItem();}
    Controller controller;
    Model model;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setModel(instrumentos.presentation.instrumentos.Model model) {
        this.model = model;
        model.addObserver(this);
    }
    private boolean isValid(){
        boolean valid = true;
        if(serie.getText().isEmpty()){
            SerieLbl.setBackground(Color.red);
            serie.setToolTipText("Serie requerida");
            valid = false;
        }else{
            SerieLbl.setBackground(panel.getBackground());
            serie.setToolTipText(null);
        }
        if(descripcion.getText().isEmpty()){
            DescripcionLbl.setBackground(Color.red);
            descripcion.setToolTipText("Descripcion requerida");
            valid = false;
        }else{
            DescripcionLbl.setBackground(panel.getBackground());
            descripcion.setToolTipText(null);
        }
        if(maximo.getText().isEmpty()){
            MaximoLbl.setBackground(Color.red);
            maximo.setToolTipText("Máximo requerido");
            valid = false;
        }else{
            MaximoLbl.setBackground(panel.getBackground());
            maximo.setToolTipText(null);
        }
        if(minimo.getText().isEmpty()){
            MinimoLbl.setBackground(Color.red);
            minimo.setToolTipText("Mínimo requerido");
            valid = false;
        }else{
            MinimoLbl.setBackground(panel.getBackground());
            minimo.setToolTipText(null);
        }
        if(tolerancia.getText().isEmpty()){
            ToleranciaLbl.setBackground(Color.red);
            tolerancia.setToolTipText("Tolerancia requerida");
            valid = false;
        }else{
            ToleranciaLbl.setBackground(panel.getBackground());
            tolerancia.setToolTipText(null);
        }
        return valid;
    }

    @Override
    public void update(Observable updatedModel, Object properties) {
        int changedProps = (int) properties;
        if ((changedProps & instrumentos.presentation.instrumentos.Model.LIST) == instrumentos.presentation.instrumentos.Model.LIST) {
            int[] cols = {TableModel.SERIE, TableModel.DESCRIPCION, TableModel.MINIMO, TableModel.MAXIMO, TableModel.TOLERANCIA};
            list.setModel(new TableModel(cols, model.getList()));
            list.setRowHeight(30);
            TableColumnModel columnModel = list.getColumnModel();
            columnModel.getColumn(4).setPreferredWidth(200);
        }
        if ((changedProps & instrumentos.presentation.instrumentos.Model.CURRENT) == Model.CURRENT) {
            serie.setText(model.getCurrent().getSerie());
            descripcion.setText(model.getCurrent().getDescripcion());
            minimo.setText(String.valueOf(model.getCurrent().getMinimo()));
            maximo.setText(String.valueOf(model.getCurrent().getMaximo()));
            tolerancia.setText(String.valueOf(model.getCurrent().getTolerancia()));
            TipoInstrumento currentTipo = model.getCurrent().getTipo();
            if (currentTipo != null) {
                tipo.setSelectedItem(currentTipo.getNombre());
            } else {
                tipo.setSelectedItem(controller.getTiposInstrumentos());
            }
        }
        if(model.getMode() == Application.MODE_EDIT){
            serie.setEnabled(false);
            delete.setEnabled(true);
        } else {
            serie.setEnabled(true);
            delete.setEnabled(false);
        }
        this.panel.revalidate();
    }

    private void actualizarComboBox(){
        List<TipoInstrumento> tipos = controller.getTiposInstrumentos();
        tipo.removeAllItems();
        for(TipoInstrumento tip : tipos){
            tipo.addItem(tip);
        }
    }

}
