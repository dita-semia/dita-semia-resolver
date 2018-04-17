package org.DitaSemia.Oxygen.AuthorOperations;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.DitaSemia.Oxygen.ExtensionBundle.DitaSemiaStylesFilter;
import org.DitaSemia.Oxygen.ExtensionBundle.DitaSemiaExtensionBundle;
import org.DitaSemia.Oxygen.ExtensionBundle.DitaSemiaReferenceResolver;
import org.DitaSemia.Oxygen.ExtensionBundle.DitaSemiaLinkTextResolver;
import org.DitaSemia.Oxygen.ExtensionBundle.DitaSemiaMapExtensionBundle;
import org.DitaSemia.Oxygen.ExtensionBundle.DitaSemiaMapReferenceResolver;
import org.DitaSemia.Oxygen.ExtensionBundle.DitaSemiaUniqueAttributesRecognizer;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class ProfilingOptionsDialog implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(ProfilingOptionsDialog.class.getName());
	
	private DitaSemiaExtensionBundle 			bundle				= null;
	private DitaSemiaReferenceResolver 			resolver			= null;
	private DitaSemiaLinkTextResolver 			linkTextResolver	= null;
	private DitaSemiaStylesFilter 				stylesFilter		= null;
	private DitaSemiaUniqueAttributesRecognizer recognizer			= null;
	private DitaSemiaMapExtensionBundle 		mapBundle			= null;
	private DitaSemiaMapReferenceResolver 		mapResolver			= null;
	private DitaSemiaLinkTextResolver 			mapLinkTextResolver	= null;
	private DitaSemiaStylesFilter 				mapStylesFilter		= null;
	private DitaSemiaUniqueAttributesRecognizer mapRecognizer		= null;
	
	private JDialog dialog 	= new JDialog();
	private JPanel 	main	= new JPanel();
	
	private static final String BUNDLE 					= "Extension Bundle";
	private static final String RESOLVER 				= "Reference Resolver";
	private static final String LINK_TEXT_RESOLVER 		= "Link Text Resolver";
	private static final String STYLES_FILTER 			= "Styles Filter";
	private static final String RECOGNIZER 				= "Unique Attributes Recognizer";
	private static final String MAP_BUNDLE 				= "Map Extension Bundle";
	private static final String MAP_RESOLVER 			= "Map Reference Resolver";
	private static final String MAP_LINK_TEXT_RESOLVER 	= "Map Link Text Resolver";
	private static final String MAP_STYLES_FILTER 		= "Map Styles Filter";
	private static final String MAP_RECOGNIZER 			= "Map Unique Attributes Recognizer";
	
	private static final String[] TEXTS = {BUNDLE, RESOLVER, LINK_TEXT_RESOLVER, STYLES_FILTER, RECOGNIZER, MAP_BUNDLE, MAP_RESOLVER, MAP_LINK_TEXT_RESOLVER, MAP_STYLES_FILTER, MAP_RECOGNIZER};
	
	private JToggleButton bundleButton 				= new JToggleButton(BUNDLE + " active");
	private JToggleButton resolverButton 			= new JToggleButton(RESOLVER + " active");
	private JToggleButton linkTextResolverButton 	= new JToggleButton(LINK_TEXT_RESOLVER + " active");
	private JToggleButton stylesButton 				= new JToggleButton(STYLES_FILTER + " active");
	private JToggleButton recognizerButton 			= new JToggleButton(RECOGNIZER + " active");
	private JToggleButton mapBundleButton			= new JToggleButton(MAP_BUNDLE + " active");
	private JToggleButton mapResolverButton 		= new JToggleButton(MAP_RESOLVER + " active");
	private JToggleButton mapLinkTextResolverButton = new JToggleButton(MAP_LINK_TEXT_RESOLVER + " active");
	private JToggleButton mapStylesButton 			= new JToggleButton(MAP_STYLES_FILTER + " active");
	private JToggleButton mapRecognizerButton 		= new JToggleButton(MAP_RECOGNIZER + " active");
	
	@Override
	public String getDescription() {
		return "Opens a dialog to enable and disable dita semia functions";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap)
			throws IllegalArgumentException, AuthorOperationException {
		ProfilingOptionsDialog dialog = DitaSemiaExtensionBundle.getDialog();
		dialog.initDialog();
		dialog.start();
	}
	
	private void start() {
		dialog.setVisible(true);
	}

	private void initDialog() {
		main.setLayout(new GridLayout(2, 5));
		
		initButtons();
		
		main.add(bundleButton);
		main.add(resolverButton);
		main.add(linkTextResolverButton);
		main.add(stylesButton);
		main.add(recognizerButton);
		main.add(mapBundleButton);
		main.add(mapResolverButton);
		main.add(mapLinkTextResolverButton);
		main.add(mapStylesButton);
		main.add(mapRecognizerButton);
		
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		WindowListener exitListener = new WindowAdapter() {

		     @Override
		     public void windowClosing(WindowEvent e) {
	    		 if (bundle != null) {
	    			 bundle.setActive(true);
	    		 }
	    		 if (resolver != null) {
	    			 resolver.setActive(true);
	    		 }
		    	 if (linkTextResolver != null) {
		    		 linkTextResolver.setActive(true);
		    	 }
		    	 if (stylesFilter != null) {
		    		 stylesFilter.setActive(true);
		    	 }
		    	 if (recognizer != null) {
		    		 recognizer.setActive(true);
		    	 }
		    	 if (mapBundle != null) {
		    		 mapBundle.setActive(true);
		    	 }
		    	 if (mapResolver != null) {
		    		 mapResolver.setActive(true);
		    	 }
		    	 if (mapLinkTextResolver != null) {
		    		 mapLinkTextResolver.setActive(true);
		    	 }
		    	 if (mapStylesFilter != null) {
		    		 mapStylesFilter.setActive(true);
		    	 }
		    	 if (mapRecognizer != null) {
		    		 mapRecognizer.setActive(true);
		    	 }
		    	 dialog.dispose();
		     }
		 };
		 dialog.addWindowListener(exitListener);

		 dialog.add(main);
		 dialog.setSize(new Dimension(1200, 140));
		 dialog.setResizable(false);
		 dialog.setLocationRelativeTo(null);
	}

	private void initButtons() {
		ActionListener l = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractButton abstractButton = (AbstractButton) e.getSource();
			    toggle(abstractButton);
			}
		};
		if (bundle != null) {
			bundle.setActive(true);
			bundleButton.setEnabled(true);
			bundleButton.setSelected(true);
			bundleButton.addActionListener(l);
		} else {
			bundleButton.setEnabled(false);
			bundleButton.setText(BUNDLE + " inactive");
		}
		if (resolver != null) {
			resolver.setActive(true);
			resolverButton.setSelected(true);
			resolverButton.setEnabled(true);
			resolverButton.addActionListener(l);
		} else {
			resolverButton.setEnabled(false);
			resolverButton.setText(RESOLVER + " inactive");
		}
		if (linkTextResolver != null) {
			linkTextResolver.setActive(true);
			linkTextResolverButton.setEnabled(true);
			linkTextResolverButton.setSelected(true);
			linkTextResolverButton.addActionListener(l);
		} else {
			linkTextResolverButton.setEnabled(false);
			linkTextResolverButton.setText(LINK_TEXT_RESOLVER + " inactive");
		}
		if (stylesFilter != null) {
			stylesFilter.setActive(true);
			stylesButton.setSelected(true);
			stylesButton.setEnabled(true);
			stylesButton.addActionListener(l);
		} else {
			stylesButton.setEnabled(false);
			stylesButton.setText(STYLES_FILTER + " inactive");
		}
		if (recognizer != null) {
			recognizer.setActive(true);
			recognizerButton.setSelected(true);
			recognizerButton.setEnabled(true);
			recognizerButton.addActionListener(l);
		} else {
			recognizerButton.setEnabled(false);
			recognizerButton.setText(RECOGNIZER + " inactive");
		}
		if (mapBundle != null) {
			mapBundle.setActive(true);
			mapBundleButton.setSelected(true);
			mapBundleButton.setEnabled(true);
			mapBundleButton.addActionListener(l);
		} else {
			mapBundleButton.setEnabled(false);
			mapBundleButton.setText(MAP_BUNDLE + " inactive");
		}
		if (mapResolver != null) {
			mapResolver.setActive(true);
			mapResolverButton.setSelected(true);
			mapResolverButton.setEnabled(true);
			mapResolverButton.addActionListener(l);
		} else {
			mapResolverButton.setEnabled(false);
			mapResolverButton.setText(MAP_RESOLVER + " inactive");
		}
		if (mapLinkTextResolver != null) {
			mapLinkTextResolver.setActive(true);
			mapLinkTextResolverButton.setSelected(true);
			mapLinkTextResolverButton.setEnabled(true);
			mapLinkTextResolverButton.addActionListener(l);
		} else {
			mapLinkTextResolverButton.setEnabled(false);
			mapLinkTextResolverButton.setText(MAP_LINK_TEXT_RESOLVER + " inactive");
		}
		if (mapStylesFilter != null) {
			mapStylesFilter.setActive(true);
			mapStylesButton.setSelected(true);
			mapStylesButton.setEnabled(true);
			mapStylesButton.addActionListener(l);
		} else {
			mapStylesButton.setEnabled(false);
			mapStylesButton.setText(MAP_STYLES_FILTER + " inactive");
		}
		if (mapRecognizer != null) {
			mapRecognizer.setActive(true);
			mapRecognizerButton.setSelected(true);
			mapRecognizerButton.setEnabled(true);
			mapRecognizerButton.addActionListener(l);
		} else {
			mapRecognizerButton.setEnabled(false);
			mapRecognizerButton.setText(MAP_RECOGNIZER + " inactive");
		}
	}
	
	private void toggle(AbstractButton button) {
		String name = extractName(button.getText());
		if (button.isSelected()) {
			button.setText(name + " active");
			checkActivation(name, true);
		} else {
			button.setText(name + " inactive");
			checkActivation(name, false);
		}
	}
	
	private void checkActivation(String name, boolean isSelected) {
		switch (name) {
		case BUNDLE:
			bundle.setActive(isSelected);
			break;
		case RESOLVER:
			resolver.setActive(isSelected);
			break;
		case LINK_TEXT_RESOLVER:
			linkTextResolver.setActive(isSelected);
			break;
		case STYLES_FILTER:
			stylesFilter.setActive(isSelected);
			break;
		case RECOGNIZER:
			recognizer.setActive(isSelected);
			break;
		case MAP_BUNDLE:
			mapBundle.setActive(isSelected);
			break;
		case MAP_RESOLVER:
			mapResolver.setActive(isSelected);
			break;
		case MAP_LINK_TEXT_RESOLVER:
			mapLinkTextResolver.setActive(isSelected);
			break;
		case MAP_STYLES_FILTER:
			mapStylesFilter.setActive(isSelected);
			break;
		case MAP_RECOGNIZER:
			mapRecognizer.setActive(isSelected);
			break;
		}
	}
	
	private String extractName(String text) {
		for (String s : TEXTS) {
			if (text.startsWith(s)) {
				return s;
			}
		}
		return "";
	}
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

	public void setExtensionBundle(DitaSemiaExtensionBundle extensionBundle) {
		this.bundle = extensionBundle;
	}
	
	public void setReferenceResolver(DitaSemiaReferenceResolver referenceResolver) {
		this.resolver = referenceResolver;
	}
	
	public void setLinkTextResolver(DitaSemiaLinkTextResolver linkResolver) {
		this.linkTextResolver = linkResolver;
	}
	
	public void setStylesFilter(DitaSemiaStylesFilter filter) {
		this.stylesFilter = filter;
	}
	
	public void setUniqueAttributesRecognizer(DitaSemiaUniqueAttributesRecognizer attributesRecognizer) {
		this.recognizer = attributesRecognizer;
	}
	
	public void setMapExtensionBundle(DitaSemiaMapExtensionBundle mapBundle) {
		this.mapBundle = mapBundle;
	}

	public void setMapReferenceResolver(DitaSemiaMapReferenceResolver mapResolver) {
		this.mapResolver = mapResolver;
	}

	public void setMapLinkTextResolver(DitaSemiaLinkTextResolver mapLinkTextResolver) {
		this.mapLinkTextResolver = mapLinkTextResolver;
	}

	public void setMapStylesFilter(DitaSemiaStylesFilter mapStylesFilter) {
		this.mapStylesFilter = mapStylesFilter;
	}

	public void setMapUniqueAttributesRecognizer(DitaSemiaUniqueAttributesRecognizer mapRecognizer) {
		this.mapRecognizer = mapRecognizer;
	}
}
