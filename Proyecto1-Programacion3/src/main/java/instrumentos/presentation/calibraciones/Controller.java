package instrumentos.presentation.calibraciones;
import instrumentos.Application;
import instrumentos.logic.Instrumento;
import instrumentos.logic.Service;
import instrumentos.logic.Calibraciones;
import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    View view;
    Model model;
    private Map<Instrumento, List<Calibraciones>> calibracionesInstrumento = new HashMap<>();

    public void setController(instrumentos.presentation.instrumentos.Controller controller) {
        this.controller = controller;
    }

    instrumentos.presentation.instrumentos.Controller controller;

    //----------------------------------------------------------------------------------------------------------------------
    public Controller(Model model, View view) {
        model.init(Service.instance().search(new Calibraciones()));
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
        this.controller = null;
    }

    public void search(Calibraciones filter) throws Exception{
        filter.setInstrumento(controller.getCurrent());
        List<Calibraciones> rows = Service.instance().search(filter);
        if (rows.isEmpty()) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
        controller.setListaC(rows);
        model.setList();
        model.setCurrent(new Calibraciones());
        model.setMode(1);
        model.commit();
    }
    public void delete (Calibraciones filter) throws Exception {
        try {
            filter = model.getCurrent();
            List<Calibraciones> nuevaL = Service.instance().delete(filter);
            controller.setListaC(nuevaL);
            model.setList();
            model.setCurrent(new Calibraciones()); //Para que al borrar quede el tipo de instrumento sin datos hasta que se seleccione otro
            model.setMode(1);
            model.commit();

        }catch (Exception e){
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
    }
    public void edit(int row){
        Calibraciones e = controller.getCurrent().getListCalibracion().get(row);
        try {
            model.setCurrent(Service.instance().read(e));
            model.setMode(2);
            model.commit();
        } catch (Exception ex) {}
    }
    public void clear(){
        model.setCurrent(new Calibraciones());
        model.getCurrent().setInstrumento(controller.getCurrent());
        model.setMode(1);
        model.commit();
    }
    public void save(Calibraciones filter) throws Exception{
        Calibraciones e = new Calibraciones();
        filter.setInstrumento(controller.getCurrent());
        e.setNumero(view.getNumero().getText());
        e.setMediciones(view.getMediciones().getText());
        e.setFecha(view.getFecha().getText());
        e.setInstrumento(model.getSelected());
        try {
            if(model.getMode() == 2) {
                Service.instance().update(e);
             List<Calibraciones> rows = Service.instance().adding(filter);
                calibracionesInstrumento.put(controller.getCurrent(), rows);
                model.setList();
                controller.setListaC(rows);
                model.setCurrent(e);
                model.commit();
            } else if (model.getMode() == 1){
                Service.instance().create(e);
               List<Calibraciones> rows = Service.instance().adding(filter);
                calibracionesInstrumento.put(controller.getCurrent(), rows);
                model.setList();
                controller.setListaC(rows);
                model.setCurrent(e);
                model.setMode(2);
                model.commit();
            }
        } catch (Exception ex) {

            throw new Exception("DATOS INCOMPLETOS");
        }
    }
    public void setSelectedInstrumento() throws Exception {
        try {
            Instrumento selected = controller.getCurrent();
            model.setSelected(selected);
            calibracionesInstrumento.putIfAbsent(selected, new ArrayList<>());
            model.commit();
        }catch (Exception e){
        }
    }
    public List<Calibraciones> obtenerListaInstrumentos(){
        if (controller != null) {
            return controller.obtenerCalibraciones();
        }else{
            return Collections.emptyList();
        }
    }
    public Instrumento getSelectedInstrumento(){
        return controller.getCurrent();
    }
    public void shown(){
        Instrumento selectedInstrumento = getSelectedInstrumento();
        List<Calibraciones> calibraciones = calibracionesInstrumento.computeIfAbsent(selectedInstrumento, k -> new ArrayList<>());
        controller.setListaC(calibraciones);
        model.setList();
        model.commit();
    }
}
