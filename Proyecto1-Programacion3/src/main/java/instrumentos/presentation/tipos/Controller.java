package instrumentos.presentation.tipos;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import instrumentos.logic.Instrumento;
import instrumentos.logic.Service;
import instrumentos.logic.TipoInstrumento;
import java.util.List;

public class Controller{
    View view;

    Model model;

    public Controller(View view, Model model) {
        model.init(Service.instance().search(new TipoInstrumento()));
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
    }

    public void search(TipoInstrumento filter) throws  Exception{
        List<TipoInstrumento> rows = Service.instance().search(filter);
        if (rows.isEmpty()) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
        model.setList(rows);
        model.setCurrent(new TipoInstrumento());
        model.setMode(1);
        model.commit();
    }

    public void delete (TipoInstrumento filter) throws Exception {
            for(Instrumento t : Service.instance().getInstrumentos()){
                if (t.getTipo().equals(model.getCurrent())){
                    throw new Exception("NO SE PUEDE ELIMINAR PORQUE EXISTE UNO O MAS INSTRUMENTOS ASOCIADOS");
                }
            }
            filter = model.getCurrent();
            List<TipoInstrumento> nuevaL = Service.instance().delete(filter);
            model.setList(nuevaL);
            model.setCurrent(new TipoInstrumento()); //Para que al borrar quede el tipo de instrumento sin datos hasta que se seleccione otro
            model.setMode(1);
            model.commit();
    }
    public void edit(int row){
        TipoInstrumento e = model.getList().get(row);
        try {
            model.setCurrent(Service.instance().read(e));
            model.setMode(2);
            model.commit();
        } catch (Exception ex) {}
    }

    public void clear(){
        model.setCurrent(new TipoInstrumento());
        model.setMode(1);
        model.commit();
    }
    public void save(TipoInstrumento filter) throws Exception{
        TipoInstrumento e = new TipoInstrumento();
        e.setCodigo(view.getCodigo().getText());
        e.setNombre(view.getNombre().getText());
        e.setUnidad(view.getUnidad().getText());

        try {
            if(model.getMode() == 2) {
                Service.instance().update(e);
                List<TipoInstrumento> rows = Service.instance().search(filter);
                model.setCurrent(e);
                model.setList(rows);
                model.commit();
            } else if (model.getMode() == 1){
                Service.instance().create(e);
                List<TipoInstrumento> rows = Service.instance().search(filter);
                model.setCurrent(e);
                model.setMode(2);
                model.setList(rows);
                model.commit();
            }
        } catch (Exception ex) {
            throw new Exception("DATOS INCOMPLETOS");
        }
    }
    public List<TipoInstrumento> getTipos(){
        return Service.instance().getTipos();
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
            String archivo = "Tipos.pdf";
            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            PdfWriter writer = new PdfWriter(archivo);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            document.setMargins(20, 20, 20, 20);
            Table header = new Table(1);
            header.setWidth(400);
            header.setHorizontalAlignment(HorizontalAlignment.CENTER);
            header.addCell(getCeldaP(new Paragraph("Sistema De Instrumentos").setFont(font).setBold().setFontSize(25), TextAlignment.CENTER, false));
            header.addCell(getCeldaI(new Image(ImageDataFactory.create("Proyecto1-Programacion3\\src\\main\\resources\\instrumentos\\presentation\\icons\\Laboratorio.png")).setWidth(450).setHeight(280), HorizontalAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph(" "),TextAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph(" "),TextAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph("Tipos de Instrumentos").setFont(font).setBold().setFontSize(20), TextAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph(" "),TextAlignment.LEFT, false));
            document.add(header);
            Table titulos = new Table(3);
            titulos.setWidth(400);
            titulos.setHorizontalAlignment(HorizontalAlignment.CENTER);
            titulos.addCell(getCeldaP(new Paragraph("Código").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("Nombre").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("Unidad").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            for (TipoInstrumento t : model.getList()) {
                titulos.addCell(getCeldaP(new Paragraph(t.getCodigo()).setBackgroundColor(ColorConstants.LIGHT_GRAY), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(t.getNombre()), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(t.getUnidad()), TextAlignment.CENTER, true));
            }
            document.add(titulos);
            document.close();
        }catch (Exception e){
            throw new Exception("No se pudo crear el PDF");
        }
    }
}
