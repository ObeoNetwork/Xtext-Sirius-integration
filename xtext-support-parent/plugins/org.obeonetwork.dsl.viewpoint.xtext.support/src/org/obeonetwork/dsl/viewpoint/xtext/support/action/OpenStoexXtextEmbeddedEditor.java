package org.obeonetwork.dsl.viewpoint.xtext.support.action;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.obeonetwork.dsl.viewpoint.xtext.support.StoexXtextEmbeddedEditor;

import com.google.inject.Injector;
import de.uka.ipd.sdq.stoex.RandomVariable;

public abstract class OpenStoexXtextEmbeddedEditor extends OpenXtextEmbeddedEditor {

	protected void openEmbeddedEditor(IGraphicalEditPart graphicalEditPart, RandomVariable randomVariable) {
		StoexXtextEmbeddedEditor embeddedEditor = new StoexXtextEmbeddedEditor(graphicalEditPart, getInjector());
		embeddedEditor.showEditor((EObject) randomVariable);
	}
	
	protected abstract Injector getInjector();
	
}
