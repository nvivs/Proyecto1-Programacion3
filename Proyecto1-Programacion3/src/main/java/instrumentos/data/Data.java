package instrumentos.data;

import instrumentos.logic.TipoInstrumento;
import instrumentos.logic.Calibraciones;

import java.util.ArrayList;
import java.util.List;

public class Data {
    private List<TipoInstrumento> tipos;
    private List<Calibraciones> calibracionIns;

    public Data() {
        tipos = new ArrayList<>();

        tipos.add(new TipoInstrumento("TER","Termómetro","Grados Celcius") );
        tipos.add(new TipoInstrumento("BAR","Barómetro","PSI") );

        calibracionIns = new ArrayList<>();
        calibracionIns.add(new Calibraciones("001", "21/08/2023", 3));
    }

    public List<TipoInstrumento> getTipos() {
        return tipos;
    }
    public List<Calibraciones> getCalibraciones() { return calibracionIns; }
 }