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

package org.obeonetwork.dsl.viewpoint.xtext.support.action;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.sirius.tools.api.ui.IExternalJavaAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.obeonetwork.dsl.viewpoint.xtext.support.XtextEmbeddedEditor;

import com.google.inject.Injector;

public abstract class OpenXtextEmbeddedEditor implements IExternalJavaAction {

	public boolean canExecute(Collection<? extends EObject> arg0) {
		return true;
	}

	public void execute(Collection<? extends EObject> context,
			Map<String, Object> parameters) {
		DiagramEditPart diagramEditPart = ((DiagramEditor) getActiveEditor())
				.getDiagramEditPart();
		for (EObject o : context) {
			EditPart editPart = diagramEditPart
					.findEditPart(diagramEditPart, o);
			if (editPart != null && (editPart instanceof IGraphicalEditPart)) {
				openEmbeddedEditor((IGraphicalEditPart) editPart);
				break;
			}
		}

	}
	
	protected  void openEmbeddedEditor(IGraphicalEditPart graphicalEditPart) {
			XtextEmbeddedEditor embeddedEditor = new XtextEmbeddedEditor(graphicalEditPart, getInjector());
			embeddedEditor.showEditor();	
	}
	
	/**
	 * Return the injector associated to you domain model plug-in. 
	 * @return
	 */
	protected abstract Injector getInjector();
	
	protected IEditorPart getActiveEditor() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();
		return page.getActiveEditor();
	}

}
