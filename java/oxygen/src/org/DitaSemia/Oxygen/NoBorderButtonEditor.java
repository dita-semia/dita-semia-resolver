package org.DitaSemia.Oxygen;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import ro.sync.exml.view.graphics.Dimension;
import ro.sync.exml.view.graphics.Point;
import ro.sync.exml.view.graphics.Rectangle;
import ro.sync.ecss.component.editor.ButtonEditor;
import ro.sync.ecss.extensions.api.editor.AuthorInplaceContext;
import ro.sync.ecss.extensions.api.editor.RendererLayoutInfo;

public class NoBorderButtonEditor extends ButtonEditor {
  
  @Override
  public Object getRendererComponent(AuthorInplaceContext context) {
    JButton button = (JButton)super.getRendererComponent(context);
    button.setBorder(new EmptyBorder(0, 0, 0, 0));
    return button;
  }
  
  @Override
  public Object getEditorComponent(AuthorInplaceContext context, Rectangle allocation, Point mouseLocation) {
	  JButton button = (JButton)super.getRendererComponent(context);
	  button.setBorder(new EmptyBorder(0, 0, 0, 0));
	  return button;
  }
  
  @Override 
  public RendererLayoutInfo getRenderingInfo(AuthorInplaceContext context) { 
	  JButton button = (JButton) getRendererComponent(context); 
	  final java.awt.Dimension preferredSize = button.getPreferredSize(); 

	  return new RendererLayoutInfo(
		  0/*button.getBaseline(preferredSize.width, preferredSize.height)*/, 
		  new Dimension(preferredSize.width, preferredSize.height)); 
  } 

}
