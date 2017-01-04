package org.DitaSemia.Base.AdvancedKeyref;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;


@SuppressWarnings("serial")
public class AdvancedKeyrefDialog extends JDialog {
	
//	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AdvancedKeyrefDialog.class.getName());
	
	JPanel 											mainPanel				= new JPanel();
	JOptionPane 									optionPane;
	private KeyDefInterface							contextKeyDef;
	private KeyDefInterface							selectedKeyDef 			= null;
	private String									outputclass;
	private	JComboBox<String> 						outputclassComboBox;
	private boolean									accepted				= false;
	
	private JTable									keyTable				= null;
	private KeyDefTableModel 						keyDefTableModel 		= null;
	
	protected JTextField 							keyField				= null;
	protected JTextField 							nameField				= null;
	protected JTextField 							typeField				= null;
	protected JTextField 							namespaceField			= null;
	protected JTextField 							descField				= null;
	protected JTextField 							urlField				= null;
	protected JTextField 							defIdField				= null;

	protected KeyRefInterface 						currentKeyRef;
	private int 									fixedPathLen;
	
	protected JLabel 								contextKeyField			= null;
	protected JLabel 								contextTypeField		= null;
	protected JLabel 								contextNamespaceField	= null;
	protected JTextField							searchField				= null;
	
	private JButton									expand;
	private JButton									reduce;
	private int 									previewLength			= 1;
	private JEditorPane								previewTextField		= null;
	private List<String>							namespaceList			= new ArrayList<String>();
	private List<String>							currentNamespaceList	= null;
	private String									currentPreview			= "";
	private String									currentKey				= "";
	
	private KeyAdapter 								keyListener;
	
	private ArrayList<JComboBox<Object>>			comboBoxes;
	
	private TableRowSorter<KeyDefTableModel> 		sorter					= null;
	private Object[]								comboBoxFilters			= new Object[4];
	private RowFilter<KeyDefTableModel, Object> 	searchFieldFilter		= null;
	private ArrayList<RowFilter<KeyDefTableModel, Object>> 	filters;

	private boolean									boxAction				= false;
	
	private Frame									parentFrame;
	
	private KeyDefListInterface 					keyDefList;

	
	public AdvancedKeyrefDialog(Frame parentFrame, KeyDefListInterface keyDefList, KeyRefInterface currentKeyRef, KeyDefInterface contextKeyDef, KeyPrioritizer keyPrioritizer) {
		super(parentFrame, true);
		
		this.setTitle("Edit KeyRef-Element");
		
		this.parentFrame 	= parentFrame;
		this.keyDefList		= keyDefList;
		this.contextKeyDef 	= contextKeyDef;
				
		this.currentKeyRef	= currentKeyRef;
		this.fixedPathLen	= currentKeyRef.getFixedPathLen();
		this.outputclass	= currentKeyRef.getOutputclass();
		
		String[] options 	= {"OK", "Cancel"};
		optionPane 			= new JOptionPane(mainPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, null);

		keyDefTableModel 	= new KeyDefTableModel(keyDefList, currentKeyRef, keyPrioritizer);
		
		createDialog();
	}
	
	public boolean showDialog() {
		setVisible(true);
		return accepted;
	}
	
	public void setProperties(Dimension size, Point position, int[] colwidth) {
		if (size != null) {
			this.setPreferredSize(size);
		} else {
			this.setPreferredSize(new Dimension(700, 600));
		}
		pack();
		if (position != null) {
			this.setLocation(position);
		} else {
			this.setLocationRelativeTo(parentFrame);
		}
		int colCount = keyTable.getColumnCount();
		if (colwidth != null && colwidth.length == colCount) {
			if (colwidth.length == colCount) {
				for (int i = 0; i < colCount; ++i) {
					keyTable.getColumnModel().getColumn(i).setPreferredWidth(colwidth[i]);
				}
			}
		}
	}
	
	public void createDialog() {
		init();
		
		this.addComponentListener(new ComponentAdapter() {
			@Override
            public void componentShown(ComponentEvent ce) {
            	keyTable.requestFocus();
            	keyTable.changeSelection(0, 0, false, false);
            	setPreviewLength();
            	currentNamespaceList = createCurrentNamespaceList();
            	updatePreview();
            }
		});	
		
		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();
				
				if (	isVisible() 
						&& (e.getSource() == optionPane) 
						&& (JOptionPane.VALUE_PROPERTY.equals(prop) 
								|| JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
					
		            Object value = optionPane.getValue();
		            
		            if (value.equals("OK")) {
		            	doOk();    
		            } else if (value.equals("Cancel")) {
		            	cancel();
		            }
		        }
			}
		});
		
		keyTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
	            if (e.getClickCount() >= 2) {
	               	doOk();
	            } 
	        }
		});
		
        keyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent evt) {
				if (!evt.getValueIsAdjusting()) {
					if (keyTable.getSelectedRow() >= 0) {
						selectedKeyDef 		= keyDefTableModel.get(keyTable.convertRowIndexToModel(keyTable.getSelectedRow())).getKeyDef();
					}
					updateDetails();
					boxAction = false;
					updateComboBoxes();
					boxAction = true;
				}
			}
        });
        
        registerKeyBindings();
		
		this.add(optionPane);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	private void registerKeyBindings() {
		
		Action enterAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOk();
			}
		};
		
		Action cancelAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		};
		
		Action rowUp = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (keyTable.getSelectedRow() > 0) {
					try {
					keyTable.changeSelection(keyTable.getSelectedRow()-1, 0, false, false);
					} catch (Exception ex) {
						logger.error(ex, ex);
					}
				}
			}
		};
		
		Action rowDown = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (keyTable.getSelectedRow()+1 < keyDefTableModel.getRowCount()) {
					keyTable.changeSelection(keyTable.getSelectedRow()+1, 0, false, false);
				}
			}
		};
		
		Action expand = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fixedPathLen == 0 && previewLength <= namespaceList.size()) {
					expandNamespace();
				}
			}
		};
		
		Action reduce = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fixedPathLen == 0 && previewLength > 1) {
					reduceNamespace();
				}
			}
		};
		
		Action home = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				keyTable.changeSelection(0, 0, false, false);
			}
		};
		
		Action scrollUp = keyTable.getActionMap().get("scrollUpChangeSelection");
		Action scrollDown = keyTable.getActionMap().get("scrollDownChangeSelection");
		
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "enter");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "rowUp");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), "rowUp");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "rowDown");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), "rowDown");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "expand");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "expand");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "expand");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), "expand");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "reduce");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "reduce");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "reduce");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), "reduce");
		keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "home");
		
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "enter");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "rowUp");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), "rowUp");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "rowDown");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), "rowDown");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "expand");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "expand");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "expand");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), "expand");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "reduce");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "reduce");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "reduce");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), "reduce");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "scrollDown");
		keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "scrollUp");
		
//		logger.info("ohne alles: " + keyTable.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)));
//		logger.info("in focused window: " + keyTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)));
//		logger.info("ancestor of focused: " + keyTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)));
//		logger.info("focused: " + keyTable.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)));
		
		keyTable.getActionMap().put("enter", enterAction);
		keyTable.getActionMap().put("cancel", cancelAction);
		keyTable.getActionMap().put("rowUp", rowUp);
		keyTable.getActionMap().put("rowDown", rowDown);
		keyTable.getActionMap().put("expand", expand);
		keyTable.getActionMap().put("reduce", reduce);
		keyTable.getActionMap().put("home", home);
		keyTable.getActionMap().put("scrollUp", scrollUp);
		keyTable.getActionMap().put("scrollDown", scrollDown);
	}
	
	public boolean getResult() {
		return accepted;
	}
	
	private void doOk() {
		int selectedRow = keyTable.getSelectedRow();
    	if (selectedRow >= 0) {
        	KeyDefTableModelEntry keyDef = keyDefTableModel.get(keyTable.convertRowIndexToModel(selectedRow));
        	selectedKeyDef = keyDef.getKeyDef();
    	}
    	accepted = true;
    	dispose();
	}
	
	private void cancel() {
		dispose();
	}
	
	private void init() {
		filters 				= new ArrayList<RowFilter<KeyDefTableModel, Object>>(2);

		JPanel contextPanel		= new JPanel();
		JPanel filterPanel		= new JPanel();
		JPanel tablePanel 		= new JPanel();
		JPanel selectionPanel 	= new JPanel();
		JPanel previewPanel		= new JPanel();
		
		createTablePanel(tablePanel);
		createContextPanel(contextPanel);
		createFilterPanel(filterPanel);
		createSelectionPanel(selectionPanel);
		createPreviewPanel(previewPanel);
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.add(contextPanel);
		mainPanel.add(filterPanel);
		mainPanel.add(tablePanel);
		mainPanel.add(selectionPanel);
		mainPanel.add(previewPanel);
	}

	private void createPreviewPanel(JPanel previewPanel) {
		previewPanel.setBorder(BorderFactory.createTitledBorder("Darstellung"));
		previewPanel.setLayout(new GridBagLayout());
		
		previewTextField = new JEditorPane("text/html", "");
		previewTextField.setEditable(false);
		previewTextField.setOpaque(false);
	
		List<String> values = new ArrayList<>();
		boolean enabled;
		if (currentKeyRef.isOutputclassFixed()) {
			values.add(currentKeyRef.getOutputclass());
			enabled = false;
		} else {
			values.add("Key");
			values.add("Key (Name)");
			values.add("Key - Name");
			values.add("Name");
			enabled = true;
		}
		
		outputclassComboBox = new JComboBox<>(values.toArray(new String[0]));
		outputclassComboBox.setEnabled(enabled);
		if (enabled) {
			switch(outputclass) {
			case KeyRef.OC_KEY:
				outputclassComboBox.setSelectedIndex(0);
				break;
			case KeyRef.OC_KEY_NAME_BRACED:
				outputclassComboBox.setSelectedIndex(1);
				break;
			case KeyRef.OC_KEY_NAME_DASHED:
				outputclassComboBox.setSelectedIndex(2);
				break;
			case KeyRef.OC_NAME:
				outputclassComboBox.setSelectedIndex(3);
				break;
			default:
				outputclassComboBox.setSelectedIndex(0);
				break;
			}
			outputclassComboBox.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					updatePreview();
				}
			});
		}
		
		JPanel 	buttonPanel = new JPanel();
		expand 				= new JButton("+");		
		reduce	 			= new JButton("-");
		
		expand.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				expandNamespace();
			}
		});
		
		reduce.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				reduceNamespace();
			}
		});
		
		buttonPanel.add(reduce);
		buttonPanel.add(expand);
		
		GridBagConstraints textFieldConstraints = new GridBagConstraints();
		textFieldConstraints.anchor 	= GridBagConstraints.WEST;
		textFieldConstraints.gridx 		= 0;
		textFieldConstraints.fill 		= GridBagConstraints.BOTH;
		textFieldConstraints.weightx 	= 1;
		
		GridBagConstraints comboConstraints = new GridBagConstraints();
		comboConstraints.gridx = 1;
		comboConstraints.insets = new Insets(0, 5, 0, 0);
		
		GridBagConstraints buttonConstraints 	= new GridBagConstraints();
		buttonConstraints.anchor 	= GridBagConstraints.EAST;
		
		if (currentKeyRef.getFixedPathLen() == 0) {
			expand.setEnabled(true);
			reduce.setEnabled(true);
		} else {
			expand.setEnabled(false);
			reduce.setEnabled(false);
		}
		
		previewPanel.add(previewTextField, textFieldConstraints);
		previewPanel.add(outputclassComboBox, comboConstraints);
		previewPanel.add(buttonPanel, buttonConstraints);
	}

	private void createContextPanel(JPanel panel) {
		
		JPanel searchPanel			= new JPanel();
        JPanel contextPanel	 		= new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Details zum Kontext"));
        panel.setLayout(new GridBagLayout());
		createSearchPanel(searchPanel);
		
		searchPanel.addKeyListener(keyListener);
		contextPanel.addKeyListener(keyListener);
		panel.addKeyListener(keyListener);

		GridBagConstraints contextConstraints = new GridBagConstraints();
		contextConstraints.gridx 		= 0;
		contextConstraints.gridy		= 0;
		contextConstraints.weightx 		= 1;
		contextConstraints.weighty 		= 0;
		contextConstraints.gridwidth 	= 2;
		contextConstraints.fill 		= GridBagConstraints.BOTH;
		contextConstraints.anchor 		= GridBagConstraints.NORTHWEST;		
		
		GridBagConstraints searchConstraints = new GridBagConstraints();
		searchConstraints.gridx 	= 2;
		searchConstraints.gridy		= 0;
		searchConstraints.weightx 	= 1;
		searchConstraints.weighty 	= 0;
		searchConstraints.gridwidth = 1;
		searchConstraints.fill 		= GridBagConstraints.BOTH;
		searchConstraints.anchor 	= GridBagConstraints.SOUTHEAST;	
		
        GridBagConstraints labelConstr = new GridBagConstraints();
        labelConstr.gridx 		= 0;
        labelConstr.gridy 		= 0;
        labelConstr.weightx 	= 1;
        labelConstr.weighty 	= 0;
        labelConstr.gridwidth 	= 1;
        labelConstr.fill 		= GridBagConstraints.BOTH;
        labelConstr.anchor 		= GridBagConstraints.NORTHWEST;
        
        contextKeyField 		= addLabelPair("Key", 		labelConstr, contextPanel);
        contextTypeField 		= addLabelPair("Typ", 		labelConstr, contextPanel);
        contextNamespaceField 	= addLabelPair("Namespace",	labelConstr, contextPanel);
        
        if (contextKeyDef != null) {
	        contextKeyField.setText(contextKeyDef.getKey());
	    	contextTypeField.setText(contextKeyDef.getType());
	    	contextNamespaceField.setText(contextKeyDef.getNamespace());
        } else {
        	contextKeyField.setText("-");
	    	contextTypeField.setText("-");
	    	contextNamespaceField.setText("-");
        }
    	
        
        panel.add(contextPanel, contextConstraints);
        panel.add(searchPanel, searchConstraints);
	}

	private void createSearchPanel(JPanel searchPanel) {
		searchField = new JSearchTextField("Suche...");
		searchField.getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    createFilter(-1, searchField.getText());
                }
                public void insertUpdate(DocumentEvent e) {
                	createFilter(-1, searchField.getText());
                }
                public void removeUpdate(DocumentEvent e) {
                	createFilter(-1, searchField.getText());
                }
            });
		searchPanel.setLayout(new GridLayout(2, 1));
		searchPanel.add(new Label(""));
		searchField.setMinimumSize(new Dimension(150, 10));
		searchPanel.add(searchField);
	}
	
	private void createFilterPanel(JPanel filterPanel) {
		comboBoxes = new ArrayList<>(4);
		filterPanel.setLayout(new GridLayout(1, 4, 5, 5));
		filterPanel.setBorder(BorderFactory.createLineBorder(mainPanel.getBackground()));
		
		for (int i = 1; i <= 4; i++) {
			
			int column = i;
			JComboBox<Object> box = keyDefTableModel.getCombobox(i);
			box.addKeyListener(keyListener);

			box.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if (box.getSelectedIndex() >= 0 && box.getSelectedItem() instanceof String && boxAction) {
						createFilter(column, box.getSelectedItem().toString());
					}
				}
			});
			
			comboBoxes.add(box);
			filterPanel.add(box);
		}	
	}

	private void createTablePanel(JPanel tablePanel) {
		
		tablePanel.setLayout(new GridBagLayout());
		
		keyTable 				= new JTable(keyDefTableModel);
		JScrollPane scrollPane 	= new JScrollPane(keyTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		enableSorting(keyTable);
		keyTable.getTableHeader().setReorderingAllowed(false); 
		keyTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		keyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		keyTable.getColumnModel().getColumn(0).setPreferredWidth(25);
		keyTable.getColumnModel().getColumn(0).setMaxWidth(30);
		keyTable.getColumnModel().getColumn(0).setResizable(false);
		keyTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		keyTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		keyTable.getColumnModel().getColumn(3).setPreferredWidth(150);
		keyTable.getColumnModel().getColumn(4).setPreferredWidth(150);
		
		GridBagConstraints localConstr 	= new GridBagConstraints();
		localConstr.insets 				= new Insets(2, 2, 2, 2);
		localConstr.anchor 				= GridBagConstraints.WEST;
		localConstr.fill 				= GridBagConstraints.BOTH;
		localConstr.gridx 				= 0;
		localConstr.gridy 				= 0;
		localConstr.weightx 			= 1;
        localConstr.weighty 			= 1;
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        keyTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        
		tablePanel.add(scrollPane, localConstr);
		

		sorter 	= new TableRowSorter<>(keyDefTableModel);
		sorter.toggleSortOrder(0);
		sorter.toggleSortOrder(0); // initial sorting be priority descending
		sorter.addRowSorterListener(new RowSorterListener() {
            @Override
            public void sorterChanged (RowSorterEvent e) {
                if (e.getType () == RowSorterEvent.Type.SORTED) {
                    int selection = keyTable.getSelectedRow();
			        if (selection >= 0) {
			        	boxAction = false;
						reloadBoxItems();
						updateComboBoxes();
						boxAction = true;
			        	keyTable.setRowSelectionInterval(selection, selection);
			        	Rectangle selectRect = keyTable.getCellRect(selection, 0, true);
			        	selectRect.height = (keyTable.getParent().getHeight() / 2);
			        	keyTable.scrollRectToVisible(selectRect);
			        } else {
			        	if (keyTable.getRowCount() > 0) {
			        		keyTable.changeSelection(0, 0, false, false);
				        	boxAction = false;
							reloadBoxItems();
							updateComboBoxes();
							boxAction = true;
			        	}
			        }
                }
            }
        });
		keyTable.setRowSorter(sorter);
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void createFilter(int column, String filterText) {
		try {
			if (column == 0 && filterText == null) {
				// reset ComboBoxFilter
				for (int i = 0; i < comboBoxFilters.length; i++) {
					comboBoxFilters[i] = null;
				}
				for (Object o : comboBoxFilters) {
					filters.add((RowFilter<KeyDefTableModel, Object>) o);
				}
			}
			
			if (filterText != null) {
				if (column > 0) {
		    		// ComboBoxFilter
					filters = new ArrayList<RowFilter<KeyDefTableModel, Object>>();
					comboBoxFilters[column-1] = null;
					if (!filterText.startsWith("<")) {
			    		comboBoxFilters[column-1] = RowFilter.regexFilter("(?i)" + filterText, column);
					}
					for (Object o : comboBoxFilters) {
						if (o != null) {
							filters.add((RowFilter<KeyDefTableModel, Object>) o);
						}
					}
		    	} else if (column == -1){
		    		//SearchFieldFilter
		    		filters.remove(searchFieldFilter);
		    		searchFieldFilter = RowFilter.regexFilter("(?i)" + filterText);
		    		filters.add(searchFieldFilter);
		    	}
			}
	    	RowFilter<KeyDefTableModel, Object> rowf = RowFilter.andFilter(filters);
	    	sorter.setRowFilter(rowf);
	    	keyTable.scrollRectToVisible(new Rectangle(keyTable.getCellRect(keyTable.getSelectedRow(), 0, true)));
	    	if (keyTable.getSelectedRow() < 0 && keyTable.getRowCount() > 0) {
	    		keyTable.changeSelection(0, 0, false, false);
	    	}
	    	
	    } catch (java.util.regex.PatternSyntaxException e) {
	    	logger.error(e, e);
	    }
	}
	
	private void reloadBoxItems() {
		try{
		int boxCounter = 1;
		for (JComboBox<Object> box : comboBoxes) {
			Object selected = box.getSelectedItem();
			List<String> items = new ArrayList<>();
			box.removeAllItems();
			for (int i = 0; i < keyTable.getRowCount(); i++) {
				String item = (String) keyTable.getModel().getValueAt(keyTable.convertRowIndexToModel(i), boxCounter);
				if (!items.contains(item) && !item.isEmpty()) {
					items.add(item);
				}
			}
			Collections.sort(items);
			for (Object o : items) {
				box.addItem(o);
			}
			box.insertItemAt("<" + keyTable.getModel().getColumnName(boxCounter) + ">", 0);
			box.insertItemAt(new JSeparator(JSeparator.HORIZONTAL), 1);
			box.setSelectedItem(selected);
			boxCounter++;
		}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	private void updateDetails() {
		if (keyTable.getSelectedRow() >= 0) {
			final KeyDefTableModelEntry keyDef = keyDefTableModel.get(keyTable.convertRowIndexToModel(keyTable.getSelectedRow()));
			selectedKeyDef = keyDef.getKeyDef();
			
			keyField.setText(keyDef.getKey());
			nameField.setText(keyDef.getName());
			typeField.setText(keyDef.getType());
			namespaceField.setText(keyDef.getNamespace());
	        descField.setText(keyDef.getDesc());
	        urlField.setText(keyDef.getDefUrl());
	        defIdField.setText(keyDef.getDefId());
	        
	        namespaceList 			= keyDef.getNamespaceList();
	        currentNamespaceList    = createCurrentNamespaceList();
	        currentKey 				= keyDef.getKey();
	        updatePreview();
		} else {
			keyField.setText("");
	        nameField.setText("");
	        typeField.setText("");
	        namespaceField.setText("");
	        descField.setText("");
	        
	        previewTextField.setText("");
		}		
	}
	
	
	private void updateComboBoxes() {
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		try{
		for (int i = 0; i <= 3; i++) {
			JComboBox<Object> currentBox = comboBoxes.get(i);
			if (currentBox.isEnabled()) {
				if (currentBox.getItemCount() > 3 && currentBox.getItemAt(3).getClass().equals(JSeparator.class)) {
					currentBox.removeItemAt(3);
					currentBox.removeItemAt(2);
				}
				switch (i) {
				case 0:
					currentBox.insertItemAt(selectedKeyDef.getKey(), 2);
					break;
				case 1:
					currentBox.insertItemAt(selectedKeyDef.getName(), 2);
					break;
				case 2:
					currentBox.insertItemAt(selectedKeyDef.getType(), 2);
					break;
				case 3:
					currentBox.insertItemAt(selectedKeyDef.getNamespace(), 2);
					break;
				}
				currentBox.insertItemAt(separator, 3);
			}
		}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	private List<String> createCurrentNamespaceList() {
		List<String> nsList = new ArrayList<>();
		int index = namespaceList.size() - 1;
		int i = previewLength;
		while (i > 1 && index >= 0) {
			nsList.add(0, namespaceList.get(index));
			index--;
			i--;
		}
		return nsList;
	}
	
	private void setPreviewLength() {
		//logger.info("setPreviewLength");
		if (fixedPathLen > 0) {
			previewLength = fixedPathLen;
		} else if (fixedPathLen == -1) {
			previewLength = namespaceList.size() + 1;
		} else if (fixedPathLen == 0) {
			previewLength = currentKeyRef.getPathLen();
		} 
	}
	
	private void updatePreview() {
		StringBuilder 	sb 				= new StringBuilder();
		if (previewLength == namespaceList.size() + 1) {
			expand.setEnabled(false);
		}
		
		if (previewLength > 1) {
			reduce.setEnabled(true);
			for (String s : currentNamespaceList) {
				sb.append(s);
				sb.append(KeyspecInterface.PATH_DELIMITER);
			}
			sb.append(currentKey);
			currentPreview = sb.toString();
		} else {
			reduce.setEnabled(false);
			currentPreview = currentKey;
		}
		
		checkPreviewProperties();
		
		String 		name 		= selectedKeyDef.getName();
		String 		nameSuffix  = "";
		
		switch (outputclassComboBox.getSelectedIndex()) {
		case 0:
			outputclass = KeyRef.OC_KEY;
			break;
		case 1:
			outputclass = KeyRef.OC_KEY_NAME_BRACED;
			if (name != null && !name.isEmpty()) {
				nameSuffix = " (" + name + ")";
			} 
			break;
		case 2:
			outputclass = KeyRef.OC_KEY_NAME_DASHED;
			if (name != null && !name.isEmpty()) {
				nameSuffix = " - " + name;
			} 
			break;
		case 3:
			outputclass = KeyRef.OC_NAME;
			if (name != null && !name.isEmpty()) {
				nameSuffix = name;
			} 
			break;
		}
		String previewString = stylePreview(currentPreview, nameSuffix);
		previewTextField.setText(previewString);
	}
	
	private String stylePreview(String preview, String nameSuffix) {
		String text 	= "";
		String style 	= " style= font-family:\"serif\"; ";
		if (!outputclass.equals(KeyRef.OC_NAME)) {
			KeyTypeDef keyTypeDef = keyDefList.getKeyTypeDef(selectedKeyDef.getType());
			String 	fontFamily 	= "\"serif\"";
			String 	prefix		= keyTypeDef.prefix;
			String 	suffix		= keyTypeDef.suffix;
			String 	italicPre	= "";
			String 	italicSuf	= "";
			
			if (keyTypeDef.isCodeFont) {
				fontFamily = "\"monospace\"";
			} 
			if (keyTypeDef.isItalicFont) {
				italicPre = "<i>";
				italicSuf = "</i>";
			}
			prefix 	= prefix.replace("<", "&lt;");
			prefix 	= prefix.replace(">", "&gt;");
			suffix 	= suffix.replace("<", "&lt;");
			suffix 	= suffix.replace(">", "&gt;");
			style 	= " style= font-family:\"" + fontFamily + "\"; ";
			text = "<span " + style + ">  " + italicPre + prefix + preview + suffix + italicSuf + "</span>" +
					"<span style= font-family:\"serif\">" + nameSuffix + "</span>";
		} else {
			text = "<span " + style + ">" + nameSuffix + "</span>";
		}
		
		
		return text;
	}
	
	private void checkPreviewProperties() {
		previewLength = currentNamespaceList.size() + 1;
		
		if (fixedPathLen == 0) {
			if (previewLength <= namespaceList.size()) {
				expand.setEnabled(true);
			} else {
				expand.setEnabled(false);
			}
			
			if (previewLength > 1) {
				reduce.setEnabled(true);
			} else {
				reduce.setEnabled(false);
			}
		} else {
			expand.setEnabled(false);
			reduce.setEnabled(false);
		}
	}
	
	private void expandNamespace() {
		try {
		if (currentNamespaceList != null) {
			List<String> expandedNamespaceList = new ArrayList<String>();
			int i = namespaceList.size() - previewLength;
			expandedNamespaceList.add(namespaceList.get(i));
			for (String s : currentNamespaceList) {
				expandedNamespaceList.add(s);
			}
			currentNamespaceList = expandedNamespaceList;
			previewLength++;
			updatePreview();
			reduce.setEnabled(true);
			optionPane.requestFocus();
		}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	private void reduceNamespace() {
		try {
		List<String> reducedNamespaceList = new ArrayList<String>();
		if (currentNamespaceList != null) {
			for (int i = 1; i < currentNamespaceList.size(); i++) {
				reducedNamespaceList.add(currentNamespaceList.get(i));
			}
			currentNamespaceList = reducedNamespaceList;
			previewLength--;
			if (previewLength == 1) {
				reduce.setEnabled(false);
			}
			updatePreview();
			expand.setEnabled(true);
			optionPane.requestFocus();
		}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	private void enableSorting(JTable table) {
		KeyDefTableModel model = (KeyDefTableModel) table.getModel();
        if (model == null) {
            return;
        }

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>();
        table.setRowSorter(sorter);
        sorter.setModel(model);
	}

	private void createSelectionPanel(JPanel selectionPanel) {
		selectionPanel.setLayout(new GridBagLayout());
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Details zur Auswahl"));
		
		GridBagConstraints localConstr = new GridBagConstraints();
		
		localConstr.gridx 		= 0;
        localConstr.gridy 		= 0;
        localConstr.weightx 	= 0;
        localConstr.weighty 	= 0;
        localConstr.gridwidth 	= 1;
        localConstr.fill 		= GridBagConstraints.BOTH;
        localConstr.anchor 		= GridBagConstraints.NORTHWEST;
        
		keyField 		= addLabelFieldPair("Key", 			localConstr, selectionPanel);
		nameField 		= addLabelFieldPair("Name", 		localConstr, selectionPanel);
        typeField 		= addLabelFieldPair("Typ", 			localConstr, selectionPanel);
        namespaceField 	= addLabelFieldPair("Namespace", 	localConstr, selectionPanel);
        descField 		= addLabelFieldPair("Beschreibung", localConstr, selectionPanel);
        urlField 		= addLabelFieldPair("URL", 			localConstr, selectionPanel);
        defIdField 		= addLabelFieldPair("ID", 			localConstr, selectionPanel);
	}
	
	private JLabel addLabelPair(String labelText, GridBagConstraints constr, JPanel mainPanel) {
		JLabel textLabel = new JLabel(labelText);
        constr.gridy++;
		constr.gridx 	= 0;
        constr.weightx 	= 1;
        mainPanel.add(textLabel, constr);
        
        JLabel label = new JLabel();
        constr.gridx++;
        constr.weightx 		= 20;
        constr.insets.right = 0;
        constr.gridwidth 	= 3;
        mainPanel.add(label, constr);
        
        return label;
	}
	
	private JTextField addLabelFieldPair(String labelText, GridBagConstraints constr, JPanel mainPanel) {
        JLabel label = new JLabel(labelText);
        constr.gridy++;
        constr.gridx 	= 0;
        constr.weightx 	= 1;
        mainPanel.add(label, constr);
        
        JTextField field = new JTextField();
        field.setEditable(false);
        constr.gridx++;
        constr.weightx 		= 25;
        constr.insets.right = 0;
        constr.gridwidth 	= 3;
        mainPanel.add(field, constr);
        
        return field;
	}
	
	public KeyDefInterface getSelectedKeyDef() {
		return selectedKeyDef;
	}
	
	public String getKeyText() {
		return currentPreview;
	}
	
	public String getOutputclass() {
		return outputclass;
	}
	
	public String getColWidth() {
		String colWidth = "";
		for (int i = 0; i < 5; i++) {
			colWidth += keyTable.getColumnModel().getColumn(i).getWidth();
			if (i < 4) {
				colWidth += ",";
			}
		}
		return colWidth;
	}
	
	private static class KeyDefTableModel extends AbstractTableModel {
		
		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(KeyDefTableModel.class.getName());
		
	    private static final String[] 	COLUMN_NAMES 		= {"#", "Key", "Name", "Typ", "Namespace"};
	    private static final int		TYPE_COLUMN 		= 3;
	    private static final int 		NAMESPACE_COLUMN 	= 4;
	   	
	    private List<KeyDefTableModelEntry> currentKeyDefList 	= null;
	    final Set<String> 					typeFilter;
	    final List<String>					namespaceFilter;
		
		public KeyDefTableModel(KeyDefListInterface keyDefList, KeyRefInterface currentKeyRef, KeyPrioritizer keyPrioritizer) {
			
			currentKeyDefList				= new ArrayList<KeyDefTableModelEntry>();
			
			//TODO
			typeFilter 						= currentKeyRef.getTypeFilter();
			namespaceFilter					= currentKeyRef.getNamespaceFilter();
//			final boolean isNamespaceFixed 	= currentKeyRef.isNamespaceFixed();
			
			if (keyDefList != null) {
				for (KeyDefInterface keydef : keyDefList.getKeyDefs()) {
					if ((keydef.matchesTypeFilter(typeFilter)) && (keydef.matchesNamespaceFilter(namespaceFilter))) {
						final int priority = (keyPrioritizer != null) ? keyPrioritizer.getPriority(keydef) : 0;
						currentKeyDefList.add(KeyDefTableModelEntry.fromKeyDef(keydef, priority));
					}
				}
			}

//			logger.info("gefilterte Liste: " + currentKeyDefList);
			Collections.sort(currentKeyDefList, new KeyDefTableModelEntry.Comparator());
		}
		
	    @SuppressWarnings("unchecked")
		public JComboBox<Object> getCombobox(int col) {
	    	JComboBox<Object> comboBox;
	    	String value;
	    	ArrayList<String> items = new ArrayList<>();
	    	if (col == TYPE_COLUMN && typeFilter != null && !typeFilter.isEmpty()) {
	    		for (String s : typeFilter) {
	    			items.add(s);
	    		}
	    	} else if (col == NAMESPACE_COLUMN && namespaceFilter != null) {
	    		items.add(String.join(KeyRef.PATH_DELIMITER, namespaceFilter));
	    	} else {
		    	for (KeyDefTableModelEntry keyDef : currentKeyDefList) {
	
			    	value = keyDef.get(col);
		    		if (value != null && !value.equals("") && !items.contains(value)) {
		    			items.add(value);
		    		}
		    	}
	    	}
	    	
	    	Collections.sort(items);
	    	Object[] data;
	    	JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
	    	if (items.size() > 1) {
	    		data = new Object[items.size()+2];
			    data[0] = "<" + COLUMN_NAMES[col] + ">";
			    data[1] = separator;
			    for (int i = 2; i < data.length; i++) {
			    	data[i] = items.get(i-2);
			    }
		    	comboBox = new JComboBox<>(data);
	    	} else if (items.size() == 1) {
	    		data = new String[1];
	    		data[0] = items.get(0);
	    		comboBox = new JComboBox<>(data);
	    		comboBox.setEnabled(false);
	    	} else {
	    		comboBox = new JComboBox<>();
	    		comboBox.setEnabled(false);
	    	}
	    	comboBox.setRenderer(new SeparatorComboBoxRenderer());
	        comboBox.addActionListener(new SeparatorComboBoxListener(comboBox));
	    	return comboBox;
	    }
		
	    public KeyDefTableModelEntry get(int index) {
	    	return currentKeyDefList.get(index);
	    }
	    
	    public int getColumnCount() {
	        return COLUMN_NAMES.length;
	    }

	    public int getRowCount() {
	        return currentKeyDefList.size();
	    }

	    public String getColumnName(int col) {
	        return COLUMN_NAMES[col];
	    }

	    public Object getValueAt(int row, int col) {
	    	KeyDefTableModelEntry keyDef = get(row);
	    	if (col == 0) {
	    		return keyDef.getPriority();
	    	}
			return keyDef.get(col);
	    }

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return Integer.class;
			} else {
				return super.getColumnClass(columnIndex);
			}
		}
	}
}

@SuppressWarnings({ "serial", "rawtypes" })
class SeparatorComboBoxRenderer extends BasicComboBoxRenderer implements ListCellRenderer {
	
   public SeparatorComboBoxRenderer() {
      super();
   }
   
    
   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {	   
      if (isSelected) {
          setBackground(list.getSelectionBackground());
          setForeground(list.getSelectionForeground());
      } else {
    	  setBackground(list.getBackground());
          setForeground(list.getForeground());
      }
      
      setFont(list.getFont());

      if (value instanceof JSeparator) {
         return (Component) value;
      } else if (value instanceof String) {
    	 setText((String) value);
      } else { 
         setText((value == null) ? "" : value.toString());
      }
  
      return this;
  } 
   
}
  
class SeparatorComboBoxListener implements ActionListener {
   JComboBox<Object> combobox;
   Object oldItem;
     
   SeparatorComboBoxListener(JComboBox<Object> combobox) {
      this.combobox = combobox;
      if (combobox.getItemCount() > 0) {
    	  combobox.setSelectedIndex(0);
      }
      oldItem = combobox.getSelectedItem();
   }
   
      
   public void actionPerformed(ActionEvent e) {
      Object selectedItem = combobox.getSelectedItem();
      if (selectedItem instanceof JSeparator) {
         combobox.setSelectedItem(oldItem);
      } else {
         oldItem = selectedItem;
      }
   }
}