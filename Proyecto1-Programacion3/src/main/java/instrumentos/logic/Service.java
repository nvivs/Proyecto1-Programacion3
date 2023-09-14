package instrumentos.logic;

import instrumentos.data.Data;
import instrumentos.data.XmlPersister;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
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
        try{
            data = XmlPersister.instance().load();
        }catch(Exception e){
            data = new Data();
        }
    }
    public void stop(){
        try{
            XmlPersister.instance().store(data);
        }catch(Exception e){
            System.out.println(e);
        }
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
    public List<TipoInstrumento> getTipos(){
        return data.getTipos();
     }

    public List<TipoInstrumento> search(TipoInstrumento e){
        return data.getTipos().stream()
                .filter(i->i.getNombre().contains(e.getNombre()))
                .sorted(Comparator.comparing(TipoInstrumento::getNombre))
                .collect(Collectors.toList());
    }

    //================= INSTRUMENTOS =================

    public void create(Instrumento e) throws Exception{
        Instrumento result = data.getInstrumentos().stream()
                .filter(i->i.getSerie().equals(e.getSerie())).findFirst().orElse(null);
        if (result==null) data.getInstrumentos().add(e);
        else throw new Exception("Instrumento ya existe");
    }

    public Instrumento read(Instrumento e) throws Exception{
        Instrumento result = data.getInstrumentos().stream()
                .filter(i->i.getSerie().equals(e.getSerie())).findFirst().orElse(null);
        if (result!=null) return result;
        else throw new Exception("Instrumento no existe");
    }

    public void update(Instrumento e) throws Exception{
        Instrumento result;
        try{
            result = this.read(e);
            data.getInstrumentos().remove(result);
            data.getInstrumentos().add(e);
        }catch (Exception ex) {
            throw new Exception("Instrumento no existe");
        }
    }

    public List<Instrumento> delete(Instrumento e) throws Exception{
        List<Instrumento> nueva = data.getInstrumentos();
        if(nueva.remove(e)){
            return nueva;
        }else{
            throw new Exception("Instrumento no existe");
        }
    }

    public List<Instrumento> search(Instrumento e){
        return data.getInstrumentos().stream()
                .filter(i->i.getDescripcion().contains(e.getDescripcion()))
                .sorted(Comparator.comparing(Instrumento::getDescripcion))
                .collect(Collectors.toList());
    }
    public List<Instrumento> getInstrumentos(){ return data.getInstrumentos(); }

    //================= CALIBRACIONES DE INSTRUMENTOS ==================

    public void create(Calibraciones cal) throws Exception {
        Instrumento instru = read(cal.getInstrumento());
        List<Calibraciones> nueva = instru.getListCalibracion();
        Calibraciones result = nueva.stream()
                .filter(i->i.getNumero().equals(cal.getNumero()))
                .findFirst().orElse(null);
        if(result==null) nueva.add(cal);
        else throw new Exception("Calibracion ya existe.");
    }
    public Calibraciones read(Calibraciones cal) throws Exception {
        Instrumento instru = read(cal.getInstrumento());
        List<Calibraciones> nueva = instru.getListCalibracion();
        Calibraciones result = nueva.stream()
                .filter(i->i.getNumero().equals(cal.getNumero()))
                .findFirst().orElse(null);
         return result;
        //else throw new Exception("Calibracion no existe.");
    }
    public void update(Calibraciones cal) throws Exception{
        Calibraciones result;
        Instrumento instru = read(cal.getInstrumento());
        List<Calibraciones> nueva = instru.getListCalibracion();
        try{
            result = this.read(cal);
            nueva.remove(result);
            nueva.add(cal);
        } catch (Exception ex) {
            throw new Exception("Calibracion no existe");
        }
    }
    public List<Calibraciones> delete(Calibraciones cal) throws Exception{
        Instrumento instru = read(cal.getInstrumento());
        List<Calibraciones> nueva = instru.getListCalibracion();
        if(nueva.remove(cal)){
            return nueva;
        }else{
            throw new Exception("Calibracion no existe");
        }
    }
    public List<Calibraciones> search(Calibraciones cal) {
        try {
            Instrumento instru = read(cal.getInstrumento());
            List<Calibraciones> Nueva = instru.getListCalibracion();
            List<Calibraciones> resultado = new ArrayList<>();
            for (Calibraciones calibracion : Nueva) {
                if (calibracion.getFecha().contains(cal.getFecha())) {
                    resultado.add(calibracion);
                }
            }
            return resultado;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
    public List<Calibraciones> adding (Calibraciones cal) throws Exception {
        try {
            Instrumento instru = cal.getInstrumento();
            List<Calibraciones> Nueva = instru.getListCalibracion();
            List<Calibraciones> resultado = new ArrayList<>();
            for (Calibraciones calibracion : Nueva) {
                if(Nueva.isEmpty()){
                    resultado.add(calibracion);
                }
                if (calibracion.getNumero().contains(cal.getNumero())) {
                    resultado.add(calibracion);
                }
            }
            return resultado;
        } catch (Exception ex) {
            throw new Exception("Calibracion ya existe");
        }
    }
    public List<Calibraciones> getCalibracionesDelInstrumento(Instrumento selected) {
        List<Calibraciones> calibracionesDelInstrumento = new ArrayList<>();
        List<Calibraciones> Nueva = selected.getListCalibracion();
        for (Calibraciones calibracion : Nueva) {
            if(Nueva.isEmpty()){
                return Collections.emptyList();
            }
            if (calibracion.getInstrumento().equals(selected)) {
                calibracionesDelInstrumento.add(calibracion);
            }
        }
        return calibracionesDelInstrumento;
    }
   // public List<Calibraciones> getCalibraciones(){ return data.getCalibraciones(); }
 }
