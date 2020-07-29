package com.thoughtworks.work.action;

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.call.CalleeMethodsTreeStructure;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.thoughtworks.work.JUnitGeneratorContext;
import com.thoughtworks.work.JUnitGeneratorFileCreator;
import com.thoughtworks.work.bean.*;
import com.thoughtworks.work.util.DateTool;
import com.thoughtworks.work.util.JUnitGeneratorUtil;
import com.thoughtworks.work.util.LogAdapter;
import com.thoughtworks.work.util.Tool;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is where the magic happens.
 *
 * @author Alex Nazimok (SCI)
 * @author Jon Osborn
 * @author By: Bryan Gilbert, July 18, 2008
 * @since <pre>Sep 3, 2003</pre>
 */
public class JUnitGeneratorActionHandler extends EditorWriteActionHandler {

    private static final Logger logger = JUnitGeneratorUtil.getLogger(JUnitGeneratorActionHandler.class);

    private static final String VIRTUAL_TEMPLATE_NAME = "junitgenerator.vm";
    public static final String BASE_API_TEST = "BaseApiTest";
    public static final String BASE_BUSINESS_TEST = "BaseBusinessTest";
    public static final String BASE_REPOSITORY_TEST = "BaseRepositoryTest";
    public static final String BASE_CLIENT_TEST = "BaseClientTest";

    private final String templateKey;

    private static final Pattern IS_GET_SET = Pattern.compile("^(is|get|set)(.*)");

    private final Tool tool = new Tool();

    private final Set<String> nowImportSet = new HashSet<>();
    private Project project;
    private final HashSet<String> nowClassConstructorParamClassNameSet = new HashSet<>();

    public JUnitGeneratorActionHandler(String name) {
        this.templateKey = name;
    }

    public String getTemplate(Project project) {
        this.project = project;
        return JUnitGeneratorUtil.getInstance(project).getTemplate(this.templateKey);
    }

    public void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext) {
        PsiJavaFile file = JUnitGeneratorUtil.getSelectedJavaFile(dataContext);
        Project project = LangDataKeys.PROJECT.getData(dataContext);
        if (file == null) {
            return;
        }
        if (this.templateKey == null || this.templateKey.trim().length() == 0 ||
                getTemplate(project) == null) {
            JOptionPane.showMessageDialog(null,
                    JUnitGeneratorUtil.getProperty("junit.generator.error.noselectedtemplate"),
                    JUnitGeneratorUtil.getProperty("junit.generator.error.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PsiClass[] psiClasses = file.getClasses();

        for (PsiClass psiClass : psiClasses) {
            nowClassConstructorParamClassNameSet.clear();
            nowImportSet.clear();
            if ((psiClass != null) && (psiClass.getQualifiedName() != null)) {
                final JUnitGeneratorContext genCtx = new JUnitGeneratorContext(dataContext, file, psiClass);
                final List<TemplateEntry> entryList = new ArrayList<TemplateEntry>();

                try {
                    List<PsiMethod> methodList = new ArrayList<PsiMethod>();
                    List<String> fieldList = new ArrayList<String>();

                    List<MethodComposite> methodCompositeList = new ArrayList<MethodComposite>();

                    buildMethodList(psiClass.getMethods(), methodList, false);
                    buildFieldList(psiClass.getFields(), fieldList);

                    ArrayList<ConstructorParam> deepConstructorParamList = createDeepConstructorParamList(psiClass, project);

                    for (ConstructorParam constructorParam : deepConstructorParamList) {
                        nowClassConstructorParamClassNameSet.add(constructorParam.getClassName());
                    }

                    processMethods(genCtx, methodList, methodCompositeList);

                    List<ConstructorParam> constructorParam = createConstructorParam(psiClass);
                    constructorParam.forEach(n -> nowImportSet.addAll(n.getImportNames()));
                    entryList.add(new TemplateEntry(genCtx.getClassName(false),
                            genCtx.getPackageName(),
                            methodCompositeList,
                            fieldList,
                            constructorParam,
                            deepConstructorParamList,
                            nowImportSet.stream().filter(n -> n.split("\\.").length != 1).collect(Collectors.toList()),
                            findClassName(BASE_API_TEST),
                            findClassName(BASE_BUSINESS_TEST),
                            findClassName(BASE_REPOSITORY_TEST),
                            findClassName(BASE_CLIENT_TEST)
                    ));
                    process(genCtx, entryList);
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }

    private String findClassName(String name) {
        PsiClass[] classesByName = PsiShortNamesCache.getInstance(project).getClassesByName(name, GlobalSearchScope.allScope(project));
        if (classesByName.length == 0) return null;
        return classesByName[0].getQualifiedName();
    }

    private PsiClass findClass(String name) {
        PsiClass[] classesByName = PsiShortNamesCache.getInstance(project).getClassesByName(name, GlobalSearchScope.allScope(project));
        if (classesByName.length == 0) return null;
        return classesByName[0];
    }

    protected ArrayList<ConstructorParam> createDeepConstructorParamList(PsiClass psiClass, Project project) {
        PsiClass baseBusinessTest = findClass(BASE_BUSINESS_TEST);
        HashSet<String> needExclude = new HashSet<>();
        if (baseBusinessTest != null) {
            for (PsiField field : baseBusinessTest.getFields()) {
                needExclude.add(getSubText(field.getType().getCanonicalText()));
            }
        }
        List<ConstructorParam> deepConstructorParamList = new ArrayList<>();
        ConstructorParam constructorParam = new ConstructorParam(
                psiClass.getQualifiedName(),
                psiClass.getName(),
                psiClass.getName().substring(0, 1).toLowerCase() + psiClass.getName().substring(1)
        );
        deepConstructorParamList.add(constructorParam);
        createDeepConstructorParam(deepConstructorParamList, psiClass, project, constructorParam);
        deepConstructorParamList.forEach(n -> {
            nowImportSet.addAll(n.getImportNames());
        });
        Collections.reverse(deepConstructorParamList);
        HashSet<String> deepConstructorParamHash = new HashSet<>();
        ArrayList<ConstructorParam> newDeepConstructorParamList = new ArrayList<>();
        deepConstructorParamList.forEach(n -> {
            if (!deepConstructorParamHash.contains(n.getClassName())) {
                deepConstructorParamHash.add(n.getClassName());
                if (!needExclude.contains(n.getClassName())) {
                    newDeepConstructorParamList.add(n);
                }
            }
        });
        return newDeepConstructorParamList;
    }

    protected void createDeepConstructorParam(List<ConstructorParam> result, PsiClass psiClass, Project project, ConstructorParam parent) {
        if (psiClass.getConstructors().length == 0) return;
        for (PsiParameter parameter : psiClass.getConstructors()[0].getParameterList().getParameters()) {
            ConstructorParam constructorParam = new ConstructorParam(
                    parameter.getType().getCanonicalText(),
                    parameter.getType().getPresentableText(),
                    parameter.getName()
            );
            result.add(constructorParam);
            PsiClass paramClass = JavaPsiFacade.getInstance(project).findClass(getSubText(parameter.getType().getCanonicalText()), GlobalSearchScope.allScope(project));
            if (paramClass == null) return;
            constructorParam.setIfInterface(paramClass.isInterface());
            if (parent != null) {
                parent.getConstructorNeedParamList().add(constructorParam);
            }
            createDeepConstructorParam(result, paramClass, project, constructorParam);
        }
    }

    protected List<ConstructorParam> createConstructorParam(PsiClass psiClass) {
        List<ConstructorParam> result = new ArrayList<>();
        if (psiClass.getConstructors().length == 0) return result;
        for (PsiParameter parameter : psiClass.getConstructors()[0].getParameterList().getParameters()) {
            ConstructorParam constructorParam = new ConstructorParam(
                    parameter.getType().getCanonicalText(),
                    parameter.getType().getPresentableText(),
                    parameter.getName()
            );
            result.add(constructorParam);
        }
        return result;
    }

    private String getSubText(String text) {
        return text.split("<")[0];
    }

    /**
     * Creates a list of methods with set and get methods combined together.
     *
     * @param genCtx              the generator context
     * @param methodList          list of methods to process
     * @param methodCompositeList the composite list
     */
    private void processMethods(JUnitGeneratorContext genCtx, List<PsiMethod> methodList, List<MethodComposite> methodCompositeList) {
        List<String> methodNames = new ArrayList<String>();
        List<MethodComposite> methodComposites;

        methodComposites = toComposites(genCtx, methodList);

        if (JUnitGeneratorUtil.getInstance(genCtx.getProject()).isGenerateForOverloadedMethods()) {
            methodComposites = updateOverloadedMethods(genCtx, methodComposites);
        }

        for (MethodComposite method : methodComposites) {
            String methodName = method.getName();

            if (JUnitGeneratorUtil.getInstance(genCtx.getProject()).isCombineGetterAndSetter() &&
                    IS_GET_SET.matcher(methodName).find()) {
                methodName = parseAccessorMutator(methodName, methodList);
            }

            if (!methodNames.contains(methodName)) {
                methodNames.add(methodName);
                method.setName(methodName);
                methodCompositeList.add(method);
            }
        }
    }

    /**
     * Create a MethodComposite object for each of the methods passed in
     *
     * @param genCtx     the context
     * @param methodList the method list
     * @return the list of methods
     */
    private List<MethodComposite> toComposites(JUnitGeneratorContext genCtx, List<PsiMethod> methodList) {

        final List<MethodComposite> compositeList = new ArrayList<MethodComposite>();

        for (PsiMethod method : methodList) {
            compositeList.add(toComposite(genCtx, method));
        }

        //now that we have the complete list, we want to see if any of the methods are overloaded with each other
        //this will find methods with the same 'name'
        for (MethodComposite composite : compositeList) {
            composite.setOverloadedMethods(findOverloadedMethods(composite, compositeList));
        }
        return compositeList;
    }

    protected List<MethodComposite> findOverloadedMethods(MethodComposite source, List<MethodComposite> list) {
        final List<MethodComposite> overloadedMethods = new ArrayList<MethodComposite>();
        for (MethodComposite method : list) {
            if (!source.equals(method) && source.getName().equals(method.getName())) {
                overloadedMethods.add(method);
            }
        }
        return overloadedMethods;
    }

    /**
     * Generate the method composite class. This method will recurse until we get to the top of the chain
     *
     * @param genCtx the generator context
     *               * @param method the method in question
     * @return the method composite object
     */
    private MethodComposite toComposite(JUnitGeneratorContext genCtx, PsiMethod method) {
        List<String> paramClassList = new ArrayList<>();
        List<String> paramNameList = new ArrayList<>();
        List<ConstructorParam> constructorParams = new ArrayList<>();
        for (PsiParameter param : method.getParameterList().getParameters()) {
            paramClassList.add(param.getType().getCanonicalText());
            paramNameList.add(param.getName());
            ConstructorParam constructorParam = new ConstructorParam(param.getType().getCanonicalText(),
                    param.getType().getPresentableText(),
                    param.getName());
            constructorParams.add(constructorParam);
            nowImportSet.addAll(constructorParam.getImportNames());
        }
        ConstructorParam returnType = new ConstructorParam(method.getReturnType().getCanonicalText(),
                method.getReturnType().getPresentableText(),
                "result");
        nowImportSet.addAll(returnType.getImportNames());
        String signature = createSignature(method);
        RestInfo restInfo = createRestInfo(method);

        List<String> reflectionCode = createReflectionCode(genCtx, method);

        //create the composite object to send to the template
        final MethodComposite composite = new MethodComposite();
        composite.setMethod(method);
        composite.setName(method.getName());
        composite.setReturnType(returnType);
        composite.setParamClasses(paramClassList);
        composite.setParamNames(paramNameList);
        composite.setConstructorParams(constructorParams);
        composite.setReflectionCode(reflectionCode);
        composite.setSignature(signature);
        composite.setRestInfo(restInfo);
        //hierarchy
        CalleeMethodsTreeStructure calleeMethodsTreeStructure = new CalleeMethodsTreeStructure(project, (PsiMember) method, HierarchyBrowserBaseEx.getScopeProject());
        CallNode callNode = new CallNode();
        callNode.setClassName(method.getContainingClass().getName());
        callNode.setMethodName(method.getName());
        callNode.setInterface(method.getContainingClass().isInterface());
        hierarchy(calleeMethodsTreeStructure, callNode, calleeMethodsTreeStructure.getBaseDescriptor());
        composite.setCallNode(callNode);
        //if the super method is not the same as us, grab the data from that also
        final PsiMethod[] superMethods = method.findSuperMethods();
        if (superMethods.length > 0) {
            composite.setBase(toComposite(genCtx, superMethods[0]));
        }
        return composite;
    }

    private void hierarchy(CalleeMethodsTreeStructure calleeMethodsTreeStructure, CallNode parentCallNode, HierarchyNodeDescriptor parent) {
        for (Object childElement : calleeMethodsTreeStructure.getChildElements(parent)) {
            HierarchyNodeDescriptor child = (HierarchyNodeDescriptor) childElement;
            PsiMethod childMethod = (PsiMethod) child.getPsiElement();
            CallNode callNode = new CallNode();
            assert childMethod != null;
            PsiClass containingClass = childMethod.getContainingClass();
            assert containingClass != null;
            if (nowClassConstructorParamClassNameSet.contains(containingClass.getQualifiedName()) && containingClass.getContainingFile().getVirtualFile().getPath().contains(project.getBasePath())) {
                callNode.setClassName(containingClass.getName());
                callNode.setMethodName(childMethod.getName());
                callNode.setInterface(containingClass.isInterface());
                for (PsiParameter parameter : childMethod.getParameterList().getParameters()) {
                    callNode.getMethodParamClassList().add(parameter.getType().getPresentableText());
                    callNode.getMethodParamNameList().add(parameter.getName());
                }
                parentCallNode.addChild(callNode);
                hierarchy(calleeMethodsTreeStructure, callNode, child);
            }
        }
    }

    private String getValue(PsiAnnotationMemberValue memberValue) {
        if (memberValue instanceof PsiLiteralExpressionImpl) {
            PsiLiteralExpressionImpl value = (PsiLiteralExpressionImpl) memberValue;
            return value.getCanonicalText();
        }
        if (memberValue instanceof PsiReferenceExpressionImpl) {
            PsiReferenceExpressionImpl value = (PsiReferenceExpressionImpl) memberValue;
            if (value.resolve().getParent() instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) value.resolve().getParent();
                nowImportSet.add(psiClass.getQualifiedName());
                nowImportSet.add("static " + psiClass.getQualifiedName() + "." + value.getCanonicalText());
            }
            return value.getCanonicalText();
        }
        if (memberValue instanceof PsiBinaryExpressionImpl) {
            PsiBinaryExpressionImpl value = (PsiBinaryExpressionImpl) memberValue;
            for (PsiElement child : value.getChildren()) {
                if (child instanceof PsiReferenceExpression) {
                    if (child.getReference().resolve().getParent() instanceof PsiClass) {
                        PsiClass psiClass = (PsiClass) child.getReference().resolve().getParent();
                        nowImportSet.add(psiClass.getQualifiedName());
                        nowImportSet.add("static " + psiClass.getQualifiedName() + "." + child.getText());
                    }
                }
            }
            return value.getText();
        } else {
            return memberValue.getText();
        }
    }
    private String getValue(PsiNameValuePair psiNameValuePair) {
        return getValue(psiNameValuePair.getValue());
    }

    private List<String> getValueList(PsiNameValuePair psiNameValuePair){
        if(psiNameValuePair.getValue() instanceof PsiArrayInitializerMemberValueImpl){
            ArrayList<String> result = new ArrayList<>();
            PsiArrayInitializerMemberValueImpl psiArrayInitializerMemberValue = (PsiArrayInitializerMemberValueImpl)psiNameValuePair.getValue();
            for (PsiElement child : psiArrayInitializerMemberValue.getChildren()) {
                if(child instanceof PsiAnnotationMemberValue){
                    result.add(getValue((PsiAnnotationMemberValue)child));
                }
            }
            return result;
        }else{
            return Collections.singletonList(getValue(psiNameValuePair));
        }
    }
    private RestInfo createRestInfo(PsiMethod method) {
        PsiAnnotation classAnnotation = method.getContainingClass().getAnnotation("org.springframework.web.bind.annotation.RequestMapping");
        if (!method.getContainingClass().isInterface() && classAnnotation == null) return new RestInfo();
        String path = "";
        if (classAnnotation != null) {
            for (PsiNameValuePair attribute : classAnnotation.getParameterList().getAttributes()) {
                if (attribute.getAttributeName().equals("value")) {
                    path = getValue(attribute);
                }
            }
        }
        RestInfo restInfo = new RestInfo();
        restInfo.setPath(path);
        for (PsiAnnotation annotation : method.getAnnotations()) {
            if (annotation.getQualifiedName().endsWith("Mapping")) {
                if (annotation.getQualifiedName().contains("Get")) {
                    restInfo.setRestMethod("GET");
                }
                if (annotation.getQualifiedName().contains("Post")) {
                    restInfo.setRestMethod("POST");
                }
                if (annotation.getQualifiedName().contains("Put")) {
                    restInfo.setRestMethod("PUT");
                }
                if (annotation.getQualifiedName().contains("Delete")) {
                    restInfo.setRestMethod("DELETE");
                }
            }
        }
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            for (PsiAnnotation annotation : parameter.getAnnotations()) {
                if (annotation.getQualifiedName().equals("org.springframework.web.bind.annotation.RequestBody")) {
                    ConstructorParam constructorParam = new ConstructorParam(
                            parameter.getType().getCanonicalText(),
                            parameter.getType().getPresentableText(),
                            parameter.getName()
                    );
                    restInfo.setBody(constructorParam);
                    nowImportSet.addAll(constructorParam.getImportNames());
                }
                if (annotation.getQualifiedName().equals("org.springframework.web.bind.annotation.RequestParam")) {
                    Boolean find = false;
                    for (PsiNameValuePair attribute : annotation.getParameterList().getAttributes()) {
                        if (attribute.getAttributeName().equals("value")) {
                            find = true;
                            restInfo.getParams().add(getValue(attribute));
                        }
                    }
                    if (!find) {
                        restInfo.getParams().add('"' + parameter.getName() + '"');
                    }
                }
                if (annotation.getQualifiedName().equals("org.springframework.web.bind.annotation.RequestHeader")) {
                    Boolean find = false;
                    for (PsiNameValuePair attribute : annotation.getParameterList().getAttributes()) {
                        if (attribute.getAttributeName().equals("value")) {
                            find = true;
                            restInfo.getHeads().add(getValue(attribute));
                        }
                    }
                    if (!find) {
                        restInfo.getHeads().add('"' + parameter.getName() + '"');
                    }
                }
            }
            if (restInfo.getRestMethod() != null && restInfo.getRestMethod().equals("GET")) {
                Boolean find = false;
                for (PsiAnnotation annotation : parameter.getAnnotations()) {
                    if (annotation.getQualifiedName().contains("org.springframework.web.bind.annotation")) {
                        find = true;
                    }
                }
                if (!find) {
                    PsiClass paramClass = JavaPsiFacade.getInstance(project).findClass(getSubText(parameter.getType().getCanonicalText()), GlobalSearchScope.allScope(project));
                    for (PsiField field : paramClass.getFields()) {
                        restInfo.getParams().add('"' + field.getName() + '"');
                    }
                }
            }
        }
        for (PsiAnnotation annotation : method.getAnnotations()) {
            if (annotation.getQualifiedName().endsWith("Mapping")) {
                for (PsiNameValuePair attribute : annotation.getParameterList().getAttributes()) {
                    if (attribute.getAttributeName().equals("value")) {
                        String value = getValue(attribute);
                        value = value.replaceAll("\"", "");
                        for (String s : value.split("/")) {
                            if (!s.isEmpty()) {
                                restInfo.getPathSegments().add(s);
                            }
                        }
                    }
                }
            }
        }
        return restInfo;
    }

    private String createSignature(PsiMethod method) {
        String signature;
        StringBuilder params = new StringBuilder();
        for (PsiParameter param : method.getParameterList().getParameters()) {
            params.append(param.getText()).append(", ");
        }

        if (params.toString().endsWith(", ")) {
            params = new StringBuilder(params.substring(0, params.length() - 2));
        }

        signature = method.getName() + "(" + params + ")";

        return signature;

    }

    private List<String> createReflectionCode(JUnitGeneratorContext genCtx, PsiMethod method) {

        StringBuilder getMethodText = new StringBuilder("\"" + method.getName() + "\"");

        for (PsiParameter param : method.getParameterList().getParameters()) {
            try {
                String className = (new StringTokenizer(param.getText(), " ")).nextToken();
                getMethodText.append(", ").append(className).append(".class");
            } catch (Throwable ignored) {

            }
        }

        List<String> reflectionCode = new ArrayList<String>();
        reflectionCode.add("/*");
        reflectionCode.add("try {");
        reflectionCode.add("   Method method = " + genCtx.getClassName(false) + ".getClass().getMethod(" + getMethodText + ");");
        reflectionCode.add("   method.setAccessible(true);");
        reflectionCode.add("   method.invoke(<Object>, <Parameters>);");
        reflectionCode.add("} catch(NoSuchMethodException e) {");
        reflectionCode.add("} catch(IllegalAccessException e) {");
        reflectionCode.add("} catch(InvocationTargetException e) {");
        reflectionCode.add("}");
        reflectionCode.add("*/");

        return reflectionCode;
    }

    private List<MethodComposite> updateOverloadedMethods(JUnitGeneratorContext context, List<MethodComposite> methodList) {

        HashMap<String, Integer> methodNameMap = new HashMap<String, Integer>();
        HashMap<String, Integer> overloadMethodNameMap = new HashMap<String, Integer>();

        for (MethodComposite method : methodList) {
            String methodName = method.getName();
            if (!methodNameMap.containsKey(methodName)) {
                methodNameMap.put(methodName, 1);
            } else {
                Integer count = methodNameMap.get(methodName);
                methodNameMap.remove(methodName);
                methodNameMap.put(methodName, count + 1);
            }
        }

        for (String key : methodNameMap.keySet()) {
            if (methodNameMap.get(key) > 1) {
                overloadMethodNameMap.put(key, methodNameMap.get(key));
            }
        }

        for (int i = 0; i < methodList.size(); i++) {

            MethodComposite method = methodList.get(i);
            String methodName = method.getName();
            if (overloadMethodNameMap.containsKey(methodName)) {
                int count = overloadMethodNameMap.get(methodName);
                overloadMethodNameMap.remove(methodName);
                overloadMethodNameMap.put(methodName, count - 1);
                methodList.set(i, mutateOverloadedMethodName(context, method, count));
            }
        }

        return methodList;
    }

    private MethodComposite mutateOverloadedMethodName(JUnitGeneratorContext context, MethodComposite method, int count) {

        StringBuilder stringToAppend = new StringBuilder();
        final String overloadType = JUnitGeneratorUtil.getInstance(context.getProject()).getListOverloadedMethodsBy();

        if (JUnitGeneratorUtil.NUMBER.equalsIgnoreCase(overloadType)) {
            stringToAppend.append(count);
        } else if (JUnitGeneratorUtil.PARAM_CLASS.equalsIgnoreCase(overloadType)) {

            if (method.getParamClasses().size() > 1) {
                stringToAppend.append("For");
            }

            for (String paramClass : method.getParamClasses()) {
                paramClass = paramClass.substring(0, 1).toUpperCase() + paramClass.substring(1);
                stringToAppend.append(paramClass);
            }
        } else if (JUnitGeneratorUtil.PARAM_NAME.equalsIgnoreCase(overloadType)) {

            if (method.getParamNames().size() > 1) {
                stringToAppend.append("For");
            }

            for (String paramName : method.getParamNames()) {
                paramName = paramName.substring(0, 1).toUpperCase() + paramName.substring(1);
                stringToAppend.append(paramName);
            }
        }

        method.setName(method.getName() + stringToAppend);

        return method;
    }

    /**
     * This method takes in an accessor or mutator method that is named using get*, set*, or is* and combines
     * the method name to provide one method name: "GetSet<BaseName>"
     *
     * @param methodName - Name of accessor or mutator method
     * @param methodList - Entire list of method using to create test
     * @return String updated method name if list contains both accessor and modifier for base name
     */
    @SuppressWarnings("unchecked")
    private String parseAccessorMutator(String methodName, List methodList) {

        String baseName;

        Matcher matcher = IS_GET_SET.matcher(methodName);
        if (matcher.find()) {
            baseName = matcher.group(2);
        } else {
            baseName = methodName;
        }
        //enumerate the method list to see if we have methods with set and is or get in them
        boolean setter = false;
        boolean getter = false;
        for (PsiMethod method : (List<PsiMethod>) methodList) {
            matcher = IS_GET_SET.matcher(method.getName());
            if (matcher.find() && baseName.equals(matcher.group(2))) {
                if ("set".equals(matcher.group(1))) {
                    setter = true;
                } else if ("is".equals(matcher.group(1)) || "get".equals(matcher.group(1))) {
                    getter = true;
                }
            }
        }
        //if we have a getter and setter, then fix the method to the same name
        if (getter && setter) {
            return "GetSet" + baseName;
        }

        return methodName;
    }

    /**
     * Builds a list of class scope fields from an array of PsiFields
     *
     * @param fields    an array of fields
     * @param fieldList list to be populated
     */
    private void buildFieldList(PsiField[] fields, List<String> fieldList) {
        for (PsiField field : fields) {
            fieldList.add(field.getName());
        }
    }

    /**
     * Builds method List from an array of PsiMethods
     *
     * @param methods    array of methods
     * @param methodList list to be populated
     * @param getPrivate boolean value, if true returns only private methods, if false only returns none private methods
     */
    private void buildMethodList(PsiMethod[] methods, List<PsiMethod> methodList, boolean getPrivate) {

        for (PsiMethod method : methods) {
            if (!method.isConstructor()) {
                PsiModifierList modifiers = method.getModifierList();

                if ((!modifiers.hasModifierProperty("private") && !getPrivate) || (modifiers.hasModifierProperty("private") && getPrivate)) {
                    methodList.add(method);
                }
            }
        }
    }

    /**
     * Sets all the needed vars in VelocityContext and
     * merges the template
     *
     * @param genCtx    the context
     * @param entryList the list of entries to go into velocity scope
     */
    protected void process(JUnitGeneratorContext genCtx, List<TemplateEntry> entryList) {
        try {
            final Properties velocityProperties = new Properties();
            //use the 'string' resource loader because the template comes from a 'string'
            velocityProperties.setProperty(VelocityEngine.RESOURCE_LOADER, "string");
            velocityProperties.setProperty("string.resource.loader.class", "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
            velocityProperties.setProperty("string.resource.loader.repository.class", "org.apache.velocity.runtime.resource.loader.StringResourceRepositoryImpl");
            velocityProperties.setProperty("string.resource.loader.repository.static", "false");
            velocityProperties.setProperty("string.resource.loader.repository.name", "JUnitGenerator");

            //create the velocity engine with an externalized resource template
            final VelocityEngine ve = new VelocityEngine(velocityProperties);
            //set our custom log adapter
            ve.setProperty("runtime.log.logsystem", new LogAdapter());
            //manage the repository and put our template in with a name
            StringResourceRepository repository = new StringResourceRepositoryImpl();
            repository.putStringResource(VIRTUAL_TEMPLATE_NAME, getTemplate(genCtx.getProject()));
            ve.setApplicationAttribute("JUnitGenerator", repository);

            //init the engine
            ve.init();

            final VelocityContext context = new VelocityContext();
            context.put("entryList", entryList);
            context.put("today", JUnitGeneratorUtil.formatDate("MM/dd/yyyy"));
            context.put("date", new DateTool());
            context.put("tool", tool);

            final Template template = ve.getTemplate(VIRTUAL_TEMPLATE_NAME);
            final StringWriter writer = new StringWriter();

            template.merge(context, writer);
            String outputFileName = (String) context.get("testClass");
            if (outputFileName == null || outputFileName.trim().length() == 0) {
                if (entryList != null && entryList.size() > 0) {
                    outputFileName = entryList.get(0).getClassName() + "Test";
                } else {
                    outputFileName = "UnknownTestCaseNameTest";
                }
            }
            ApplicationManager.getApplication()
                    .runWriteAction(
                            new JUnitGeneratorFileCreator(
                                    JUnitGeneratorUtil.resolveOutputFileName(genCtx, outputFileName),
                                    writer, genCtx));
        } catch (Exception e) {
            logger.error(e);
        }
    }

}
