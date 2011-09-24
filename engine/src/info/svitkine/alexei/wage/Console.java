package info.svitkine.alexei.wage;

import java.awt.Font;
import java.io.InputStream;
import java.io.PrintStream;

public interface Console {
	public void postUpdateUI();

	public void clear();

	public InputStream getIn();

	public PrintStream getOut();

	public void setFont(Font font);

	public void setVisible(boolean b);
}
