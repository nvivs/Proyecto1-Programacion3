package instrumentos.logic;
import java.util.Objects;

public class Instrumento {
    String serie;
    String descripcion;
    int minimo;
    int maximo;
    int tolerancia;

    public Instrumento(){
        this(" ", " ", 0 , 0 , 0);


    }

    public Instrumento(String serie, String descripcion, int minimo, int maximo, int tolerancia) {
        this.serie = serie;
        this.descripcion = descripcion;
        this.minimo = minimo;
        this.maximo = maximo;
        this.tolerancia = tolerancia;

    }

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
