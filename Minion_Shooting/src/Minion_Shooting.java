import java.awt.*;
import java.awt.event.*;

class Minion_Shooting {
	public static void main(String[] args) {
		System.out.println("FLY MINION Start!");
		Minion_Shooting_frame msf=new Minion_Shooting_frame();
	}
}

class MyWindowAdapter extends WindowAdapter {
	MyWindowAdapter(){ }
	
	public void windowClosing(WindowEvent e) {
		Window wnd = e.getWindow();
		wnd.setVisible(false);
		wnd.dispose();
		System.exit(0);
	}
}
