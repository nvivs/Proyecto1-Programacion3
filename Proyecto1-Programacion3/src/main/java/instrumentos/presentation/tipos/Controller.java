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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.sql.JDBCType.BOOLEAN;
import static java.sql.JDBCType.NUMERIC;
import static javax.management.openmbean.SimpleType.STRING;

public class Controller{
    private static final long SEARCH_SIMULATION_MS = 10000L;
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
        long inicio = System.currentTimeMillis();
        System.out.println("Inicio de búsqueda: " + new java.util.Date(inicio));

        simulateCpuWorkMillis();

        List<TipoInstrumento> rows = Service.instance().search(filter);
        if (rows.isEmpty()) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
        model.setList(rows);
        model.setCurrent(new TipoInstrumento());
        model.setMode(1);
        model.commit();

        long fin = System.currentTimeMillis();
        long duracion = fin - inicio;
        System.out.println("Fin de búsqueda: " + new java.util.Date(fin));
        System.out.println("Duración total: " + duracion + " ms");

        throw new Exception(rows.size() + " registro(s) encontrado(s).\n\nTiempo de búsqueda: " + duracion + " ms");

    }

    private void simulateCpuWorkMillis() {
        long startNs = System.nanoTime();
        long targetNs = startNs + SEARCH_SIMULATION_MS * 1_000_000L;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] data = ("search-" + startNs).getBytes(StandardCharsets.UTF_8);
            long counter = 0;
            int checksum = 0;

            while (System.nanoTime() < targetNs) {
                md.update(data);
                md.update(Long.toString(counter++).getBytes(StandardCharsets.UTF_8));
                data = md.digest();
                checksum += (data[0] & 0xff);
            }

            if (checksum == Integer.MIN_VALUE) {
                System.out.println(checksum);
            }
        } catch (NoSuchAlgorithmException e) {
            long busyUntil = System.nanoTime() + SEARCH_SIMULATION_MS * 1_000_000L;
            long counter = 0;
            while (System.nanoTime() < busyUntil) {
                counter += (counter << 1) ^ 0x9e3779b97f4a7c15L;
            }
            if (counter == Long.MIN_VALUE) {
                System.out.println(counter);
            }
        }
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

    public void uploadFile(File file) throws Exception {
        long inicio = System.currentTimeMillis(); //  inicio
        System.out.println("Inicio de carga: " + new java.util.Date(inicio));

        if (file == null || !file.exists()) {
            throw new Exception("ARCHIVO NO VÁLIDO");
        }

        List<TipoInstrumento> creados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String codigo = getCellValue(row, 0);
                    String nombre = getCellValue(row, 1);
                    String unidad = getCellValue(row, 2);

                    if (codigo.isEmpty() || nombre.isEmpty() || unidad.isEmpty()) {
                        errores.add("Fila " + (i + 1) + ": datos incompletos, omitida.");
                        continue;
                    }

                    try {
                        // Simula una validación compleja o una consulta externa que toma 1ms
                        Thread.sleep(1);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                    TipoInstrumento t = new TipoInstrumento();
                    t.setCodigo(codigo);
                    t.setNombre(nombre);
                    t.setUnidad(unidad);
                    Service.instance().create(t);
                    creados.add(t);

                } catch (Exception ex) {
                    errores.add("Fila " + (i + 1) + ": " + ex.getMessage());
                }
            }

        } catch (Exception e) {
            throw new Exception("ERROR AL LEER EL ARCHIVO: " + e.getMessage());
        }

        model.setList(Service.instance().search(new TipoInstrumento()));
        model.setCurrent(new TipoInstrumento());
        model.setMode(1);
        model.commit();

        long fin = System.currentTimeMillis(); // fin
        long duracion = fin - inicio;
        System.out.println("Fin de carga: " + new java.util.Date(fin));
        System.out.println("Duración total: " + duracion + " ms");

        StringBuilder msg = new StringBuilder();
        msg.append(creados.size()).append(" tipo(s) creado(s) exitosamente.");
        msg.append("\n\nTiempo de carga: " + duracion + " ms"); //
        if (!errores.isEmpty()) {
            msg.append("\n\nAdvertencias:\n");
            errores.forEach(e -> msg.append("• ").append(e).append("\n"));
        }
        throw new Exception(msg.toString());
    }
    private String getCellValue(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(
                col, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default:      return "";
        }
    }
}
