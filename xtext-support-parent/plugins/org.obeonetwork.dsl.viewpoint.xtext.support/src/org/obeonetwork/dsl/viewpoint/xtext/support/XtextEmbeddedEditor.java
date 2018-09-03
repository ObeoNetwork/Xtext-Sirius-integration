/******************************************************************************
 * Copyright (c) 2011 Obeo  and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Obeo  - initial API and implementation
 ****************************************************************************/

package org.obeonetwork.dsl.viewpoint.xtext.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IBatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramEditDomain;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.sirius.viewpoint.DSemanticDecorator;
import org.eclipse.sirius.viewpoint.ViewpointPackage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess;
import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;

import com.google.inject.Injector;

/* largely inspired by "org.eclipse.xtext.gmf.glue" from XText examples */
public class XtextEmbeddedEditor {

	private static int MIN_EDITOR_WIDTH = 100;

	private static int MIN_EDITOR_HEIGHT = 20;

	protected IGraphicalEditPart hostEditPart;

	private IEditorPart diagramEditor;

	protected EmbeddedEditorModelAccess xtextPartialEditor;

	private final Injector xtextInjector;

	private Resource originalResource;

	protected XtextResource xtextResource;

	protected String semanticElementFragment;

	protected Decorations xtextEditorComposite;

	protected EmbeddedEditor xTextEmbeddedEditor;

	public XtextEmbeddedEditor(IGraphicalEditPart editPart, Injector xtextInjector) {
		this.hostEditPart = editPart;
		this.xtextInjector = xtextInjector;
	}

	protected EObject resolveSemanticElement(IGraphicalEditPart hostEditPart) {
		EObject eObject = hostEditPart.resolveSemanticElement();
		if (isDSemanticDecorator(eObject)) {
			DSemanticDecorator semanticDecorator = (DSemanticDecorator) eObject;
			return semanticDecorator.getTarget();
		}
		return resolveSemanticElement(hostEditPart);
	}

	private boolean isDSemanticDecorator(EObject eObject) {
		return ViewpointPackage.eINSTANCE.getDSemanticDecorator().isInstance(eObject);
	}

	public void showEditor() {
		EObject originalSemanticElement = resolveSemanticElement(hostEditPart);
		if (originalSemanticElement == null) {
			return;
		}
		this.originalResource = originalSemanticElement.eResource();
		EObject semanticElement = EcoreUtil.copy(originalResource.getContents().get(0));
		showEditor(originalSemanticElement, semanticElement);
	}
	
	public void showEditor(EObject originalSemanticElement) {
		this.originalResource = originalSemanticElement.eResource();
		showEditor(originalSemanticElement, originalSemanticElement);
	}

	public void showEditor(EObject originalSemanticElement, EObject semanticElement) {
		try {
			// Clone root EObject
			if (this.xtextResource == null) {
				this.xtextResource = createVirtualXtextResource(originalResource.getURI(), semanticElement);
			}

			// TODO manage multi resource with Xtext Linking or Scoping service
			semanticElementFragment = originalResource.getURIFragment(originalSemanticElement);
			if (semanticElementFragment == null || "".equals(semanticElementFragment)) {
				return;
			}
			IDiagramEditDomain diagramEditDomain = hostEditPart.getDiagramEditDomain();
			diagramEditor = ((DiagramEditDomain) diagramEditDomain).getEditorPart();
			createXtextEditor();
		} catch (Exception e) {
			Activator.logError(e);
		} finally {
			if (hostEditPart != null) {
				hostEditPart.refresh();
			}
		}
	}

	/**
	 * Close this editor.
	 * 
	 * @param isReconcile
	 */
	public void closeEditor(boolean isReconcile) {
		if (xtextPartialEditor != null) {
			if (isReconcile) {
				try {
					updateXtextResource();
				} catch (Exception exc) {
					Activator.logError(exc);
				}
			}
			// xtextEditor.dispose();
			if (xtextEditorComposite != null) {
				this.xtextEditorComposite.setVisible(false);
				this.xtextEditorComposite.dispose();
				xtextEditorComposite = null;
			}
			xtextPartialEditor = null;
		}
	}

	protected XtextResource createVirtualXtextResource(URI uri, EObject semanticElement) throws IOException {
		IResourceFactory resourceFactory = xtextInjector.getInstance(IResourceFactory.class);
		// TODO use the synthetic scheme.
		XtextResourceSet rs = xtextInjector.getInstance(XtextResourceSet.class);
		rs.setClasspathURIContext(getClass());
		// Create virtual resource
		XtextResource xtextVirtualResource = (XtextResource) resourceFactory
				.createResource(URI.createURI(uri.toString()));
		rs.getResources().add(xtextVirtualResource);

		// Populate virtual resource with the given semantic element to edit
		xtextVirtualResource.getContents().add(semanticElement);

		// Save and reparse in order to initialize virtual Xtext resource
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		xtextVirtualResource.save(out, Collections.emptyMap());
		xtextVirtualResource.reparse(new String(out.toByteArray()));

		return xtextVirtualResource;
	}

	protected void updateXtextResource() throws IOException, BadLocationException {
		String newText = xtextPartialEditor.getSerializedModel();
		xtextResource.reparse(newText);
		EcoreUtil.resolveAll(xtextResource);
		final IParseResult parseResult = xtextResource.getParseResult();
		if (!parseResult.hasSyntaxErrors() && !hasDanglingRefs(xtextResource)) {
			reconcile(originalResource, xtextResource);
		}
	}

	private boolean hasDanglingRefs(XtextResource xtextResource2) {
		return EcoreUtil.UnresolvedProxyCrossReferencer.find(xtextResource2).size() > 0;
	}

	protected void reconcile(Resource resourceInSirius, XtextResource resourceInEmbeddedEditor) {
		try {

			IComparisonScope scope = new DefaultComparisonScope(resourceInSirius, resourceInEmbeddedEditor, null);
			final Comparison comparison = EMFCompare.builder().build().compare(scope);

			IMerger.Registry mergerRegistry = EMFCompareRCPPlugin.getDefault().getMergerRegistry();
			final IBatchMerger merger = new BatchMerger(mergerRegistry);

			final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(originalResource);
			editingDomain.getCommandStack()
					.execute(new RecordingCommand(editingDomain, "update resource after direct text edit") {

						@Override
						protected void doExecute() {
							merger.copyAllRightToLeft(comparison.getDifferences(), new BasicMonitor());
						}
					});
		} catch (Exception e) {
			Activator.logError(e);
		}

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
		
		EObject semanticElementInDocument = xtextResource.getEObject(semanticElementFragment);
		ICompositeNode rootNode = xtextResource.getParseResult().getRootNode();
		String allText = rootNode.getText();
		ICompositeNode elementNode = NodeModelUtils.findActualNodeFor(semanticElementInDocument);
		String prefix = allText.substring(0, elementNode.getOffset() - 1);
		String editablePart = allText.substring(elementNode.getOffset(), elementNode.getEndOffset());
		String suffix = allText.substring(elementNode.getEndOffset());
		xtextEditorComposite = new Decorations(parentComposite, SWT.RESIZE | SWT.ON_TOP | SWT.BORDER);
		xtextEditorComposite.setLayout(new FillLayout());

		EmbeddedEditorFactory factory = new EmbeddedEditorFactory();
		xtextInjector.injectMembers(factory);
		IEditedResourceProvider provider = new IEditedResourceProvider() {

			@Override
			public XtextResource createResource() {
				return xtextResource;
			}
		};
		xTextEmbeddedEditor = factory.newEditor(provider).showErrorAndWarningAnnotations()
				.withParent(xtextEditorComposite);
		xtextPartialEditor = xTextEmbeddedEditor.createPartialEditor(prefix, editablePart, suffix, true);

		addKeyVerifyListener();
		setEditorBounds();
		xtextEditorComposite.setVisible(true);
		xtextEditorComposite.forceFocus();
	}

	protected void addKeyVerifyListener() {
		final StyledText xtextTextWidget = xTextEmbeddedEditor.getViewer().getTextWidget();
		xtextTextWidget.addVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent e) {
				int keyCode = e.keyCode;
				if ((e.stateMask & SWT.CTRL) != 0 && ((keyCode == SWT.KEYPAD_CR) || (keyCode == SWT.CR))) {
					e.doit = false;
					closeEditor(true);
				}
				if (keyCode == SWT.ESC) {
					e.doit = false;
					closeEditor(false);
				}

			}
		});
	}

	protected void setEditorBounds() {
		// mind the added newlines
		String editString = xtextPartialEditor.getEditablePart();

		int numLines = StringUtil.getNumLines(editString);
		int numColumns = StringUtil.getMaxColumns(editString);

		IFigure figure = hostEditPart.getFigure();
		Rectangle bounds = figure.getBounds().getCopy();
		DiagramRootEditPart diagramEditPart = (DiagramRootEditPart) hostEditPart.getRoot();
		IFigure contentPane = diagramEditPart.getContentPane();
		contentPane.translateToAbsolute(bounds);
		EditPartViewer viewer = hostEditPart.getViewer();
		Control control = viewer.getControl();
		while (control != null && false == control instanceof Shell) {
			bounds.translate(control.getBounds().x, control.getBounds().y);
			control = control.getParent();
		}

		Font font = figure.getFont();
		FontData fontData = font.getFontData()[0];
		int fontHeightInPixel = fontData.getHeight();

		int width = Math.max(fontHeightInPixel * (numColumns + 3), MIN_EDITOR_WIDTH);
		int height = Math.max(fontHeightInPixel * (numLines + 4), MIN_EDITOR_HEIGHT);
		xtextEditorComposite.setBounds(bounds.x - 200, bounds.y - 120, width, height);
		xtextEditorComposite.setBounds(bounds.x - 200, bounds.y - 120, width + 250, height + 50);
	}

}
