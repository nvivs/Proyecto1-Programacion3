package instrumentos;

import instrumentos.logic.Service;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Application {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");}
        catch (Exception ex) {};

        window = new JFrame();
        window.setContentPane(new JTabbedPane());
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                Service.instance().stop();
            }
        });
//Tipos
        instrumentos.presentation.tipos.Model tiposModel= new instrumentos.presentation.tipos.Model();
        instrumentos.presentation.tipos.View tiposView = new instrumentos.presentation.tipos.View();
        tiposController = new instrumentos.presentation.tipos.Controller(tiposView,tiposModel);
//Instrumentos
        instrumentos.presentation.instrumentos.View InstrumentosView = new instrumentos.presentation.instrumentos.View();
        instrumentos.presentation.instrumentos.Model InstrumentosModel = new instrumentos.presentation.instrumentos.Model();
        InstrumentosController = new instrumentos.presentation.instrumentos.Controller(InstrumentosView, InstrumentosModel);
//Calibraciones
        instrumentos.presentation.calibraciones.View calibracionesView = new instrumentos.presentation.calibraciones.View();
        instrumentos.presentation.calibraciones.Model calibracionesModel = new instrumentos.presentation.calibraciones.Model();
        calibracionesController = new instrumentos.presentation.calibraciones.Controller(calibracionesModel, calibracionesView);
        calibracionesController.setController(InstrumentosController);

        window.getContentPane().add("Tipos de Instrumento",tiposView.getPanel());
        window.getContentPane().add("Instrumentos", InstrumentosView.getPanel());
        window.getContentPane().add("Calibraciones", calibracionesView.getPanel());

        window.setSize(900,450);
        window.setResizable(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setIconImage((new ImageIcon(Application.class.getResource("presentation/icons/icon.png"))).getImage());
        window.setTitle("SILAB: Sistema de Laboratorio Industrial");
        window.setVisible(true);
    }

    public static instrumentos.presentation.tipos.Controller tiposController;
    public static instrumentos.presentation.instrumentos.Controller InstrumentosController;
    public static instrumentos.presentation.calibraciones.Controller calibracionesController;
    public static int MODE_EDIT = 2;
    public static JFrame window;
}
