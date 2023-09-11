package instrumentos.logic;

import java.util.Objects;

public class Calibraciones {
    String _numero;
    String _mediciones;
    String _fecha;
    //--------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------
    public Calibraciones(){this("", "", "");}
    public Calibraciones(String numero, String mediciones, String fecha){
        this._numero = numero;
        this._mediciones = mediciones;
        this._fecha = fecha;
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
