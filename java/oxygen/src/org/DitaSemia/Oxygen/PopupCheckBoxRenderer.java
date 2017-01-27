/*
 * Extends the standard PopupCheckBoxRenderer from oXygen by adding an argument "emptyLabel" to specify the value to be displayed for empty content.
 */
package org.DitaSemia.Oxygen;

import java.awt.Color;
import java.awt.FontMetrics;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import ro.sync.contentcompletion.xml.CIValue;
import ro.sync.ecss.dita.CILevelValue;
import ro.sync.ecss.extensions.api.CursorType;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.editor.InplaceEditorArgumentKeys;
import ro.sync.ecss.extensions.api.editor.InplaceRenderer;
import ro.sync.ecss.extensions.api.editor.RendererLayoutInfo;
import ro.sync.exml.view.graphics.Dimension;
import ro.sync.exml.view.graphics.Font;
import ro.sync.exml.workspace.api.Platform;

/**
 * Presents a simple or a composed value (multiple values separated by a separator) 
 * using a JLabel.
 *  
 * @author alex_jitianu
 */
public class PopupCheckBoxRenderer implements InplaceRenderer {
	
	public static final String PROPERTY_EMPTY_LABEL = "emptyLabel";
	
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(PopupCheckBoxRenderer.class.getName());
  
  /**
   * The label that presents the values.
   */
  private final JLabel label = new JLabel();
  /**
   * Default font for the label.
   */
  private final java.awt.Font defaultFont;
  /**
   * Default foreground color for the label.
   */
  private final Color defaultForeground;
  /**
   * Imposed tooltip for popup checkbox editor.
   */
  private String tooltip;
  
  /**
   * The ascending values comparator.
   */
  private static Comparator<String> ASCENDING_VALUES_COMPARATOR = new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
      return Collator.getInstance().compare(o1, o2);
    }
  };
  
  /**
   * The descending values comparator.
   */
  private static Comparator<String> DESCENDING_VALUES_COMPARATOR = new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
      return - ASCENDING_VALUES_COMPARATOR.compare(o1, o2);
    }
  };
  
  /**
   * Constructor.
   */
  public PopupCheckBoxRenderer() {
    defaultFont = label.getFont();
    defaultForeground = Color.BLACK;
  }
  
  /**
   * Prepare the renderer.
   * 
   * @param context In-place editing information.
   */
  private void prepare(AuthorInplaceContext context) {
    boolean isSA = true;
    if (context.getAuthorAccess() != null && context.getAuthorAccess().getWorkspaceAccess() != null) {
      isSA = Platform.STANDALONE.equals(
          context.getAuthorAccess().getWorkspaceAccess().getPlatform());
    }

    ro.sync.exml.view.graphics.Color color =
        (ro.sync.exml.view.graphics.Color) context.getArguments().get(InplaceEditorArgumentKeys.BG_COLOR);
    Color awtColor = null;
    if (color != null) {
      awtColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
    
    if (!isSA && awtColor != null) {
      label.setBackground(awtColor);
      label.setOpaque(true);
    } else {
      label.setOpaque(false);
    }
    
    label.setBorder(BorderFactory.createEmptyBorder());
    
    String separator = (String) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_SEPARATOR);
    String rendererSeparator = (String) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_RENDERER_SEPARATOR);
    tooltip = (String) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_TOOLTIP);
    // If no separator is specified, SPACE is used.
    if (separator == null) {
      separator = " ";
    }
    if (rendererSeparator == null) {
      rendererSeparator = separator;
    }

    String initialValue = (String) context.getArguments().get(InplaceEditorArgumentKeys.INITIAL_VALUE);
    if (logger.isDebugEnabled()) {
      logger.debug("initialValue " + initialValue);
    }
    
    List<String> cValues = new ArrayList<String>();
    if (initialValue != null) {
      List<String> initialValues = new ArrayList<String>();
      StringTokenizer stringTokenizer = new StringTokenizer(initialValue, separator);
      while (stringTokenizer.hasMoreTokens()) {
        String token = stringTokenizer.nextToken();
        initialValues.add(token.trim());
      }
      cValues = initialValues;
    }
    
    // Initialize font settings.
    Font font = (Font) context.getArguments().get(InplaceEditorArgumentKeys.FONT);
    if (font != null) {
      label.setFont(new java.awt.Font(font.getName(), font.getStyle(), font.getSize()));
    } else {
      label.setFont(defaultFont);
    }
    
    if (initialValue == null) {
      List<String> defaultValues = new ArrayList<String>();
      String defaultValue = (String) context.getArguments().get(InplaceEditorArgumentKeys.DEFAULT_VALUE);
      if (logger.isDebugEnabled()) {
        logger.debug("defaultValue " + defaultValue);
      }
      if (defaultValue != null) {
        StringTokenizer stringTokenizer = new StringTokenizer(defaultValue, separator);
        while (stringTokenizer.hasMoreTokens()) {
          String token = stringTokenizer.nextToken();
          defaultValues.add(token.trim());
        }
      }

      cValues = defaultValues;
    }
    
    StringBuilder text = new StringBuilder();
    
    //Maybe we have to sort the rendered values.
    Object obj = context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_RENDERER_SORT);
    if(InplaceEditorArgumentKeys.SORT_ASCENDING.equals(obj)) {
      Collections.sort(cValues, ASCENDING_VALUES_COMPARATOR);
    } else if(InplaceEditorArgumentKeys.SORT_DESCENDING.equals(obj)) {
      Collections.sort(cValues, DESCENDING_VALUES_COMPARATOR);
    }

    boolean hasLabels = context.getArguments().containsKey(InplaceEditorArgumentKeys.PROPERTY_LABELS);
    List<String> labels = null;
    List<CIValue> values = null;
    boolean readLabelsAndValuesProps = hasLabels;

    if (readLabelsAndValuesProps) {
      // Avoid requesting the labels is there is no need for them.
      labels = (List<String>) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_LABELS);
      // If we have labels we also need the values because there is a one-to-one corespondence
      // between the two lists.
      values = (List<CIValue>) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_VALUES);
    }

    // Create checkboxes for all values.
    for (int i = 0; i < cValues.size(); i++) {
      String val = cValues.get(i);
      int indexOf = indexOfCIValue(values, val);
      if (indexOf != -1) {
        if (labels != null && !labels.isEmpty()) {
          val = labels.get(indexOf);
        } else if(values != null) {
          CIValue ciValue = values.get(indexOf);
          if(ciValue instanceof CILevelValue){
            String presentationName = ((CILevelValue)ciValue).getPresentationName();
            if(presentationName != null && ! presentationName.isEmpty()){
              val = presentationName;
            }
          }
        }
      }
      
      if (text.length() > 0) {
        text.append(rendererSeparator);
        if (!" ".equals(rendererSeparator)) {
          // Add a space as an inset.
          text.append(' ');
        }
      }
      
      text.append(val);
    }

    if (text.length() == 0) {
      // We need a place holder.
      //text.append("[Empty]");
    	text.append((String)context.getArguments().getOrDefault(PROPERTY_EMPTY_LABEL, "[Empty]"));
    }
    color = (ro.sync.exml.view.graphics.Color) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_COLOR);
    if (color != null) {
      label.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
    } else {
      label.setForeground(defaultForeground);
    }
    
    label.setText(text.toString());
    
    if (initialValue == null) {
      String defaultValue = (String) context.getArguments().get(InplaceEditorArgumentKeys.DEFAULT_VALUE);
      if (defaultValue != null) {
        // If we are presenting the default combination we'll mark it differently.
        int style = Font.ITALIC;
        if (label.getFont().getStyle() != Font.PLAIN) {
          style = style | label.getFont().getStyle();
        }
        label.setFont(label.getFont().deriveFont(style));
      }
    }
  }

  /**
   * @see ro.sync.ecss.extensions.api.Extension#getDescription()
   */
  @Override
  public String getDescription() {
    return "A renderer that uses checkboxes.";
  }

  /**
   * @see ro.sync.ecss.extensions.api.editor.InplaceRenderer#getRendererComponent(ro.sync.ecss.extensions.api.editor.AuthorInplaceContext)
   */
  @Override
  public Object getRendererComponent(AuthorInplaceContext context) {
    prepare(context);
    return label;
  }

  /**
   * @see ro.sync.ecss.extensions.api.editor.InplaceRenderer#getRenderingInfo(ro.sync.ecss.extensions.api.editor.AuthorInplaceContext)
   */
  @Override
  public RendererLayoutInfo getRenderingInfo(AuthorInplaceContext context) {
    prepare(context);
    
    final java.awt.Dimension preferredSize = label.getPreferredSize();
    
    // Get the width
    int width = preferredSize.width;
    Integer columns = (Integer) context.getArguments().get(InplaceEditorArgumentKeys.PROPERTY_COLUMNS);
    if (columns != null && columns > 0) {
      FontMetrics fontMetrics = label.getFontMetrics(label.getFont());
      width = columns * fontMetrics.charWidth('w');
    }
    
    int imposedWidth = context.getPropertyEvaluator().evaluateWidthProperty(
        context.getArguments(), 
        context.getStyles().getFont().getSize());
    if (imposedWidth != -1) {
      width = imposedWidth;
    }

    return new RendererLayoutInfo(
        getBaseline(label, width, preferredSize.height),
        new Dimension(width, preferredSize.height));
  }
  
  public static int getBaseline(JComponent component, int width, int height){
    try{
      return component.getBaseline(width, height);
    } catch(RuntimeException ex){
      //EXM-35986 Avoid NPEs thrown when method invoked on non-AWT thread.
      logger.error(ex, ex);
    }
    return -1;
  }
  
  /**
   * @see ro.sync.ecss.extensions.api.editor.InplaceRenderer#getTooltipText(ro.sync.ecss.extensions.api.editor.AuthorInplaceContext, int, int)
   */
  @Override
  public String getTooltipText(AuthorInplaceContext context, int x, int y) {
    prepare(context);
    return tooltip == null && !context.isReadOnlyContext() ? 
        ("Click to edit: " + label.getText()) : tooltip;
  }
  
  /**
   * @see ro.sync.ecss.extensions.api.editor.InplaceRenderer#getCursorType(ro.sync.ecss.extensions.api.editor.AuthorInplaceContext, int, int)
   */
  @Override
  public CursorType getCursorType(AuthorInplaceContext context, int x, int y) {
    return CursorType.CURSOR_HAND;
  }
  
  /**
   * Returns the index of the first occurrence of the specified CIValue string 
   * in the given CIValue list, or -1 if the list does not contain the element.
   * 
   * @param values The list of CIValues.
   * @param value The string value of a CIValue.
   * 
   * @return The index of the first occurrence of the specified CIValue string 
   * in the given CIValue list, or -1 if the list does not contain the element.
   */
  private int indexOfCIValue(List<CIValue> values, String value) {
    int index = -1;
    if (value != null && values != null) {
      for (int i = 0; i < values.size(); i++) {
        String insertString = values.get(i).getInsertString();
        if (value.equals(insertString)) {
          index = i;
          break;
        }
      }
    }

    return index;
  }
  
  /**
   * @see ro.sync.ecss.extensions.api.editor.InplaceRenderer#getCursorType(int, int)
   */
  @Override
  public CursorType getCursorType(int x, int y) {
    return null;
  }
}