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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Controller{
    private static final long SEARCH_SIMULATION_MS = 10000L;
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


    public void delete (Instrumento filter) throws Exception {
        filter = model.getCurrent();
            if(filter.getListCalibracion().isEmpty()){
            List<Instrumento> nuevaL = Service.instance().delete(filter);
            model.setList(nuevaL);
            model.setCurrent(new Instrumento());
            model.setMode(1);
            model.commit();
            }
            else{
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
    private void simulateCpuWorkParallel() {
        final long TOTAL_WORK_MS = 10000L;
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        long workPerThread = TOTAL_WORK_MS / numThreads;
        long remainderMs = TOTAL_WORK_MS % numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        System.out.println("[CPU PARALLEL] Iniciando simulación con " + numThreads + " threads...");
        System.out.println("[CPU PARALLEL] Trabajo total: " + TOTAL_WORK_MS + " ms distribuido en " + workPerThread + " ms por thread");
        long startTime = System.currentTimeMillis();

        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int id = threadId;
            final long workMs = workPerThread + (threadId == 0 ? remainderMs : 0);

            executor.submit(() -> {
                try {
                    long targetNs = System.nanoTime() + workMs * 1_000_000L;
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    byte[] data = ("worker-" + id + "-" + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
                    long counter = 0;
                    int checksum = 0;

                    while (System.nanoTime() < targetNs) {
                        md.update(data);
                        md.update(Long.toString(counter++).getBytes(StandardCharsets.UTF_8));
                        data = md.digest();
                        checksum += (data[0] & 0xff);
                    }

                    if (checksum == Integer.MIN_VALUE) {
                        System.out.println("[Worker " + id + "] Checksum: " + checksum);
                    }

                } catch (NoSuchAlgorithmException e) {
                    long busyUntil = System.nanoTime() + workMs * 1_000_000L;
                    long counter = 0;
                    while (System.nanoTime() < busyUntil) {
                        counter += (counter << 1) ^ 0x9e3779b97f4a7c15L;
                    }
                    if (counter == Long.MIN_VALUE) {
                        System.out.println("[Worker " + Thread.currentThread().getId() + "] Counter: " + counter);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        executor.shutdown();
        try {
            if (!latch.await(workPerThread + 2000, TimeUnit.MILLISECONDS)) {
                System.out.println("[CPU PARALLEL] Advertencia: Algunos threads no terminaron a tiempo");
            }
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("[CPU PARALLEL] Simulación completada en " + elapsed + " ms (Aceleración teórica: " + (TOTAL_WORK_MS / Math.max(1, elapsed)) + "x)");
    }

// ─── SEARCH PARALELO ────────────────────────────────────────────────────────

    public void search(Instrumento filter) throws Exception {
        long inicio = System.currentTimeMillis();
        System.out.println("Inicio de búsqueda: " + new java.util.Date(inicio));

        // Simulación CPU paralela en lugar de serial
        simulateCpuWorkParallel();

        List<Instrumento> rows = Service.instance().search(filter);
        if (rows.isEmpty()) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
        model.setList(rows);
        model.setCurrent(new Instrumento());
        model.setMode(1);
        model.commit();

        long fin = System.currentTimeMillis();
        long duracion = fin - inicio;
        System.out.println("Fin de búsqueda: " + new java.util.Date(fin));
        System.out.println("Duración total: " + duracion + " ms");

        throw new Exception(rows.size() + " registro(s) encontrado(s).\n\nTiempo de búsqueda: " + duracion + " ms");
    }

// ─── UPLOAD PARALELO ─────────────────────────────────────────────────────────

    public void uploadFile(File file) throws Exception {
        long inicio = System.currentTimeMillis();
        System.out.println("Inicio de carga: " + new java.util.Date(inicio));

        if (file == null || !file.exists()) {
            throw new Exception("ARCHIVO NO VÁLIDO");
        }

        // 1. Colecciones seguras para acceso concurrente
        List<Instrumento> creados = java.util.Collections.synchronizedList(new ArrayList<>());
        List<String> errores   = java.util.Collections.synchronizedList(new ArrayList<>());
        List<org.apache.poi.ss.usermodel.Row> filasAProcesar = new ArrayList<>();

        // 2. Mapa de TipoInstrumento precargado para búsqueda O(1) sin saturar hilos
        //    Clave: código del tipo  →  Valor: objeto TipoInstrumento
        java.util.Map<String, TipoInstrumento> tiposMap = new java.util.concurrent.ConcurrentHashMap<>();
        Service.instance().getTipos().forEach(t -> tiposMap.put(t.getCodigo(), t));

        // Set concurrente para detectar series duplicadas dentro del mismo Excel
        java.util.Set<String> seriesEnEsteExcel = java.util.concurrent.ConcurrentHashMap.newKeySet();

        // 3. Lectura física del Excel (I/O) — sigue siendo serial (el archivo es un recurso único)
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

        // 4. Procesamiento PARALELO de filas
        filasAProcesar.parallelStream().forEach(row -> {
            int numeroFila = row.getRowNum() + 1;

            try {
                String serie       = getCellValue(row, 0);
                String tipo        = getCellValue(row, 1);
                String descripcion = getCellValue(row, 2);
                String minimo      = getCellValue(row, 3);
                String maximo      = getCellValue(row, 4);
                String tolerancia  = getCellValue(row, 5);

                if (serie.isEmpty() || tipo.isEmpty() || descripcion.isEmpty()
                        || minimo.isEmpty() || maximo.isEmpty() || tolerancia.isEmpty()) {
                    errores.add("Fila " + numeroFila + ": datos incompletos, omitida.");
                    return;
                }

                // Simulación de validación/consulta externa (1 ms)
                try { Thread.sleep(1); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

                // Búsqueda O(1) en el mapa precargado — sin bloquear otros hilos
                TipoInstrumento inst = tiposMap.get(tipo);
                if (inst == null) {
                    errores.add("Fila " + numeroFila + ": Tipo instrumento '" + tipo + "' no encontrado.");
                    return;
                }

                // Detectar serie duplicada dentro del mismo Excel
                if (!seriesEnEsteExcel.add(serie)) {
                    errores.add("Fila " + numeroFila + ": Serie duplicada en el archivo (" + serie + ").");
                    return;
                }

                Instrumento t = new Instrumento();
                t.setSerie(serie);
                t.setTipo(inst);
                t.setDescripcion(descripcion);
                t.setMinimo(Integer.parseInt(minimo));
                t.setMaximo(Integer.parseInt(maximo));
                t.setTolerancia(Integer.parseInt(tolerancia));

                creados.add(t);

            } catch (Exception ex) {
                errores.add("Fila " + numeroFila + ": " + ex.getMessage());
            }
        });

        // 5. Inserción masiva (batch) — una sola llamada en lugar de N llamadas individuales
        if (!creados.isEmpty()) {
            Service.instance().getInstrumentos().addAll(creados);
        }

        // 6. Actualización de la vista
        model.setList(Service.instance().search(new Instrumento()));
        model.setCurrent(new Instrumento());
        model.setMode(1);
        model.commit();

        long fin = System.currentTimeMillis();
        long duracion = fin - inicio;
        System.out.println("Fin de carga: " + new java.util.Date(fin));
        System.out.println("Duración total: " + duracion + " ms");

        StringBuilder msg = new StringBuilder();
        msg.append(creados.size()).append(" instrumento(s) creado(s) exitosamente.");
        msg.append("\n\nTiempo de carga: ").append(duracion).append(" ms");
        if (!errores.isEmpty()) {
            msg.append("\n\nHubo ").append(errores.size()).append(" advertencias (ver consola para detalles).");
            errores.forEach(e -> System.out.println("  • " + e));
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
