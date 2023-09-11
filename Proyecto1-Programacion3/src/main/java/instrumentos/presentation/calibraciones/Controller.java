package instrumentos.presentation.calibraciones;
import instrumentos.Application;
import instrumentos.logic.Service;
import instrumentos.logic.Calibraciones;
import instrumentos.presentation.calibraciones.Model;
import instrumentos.presentation.calibraciones.View;
import java.util.List;

public class Controller {
    View view;
    Model model;
    //----------------------------------------------------------------------------------------------------------------------
    public Controller(Model model, View view) {
        model.init(Service.instance().search(new Calibraciones()));
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
    }
    public void search(Calibraciones filter) throws Exception{
        List<Calibraciones> rows = Service.instance().search(filter);
        if (rows.isEmpty()) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
        model.setList(rows);
        model.setCurrent(new Calibraciones());
        model.setMode(1);
        model.commit();
    }
    public void delete (Calibraciones filter) throws Exception {
        try {
            filter = model.getCurrent();
            List<Calibraciones> nuevaL = Service.instance().delete(filter);
            model.setList(nuevaL);
            model.setCurrent(new Calibraciones()); //Para que al borrar quede el tipo de instrumento sin datos hasta que se seleccione otro
            model.setMode(1);
            model.commit();

        }catch (Exception e){
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
    }
    public void edit(int row){
        Calibraciones e = model.getList().get(row);
        try {
            model.setCurrent(Service.instance().read(e));
            model.setMode(2);
            model.commit();
        } catch (Exception ex) {}
    }
    public void clear(){
        model.setCurrent(new Calibraciones());
        model.setMode(1);
        model.commit();
    }
    public void save(Calibraciones filter) throws Exception{
        Calibraciones e = new Calibraciones();
        e.setNumero(view.getNumero().getText());
        e.setMediciones(view.getMediciones().getText());
        e.setFecha(view.getFecha().getText());

        try {
            if(model.getMode() == 2) {
                Service.instance().update(e);
                List<Calibraciones> rows = Service.instance().search(filter);
                model.setCurrent(e);
                model.setList(rows);
                model.commit();
            } else if (model.getMode() == 1){
                Service.instance().create(e);
                List<Calibraciones> rows = Service.instance().search(filter);
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
