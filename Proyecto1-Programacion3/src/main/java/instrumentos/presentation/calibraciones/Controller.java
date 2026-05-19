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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    public void uploadFile(File file) throws Exception {
        long inicio = System.currentTimeMillis();
        System.out.println("Inicio de carga: " + new java.util.Date(inicio));

        if (file == null || !file.exists()) {
            throw new Exception("ARCHIVO NO VÁLIDO");
        }

        // 1. Colecciones Seguras
        List<Calibraciones> validosParaInsertar = java.util.Collections.synchronizedList(new ArrayList<>());
        List<String> errores = java.util.Collections.synchronizedList(new ArrayList<>());
        List<org.apache.poi.ss.usermodel.Row> filasAProcesar = new ArrayList<>();

        // 2. Crear estructuras de búsqueda RÁPIDA (O(1)) para no saturar los hilos
        // Extraemos las claves "serie|numero" que ya están en el sistema
        Set<String> clavesExistentes = Service.instance().getInstrumentos().stream()
                .flatMap(inst -> inst.getListCalibracion().stream()
                        .map(c -> inst.getSerie() + "|" + c.getNumero()))
                .collect(Collectors.toSet());

        // Set concurrente para evitar que el mismo Excel traiga 2 números iguales para el mismo instrumento
        Set<String> clavesEnEsteExcel = java.util.concurrent.ConcurrentHashMap.newKeySet();

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
                String numero      = getCellValue(row, 0);
                String mediciones  = getCellValue(row, 1);
                String fecha       = getCellValue(row, 2);
                String instrumento = getCellValue(row, 3);

                if (numero.isEmpty() || mediciones.isEmpty() ||
                        fecha.isEmpty()  || instrumento.isEmpty()) {
                    errores.add("Fila " + numeroFila + ": datos incompletos, omitida.");
                    return;
                }

                // SIMULACIÓN DE CARGA
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

                // Verificación ultrarrápida: revisa si existe en BD o si está duplicado en el archivo
                // Como NO hay un bloque synchronized gigante, los hilos fluyen libremente
                String clave = instrumento + "|" + numero;
                if (clavesExistentes.contains(clave) || !clavesEnEsteExcel.add(clave)) {
                    errores.add("Fila " + numeroFila + ": calibración '" + numero + "' ya existe para el instrumento '" + instrumento + "'.");
                    return;
                }

                Calibraciones nueva = new Calibraciones();
                nueva.setNumero(numero);
                nueva.setMediciones(Integer.parseInt(mediciones));
                nueva.setFecha(fecha);
                nueva.setInstrumento(inst);

                validosParaInsertar.add(nueva);

            } catch (Exception ex) {
                errores.add("Fila " + numeroFila + ": " + ex.getMessage());
            }
        });

        // 5. Inserción Masiva (Batch)
        // Ya validamos todo, así que podemos inyectarlos directamente en la lista subyacente
        // de una sola vez, en lugar de llamar Service.create() 600 veces.
        if (!validosParaInsertar.isEmpty()) {
            for (Calibraciones c : validosParaInsertar) {
                c.getInstrumento().getListCalibracion().add(c);
            }
        }

        // 6. Actualización de la Vista
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

        StringBuilder msg = new StringBuilder();
        msg.append(validosParaInsertar.size()).append(" calibración(es) creada(s) exitosamente.");
        msg.append("\n\nTiempo de carga: ").append(duracion).append(" ms");
        if (!errores.isEmpty()) {
            msg.append("\n\nHubo ").append(errores.size()).append(" advertencias (ver consola para detalles).");
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

    /**
     * Simula carga de CPU real distribuida entre múltiples threads (paralelización).
     * Cada thread realiza trabajo de SHA-256 repetido para consumir ciclos de CPU.
     *
     * IMPORTANTE: El trabajo se DISTRIBUYE entre los threads, no se replica.
     * - Versión secuencial: 1 thread × 10 segundos = 10 segundos reales
     * - Versión paralela: 4 threads × (10/4 segundos cada uno) = ~2.5 segundos reales
     */
    private void simulateCpuWorkParallel() {
        final long TOTAL_WORK_MS = 10000L; // 10 segundos de TRABAJO TOTAL
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        long workPerThread = TOTAL_WORK_MS / numThreads; // Distribuir equitativamente
        long remainderMs = TOTAL_WORK_MS % numThreads;   // Asignar residuo al primer thread

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        System.out.println("[CPU PARALLEL] Iniciando simulación con " + numThreads + " threads...");
        System.out.println("[CPU PARALLEL] Trabajo total: " + TOTAL_WORK_MS + " ms distribuido en " + workPerThread + " ms por thread");
        long startTime = System.currentTimeMillis();

        // Lanzar trabajo CPU en cada thread
        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int id = threadId;
            // El primer thread trabaja más si hay residuo
            final long workMs = workPerThread + (threadId == 0 ? remainderMs : 0);

            executor.submit(() -> {
                try {
                    long targetNs = System.nanoTime() + workMs * 1_000_000L;
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    byte[] data = ("worker-" + id + "-" + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
                    long counter = 0;
                    int checksum = 0;

                    // Ejecutar trabajo CPU distribuido
                    while (System.nanoTime() < targetNs) {
                        md.update(data);
                        md.update(Long.toString(counter++).getBytes(StandardCharsets.UTF_8));
                        data = md.digest();
                        checksum += (data[0] & 0xff);
                    }

                    // Evitar que el compilador optimice el bucle
                    if (checksum == Integer.MIN_VALUE) {
                        System.out.println("[Worker " + id + "] Checksum: " + checksum);
                    }

                } catch (NoSuchAlgorithmException e) {
                    // Fallback: trabajo CPU basado en operaciones bitwise
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

        // Esperar a que todos los threads terminen
        executor.shutdown();
        try {
            // El timeout debe ser ligeramente mayor que workPerThread (no TOTAL_WORK_MS)
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
}
