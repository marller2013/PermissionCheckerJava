package com.marller.compiler;

import com.marller.api.OnNeed;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

public class NeedMethod {

    private ExecutableElement mExecutableElement;

    public NeedMethod(ExecutableElement methodElement){
        this.mExecutableElement=methodElement;
    }

    public List<String> getPermissionMethodNames(){
        return Arrays.asList(mExecutableElement.getAnnotation(OnNeed.class).value());
    }

    public String getMethodName(){
        return mExecutableElement.getSimpleName().toString();
    }
}
