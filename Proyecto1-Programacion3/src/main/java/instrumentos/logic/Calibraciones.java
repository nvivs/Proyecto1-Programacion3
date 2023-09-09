package instrumentos.logic;

public class Calibraciones {
    String _numero;
    String _fecha;
    int _mediciones;
    //--------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------
    public Calibraciones(){this("", "", 0);}
    public Calibraciones(String numero, String fecha, int mediciones){
        this._numero = numero;
        this._fecha = fecha;
        this._mediciones = mediciones;
    }
    public void setNumero(String numero){ _numero = numero; }
    public String getNumero(){ return _numero; }
    public void setFecha(String fecha){ fecha = fecha; }
    public String getFecha(){ return _fecha; }
    public void setMediciones(int mediciones){ _mediciones = mediciones; }
    public int getMediciones(){ return _mediciones; }
}
