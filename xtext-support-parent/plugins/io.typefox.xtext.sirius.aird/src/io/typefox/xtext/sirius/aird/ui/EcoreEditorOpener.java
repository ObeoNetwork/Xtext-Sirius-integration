/*******************************************************************************
 * Copyright (c) 2017 TypeFox and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package io.typefox.xtext.sirius.aird.ui;

import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.xtext.ui.editor.LanguageSpecificURIEditorOpener;

/**
 * @author koehnlein
 */
public class EcoreEditorOpener extends LanguageSpecificURIEditorOpener {
	
	@Override
	protected void selectAndReveal(IEditorPart openEditor, URI uri,
			EReference crossReference, int indexInList, boolean select) {
		if (uri.fragment() != null) {
			EcoreEditor ecoreEditor = (EcoreEditor) openEditor.getAdapter(EcoreEditor.class);
			if (ecoreEditor != null) {
				EObject eObject = ecoreEditor.getEditingDomain().getResourceSet().getEObject(uri, true);
				ecoreEditor.setSelectionToViewer(Collections.singletonList(eObject));
			}
		}
	}
	
	@Override
	protected String getEditorId() {
		return "fr.obeo.dsl.designer.sample.flow.presentation.FlowEditorID";
	}

}
