/**
 * Copyright (c) 2017 TypeFox and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.example.fowlerdsl.ui.findrefs;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.example.fowlerdsl.resource.PathURIFragmentProvider;
import org.eclipse.xtext.findReferences.IReferenceFinder;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.ui.editor.findrefs.DelegatingReferenceFinder;
import org.eclipse.xtext.ui.editor.findrefs.ReferenceAcceptor;
import org.eclipse.xtext.util.IAcceptor;

@SuppressWarnings("all")
public class StatemachineReferenceFinder extends DelegatingReferenceFinder {
  public static class StatemachineReferenceAcceptor extends ReferenceAcceptor {
    protected StatemachineReferenceAcceptor(final IAcceptor<IReferenceDescription> delegate, final IResourceServiceProvider.Registry resourceServiceProviderRegistry) {
      super(delegate, resourceServiceProviderRegistry);
    }
    
    @Override
    public void accept(final EObject source, final URI sourceURI, final EReference eReference, final int index, final EObject targetOrProxy, final URI targetURI) {
      super.accept(source, sourceURI, eReference, index, targetOrProxy, targetURI);
      this.accept(this.createReferenceDescription(PathURIFragmentProvider.getPathURI(source), targetURI, eReference, index, this.findExportedContainer(source)));
    }
  }
  
  @Override
  protected IReferenceFinder.Acceptor toAcceptor(final IAcceptor<IReferenceDescription> acceptor) {
    IResourceServiceProvider.Registry _resourceServiceProviderRegistry = this.getResourceServiceProviderRegistry();
    return new StatemachineReferenceFinder.StatemachineReferenceAcceptor(acceptor, _resourceServiceProviderRegistry);
  }
}
