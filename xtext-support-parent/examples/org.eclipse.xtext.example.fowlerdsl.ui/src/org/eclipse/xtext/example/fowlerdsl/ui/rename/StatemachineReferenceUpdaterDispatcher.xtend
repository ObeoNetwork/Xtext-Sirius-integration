/*******************************************************************************
 * Copyright (c) 2017 TypeFox and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package org.eclipse.xtext.example.fowlerdsl.ui.rename

import com.google.inject.Inject
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription
import org.eclipse.xtext.ui.refactoring.IRefactoringUpdateAcceptor
import org.eclipse.xtext.ui.refactoring.impl.ReferenceUpdaterDispatcher
import org.eclipse.xtext.ui.refactoring.impl.StatusWrapper

import static extension org.eclipse.xtext.example.fowlerdsl.resource.PathURIFragmentProvider.*

class StatemachineReferenceUpdaterDispatcher extends ReferenceUpdaterDispatcher {
	
	@Inject IResourceServiceProvider.Registry resourceServiceProviderRegistry;
	
	override protected createFindReferenceAcceptor(IRefactoringUpdateAcceptor updateAcceptor) {
		new StatemachineReferenceDescriptionAcceptor(resourceServiceProviderRegistry, updateAcceptor.getRefactoringStatus())
	}

	static class StatemachineReferenceDescriptionAcceptor extends ReferenceDescriptionAcceptor {
	
		new(Registry resourceServiceProviderRegistry, StatusWrapper status) {
			super(resourceServiceProviderRegistry, status)
		}
		
		override void accept(EObject source, URI sourceURI, EReference eReference, int index, EObject targetOrProxy, URI targetURI) {
			accept(new DefaultReferenceDescription(source.pathURI, targetURI, eReference, index, null)) 
		}	
	}
}