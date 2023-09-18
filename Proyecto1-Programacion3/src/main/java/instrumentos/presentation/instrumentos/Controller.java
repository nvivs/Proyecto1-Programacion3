package instrumentos.presentation.instrumentos;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import instrumentos.logic.Calibraciones;
import instrumentos.logic.Instrumento;
import instrumentos.logic.Service;
import instrumentos.logic.TipoInstrumento;

import java.util.Collections;
import java.util.List;

public class Controller{
    View view;
    Model model;


    public Controller(View view, Model model) {
        model.init(Service.instance().search(new Instrumento()), Service.instance().search(new Calibraciones()));
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
    }
    public TipoInstrumento getSelected() {
        return model.getSelected();
    }

    public void setSelected(TipoInstrumento selected) {
        model.setSelected(selected);
    }
    public Instrumento getCurrent(){return model.getCurrent();}

    public void search(Instrumento filter) throws  Exception{
        List<Instrumento> rows = Service.instance().search(filter);
        if (rows.isEmpty()) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
        model.setList(rows);
        model.setCurrent(new Instrumento());
        model.setMode(1);
        model.commit();
    }

    public void delete (Instrumento filter) throws Exception {
            if(!filter.getListCalibracion().isEmpty()){
            filter = model.getCurrent();
            List<Instrumento> nuevaL = Service.instance().delete(filter);
            model.setList(nuevaL);
            model.setCurrent(new Instrumento());
            model.setMode(1);
            model.commit();
            }else{
                throw new Exception("NO SE PUEDE ELIMINAR EL INSTRUMENTO PORQUE EXISTEN CALIBRACIONES ASOCIADAS");
            }
    }
    public void edit(int row){
        Instrumento e = model.getList().get(row);
        try {
            model.setCurrent(Service.instance().read(e));
            model.setMode(2);
            setSelected(model.getCurrent().getTipo());
            model.commit();
        } catch (Exception ex) {}
    }

    public void clear(){
        model.setCurrent(new Instrumento());
        model.setMode(1);
        model.commit();
    }
    public void save(Instrumento filter) throws Exception{
        Instrumento e = new Instrumento();
        e.setSerie(view.getSerie().getText());
        e.setDescripcion(view.getDescripcion().getText());
        if(Integer.parseInt(view.getMinimo().getText())<Integer.parseInt(view.getMaximo().getText())){
            e.setMinimo(Integer.parseInt(view.getMinimo().getText()));
            e.setMaximo(Integer.parseInt(view.getMaximo().getText()));
        }else{
            throw new Exception("MÍNIMO NO PUEDE SER MAYOR QUE MÁXIMO");
        }
        e.setTolerancia(Integer.parseInt(view.getTolerancia().getText()));
        e.setTipo(view.getTipo());
        if(e.getTipo() == null){
            throw new Exception("TIPO DE INSTRUMENTO REQUERIDO");
        }
        try {
            if(model.getMode() == 2) {
                Service.instance().update(e);
                List<Instrumento> rows = Service.instance().search(filter);
                model.setCurrent(e);
                model.setList(rows);
                model.commit();
            } else if (model.getMode() == 1){
                Service.instance().create(e);
                List<Instrumento> rows = Service.instance().search(filter);
                model.setCurrent(e);
                model.setMode(2);
                model.setList(rows);
                model.commit();
            }
        } catch (Exception ex) {
            throw new Exception("DATOS INCOMPLETOS");
        }
    }
    public List<Calibraciones> obtenerCalibraciones(){
           return model.getList2();
    }
    public void setListaC(List<Calibraciones> c){
        model.setList2(c);
        model.commit();
    }
    public List<TipoInstrumento> getTiposInstrumentos(){
        return Service.instance().getTipos();
    }
    public void shown() throws Exception {
        model.setListType(Service.instance().search(new TipoInstrumento()));
        model.commit();
    }
    private Cell getCeldaI(Image image, HorizontalAlignment horizontalAlignment, boolean border){
        image.setMargins(0,0,0,0);
        Cell cellI = new Cell().add(image);
        image.setHorizontalAlignment(horizontalAlignment);
        if(!border) cellI.setBorder(Border.NO_BORDER);
        return cellI;
    }
    private Cell getCeldaP(Paragraph paragraph, TextAlignment textAlignment, boolean border){
        paragraph.setMargin(0);
        Cell cellP = new Cell().add(paragraph);
        cellP.setTextAlignment(textAlignment);
        if(!border) cellP.setBorder(Border.NO_BORDER) ;
        return cellP;
    }
    public void createDocument() throws Exception{
        try {
            String archivo = "Instrumentos.pdf";
            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            PdfWriter writer = new PdfWriter(archivo);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            document.setMargins(20, 40, 20, 40);
            Table header = new Table(1);
            header.setWidth(400);
            header.setHorizontalAlignment(HorizontalAlignment.CENTER);
            header.addCell(getCeldaP(new Paragraph("Sistema De Instrumentos").setFont(font).setBold().setUnderline().setFontSize(25), TextAlignment.CENTER, false));
            header.addCell(getCeldaI(new Image(ImageDataFactory.create("Proyecto1-Programacion3\\src\\main\\resources\\instrumentos\\presentation\\icons\\Laboratorio.png")).setWidth(450).setHeight(280), HorizontalAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph(" "),TextAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph(" "),TextAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph("Instrumentos").setFont(font).setBold().setFontSize(20), TextAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph(" "),TextAlignment.LEFT, false));
            document.add(header);
            Table titulos = new Table(6);
            titulos.setWidth(530);
            titulos.setHorizontalAlignment(HorizontalAlignment.CENTER);
            titulos.addCell(getCeldaP(new Paragraph("Serie").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("Tipo").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("Descripción").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("Mínimo").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("Máximo").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("Tolerancia").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));

            for (Instrumento i : model.getList()) {
                titulos.addCell(getCeldaP(new Paragraph(i.getSerie()).setBackgroundColor(ColorConstants.LIGHT_GRAY), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(i.getTipo().getNombre()), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(i.getDescripcion()), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(String.valueOf(i.getMinimo())), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(String.valueOf(i.getMaximo())), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(String.valueOf(i.getTolerancia())), TextAlignment.CENTER, true));
            }
            document.add(titulos);
            document.close();
        }catch (Exception e){
            throw new Exception("No se pudo crear el PDF");
        }
    }
}
