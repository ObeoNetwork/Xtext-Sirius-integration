Xtext Sirius Embedded Editor Example
------------------------------------

This plug-in contains a basic framework easing the embedding of an Xtext editor inside a diagram representation created with Sirius.  
This is not needed at all to load Xtext models and create diagrams on top of those, this functionnaliy is provided by the Sirius project itself in its update-site.


### Features

![Screenshot of diagram without embedded text editor](http://github.com/ObeoNetwork/Xtext-Sirius-integration/raw/master/screenshot-diagram+text.png "Screenshot without the text editor")


![Screenshot of diagram with embedded text editor](http://github.com/ObeoNetwork/Xtext-Sirius-integration/raw/master/screenshot-diagram.png "Screenshot with the text editor")



### Requires

- Eclipse Sirius 1.x
- Xtext 2.x

### How to 

- Create a tool in your modeler, and in the model operation, add an _External Java Action Call_ and set _OpenEmbeddedEditor_ as id.
- Extends _OpenXtextEmbeddedEditor_ to bind your Xtext model.

```java
import org.eclipse.xtext.example.domainmodel.ui.internal.DomainmodelActivator;
import com.google.inject.Injector;
import org.obeonetwork.dsl.viewpoint.xtext.support.action.OpenXtextEmbeddedEditor;

public class OpenEmbeddedEditor extends OpenXtextEmbeddedEditor {
	@Override
	protected Injector getInjector() {
		return  DomainmodelActivator.getInstance().getInjector("org.eclipse.xtext.example.domainmodel.Domainmodel");
	}
}
```

- register your java action

```xml
   <extension point="org.eclipse.sirius.externalJavaAction">
      <javaActions
            actionClass="com.yourcompany.OpenEmbeddedEditor"
            id="OpenEmbeddedEditor">
      </javaActions>
   </extension>
```


### Known limitation
