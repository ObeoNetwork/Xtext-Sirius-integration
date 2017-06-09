package org.eclipse.sirius.example.fowlerdsl.xtextwidget;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource.ResourceHandler;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.xtext.Constants;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
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

	@Inject
	private IResourceFactory resourceFactory;

	@Inject
	private XtextResourceSet resourceSet;

	private KeyAdapter ctrlKeyList = new KeyAdapter() {
	};

	private XtextResource virtualResource;

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

		sourceViewer.setDocument(document);

		final IContentAssistant contentAssistant = viewerConfiguration.getContentAssistant(sourceViewer);
		contentAssistant.install(sourceViewer);

		ctrlKeyList = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				// CTRL + SPACE to complement
				if (viewerConfiguration != null && e.keyCode == SWT.SPACE && e.stateMask == SWT.CTRL) {
					contentAssistant.showPossibleCompletions();
				}
			}
		};
		sourceViewer.getTextWidget().addKeyListener(ctrlKeyList);
		try {
			virtualResource = createVirtualXtextResource(URI.createURI("_synthetic.statemachine"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private XtextResource createVirtualXtextResource(URI uri) throws IOException {
		resourceSet.setClasspathURIContext(getClass());
		// Create virtual resource
		XtextResource xtextVirtualResource = (XtextResource) resourceFactory
				.createResource(URI.createURI(uri.toString()));
		resourceSet.getResources().add(xtextVirtualResource);

		// // Populate virtual resource with the given semantic element to edit
		// xtextVirtualResource.getContents().add(semanticElement);
		//
		// // Save and reparse in order to initialize virtual Xtext resource
		// ByteArrayOutputStream out = new ByteArrayOutputStream();
		// xtextVirtualResource.save(out, Collections.emptyMap());
		// xtextVirtualResource.reparse(new String(out.toByteArray()));

		return xtextVirtualResource;
	}

	public Control getControl() {
		return sourceViewer.getControl();
	}

	public void update(EObject semanticElement) {

		if (semanticElement != null) {
			if (semanticElement.eResource() instanceof XtextResource) {
				virtualResource.getContents().clear();
				virtualResource.getContents()
						.add(EcoreUtil.copy(semanticElement.eResource().getContents().iterator().next()));
				// Save and reparse in order to initialize virtual Xtext
				// resource
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					virtualResource.save(out, Collections.emptyMap());
					virtualResource.reparse(new String(out.toByteArray()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				document.setInput(virtualResource);
				ICompositeNode elementNode = NodeModelUtils.findActualNodeFor(semanticElement);
				update(elementNode.getText().trim());
			}
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
