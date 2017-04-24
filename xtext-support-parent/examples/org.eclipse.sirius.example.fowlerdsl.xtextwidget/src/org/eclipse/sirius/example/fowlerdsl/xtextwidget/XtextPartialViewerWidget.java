package org.eclipse.sirius.example.fowlerdsl.xtextwidget;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.xtext.Constants;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.XtextSourceViewer;
import org.eclipse.xtext.ui.editor.XtextSourceViewerConfiguration;
import org.eclipse.xtext.ui.editor.model.XtextDocument;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class XtextPartialViewerWidget {

	private int style;

	private XtextSourceViewer sourceViewer;
	
	private XtextDocument document;
	
	@Inject
	@Named(Constants.FILE_EXTENSIONS)
	private String fileExtension;

	@Inject
	private XtextSourceViewer.Factory sourceViewerFactory;

	@Inject
	private Provider<XtextSourceViewerConfiguration> sourceViewerConfigurationProvider;

	@Inject
	private Provider<XtextDocument> documentProvider;

	@Inject 
	private Provider<IDocumentPartitioner> documentPartitioner;
	
	public XtextPartialViewerWidget(Composite parentComposite, Injector xtextInjector, int style) {
		this.style = style;
		
		xtextInjector.injectMembers(this);

		createEditor(parentComposite);
	}
	
	public XtextPartialViewerWidget(Composite control, Injector injector) {
		this(control, injector, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	private void createEditor(Composite parent) {
		sourceViewer = sourceViewerFactory.createSourceViewer(parent, null, null, true, style);
		sourceViewer.setEditable(false);
		
		XtextSourceViewerConfiguration viewerConfiguration = sourceViewerConfigurationProvider.get();
		sourceViewer.configure(viewerConfiguration);	
		
		document = documentProvider.get();
		
		IDocumentPartitioner partitioner = documentPartitioner.get(); 
		partitioner.connect(document); 
		document.setDocumentPartitioner(partitioner);
		
		document.set("");
		sourceViewer.setDocument(document);
		
	}
	
	public Control getControl() {
		return sourceViewer.getControl();
	}
	
	public void update(EObject semanticElement) {
		
		if (semanticElement != null) {
			ICompositeNode elementNode = NodeModelUtils.findActualNodeFor(semanticElement);
			update(elementNode.getText().trim());
		}
		
	}
	
	public void update(String text) {
		IDocument document = sourceViewer.getDocument();
		
		sourceViewer.setRedraw(false);
		document.set(text);
		sourceViewer.setVisibleRegion(0, text.length());
		sourceViewer.setSelectedRange(0, 0);
		sourceViewer.setRedraw(true);
	}
	
}
