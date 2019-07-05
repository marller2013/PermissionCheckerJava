package com.marller.compiler;

import com.google.auto.service.AutoService;
import com.marller.api.OnNeed;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;


@AutoService(Processor.class)
public class PermissionProcessor extends AbstractProcessor {

    private ProcessingEnvironment processingEnvironment;
    HashMap<Element, List<Element>> annotationCache=new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment=processingEnvironment;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> onNeeds=roundEnvironment.getElementsAnnotatedWith(OnNeed.class);
        for (Element mElement:onNeeds) {
            if (mElement instanceof ExecutableElement) {
                annotationCache.put(mElement.getEnclosingElement(),mElement.);
            }
        }
//        for (Map.Entry<>:
//             ) {
//
//        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(OnNeed.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
