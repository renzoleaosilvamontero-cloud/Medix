package com.mycompany.medix.ui;
import java.awt.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.*;
public final class CalendarPicker {
  private CalendarPicker(){}
  public static LocalDate choose(Component parent,LocalDate initial){
    final LocalDate[] selected={null}; final YearMonth[] month={YearMonth.from(initial)};
    JDialog dlg=new JDialog(SwingUtilities.getWindowAncestor(parent),"Seleccionar fecha",Dialog.ModalityType.APPLICATION_MODAL);
    JPanel root=new JPanel(new BorderLayout(8,8)); JPanel header=new JPanel(new BorderLayout());
    JButton prev=new JButton("‹"),next=new JButton("›"); JLabel title=new JLabel("",SwingConstants.CENTER);
    header.add(prev,BorderLayout.WEST);header.add(title,BorderLayout.CENTER);header.add(next,BorderLayout.EAST);
    JPanel days=new JPanel(new GridLayout(0,7,3,3));
    Runnable render=()->{title.setText(month[0].getMonth().getDisplayName(TextStyle.FULL,new Locale("es"))+" "+month[0].getYear());days.removeAll();
      for(String w:new String[]{"L","M","X","J","V","S","D"})days.add(new JLabel(w,SwingConstants.CENTER));
      int offset=month[0].atDay(1).getDayOfWeek().getValue()-1;
      for(int i=0;i<offset;i++)days.add(new JLabel(""));
      for(int day=1;day<=month[0].lengthOfMonth();day++){LocalDate d=month[0].atDay(day);JButton b=new JButton(Integer.toString(day));b.setEnabled(!d.isBefore(LocalDate.now()));b.addActionListener(e->{selected[0]=d;dlg.dispose();});days.add(b);}
      days.revalidate();days.repaint();};
    prev.addActionListener(e->{month[0]=month[0].minusMonths(1);render.run();});next.addActionListener(e->{month[0]=month[0].plusMonths(1);render.run();});
    root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));root.add(header,BorderLayout.NORTH);root.add(days,BorderLayout.CENTER);render.run();
    dlg.setContentPane(root);dlg.pack();dlg.setLocationRelativeTo(parent);dlg.setVisible(true);return selected[0];
  }
}