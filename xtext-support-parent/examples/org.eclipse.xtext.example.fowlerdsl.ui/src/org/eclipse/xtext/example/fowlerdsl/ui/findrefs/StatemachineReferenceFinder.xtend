/*******************************************************************************
 * Copyright (c) 2017 TypeFox and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package  org.eclipse.xtext.example.fowlerdsl.ui.findrefs

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.resource.IReferenceDescription
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry
import org.eclipse.xtext.ui.editor.findrefs.DelegatingReferenceFinder
import org.eclipse.xtext.ui.editor.findrefs.ReferenceAcceptor
import org.eclipse.xtext.util.IAcceptor

import static extension org.eclipse.xtext.example.fowlerdsl.resource.PathURIFragmentProvider.*

class StatemachineReferenceFinder extends DelegatingReferenceFinder {
		
	override protected toAcceptor(IAcceptor<IReferenceDescription> acceptor) {
		new StatemachineReferenceAcceptor(acceptor, resourceServiceProviderRegistry)
	}
	
	static class StatemachineReferenceAcceptor extends ReferenceAcceptor {
	
		protected new(IAcceptor<IReferenceDescription> delegate, Registry resourceServiceProviderRegistry) {
			super(delegate, resourceServiceProviderRegistry)
		}
	
		override void accept(EObject source, URI sourceURI, EReference eReference, int index, EObject targetOrProxy, URI targetURI) {
			super.accept(source, sourceURI, eReference, index, targetOrProxy, targetURI)
			this.accept(this.createReferenceDescription(source.pathURI, targetURI, eReference, index, this.findExportedContainer(source))) 
		}	
	}
}
