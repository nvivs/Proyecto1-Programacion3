package instrumentos.logic;
import jakarta.xml.bind.annotation.*;

import java.util.List;
import java.util.Objects;
@XmlAccessorType(XmlAccessType.FIELD)
public class Instrumento {
    @XmlID
    String serie;
    @XmlIDREF
    TipoInstrumento tipo;
    String descripcion;
    int minimo;
    int maximo;
    int tolerancia;
    /*@XmlIDREF
    @XmlElementWrapper(name = "calibraciones")
    @XmlElement(name = "calibracion")
    List<Calibraciones> listCalibracion;

     */

    public Instrumento(){
        this("", "", 0 , 0 , 0, null);


    }

    public Instrumento(String serie, String descripcion, int minimo, int maximo, int tolerancia, TipoInstrumento tipo) {
        this.serie = serie;
        this.descripcion = descripcion;
        this.minimo = minimo;
        this.maximo = maximo;
        this.tolerancia = tolerancia;
        this.tipo = tipo;


    }
    public String getSerie() {return serie;}
    public void setSerie(String serie) {this.serie = serie;}
    public TipoInstrumento getTipo() {return tipo;}
    public void setTipo(TipoInstrumento tipo) {this.tipo = tipo;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public int getMinimo() {return minimo;}
    public void setMinimo(int minimo) {this.minimo = minimo;}
    public int getMaximo() {return maximo;}
    public void setMaximo(int maximo) {this.maximo = maximo;}
    public int getTolerancia() {return tolerancia;}
    public void setTolerancia(int tolerancia) {this.tolerancia = tolerancia;}

    /*@Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(serie).append(" - ").append(descripcion).append(" (").append(minimo)
                .append(" - ").append(maximo);

        return sb.toString();
    }*/
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.serie);
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
        final Instrumento other = (Instrumento) obj; //casting
        if (!Objects.equals(this.serie, other.serie)) {
            return false;
        }
        return true;
    }
}
