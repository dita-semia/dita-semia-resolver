package org.DitaSemia.Base.AdvancedKeyref;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class KeyDefInformationDialog extends JDialog {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(KeyDefInformationDialog.class.getName());
	
	private KeyDef keyDef;
	
	public KeyDefInformationDialog(Frame parentFrame, KeyDef keyDef) {
		super(parentFrame);
	
		this.keyDef = keyDef;
		init();
	}
	
	public void showDialog() {
		setVisible(true);
	}
	
	public void init() {
		setTitle("Key Information");
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JPanel mainPanel = new JPanel();
		JPanel panel = new JPanel();
		
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets		= new Insets(2, 3, 2, 3);
		constraints.gridx 		= 1;
		constraints.gridy		= 0;
		constraints.weightx 	= 1;
		constraints.weighty 	= 0;
		constraints.gridwidth 	= 2;
		constraints.fill 		= GridBagConstraints.BOTH;
		constraints.anchor 		= GridBagConstraints.NORTHWEST;	
		
		JPanel labelPanel 	= new JPanel(new GridBagLayout());
		labelPanel.setBorder(BorderFactory.createEmptyBorder());
		
		Action cancelAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};

		labelPanel.getActionMap().put("cancel", cancelAction);		
		labelPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		labelPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "cancel");
		
		GridBagConstraints labelConstr = new GridBagConstraints();
        labelConstr.gridx 			= 0;
        labelConstr.gridy 			= 0;
        labelConstr.weightx 		= 1;
        labelConstr.weighty 		= 0;
        labelConstr.gridwidth 		= 1;
        labelConstr.fill 			= GridBagConstraints.BOTH;
        labelConstr.anchor 			= GridBagConstraints.NORTHWEST;
        
		JTextField key 				= addLabelFieldPair("Key", 					labelConstr, labelPanel);
        JTextField type 			= addLabelFieldPair("Type", 				labelConstr, labelPanel);
        JTextField namespace 		= addLabelFieldPair("Namespace",			labelConstr, labelPanel);
        JTextField name 			= addLabelFieldPair("Name",					labelConstr, labelPanel);
        JTextField desc				= addLabelFieldPair("Description", 			labelConstr, labelPanel);
        JTextField defId			= addLabelFieldPair("Def-ID", 				labelConstr, labelPanel);
        JTextField defUrl			= addLabelFieldPair("Def-URL", 				labelConstr, labelPanel);
        JTextField ref				= addLabelFieldPair("Ref", 					labelConstr, labelPanel);
        JTextField flags			= addLabelFieldPair("Flags", 				labelConstr, labelPanel);
        JTextField filterProperties	= addLabelFieldPair("Filter Properties", 	labelConstr, labelPanel);
        
        if (keyDef != null) {
	        key.setText(keyDef.getKey());
	    	type.setText(keyDef.getType());
	    	namespace.setText(keyDef.getNamespace());
	    	name.setText(keyDef.getName());
	    	desc.setText(keyDef.getDesc());
	    	defId.setText(keyDef.getDefId());
	    	defUrl.setText(keyDef.getDefUrl().toExternalForm());
	    	ref.setText(keyDef.getRefString());
	    	flags.setText(keyDef.getFlags());
	    	filterProperties.setText(keyDef.getFilterProperties().toString());
        } else {
        	key.setText("-");
	    	type.setText("-");
	    	namespace.setText("-");
	    	name.setText("-");
	    	desc.setText("-");
	    	defId.setText("-");
	    	defUrl.setText("-");
	    	ref.setText("-");
	    	flags.setText("-");
	    	filterProperties.setText("-");
        }
        
        mainPanel.add(labelPanel, constraints);
        
        JPanel buttonPanel = new JPanel();
        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
        buttonPanel.add(ok);
        
        constraints.gridy		= 1;
        constraints.gridwidth 	= 2;
        constraints.anchor		= GridBagConstraints.CENTER;
		
		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainPanel.setBorder(BorderFactory.createEmptyBorder());
        
		setResizable(true);
		scrollPane.setPreferredSize(new Dimension(800, 230));
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(scrollPane);
        panel.add(buttonPanel);
        add(panel);
        pack();
//		setSize(800, 310);
	}
	
	private JTextField addLabelFieldPair(String labelText, GridBagConstraints constr, JPanel mainPanel) {
		JLabel textLabel = new JLabel(labelText);
        constr.gridy++;
		constr.gridx 	= 0;
        constr.weightx 	= 1;
        constr.insets.right = 5;
        mainPanel.add(textLabel, constr);
        
        JTextField field = new JTextField();
        field.setEditable(false);
        constr.gridx++;
        constr.weightx 		= 20;
        constr.insets.right = 0;
        constr.gridwidth 	= 3;
        mainPanel.add(field, constr);
        
        return field;
	}
}
