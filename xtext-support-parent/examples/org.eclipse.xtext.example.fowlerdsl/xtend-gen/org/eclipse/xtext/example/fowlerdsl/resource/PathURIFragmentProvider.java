/**
 * Copyright (c) 2017 TypeFox and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.example.fowlerdsl.resource;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.SegmentSequence;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.xtext.EcoreUtil2;

/**
 * Calculates the standard EMF path URI fragment
 * 
 * @author koehnlein
 */
@SuppressWarnings("all")
public class PathURIFragmentProvider {
  /**
   * inspired by {@link ResourceImpl#getURIFragment()}
   */
  public static String getPathURIFragment(final EObject element) {
    final SegmentSequence.Builder builder = SegmentSequence.newBuilder("/");
    boolean isContained = false;
    InternalEObject internalEObject = ((InternalEObject) element);
    for (InternalEObject container = internalEObject.eInternalContainer(); ((container != null) || isContained); container = internalEObject.eInternalContainer()) {
      {
        builder.append(container.eURIFragmentSegment(internalEObject.eContainingFeature(), internalEObject));
        internalEObject = container;
        Resource.Internal _eDirectResource = container.eDirectResource();
        boolean _tripleNotEquals = (_eDirectResource != null);
        if (_tripleNotEquals) {
          isContained = true;
        }
      }
    }
    if ((!isContained)) {
      return "/-1";
    }
    builder.append(PathURIFragmentProvider.getURIFragmentRootSegment(internalEObject));
    builder.append("");
    builder.reverse();
    return builder.toSegmentSequence().toString();
  }
  
  public static URI getPathURI(final EObject object) {
    final Resource resource = object.eResource();
    if ((resource != null)) {
      final String fragment = PathURIFragmentProvider.getPathURIFragment(object);
      final URI resourceURI = EcoreUtil2.getPlatformResourceOrNormalizedURI(resource);
      return resourceURI.appendFragment(fragment);
    } else {
      return EcoreUtil2.getPlatformResourceOrNormalizedURI(object);
    }
  }
  
  protected static String getURIFragmentRootSegment(final EObject eObject) {
    EList<EObject> contents = eObject.eResource().getContents();
    int _size = contents.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      return Integer.toString(contents.indexOf(eObject));
    } else {
      return "";
    }
  }
}
