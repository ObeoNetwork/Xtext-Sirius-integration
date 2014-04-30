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
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.sirius.viewpoint.DSemanticDecorator;
import org.eclipse.sirius.viewpoint.ViewpointPackage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.CompoundXtextEditorCallback;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.info.ResourceWorkingCopyFileEditorInput;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/* largely inspired by "org.eclipse.xtext.gmf.glue" from XText examples */
public class XtextEmbeddedEditor {

	private static int MIN_EDITOR_WIDTH = 100;

	private static int MIN_EDITOR_HEIGHT = 20;

	private IGraphicalEditPart hostEditPart;

	private IEditorPart diagramEditor;

	private XtextEditor xtextEditor;

	private int editorOffset;

	private int initialEditorSize;

	private int initialDocumentSize;

	private Composite xtextEditorComposite;

	private final Injector xtextInjector;

	private Resource originalResource;

	private XtextResource xtextResource;

	private String semanticElementFragment;

	public XtextEmbeddedEditor(IGraphicalEditPart editPart,
			Injector xtextInjector) {
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
		return ViewpointPackage.eINSTANCE.getDSemanticDecorator().isInstance(
				eObject);
	}

	public void showEditor() {
		try {
			EObject originalSemanticElement = resolveSemanticElement(hostEditPart);
			if (originalSemanticElement == null) {
				return;
			}
			this.originalResource = originalSemanticElement.eResource();
			// Clone root EObject
			EObject semanticElement = EcoreUtil.copy(originalResource
					.getContents().get(0));
			this.xtextResource = createVirtualXtextResource(
					originalResource.getURI(), semanticElement);

			// TODO manage multi resource with Xtext Linking or Scoping service
			semanticElementFragment = originalResource
					.getURIFragment(originalSemanticElement);
			if (semanticElementFragment == null
					|| "".equals(semanticElementFragment)) {
				return;
			}
			IDiagramEditDomain diagramEditDomain = hostEditPart
					.getDiagramEditDomain();
			diagramEditor = ((DiagramEditDomain) diagramEditDomain)
					.getEditorPart();
			createXtextEditor(new ResourceWorkingCopyFileEditorInput(
					xtextResource));
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
		if (xtextEditor != null) {
			if (isReconcile) {
				try {
					final IXtextDocument xtextDocument = xtextEditor
							.getDocument();
					if (!isDocumentHasErrors(xtextDocument)) {
						// subtract 2 for the artificial newlines
						int documentGrowth = xtextDocument.getLength()
								- initialDocumentSize - 2;
						String newText = xtextDocument.get(editorOffset + 1,
								initialEditorSize + documentGrowth);
						updateXtextResource(editorOffset, initialEditorSize,
								newText);
					}
				} catch (Exception exc) {
					Activator.logError(exc);
				}
			}
			xtextEditor.dispose();
			xtextEditorComposite.setVisible(false);
			xtextEditorComposite.dispose();
			xtextEditor = null;
		}
	}

	private XtextResource createVirtualXtextResource(URI uri,
			EObject semanticElement) throws IOException {
		IResourceFactory resourceFactory = xtextInjector
				.getInstance(IResourceFactory.class);
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


	protected void updateXtextResource(final int offset,
			final int replacedTextLength, final String newText)
			throws IOException, BadLocationException {
		ICompositeNode oldRootNode = xtextResource.getParseResult()
				.getRootNode();
		// final IParseResult parseResult =
		// xtextResource.getParser().reparse(oldRootNode, offset,
		// Reparse the entire document
		String originalRegion = NodeModelUtils.getTokenText(oldRootNode);
		int changeOffset = offset - oldRootNode.getTotalOffset();
		StringBuffer reparseRegion = new StringBuffer();
		reparseRegion.append(originalRegion.substring(0, changeOffset));
		reparseRegion.append(newText);
		if (changeOffset + replacedTextLength < originalRegion.length())
			reparseRegion.append(originalRegion.substring(changeOffset
					+ replacedTextLength));
		String allText = reparseRegion.toString();
		xtextResource.reparse(allText);
		EcoreUtil.resolveAll(xtextResource);
		final IParseResult parseResult = xtextResource.getParseResult();
		if (!parseResult.hasSyntaxErrors()) {
			reconcile(originalResource, xtextResource);
		}
	}

	private void reconcile(Resource originalResource2,
			XtextResource xtextResource2) {
		try {

			IComparisonScope scope = new DefaultComparisonScope(
					originalResource2, xtextResource2, null);
			final Comparison comparison = EMFCompare.builder().build()
					.compare(scope);

			IMerger.Registry mergerRegistry = EMFCompareRCPPlugin.getDefault()
					.getMergerRegistry();
			final IBatchMerger merger = new BatchMerger(mergerRegistry);

			final TransactionalEditingDomain editingDomain = TransactionUtil
					.getEditingDomain(originalResource);
			editingDomain.getCommandStack().execute(
					new RecordingCommand(editingDomain,
							"update resource after direct text edit") {

						@Override
						protected void doExecute() {
							System.out.println("merging "+ comparison.getDifferences().size() + " differences");
							merger.copyAllRightToLeft(
									comparison.getDifferences(),
									new BasicMonitor());
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
	protected void createXtextEditor(IEditorInput editorInput) throws Exception {
		DiagramRootEditPart diagramEditPart = (DiagramRootEditPart) hostEditPart
				.getRoot();
		Composite parentComposite = (Composite) diagramEditPart.getViewer()
				.getControl();
		xtextEditorComposite = new Decorations(parentComposite, SWT.RESIZE
				| SWT.ON_TOP | SWT.BORDER);
		xtextEditorComposite.setLayout(new FillLayout());
		IEditorSite editorSite = diagramEditor.getEditorSite();
		this.xtextEditor = xtextInjector.getInstance(XtextEditor.class);
		// remove dirty state editor callback
		xtextEditor.setXtextEditorCallback(new CompoundXtextEditorCallback(
				Guice.createInjector(new Module() {
					public void configure(Binder binder) {
					}
				})));
		xtextEditor.init(editorSite, editorInput);
		xtextEditor.createPartControl(xtextEditorComposite);
		addKeyVerifyListener();
		setEditorRegion();
		setEditorBounds();
		xtextEditorComposite.setVisible(true);
		xtextEditorComposite.forceFocus();
		xtextEditor.setFocus();
	}

	private void addKeyVerifyListener() {
		ISourceViewer sourceViewer = xtextEditor.getInternalSourceViewer();
		final StyledText xtextTextWidget = sourceViewer.getTextWidget();
		xtextTextWidget.addVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent e) {
				if ((e.stateMask & SWT.CTRL) != 0
						&& ((e.keyCode == SWT.KEYPAD_CR) || (e.keyCode == SWT.CR))) {
					e.doit = false;
				}
			}
		});
		xtextTextWidget.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.keyCode;
				if ((e.stateMask & SWT.CTRL) != 0
						&& ((keyCode == SWT.KEYPAD_CR) || (keyCode == SWT.CR))) {
					closeEditor(true);
				}
				if (keyCode == SWT.ESC) {
					closeEditor(true);
				}
			}
		});
	}

	private void setEditorRegion() throws BadLocationException {
		final IXtextDocument xtextDocument = xtextEditor.getDocument();
		boolean success = xtextEditor.getDocument().modify(
				new IUnitOfWork<Boolean, XtextResource>() {

					public Boolean exec(XtextResource state) throws Exception {
						EObject semanticElementInDocument = state
								.getEObject(semanticElementFragment);
						if (semanticElementInDocument == null) {
							return false;
						}
						ICompositeNode xtextNode = getCompositeNode(semanticElementInDocument);
						if (xtextNode == null) {
							return false;
						}
						// getOffset() and getLength() are trimming whitespaces
						editorOffset = xtextNode.getOffset();
						initialEditorSize = xtextNode.getLength();
						initialDocumentSize = xtextDocument.getLength();

						// insert a newline directly before and after the node
						xtextDocument.replace(editorOffset, 0, "\n");
						xtextDocument.replace(editorOffset + 1
								+ initialEditorSize, 0, "\n");
						return true;
					}

				});
		if (success) {
			xtextEditor.showHighlightRangeOnly(true);
			xtextEditor.setHighlightRange(editorOffset + 1, initialEditorSize,
					true);
			xtextEditor.setFocus();
		}
	}

	private void setEditorBounds() {
		final IXtextDocument xtextDocument = xtextEditor.getDocument();
		// mind the added newlines
		String editString = "";
		try {
			editString = xtextDocument.get(editorOffset + 1, initialEditorSize);
		} catch (BadLocationException exc) {
			Activator.logError(exc);
		}
		int numLines = StringUtil.getNumLines(editString);
		int numColumns = StringUtil.getMaxColumns(editString);

		IFigure figure = hostEditPart.getFigure();
		Rectangle bounds = figure.getBounds().getCopy();
		DiagramRootEditPart diagramEditPart = (DiagramRootEditPart) hostEditPart
				.getRoot();
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

		int width = Math.max(fontHeightInPixel * (numColumns + 3),
				MIN_EDITOR_WIDTH);
		int height = Math.max(fontHeightInPixel * (numLines + 4),
				MIN_EDITOR_HEIGHT);
		xtextEditorComposite.setBounds(bounds.x - 200, bounds.y - 120, width,
				height);
		xtextEditorComposite.setBounds(bounds.x - 200, bounds.y - 120,
				width + 250, height + 50);
	}

	private ICompositeNode getCompositeNode(EObject semanticElement) {
		return NodeModelUtils.getNode(semanticElement);
	}

	private boolean isDocumentHasErrors(final IXtextDocument xtextDocument) {
		return (xtextDocument
				.readOnly(new IUnitOfWork<Boolean, XtextResource>() {
					public Boolean exec(XtextResource state) throws Exception {
						IParseResult parseResult = state.getParseResult();
						return !state.getErrors().isEmpty()
								|| parseResult == null
								|| parseResult.hasSyntaxErrors();
					}
				}));
	}

}
