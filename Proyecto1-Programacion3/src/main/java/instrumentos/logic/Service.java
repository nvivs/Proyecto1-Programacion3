package instrumentos.logic;

import instrumentos.data.Data;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Service {
    private static Service theInstance;

    public static Service instance(){
        if (theInstance == null) theInstance = new Service();
        return theInstance;
    }
    private Data data;

    private Service(){
        data = new Data();
    }

    //================= TIPOS DE INSTRUMENTO ============
    public void create(TipoInstrumento e) throws Exception{
        TipoInstrumento result = data.getTipos().stream()
                .filter(i->i.getCodigo().equals(e.getCodigo())).findFirst().orElse(null);
        if (result==null) data.getTipos().add(e);
        else throw new Exception("Tipo ya existe");
    }

    public TipoInstrumento read(TipoInstrumento e) throws Exception{
        TipoInstrumento result = data.getTipos().stream()
                .filter(i->i.getCodigo().equals(e.getCodigo())).findFirst().orElse(null);
        if (result!=null) return result;
        else throw new Exception("Tipo no existe");
    }

    public void update(TipoInstrumento e) throws Exception{
        TipoInstrumento result;
        try{
            result = this.read(e);
            data.getTipos().remove(result);
            data.getTipos().add(e);
        }catch (Exception ex) {
            throw new Exception("Tipo no existe");
        }
    }

    public List<TipoInstrumento> delete(TipoInstrumento e) throws Exception{
        List<TipoInstrumento> nueva = data.getTipos();
        if(nueva.remove(e)){
            return nueva;
        }else{
            throw new Exception("Instrumento no existe");
        }
     }

    public List<TipoInstrumento> search(TipoInstrumento e){
        return data.getTipos().stream()
                .filter(i->i.getNombre().contains(e.getNombre()))
                .sorted(Comparator.comparing(TipoInstrumento::getNombre))
                .collect(Collectors.toList());
    }

    //================= CALIBRACIONES DE INSTRUMENTOS ==================

    public void create(Calibraciones cal) throws Exception {
        Calibraciones result = data.getCalibraciones().stream()
                .filter(i->i.getNumero().equals(cal.getNumero()))
                .findFirst().orElse(null);
        if(result==null) data.getCalibraciones().add(cal);
        else throw new Exception("Calibracion ya existe.");
    }
    public Calibraciones read(Calibraciones cal) throws Exception {
        Calibraciones result = data.getCalibraciones().stream()
                .filter(i->i.getNumero().equals(cal.getNumero()))
                .findFirst().orElse(null);
        if(result!=null) return result;
        else throw new Exception("Calibracion no existe.");
    }
    public void update(Calibraciones cal) throws Exception{
        Calibraciones result;
        try{
            result = this.read(cal);
            data.getCalibraciones().remove(result);
            data.getCalibraciones().add(cal);
        } catch (Exception ex) {
            throw new Exception("Calibracion no existe");
        }
    }
    public List<Calibraciones> delete(Calibraciones cal) throws Exception{
        List<Calibraciones> nueva = data.getCalibraciones();
        if(nueva.remove(cal)){
            return nueva;
        }else{
            throw new Exception("Calibracion no existe");
        }
    }

    public List<Calibraciones> search(Calibraciones cal){
        return data.getCalibraciones().stream()
                .filter(i->i.getNumero().contains(cal.getNumero()))
                .sorted(Comparator.comparing(Calibraciones::getNumero))
                .collect(Collectors.toList());
    }
 }
