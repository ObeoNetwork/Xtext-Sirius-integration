package org.obeonetwork.dsl.viewpoint.xtext.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;

import com.google.inject.Injector;

import de.uka.ipd.sdq.stoex.RandomVariable;

public class StoexXtextEmbeddedEditor extends XtextEmbeddedEditor {

	private final Injector xtextInjector;

	public StoexXtextEmbeddedEditor(IGraphicalEditPart editPart, Injector xtextInjector) {
		super(editPart, xtextInjector);
		this.xtextInjector = xtextInjector;
	}

	protected XtextResource createVirtualXtextResource(URI uri, EObject semanticElement) throws IOException {
		IResourceFactory resourceFactory = xtextInjector.getInstance(IResourceFactory.class);
		// TODO use the syntheticscheme.
		XtextResourceSet rs = xtextInjector.getInstance(XtextResourceSet.class);
		rs.setClasspathURIContext(getClass()); // Create virtual resource
		XtextResource xtextVirtualResource = (XtextResource) resourceFactory
				.createResource(URI.createURI(uri.toString()));
		rs.getResources().add(xtextVirtualResource);

		// Populate virtual resource with the given semantic element to edit
		xtextVirtualResource.getContents().add((EObject) ((RandomVariable) semanticElement).getExpression());

		// Save and reparse in order to initialize virtual Xtext resource
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		xtextVirtualResource.save(out, Collections.emptyMap());
		xtextVirtualResource.reparse(new String(out.toByteArray()));

		return xtextVirtualResource;
	}

	// TODO ist id neu gesetzt/übernommen?
	@Override
	protected void reconcile(Resource resourceInSirius, XtextResource resourceInEmbeddedEditor) {
		EObject contentInSirius = resourceInSirius.getEObject(semanticElementFragment);
		EObject newContent = resourceInEmbeddedEditor.getContents().get(0);

		// replace specification
		String newSpecification = resourceInEmbeddedEditor.getParseResult().getRootNode().getText();

		((RandomVariable) contentInSirius).setSpecification(newSpecification);

		RandomVariable newObj = (RandomVariable) EcoreUtil.copy(contentInSirius);
		EClass newObjClass = newObj.eClass();
		newObjClass.eSet(newObjClass.getEStructuralFeature("specification"), newSpecification);

		EcoreUtil.replace(newObj.getExpression(), newContent);
		EcoreUtil.replace(contentInSirius, newObj);
	}

	/**
	 * Create the XText editor
	 * 
	 * @param editorInput
	 *            the editor input
	 * @throws Exception
	 */
	protected void createXtextEditor() throws Exception {
		DiagramRootEditPart diagramEditPart = (DiagramRootEditPart) hostEditPart.getRoot();
		Composite parentComposite = (Composite) diagramEditPart.getViewer().getControl();

		EObject semanticElementInDocument = xtextResource.getContents().get(0);
		ICompositeNode rootNode = xtextResource.getParseResult().getRootNode();
		String allText = rootNode.getText();
		ICompositeNode elementNode = NodeModelUtils.findActualNodeFor(semanticElementInDocument);
		String prefix = "";
		String editablePart = allText.substring(elementNode.getOffset(), elementNode.getEndOffset());
		String suffix = allText.substring(elementNode.getEndOffset());
		xtextEditorComposite = new Decorations(parentComposite, SWT.RESIZE | SWT.ON_TOP | SWT.BORDER);
		xtextEditorComposite.setLayout(new FillLayout());

		EmbeddedEditorFactory factory = new EmbeddedEditorFactory();
		xtextInjector.injectMembers(factory);
		xTextEmbeddedEditor = factory.newEditor(new IEditedResourceProvider() {

			@Override
			public XtextResource createResource() {
				return xtextResource;
			}
		}).showErrorAndWarningAnnotations().withParent(xtextEditorComposite);
		xtextPartialEditor = xTextEmbeddedEditor.createPartialEditor(prefix, editablePart, suffix, true);

		addKeyVerifyListener();
		setEditorBounds();
		xtextEditorComposite.setVisible(true);
		xtextEditorComposite.forceFocus();
	}

}
