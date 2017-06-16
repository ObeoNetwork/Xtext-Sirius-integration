/*******************************************************************************
 * Copyright (c) 2017 TypeFox and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.example.fowlerdsl.resource

import org.eclipse.emf.common.util.SegmentSequence
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.InternalEObject
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.xtext.EcoreUtil2

/**
 * Calculates the standard EMF path URI fragment
 * 
 * @author koehnlein
 */
class PathURIFragmentProvider {

	/** 
	 * inspired by {@link ResourceImpl#getURIFragment()}
	 */
	static def String getPathURIFragment(EObject element) {
		val builder = SegmentSequence.newBuilder("/")
		var isContained = false
		var internalEObject = element as InternalEObject
		for (var container = internalEObject.eInternalContainer(); 
			container !== null && !isContained; 
			container = internalEObject.eInternalContainer()) {
			builder.append(container.eURIFragmentSegment(internalEObject.eContainingFeature(), internalEObject))
			internalEObject = container
			if (container.eDirectResource() !== null) {
				isContained = true
			}
		}
		if (!isContained) {
			return "/-1"
		}
		builder.append(getURIFragmentRootSegment(internalEObject))
		builder.append("")
		builder.reverse()
		return builder.toSegmentSequence().toString()
	}

	static def URI getPathURI(EObject object) {
		val resource = object.eResource
		if(resource !== null) {
			val fragment = object.pathURIFragment
			val resourceURI = EcoreUtil2.getPlatformResourceOrNormalizedURI(resource)
			return resourceURI.appendFragment(fragment)
		} else {
			return EcoreUtil2.getPlatformResourceOrNormalizedURI(object)
		}
	}

	protected static def String getURIFragmentRootSegment(EObject eObject) {
		var contents = eObject.eResource.contents
		if (contents.size > 1)
			return Integer.toString(contents.indexOf(eObject))
		else
			return ''
	}
}
