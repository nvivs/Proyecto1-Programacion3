package instrumentos.presentation.calibraciones;

import instrumentos.logic.Calibraciones;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class Model extends java.util.Observable {
    List<Calibraciones> list;
    Calibraciones current;
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
    public void setMode(int m){
        mode = m;
    }
    public int getMode(){
        return mode;
    }
    public List<Calibraciones> getList() {
        return list;
    }
    public void setList(List<Calibraciones> list){
        this.list = list;
        changedProps +=LIST;
    }
//----------------------------------------------------------------------------------------------------------------------
    public Calibraciones getNext() {
        if (current!=null&&list!=null){
            int currentyIndex = list.indexOf(current);
            if(currentyIndex >= 0 && currentyIndex < list.size()-1){
                return list.get(currentyIndex+1);
            }
        }
        return null;
    }
    public void init(List<Calibraciones> list){
        setList(list);
        setCurrent(new Calibraciones());
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
