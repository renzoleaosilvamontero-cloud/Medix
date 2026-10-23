package com.mycompany.medix;
import com.mycompany.medix.domain.*;
import com.mycompany.medix.repository.MedixRepository;
import com.mycompany.medix.service.*;
import com.mycompany.medix.ui.CalendarPicker;
import java.awt.*;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MedixApplication {
  private final MedixRepository repo=new MedixRepository();
  private final AuthService auth=new AuthService(repo);
  private final AppointmentService appointments=new AppointmentService(repo);
  public static void main(String[] args){SwingUtilities.invokeLater(()->new MedixApplication().login());}
  private void login(){
    JTextField user=new JTextField(18);JPasswordField password=new JPasswordField(18);JCheckBox show=new JCheckBox("Mostrar contraseña");
    JPanel p=new JPanel(new GridLayout(0,1,5,5));p.add(new JLabel("Usuario"));p.add(user);p.add(new JLabel("Contraseña"));p.add(password);p.add(show);
    show.addActionListener(e->password.setEchoChar(show.isSelected()?(char)0:'•'));
    while(true){int result=JOptionPane.showConfirmDialog(null,p,"Medix | Iniciar sesión",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);if(result!=JOptionPane.OK_OPTION)return;
      try{var session=auth.login(user.getText(),password.getPassword());if(session.isEmpty()){JOptionPane.showMessageDialog(null,"Usuario o contraseña incorrectos.");password.setText("");continue;}
        Session s=session.get();if(s.mustChangePassword()&&!changePassword(s)){return;}new MainWindow(s).setVisible(true);return;
      }catch(Exception ex){error(ex);return;}
    }
  }
  private boolean changePassword(Session s){
    JPasswordField old=new JPasswordField(),next=new JPasswordField(),confirm=new JPasswordField();JPanel p=new JPanel(new GridLayout(0,2,5,5));
    p.add(new JLabel("Contraseña actual"));p.add(old);p.add(new JLabel("Nueva contraseña (12 caracteres mínimo)"));p.add(next);p.add(new JLabel("Confirmar"));p.add(confirm);
    if(JOptionPane.showConfirmDialog(null,p,"Cambiar contraseña",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return false;
    if(!java.util.Arrays.equals(next.getPassword(),confirm.getPassword())){JOptionPane.showMessageDialog(null,"La confirmación no coincide.");return false;}
    try{auth.changePassword(s,old.getPassword(),next.getPassword());return true;}catch(Exception ex){error(ex);return false;}
  }
  private static void error(Exception e){JOptionPane.showMessageDialog(null,e.getMessage()==null?"Error inesperado":e.getMessage(),"Medix",JOptionPane.ERROR_MESSAGE);}
  private final class MainWindow extends JFrame {
    private final Session session; private final DefaultTableModel patientsModel=new DefaultTableModel(new Object[]{"ID","Nombre","Apellido","DNI","Teléfono","Correo","Dirección"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable patientTable=new JTable(patientsModel);
    MainWindow(Session s){super("Medix — "+s.username()+" ("+s.role()+")");session=s;setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(900,600);setLocationRelativeTo(null);
      JTabbedPane tabs=new JTabbedPane();if(s.role()!=Role.PATIENT)tabs.addTab("Pacientes",patientPanel());else tabs.addTab("Mi perfil",patientPanel());
      tabs.addTab(s.role()==Role.DOCTOR?"Mi agenda":"Citas",schedulePanel());
      if(s.role()!=Role.DOCTOR)tabs.addTab("Reservar cita",appointmentPanel());
      JButton logout=new JButton("Cerrar sesión");logout.addActionListener(e->{dispose();login();});add(tabs,BorderLayout.CENTER);add(logout,BorderLayout.SOUTH);refreshPatients();
    }
    private JPanel patientPanel(){
      JPanel panel=new JPanel(new BorderLayout(8,8));panel.add(new JScrollPane(patientTable),BorderLayout.CENTER);
      JPanel actions=new JPanel();JButton refresh=new JButton("Actualizar");refresh.addActionListener(e->refreshPatients());actions.add(refresh);
      if(session.role()==Role.CUSTOMER_SERVICE||session.role()==Role.MANAGER||session.role()==Role.PATIENT){JButton edit=new JButton("Editar datos");edit.addActionListener(e->editPatient());actions.add(edit);}
      if(session.role()==Role.MANAGER){
        JButton create=new JButton("Alta de paciente");create.addActionListener(e->createPatient());actions.add(create);
        JButton internal=new JButton("Alta de cuenta interna");internal.addActionListener(e->createInternalAccount());actions.add(internal);
      }
      panel.add(actions,BorderLayout.SOUTH);return panel;
    }
    private void refreshPatients(){try{patientsModel.setRowCount(0);for(Patient x:repo.patients(session))patientsModel.addRow(new Object[]{x.id(),x.firstName(),x.lastName(),x.dni(),x.phone(),x.email(),x.address()});}catch(Exception e){error(e);}}
    private void createInternalAccount(){
      JTextField username=new JTextField(),name=new JTextField();
      JComboBox<Role> role=new JComboBox<>(new Role[]{Role.CUSTOMER_SERVICE,Role.DOCTOR,Role.MANAGER});
      JPanel form=new JPanel(new GridLayout(0,2,5,5));addField(form,"Usuario",username);addField(form,"Nombre completo",name);addField(form,"Perfil",role);
      if(JOptionPane.showConfirmDialog(this,form,"Crear cuenta interna",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
      try{String temporary=repo.createInternalAccount(session,username.getText(),name.getText(),(Role)role.getSelectedItem());
        JOptionPane.showMessageDialog(this,"Cuenta creada. Entrega esta contraseña temporal ahora; no volverá a mostrarse.\\nUsuario: "+username.getText()+"\\nContraseña temporal: "+temporary,"Credencial temporal",JOptionPane.INFORMATION_MESSAGE);
      }catch(Exception ex){error(ex);}
    }
    private void createPatient(){
      JTextField username=new JTextField(),first=new JTextField(),last=new JTextField(),dni=new JTextField();
      JPanel form=new JPanel(new GridLayout(0,2,5,5));addField(form,"Usuario",username);addField(form,"Nombre",first);addField(form,"Apellido",last);addField(form,"DNI",dni);
      if(JOptionPane.showConfirmDialog(this,form,"Crear cuenta de paciente",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
      try{String temporary=repo.createPatientAccount(session,username.getText(),first.getText(),last.getText(),dni.getText());
        refreshPatients();JOptionPane.showMessageDialog(this,"Cuenta creada. Entrega esta contraseña temporal al paciente ahora; no volverá a mostrarse.\\nUsuario: "+username.getText()+"\\nContraseña temporal: "+temporary,"Credencial temporal",JOptionPane.INFORMATION_MESSAGE);
      }catch(Exception ex){error(ex);}
    }
    private void editPatient(){
      int row=patientTable.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Selecciona un paciente.");return;}
      long id=(long)patientsModel.getValueAt(row,0);String first=(String)patientsModel.getValueAt(row,1),last=(String)patientsModel.getValueAt(row,2),dni=(String)patientsModel.getValueAt(row,3);
      JTextField f=new JTextField(first),l=new JTextField(last),d=new JTextField(dni),phone=new JTextField(val(row,4)),email=new JTextField(val(row,5)),address=new JTextField(val(row,6));
      boolean locked=session.role()==Role.CUSTOMER_SERVICE;f.setEditable(!locked);l.setEditable(!locked);d.setEditable(!locked);
      JPanel p=new JPanel(new GridLayout(0,2,5,5));addField(p,"Nombre",f);addField(p,"Apellido",l);addField(p,"DNI",d);addField(p,"Teléfono",phone);addField(p,"Correo",email);addField(p,"Dirección",address);
      if(JOptionPane.showConfirmDialog(this,p,"Editar paciente",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
      try{repo.updatePatient(session,new Patient(id,f.getText(),l.getText(),d.getText(),phone.getText(),email.getText(),address.getText()));refreshPatients();}catch(Exception e){error(e);}
    }
    private String val(int row,int col){Object v=patientsModel.getValueAt(row,col);return v==null?"":v.toString();}
    private JPanel schedulePanel(){
      JPanel panel=new JPanel(new BorderLayout(8,8));
      DefaultTableModel model=new DefaultTableModel(new Object[]{"ID","Paciente","Doctor","Inicio","Duración","Estado"},0){public boolean isCellEditable(int r,int c){return false;}};
      JTable table=new JTable(model);panel.add(new JScrollPane(table),BorderLayout.CENTER);
      JButton refresh=new JButton("Actualizar");refresh.addActionListener(e->{try{model.setRowCount(0);for(AppointmentInfo a:repo.appointments(session))model.addRow(new Object[]{a.id(),a.patientName(),a.doctorName(),a.startsAt(),a.durationMinutes(),a.status()});}catch(Exception ex){error(ex);}});
      JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));actions.add(refresh);panel.add(actions,BorderLayout.SOUTH);refresh.doClick();return panel;
    }
    private JPanel appointmentPanel(){
      JPanel p=new JPanel(new GridLayout(0,2,8,8));p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
      JTextField date=new JTextField(LocalDate.now().plusDays(1).toString());date.setEditable(false);JButton choose=new JButton("Elegir fecha…");
      JComboBox<Doctor> doctor=new JComboBox<>();JComboBox<String> slot=new JComboBox<>();JComboBox<Patient> patient=new JComboBox<>();JSpinner duration=new JSpinner(new SpinnerNumberModel(30,5,240,5));
      p.add(new JLabel("Fecha"));JPanel dateBox=new JPanel(new BorderLayout());dateBox.add(date);dateBox.add(choose,BorderLayout.EAST);p.add(dateBox);
      p.add(new JLabel("Doctor disponible"));p.add(doctor);p.add(new JLabel("Hora libre"));p.add(slot);p.add(new JLabel("Duración (minutos)"));p.add(duration);
      if(session.role()!=Role.PATIENT){p.add(new JLabel("Paciente"));p.add(patient);try{for(Patient x:repo.patients(session))patient.addItem(x);}catch(Exception e){error(e);}}
      JButton book=new JButton("Reservar cita");p.add(new JLabel());p.add(book);
      Runnable refresh=()->{try{LocalDate d=LocalDate.parse(date.getText());doctor.removeAllItems();for(Doctor x:appointments.doctors(session,d))doctor.addItem(x);refreshSlots(doctor,slot,d);}catch(Exception ex){doctor.removeAllItems();slot.removeAllItems();}};
      choose.addActionListener(e->{LocalDate d=CalendarPicker.choose(this,LocalDate.parse(date.getText()));if(d!=null){date.setText(d.toString());refresh.run();}});
      doctor.addActionListener(e->{try{if(doctor.getSelectedItem() instanceof Doctor x)refreshSlots(doctor,slot,LocalDate.parse(date.getText()));}catch(Exception ex){error(ex);}});
      refresh.run();
      book.addActionListener(e->{try{Doctor d=(Doctor)doctor.getSelectedItem();if(d==null||slot.getSelectedItem()==null)throw new IllegalArgumentException("No hay horarios disponibles para la selección.");
        long pid=session.role()==Role.PATIENT?session.patientId():((Patient)patient.getSelectedItem()).id();
        appointments.book(session,pid,d.id(),LocalDateTime.of(LocalDate.parse(date.getText()),LocalTime.parse(slot.getSelectedItem().toString())),(Integer)duration.getValue());
        JOptionPane.showMessageDialog(this,"Cita reservada.");refresh.run();
      }catch(Exception ex){error(ex);}});
      return p;
    }
    private void refreshSlots(JComboBox<Doctor> doctors,JComboBox<String> slots,LocalDate date){slots.removeAllItems();try{Doctor d=(Doctor)doctors.getSelectedItem();if(d!=null)for(LocalTime t:appointments.slots(session,d.id(),date))slots.addItem(t.toString());if(slots.getItemCount()==0)slots.addItem("Sin disponibilidad");}catch(Exception ex){error(ex);}}
    private void addField(JPanel p,String label,JComponent field){p.add(new JLabel(label));p.add(field);}
  }
}