package com.marller.compiler;

import com.marller.api.OnDenied;
import com.marller.api.OnNeverAsk;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class ClassBuilder {

    private Filer filerUtils;
    private Elements elementsUtils;
    private Types typesUtils;

    private TypeMirror typeActivity;
    private TypeMirror typeFragment;
    private TypeMirror typeSupportFragment;

    private TypeElement mTypeElement;
    private List<NeedMethod> methodElements;

    private AtomicInteger currentCode;

    private static String CALLER = "target";
    private static String REQUEST = "requestCode";
    private static String RESULT = "grantResults";

    public ClassBuilder(ProcessingEnvironment processingEnvironment, TypeElement typeElement, List<NeedMethod> methodElements) {
        this.mTypeElement = typeElement;
        this.methodElements = methodElements;
        this.filerUtils = processingEnvironment.getFiler();
        this.elementsUtils = processingEnvironment.getElementUtils();
        this.typesUtils = processingEnvironment.getTypeUtils();
        typeActivity = elementsUtils.getTypeElement("android.app.Activity").asType();
        typeFragment = elementsUtils.getTypeElement("android.app.Fragment").asType();
        try {
            typeSupportFragment = elementsUtils.getTypeElement("android.support.v4.app.Fragment").asType();
        } catch (Exception e) {
            typeSupportFragment = null;
        }
        currentCode = new AtomicInteger(1000);
    }


    public String getPackageName() {
        return elementsUtils.getPackageOf(mTypeElement).getQualifiedName().toString();
    }

    public String getFileName() {
        return mTypeElement.getQualifiedName().toString();
    }

    public String getClassName() {
        System.out.println("检测出来的包名-------->" + getPackageName());
        System.out.println("检测出来的文件名-------->" + getFileName());
        String className = getFileName().substring(getPackageName().length() + 1);
        System.out.println("检测出来的类名-------->" + className);
        int index = className.lastIndexOf(".");
        if (index > 0) {
            className = className.substring(index + 1);
        }
        System.out.println("检测出来的类名-------->" + className);

        return className;
    }


    private TypeMirror getCurrentClassType() {
        return elementsUtils.getTypeElement(getFileName()).asType();
    }

    private TypeName getTargetTypeName() {
        return TypeName.get(getCurrentClassType());
    }

    public boolean isActivity() {
        return typesUtils.isSubtype(getCurrentClassType(), typeActivity);
    }

    public boolean isFragment() {
        return typesUtils.isSubtype(getCurrentClassType(), typeFragment);
    }

    public boolean isSupportFragment() {
        return typeSupportFragment == null ? false : typesUtils.isSubtype(getCurrentClassType(), typeSupportFragment);
    }

    private <A extends Annotation> List<ExecutableElement> getChildrenWithAnnotation(Class<A> clazz) {
        List<ExecutableElement> executableElements = new ArrayList<>();
        for (Element element : mTypeElement.getEnclosedElements()) {
            if (element.getAnnotation(clazz) != null) {
                executableElements.add((ExecutableElement) element);
            }
        }
        return executableElements;
    }

    public void generatePermissionCode() throws Exception {

        String className = getClassName() + "Permission";
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);
        String checkCaller = CALLER;
        if (isActivity() || isFragment() || isSupportFragment()) {
            if (!isActivity()) {
                checkCaller = CALLER + ".getActivity()";
            }
        } else {
            throw new Exception("checkSelfPermission should only called by Activity or Fragment!");
        }

        MethodSpec.Builder requestResult = MethodSpec.methodBuilder("onRequestPermissionResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(getTargetTypeName(), CALLER)
                .addParameter(int.class, REQUEST)
                .addParameter(int[].class, RESULT)
                .addJavadoc("Please call this method in " + getClassName() + "#onRequestPermissionsResult");
        for (NeedMethod needMethod : methodElements) {
            String invokeOriginal = CALLER + "." + needMethod.getMethodName() + "()";
            String propertyPermission = "PERMISSION_" + needMethod.getMethodName().toUpperCase();
            String propertyRequest = "REQUEST_" + needMethod.getMethodName().toUpperCase();
            StringBuilder permissions = new StringBuilder();
            permissions.append("{");
            for (int i = 0; i < needMethod.getPermissionMethodNames().length; i++) {
                String permissionName = needMethod.getPermissionMethodNames()[i];
                if (i != needMethod.getPermissionMethodNames().length - 1) {
                    permissions.append("\"");
                    permissions.append(permissionName);
                    permissions.append("\"");
                    permissions.append(",");
                } else {
                    permissions.append("\"");
                    permissions.append(permissionName);
                    permissions.append("\"");
                }

            }
            permissions.append("}");
            typeSpec.addField(FieldSpec.builder(String[].class, propertyPermission, Modifier.PRIVATE, Modifier.STATIC)
                    .initializer(permissions.toString())
                    .build());
            typeSpec.addField(FieldSpec.builder(int.class, propertyRequest, Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("$L", currentCode.getAndIncrement())
                    .build());
            addPermissionMethod(needMethod,typeSpec,propertyPermission,propertyRequest,checkCaller,invokeOriginal);
            requestResult.addCode(addRequestResult(needMethod,propertyPermission,propertyRequest,invokeOriginal));
        }
        typeSpec.addMethod(requestResult.build());
//        Writer writer=filerUtils.createResource(StandardLocation.SOURCE_OUTPUT,getPackageName(),className+".java")
//                .openWriter();
        JavaFile.builder(getPackageName(), typeSpec.build()).addFileComment("generated by APT.Do not modify!").build().writeTo(filerUtils);
//        writer.close();
    }

    private void addPermissionMethod(NeedMethod needMethod, TypeSpec.Builder typeSpec, String propertyPermission,
                                     String propertyRequest, String checkCaller, String invokeOriginal) {
        MethodSpec methodSpec=MethodSpec.methodBuilder(needMethod.getMethodName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(getTargetTypeName(),CALLER)
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if($T.VERSION.SDK_INT >= $T.VERSION_CODES.M)", ClassName.get("android.os", "Build"), ClassName.get("android.os", "Build"))
                        .addStatement("boolean isDenied = false")
                        .beginControlFlow("for (int i = $L; i < " + propertyPermission + ".length; i++ )", 0)
                        .beginControlFlow("if (" + checkCaller + ".checkSelfPermission(" + propertyPermission + "[i]) == $T.PERMISSION_DENIED)", ClassName.get("android.content.pm", "PackageManager"))
                        .addStatement("isDenied = true")
                        .addStatement("break")
                        .endControlFlow()
                        .endControlFlow()
                        .beginControlFlow("if (isDenied)")
                        .addStatement(CALLER + ".requestPermissions(" + propertyPermission + "," + propertyRequest + ")")
                        .nextControlFlow("else")
                        .addStatement(invokeOriginal)
                        .endControlFlow()
                        .nextControlFlow("else")
                        .addStatement(invokeOriginal)
                        .endControlFlow()
                        .build()
                )
                .addJavadoc("please call this function replace " + getClassName() + "#" + needMethod.getMethodName())
                .build();
        typeSpec.addMethod(methodSpec);
    }

    private CodeBlock addRequestResult(NeedMethod needMethod, String propertyPermission,
                                  String propertyRequest, String invokeOriginal) {
        ExecutableElement onDenied=null;
        ExecutableElement onNeverAsk=null;
        for (ExecutableElement mExecutableElement:getChildrenWithAnnotation(OnDenied.class)) {
            if (Arrays.equals(mExecutableElement.getAnnotation(OnDenied.class).values(),needMethod.getPermissionMethodNames())) {
                onDenied=mExecutableElement;
            }
        }
        for (ExecutableElement mExecutableElement:getChildrenWithAnnotation(OnNeverAsk.class)) {
            if (Arrays.equals(mExecutableElement.getAnnotation(OnNeverAsk.class).values(),needMethod.getPermissionMethodNames())) {
                onNeverAsk=mExecutableElement;
            }
        }
        CodeBlock.Builder resultBlock = CodeBlock.builder().beginControlFlow("if (" + REQUEST + "== " + propertyRequest + ")");
        resultBlock.addStatement("$T resultCodes = new int[" + RESULT + ".length]", int[].class);
        resultBlock.beginControlFlow("for (int i = $L; i < " + RESULT + ".length; i++ )", 0);
        resultBlock.beginControlFlow("if (" + RESULT + "[i] == $T.PERMISSION_GRANTED)", ClassName.get("android.content.pm", "PackageManager"));
        resultBlock.addStatement("resultCodes[i] = " + RESULT + "[i]");
        resultBlock.endControlFlow();
        resultBlock.endControlFlow();
        resultBlock.beginControlFlow("if ($T.equals(resultCodes, " + RESULT + "))", ClassName.get("java.util", "Arrays"));
        resultBlock.addStatement(invokeOriginal);
        resultBlock.nextControlFlow("else");
        resultBlock.beginControlFlow("for (int i = $L; i < " + propertyPermission + ".length; i++ )", 0);
        resultBlock.beginControlFlow("if (!" + CALLER + ".shouldShowRequestPermissionRationale(" + propertyPermission + "[i]))");
        if (null != onNeverAsk) {
            resultBlock.addStatement(CALLER + "." + onNeverAsk.getSimpleName().toString() + "()");
        }
        resultBlock.addStatement("return");
        resultBlock.endControlFlow();
        resultBlock.endControlFlow();
        if (null != onDenied) {
            resultBlock.addStatement(CALLER + "." + onDenied.getSimpleName().toString() + "()");
        }
        resultBlock.endControlFlow();
        resultBlock.endControlFlow();
        return resultBlock.build();
    }

}