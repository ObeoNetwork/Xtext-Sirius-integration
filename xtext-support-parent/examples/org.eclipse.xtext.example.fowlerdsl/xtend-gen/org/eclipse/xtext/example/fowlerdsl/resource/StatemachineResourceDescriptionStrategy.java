/**
 * Copyright (c) 2017 TypeFox and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.example.fowlerdsl.resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.example.fowlerdsl.resource.PathURIFragmentProvider;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;

@SuppressWarnings("all")
public class StatemachineResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {
  @Override
  protected IReferenceDescription createReferenceDescription(final EObject owner, final URI exportedContainerURI, final EReference eReference, final int indexInList, final EObject target) {
    final URI ownerURI = PathURIFragmentProvider.getPathURI(owner);
    final URI targetURI = PathURIFragmentProvider.getPathURI(target);
    return new DefaultReferenceDescription(ownerURI, targetURI, eReference, indexInList, exportedContainerURI);
  }
}
