package instrumentos.presentation.tipos;

import instrumentos.Application;
import instrumentos.logic.Service;
import instrumentos.logic.TipoInstrumento;

import java.util.List;

public class Controller{
    View view;
    Model model;

    public Controller(View view, Model model) {
        model.init(Service.instance().search(new TipoInstrumento()));
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
    }

    public void search(TipoInstrumento filter) throws  Exception{
        List<TipoInstrumento> rows = Service.instance().search(filter);
        if (rows.isEmpty()) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
        model.setList(rows);
        model.setCurrent(new TipoInstrumento());
        model.setMode(1);
        model.commit();
    }

    public void delete (TipoInstrumento filter) throws Exception {
        try {
            filter = model.getCurrent();
            List<TipoInstrumento> nuevaL = Service.instance().delete(filter);
            model.setList(nuevaL);
            model.setCurrent(new TipoInstrumento()); //Para que al borrar quede el tipo de instrumento sin datos hasta que se seleccione otro
            model.setMode(1);
            model.commit();

        }catch (Exception e){
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
    }
    public void edit(int row){
        TipoInstrumento e = model.getList().get(row);
        try {
            model.setCurrent(Service.instance().read(e));
            model.setMode(2);
            model.commit();
        } catch (Exception ex) {}
    }

    public void clear(){
        model.setCurrent(new TipoInstrumento());
        model.setMode(1);
        model.commit();
    }
    public void save(TipoInstrumento filter) throws Exception{
        TipoInstrumento e = new TipoInstrumento();
        e.setCodigo(view.getCodigo().getText());
        e.setNombre(view.getNombre().getText());
        e.setUnidad(view.getUnidad().getText());

        try {
            if(model.getMode() == 2) {
                Service.instance().update(e);
                List<TipoInstrumento> rows = Service.instance().search(filter);
                model.setCurrent(e);
                model.setList(rows);
                model.commit();
            } else if (model.getMode() == 1){
                Service.instance().create(e);
                List<TipoInstrumento> rows = Service.instance().search(filter);
                model.setCurrent(e);
                model.setMode(2);
                model.setList(rows);
                model.commit();
            }
        } catch (Exception ex) {
            throw new Exception("DATOS INCOMPLETOS");
        }
    }
}
