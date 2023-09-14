package instrumentos.presentation.instrumentos;

import instrumentos.logic.Calibraciones;
import instrumentos.logic.Instrumento;
import instrumentos.logic.Service;
import instrumentos.logic.TipoInstrumento;

import java.util.Collections;
import java.util.List;

public class Controller{
    View view;
    Model model;


    public Controller(View view, Model model) {
        model.init(Service.instance().search(new Instrumento()), Service.instance().search(new Calibraciones()));
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
    }
    public TipoInstrumento getSelected() {
        return model.getSelected();
    }

    public void setSelected(TipoInstrumento selected) {
        model.setSelected(selected);
    }
    public Instrumento getCurrent(){return model.getCurrent();}

    public void search(Instrumento filter) throws  Exception{
        List<Instrumento> rows = Service.instance().search(filter);
        if (rows.isEmpty()) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
        model.setList(rows);
        model.setCurrent(new Instrumento());
        model.setMode(1);
        model.commit();
    }

    public void delete (Instrumento filter) throws Exception {
        try {
            filter = model.getCurrent();
            List<Instrumento> nuevaL = Service.instance().delete(filter);
            model.setList(nuevaL);
            model.setCurrent(new Instrumento());
            model.setMode(1);
            model.commit();

        }catch (Exception e){
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
    }
    public void edit(int row){
        Instrumento e = model.getList().get(row);
        try {
            setSelected(model.getCurrent().getTipo());
            model.setCurrent(Service.instance().read(e));
            model.setMode(2);
            model.commit();
        } catch (Exception ex) {}
    }

    public void clear(){
        model.setCurrent(new Instrumento());
        model.setMode(1);
        model.commit();
    }
    public void save(Instrumento filter) throws Exception{
        Instrumento e = new Instrumento();
        e.setSerie(view.getSerie().getText());
        e.setDescripcion(view.getDescripcion().getText());
        if(Integer.parseInt(view.getMinimo().getText())<Integer.parseInt(view.getMaximo().getText())){
            e.setMinimo(Integer.parseInt(view.getMinimo().getText()));
            e.setMaximo(Integer.parseInt(view.getMaximo().getText()));
        }else{
            throw new Exception("MÍNIMO NO PUEDE SER MAYOR QUE MÁXIMO");
        }
        e.setTolerancia(Integer.parseInt(view.getTolerancia().getText()));
        e.setTipo(view.getTipo());
        try {
            if(model.getMode() == 2) {
                Service.instance().update(e);
                List<Instrumento> rows = Service.instance().search(filter);
                model.setCurrent(e);
                model.setList(rows);
                model.commit();
            } else if (model.getMode() == 1){
                Service.instance().create(e);
                List<Instrumento> rows = Service.instance().search(filter);
                model.setCurrent(e);
                model.setMode(2);
                model.setList(rows);
                model.commit();
            }
        } catch (Exception ex) {
            throw new Exception("DATOS INCOMPLETOS");
        }
    }
    public void addCalibracion(Calibraciones c) throws Exception {
        if(Service.instance().read(c)==null) {
            model.getCurrent().addCalibraciones(c);
            model.commit();
        }
    }
    public List<Calibraciones> obtenerCalibraciones(){
           return model.getList2();
    }
    public void setListaC(List<Calibraciones> c){
        model.setList2(c);
        model.commit();
    }
    public List<TipoInstrumento> getTiposInstrumentos(){
        return Service.instance().getTipos();
    }
    public void shown(){
        model.setListType(Service.instance().search(new TipoInstrumento()));
        model.commit();
    }
}
