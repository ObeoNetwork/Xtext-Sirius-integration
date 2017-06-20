/*******************************************************************************
 * Copyright (c) 2017 TypeFox and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package io.typefox.xtext.sirius.aird;

import org.eclipse.xtext.resource.generic.AbstractGenericResourceRuntimeModule;
import org.eclipse.xtext.validation.IResourceValidator;

/**
 * The DI module to configure the Xtext language services for aird models.
 *  
 * @author koehnlein
 */
public class AirdRuntimeModule extends AbstractGenericResourceRuntimeModule {

	@Override
	protected String getLanguageName() {
		return "io.typefox.xtext.sirius.aird";
	}

	@Override
	protected String getFileExtensions() {
		return "aird";
	}
	
	public IResourceValidator bindIResourceValidator() {
		return IResourceValidator.NULL;
	}
}
