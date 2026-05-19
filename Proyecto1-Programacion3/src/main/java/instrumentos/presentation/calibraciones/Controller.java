package instrumentos.presentation.calibraciones;

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
import instrumentos.logic.Instrumento;
import instrumentos.logic.Medida;
import instrumentos.logic.Service;
import instrumentos.logic.Calibraciones;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    View view;
    Model model;
    private Map<Instrumento, List<Calibraciones>> calibracionesInstrumento = new HashMap<>();

    public void setController(instrumentos.presentation.instrumentos.Controller controller) {
        this.controller = controller;
    }
    instrumentos.presentation.instrumentos.Controller controller;

    //------------------------------------------------------------------------------------------------------------------
    public Controller(Model model, View view) {
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
        this.controller = null;
    }

    //------------------------------------------------------------------------------------------------------------------
    public void search(Calibraciones filter) throws Exception {
        long inicio = System.currentTimeMillis();
        System.out.println("Inicio de búsqueda: " + new java.util.Date(inicio));

        // Simula carga CPU real distribuida entre múltiples hilos
        simulateCpuWorkParallel();

        filter.setInstrumento(controller.getCurrent());
        List<Calibraciones> rows = Service.instance().search(filter);
        if (rows.isEmpty()) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
        controller.setListaC(rows);
        model.setProps();
        model.setCurrent(new Calibraciones());
        model.setMode(1);
        model.commit();

        long fin = System.currentTimeMillis();
        long duracion = fin - inicio;
        System.out.println("Fin de búsqueda: " + new java.util.Date(fin));
        System.out.println("Duración total: " + duracion + " ms");

        throw new Exception(rows.size() + " registro(s) encontrado(s).\n\nTiempo de búsqueda: " + duracion + " ms");
    }

    //------------------------------------------------------------------------------------------------------------------
    public void uploadFile(File file) throws Exception {
        long inicio = System.currentTimeMillis();
        System.out.println("Inicio de carga: " + new java.util.Date(inicio));

        if (file == null || !file.exists()) {
            throw new Exception("ARCHIVO NO VÁLIDO");
        }

        // 1. Leer todas las filas del Excel en memoria (no thread-safe, se hace antes del paralelo)
        List<org.apache.poi.ss.usermodel.Row> filas = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row != null) filas.add(row);
            }
        } catch (Exception e) {
            throw new Exception("ERROR AL LEER EL ARCHIVO: " + e.getMessage());
        }

        // 2. Estructuras thread-safe para recolectar resultados
        List<Calibraciones> validosParaInsertar = new CopyOnWriteArrayList<>();
        List<String> errores = new CopyOnWriteArrayList<>();
        // Controla duplicados de número dentro del mismo Excel
        Set<String> numerosEnEsteExcel = ConcurrentHashMap.newKeySet();
        // Pre-cargar números ya existentes en todos los instrumentos para validación rápida
        Set<String> numerosExistentes = ConcurrentHashMap.newKeySet();
        for (Instrumento inst : Service.instance().getInstrumentos()) {
            for (Calibraciones c : inst.getListCalibracion()) {
                numerosExistentes.add(inst.getSerie() + "|" + c.getNumero());
            }
        }

        // 3. Paralelizar el procesamiento de filas
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(filas.size());

        System.out.println("[UPLOAD PARALLEL] Procesando " + filas.size() + " filas con " + numThreads + " threads...");

        for (int i = 0; i < filas.size(); i++) {
            final org.apache.poi.ss.usermodel.Row row = filas.get(i);
            final int numeroFila = i + 2; // +2 porque empieza en fila 2 (fila 1 es header)

            executor.submit(() -> {
                try {
                    String numero      = getCellValue(row, 0);
                    String mediciones  = getCellValue(row, 1);
                    String fecha       = getCellValue(row, 2);
                    String instrumento = getCellValue(row, 3);

                    // Validación de campos obligatorios
                    if (numero.isEmpty() || mediciones.isEmpty() ||
                            fecha.isEmpty()  || instrumento.isEmpty()) {
                        errores.add("Fila " + numeroFila + ": datos incompletos, omitida.");
                        return;
                    }

                    // Simula validación o consulta externa
                    try { Thread.sleep(1); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

                    // Buscar instrumento por serie
                    Instrumento inst;
                    try {
                        Instrumento filtro = new Instrumento();
                        filtro.setSerie(instrumento);
                        inst = Service.instance().read(filtro);
                    } catch (Exception ex) {
                        errores.add("Fila " + numeroFila + ": instrumento '" + instrumento + "' no encontrado.");
                        return;
                    }

                    // Verificar duplicado: ya existe en el sistema o ya está en este Excel
                    String clave = instrumento + "|" + numero;
                    if (numerosExistentes.contains(clave) || !numerosEnEsteExcel.add(clave)) {
                        errores.add("Fila " + numeroFila + ": calibración '" + numero + "' ya existe para el instrumento '" + instrumento + "'.");
                        return;
                    }

                    // Parseo y construcción del objeto
                    Calibraciones nueva = new Calibraciones();
                    nueva.setNumero(numero);
                    nueva.setMediciones(Integer.parseInt(mediciones));
                    nueva.setFecha(fecha);
                    nueva.setInstrumento(inst);

                    validosParaInsertar.add(nueva);

                } catch (NumberFormatException nfe) {
                    errores.add("Fila " + numeroFila + ": 'mediciones' debe ser un número entero válido.");
                } catch (Exception ex) {
                    errores.add("Fila " + numeroFila + ": " + ex.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 4. Esperar a que todos los threads terminen
        executor.shutdown();
        try {
            if (!latch.await(60, TimeUnit.SECONDS)) {
                System.out.println("[UPLOAD PARALLEL] Advertencia: algunos threads no terminaron a tiempo.");
            }
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 5. Inserción masiva (batch) — ya validado todo, se inserta directo en la lista
        if (!validosParaInsertar.isEmpty()) {
            for (Calibraciones c : validosParaInsertar) {
                c.getInstrumento().getListCalibracion().add(c);
            }
        }

        // 6. Refrescar vista del instrumento actualmente seleccionado
        if (!validosParaInsertar.isEmpty() && controller.getCurrent() != null
                && controller.getCurrent().getTipo() != null) {
            Instrumento actual = controller.getCurrent();
            List<Calibraciones> lista = actual.getListCalibracion();
            calibracionesInstrumento.put(actual, lista);
            controller.setListaC(lista);
            model.setProps();
            model.commit();
        }

        long fin = System.currentTimeMillis();
        long duracion = fin - inicio;
        System.out.println("Fin de carga: " + new java.util.Date(fin));
        System.out.println("Duración total: " + duracion + " ms");
        System.out.println("Creados: " + validosParaInsertar.size() + " | Errores: " + errores.size());
        errores.forEach(System.out::println);

        StringBuilder msg = new StringBuilder();
        msg.append(validosParaInsertar.size()).append(" calibración(es) creada(s) exitosamente.");
        msg.append("\n\nTiempo de carga: ").append(duracion).append(" ms");
        if (!errores.isEmpty()) {
            msg.append("\n\nHubo ").append(errores.size()).append(" advertencia(s) (ver consola para detalles).");
        }
        throw new Exception(msg.toString());
    }

    //------------------------------------------------------------------------------------------------------------------
    private String getCellValue(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(
                col, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    java.time.LocalDate ld = cell.getLocalDateTimeCellValue().toLocalDate();
                    return ld.toString();
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) return String.valueOf((long) d);
                return String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default:      return "";
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Simula carga de CPU real distribuida entre múltiples threads.
     * Cada thread realiza trabajo de SHA-256 repetido para consumir ciclos de CPU.
     * El trabajo se DISTRIBUYE entre los threads, no se replica:
     * - Secuencial:  1 thread  × 10 s       = 10 s reales
     * - Paralelo:    4 threads × (10/4 s c/u) = ~2.5 s reales
     */
    private void simulateCpuWorkParallel() {
        final long TOTAL_WORK_MS = 10000L;
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        long workPerThread = TOTAL_WORK_MS / numThreads;
        long remainderMs   = TOTAL_WORK_MS % numThreads;

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
        System.out.println("[CPU PARALLEL] Simulación completada en " + elapsed + " ms (Aceleración teórica: "
                + (TOTAL_WORK_MS / Math.max(1, elapsed)) + "x)");
    }

    //------------------------------------------------------------------------------------------------------------------
    public void delete(Calibraciones filter) throws Exception {
        try {
            filter = model.getCurrent();
            List<Calibraciones> nuevaL = Service.instance().delete(filter);
            model.setProps();
            model.setCurrent(new Calibraciones());
            controller.setListaC(nuevaL);
            model.setMode(1);
            model.commit();
        } catch (Exception e) {
            throw new Exception("NINGUN REGISTRO COINCIDE");
        }
    }

    public void edit(int row) {
        Calibraciones e = controller.getCurrent().getListCalibracion().get(row);
        try {
            model.setCurrent(Service.instance().read(e));
            model.setMode(2);
            model.commit();
        } catch (Exception ex) {}
    }

    public void clear() {
        model.setCurrent(new Calibraciones());
        model.getCurrent().setInstrumento(controller.getCurrent());
        model.setMode(1);
        model.commit();
    }

    public void save(Calibraciones filter) throws Exception {
        Calibraciones e = new Calibraciones();
        filter.setInstrumento(controller.getCurrent());
        e.setNumero(view.getNumero().getText());
        e.setMediciones(Integer.parseInt(view.getMediciones().getText()));
        e.setFecha(view.getFecha().getText());
        e.setInstrumento(model.getSelected());
        if (e.getInstrumento() == null) {
            throw new Exception("No tiene un instrumento seleccionado");
        }
        try {
            if (model.getMode() == 2) {
                Service.instance().update(e);
                List<Calibraciones> rows = Service.instance().adding(filter);
                calibracionesInstrumento.put(controller.getCurrent(), rows);
                model.setCurrent(e);
                controller.setListaC(rows);
                model.setProps();
                model.commit();
            } else if (model.getMode() == 1) {
                Service.instance().create(e);
                List<Calibraciones> rows = Service.instance().adding(filter);
                calibracionesInstrumento.put(controller.getCurrent(), rows);
                model.setCurrent(e);
                controller.setListaC(rows);
                model.setProps();
                model.setMode(2);
                model.commit();
            }
        } catch (Exception ex) {
            throw new Exception("DATOS INCOMPLETOS");
        }
    }

    public void crearNum() {
        if (controller.getCurrent().getTipo() != null) { model.crearNumeros(); }
    }

    //------------------------------------------------------------------------------------------------------------------
    public void setSelectedInstrumento() throws Exception {
        if (controller.getCurrent().getTipo() == null) {
            throw new Exception("No seleccionó ningún instrumento. No podrá agregar calibraciones");
        }
        try {
            Instrumento selected = controller.getCurrent();
            model.setSelected(selected);
            calibracionesInstrumento.putIfAbsent(selected, new ArrayList<>());
            model.commit();
        } catch (Exception e) {}
    }

    public List<Calibraciones> obtenerListaInstrumentos() {
        if (controller != null) {
            return controller.obtenerCalibraciones();
        } else {
            return Collections.emptyList();
        }
    }

    public void editarMedidas() {
        try {
            model.setMode(2);
            model.commit();
        } catch (Exception ex) {}
    }

    public void CreateMeasure() {
        model.getCurrent().CreateMedidas();
    }

    public List<Medida> obtenerListaMedidas() {
        return model.getCurrent().getMedidas();
    }

    public void setCurrent(Calibraciones e) { model.setCurrent(e); }

    public Instrumento getSelectedInstrumento() {
        return controller.getCurrent();
    }

    public void shown() {
        Instrumento selectedInstrumento = getSelectedInstrumento();
        if (selectedInstrumento.getTipo() == null) {
            controller.setListaC(Collections.emptyList());
            model.setProps();
        } else {
            model.setProps();
            List<Calibraciones> calibraciones = calibracionesInstrumento
                    .computeIfAbsent(selectedInstrumento, k -> new ArrayList<>());
            controller.setListaC(calibraciones);
        }
        model.commit();
    }

    //------------------------------------------------------------------------------------------------------------------
    private Cell getCeldaI(Image image, HorizontalAlignment horizontalAlignment, boolean border) {
        image.setMargins(0, 0, 0, 0);
        Cell cellI = new Cell().add(image);
        image.setHorizontalAlignment(horizontalAlignment);
        if (!border) cellI.setBorder(Border.NO_BORDER);
        return cellI;
    }

    private Cell getCeldaP(Paragraph paragraph, TextAlignment textAlignment, boolean border) {
        paragraph.setMargin(0);
        Cell cellP = new Cell().add(paragraph);
        cellP.setTextAlignment(textAlignment);
        if (!border) cellP.setBorder(Border.NO_BORDER);
        return cellP;
    }

    public void createDocument() throws Exception {
        try {
            String archivo = "Calibraciones.pdf";
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
            header.addCell(getCeldaP(new Paragraph(" "), TextAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph(" "), TextAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph("Calibraciones").setFont(font).setBold().setFontSize(20), TextAlignment.CENTER, false));
            header.addCell(getCeldaP(new Paragraph(" "), TextAlignment.LEFT, false));
            document.add(header);

            Table titulos = new Table(4);
            titulos.setWidth(530);
            titulos.setHorizontalAlignment(HorizontalAlignment.CENTER);
            titulos.addCell(getCeldaP(new Paragraph("Número").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("Fecha").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("Mediciones").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));
            titulos.addCell(getCeldaP(new Paragraph("№ Serie Instrumento").setBackgroundColor(ColorConstants.CYAN).setFontColor(ColorConstants.BLACK), TextAlignment.CENTER, true));

            for (Calibraciones c : controller.getCurrent().getListCalibracion()) {
                titulos.addCell(getCeldaP(new Paragraph(c.getNumero()).setBackgroundColor(ColorConstants.LIGHT_GRAY), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(c.getFecha()), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(String.valueOf(c.getMediciones())), TextAlignment.CENTER, true));
                titulos.addCell(getCeldaP(new Paragraph(c.getInstrumento().getSerie()), TextAlignment.CENTER, true));
            }

            document.add(titulos);
            document.close();
        } catch (Exception e) {
            throw new Exception("No se pudo crear el PDF");
        }
    }
}