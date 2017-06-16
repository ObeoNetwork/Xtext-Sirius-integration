package org.eclipse.xtext.example.fowlerdsl.ui.findrefs;

import java.lang.reflect.Field;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.findReferences.IReferenceFinder;
import org.eclipse.xtext.resource.IFragmentProvider;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.findrefs.DelegatingReferenceFinder;
import org.eclipse.xtext.ui.editor.findrefs.ReferenceAcceptor;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class StatemachineReferenceFinder extends DelegatingReferenceFinder {
  public static class StatemachineReferenceAcceptor extends ReferenceAcceptor {
    protected StatemachineReferenceAcceptor(final IAcceptor<IReferenceDescription> delegate, final IResourceServiceProvider.Registry resourceServiceProviderRegistry) {
      super(delegate, resourceServiceProviderRegistry);
    }
    
    @Override
    public void accept(final EObject source, final URI sourceURI, final EReference eReference, final int index, final EObject targetOrProxy, final URI targetURI) {
      super.accept(source, sourceURI, eReference, index, targetOrProxy, targetURI);
      this.accept(this.createReferenceDescription(this.getFallbackURI(source), targetURI, eReference, index, this.findExportedContainer(source)));
    }
    
    protected URI getFallbackURI(final EObject object) {
      try {
        final Resource resource = object.eResource();
        if ((resource instanceof XtextResource)) {
          final Field field = XtextResource.class.getField("fragmentProviderFallback");
          field.setAccessible(true);
          Object _get = field.get(resource);
          final IFragmentProvider.Fallback fragmentProviderFallback = ((IFragmentProvider.Fallback) _get);
          final String fragment = fragmentProviderFallback.getFragment(object);
          final URI resourceURI = EcoreUtil2.getPlatformResourceOrNormalizedURI(resource);
          return resourceURI.appendFragment(fragment);
        } else {
          return EcoreUtil2.getPlatformResourceOrNormalizedURI(object);
        }
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    }
  }
  
  @Override
  protected IReferenceFinder.Acceptor toAcceptor(final IAcceptor<IReferenceDescription> acceptor) {
    IResourceServiceProvider.Registry _resourceServiceProviderRegistry = this.getResourceServiceProviderRegistry();
    return new StatemachineReferenceFinder.StatemachineReferenceAcceptor(acceptor, _resourceServiceProviderRegistry);
  }
}
