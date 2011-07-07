Xtext Viewpoint integration plug-in
-----------------------------------

### Features

This plug-in provides support to integrate an Xtext editor inside a diagram representation created with Viewpoint.  


### Requires

- OD 5.0.4
- Xtext 2.0

### How to 

- Create a tool in your modeler, and in the model operation, add a Java Launch operation and set OpenEmbeddedEditor as id.
- Extends OpenXtextEmbeddedEditor to bind your Xtext model. 
```java
import org.eclipse.xtext.example.domainmodel.ui.internal.DomainmodelActivator;
import com.google.inject.Injector;
import fr.obeo.dsl.viewpoint.xtext.support.action.OpenXtextEmbeddedEditor;

public class OpenEmbeddedEditor extends OpenXtextEmbeddedEditor {
	@Override
	protected Injector getInjector() {
		return  DomainmodelActivator.getInstance().getInjector("org.eclipse.xtext.example.domainmodel.Domainmodel");
	}
}
```
- register your java action
```xml
   <extension point="fr.obeo.dsl.viewpoint.externalJavaAction">
      <javaActions
            actionClass="com.yourcompany.OpenEmbeddedEditor"
            id="OpenEmbeddedEditor">
      </javaActions>
   </extension>
```


### Known limitation