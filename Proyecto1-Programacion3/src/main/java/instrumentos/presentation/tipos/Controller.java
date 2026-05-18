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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.sql.JDBCType.BOOLEAN;
import static java.sql.JDBCType.NUMERIC;
import static javax.management.openmbean.SimpleType.STRING;

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
        long inicio = System.currentTimeMillis();
        System.out.println("Inicio de búsqueda: " + new java.util.Date(inicio));

        // Simula latencia de backend distribuida entre hilos para pruebas de paralelización
        final long totalDelayMs = 10000L; // tiempo total a simular
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        long perThread = totalDelayMs / numThreads;
        long remainder = totalDelayMs % numThreads;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            final long sleepMs = perThread + (i == 0 ? remainder : 0);
            executor.submit(() -> {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(totalDelayMs + 2000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

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
        long inicio = System.currentTimeMillis();
        System.out.println("Inicio de carga: " + new java.util.Date(inicio));

        if (file == null || !file.exists()) {
            throw new Exception("ARCHIVO NO VÁLIDO");
        }

        // 1. Colecciones Seguras
        List<TipoInstrumento> validosParaInsertar = java.util.Collections.synchronizedList(new ArrayList<>());
        List<String> errores = java.util.Collections.synchronizedList(new ArrayList<>());
        List<org.apache.poi.ss.usermodel.Row> filasAProcesar = new ArrayList<>();

        // 2. Crear estructuras de búsqueda RÁPIDA (O(1)) para no saturar los hilos
        // Extraemos los códigos que ya están en el sistema
        java.util.Set<String> codigosExistentes = Service.instance().getTipos().stream()
                .map(TipoInstrumento::getCodigo)
                .collect(java.util.stream.Collectors.toSet());

        // Set concurrente para evitar que el mismo Excel traiga 2 códigos iguales
        java.util.Set<String> codigosEnEsteExcel = java.util.concurrent.ConcurrentHashMap.newKeySet();

        // 3. Lectura física del Excel (I/O)
        try (FileInputStream fis = new FileInputStream(file);
             org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row != null) filasAProcesar.add(row);
            }
        } catch (Exception e) {
            throw new Exception("ERROR AL LEER EL ARCHIVO: " + e.getMessage());
        }

        // 4. Procesamiento PARALELO (Aquí ocurre la magia a máxima velocidad)
        filasAProcesar.parallelStream().forEach(row -> {
            int numeroFila = row.getRowNum() + 1;

            try {
                String codigo = getCellValue(row, 0);
                String nombre = getCellValue(row, 1);
                String unidad = getCellValue(row, 2);

                if (codigo.isEmpty() || nombre.isEmpty() || unidad.isEmpty()) {
                    errores.add("Fila " + numeroFila + ": datos incompletos, omitida.");
                    return;
                }

                // SIMULACIÓN DE CARGA
                try { Thread.sleep(1); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

                // Verificación ultrarrápida: revisa si existe en BD o si está duplicado en el archivo
                // Como NO hay un bloque synchronized gigante, los hilos fluyen libremente
                if (codigosExistentes.contains(codigo) || !codigosEnEsteExcel.add(codigo)) {
                    errores.add("Fila " + numeroFila + ": Tipo de instrumento ya existe (" + codigo + ").");
                    return;
                }

                TipoInstrumento t = new TipoInstrumento();
                t.setCodigo(codigo);
                t.setNombre(nombre);
                t.setUnidad(unidad);

                validosParaInsertar.add(t);

            } catch (Exception ex) {
                errores.add("Fila " + numeroFila + ": " + ex.getMessage());
            }
        });

        // 5. Inserción Masiva (Batch)
        // Ya validamos todo, así que podemos inyectarlos directamente en la lista subyacente
        // de una sola vez, en lugar de llamar Service.create() 10,000 veces.
        if (!validosParaInsertar.isEmpty()) {
            Service.instance().getTipos().addAll(validosParaInsertar);
        }

        // 6. Actualización de la Vista
        model.setList(Service.instance().search(new TipoInstrumento()));
        model.setCurrent(new TipoInstrumento());
        model.setMode(1);
        model.commit();

        long fin = System.currentTimeMillis();
        long duracion = fin - inicio;
        System.out.println("Fin de carga: " + new java.util.Date(fin));
        System.out.println("Duración total: " + duracion + " ms");

        StringBuilder msg = new StringBuilder();
        msg.append(validosParaInsertar.size()).append(" tipo(s) creado(s) exitosamente.");
        msg.append("\n\nTiempo de carga: " + duracion + " ms");
        if (!errores.isEmpty()) {
            msg.append("\n\nHubo ").append(errores.size()).append(" advertencias (ver consola para detalles).");
            // Puedes imprimir los errores a la consola en vez de poner 10,000 en el popup
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

    public void limpiarBaseDeDatosTemporal() throws Exception {
        // Vaciar la lista en el Service
        Service.instance().getTipos().clear();

        // Actualizar la vista
        model.setList(Service.instance().search(new TipoInstrumento()));
        model.setCurrent(new TipoInstrumento());
        model.setMode(1);
        model.commit();

        // Forzar guardado en XML (opcional, dependiendo de cuándo llames a stop())
        Service.instance().stop();

        throw new Exception("Base de datos de Tipos limpiada para pruebas.");
    }
}


