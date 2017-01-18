package org.DitaSemia.Oxygen.AuthorOperations;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.DitaSemia.Base.ProgressListener;
import org.DitaSemia.Oxygen.BookCacheHandler;
import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class RefreshBookCache implements AuthorOperation {

	private static final Logger logger = Logger.getLogger(RefreshBookCache.class.getName());

	@Override
	public String getDescription() {
		return "Cache aktualisieren";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap argumentsMap) {
		try {
		
			final AuthorDocumentController 	documentController 	= authorAccess.getDocumentController();
			final AuthorNode				rootNode			= documentController.getNodeAtOffset(0);
	
			final Frame parentFrame = (Frame)authorAccess.getWorkspaceAccess().getParentFrame();

		    final JDialog dialog = new JDialog(parentFrame, "Refreshing Book Cache...", true);
		    JPanel barPanel = new JPanel();
		    JProgressBar progressBar = new JProgressBar(0, 1);
		    dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.PAGE_AXIS));
		    barPanel.setLayout(new BoxLayout(barPanel, BoxLayout.LINE_AXIS));
		    
		    barPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		    barPanel.add(progressBar);
		    progressBar.setMaximumSize(new Dimension(272, 27));
		    barPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		    
		    dialog.add(Box.createRigidArea(new Dimension(0, 5)));
		    dialog.add(barPanel);
		    dialog.add(Box.createRigidArea(new Dimension(0, 5)));
		    
		    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		    dialog.setSize(300, 75);
		    dialog.setLocationRelativeTo(parentFrame);
		    
		    final ProgressListener progressListener = new ProgressListener() {
				@Override
				public void setProgress(int progress, int total) {
					if (progressBar.getMaximum() != total) {
						progressBar.setMaximum(total);
					}
					progressBar.setValue(progress);
				}
		    };

		    Thread workerThread = new Thread(new Runnable() {
		    	@Override
		    	public void run() {
		    		BookCacheHandler.getInstance().refreshBookCache(rootNode.getXMLBaseURL(), progressListener);
		    		dialog.setVisible(false);
		    	}
		    });

		    workerThread.start();
		    dialog.setVisible(true);

		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null;
	}

}
