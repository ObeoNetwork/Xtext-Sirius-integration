package org.eclipse.xtext.example.fowlerdsl;

import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.example.fowlerdsl.statemachine.State;
import org.eclipse.xtext.example.fowlerdsl.statemachine.Transition;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.naming.SimpleNameProvider;
import org.eclipse.xtext.resource.IFragmentProvider;

import com.google.inject.Inject;

public class StatemachineFragmentProvider implements IFragmentProvider {

	@Inject
	private IQualifiedNameProvider qualifiedNameProvider = new SimpleNameProvider();

	public String getFragment(EObject obj, Fallback fallback) {
		QualifiedName qName = qualifiedNameProvider.getFullyQualifiedName(obj);
		if (obj instanceof Transition && obj.eContainer() instanceof State) {
			String r = ((State) obj.eContainer()).getName();
			if (r == null) {
				r = "";
			}
			r += "-|";
			if (((Transition) obj).getEvent() != null && ((Transition) obj).getEvent().getCode() != null) {
				r += ((Transition) obj).getEvent().getCode();
			}
			r += "|-";
			if (((Transition) obj).getState() != null && ((Transition) obj).getState().getName() != null) {
				r += ((Transition) obj).getState().getName();
			}
			return r;

		} else {

			return qName != null ? qName.toString() : fallback.getFragment(obj);
		}
	}

	public EObject getEObject(Resource resource, String fragment, Fallback fallback) {
		if (fragment != null) {
			Iterator<EObject> i = EcoreUtil.getAllContents(resource, false);
			while (i.hasNext()) {
				EObject eObject = i.next();
				String candidateFragment = (eObject.eIsProxy()) ? ((InternalEObject) eObject).eProxyURI().fragment()
						: getFragment(eObject, fallback);
				if (fragment.equals(candidateFragment))
					return eObject;
			}
		}
		return fallback.getEObject(fragment);
	}
}
