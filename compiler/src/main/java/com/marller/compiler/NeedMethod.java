package com.marller.compiler;

import com.marller.api.OnNeed;


import javax.lang.model.element.ExecutableElement;

public class NeedMethod {

    private ExecutableElement mExecutableElement;

    public NeedMethod(ExecutableElement methodElement){
        this.mExecutableElement=methodElement;
    }

    public String[] getPermissionMethodNames(){
        return mExecutableElement.getAnnotation(OnNeed.class).values();
    }

    public String getMethodName(){
        return mExecutableElement.getSimpleName().toString();
    }
}
