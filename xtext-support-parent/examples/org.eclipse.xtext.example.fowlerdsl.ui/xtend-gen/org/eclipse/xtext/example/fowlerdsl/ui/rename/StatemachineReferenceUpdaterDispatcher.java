/**
 * Copyright (c) 2017 TypeFox and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.example.fowlerdsl.ui.rename;

import com.google.inject.Inject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.example.fowlerdsl.resource.PathURIFragmentProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription;
import org.eclipse.xtext.ui.refactoring.IRefactoringUpdateAcceptor;
import org.eclipse.xtext.ui.refactoring.impl.ReferenceUpdaterDispatcher;
import org.eclipse.xtext.ui.refactoring.impl.StatusWrapper;

@SuppressWarnings("all")
public class StatemachineReferenceUpdaterDispatcher extends ReferenceUpdaterDispatcher {
  public static class StatemachineReferenceDescriptionAcceptor extends ReferenceUpdaterDispatcher.ReferenceDescriptionAcceptor {
    public StatemachineReferenceDescriptionAcceptor(final IResourceServiceProvider.Registry resourceServiceProviderRegistry, final StatusWrapper status) {
      super(resourceServiceProviderRegistry, status);
    }
    
    @Override
    public void accept(final EObject source, final URI sourceURI, final EReference eReference, final int index, final EObject targetOrProxy, final URI targetURI) {
      URI _pathURI = PathURIFragmentProvider.getPathURI(source);
      DefaultReferenceDescription _defaultReferenceDescription = new DefaultReferenceDescription(_pathURI, targetURI, eReference, index, null);
      this.accept(_defaultReferenceDescription);
    }
  }
  
  @Inject
  private IResourceServiceProvider.Registry resourceServiceProviderRegistry;
  
  @Override
  protected ReferenceUpdaterDispatcher.ReferenceDescriptionAcceptor createFindReferenceAcceptor(final IRefactoringUpdateAcceptor updateAcceptor) {
    StatusWrapper _refactoringStatus = updateAcceptor.getRefactoringStatus();
    return new StatemachineReferenceUpdaterDispatcher.StatemachineReferenceDescriptionAcceptor(this.resourceServiceProviderRegistry, _refactoringStatus);
  }
}
