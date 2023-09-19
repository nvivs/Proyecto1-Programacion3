package instrumentos.data;

import instrumentos.logic.Instrumento;
import instrumentos.logic.TipoInstrumento;
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

    public Data() {
        tipos = new ArrayList<>();
        instrumentos = new ArrayList<>();
        tipos.add(new TipoInstrumento("TER","Termómetro","Grados Celcius") );
        tipos.add(new TipoInstrumento("BAR","Barómetro","PSI") );
    }

    public List<TipoInstrumento> getTipos() {
        return tipos;
    }
    public List<Instrumento> getInstrumentos() {
        return instrumentos;
    }
}
