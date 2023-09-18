package instrumentos.logic;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@XmlAccessorType(XmlAccessType.FIELD)
public class Calibraciones {
    @XmlID
    String _numero;
    int _mediciones;
    String _fecha;
    @XmlIDREF
    Instrumento instrumento;

    List<Medida> medidas;
    public Instrumento getInstrumento() {
        return instrumento;
    }
    public void setInstrumento(Instrumento instrumento) {
        this.instrumento = instrumento;
    }

    //--------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------
    public Calibraciones(){
        this("", 0, "",null);
        medidas = new ArrayList<>();
    }
    public Calibraciones(String numero, int mediciones, String fecha, Instrumento instrumento){
        this._numero = numero;
        this._mediciones = mediciones;
        this._fecha = fecha;
        this.instrumento = instrumento;
        medidas = new ArrayList<>();
    }
    public void setNumero(String numero){ this._numero = numero; }
    public String getNumero(){ return this._numero; }
    public void setFecha(String fecha){ this._fecha = fecha; }
    public String getFecha(){ return _fecha; }
    public void setMediciones(int mediciones){ this._mediciones = mediciones; }
    public int getMediciones(){ return _mediciones; }
    public List<Medida> getMedidas() {
        return medidas;
    }

    public void CreateMedidas() {
        int total = 0;
        for(int i = 0; i<_mediciones; i++){
            Medida med = new Medida();
            med.setMedida(i+1);
            if(instrumento!=null){
                total = ((instrumento.getMinimo())+i *((instrumento.getMaximo()-instrumento.getMinimo())/ (_mediciones)));
            }
            med.setReferencia(total);
            med.setLectura(0);
            medidas.add(med);
        }
    }
//----------------------------------------------------------------------------------------------------------------------
    public String toString(){ return this._numero; }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this._numero);
        return hash;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Calibraciones other = (Calibraciones) obj; //casting
        if (!Objects.equals(this._numero, other._numero)) {
            return false;
        }
        return true;
    }
}
