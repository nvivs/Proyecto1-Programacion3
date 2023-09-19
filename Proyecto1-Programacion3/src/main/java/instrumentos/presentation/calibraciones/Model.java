package instrumentos.presentation.calibraciones;

import instrumentos.logic.Calibraciones;
import instrumentos.logic.Instrumento;
import java.util.List;
import java.util.Observer;

public class Model extends java.util.Observable {
    List<Calibraciones> list;
    Calibraciones current;
    Instrumento selected;
    int mode = 1; // 1 = No editado.
    int changedProps = NONE;
    //----------------------------------------------------------------------------------------------------------------------
    public static int NONE=0;
    public static int LIST=1;
    public static int CURRENT=2;
    //----------------------------------------------------------------------------------------------------------------------
    public Model() {
    }
    public void setCurrent(Calibraciones current) {
        changedProps +=CURRENT;
        this.current = current;
    }
    public Calibraciones getCurrent() {
        return current;
    }
    public Instrumento getSelected() {
        return selected;
    }
    public void setSelected(Instrumento selected) {
        this.selected = selected;
    }
    public void setMode(int m){
        mode = m;
    }
    public int getMode(){
        return mode;
    }
    public List<Calibraciones> getList() {
        return list;
    }
    public void setProps(){
        changedProps += LIST;
    }
//----------------------------------------------------------------------------------------------------------------------
    @Override
    public void addObserver(Observer o) {
        super.addObserver(o);
        commit();
    }
    public void commit(){
        setChanged();
        notifyObservers(changedProps);
        changedProps = NONE;
    }
}
