package com.mycompany.paciente01;

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.security.SecureRandom;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Paciente01 extends JFrame {

    // ==============================
    // INICIO DE SESION Y CUENTAS
    // ==============================
    private JTextField txtUsuarioLogin;
    private JPasswordField txtClaveLogin;
    private JCheckBox chkMostrarLogin;

    // Prototipo: las cuentas se guardan en memoria mientras el programa está abierto.
    // Para producción, reemplazar por usuarios en MySQL y contraseñas con hash.
    private final Map<String, CuentaLocal> cuentas = new HashMap<>();

    private static class CuentaLocal {
        String contrasena;
        String rol;

        CuentaLocal(String contrasena, String rol) {
            this.contrasena = contrasena;
            this.rol = rol;
        }
    }


    // ==============================
    // CAMPOS DEL FORMULARIO
    // ==============================

    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtDni;
    private JTextField txtFecha;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtDireccion;

    private JComboBox<String> cmbSexo;

    private JPasswordField txtContrasena;
    private JPasswordField txtConfirmar;

    // checkbox para mostrar/ocultar contraseñas del formulario
    private JCheckBox chkMostrarContrasena;

    // checkbox para mostrar/ocultar contraseñas en la tabla
    private JCheckBox chkMostrarTabla;

    private final char ECHO_OCULTO = '\u2022'; // •
    private char echoContrasena = ECHO_OCULTO;
    private char echoConfirmar  = ECHO_OCULTO;

    // máscara para ocultar las contraseñas de la tabla
    private static final String MASCARA = "••••••••";

    // ==============================
    // TABLA
    // ==============================

    private JTable tabla;
    private DefaultTableModel modelo;

    // contador para el ID automático
    private int siguienteId = 1;

    // respaldo de las contraseñas reales (por índice de fila)
    private final ArrayList<String> contrasenas = new ArrayList<>();
    
    // respaldo de citas registradas (Opcional, para llevar registro)
    private final ArrayList<String> citasRegistradas = new ArrayList<>();

    // ==============================
    // BOTONES
    // ==============================
   

    private JButton btnRegistrar;
    private JButton btnLimpiar;
    private JButton btnEliminar;
    private JButton btnEditar;
    private JButton btnAgendarCita; // NUEVO BOTÓN
