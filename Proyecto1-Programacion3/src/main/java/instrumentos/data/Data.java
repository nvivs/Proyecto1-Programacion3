package instrumentos.data;

import instrumentos.logic.Instrumento;
import instrumentos.logic.Medida;
import instrumentos.logic.TipoInstrumento;
import instrumentos.logic.Calibraciones;
import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Data {
    @XmlElementWrapper(name = "tipos")
    @XmlElement(name = "tipo")
    private List<TipoInstrumento> tipos;
    @XmlElementWrapper(name = "instrumentos")
    @XmlElement(name = "instrumento")
    private List <Instrumento> instrumentos;

   // private List<Calibraciones> calibracionIns;
    private List<Medida> med;

    public Data() {
        tipos = new ArrayList<>();
        instrumentos = new ArrayList<>();
        med = new ArrayList<>();
        tipos.add(new TipoInstrumento("TER","Termómetro","Grados Celcius") );
        tipos.add(new TipoInstrumento("BAR","Barómetro","PSI") );
    }

    public void add(Instrumento i){
        instrumentos.add(i);
    }
    public List<TipoInstrumento> getTipos() {
        return tipos;
    }
    public List<Instrumento> getInstrumentos() {
        return instrumentos;
    }
    public List<Medida> getMedidas(){ return med; }
}
