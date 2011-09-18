package info.svitkine.alexei.wage;

import java.awt.*;
import java.awt.event.ActionListener;

public class SaveDialog extends Dialog {
	public static final String NO_TEXT = "No";
	public static final String YES_TEXT = "Yes";
	public static final String CANCEL_TEXT = "Cancel";

	public SaveDialog(final ActionListener actionListener) {
		super(actionListener, "Save changes before closing?",
			new DialogButton[] {
				new DialogButton(NO_TEXT, new Rectangle(19, 67, 68, 28)),
				new DialogButton(YES_TEXT, new Rectangle(112, 67, 68, 28)),
				new DialogButton(CANCEL_TEXT, new Rectangle(205, 67, 68, 28))
		}, 1);
	}
}
