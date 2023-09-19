package instrumentos.logic;

import java.util.Objects;

public class Medida {
    private int _medida;
    private int _referencia;
    private int _lectura;
//----------------------------------------------------------------------------------------------------------------------
    public Medida() {this(0,0,0);}
    public Medida(int medida, int referencia, int lectura) {
        this._medida = medida;
        this._referencia = referencia;
        this._lectura = lectura;
    }
    public void setMedida(int medida) { this._medida = medida; }
    public int getMedida() { return _medida; }
    public void setReferencia(int referencia) { this._referencia = referencia; }
    public int getReferencia() { return _referencia; }
    public void setLectura(int lectura) { this._lectura = lectura; }
    public int getLectura() { return _lectura; }
//----------------------------------------------------------------------------------------------------------------------
    @Override
    public String toString() { return ""; }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this._medida);
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
        final Medida other = (Medida) obj; //casting
        return Objects.equals(this._medida, other._medida);
    }
}
