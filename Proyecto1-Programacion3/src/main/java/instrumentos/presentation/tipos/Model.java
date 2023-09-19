package instrumentos.presentation.tipos;
import instrumentos.logic.TipoInstrumento;
import java.util.List;
import java.util.Observer;

public class Model extends java.util.Observable{
    List<TipoInstrumento> list;
    TipoInstrumento current;
    int mode = 1; // 1 = No editado.
    int changedProps = NONE;

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

    public Model() {
    }

    public void init(List<TipoInstrumento> list){
        setList(list);
        setCurrent(new TipoInstrumento());
    }

    public List<TipoInstrumento> getList() {
        return list;
    }
    public void setList(List<TipoInstrumento> list){
        this.list = list;
        changedProps +=LIST;
    }

    public TipoInstrumento getCurrent() {
        return current;
    }

    public void setMode(int m){
        mode = m;
    }
    public int getMode(){
        return mode;
    }

    public void setCurrent(TipoInstrumento current) {
        changedProps +=CURRENT;
        this.current = current;
    }

    public static int NONE=0;
    public static int LIST=1;
    public static int CURRENT=2;
}
