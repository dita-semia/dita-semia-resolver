package org.DitaSemia.Oxygen.AdvancedKeyRef;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;

import org.DitaSemia.Base.AdvancedKeyref.AdvancedKeyrefDialog;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyDefListInterface;
import org.DitaSemia.Base.AdvancedKeyref.KeyPrioritizer;
import org.DitaSemia.Base.AdvancedKeyref.KeyRefInterface;
import org.apache.log4j.Logger;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;

@SuppressWarnings("serial")
public class OxyAdvancedKeyrefDialog extends AdvancedKeyrefDialog{
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OxyAdvancedKeyrefDialog.class.getName());
	
	private static final String PROPERTIES 	= "OxyAdvancedKeyrefDialog";
	private static final String SIZE		= "-size";
	private static final String POSITION	= "-position";
	private static final String COLWIDTH	= "-colwidth";
	
	public OxyAdvancedKeyrefDialog(Frame parentFrame, KeyDefListInterface keyDefList, KeyRefInterface contextKeyRef, KeyDefInterface contextKeyDef, KeyPrioritizer keyPrioritizer) {
		super(parentFrame, keyDefList, contextKeyRef, contextKeyDef, keyPrioritizer);
	}
	
	public Dimension stringToSize(String sizeString) {
		String[] size = sizeString.split(",");
		if (size.length == 2) {
			return new Dimension(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
		} else {
			return null;
		}
	}
	
	public Point stringToPosition(String positionString) {
		String[] position = positionString.split(",");
		if (position.length == 2) {
			return new Point(Integer.parseInt(position[0]), Integer.parseInt(position[1]));
		} else {
			return null;
		}
	}
	
	public int[] stringToColwidth(String colwidthString) {
		String[] colwidthStr = colwidthString.split(",");
		int[] colwidth = new int[5];
		if (colwidthStr.length == 5) {
			for (int i = 0; i < 5; i++) {
				if (i == 0) {
					colwidth[i] = 25;
				} else {
					colwidth[i] = Integer.parseInt(colwidthStr[i]);
				}
			}
			return colwidth;
		} else {
			return null;
		}
	}

	public boolean showDialog() {
		final WSOptionsStorage 	optionsStorage 		= PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
		Dimension size = stringToSize(optionsStorage.getOption(PROPERTIES + SIZE, ""));
		Point position = stringToPosition(optionsStorage.getOption(PROPERTIES + POSITION, ""));
		int[] colwidth = stringToColwidth(optionsStorage.getOption(PROPERTIES + COLWIDTH, ""));
		setProperties(size, position, colwidth);
		setVisible(true);
		saveState();
		return getResult();
	}
	
	private void saveState() {
		final WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();

		// Position und Größe des Dialogs
    	final Point 	position	= getLocation();
    	final Dimension size		= getSize();
    	final String	posString 	= String.valueOf(position.x) + "," + String.valueOf(position.y);
    	final String	sizeString	= String.valueOf(size.width) + "," + String.valueOf(size.height);
    	
//    	logger.info("saveState: " + size + ", " + position + ", " + getColWidth());
    	
    	optionsStorage.setOption(PROPERTIES + POSITION, posString);
    	optionsStorage.setOption(PROPERTIES + SIZE, sizeString);
    	optionsStorage.setOption(PROPERTIES + COLWIDTH, getColWidth());
	}

}
