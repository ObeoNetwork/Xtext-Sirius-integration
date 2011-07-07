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

package fr.obeo.dsl.viewpoint.xtext.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.diff.merge.service.MergeService;
import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramEditDomain;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;
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

import fr.obeo.dsl.viewpoint.DSemanticDecorator;
import fr.obeo.dsl.viewpoint.ViewpointPackage;

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
		try {
			EObject originalSemanticElement = resolveSemanticElement(hostEditPart);
			if (originalSemanticElement == null) {
				return;
			}
			this.originalResource = originalSemanticElement.eResource();
			// Clone root EObject
			EObject semanticElement = EcoreUtil2.clone(originalResource.getContents().get(0));
			this.xtextResource = createVirtualXtextResource(semanticElement);

			// TODO manage multi resource with Xtext Linking or Scoping service
			semanticElementFragment = originalResource.getURIFragment(originalSemanticElement);
			if (semanticElementFragment == null || "".equals(semanticElementFragment)) {
				return;
			}
			IDiagramEditDomain diagramEditDomain = hostEditPart.getDiagramEditDomain();
			diagramEditor = ((DiagramEditDomain) diagramEditDomain).getEditorPart();
			createXtextEditor(new ResourceWorkingCopyFileEditorInput(xtextResource));
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
	 * @param isReconcile
	 */
	public void closeEditor(boolean isReconcile) {
		if (xtextEditor != null) {
			if (isReconcile) {
				try {
					final IXtextDocument xtextDocument = xtextEditor.getDocument();
					if (!isDocumentHasErrors(xtextDocument)) {
						// subtract 2 for the artificial newlines
						int documentGrowth = xtextDocument.getLength() - initialDocumentSize - 2;
						String newText = xtextDocument.get(editorOffset + 1, initialEditorSize + documentGrowth);
						updateXtextResource(editorOffset, initialEditorSize, newText);
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

	private XtextResource createVirtualXtextResource(EObject semanticElement) throws IOException {
		IResourceFactory resourceFactory = xtextInjector.getInstance(IResourceFactory.class);
		XtextResourceSet rs = xtextInjector.getInstance(XtextResourceSet.class);
		rs.setClasspathURIContext(getClass());
		URI uri = createURI();
		// Create virtual resource
		XtextResource xtextVirtualResource = (XtextResource) resourceFactory.createResource(uri);
		rs.getResources().add(xtextVirtualResource);

		// Populate virtual resource with the given semantic element to edit
		xtextVirtualResource.getContents().add(semanticElement);
		//save to create file
		//TODO remove crappy hack
		xtextVirtualResource.save(Collections.emptyMap());
		
		// Save and reparse in order to initialize virtual Xtext resource
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		xtextVirtualResource.save(out, Collections.emptyMap());
		xtextVirtualResource.reparse(new String(out.toByteArray()));				
		
		return xtextVirtualResource;
	}

	private URI createURI() {
		// FIXME remove crappy code and use in memory resource
		// URI uri = URI.createURI("XtextResource.ram");
		final String TEMPORARY_PROJECT_NAME = "tempProject";
		final IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(TEMPORARY_PROJECT_NAME);
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(TEMPORARY_PROJECT_NAME);
		try {
			if (!project.exists()){
				project.create(projectDescription, new NullProgressMonitor());
				project.open(new NullProgressMonitor());
			}
		} catch (final CoreException e) {
			// Propagate as runtime exception
			throw new RuntimeException(e);
		}
		URI uri = URI.createPlatformResourceURI("/" + TEMPORARY_PROJECT_NAME + "/temp.dmodel", false);
		return uri;
	}

	protected void updateXtextResource(final int offset, final int replacedTextLength, final String newText) throws IOException, BadLocationException {
		ICompositeNode oldRootNode = xtextResource.getParseResult().getRootNode();
		// final IParseResult parseResult =
		// xtextResource.getParser().reparse(oldRootNode, offset,
		// Reparse the entire document
		String originalRegion = NodeModelUtils.getTokenText(oldRootNode);
		int changeOffset = offset - oldRootNode.getTotalOffset();
		StringBuffer reparseRegion = new StringBuffer();
		reparseRegion.append(originalRegion.substring(0, changeOffset));
		reparseRegion.append(newText);
		if (changeOffset + replacedTextLength < originalRegion.length())
			reparseRegion.append(originalRegion.substring(changeOffset + replacedTextLength));
		String allText = reparseRegion.toString();
		xtextResource.reparse(allText);
		final IParseResult parseResult = xtextResource.getParseResult();
		if (!parseResult.hasSyntaxErrors()) {
			applyChanges(originalResource.getContents().get(0), parseResult.getRootASTElement());
		}
	}

	private List<DiffElement> getChanges(EObject oldRootObject, EObject newRootObject) throws InterruptedException {
		// use EMF compare in order to find changes
		// between oldRootObject tree and newRootObject tree
		Map<String, Object> options = new HashMap<String, Object>();
		MatchModel matchModel = MatchService.doMatch(newRootObject, oldRootObject, options);
		DiffModel diffModel = DiffService.doDiff(matchModel);
		List<DiffElement> changes = collectChanges(diffModel);
		return changes;
	}

	private List<DiffElement> collectChanges(DiffModel diffModel) {
		ArrayList<DiffElement> changes = new ArrayList<DiffElement>();
		for (DiffElement diffElement : diffModel.getOwnedElements()) {
			collectChanges(changes, diffElement);
		}
		return changes;
	}

	private void collectChanges(List<DiffElement> changes, DiffElement diffElement) {
		if (diffElement.getSubDiffElements().size() == 0 && !(diffElement instanceof DiffGroup)) {
			changes.add(diffElement);
		} else {
			for (DiffElement elem : diffElement.getSubDiffElements()) {
				collectChanges(changes, elem);
			}
		}
	}

	private void applyChanges(EObject oldRootObject, EObject newRootObject) {
		try {
			final List<DiffElement> changes = getChanges(oldRootObject, newRootObject);
			final TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(originalResource);
			AbstractTransactionalCommand gmfCommand = new AbstractTransactionalCommand(editingDomain, "update resource after direct text edit", null) {
				@Override
				protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					try {
						for (DiffElement change : changes) {
							System.out.println("## change: " + change);
							
							MergeService.merge(change, true);
						}
						return CommandResult.newOKCommandResult();
					} catch (Exception exc) {
						return CommandResult.newErrorCommandResult(exc);
					}
				}
			};
			gmfCommand.execute(new NullProgressMonitor(), null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Create the XText editor
	 * @param editorInput the editor input
	 * @throws Exception
	 */
	protected void createXtextEditor(IEditorInput editorInput) throws Exception {
		DiagramRootEditPart diagramEditPart = (DiagramRootEditPart) hostEditPart.getRoot();
		Composite parentComposite = (Composite) diagramEditPart.getViewer().getControl();
		xtextEditorComposite = new Decorations(parentComposite, SWT.RESIZE | SWT.ON_TOP | SWT.BORDER);
		xtextEditorComposite.setLayout(new FillLayout());
		IEditorSite editorSite = diagramEditor.getEditorSite();
		this.xtextEditor = xtextInjector.getInstance(XtextEditor.class);
		// remove dirty state editor callback
		xtextEditor.setXtextEditorCallback(new CompoundXtextEditorCallback(Guice.createInjector(new Module() {
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
				if ((e.stateMask & SWT.CTRL) != 0 && ((e.keyCode == SWT.KEYPAD_CR) || (e.keyCode == SWT.CR))) {
					e.doit = false;
				}
			}
		});
		xtextTextWidget.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.keyCode;
				if ((e.stateMask & SWT.CTRL) != 0 && ((keyCode == SWT.KEYPAD_CR) || (keyCode == SWT.CR))) {
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
		boolean success = xtextEditor.getDocument().modify(new IUnitOfWork<Boolean, XtextResource>() {

			public Boolean exec(XtextResource state) throws Exception {
				EObject semanticElementInDocument = state.getEObject(semanticElementFragment);
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
				xtextDocument.replace(editorOffset + 1 + initialEditorSize, 0, "\n");
				return true;
			}

		});
		if (success) {
			xtextEditor.showHighlightRangeOnly(true);
			xtextEditor.setHighlightRange(editorOffset + 1, initialEditorSize, true);
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

	private ICompositeNode getCompositeNode(EObject semanticElement) {
		return NodeModelUtils.getNode(semanticElement);
	}

	private boolean isDocumentHasErrors(final IXtextDocument xtextDocument) {
		return (xtextDocument.readOnly(new IUnitOfWork<Boolean, XtextResource>() {
			public Boolean exec(XtextResource state) throws Exception {
				IParseResult parseResult = state.getParseResult();
				return !state.getErrors().isEmpty() || parseResult == null || parseResult.hasSyntaxErrors();
			}
		}));
	}

}
