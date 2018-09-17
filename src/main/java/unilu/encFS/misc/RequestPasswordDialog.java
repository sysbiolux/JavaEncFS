package unilu.encFS.misc;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class RequestPasswordDialog {

	
	public static String requestConfirmedPassword(String title, String password)
	{
		if(password == null)
		{
			return requestConfirmedPassword(title);
		}
		else
		{
			return password;
		}
	}
	
	public static String requestConfirmedPassword(String title)
	{
		boolean passmatch = false;
		String password = null;
		String password2 = null;
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Enter a password:");
		JLabel label2 = new JLabel("Repeat the password:");
		JPasswordField pass = new JPasswordField(20);
		JPasswordField pass2 = new JPasswordField(20);
		panel.add(label);		
		panel.add(pass);
		panel.add(label2);		
		panel.add(pass2);
		String[] options = new String[]{"OK", "Cancel"};
		while(!passmatch)
		{
			int option = JOptionPane.showOptionDialog(null, panel, title,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
			if(option == 0) // pressing OK button
			{
				password = new String(pass.getPassword());
				password2 = new String(pass.getPassword());
				if (password.equals(password2))
				{
					passmatch = true;
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Passwords did not match. Try again", "Passwords don't match",JOptionPane.ERROR_MESSAGE);
				}
			}
			else
			{
				return null;
			}
		}
		return password;
	}
	
	public static String requestPassword(String title, String password)
	{
		if(password == null)
		{
			return requestPassword(title);
		}
		else
		{
			return password;
		}
	}
	
	public static String requestPassword(String title)
	{
		String password = null;		
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Enter the password:");		
		JPasswordField pass = new JPasswordField(20);
		panel.add(label);		
		panel.add(pass);
		String[] options = new String[]{"OK", "Cancel"};
		int option = JOptionPane.showOptionDialog(null, panel, title,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
		if(option == 0) // pressing OK button
		{
			password = new String(pass.getPassword());
		}
		else
		{
			return null;
		}
		return password;
	}
}
