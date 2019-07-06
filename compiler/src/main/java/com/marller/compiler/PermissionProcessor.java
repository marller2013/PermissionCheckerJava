package com.marller.compiler;

import com.google.auto.service.AutoService;
import com.marller.api.OnNeed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    HashMap<Element, ExecutableElement> annotationCache = new HashMap<>();
    private List<Element> enclosingElements = new ArrayList<>();
    private List<ExecutableElement> realElements = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment=processingEnvironment;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> onNeeds=roundEnvironment.getElementsAnnotatedWith(OnNeed.class);
        if (null == onNeeds || onNeeds.size() <= 0) {
            return true;
        }
        System.out.println("检测出来使用OnNeed注解的元素个数-------->" + onNeeds.size());
        for (Element mElement:onNeeds) {
            if (mElement instanceof ExecutableElement) {
                if (!enclosingElements.contains(mElement.getEnclosingElement())) {
                    enclosingElements.add(mElement.getEnclosingElement());
                }
                realElements.add((ExecutableElement) mElement);
            }
        }
        for (Element mElement : enclosingElements) {
            List<NeedMethod> needMethods = new ArrayList<>();
            for (ExecutableElement executableElement : realElements) {
                if (executableElement.getEnclosingElement() == mElement) {
                    NeedMethod needMethod = new NeedMethod(executableElement);
                    needMethods.add(needMethod);
                }
            }
            ClassBuilder classBuilder = new ClassBuilder(processingEnvironment, (TypeElement) mElement, needMethods);
            try {
                classBuilder.generatePermissionCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
