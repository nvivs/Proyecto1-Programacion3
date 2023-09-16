package instrumentos.logic;

import jakarta.xml.bind.annotation.*;

import java.util.Objects;
@XmlAccessorType(XmlAccessType.FIELD)
public class Calibraciones {
    @XmlID
    String _numero;
    String _mediciones;
    String _fecha;
    @XmlIDREF
    Instrumento instrumento;
    public Instrumento getInstrumento() {
        return instrumento;
    }

    public void setInstrumento(Instrumento instrumento) {
        this.instrumento = instrumento;
    }

    //--------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------
    public Calibraciones(){this("", "", "",null);}
    public Calibraciones(String numero, String mediciones, String fecha, Instrumento instrumento){
        this._numero = numero;
        this._mediciones = mediciones;
        this._fecha = fecha;
        this.instrumento = instrumento;
    }
    public void setNumero(String numero){ this._numero = numero; }
    public String getNumero(){ return this._numero; }
    public void setFecha(String fecha){ this._fecha = fecha; }
    public String getFecha(){ return _fecha; }
    public void setMediciones(String mediciones){ this._mediciones = mediciones; }
    public String getMediciones(){ return _mediciones; }
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
